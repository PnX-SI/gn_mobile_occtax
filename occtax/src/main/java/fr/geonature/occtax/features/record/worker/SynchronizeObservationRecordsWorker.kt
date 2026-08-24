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
import fr.geonature.datasync.api.error.BaseApiException
import fr.geonature.datasync.auth.error.AuthException
import fr.geonature.datasync.sync.ServerStatus
import fr.geonature.occtax.features.record.domain.ObservationRecord
import fr.geonature.occtax.features.record.domain.SynchronizationStatus
import fr.geonature.occtax.features.record.repository.IObservationRecordRepository
import fr.geonature.occtax.features.record.repository.ISynchronizeObservationRecordRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.tinylog.Logger
import java.util.Date
import java.util.UUID
import kotlin.time.Duration.Companion.milliseconds

/**
 * Dedicated worker to synchronize all [ObservationRecord]s with valid status.
 *
 * @author S. Grimault
 */
@HiltWorker
class SynchronizeObservationRecordsWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val observationRecordRepository: IObservationRecordRepository,
    private val synchronizeObservationRecordRepository: ISynchronizeObservationRecordRepository
) : CoroutineWorker(
    appContext,
    workerParams
) {
    private val workManager = WorkManager.getInstance(applicationContext)

    override suspend fun doWork(): Result {
        val startTime = Date()

        val alreadyRunning = withContext(Dispatchers.IO) {
            workManager
                .getWorkInfosByTag(OBSERVATION_RECORDS_SYNC_WORKER_TAG)
                .get()
                .any { it.id != id && it.state == WorkInfo.State.RUNNING }
        }

        if (alreadyRunning) {
            Logger.warn { "already running: abort" }

            return Result.retry()
        }

        val observationRecordsToSynchronize = observationRecordRepository.readAll()
            .getOrDefault(emptyList())
            .filter { it.status == ObservationRecord.Status.TO_SYNC }

        if (observationRecordsToSynchronize.isEmpty()) {
            Logger.info { "no observation records to synchronize" }

            return Result.success(workData(state = WorkInfo.State.SUCCEEDED))
        }

        val observationRecordsSynchronized = mutableListOf<ObservationRecord>()

        observationRecordsToSynchronize.forEach { observationRecordToSync ->
            setProgress(
                workData(
                    WorkInfo.State.RUNNING,
                    observationRecordToSync.internalId,
                    ObservationRecord.Status.SYNC_IN_PROGRESS
                )
            )

            delay(500.milliseconds)

            synchronizeObservationRecordRepository.synchronize(observationRecordToSync)
                .onSuccess {
                    setProgress(
                        workData(
                            state = WorkInfo.State.RUNNING,
                            recordInternalId = observationRecordToSync.internalId,
                            recordStatus = ObservationRecord.Status.SYNC_SUCCESSFUL
                        )
                    )
                    observationRecordsSynchronized.add(it)
                }
                .onFailure {
                    Logger.warn(it) { "failed to synchronize observation record '${observationRecordToSync.internalId}'" }
                    setProgress(
                        workData(
                            state = WorkInfo.State.RUNNING,
                            recordInternalId = observationRecordToSync.internalId,
                            recordStatus = ObservationRecord.Status.SYNC_ERROR,
                            serverStatus = when (it) {
                                is BaseApiException.UnauthorizedException, is AuthException.NotConnectedException -> ServerStatus.UNAUTHORIZED
                                is BaseApiException.InternalServerException -> ServerStatus.INTERNAL_SERVER_ERROR
                                is BaseApiException.ApiException -> if (it.statusCode == 403) ServerStatus.FORBIDDEN else ServerStatus.INTERNAL_SERVER_ERROR
                                else -> ServerStatus.OK
                            }
                        )
                    )

                    // abort the current synchronization if the error is related to authentication or authorization
                    if (it is BaseApiException.UnauthorizedException || it is AuthException.NotConnectedException) {
                        return Result.failure(
                            workData(
                                state = WorkInfo.State.FAILED,
                                recordInternalId = observationRecordToSync.internalId,
                                recordStatus = ObservationRecord.Status.TO_SYNC,
                                serverStatus = ServerStatus.UNAUTHORIZED
                            )
                        )
                    }
                }

            delay(500.milliseconds)
        }

        Logger.info {
            "observation records synchronization ${if (observationRecordsSynchronized.size == observationRecordsToSynchronize.size) "successfully finished" else "finished with errors"} in ${Date().time - startTime.time}ms"
        }

        return if (observationRecordsSynchronized.size == observationRecordsToSynchronize.size) {
            Result.success(
                workData(state = WorkInfo.State.SUCCEEDED)
            )
        } else {
            Result.failure(
                workData(state = WorkInfo.State.FAILED)
            )
        }
    }

    private fun workData(
        state: WorkInfo.State,
        recordInternalId: Long? = null,
        recordStatus: ObservationRecord.Status? = null,
        serverStatus: ServerStatus = ServerStatus.OK
    ): Data {
        return workDataOf(
            KEY_WORKER_STATUS to state.ordinal,
            KEY_OBSERVATION_RECORD_INTERNAL_ID to recordInternalId,
            KEY_OBSERVATION_RECORD_STATUS to recordStatus?.ordinal,
            KEY_SERVER_STATUS to serverStatus.ordinal
        )
    }

    companion object {

        private const val KEY_WORKER_STATUS = "key_worker_status"
        private const val KEY_OBSERVATION_RECORD_INTERNAL_ID = "key_observation_record_internal_id"
        private const val KEY_OBSERVATION_RECORD_STATUS = "key_observation_record_status"
        private const val KEY_SERVER_STATUS = "key_server_status"

        private const val OBSERVATION_RECORDS_SYNC_WORKER = "observation_records_sync_worker"
        const val OBSERVATION_RECORDS_SYNC_WORKER_TAG = "observation_records_sync_worker_tag"

        /**
         * Convenience method for enqueuing unique work to this worker.
         */
        fun enqueueUniqueWork(context: Context): UUID {
            val workRequest = OneTimeWorkRequest
                .Builder(SynchronizeObservationRecordsWorker::class.java)
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
                        ExistingWorkPolicy.KEEP,
                        workRequest
                    )
            }
        }

        fun toSynchronizationStatus(workInfo: WorkInfo): SynchronizationStatus? {
            val validWorkInfo = workInfo.progress.getInt(
                KEY_WORKER_STATUS,
                -1
            )
                .takeIf { it >= 0 }
                ?.let { workInfo.progress } ?: workInfo.outputData.getInt(
                KEY_WORKER_STATUS,
                -1
            )
                .takeIf { it >= 0 }
                ?.let { workInfo.outputData } ?: return null

            val workerStatus = WorkInfo.State.entries[validWorkInfo.getInt(
                KEY_WORKER_STATUS,
                0
            )]
            val observationRecordInternalId = validWorkInfo.getLong(
                KEY_OBSERVATION_RECORD_INTERNAL_ID,
                0
            )
                .takeIf { it > 0 }
            val observationRecordStatus = validWorkInfo.getInt(
                KEY_OBSERVATION_RECORD_STATUS,
                -1
            )
                .takeIf { it >= 0 }
                ?.let { ObservationRecord.Status.entries[it] }

            val serverStatus = validWorkInfo.getInt(
                KEY_SERVER_STATUS,
                -1
            )
                .takeIf { it >= 0 }
                ?.let { ServerStatus.entries[it] }

            return if (observationRecordInternalId != null && observationRecordStatus != null && serverStatus != null) SynchronizationStatus.ObservationRecordStatus(
                state = workerStatus,
                internalId = observationRecordInternalId,
                status = observationRecordStatus,
                serverStatus = serverStatus
            ) else SynchronizationStatus.WorkerStatus(state = workerStatus)
        }
    }
}