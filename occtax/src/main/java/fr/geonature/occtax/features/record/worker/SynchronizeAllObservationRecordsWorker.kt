package fr.geonature.occtax.features.record.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import fr.geonature.commons.interactor.BaseResultUseCase
import fr.geonature.occtax.features.record.domain.ObservationRecord
import fr.geonature.occtax.features.record.usecase.GetAllObservationRecordsUseCase
import fr.geonature.occtax.features.record.usecase.SynchronizeObservationRecordUseCase
import kotlinx.coroutines.delay
import org.tinylog.Logger
import java.util.UUID

/**
 * Synchronizes all eligible [ObservationRecord] through dedicated worker.
 *
 * @author S. Grimault
 */
@HiltWorker
class SynchronizeAllObservationRecordsWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val getAllObservationRecordsUseCase: GetAllObservationRecordsUseCase,
    private val synchronizeObservationRecordUseCase: SynchronizeObservationRecordUseCase
) : CoroutineWorker(
    appContext,
    workerParams
) {
    override suspend fun doWork(): Result {
        val observationRecordsToSynchronize =
            getAllObservationRecordsUseCase.run(BaseResultUseCase.None())
                .getOrElse { emptyList() }
                .filter { it.status == ObservationRecord.Status.TO_SYNC }

        if (observationRecordsToSynchronize.isEmpty()) {
            setProgress(
                workData(
                    WorkInfo.State.CANCELLED
                )
            )

            Logger.info { "no observation records to synchronize" }

            return Result.success()
        }

        Logger.info { "${observationRecordsToSynchronize.size} observation record(s) to synchronize..." }

        setProgress(
            workData(
                WorkInfo.State.RUNNING,
                observationRecordsToSynchronize.size
            )
        )

        val observationRecordsSynchronized = mutableListOf<ObservationRecord>()

        observationRecordsToSynchronize.forEach {
            val result =
                synchronizeObservationRecordUseCase.run(SynchronizeObservationRecordUseCase.Params(it))

            if (result.isFailure) {
                (result.exceptionOrNull()?.message
                    ?: "failed to synchronize observation record '${it.internalId}'").also {
                    Logger.warn { it }
                }

                setProgress(
                    workData(
                        WorkInfo.State.FAILED,
                        observationRecordsToSynchronize.size - observationRecordsSynchronized.size
                    )
                )

                delay(1000)

                return@forEach
            }

            observationRecordsSynchronized.add(it)

            setProgress(
                workData(
                    WorkInfo.State.RUNNING,
                    observationRecordsToSynchronize.size - observationRecordsSynchronized.size
                )
            )
        }

        Logger.info {
            "observation records synchronization ${if (observationRecordsToSynchronize.size == observationRecordsSynchronized.size) "successfully finished" else "finished with errors"}"
        }

        return if (observationRecordsToSynchronize.size == observationRecordsSynchronized.size) {
            Result.success(
                workData(
                    WorkInfo.State.SUCCEEDED,
                    0
                )
            )
        } else {
            Result.failure(
                workData(
                    WorkInfo.State.FAILED,
                    observationRecordsToSynchronize.size - observationRecordsSynchronized.size
                )
            )
        }
    }

    private fun workData(
        state: WorkInfo.State,
        records: Int = 0
    ): Data {
        return workDataOf(
            KEY_STATUS to state.ordinal,
            KEY_RECORDS to records
        )
    }

    companion object {
        const val KEY_STATUS = "key_status"
        const val KEY_RECORDS = "key_records"

        // the name of this worker
        private const val OBSERVATION_RECORDS_SYNC_WORKER = "observation_records_sync_worker"
        const val OBSERVATION_RECORDS_SYNC_WORKER_TAG = "observation_records_sync_worker_tag"

        /**
         * Convenience method for enqueuing unique work to this worker.
         */
        fun enqueueUniqueWork(context: Context): UUID {
            val workRequest = OneTimeWorkRequest
                .Builder(SynchronizeAllObservationRecordsWorker::class.java)
                .addTag(OBSERVATION_RECORDS_SYNC_WORKER_TAG)
                .setConstraints(
                    Constraints
                        .Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()

            return workRequest.id.also {
                WorkManager.getInstance(context)
                    .enqueueUniqueWork(
                        OBSERVATION_RECORDS_SYNC_WORKER,
                        ExistingWorkPolicy.REPLACE,
                        workRequest
                    )
            }
        }
    }
}