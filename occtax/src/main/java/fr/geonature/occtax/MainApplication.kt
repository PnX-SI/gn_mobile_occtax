package fr.geonature.occtax

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import fr.geonature.commons.util.getFile
import fr.geonature.commons.util.getPrimaryExternalStorage
import fr.geonature.datasync.sync.worker.DataSyncWorker
import fr.geonature.mountpoint.util.MountPointUtils
import fr.geonature.occtax.features.record.worker.CheckObservationRecordsToSynchronizeWorker
import fr.geonature.occtax.ui.home.HomeActivity
import org.tinylog.Level
import org.tinylog.Logger
import org.tinylog.provider.ProviderRegistry
import javax.inject.Inject

/**
 * Base class to maintain global application state.
 *
 * @author S. Grimault
 */
@HiltAndroidApp
class MainApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()

        configureLogger()

        Logger.info {
            "internal storage: '${MountPointUtils.getInternalStorage(this)}'"
        }
        Logger.info {
            "external storage: '${MountPointUtils.getExternalStorage(this)}'"
        }

        val notificationManager = NotificationManagerCompat.from(this)
        configureSynchronizeDataChannel(notificationManager)

        checkObservationRecordsToSynchronize()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration
            .Builder()
            .setWorkerFactory(workerFactory)
            .build()

    private fun configureLogger() {
        System.setProperty(
            "tinylog.directory",
            applicationContext
                .getPrimaryExternalStorage()
                .getFile("logs")
                .absolutePath
        )
        System.setProperty(
            "tinylog.level",
            if (BuildConfig.DEBUG) Level.DEBUG.name.lowercase() else Level.INFO.name.lowercase()
        )

        Thread.setDefaultUncaughtExceptionHandler(TinylogUncaughtExceptionHandler())

        Logger.info { "starting ${BuildConfig.APPLICATION_ID} (version ${BuildConfig.VERSION_NAME})..." }
        Logger.info { "logs directory: '${System.getProperty("tinylog.directory")}'" }
        Logger.info { "logs level: '${System.getProperty("tinylog.level")}'" }
    }

    private fun checkObservationRecordsToSynchronize() {
        CheckObservationRecordsToSynchronizeWorker.enqueueUniquePeriodicWork(
            this,
            HomeActivity::class.java
        )
    }

    private fun configureSynchronizeDataChannel(notificationManager: NotificationManagerCompat): NotificationChannel {
        val channel = NotificationChannel(
            DataSyncWorker.DEFAULT_CHANNEL_DATA_SYNCHRONIZATION,
            getText(R.string.channel_name_data_synchronization),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.channel_description_data_synchronization)
        }

        // register this channel with the system
        notificationManager.createNotificationChannel(channel)

        return channel
    }

    private class TinylogUncaughtExceptionHandler : Thread.UncaughtExceptionHandler {
        override fun uncaughtException(thread: Thread, ex: Throwable) {
            Logger.error(ex)
            ProviderRegistry.getLoggingProvider()
                .shutdown()
            android.os.Process.killProcess(android.os.Process.myPid())
        }
    }
}