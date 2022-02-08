package fr.geonature.occtax.ui.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.ListView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import fr.geonature.commons.data.entity.Dataset
import fr.geonature.commons.data.entity.InputObserver
import fr.geonature.commons.data.helper.Provider.buildUri
import fr.geonature.maps.settings.MapSettings
import fr.geonature.maps.util.MapSettingsPreferencesUtils
import fr.geonature.occtax.R
import fr.geonature.occtax.settings.AppSettings
import fr.geonature.occtax.ui.dataset.DatasetListActivity
import fr.geonature.occtax.ui.observers.InputObserverListActivity
import fr.geonature.occtax.util.SettingsUtils.getDefaultDatasetId
import fr.geonature.occtax.util.SettingsUtils.getDefaultObserverId
import java.util.Locale

/**
 * Global settings.
 *
 * @author S. Grimault
 */
class PreferencesFragment : PreferenceFragmentCompat() {

    private lateinit var datasetResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var observerResultLauncher: ActivityResultLauncher<Intent>

    private var listener: OnPreferencesFragmentListener? = null

    private val loaderCallbacks = object : LoaderManager.LoaderCallbacks<Cursor> {
        override fun onCreateLoader(
            id: Int,
            args: Bundle?
        ): Loader<Cursor> {

            return when (id) {
                LOADER_DATASET -> CursorLoader(
                    requireContext(),
                    buildUri(
                        Dataset.TABLE_NAME,
                        "occtax",
                        args!!.getLong(KEY_SELECTED_DATASET).toString()
                    ),
                    null,
                    null,
                    null,
                    null
                )
                LOADER_OBSERVER -> CursorLoader(
                    requireContext(),
                    buildUri(
                        InputObserver.TABLE_NAME,
                        args!!.getLong(KEY_SELECTED_OBSERVER).toString()
                    ),
                    null,
                    null,
                    null,
                    null
                )
                else -> throw IllegalArgumentException()
            }
        }

        override fun onLoadFinished(
            loader: Loader<Cursor>,
            data: Cursor?
        ) {

            when (loader.id) {
                LOADER_DATASET -> updateDefaultDatasetPreference(
                    if ((data != null) && data.moveToFirst()) Dataset.fromCursor(
                        data
                    ) else null
                )
                LOADER_OBSERVER -> updateDefaultObserverPreference(
                    if ((data != null) && data.moveToFirst()) InputObserver.fromCursor(
                        data
                    ) else null
                )
            }

            LoaderManager.getInstance(this@PreferencesFragment)
                .destroyLoader(loader.id)
        }

        override fun onLoaderReset(loader: Loader<Cursor>) {
            // nothing to do ...
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        datasetResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if ((it.resultCode != Activity.RESULT_OK) || (it.data == null)) {
                    return@registerForActivityResult
                }

                updateDefaultDatasetPreference(
                    it.data?.getParcelableExtra(
                        DatasetListActivity.EXTRA_SELECTED_DATASET
                    )
                )
            }
        observerResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if ((it.resultCode != Activity.RESULT_OK) || (it.data == null)) {
                    return@registerForActivityResult
                }

                val selectedInputObservers =
                    it.data?.getParcelableArrayListExtra<InputObserver>(InputObserverListActivity.EXTRA_SELECTED_INPUT_OBSERVERS)
                        ?: ArrayList()
                updateDefaultObserverPreference(if (selectedInputObservers.isNotEmpty()) selectedInputObservers[0] else null)
            }

        setDefaultPreferences(arguments?.getParcelable(ARG_APP_SETTINGS))
        loadDefaultDataset()
        loadDefaultObserver()
        configurePermissions()

