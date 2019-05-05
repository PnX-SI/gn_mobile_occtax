package fr.geonature.occtax.ui.home

import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import fr.geonature.commons.data.AppSync
import fr.geonature.commons.data.Provider.buildUri
import fr.geonature.occtax.ui.settings.PreferencesFragment
import fr.geonature.occtax.ui.shared.view.ListItemActionView

/**
 * Home screen [Fragment].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class HomeFragment : Fragment() {

    private var listener: OnHomeFragmentFragmentListener? = null
    private lateinit var appSyncView: AppSyncView

    private val loaderCallbacks = object : LoaderManager.LoaderCallbacks<Cursor> {
        override fun onCreateLoader(
                id: Int,
                args: Bundle?): Loader<Cursor> {
            when (id) {
                LOADER_APP_SYNC -> return CursorLoader(requireContext(),
                                                       buildUri(AppSync.TABLE_NAME,
                                                                args!!.getString(AppSync.COLUMN_ID)!!),
                                                       arrayOf(AppSync.COLUMN_ID,
                                                               AppSync.COLUMN_LAST_SYNC,
                                                               AppSync.COLUMN_INPUTS_TO_SYNCHRONIZE),
                                                       null,
                                                       null,
                                                       null)
                else -> throw IllegalArgumentException()
            }
        }

        override fun onLoadFinished(
                loader: Loader<Cursor>,
                data: Cursor?) {

            if (data == null) return

            when (loader.id) {
                LOADER_APP_SYNC -> {
                    if (data.moveToFirst()) {
                        appSyncView.setAppSync(AppSync.fromCursor(data))
                    }
                }
            }
        }

        override fun onLoaderReset(loader: Loader<Cursor>) {
            // nothing to do...
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View {
        return inflater.inflate(fr.geonature.occtax.R.layout.home_fragment,
                                container,
                                false)
    }

    override fun onViewCreated(
            view: View,
            savedInstanceState: Bundle?) {
        super.onViewCreated(view,
                            savedInstanceState)

        setHasOptionsMenu(true)

        appSyncView = view.findViewById(fr.geonature.occtax.R.id.appSyncView)
        appSyncView.setListener(object : ListItemActionView.OnListItemActionViewListener {
            override fun onAction() {
                listener?.onStartSync()
            }
        })

        view.findViewById<View>(fr.geonature.occtax.R.id.fab)
                .setOnClickListener { listener?.onStartInput() }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is OnHomeFragmentFragmentListener) {
            listener = context
        }
        else {
            throw RuntimeException("$context must implement OnHomeFragmentFragmentListener")
        }
    }

    override fun onDetach() {
        super.onDetach()

        listener = null
    }

    override fun onCreateOptionsMenu(
            menu: Menu,
            inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu,
                                  inflater)

        inflater.inflate(fr.geonature.occtax.R.menu.settings,
                         menu)
    }

    /*
    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)

        val menuItemSettings = menu.findItem(R.id.menu_settings)
        menuItemSettings.isEnabled = true
    }
    */

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            fr.geonature.occtax.R.id.menu_settings -> {
                listener?.onShowSettings()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        LoaderManager.getInstance(this)
                .initLoader(LOADER_APP_SYNC,
                            bundleOf(AppSync.COLUMN_ID to requireContext().packageName),
                            loaderCallbacks)
    }

    /**
     * Callback used by [PreferencesFragment].
     */
    interface OnHomeFragmentFragmentListener {
        fun onShowSettings()
        fun onStartSync()
        fun onStartInput()
    }

    companion object {
        private const val LOADER_APP_SYNC = 1

        /**
         * Use this factory method to create a new instance of [HomeFragment].
         *
         * @return A new instance of [HomeFragment]
         */
        fun newInstance() = HomeFragment()
    }
}
