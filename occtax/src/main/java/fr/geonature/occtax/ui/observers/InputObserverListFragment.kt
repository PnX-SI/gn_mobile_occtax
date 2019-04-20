package fr.geonature.occtax.ui.observers

import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import fr.geonature.commons.data.InputObserver
import fr.geonature.commons.data.Provider.buildUri
import fr.geonature.occtax.R.layout.fragment_list_inputobserver

/**
 * [Fragment] to let the user to choose an [InputObserver] from the list.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class InputObserverListFragment : Fragment() {

    private var listener: OnInputObserverListFragmentListener? = null
    private var adapter: InputObserverRecyclerViewAdapter? = null

    private val loaderCallbacks = object : LoaderManager.LoaderCallbacks<Cursor> {
        override fun onCreateLoader(
                id: Int,
                args: Bundle?): Loader<Cursor> {

            when (id) {
                LOADER_OBSERVERS -> {
                    val selections = if (args?.getString(KEY_FILTER, null) == null) Pair(null, null)
                    else {
                        val filter = "%${args.getString(KEY_FILTER)}%"
                        Pair("(${InputObserver.COLUMN_LASTNAME} LIKE ? OR ${InputObserver.COLUMN_FIRSTNAME} LIKE ?)",
                                arrayOf(filter, filter))
                    }

                    return CursorLoader(requireContext(),
                            buildUri(InputObserver.TABLE_NAME),
                            arrayOf(InputObserver.COLUMN_ID,
                                    InputObserver.COLUMN_LASTNAME,
                                    InputObserver.COLUMN_FIRSTNAME),
                            selections.first,
                            selections.second,
                            null)
                }

                else -> throw IllegalArgumentException()
            }
        }

        override fun onLoadFinished(
                loader: Loader<Cursor>,
                data: Cursor) {
            when (loader.id) {
                LOADER_OBSERVERS -> adapter?.bind(data)
            }
        }

        override fun onLoaderReset(loader: Loader<Cursor>) {
            when (loader.id) {
                LOADER_OBSERVERS -> adapter?.bind(null)
            }
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(fragment_list_inputobserver, container,

                false)

        // Set the adapter
        if (view is RecyclerView) {
            adapter = InputObserverRecyclerViewAdapter(listener)
            with(view) {
                layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
            }
            view.adapter = adapter

            val dividerItemDecoration = DividerItemDecoration(view.getContext(),
                    (view.layoutManager as LinearLayoutManager).orientation)
            view.addItemDecoration(dividerItemDecoration)

            LoaderManager.getInstance(this)
                    .initLoader(LOADER_OBSERVERS, null, loaderCallbacks)
        }

        return view
    }

    override fun onViewCreated(
            view: View,
            savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // we have a menu item to show in action bar
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(
            menu: Menu,
            inflater: MenuInflater) {

        super.onCreateOptionsMenu(menu, inflater)

        inflater.inflate(fr.geonature.occtax.R.menu.search, menu)

        val searchItem = menu.findItem(fr.geonature.occtax.R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                // KeyboardUtils.hideSoftKeyboard(context)

                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                LoaderManager.getInstance(this@InputObserverListFragment)
                        .restartLoader(LOADER_OBSERVERS,
                                bundleOf(Pair(KEY_FILTER, newText)),
                                loaderCallbacks)

                return true
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                listener?.onSelectedObserver(null)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context !is OnInputObserverListFragmentListener) {
            throw RuntimeException("$context must implement OnInputObserverListFragmentListener")
        }

        listener = context
    }

    override fun onDetach() {
        super.onDetach()

        listener = null
    }

    companion object {

        private const val LOADER_OBSERVERS = 1
        private const val KEY_FILTER = "filter"

        /**
         * Use this factory method to create a new instance of [InputObserverListFragment].
         *
         * @return A new instance of [InputObserverListFragment]
         */
        @JvmStatic
        fun newInstance() = InputObserverListFragment()
    }

    /**
     * Callback used by [InputObserverListFragment].
     */
    interface OnInputObserverListFragmentListener {

        /**
         * Called when [InputObserver] has been selected.
         *
         * @param inputObserver the selected [InputObserver]
         */
        fun onSelectedObserver(inputObserver: InputObserver?)
    }
}
