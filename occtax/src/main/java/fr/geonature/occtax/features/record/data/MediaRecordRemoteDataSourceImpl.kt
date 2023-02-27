package fr.geonature.occtax.features.record.data

import fr.geonature.commons.data.entity.Nomenclature
import fr.geonature.commons.features.nomenclature.data.INomenclatureLocalDataSource
import fr.geonature.commons.features.nomenclature.error.NomenclatureException
import fr.geonature.datasync.api.IGeoNatureAPIClient
import fr.geonature.datasync.api.model.Media
import fr.geonature.occtax.features.record.error.MediaRecordException
import org.tinylog.kotlin.Logger
import retrofit2.await
import retrofit2.awaitResponse
import java.io.File
import java.util.Locale

/**
 * Default implementation of [IMediaRecordRemoteDataSource].
 *
 * @author S. Grimault
 */
class MediaRecordRemoteDataSourceImpl(
    private val geoNatureAPIClient: IGeoNatureAPIClient,
    private val nomenclatureLocalDataSource: INomenclatureLocalDataSource,
) :
    IMediaRecordRemoteDataSource {

    override suspend fun sendMediaFile(
        file: File,
        author: String,
        title: String,
        description: String
    ): Media {
        val nomenclatureForImage = getNomenclatureForImage()

        val idTableLocation =
            runCatching {
                geoNatureAPIClient.getIdTableLocation()
                    .await()
            }.onFailure {
                Logger.warn { "failed to fetch ID table location" }
            }
                .getOrNull() ?: throw MediaRecordException.SynchronizeException(file)

        val isFrenchLocale = Locale.getDefault().isO3Language == Locale.FRENCH.isO3Language

        return runCatching {
            geoNatureAPIClient.sendMediaFile(
                nomenclatureForImage.id,
                idTableLocation,
                author = author,
                titleEn = if (isFrenchLocale) null else title,
                titleFr = if (isFrenchLocale) title else null,
                descriptionEn = if (isFrenchLocale) null else description,
                descriptionFr = if (isFrenchLocale) description else null,
                file
            )
                .await()
        }.onSuccess {
            Logger.info { "media file '${file.absolutePath}' successfully synchronized" }
        }
            .onFailure {
                Logger.warn(it) { "failed to send media file '${file.absolutePath}'..." }
            }
            .getOrNull() ?: throw MediaRecordException.SynchronizeException(file)
    }

    override suspend fun deleteMediaFile(media: Media) {
        runCatching {
            geoNatureAPIClient.deleteMediaFile(media.id)
                .awaitResponse()
        }.onFailure {
            Logger.warn(it) { "failed to delete media file ${media.id}..." }
        }
            .getOrNull() ?: throw MediaRecordException.DeleteException(media)
    }

    private suspend fun getNomenclatureForImage(): Nomenclature {
        return nomenclatureLocalDataSource.getNomenclatureValuesByTypeAndTaxonomy("TYPE_MEDIA")
            .firstOrNull {
                it.defaultLabel.lowercase()
                    .contains("photo")
            }
            ?: throw NomenclatureException.NoNomenclatureFoundException("no nomenclature found matching media type 'image/*'")
    }
}