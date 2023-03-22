package fr.geonature.occtax.features.record.repository

import android.content.Context
import fr.geonature.commons.data.entity.Nomenclature
import fr.geonature.commons.features.nomenclature.data.INomenclatureLocalDataSource
import fr.geonature.commons.settings.IAppSettingsManager
import fr.geonature.commons.settings.error.AppSettingsException
import fr.geonature.datasync.api.IGeoNatureAPIClient
import fr.geonature.datasync.api.model.AuthUser
import fr.geonature.datasync.auth.IAuthManager
import fr.geonature.datasync.auth.error.AuthException
import fr.geonature.datasync.settings.error.DataSyncSettingsNotFoundException
import fr.geonature.occtax.R
import fr.geonature.occtax.features.record.data.IMediaRecordLocalDataSource
import fr.geonature.occtax.features.record.data.IObservationRecordLocalDataSource
import fr.geonature.occtax.features.record.data.IObservationRecordRemoteDataSource
import fr.geonature.occtax.features.record.domain.ObservationRecord
import fr.geonature.occtax.features.record.domain.TaxonRecord
import fr.geonature.occtax.features.record.error.ObservationRecordException
import fr.geonature.occtax.settings.AppSettings
import org.tinylog.kotlin.Logger
import retrofit2.await
import retrofit2.awaitResponse
import java.text.DateFormat
import java.util.Date
import java.util.Locale

/**
 * Default implementation of [ISynchronizeObservationRecordRepository].
 *
 * @author S. Grimault
 */
