package fr.geonature.occtax.ui.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.ListView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.PreferenceScreen
import dagger.hilt.android.AndroidEntryPoint
import fr.geonature.commons.data.ContentProviderAuthority
import fr.geonature.commons.data.entity.Dataset
import fr.geonature.commons.data.entity.InputObserver
import fr.geonature.commons.data.helper.ProviderHelper.buildUri
import fr.geonature.datasync.api.IGeoNatureAPIClient
import fr.geonature.datasync.features.settings.presentation.ConfigureServerSettingsDialogFragment
import fr.geonature.maps.settings.MapSettings
import fr.geonature.maps.util.MapSettingsPreferencesUtils
import fr.geonature.mountpoint.util.MountPointUtils
import fr.geonature.occtax.R
import fr.geonature.occtax.ui.dataset.DatasetListActivity
import fr.geonature.occtax.ui.observers.InputObserverListActivity
import fr.geonature.occtax.util.SettingsUtils.getDefaultDatasetId
import fr.geonature.occtax.util.SettingsUtils.getDefaultObserverId
import java.util.Locale
import javax.inject.Inject

/**
 * Global settings.
 *
 * @author S. Grimault
 */
@AndroidEntryPoint
class PreferencesFragment : PreferenceFragmentCompat() {

    @ContentProviderAuthority
    @Inject
    lateinit var authority: String

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
                        authority,
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
                        authority,
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

        loadDefaultDataset()
        loadDefaultObserver()
        configurePermissionsPreference()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            configureNotificationsPreferences()
        }

        setServerUrlsPreferences(arguments?.getParcelable(ARG_SERVER_URLS))
        setMapSettingsPreferences(arguments?.getParcelable(ARG_MAP_SETTINGS))
        setMountPointsPreferences(preferenceScreen)

        (preferenceScreen.findPreference(getString(R.string.preference_category_about_app_version_key)) as Preference?)?.also {
            it.summary = listener?.getAppVersion()
        }
    }

    override fun onCreatePreferences(
        savedInstanceState: Bundle?,
        rootKey: String?
    ) {
        addPreferencesFromResource(R.xml.preferences_servers)
        addPreferencesFromResource(R.xml.preferences_dataset)
        addPreferencesFromResource(R.xml.preferences_observers)
        addPreferencesFromResource(R.xml.map_preferences)
        addPreferencesFromResource(R.xml.preferences_permissions)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            addPreferencesFromResource(R.xml.preferences_notifications)
        }

        addPreferencesFromResource(R.xml.preferences_storage)
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

    private fun setServerUrlsPreferences(serverUrls: IGeoNatureAPIClient.ServerUrls?) {
        val serverSettingsPreference: Preference =
            preferenceScreen.findPreference(getString(R.string.preference_category_server_geonature_url_key))
                ?: return

        val serverUrl =
            PreferenceManager.getDefaultSharedPreferences(serverSettingsPreference.context)
                .getString(
                    getString(R.string.preference_category_server_geonature_url_key),
                    serverUrls?.geoNatureBaseUrl
                )

        serverSettingsPreference.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val supportFragmentManager =
                activity?.supportFragmentManager ?: return@OnPreferenceClickListener false

            ConfigureServerSettingsDialogFragment.newInstance(serverUrl).apply {
                setOnConfigureServerSettingsDialogFragmentListener(object :
                    ConfigureServerSettingsDialogFragment.OnConfigureServerSettingsDialogFragmentListener {
                    override fun onChanged(url: String) {
                        PreferenceManager.getDefaultSharedPreferences(serverSettingsPreference.context)
                            .edit().also {
                                it.putString(
                                    getString(R.string.preference_category_server_geonature_url_key),
                                    url
                                )

                                serverSettingsPreference.summary = url

                                it.apply()
                            }
                    }
                })
                show(
                    supportFragmentManager,
                    SERVER_SETTINGS_DIALOG_FRAGMENT
                )
            }

            true
        }

        PreferenceManager.getDefaultSharedPreferences(serverSettingsPreference.context)
            .edit().also {
                if (serverUrl.isNullOrBlank()) {
                    it.remove(getString(R.string.preference_category_server_geonature_url_key))
                    serverSettingsPreference.setSummary(R.string.preference_category_server_geonature_url_not_set)
                } else {
                    it.putString(
                        getString(R.string.preference_category_server_geonature_url_key),
                        serverUrl
                    )
                    serverSettingsPreference.summary = serverUrl
                }

                it.apply()
            }
    }

    private fun setMapSettingsPreferences(mapSettings: MapSettings?) {
        val context = context ?: return

        MapSettingsPreferencesUtils.setDefaultPreferences(
            context,
            MapSettings.Builder.newInstance().from(mapSettings).build(),
            preferenceScreen
        )
    }

    private fun setMountPointsPreferences(preferenceScreen: PreferenceScreen) {
        val context = preferenceScreen.context

        preferenceScreen.findPreference<Preference?>(context.getString(R.string.preference_category_storage_internal_key))?.summary =
            MountPointUtils.getInternalStorage(preferenceScreen.context).mountPath.absolutePath
        MountPointUtils
            .getExternalStorage(
                preferenceScreen.context,
                Environment.MEDIA_MOUNTED,
                Environment.MEDIA_MOUNTED_READ_ONLY
            )
            ?.also { mountPoint ->
                preferenceScreen
                    .findPreference<Preference?>(context.getString(R.string.preference_category_storage_external_key))
                    ?.also {
                        it.summary = mountPoint.mountPath.absolutePath
                        it.isEnabled = true
                    }
            }
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

        val editor =
            PreferenceManager.getDefaultSharedPreferences(defaultObserverPreference.context)
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

    private fun configurePermissionsPreference() {
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun configureNotificationsPreferences() {
        preferenceScreen
            .findPreference<Preference>(getString(R.string.preference_category_notifications_configure_key))
            ?.apply {
                onPreferenceClickListener = Preference.OnPreferenceClickListener {
                    startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(
                            Settings.EXTRA_APP_PACKAGE,
                            context.packageName
                        )
                    })
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

        private const val ARG_SERVER_URLS = "server_urls"
        private const val ARG_MAP_SETTINGS = "arg_map_settings"

        private const val LOADER_DATASET = 1
        private const val LOADER_OBSERVER = 2
        private const val KEY_SELECTED_DATASET = "selected_dataset"
        private const val KEY_SELECTED_OBSERVER = "selected_observer"

        private const val SERVER_SETTINGS_DIALOG_FRAGMENT = "server_settings_dialog_fragment"

        /**
         * Use this factory method to create a new instance of [PreferencesFragment].
         *
         * @return A new instance of [PreferencesFragment]
         */
        @JvmStatic
        fun newInstance(serverUrls: IGeoNatureAPIClient.ServerUrls?, mapSettings: MapSettings?) =
            PreferencesFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(
                        ARG_SERVER_URLS,
                        serverUrls
                    )
                    putParcelable(
                        ARG_MAP_SETTINGS,
                        mapSettings
                    )
                }
            }
    }
}
