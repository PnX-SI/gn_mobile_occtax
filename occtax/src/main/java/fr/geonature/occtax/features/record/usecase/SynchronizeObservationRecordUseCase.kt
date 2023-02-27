package fr.geonature.occtax.features.record.usecase

import fr.geonature.commons.interactor.BaseResultUseCase
import fr.geonature.commons.settings.IAppSettingsManager
import fr.geonature.commons.settings.error.AppSettingsException
import fr.geonature.datasync.settings.error.DataSyncSettingsNotFoundException
import fr.geonature.occtax.features.record.data.IObservationRecordRemoteDataSource
import fr.geonature.occtax.features.record.domain.ObservationRecord
import fr.geonature.occtax.features.record.error.ObservationRecordException
import fr.geonature.occtax.features.record.repository.IMediaRecordRepository
import fr.geonature.occtax.features.record.repository.IObservationRecordRepository
import fr.geonature.occtax.settings.AppSettings
import org.tinylog.kotlin.Logger
import javax.inject.Inject

/**
 * Synchronize an [ObservationRecord].
 *
 * @author S. Grimault
 */
class SynchronizeObservationRecordUseCase @Inject constructor(
    private val appSettingsManager: IAppSettingsManager<AppSettings>,
    private val observationRecordRemoteDataSource: IObservationRecordRemoteDataSource,
    private val observationRecordRepository: IObservationRecordRepository,
    private val mediaRecordRepository: IMediaRecordRepository
) :
    BaseResultUseCase<Unit, SynchronizeObservationRecordUseCase.Params>() {

    override suspend fun run(params: Params): Result<Unit> {
        val observationRecord = params.observationRecord

        Logger.info { "synchronize observation record '${observationRecord.internalId}'..." }

        if (observationRecord.status != ObservationRecord.Status.TO_SYNC) {
            return Result.failure(ObservationRecordException.InvalidStatusException(observationRecord.internalId))
        }

        val appSettings = appSettingsManager.loadAppSettings()
            ?: return Result.failure(AppSettingsException.NoAppSettingsFoundLocallyException)
        val dataSyncSettings = appSettings.dataSyncSettings
            ?: return Result.failure(DataSyncSettingsNotFoundException())

        observationRecordRemoteDataSource.setBaseUrl(dataSyncSettings.geoNatureServerUrl)

        val observationRecordSent = runCatching {
            observationRecordRemoteDataSource.sendObservationRecord(
                observationRecord,
                appSettings
            )
        }.onFailure { Logger.error { "failed to synchronize observation record '${observationRecord.internalId}'" } }
            .getOrElse {
                return Result.failure(it)
            }

        Logger.info { "observation record created from GeoNature: '${observationRecordSent.id}'" }
        Logger.info { "synchronize ${observationRecordSent.taxa.taxa.size} taxa from observation record '${observationRecord.internalId}'..." }

        observationRecord.taxa.taxa.forEach {
            mediaRecordRepository.synchronizeMediaFiles(it)
        }

        runCatching {
            observationRecordRemoteDataSource.sendTaxaRecords(
                observationRecordSent,
                appSettings
            )
        }.onFailure {
            Logger.error { "failed to synchronize all taxa from observation record '${observationRecord.internalId}'" }
            Logger.info { "deleting observation record '${observationRecordSent.id}' from GeoNature..." }

            deleteAllSynchronizedMediaFiles(observationRecordSent)

            runCatching {
                observationRecordRemoteDataSource.deleteObservationRecord(observationRecordSent)
            }.onFailure {
                Logger.warn { "failed to delete observation record '${observationRecordSent.id}' from GeoNature" }
            }
        }
            .getOrElse {
                return Result.failure(it)
            }

        Logger.info { "observation record '${observationRecord.internalId}' successfully synchronized" }

        runCatching { observationRecordRepository.delete(observationRecord.internalId) }.onFailure {
            Logger.warn { "failed to delete a fully synchronized observation record '${observationRecord.internalId}'" }
        }

        return Result.success(Unit)
    }

    private suspend fun deleteAllSynchronizedMediaFiles(observationRecord: ObservationRecord) {
        Logger.info { "deleting already uploaded media files..." }

        observationRecord.taxa.taxa.forEach { taxonRecord ->
            runCatching { mediaRecordRepository.deleteAllMediaFiles(taxonRecord) }
        }
    }

    data class Params(val observationRecord: ObservationRecord)
}