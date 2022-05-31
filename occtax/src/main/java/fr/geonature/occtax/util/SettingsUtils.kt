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
     * Gets the default input observers ID to use.
     *
     * @param context the current context
     *
     * @return the default input observers ID or empty list if not set
     */
    fun getDefaultObserversId(context: Context): List<Long> {
        return getDefaultSharedPreferences(context)
            .getStringSet(
                context.getString(R.string.preference_category_observers_default_key),
                emptySet()
            )
            ?.mapNotNull { it.toLongOrNull() }
            ?.toList()
            ?: emptyList()
    }
}
