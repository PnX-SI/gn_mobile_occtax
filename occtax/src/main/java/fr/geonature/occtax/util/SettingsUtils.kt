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

    /**
     * Whether to show the map scale (default: `true`).
     *
     * @param context the current context
     *
     * @return `true` if the map scale should be shown
     */
    fun getMapShowScale(context: Context): Boolean {
        return getDefaultSharedPreferences(context)
            .getBoolean(
                context.getString(R.string.preference_category_map_show_scale_key),
                true
            )
    }

    /**
     * Whether to show north compass during map rotation (default: `true`).
     *
     * @param context the current context
     *
     * @return `true` if the map compass should be shown
     */
    fun getMapShowCompass(context: Context): Boolean {
        return getDefaultSharedPreferences(context)
            .getBoolean(
                context.getString(R.string.preference_category_map_show_compass_key),
                true
            )
    }
}
