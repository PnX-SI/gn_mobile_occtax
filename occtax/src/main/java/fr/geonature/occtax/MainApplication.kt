package fr.geonature.occtax

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp
import fr.geonature.mountpoint.util.MountPointUtils
import fr.geonature.occtax.di.ServiceLocator

/**
 * Base class to maintain global application state.
 *
 * @author S. Grimault
 */
@HiltAndroidApp
class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Log.i(
            TAG,
            "internal storage: " + MountPointUtils.getInternalStorage(this)
        )
        Log.i(
            TAG,
            "external storage: " + MountPointUtils.getExternalStorage(this)
        )
    }

    val sl = ServiceLocator(this)

    companion object {
        private val TAG = MainApplication::class.java.name
    }
}