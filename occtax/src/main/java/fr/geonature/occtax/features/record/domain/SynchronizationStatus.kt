package fr.geonature.occtax.features.record.domain

import androidx.work.WorkInfo

/**
 * Describes the current status of [ObservationRecord] synchronization.
 *
 * @author S. Grimault
 */
sealed class SynchronizationStatus(open val state: WorkInfo.State ) {

    /**
     * The current worker status.
     */
    data class WorkerStatus(override val state: WorkInfo.State) : SynchronizationStatus(state)

    /**
     * The current [ObservationRecord] status.
     */
    data class ObservationRecordStatus(
        override val state: WorkInfo.State,
        val internalId: Long,
        val status: ObservationRecord.Status
    ) : SynchronizationStatus(state)
}
