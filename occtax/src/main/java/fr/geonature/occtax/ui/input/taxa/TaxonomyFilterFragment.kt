package fr.geonature.occtax.ui.input.taxa

import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import fr.geonature.commons.data.Taxonomy
import fr.geonature.commons.data.helper.Provider
import fr.geonature.occtax.R

/**
 * [Fragment] to let the user to apply filters on taxa list.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class TaxonomyFilterFragment : Fragment() {

    private var listener: OnTaxaFilterFragmentListener? = null
    private var adapter: TaxonomyRecyclerViewAdapter? = null

    private val loaderCallbacks = object : LoaderManager.LoaderCallbacks<Cursor> {
        override fun onCreateLoader(
            id: Int,
            args: Bundle?
        ): Loader<Cursor> {
            return when (id) {
                LOADER_TAXONOMY -> CursorLoader(
                    requireContext(),
                    Provider.buildUri(Taxonomy.TABLE_NAME),
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
            if (data == null) {
                Log.w(
                    TAG,
                    "Failed to load data from '${(loader as CursorLoader).uri}'"
                )

                return
            }

            when (loader.id) {
                LOADER_TAXONOMY -> {
                    adapter?.bind(data)
                }
            }
        }

        override fun onLoaderReset(loader: Loader<Cursor>) {
            when (loader.id) {
                LOADER_TAXONOMY -> adapter?.bind(null)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val recyclerView = inflater.inflate(
            R.layout.recycler_view,
            container,
            false
        ) as RecyclerView

        // Set the adapter
        adapter = TaxonomyRecyclerViewAdapter(object :
            TaxonomyRecyclerViewAdapter.OnTaxonomyRecyclerViewAdapterListener {
            override fun onSelectedTaxonomy(taxonomy: Taxonomy) {
                listener?.onSelectedTaxonomy(taxonomy)
            }

            override fun onNoTaxonomySelected() {
                listener?.onNoTaxonomySelected()
            }

            override fun scrollToFirstSelectedItemPosition(position: Int) {
                recyclerView.smoothScrollToPosition(position)
            }
        }).also {
            it.setSelectedTaxonomy(arguments?.getParcelable(ARG_SELECTED_TAXONOMY))
        }

        with(recyclerView) {
            layoutManager = LinearLayoutManager(context)
            adapter = this@TaxonomyFilterFragment.adapter
        }

        return recyclerView
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        LoaderManager.getInstance(this)
            .initLoader(
                LOADER_TAXONOMY,
                null,
                loaderCallbacks
            )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context !is OnTaxaFilterFragmentListener) {
            throw RuntimeException("$context must implement OnTaxaFilterFragmentListener")
        }

        listener = context
    }

    override fun onDetach() {
        super.onDetach()

        listener = null
    }

    /**
     * Callback used by [TaxonomyFilterFragment].
     */
    interface OnTaxaFilterFragmentListener {

        /**
         * Called when a [Taxonomy] has been selected.
         *
         * @param taxonomy the selected [Taxonomy]
         */
        fun onSelectedTaxonomy(taxonomy: Taxonomy)

        /**
         * Called when no [Taxonomy] has been selected.
         */
        fun onNoTaxonomySelected()
    }

    companion object {

        private val TAG = TaxonomyFilterFragment::class.java.name
        private const val ARG_SELECTED_TAXONOMY = "arg_selected_taxonomy"

        private const val LOADER_TAXONOMY = 1

        /**
         * Use this factory method to create a new instance of [TaxonomyFilterFragment].
         *
         * @return A new instance of [TaxonomyFilterFragment]
         */
        @JvmStatic
        fun newInstance(selectedTaxonomy: Taxonomy? = null) = TaxonomyFilterFragment().apply {
            arguments = Bundle().apply { putParcelable(ARG_SELECTED_TAXONOMY, selectedTaxonomy) }
        }
    }
}