class SynchronizeObservationRecordRepositoryImpl(
    private val context: Context,
    private val geoNatureAPIClient: IGeoNatureAPIClient,
    private val authManager: IAuthManager,
    private val appSettingsManager: IAppSettingsManager<AppSettings>,
    private val nomenclatureLocalDataSource: INomenclatureLocalDataSource,
    private val observationRecordLocalDataSource: IObservationRecordLocalDataSource,
    private val observationRecordRemoteDataSource: IObservationRecordRemoteDataSource,
    private val mediaRecordLocalDataSource: IMediaRecordLocalDataSource
) : ISynchronizeObservationRecordRepository {

    override suspend fun synchronize(observationRecord: ObservationRecord): Result<ObservationRecord> {
        Logger.info {
            "synchronize observation record '${observationRecord.internalId}'..."
        }

        val authUser = authManager.getAuthLogin()?.user
            ?: return Result.failure(AuthException.NotConnectedException)

        val appSettings = appSettingsManager.loadAppSettings()
            ?: return Result.failure(AppSettingsException.NoAppSettingsFoundLocallyException)

        val dataSyncSettings = appSettings.dataSyncSettings
            ?: return Result.failure(DataSyncSettingsNotFoundException())

        observationRecordRemoteDataSource.setBaseUrl(dataSyncSettings.geoNatureServerUrl)

        if (observationRecord.status != ObservationRecord.Status.TO_SYNC) {
            return Result.failure(
                ObservationRecordException.InvalidStatusException(observationRecord.internalId)
            )
        }

        val nomenclatureForImage = getNomenclatureForImage()

        val idTableLocation =
            runCatching {
                geoNatureAPIClient.getIdTableLocation()
                    .await()
            }.onFailure {
                Logger.warn {
                    "failed to fetch ID table location"
                }
            }
                .getOrNull()

        val observationRecordSent = runCatching {
            observationRecordRemoteDataSource.sendObservationRecord(
                observationRecord,
                appSettings
            )
        }.onFailure {
            Logger.error {
                "failed to synchronize observation record '${observationRecord.internalId}'"
            }
        }
            .getOrElse {
                return Result.failure(it)
            }

        Logger.info {
            "observation record created from GeoNature: '${observationRecordSent.id}'"
        }
        Logger.info {
            "synchronize ${observationRecordSent.taxa.taxa.size} taxa from observation record '${observationRecord.internalId}'..."
        }

        if (nomenclatureForImage != null && idTableLocation != null) {
            observationRecord.taxa.taxa.forEach {
                synchronizeMediaFiles(
                    authUser,
                    it,
                    nomenclatureForImage,
                    idTableLocation
                )
            }
        }

        runCatching {
            observationRecordRemoteDataSource.sendTaxaRecords(
                observationRecordSent,
                appSettings
            )
        }.onFailure {
            Logger.error {
                "failed to synchronize all taxa from observation record '${observationRecord.internalId}'"
            }
            Logger.info {
                "deleting observation record '${observationRecordSent.id}' from GeoNature..."
            }

            deleteAllSynchronizedMediaFiles(observationRecordSent)

            runCatching {
                observationRecordRemoteDataSource.deleteObservationRecord(observationRecordSent)
            }.onFailure {
                Logger.warn {
                    "failed to delete observation record '${observationRecordSent.id}' from GeoNature"
                }
            }
        }
            .getOrElse {
                return Result.failure(it)
            }

        Logger.info {
            "observation record '${observationRecord.internalId}' successfully synchronized"
        }

        runCatching { observationRecordLocalDataSource.delete(observationRecord.internalId) }.onFailure {
            Logger.warn {
                "failed to delete a fully synchronized observation record '${observationRecord.internalId}'"
            }
        }

        return Result.success(observationRecordSent)
    }

    private suspend fun getNomenclatureForImage(): Nomenclature? {
        val nomenclatures = runCatching {
            nomenclatureLocalDataSource.getNomenclatureValuesByTypeAndTaxonomy("TYPE_MEDIA")
        }.getOrNull()

        if (nomenclatures == null) {
            Logger.warn {
                "'TYPE_MEDIA' nomenclature type not found"
            }

            return null
        }

        val nomenclatureForImage = nomenclatures.firstOrNull {
            it.defaultLabel.lowercase()
                .contains("photo")
        }

        if (nomenclatureForImage == null) {
            Logger.warn {
                "no nomenclature found matching media type 'image/*'"
            }

            return null
        }

        return nomenclatureForImage
    }

    /**
     * Synchronize all media files from given counting linked to given taxon.
     */
    private suspend fun synchronizeMediaFiles(
        authUser: AuthUser,
        taxonRecord: TaxonRecord,
        nomenclatureForImage: Nomenclature,
        idTableLocation: Long
    ) {
        val isFrenchLocale = Locale.getDefault().isO3Language == Locale.FRENCH.isO3Language

        taxonRecord.counting.counting.forEach { countingRecord ->
            countingRecord.medias.medias = mediaRecordLocalDataSource.loadAll(
                taxonRecord,
                countingRecord
            )
                .mapNotNull { file ->
                    val modifiedAt = DateFormat.getDateInstance(DateFormat.MEDIUM)
                        .format(Date(file.lastModified()))
                    val mediaTitle = context.getString(
                        R.string.counting_media_title,
                        taxonRecord.taxon.name,
                        modifiedAt
                    )
                    val mediaDescription = context.getString(
                        R.string.counting_media_description,
                        taxonRecord.taxon.name,
                        modifiedAt
                    )

                    runCatching {
                        geoNatureAPIClient.sendMediaFile(
                            nomenclatureForImage.id,
                            idTableLocation,
                            author = "${authUser.lastname.uppercase()} ${authUser.firstname.replaceFirstChar { c -> c.uppercase() }}",
                            titleEn = if (isFrenchLocale) null else mediaTitle,
                            titleFr = if (isFrenchLocale) mediaTitle else null,
                            descriptionEn = if (isFrenchLocale) null else mediaDescription,
                            descriptionFr = if (isFrenchLocale) mediaDescription else null,
                            file
                        )
                            .await()
                    }.onSuccess {
                        Logger.info {
                            "media file '${file.absolutePath}' successfully synchronized"
                        }
                    }
                        .onFailure {
                            Logger.warn(it) {
                                "failed to send media file '${file.absolutePath}'..."
                            }
                        }
                        .getOrNull()
                }
        }
    }

    private suspend fun deleteAllSynchronizedMediaFiles(observationRecord: ObservationRecord) {
        Logger.info {
            "deleting already uploaded media files..."
        }

        observationRecord.taxa.taxa.forEach { taxonRecord ->
            taxonRecord.counting.counting.forEach { countingRecord ->
                countingRecord.medias.medias.forEach { media ->
                    runCatching {
                        geoNatureAPIClient.deleteMediaFile(media.id)
                            .awaitResponse()
                    }.onFailure {
                        Logger.warn(it) {
                            "failed to delete media file ${media.id}..."
                        }
                    }
                }
            }
        }
    }
}