package fr.geonature.occtax.features.record.repository

import fr.geonature.occtax.features.record.data.IObservationRecordDataSource
import fr.geonature.occtax.features.record.domain.ObservationRecord
import fr.geonature.occtax.settings.AppSettings

/**
 * Default implementation of [IObservationRecordRepository].
 *
 * @author S. Grimault
 */
class ObservationRecordRepositoryImpl(private val observationRecordDataSource: IObservationRecordDataSource) :
    IObservationRecordRepository {

    override suspend fun readAll(): Result<List<ObservationRecord>> {
        return runCatching { observationRecordDataSource.readAll() }
    }

    override suspend fun read(id: Long): Result<ObservationRecord> {
        return runCatching { observationRecordDataSource.read(id) }
    }

    override suspend fun save(
        observationRecord: ObservationRecord,
        status: ObservationRecord.Status
    ): Result<ObservationRecord> {
        return runCatching {
            observationRecordDataSource.save(
                observationRecord,
                status
            )
        }
    }

    override suspend fun delete(id: Long): Result<ObservationRecord> {
        return runCatching { observationRecordDataSource.delete(id) }
    }

    override suspend fun export(id: Long, settings: AppSettings?): Result<ObservationRecord> {
        return runCatching {
            observationRecordDataSource.export(
                id,
                settings
            )
        }
    }

    override suspend fun export(
        observationRecord: ObservationRecord,
        settings: AppSettings?
    ): Result<ObservationRecord> {
        return runCatching {
            observationRecordDataSource.export(
                observationRecord,
                settings
            )
        }
    }
}