package fr.geonature.occtax.util

import android.content.Context
import androidx.preference.Preference
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import fr.geonature.occtax.R

/**
 * Helper about application settings through [Preference].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
object SettingsUtils {

    /**
     * Gets the default dataset ID to use.
     *
     * @param context the current context
     *
     * @return the default dataset ID or `null` if not set
     */
    fun getDefaultDatasetId(context: Context): Long? {
        return getDefaultSharedPreferences(context)
            .getLong(
                context.getString(R.string.preference_category_dataset_default_key),
                0
            )
            .takeIf { it > 0 }
    }

    /**
     * Gets the default input observer ID to use.
     *
     * @param context the current context
     *
     * @return the default input observer ID or `null` if not set
     */
    fun getDefaultObserverId(context: Context): Long? {
        return getDefaultSharedPreferences(context)
            .getLong(
                context.getString(R.string.preference_category_observers_default_key),
                0
            )
            .takeIf { it > 0 }
    }
}
