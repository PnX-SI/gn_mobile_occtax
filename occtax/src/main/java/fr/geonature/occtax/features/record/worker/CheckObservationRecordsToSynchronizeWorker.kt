package fr.geonature.occtax.features.record.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager.getInstance
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import fr.geonature.occtax.R
import fr.geonature.occtax.features.record.domain.ObservationRecord
import fr.geonature.occtax.features.record.repository.IObservationRecordRepository
import org.tinylog.Logger
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import kotlin.time.toJavaDuration

/**
 * Checks [ObservationRecord]s to synchronize (i.e. with status [ObservationRecord.Status.TO_SYNC]).
 *
 * @author S. Grimault
 */
@HiltWorker
class CheckObservationRecordsToSynchronizeWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val observationRecordRepository: IObservationRecordRepository,
) : CoroutineWorker(
    appContext,
    workerParams
) {
    override suspend fun doWork(): Result {
        val observationRecordsToSynchronize = observationRecordRepository.readAll()
            .getOrDefault(emptyList())
            .filter { it.status == ObservationRecord.Status.TO_SYNC }

        val numberOfObservationRecordsToSynchronize = observationRecordsToSynchronize.size

        Logger.info { "available observation records to synchronize: $numberOfObservationRecordsToSynchronize" }

        with(NotificationManagerCompat.from(applicationContext)) {
            cancel(SYNC_NOTIFICATION_ID)

            inputData
                .getString(KEY_INTENT_CLASS_NAME)
                ?.also {
                    if (observationRecordsToSynchronize.isNotEmpty()) {
                        if (ActivityCompat.checkSelfPermission(
                                applicationContext,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                Logger.warn { "missing permission '${Manifest.permission.POST_NOTIFICATIONS}' to post notifications: abort" }
                            }

                            return@also
                        }

                        notify(
                            SYNC_NOTIFICATION_ID,
                            NotificationCompat
                                .Builder(
                                    applicationContext,
                                    inputData.getString(KEY_NOTIFICATION_CHANNEL_ID)
                                        ?: DEFAULT_CHANNEL_CHECK_OBSERVATION_RECORDS_TO_SYNCHRONIZE
                                )
                                .setContentTitle(applicationContext.getText(R.string.notification_observation_records_to_synchronize_title))
                                .setContentText(
                                    applicationContext.resources.getQuantityString(
                                        R.plurals.notification_observation_records_to_synchronize_description,
                                        numberOfObservationRecordsToSynchronize,
                                        numberOfObservationRecordsToSynchronize
                                    )
                                )
                                .setContentIntent(
                                    PendingIntent.getActivity(
                                        applicationContext,
                                        0,
                                        Intent(
                                            applicationContext,
                                            Class.forName(it)
                                        ).apply {
                                            flags =
                                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        },
                                        PendingIntent.FLAG_IMMUTABLE
                                    )
                                )
                                .setSmallIcon(fr.geonature.datasync.R.drawable.ic_sync)
                                .setNumber(numberOfObservationRecordsToSynchronize)
                                .build()
                        )
                    }
                }
        }

        return Result.success()
    }

    companion object {

        private const val DEFAULT_CHANNEL_CHECK_OBSERVATION_RECORDS_TO_SYNCHRONIZE = "channel_check_observation_records_to_synchronize"

        private const val CHECK_OBSERVATION_RECORDS_TO_SYNC_WORKER =
            "check_observation_records_to_sync_worker"
        private const val CHECK_OBSERVATION_RECORDS_TO_SYNC_WORKER_TAG =
            "check_observation_records_to_sync_worker_tag"

        private const val SYNC_NOTIFICATION_ID = 2

        private const val KEY_INTENT_CLASS_NAME = "intent_class_name"
        private const val KEY_NOTIFICATION_CHANNEL_ID = "notification_channel_id"

        /**
         * Convenience method for enqueuing periodic work to this worker.
         */
        fun enqueueUniquePeriodicWork(
            context: Context,
            notificationComponentClassIntent: Class<*>,
            notificationChannelId: String = DEFAULT_CHANNEL_CHECK_OBSERVATION_RECORDS_TO_SYNCHRONIZE,
            repeatInterval: Duration = 15.toDuration(DurationUnit.MINUTES)
        ) {
            NotificationManagerCompat
                .from(context)
                .cancel(SYNC_NOTIFICATION_ID)

            // register this notification channel with the system
            NotificationManagerCompat.from(context)
                .createNotificationChannel(
                    NotificationChannel(
                        notificationChannelId,
                        context.getText(R.string.channel_name_check_observation_records_to_synchronize),
                        NotificationManager.IMPORTANCE_LOW
                    ).apply {
                        description =
                            context.getString(R.string.channel_description_check_observation_records_to_synchronize)
                        setShowBadge(true)
                    }
                )

            getInstance(context).enqueueUniquePeriodicWork(
                CHECK_OBSERVATION_RECORDS_TO_SYNC_WORKER,
                ExistingPeriodicWorkPolicy.REPLACE,
                PeriodicWorkRequestBuilder<CheckObservationRecordsToSynchronizeWorker>(repeatInterval.toJavaDuration())
                    .addTag(CHECK_OBSERVATION_RECORDS_TO_SYNC_WORKER_TAG)
                    .setInputData(
                        Data
                            .Builder()
                            .putString(
                                KEY_INTENT_CLASS_NAME,
                                notificationComponentClassIntent.name
                            )
                            .putString(
                                KEY_NOTIFICATION_CHANNEL_ID,
                                notificationChannelId
                            )
                            .build()
                    )
                    .build()
            )
        }
    }
}