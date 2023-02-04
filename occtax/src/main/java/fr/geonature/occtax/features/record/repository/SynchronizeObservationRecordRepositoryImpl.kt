package fr.geonature.occtax.features.record.repository

import fr.geonature.commons.settings.IAppSettingsManager
import fr.geonature.commons.settings.error.AppSettingsException
import fr.geonature.datasync.auth.IAuthManager
import fr.geonature.datasync.auth.error.AuthException
import fr.geonature.datasync.packageinfo.ISynchronizeObservationRecordRepository
import fr.geonature.datasync.settings.error.DataSyncSettingsNotFoundException
import fr.geonature.occtax.features.record.data.IObservationRecordDataSource
import fr.geonature.occtax.features.record.data.IObservationRecordRemoteDataSource
import fr.geonature.occtax.features.record.domain.ObservationRecord
import fr.geonature.occtax.features.record.error.ObservationRecordException
import fr.geonature.occtax.settings.AppSettings
import org.tinylog.kotlin.Logger

/**
 * Default implementation of [ISynchronizeObservationRecordRepository].
 *
 * @author S. Grimault
 */
class SynchronizeObservationRecordRepositoryImpl(
    private val authManager: IAuthManager,
    private val appSettingsManager: IAppSettingsManager<AppSettings>,
    private val observationRecordDataSource: IObservationRecordDataSource,
    private val observationRecordRemoteDataSource: IObservationRecordRemoteDataSource
) : ISynchronizeObservationRecordRepository {

    override suspend fun invoke(recordId: Long): Result<Unit> {
        Logger.info { "synchronize observation record '$recordId'..." }

        authManager.getAuthLogin()?.user
            ?: return Result.failure(AuthException.NotConnectedException)

        val appSettings = appSettingsManager.loadAppSettings()
            ?: return Result.failure(AppSettingsException.NoAppSettingsFoundLocallyException)

        val dataSyncSettings = appSettings.dataSyncSettings
            ?: return Result.failure(DataSyncSettingsNotFoundException())

        observationRecordRemoteDataSource.setBaseUrl(dataSyncSettings.geoNatureServerUrl)

        val observationRecord =
            runCatching { observationRecordDataSource.read(recordId) }.getOrElse {
                return Result.failure(it)
            }

        if (observationRecord.status != ObservationRecord.Status.TO_SYNC) {
            return Result.failure(ObservationRecordException.InvalidStatusException(observationRecord.internalId))
        }

        val observationRecordSent = runCatching {
            observationRecordRemoteDataSource.sendObservationRecord(
                observationRecord,
                appSettings
            )
        }.onFailure { Logger.error { "failed to synchronize observation record '$recordId'" } }
            .getOrElse {
                return Result.failure(it)
            }

        Logger.info { "observation record created from GeoNature: '${observationRecordSent.id}'" }
        Logger.info { "synchronize ${observationRecordSent.taxa.taxa.size} taxa from observation record '$recordId'..." }

        runCatching {
            observationRecordRemoteDataSource.sendTaxaRecords(
                observationRecordSent,
                appSettings
            )
        }.onFailure {
            Logger.error { "failed to synchronize all taxa from observation record '$recordId'" }
            Logger.info { "deleting observation record '${observationRecordSent.id}' from GeoNature..." }
            runCatching { observationRecordRemoteDataSource.deleteObservationRecord(observationRecordSent) }.onFailure {
                Logger.warn { "failed to delete observation record '${observationRecordSent.id}' from GeoNature" }
            }
        }
            .getOrElse {
                return Result.failure(it)
            }

        Logger.info { "observation record '$recordId' successfully synchronized" }

        runCatching { observationRecordDataSource.delete(recordId) }.onFailure {
            Logger.warn { "failed to delete a fully synchronized observation record '$recordId'" }
        }

        return Result.success(Unit)
    }
}