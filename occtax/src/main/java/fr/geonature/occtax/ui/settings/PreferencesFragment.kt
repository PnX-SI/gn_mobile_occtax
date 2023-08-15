package fr.geonature.occtax.ui.settings

import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.ListView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
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
import fr.geonature.compat.content.getParcelableArrayExtraCompat
import fr.geonature.compat.content.getParcelableExtraCompat
import fr.geonature.compat.os.getParcelableCompat
import fr.geonature.datasync.api.IGeoNatureAPIClient
import fr.geonature.datasync.features.settings.presentation.ConfigureServerSettingsDialogFragment
import fr.geonature.maps.settings.MapSettings
import fr.geonature.maps.util.MapSettingsPreferencesUtils
import fr.geonature.mountpoint.util.MountPointUtils
import fr.geonature.occtax.R
import fr.geonature.occtax.ui.dataset.DatasetListActivity
import fr.geonature.occtax.ui.observers.InputObserverListActivity
import fr.geonature.occtax.util.SettingsUtils.getDefaultDatasetId
import fr.geonature.occtax.util.SettingsUtils.getDefaultObserversId
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
                        args!!.getLong(KEY_SELECTED_DATASET)
                            .toString()
                    ),
                    null,
                    null,
                    null,
                    null
                )
                LOADER_OBSERVERS_IDS -> CursorLoader(
                    requireContext(),
                    buildUri(
                        authority,
                        InputObserver.TABLE_NAME,
                        args?.getLongArray(KEY_SELECTED_OBSERVER_IDS)
                            ?.joinToString(",")
                            ?: ""
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
                LOADER_OBSERVERS_IDS -> {
                    updateDefaultObserversPreference(data?.let { cursor ->
                        mutableListOf<InputObserver>().let {
                            if (cursor.moveToFirst()) {
                                while (!cursor.isAfterLast) {
                                    InputObserver.fromCursor(cursor)
                                        ?.run {
                                            it.add(this)
                                        }

                                    cursor.moveToNext()
                                }
                            }
                            it
                        }
                    } ?: emptyList())
                }
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
                    it.data?.getParcelableExtraCompat(
                        DatasetListActivity.EXTRA_SELECTED_DATASET
                    )
                )
            }
        observerResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if ((it.resultCode != Activity.RESULT_OK) || (it.data == null)) {
                    return@registerForActivityResult
                }

                updateDefaultObserversPreference(
                    it.data?.getParcelableArrayExtraCompat<InputObserver>(InputObserverListActivity.EXTRA_SELECTED_INPUT_OBSERVERS)
                        ?.toList()
                        ?: emptyList()
                )
            }

        loadDefaultDataset()
        loadDefaultObserver()
        configurePermissionsPreference()

        configureNotificationsPreferences()

        setServerUrlsPreferences(arguments?.getParcelableCompat(ARG_SERVER_URLS))
        setMapSettingsPreferences(arguments?.getParcelableCompat(ARG_MAP_SETTINGS))
        setMountPointsPreferences(preferenceScreen)
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
        addPreferencesFromResource(R.xml.preferences_notifications)
        addPreferencesFromResource(R.xml.preferences_storage)
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
            updateDefaultObserversPreference()
            return
        }

        val defaultObserversId = getDefaultObserversId(context)

        if (defaultObserversId.isEmpty()) {
            updateDefaultObserversPreference()
            return
        }

        LoaderManager.getInstance(this)
            .initLoader(
                LOADER_OBSERVERS_IDS,
                bundleOf(
                    Pair(
                        KEY_SELECTED_OBSERVER_IDS,
                        defaultObserversId.toTypedArray()
                            .toLongArray()
                    )
                ),
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

            ConfigureServerSettingsDialogFragment.newInstance(serverUrl)
                .apply {
                    setOnConfigureServerSettingsDialogFragmentListener(object :
                        ConfigureServerSettingsDialogFragment.OnConfigureServerSettingsDialogFragmentListener {
                        override fun onChanged(url: String) {
                            PreferenceManager.getDefaultSharedPreferences(serverSettingsPreference.context)
                                .edit()
                                .also {
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
            .edit()
            .also {
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
            MapSettings.Builder()
                .from(mapSettings)
                .build(),
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

    private fun updateDefaultObserversPreference(defaultObservers: List<InputObserver> = emptyList()) {
        val defaultObserverPreference: Preference =
            preferenceScreen.findPreference(getString(R.string.preference_category_observers_default_key))
                ?: return

        defaultObserverPreference.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val context = context ?: return@OnPreferenceClickListener false

            observerResultLauncher.launch(
                InputObserverListActivity.newIntent(
                    context,
                    ListView.CHOICE_MODE_MULTIPLE,
                    defaultObservers
                )
            )

            true
        }

        val editor =
            PreferenceManager.getDefaultSharedPreferences(defaultObserverPreference.context)
                .edit()

        if (defaultObservers.isEmpty()) {
            editor.remove(getString(R.string.preference_category_observers_default_key))

            defaultObserverPreference.setSummary(R.string.preference_category_observers_default_not_set)
        } else {
            editor.putStringSet(
                getString(R.string.preference_category_observers_default_key),
                defaultObservers.map { it.id.toString() }
                    .toSet()
            )
            defaultObserverPreference.summary =
                if (defaultObservers.size < 3) defaultObservers.joinToString(", ") {
                    "${it.lastname?.uppercase(Locale.getDefault())} ${it.firstname}"
                } else getString(
                    R.string.preference_category_observers_default_set_multiple,
                    defaultObservers.size
                )
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

    companion object {

        private const val ARG_SERVER_URLS = "server_urls"
        private const val ARG_MAP_SETTINGS = "arg_map_settings"

        private const val LOADER_DATASET = 1
        private const val LOADER_OBSERVERS_IDS = 2
        private const val KEY_SELECTED_DATASET = "selected_dataset"
        private const val KEY_SELECTED_OBSERVER_IDS = "selected_observer_ids"

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
