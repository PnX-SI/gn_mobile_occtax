package fr.geonature.occtax.ui.dataset

import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.fragment.app.Fragment
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import fr.geonature.commons.data.ContentProviderAuthority
import fr.geonature.commons.data.entity.Dataset
import fr.geonature.commons.data.helper.ProviderHelper.buildUri
import fr.geonature.compat.os.getParcelableCompat
import fr.geonature.occtax.R
import fr.geonature.occtax.R.layout.fast_scroll_recycler_view
import javax.inject.Inject

/**
 * [Fragment] to let the user to choose a [Dataset] from the list.
 *
 * @author S. Grimault
 */
@AndroidEntryPoint
class DatasetListFragment : Fragment() {

    @ContentProviderAuthority
    @Inject
    lateinit var authority: String

    private var listener: OnDatasetListFragmentListener? = null
    private var adapter: DatasetRecyclerViewAdapter? = null

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
                        "active"
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

            if (data == null) return

            when (loader.id) {
                LOADER_DATASET -> adapter?.bind(data)
            }
        }

        override fun onLoaderReset(loader: Loader<Cursor>) {
            when (loader.id) {
                LOADER_DATASET -> adapter?.bind(null)
            }
        }
    }

    private var actionMode: ActionMode? = null
    private val actionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(
            mode: ActionMode?,
            menu: Menu?
        ): Boolean {
            return true
        }

        override fun onPrepareActionMode(
            mode: ActionMode?,
            menu: Menu?
        ): Boolean {
            return false
        }

        override fun onActionItemClicked(
            mode: ActionMode?,
            item: MenuItem?
        ): Boolean {
            return false
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            actionMode = null
            listener?.onSelectedDataset(adapter?.getSelectedDataset())
        }
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
            adapter = DatasetRecyclerViewAdapter(object :
                DatasetRecyclerViewAdapter.OnDatasetRecyclerViewAdapterListener {
                override fun onSelectedDataset(dataset: Dataset) {
                    listener?.onSelectedDataset(dataset)
                }

                override fun scrollToFirstSelectedItemPosition(position: Int) {
                    view.smoothScrollToPosition(position)
                }
            })
            adapter?.setSelectedDataset(arguments?.getParcelableCompat(ARG_SELECTED_DATASET))
                .also { updateActionMode(adapter?.getSelectedDataset()) }

            with(view) {
                layoutManager = LinearLayoutManager(context)
                adapter = this@DatasetListFragment.adapter
            }

            val dividerItemDecoration = DividerItemDecoration(
                view.getContext(),
                (view.layoutManager as LinearLayoutManager).orientation
            )
            view.addItemDecoration(dividerItemDecoration)

            LoaderManager.getInstance(this)
                .initLoader(
                    LOADER_DATASET,
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                listener?.onSelectedDataset(adapter?.getSelectedDataset())
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context !is OnDatasetListFragmentListener) {
            throw RuntimeException("$context must implement OnDatasetListFragmentListener")
        }

        listener = context
    }

    override fun onDetach() {
        super.onDetach()

        listener = null
    }

    private fun updateActionMode(dataset: Dataset?) {
        if (dataset == null) {
            actionMode?.finish()
            return
        }

        if (actionMode == null) {
            actionMode =
                (activity as AppCompatActivity?)?.startSupportActionMode(actionModeCallback)
            actionMode?.setTitle(R.string.activity_dataset_title)
        }

        actionMode?.subtitle = resources.getQuantityString(
            R.plurals.action_title_item_count_selected,
            1,
            1
        )
    }

    /**
     * Callback used by [DatasetListFragment].
     */
    interface OnDatasetListFragmentListener {

        /**
         * Called when [Dataset] has been selected.
         *
         * @param dataset the selected [Dataset]
         */
        fun onSelectedDataset(dataset: Dataset?)
    }

    companion object {

        private const val ARG_SELECTED_DATASET = "arg_selected_dataset"
        private const val LOADER_DATASET = 1

        /**
         * Use this factory method to create a new instance of [DatasetListFragment].
         *
         * @return A new instance of [DatasetListFragment]
         */
        @JvmStatic
        fun newInstance(selectedDataset: Dataset?) = DatasetListFragment().apply {
            arguments = Bundle().apply {
                putParcelable(
                    ARG_SELECTED_DATASET,
                    selectedDataset
                )
            }
        }
    }
}