        (preferenceScreen.findPreference(getString(R.string.preference_category_about_app_version_key)) as Preference?)?.also {
            it.summary = listener?.getAppVersion()
        }
    }

    override fun onCreatePreferences(
        savedInstanceState: Bundle?,
        rootKey: String?
    ) {
        addPreferencesFromResource(R.xml.preferences_dataset)
        addPreferencesFromResource(R.xml.preferences_observers)
        addPreferencesFromResource(R.xml.map_preferences)
        addPreferencesFromResource(R.xml.preferences_permissions)
        addPreferencesFromResource(R.xml.preferences_about)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is OnPreferencesFragmentListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnPreferencesFragmentListener")
        }
    }

    override fun onDetach() {
        super.onDetach()

        listener = null
    }

    private fun setDefaultPreferences(appSettings: AppSettings?) {
        val context = context ?: return

        MapSettingsPreferencesUtils.setDefaultPreferences(
            context,
            MapSettings.Builder.newInstance().from(appSettings?.mapSettings).build(),
            preferenceScreen
        )
    }

    private fun loadDefaultDataset() {
        val context = context

        if (context == null) {
            updateDefaultDatasetPreference()
            return
        }

        val defaultDatasetId = getDefaultDatasetId(context)

        if (defaultDatasetId == null) {
            updateDefaultDatasetPreference()
            return
        }

        LoaderManager.getInstance(this)
            .initLoader(
                LOADER_DATASET,
                Bundle().apply {
                    putLong(
                        KEY_SELECTED_DATASET,
                        defaultDatasetId
                    )
                },
                loaderCallbacks
            )
    }

    private fun loadDefaultObserver() {
        val context = context

        if (context == null) {
            updateDefaultObserverPreference()
            return
        }

        val defaultObserverId = getDefaultObserverId(context)

        if (defaultObserverId == null) {
            updateDefaultObserverPreference()
            return
        }

        LoaderManager.getInstance(this)
            .initLoader(
                LOADER_OBSERVER,
                Bundle().apply {
                    putLong(
                        KEY_SELECTED_OBSERVER,
                        defaultObserverId
                    )
                },
                loaderCallbacks
            )
    }

    private fun updateDefaultDatasetPreference(defaultDataset: Dataset? = null) {
        val defaultDatasetPreference: Preference =
            preferenceScreen.findPreference(getString(R.string.preference_category_dataset_default_key))
                ?: return

        defaultDatasetPreference.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val context = context ?: return@OnPreferenceClickListener false

            datasetResultLauncher.launch(
                DatasetListActivity.newIntent(
                    context,
                    defaultDataset
                )
            )

            true
        }

        val editor = PreferenceManager.getDefaultSharedPreferences(defaultDatasetPreference.context)
            .edit()

        if (defaultDataset == null) {
            editor.remove(getString(R.string.preference_category_dataset_default_key))

            defaultDatasetPreference.setSummary(R.string.preference_category_dataset_default_not_set)
        } else {
            editor.putLong(
                getString(R.string.preference_category_dataset_default_key),
                defaultDataset.id
            )

            defaultDatasetPreference.summary = defaultDataset.name
        }

        editor.apply()
    }

    private fun updateDefaultObserverPreference(defaultObserver: InputObserver? = null) {
        val defaultObserverPreference: Preference =
            preferenceScreen.findPreference(getString(R.string.preference_category_observers_default_key))
                ?: return

        defaultObserverPreference.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val context = context ?: return@OnPreferenceClickListener false

            observerResultLauncher.launch(
                InputObserverListActivity.newIntent(
                    context,
                    ListView.CHOICE_MODE_SINGLE,
                    if (defaultObserver == null) listOf() else listOf(defaultObserver)
                )
            )

            true
        }

        val editor = PreferenceManager.getDefaultSharedPreferences(defaultObserverPreference.context)
            .edit()

        if (defaultObserver == null) {
            editor.remove(getString(R.string.preference_category_observers_default_key))

            defaultObserverPreference.setSummary(R.string.preference_category_observers_default_not_set)
        } else {
            editor.putLong(
                getString(R.string.preference_category_observers_default_key),
                defaultObserver.id
            )

            defaultObserverPreference.summary =
                defaultObserver.lastname?.uppercase(Locale.getDefault()) + if (defaultObserver.lastname == null) "" else " " + defaultObserver.firstname
        }

        editor.apply()
    }

    private fun configurePermissions() {
        preferenceScreen
            .findPreference<Preference>(getString(R.string.preference_category_permissions_configure_key))
            ?.apply {
                onPreferenceClickListener = Preference.OnPreferenceClickListener {
                    startActivity(
                        Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts(
                                "package",
                                it.context.packageName,
                                null
                            )
                        )
                    )

                    true
                }
            }
    }

    /**
     * Callback used by [PreferencesFragment].
     */
    interface OnPreferencesFragmentListener {
        fun getAppVersion(): String
    }

    companion object {

        private const val ARG_APP_SETTINGS = "arg_app_settings"
        private const val LOADER_DATASET = 1
        private const val LOADER_OBSERVER = 2
        private const val KEY_SELECTED_DATASET = "selected_dataset"
        private const val KEY_SELECTED_OBSERVER = "selected_observer"

        /**
         * Use this factory method to create a new instance of [PreferencesFragment].
         *
         * @return A new instance of [PreferencesFragment]
         */
        @JvmStatic
        fun newInstance(appSettings: AppSettings? = null) = PreferencesFragment().apply {
            arguments = Bundle().apply {
                putParcelable(
                    ARG_APP_SETTINGS,
                    appSettings
                )
            }
        }
    }
}
