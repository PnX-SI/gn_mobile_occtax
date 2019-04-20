package fr.geonature.occtax.ui.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import fr.geonature.commons.data.InputObserver
import fr.geonature.commons.data.Provider.buildUri
import fr.geonature.occtax.R.string
import fr.geonature.occtax.R.xml
import fr.geonature.occtax.ui.observers.InputObserverListActivity

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
                updateDefaultObserverPreference(null)
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

        val defaultObserverPreference = preferenceScreen.findPreference(getString(string.preference_category_observers_default_key))

        if (defaultObserverPreference != null) {
            loadDefaultObserver()

            defaultObserverPreference.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                startActivityForResult(InputObserverListActivity.newIntent(requireContext()), 0)
                true
            }
        }

        val aboutAppVersionPreference = preferenceScreen.findPreference(getString(string.preference_category_about_app_version_key))

        if (aboutAppVersionPreference != null) {
            aboutAppVersionPreference.summary = listener!!.getAppVersion()
        }
    }

    override fun onCreatePreferences(
            savedInstanceState: Bundle?,
            rootKey: String?) {
        addPreferencesFromResource(xml.preferences)
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
            val selectedInputObserver = data.getParcelableExtra<InputObserver>(
                    InputObserverListActivity.EXTRA_SELECTED_OBSERVER)
            updateDefaultObserverPreference(selectedInputObserver)
        }
    }

    private fun loadDefaultObserver() {
        val defaultObserverId = PreferenceManager.getDefaultSharedPreferences(requireContext())
                .getLong(getString(string.preference_category_observers_default_key), 0)

        val args = Bundle()
        args.putLong(KEY_SELECTED_OBSERVER, defaultObserverId)

        LoaderManager.getInstance(this)
                .initLoader(LOADER_OBSERVER, args, loaderCallbacks)
    }

    private fun updateDefaultObserverPreference(defaultObserver: InputObserver?) {
        val defaultObserverPreference = preferenceScreen.findPreference(getString(string.preference_category_observers_default_key))

        if (defaultObserverPreference != null) {
            val editor = PreferenceManager.getDefaultSharedPreferences(context)
                    .edit()

            if (defaultObserver == null) {
                editor.remove(getString(string.preference_category_observers_default_key))

                defaultObserverPreference.setSummary(string.preference_category_observers_default_not_set)
            }
            else {
                editor.putLong(getString(string.preference_category_observers_default_key),
                        defaultObserver.id)

                defaultObserverPreference.summary = defaultObserver.lastname?.toUpperCase() + if (defaultObserver.lastname == null) "" else " " + defaultObserver.firstname
            }

            editor.apply()
        }
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
