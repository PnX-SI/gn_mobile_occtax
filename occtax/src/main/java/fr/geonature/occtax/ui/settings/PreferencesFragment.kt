package fr.geonature.occtax.ui.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.widget.ListView
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import fr.geonature.commons.data.InputObserver
import fr.geonature.commons.data.Provider.buildUri
import fr.geonature.occtax.R
import fr.geonature.occtax.ui.observers.InputObserverListActivity
import fr.geonature.occtax.util.SettingsUtils.getDefaultObserverId
import java.util.Locale

/**
 * Global settings.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class PreferencesFragment : PreferenceFragmentCompat() {

    private var listener: OnPreferencesFragmentListener? = null

    private val loaderCallbacks = object : LoaderManager.LoaderCallbacks<Cursor> {
        override fun onCreateLoader(
                id: Int,
                args: Bundle?): Loader<Cursor> {

            when (id) {
                LOADER_OBSERVER -> return CursorLoader(requireContext(),
                                                       buildUri(InputObserver.TABLE_NAME,
                                                                args!!.getLong(KEY_SELECTED_OBSERVER).toString()),
                                                       arrayOf(InputObserver.COLUMN_ID,
                                                               InputObserver.COLUMN_LASTNAME,
                                                               InputObserver.COLUMN_FIRSTNAME),
                                                       null,
                                                       null,
                                                       null)
                else -> throw IllegalArgumentException()
            }
        }

        override fun onLoadFinished(
                loader: Loader<Cursor>,
                data: Cursor?) {
            if ((data != null) && data.moveToFirst()) {
                updateDefaultObserverPreference(InputObserver.fromCursor(data))
            }
            else {
                updateDefaultObserverPreference()
            }

            LoaderManager.getInstance(this@PreferencesFragment)
                .destroyLoader(LOADER_OBSERVER)
        }

        override fun onLoaderReset(loader: Loader<Cursor>) {
            // nothing to do ...
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val defaultObserverPreference = preferenceScreen.findPreference(getString(R.string.preference_category_observers_default_key))

        if (defaultObserverPreference != null) {
            loadDefaultObserver()

            defaultObserverPreference.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                startActivityForResult(InputObserverListActivity.newIntent(requireContext()),
                                       0)
                true
            }
        }

        val aboutAppVersionPreference = preferenceScreen.findPreference(getString(R.string.preference_category_about_app_version_key))

        if (aboutAppVersionPreference != null) {
            aboutAppVersionPreference.summary = listener!!.getAppVersion()
        }
    }

    override fun onCreatePreferences(
            savedInstanceState: Bundle?,
            rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is OnPreferencesFragmentListener) {
            listener = context
        }
        else {
            throw RuntimeException("$context must implement OnPreferencesFragmentListener")
        }
    }

    override fun onDetach() {
        super.onDetach()

        listener = null
    }

    override fun onActivityResult(
            requestCode: Int,
            resultCode: Int,
            data: Intent?) {
        if ((resultCode == Activity.RESULT_OK) && (data != null)) {
            val selectedInputObservers = data.getParcelableArrayListExtra<InputObserver>(InputObserverListActivity.EXTRA_SELECTED_INPUT_OBSERVERS)
            updateDefaultObserverPreference(if (selectedInputObservers.size > 0) selectedInputObservers[0] else null)
        }
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
            .initLoader(LOADER_OBSERVER,
                        Bundle().apply {
                            putLong(KEY_SELECTED_OBSERVER,
                                    defaultObserverId)
                        },
                        loaderCallbacks)
    }

    private fun updateDefaultObserverPreference(defaultObserver: InputObserver? = null) {
        val defaultObserverPreference = preferenceScreen.findPreference(getString(R.string.preference_category_observers_default_key))
                ?: return

        defaultObserverPreference.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val context = context ?: return@OnPreferenceClickListener false

            startActivityForResult(InputObserverListActivity.newIntent(context,
                                                                       ListView.CHOICE_MODE_SINGLE,
                                                                       if (defaultObserver == null) listOf() else listOf(defaultObserver)),
                                   0)
            true
        }

        val editor = PreferenceManager.getDefaultSharedPreferences(context)
            .edit()

        if (defaultObserver == null) {
            editor.remove(getString(R.string.preference_category_observers_default_key))

            defaultObserverPreference.setSummary(R.string.preference_category_observers_default_not_set)
        }
        else {
            editor.putLong(getString(R.string.preference_category_observers_default_key),
                           defaultObserver.id)

            defaultObserverPreference.summary = defaultObserver.lastname?.toUpperCase(Locale.getDefault()) + if (defaultObserver.lastname == null) "" else " " + defaultObserver.firstname
        }

        editor.apply()
    }

    /**
     * Callback used by [PreferencesFragment].
     */
    interface OnPreferencesFragmentListener {
        fun getAppVersion(): String
    }

    companion object {

        private const val LOADER_OBSERVER = 1
        private const val KEY_SELECTED_OBSERVER = "selected_observer"

        /**
         * Use this factory method to create a new instance of [PreferencesFragment].
         *
         * @return A new instance of [PreferencesFragment]
         */
        @JvmStatic
        fun newInstance() = PreferencesFragment().apply {
            arguments = Bundle()
        }
    }
}
