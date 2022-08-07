package fr.geonature.occtax.ui.observers

import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import fr.geonature.commons.data.ContentProviderAuthority
import fr.geonature.commons.data.entity.InputObserver
import fr.geonature.commons.data.helper.ProviderHelper.buildUri
import fr.geonature.occtax.R
import fr.geonature.occtax.R.layout.fast_scroll_recycler_view
import javax.inject.Inject

/**
 * [Fragment] to let the user to choose an [InputObserver] from the list.
 *
 * @author S. Grimault
 */
@AndroidEntryPoint
class InputObserverListFragment : Fragment() {

    @ContentProviderAuthority
    @Inject
    lateinit var authority: String

    private lateinit var savedState: Bundle
    private var listener: OnInputObserverListFragmentListener? = null
    private var adapter: InputObserverRecyclerViewAdapter? = null

    private val loaderCallbacks = object : LoaderManager.LoaderCallbacks<Cursor> {
        override fun onCreateLoader(
            id: Int,
            args: Bundle?
        ): Loader<Cursor> {

            return when (id) {
                LOADER_OBSERVERS -> {
                    val observersFilter =
                        InputObserver.Filter().byName(args?.getString(KEY_FILTER)).build()

                    CursorLoader(
                        requireContext(),
                        buildUri(
                            authority,
                            InputObserver.TABLE_NAME
                        ),
                        null,
                        observersFilter.first,
                        observersFilter.second.map { it.toString() }.toTypedArray(),
                        InputObserver.OrderBy().byName(args?.getString(KEY_FILTER)).build()
                    )
                }

                else -> throw IllegalArgumentException()
            }
        }

        override fun onLoadFinished(
            loader: Loader<Cursor>,
            data: Cursor?
        ) {

            if (data == null) return

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

    private var actionMode: ActionMode? = null
    private val actionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(
            mode: ActionMode?,
            menu: Menu?
        ): Boolean {
            mode?.menuInflater?.inflate(
                R.menu.search,
                menu
            )

            (menu?.findItem(R.id.action_search)?.actionView as SearchView?)?.apply {
                configureSearchView(this)
            }

            return true
        }

        override fun onPrepareActionMode(
            mode: ActionMode?,
            menu: Menu?
        ): Boolean {
            val searchCriterion = savedState.getString(KEY_FILTER)

            (menu?.findItem(R.id.action_search)?.actionView as SearchView?)?.apply {
                isIconified = searchCriterion.isNullOrEmpty()
                setQuery(
                    searchCriterion,
                    false
                )
            }
            
            return !searchCriterion.isNullOrEmpty()
        }

        override fun onActionItemClicked(
            mode: ActionMode?,
            item: MenuItem?
        ): Boolean {
            return false
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            actionMode = null
            listener?.onSelectedInputObservers(adapter?.getSelectedInputObservers() ?: listOf())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        savedState = savedInstanceState ?: Bundle()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(
            fast_scroll_recycler_view,
            container,
            false
        )

        // Set the adapter
        if (view is RecyclerView) {
            adapter = InputObserverRecyclerViewAdapter(object :
                InputObserverRecyclerViewAdapter.OnInputObserverRecyclerViewAdapterListener {
                override fun onSelectedInputObservers(inputObservers: List<InputObserver>) {
                    if (adapter?.isSingleChoice() == true) {
                        listener?.onSelectedInputObservers(inputObservers)
                        return
                    }

                    updateActionMode(inputObservers)
                }

                override fun scrollToFirstSelectedItemPosition(position: Int) {
                    view.smoothScrollToPosition(position)
                }
            })
            adapter?.setChoiceMode(
                arguments?.getInt(ARG_CHOICE_MODE)
                    ?: ListView.CHOICE_MODE_SINGLE
            )
            adapter?.setSelectedInputObservers(
                arguments?.getParcelableArrayList(ARG_SELECTED_INPUT_OBSERVERS)
                    ?: listOf()
            )
                .also { updateActionMode(adapter?.getSelectedInputObservers() ?: listOf()) }

            with(view) {
                layoutManager = LinearLayoutManager(context)
                adapter = this@InputObserverListFragment.adapter
            }

            val dividerItemDecoration = DividerItemDecoration(
                view.getContext(),
                (view.layoutManager as LinearLayoutManager).orientation
            )
            view.addItemDecoration(dividerItemDecoration)

            LoaderManager.getInstance(this)
                .initLoader(
                    LOADER_OBSERVERS,
                    null,
                    loaderCallbacks
                )
        }

        return view
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(
            view,
            savedInstanceState
        )

        // we have a menu item to show in action bar
        setHasOptionsMenu(true)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(savedState.apply { putAll(outState) })
    }

    override fun onCreateOptionsMenu(
        menu: Menu,
        inflater: MenuInflater
    ) {

        super.onCreateOptionsMenu(
            menu,
            inflater
        )

        inflater.inflate(
            R.menu.search,
            menu
        )

        (menu.findItem(R.id.action_search)?.actionView as SearchView?)?.apply {
            configureSearchView(this)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                listener?.onSelectedInputObservers(emptyList())
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

    private fun configureSearchView(searchView: SearchView) {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                savedState.putString(
                    KEY_FILTER,
                    newText
                )

                LoaderManager.getInstance(this@InputObserverListFragment)
                    .restartLoader(
                        LOADER_OBSERVERS,
                        bundleOf(
                            Pair(
                                KEY_FILTER,
                                newText
                            )
                        ),
                        loaderCallbacks
                    )

                return true
            }
        })
    }

    private fun updateActionMode(inputObservers: List<InputObserver>) {
        if (inputObservers.isEmpty()) {
            actionMode?.finish()
            return
        }

        if (actionMode == null) {
            actionMode =
                (activity as AppCompatActivity?)?.startSupportActionMode(actionModeCallback)
            actionMode?.setTitle(R.string.activity_observers_title)
        }

        actionMode?.subtitle = resources.getQuantityString(
            R.plurals.action_title_item_count_selected,
            inputObservers.size,
            inputObservers.size
        )
    }

    /**
     * Callback used by [InputObserverListFragment].
     */
    interface OnInputObserverListFragmentListener {

        /**
         * Called when [InputObserver]s were been selected.
         *
         * @param inputObservers the selected [InputObserver]s
         */
        fun onSelectedInputObservers(inputObservers: List<InputObserver>)
    }

    companion object {

        private const val ARG_CHOICE_MODE = "arg_choice_mode"
        private const val ARG_SELECTED_INPUT_OBSERVERS = "arg_selected_input_observers"
        private const val LOADER_OBSERVERS = 1
        private const val KEY_FILTER = "filter"

        /**
         * Use this factory method to create a new instance of [InputObserverListFragment].
         *
         * @return A new instance of [InputObserverListFragment]
         */
        @JvmStatic
        fun newInstance(
            choiceMode: Int = ListView.CHOICE_MODE_SINGLE,
            selectedObservers: List<InputObserver> = listOf()
        ) = InputObserverListFragment().apply {
            arguments = Bundle().apply {
                putInt(
                    ARG_CHOICE_MODE,
                    choiceMode
                )
                putParcelableArrayList(
                    ARG_SELECTED_INPUT_OBSERVERS,
                    ArrayList(selectedObservers)
                )
            }
        }
    }
}
