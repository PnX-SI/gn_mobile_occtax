package fr.geonature.occtax.ui.input.taxa

import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.appcompat.widget.SearchView
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import fr.geonature.commons.data.AbstractTaxon
import fr.geonature.commons.data.Taxon
import fr.geonature.commons.data.Taxonomy
import fr.geonature.commons.data.helper.Provider.buildUri
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
    private var adapter: TaxaRecyclerViewAdapter? = null
    private lateinit var savedState: Bundle
    private var emptyTextView: View? = null
    private var filterChipGroup: ChipGroup? = null

    private val loaderCallbacks = object : LoaderManager.LoaderCallbacks<Cursor> {
        override fun onCreateLoader(
            id: Int,
            args: Bundle?
        ): Loader<Cursor> {

            return when (id) {
                LOADER_TAXA -> {
                    val selectedFeatureId = args?.getString(
                        KEY_SELECTED_FEATURE_ID,
                        null
                    )

                    val taxonFilter =
                        Taxon.Filter().byName(args?.getString(KEY_FILTER_BY_NAME)).also {
                            val filterByTaxonomy = args?.getParcelable<Taxonomy>(
                                KEY_FILTER_BY_TAXONOMY
                            )

                            if (filterByTaxonomy != null) {
                                (it as Taxon.Filter).byTaxonomy(filterByTaxonomy)
                            }
                        }.build()

                    CursorLoader(
                        requireContext(),
                        buildUri(
                            Taxon.TABLE_NAME,
                            if (selectedFeatureId == null) "" else "area/$selectedFeatureId"
                        ),
                        null,
                        taxonFilter.first,
                        taxonFilter.second.map { it.toString() }.toTypedArray(),
                        null
                    )
                }
                LOADER_TAXON -> {
                    val selectedFeatureId = args?.getString(
                        KEY_SELECTED_FEATURE_ID,
                        null
                    )

                    CursorLoader(
                        requireContext(),
                        buildUri(
                            Taxon.TABLE_NAME,
                            args?.getLong(KEY_SELECTED_TAXON_ID).toString(),
                            if (selectedFeatureId == null) "" else "area/$selectedFeatureId"
                        ),
                        null,
                        null,
                        null,
                        null
                    )
                }
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
                LOADER_TAXA -> {
                    adapter?.bind(data)
                    (activity as AbstractPagerFragmentActivity?)?.validateCurrentPage()
                }
                LOADER_TAXON -> {
                    if (data.moveToFirst()) {
                        val selectedTaxon = Taxon.fromCursor(data)

                        if (selectedTaxon != null) {
                            adapter?.setSelectedTaxon(selectedTaxon)
                        }
                    }

                    LoaderManager.getInstance(this@TaxaFragment)
                        .destroyLoader(LOADER_TAXON)
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

        savedState = savedInstanceState ?: Bundle()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(
            R.layout.fragment_taxa,
            container,
            false
        )
        val recyclerView = view.findViewById<RecyclerView>(android.R.id.list)
        emptyTextView = view.findViewById(R.id.emptyTextView)
        filterChipGroup = view.findViewById(R.id.chip_group_filter)

        // Set the adapter
        adapter = TaxaRecyclerViewAdapter(object :
            TaxaRecyclerViewAdapter.OnTaxaRecyclerViewAdapterListener {
            override fun onSelectedTaxon(taxon: AbstractTaxon) {
                input?.getCurrentSelectedInputTaxon()
                    ?.also { input?.removeInputTaxon(it.taxon.id) }

                input?.addInputTaxon(InputTaxon(taxon))

                (activity as AbstractPagerFragmentActivity?)?.validateCurrentPage()
            }

            override fun onNoTaxonSelected() {
                input?.getCurrentSelectedInputTaxon()
                    ?.also { input?.removeInputTaxon(it.taxon.id) }

                (activity as AbstractPagerFragmentActivity?)?.validateCurrentPage()
            }

            override fun scrollToFirstSelectedItemPosition(position: Int) {
                recyclerView.smoothScrollToPosition(position)
            }

            override fun showEmptyTextView(show: Boolean) {
                val emptyTextView = emptyTextView ?: return
                val context = context ?: return

                if (emptyTextView.visibility == View.VISIBLE == show) {
                    return
                }

                if (show) {
                    emptyTextView.startAnimation(
                        AnimationUtils.loadAnimation(
                            context,
                            android.R.anim.fade_in
                        )
                    )
                    emptyTextView.visibility = View.VISIBLE
                } else {
                    emptyTextView.startAnimation(
                        AnimationUtils.loadAnimation(
                            context,
                            android.R.anim.fade_out
                        )
                    )
                    emptyTextView.visibility = View.GONE
                }
            }
        })

        with(recyclerView) {
            layoutManager = LinearLayoutManager(context)
            adapter = this@TaxaFragment.adapter
        }

        val dividerItemDecoration = DividerItemDecoration(
            recyclerView.context,
            (recyclerView.layoutManager as LinearLayoutManager).orientation
        )
        recyclerView.addItemDecoration(dividerItemDecoration)

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

        filterByTaxonomy(savedState.getParcelable(KEY_FILTER_BY_TAXONOMY))
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(savedState.apply { putAll(outState) })
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        if ((resultCode != Activity.RESULT_OK) || (data == null)) {
            return
        }

        when (requestCode) {
            RESULT_FILTER -> {
                val selectedTaxonomy =
                    if (data.hasExtra(TaxonomyFilterActivity.EXTRA_SELECTED_TAXONOMY)) data.getParcelableExtra<Taxonomy>(
                        TaxonomyFilterActivity.EXTRA_SELECTED_TAXONOMY
                    ) else null
                filterByTaxonomy(selectedTaxonomy)
            }
        }
    }

    override fun onCreateOptionsMenu(
        menu: Menu,
        inflater: MenuInflater
    ) {

        super.onCreateOptionsMenu(
            menu,
            inflater
        )

        with(inflater) {
            inflate(
                R.menu.search,
                menu
            )
            inflate(
                R.menu.filter,
                menu
            )
        }

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                savedState.putString(KEY_FILTER_BY_NAME, newText)
                loadTaxa()

                return true
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_filter -> {
                val context = context ?: return true
                startActivityForResult(
                    TaxonomyFilterActivity.newIntent(
                        context, savedState.getParcelable(
                            KEY_FILTER_BY_TAXONOMY
                        )
                    ), RESULT_FILTER
                )
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun getResourceTitle(): Int {
        return R.string.pager_fragment_taxa_title
    }

    override fun getSubtitle(): CharSequence? {
        val taxaFound = adapter?.itemCount ?: return null

        return resources.getQuantityString(
            R.plurals.taxa_found,
            taxaFound,
            taxaFound
        )
    }

    override fun pagingEnabled(): Boolean {
        return true
    }

    override fun validate(): Boolean {
        return this.input?.getCurrentSelectedInputTaxon() != null
    }

    override fun refreshView() {
        loadTaxa()

        val selectedInputTaxon = this.input?.getCurrentSelectedInputTaxon()

        if (selectedInputTaxon != null) {
            LoaderManager.getInstance(this)
                .initLoader(
                    LOADER_TAXON,
                    Bundle(savedState).apply {
                        putLong(
                            KEY_SELECTED_TAXON_ID,
                            selectedInputTaxon.taxon.id
                        )
                    },
                    loaderCallbacks
                )
        }
    }

    override fun setInput(input: AbstractInput) {
        this.input = input as Input
        savedState.putString(KEY_SELECTED_FEATURE_ID, input.selectedFeatureId)
    }

    private fun loadTaxa() {
        LoaderManager.getInstance(this)
            .restartLoader(
                LOADER_TAXA,
                savedState,
                loaderCallbacks
            )
    }

    private fun filterByTaxonomy(selectedTaxonomy: Taxonomy?) {
        val filterChipGroup = filterChipGroup ?: return
        val context = context ?: return

        var taxonomyChipIndex = 0
        val taxonomyChips = arrayListOf<Chip>()

        for (i in 0 until filterChipGroup.childCount) {
            with(filterChipGroup[i]) {
                val tag = tag

                if (this is Chip && tag is Taxonomy && tag.group == Taxonomy.ANY) {
                    taxonomyChipIndex = i
                }

                if (this is Chip && tag is Taxonomy) {
                    taxonomyChips.add(this)
                }
            }
        }

        taxonomyChips.forEach {
            filterChipGroup.removeView(it)
        }

        filterChipGroup.visibility =
            if (filterChipGroup.childCount > 0) View.VISIBLE else View.GONE

        if (selectedTaxonomy == null) savedState.remove(KEY_FILTER_BY_TAXONOMY) else savedState.putParcelable(
            KEY_FILTER_BY_TAXONOMY, selectedTaxonomy
        )

        if (selectedTaxonomy != null) {
            filterChipGroup.visibility = View.VISIBLE

            // build kingdom taxonomy filter chip
            with(
                LayoutInflater.from(context).inflate(
                    R.layout.chip,
                    filterChipGroup,
                    false
                ) as Chip
            ) {
                tag = Taxonomy(selectedTaxonomy.kingdom)
                text = selectedTaxonomy.kingdom
                setOnClickListener {
                    filterByTaxonomy(null)
                }
                setOnCloseIconClickListener {
                    filterByTaxonomy(null)
                }

                filterChipGroup.addView(this, taxonomyChipIndex)
            }

            // build group taxonomy filter chip
            if (selectedTaxonomy.group != Taxonomy.ANY) {
                with(
                    LayoutInflater.from(context).inflate(
                        R.layout.chip,
                        filterChipGroup,
                        false
                    ) as Chip
                ) {
                    tag = selectedTaxonomy
                    text = selectedTaxonomy.group
                    setOnClickListener {
                        filterByTaxonomy(Taxonomy((it.tag as Taxonomy).kingdom))
                    }
                    setOnCloseIconClickListener {
                        filterByTaxonomy(Taxonomy((it.tag as Taxonomy).kingdom))
                    }

                    filterChipGroup.addView(this, taxonomyChipIndex + 1)
                }
            }
        }

        loadTaxa()
    }

    companion object {

        private val TAG = TaxaFragment::class.java.name

        private const val LOADER_TAXA = 1
        private const val LOADER_TAXON = 2
        private const val RESULT_FILTER = 3
        private const val KEY_FILTER_BY_NAME = "filter_by_name"
        private const val KEY_FILTER_BY_TAXONOMY = "filter_by_taxonomy"
        private const val KEY_SELECTED_FEATURE_ID = "key_selected_feature_id"
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
