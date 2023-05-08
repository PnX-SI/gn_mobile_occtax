package fr.geonature.occtax.features.record.repository

import fr.geonature.occtax.features.record.domain.ObservationRecord
import fr.geonature.occtax.features.record.error.ObservationRecordException

/**
 * Synchronize observation record.
 *
 * @author S. Grimault
 */
interface ISynchronizeObservationRecordRepository {

    /**
     * Performs synchronization of given [ObservationRecord].
     * Returns [ObservationRecordException.InvalidStatusException] if this [ObservationRecord] has a
     * wrong status.
     */
    suspend fun synchronize(observationRecord: ObservationRecord): Result<ObservationRecord>
}