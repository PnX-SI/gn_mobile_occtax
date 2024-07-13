package fr.geonature.occtax.ui.input.map

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import fr.geonature.compat.os.getParcelableCompat
import fr.geonature.maps.settings.MapSettings
import fr.geonature.maps.util.MapSettingsPreferencesUtils

/**
 * Map settings.
 *
 * @author S. Grimault
 */
class MapPreferencesFragment : PreferenceFragmentCompat() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setDefaultPreferences(arguments?.getParcelableCompat(ARG_MAP_SETTINGS))
    }

    override fun onCreatePreferences(
        savedInstanceState: Bundle?,
        rootKey: String?
    ) {
        addPreferencesFromResource(fr.geonature.maps.R.xml.map_preferences)
    }

    private fun setDefaultPreferences(appSettings: MapSettings?) {
        val context = context ?: return

        MapSettingsPreferencesUtils.setDefaultPreferences(
            context,
            MapSettings.Builder()
                .from(appSettings)
                .build(),
            preferenceScreen
        )
    }

    companion object {

        private const val ARG_MAP_SETTINGS = "arg_map_settings"

        /**
         * Use this factory method to create a new instance of [MapPreferencesFragment].
         *
         * @return A new instance of [MapPreferencesFragment]
         */
        @JvmStatic
        fun newInstance(mapSettings: MapSettings? = null) = MapPreferencesFragment().apply {
            arguments = Bundle().apply {
                putParcelable(
                    ARG_MAP_SETTINGS,
                    mapSettings
                )
            }
        }
    }
}
