package fr.geonature.occtax

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import fr.geonature.datasync.packageinfo.worker.CheckInputsToSynchronizeWorker
import fr.geonature.mountpoint.model.MountPoint
import fr.geonature.mountpoint.util.FileUtils
import fr.geonature.mountpoint.util.MountPointUtils
import fr.geonature.occtax.ui.home.HomeActivity
import org.tinylog.Logger
import java.io.File
import javax.inject.Inject
import kotlin.system.exitProcess

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
        configureCheckInputsToSynchronizeChannel(notificationManager)
        configureSynchronizeDataChannel(notificationManager)

        checkInputsToSynchronize()
    }

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration
            .Builder()
            .setWorkerFactory(workerFactory)
            .build()
    }

    private fun configureLogger() {
        val directoryForLogs: File = FileUtils.getFile(
            FileUtils.getRootFolder(
                this,
                MountPoint.StorageType.INTERNAL,
            ),
            "logs"
        )
            .also { it.mkdirs() }

        System.setProperty(
            "tinylog.directory",
            directoryForLogs.absolutePath
        )

        Thread.setDefaultUncaughtExceptionHandler(TinylogUncaughtExceptionHandler())

        Logger.info { "starting ${BuildConfig.APPLICATION_ID}..." }
        Logger.info { "logs directory: '$directoryForLogs'" }
    }

    private fun checkInputsToSynchronize() {
        CheckInputsToSynchronizeWorker.enqueueUniquePeriodicWork(
            this,
            HomeActivity::class.java
        )
    }

    private fun configureCheckInputsToSynchronizeChannel(notificationManager: NotificationManagerCompat): NotificationChannel {
        val channel = NotificationChannel(
            CHANNEL_CHECK_INPUTS_TO_SYNCHRONIZE,
            getText(R.string.channel_name_check_inputs_to_synchronize),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.channel_description_check_inputs_to_synchronize)
            setShowBadge(true)
        }

        // register this channel with the system
        notificationManager.createNotificationChannel(channel)

        return channel
    }

    private fun configureSynchronizeDataChannel(notificationManager: NotificationManagerCompat): NotificationChannel {
        val channel = NotificationChannel(
            CHANNEL_DATA_SYNCHRONIZATION,
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
            exitProcess(1)
        }
    }

    companion object {
        const val CHANNEL_CHECK_INPUTS_TO_SYNCHRONIZE = "channel_check_inputs_to_synchronize"
        const val CHANNEL_DATA_SYNCHRONIZATION = "channel_data_synchronization"
    }
}