package fr.geonature.occtax.ui.input.taxa

import android.database.Cursor
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import fr.geonature.commons.data.Provider.buildUri
import fr.geonature.commons.data.Taxon
import fr.geonature.commons.input.AbstractInput
import fr.geonature.occtax.R
import fr.geonature.occtax.input.Input
import fr.geonature.occtax.input.InputTaxon
import fr.geonature.occtax.ui.input.IInputFragment
import fr.geonature.viewpager.ui.AbstractPagerFragmentActivity
import fr.geonature.viewpager.ui.IValidateFragment

/**
 * [Fragment] to let the user to choose a [Taxon] from the list.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class TaxaFragment : Fragment(),
                     IValidateFragment,
                     IInputFragment {

    private var input: Input? = null
    private var selectedTaxonId: Long? = null

    private var adapter: TaxaRecyclerViewAdapter? = null

    private val loaderCallbacks = object : LoaderManager.LoaderCallbacks<Cursor> {
        override fun onCreateLoader(id: Int,
                                    args: Bundle?): Loader<Cursor> {

            when (id) {
                LOADER_TAXA -> {
                    val selections = if (args?.getString(KEY_FILTER,
                                                         null) == null) Pair(null,
                                                                             null)
                    else {
                        val filter = "%${args.getString(KEY_FILTER)}%"
                        Pair("(${Taxon.COLUMN_NAME} LIKE ?)",
                             arrayOf(filter))
                    }

                    return CursorLoader(requireContext(),
                                        buildUri(Taxon.TABLE_NAME),
                                        arrayOf(Taxon.COLUMN_ID,
                                                Taxon.COLUMN_NAME,
                                                Taxon.COLUMN_HERITAGE,
                                                Taxon.COLUMN_DESCRIPTION),
                                        selections.first,
                                        selections.second,
                                        null)
                }

                LOADER_TAXON -> {
                    return CursorLoader(requireContext(),
                                        buildUri(Taxon.TABLE_NAME,
                                                 args?.getLong(KEY_SELECTED_TAXON_ID).toString()),
                                        arrayOf(Taxon.COLUMN_ID,
                                                Taxon.COLUMN_NAME,
                                                Taxon.COLUMN_HERITAGE,
                                                Taxon.COLUMN_DESCRIPTION),
                                        null,
                                        null,
                                        null)
                }

                else -> throw IllegalArgumentException()
            }
        }

        override fun onLoadFinished(loader: Loader<Cursor>,
                                    data: Cursor?) {

            if (data == null) return

            when (loader.id) {
                LOADER_TAXA -> adapter?.bind(data)
                LOADER_TAXON -> {
                    if (data.moveToFirst()) {
                        val selectedTaxon = Taxon.fromCursor(data)

                        if (selectedTaxon != null) {
                            adapter?.setSelectedTaxon(selectedTaxon)
                        }
                    }
                }
            }
        }

        override fun onLoaderReset(loader: Loader<Cursor>) {
            when (loader.id) {
                LOADER_TAXA -> adapter?.bind(null)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState?.containsKey(KEY_SELECTED_TAXON_ID) == true) {
            selectedTaxonId = savedInstanceState.getLong(KEY_SELECTED_TAXON_ID)
        }
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_taxa,
                                    container,
                                    false)
        val recyclerView = view.findViewById<RecyclerView>(android.R.id.list)

        // Set the adapter
        adapter = TaxaRecyclerViewAdapter(object : TaxaRecyclerViewAdapter.OnTaxaRecyclerViewAdapterListener {
            override fun onSelectedTaxon(taxon: Taxon) {
                val selectedTaxonId = selectedTaxonId

                if (selectedTaxonId != null) {
                    input?.removeInputTaxon(selectedTaxonId)
                }

                input?.addInputTaxon(InputTaxon().apply { id = taxon.id })
                this@TaxaFragment.selectedTaxonId = taxon.id

                (activity as AbstractPagerFragmentActivity?)?.validateCurrentPage()
            }

            override fun onNoTaxonSelected() {
                val selectedTaxonId = selectedTaxonId

                if (selectedTaxonId != null) {
                    input?.removeInputTaxon(selectedTaxonId)
                }

                (activity as AbstractPagerFragmentActivity?)?.validateCurrentPage()
            }

            override fun scrollToFirstSelectedItemPosition(position: Int) {
                recyclerView.smoothScrollToPosition(position)
            }
        })

        with(recyclerView) {
            layoutManager = LinearLayoutManager(context)
            adapter = this@TaxaFragment.adapter
        }

        val dividerItemDecoration = DividerItemDecoration(recyclerView.context,
                                                          (recyclerView.layoutManager as LinearLayoutManager).orientation)
        recyclerView.addItemDecoration(dividerItemDecoration)

        return view
    }

    override fun onViewCreated(view: View,
                               savedInstanceState: Bundle?) {
        super.onViewCreated(view,
                            savedInstanceState)

        // we have a menu item to show in action bar
        setHasOptionsMenu(true)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        val selectedTaxonId = selectedTaxonId

        if (selectedTaxonId != null) {
            outState.putLong(KEY_SELECTED_TAXON_ID,
                             selectedTaxonId)
        }

        super.onSaveInstanceState(outState)
    }

    override fun onCreateOptionsMenu(menu: Menu,
                                     inflater: MenuInflater) {

        super.onCreateOptionsMenu(menu,
                                  inflater)

        inflater.inflate(R.menu.search,
                         menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                LoaderManager.getInstance(this@TaxaFragment)
                    .restartLoader(LOADER_TAXA,
                                   bundleOf(Pair(KEY_FILTER,
                                                 newText)),
                                   loaderCallbacks)

                return true
            }
        })
    }

    override fun getResourceTitle(): Int {
        return R.string.pager_fragment_taxa_title
    }

    override fun pagingEnabled(): Boolean {
        return true
    }

    override fun validate(): Boolean {
        return this.input?.getCurrentSelectedInputTaxon() != null
    }

    override fun refreshView() {
        LoaderManager.getInstance(this)
            .initLoader(LOADER_TAXA,
                        null,
                        loaderCallbacks)

        val selectedInputTaxon = this.input?.getCurrentSelectedInputTaxon()

        if (selectedInputTaxon != null) {
            LoaderManager.getInstance(this)
                .initLoader(LOADER_TAXON,
                            bundleOf(Pair(KEY_SELECTED_TAXON_ID,
                                          selectedInputTaxon.id)),
                            loaderCallbacks)
        }
    }

    override fun setInput(input: AbstractInput) {
        this.input = input as Input
    }

    companion object {

        private const val LOADER_TAXA = 1
        private const val LOADER_TAXON = 2
        private const val KEY_FILTER = "filter"
        private const val KEY_SELECTED_TAXON_ID = "selected_taxon_id"

        /**
         * Use this factory method to create a new instance of [TaxaFragment].
         *
         * @return A new instance of [TaxaFragment]
         */
        @JvmStatic
        fun newInstance() = TaxaFragment()
    }
}
