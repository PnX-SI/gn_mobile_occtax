package fr.geonature.occtax.ui.input.taxa

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.database.Cursor
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ProgressBar
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
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
import dagger.hilt.android.AndroidEntryPoint
import fr.geonature.commons.data.ContentProviderAuthority
import fr.geonature.commons.data.entity.AbstractTaxon
import fr.geonature.commons.data.entity.Taxon
import fr.geonature.commons.data.entity.TaxonWithArea
import fr.geonature.commons.data.entity.Taxonomy
import fr.geonature.commons.data.helper.ProviderHelper.buildUri
import fr.geonature.commons.util.ThemeUtils
import fr.geonature.compat.content.getParcelableArrayExtraCompat
import fr.geonature.compat.os.getParcelableArrayCompat
import fr.geonature.occtax.R
import fr.geonature.occtax.features.settings.domain.AppSettings
import fr.geonature.occtax.ui.input.AbstractInputFragment
import org.tinylog.Logger
import java.util.Locale
import javax.inject.Inject

/**
 * [Fragment] to let the user to choose a [Taxon] from the list.
 *
 * @author S. Grimault
 */
@AndroidEntryPoint
class TaxaFragment : AbstractInputFragment() {

    @ContentProviderAuthority
    @Inject
    lateinit var authority: String

    private lateinit var savedState: Bundle
    private lateinit var taxaFilterResultLauncher: ActivityResultLauncher<Intent>

    private var adapter: TaxaRecyclerViewAdapter? = null
    private var progressBar: ProgressBar? = null
    private var emptyTextView: View? = null
    private var filterChipGroup: ChipGroup? = null

    private val loaderCallbacks = object : LoaderManager.LoaderCallbacks<Cursor> {
        override fun onCreateLoader(
            id: Int,
            args: Bundle?
        ): Loader<Cursor> {

            val selectedFilters =
                args?.getParcelableArrayCompat<Filter<*>>(KEY_SELECTED_FILTERS)
                    ?.toList() ?: emptyList()

            return when (id) {
                LOADER_TAXA -> {
                    val taxaListId = args?.getLong(
                        KEY_TAXA_LIST_ID,
                        -1L
                    )
                        ?.takeIf { it >= 0L }
                    val selectedFeatureId = args?.getString(
                        KEY_SELECTED_FEATURE_ID,
                        null
                    )

                    Logger.debug {
                        "loading taxa${if (taxaListId == null) "" else " from taxa list ID $taxaListId"}${if (selectedFeatureId.isNullOrBlank()) "" else " with selected feature ID '$selectedFeatureId'"}..."
                    }

                    val taxonFilter =
                        TaxonWithArea.Filter()
                            .byNameOrDescription(args?.getString(KEY_FILTER_BY_NAME))
                            .also {
                                val filterByAreaObservation =
                                    selectedFilters
                                        .asSequence()
                                        .filter { filter -> filter.type == Filter.FilterType.AREA_OBSERVATION }
                                        .map { filter -> filter.value as FilterAreaObservation.AreaObservation }
                                        .map { areaObservation ->
                                            when (areaObservation.type) {
                                                FilterAreaObservation.AreaObservationType.MORE_THAN_DURATION -> "red"
                                                FilterAreaObservation.AreaObservationType.LESS_THAN_DURATION -> "grey"
                                                else -> "none"
                                            }
                                        }
                                        .toList()
                                val filterByTaxonomy =
                                    selectedFilters.find { filter -> filter.type == Filter.FilterType.TAXONOMY }?.value as Taxonomy?

                                if (filterByAreaObservation.isNotEmpty() && !selectedFeatureId.isNullOrBlank()) {
                                    (it as TaxonWithArea.Filter).byAreaColors(*filterByAreaObservation.toTypedArray())
                                }

                                if (filterByTaxonomy != null) {
                                    it.byTaxonomy(filterByTaxonomy)
                                }
                            }
                            .build()

                    CursorLoader(
                        requireContext(),
                        buildUri(
                            authority,
                            Taxon.TABLE_NAME,
                            if (taxaListId == null) "" else "list/$taxaListId",
                            if (selectedFeatureId.isNullOrBlank()) "" else "area/$selectedFeatureId"
                        ),
                        null,
                        taxonFilter.first,
                        taxonFilter.second.map { it.toString() }
                            .toTypedArray(),
                        TaxonWithArea.OrderBy()
                            .byName(args?.getString(KEY_FILTER_BY_NAME))
                            .build()
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
                            authority,
                            Taxon.TABLE_NAME,
                            args?.getLong(KEY_SELECTED_TAXON_ID)
                                .toString(),
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
                Logger.warn { "failed to load data from '${(loader as CursorLoader).uri}'" }

                return
            }

            when (loader.id) {
                LOADER_TAXA -> {
                    adapter?.bind(data)
                    listener.validateCurrentPage()
                    (activity as AppCompatActivity?)?.supportActionBar?.subtitle = getSubtitle()
                }

                LOADER_TAXON -> {
                    if (data.moveToFirst()) {
                        val selectedTaxon = Taxon.fromCursor(data)

                        if (selectedTaxon != null) {
                            adapter?.setSelectedTaxon(selectedTaxon)
                        }
                    } else {
                        // no taxon found according to given criteria
                        observationRecord?.taxa?.selectedTaxonRecord?.taxon?.id?.also {
                            observationRecord?.taxa?.delete(it)
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

        taxaFilterResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
                if ((activityResult.resultCode != Activity.RESULT_OK) || (activityResult.data == null)) {
                    return@registerForActivityResult
                }

                val selectedFilters =
                    activityResult.data?.getParcelableArrayExtraCompat<Filter<*>>(TaxaFilterActivity.EXTRA_SELECTED_FILTERS)
                        ?: emptyArray()
                applyFilters(*selectedFilters)
            }
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
        progressBar = view.findViewById(android.R.id.progress)
        emptyTextView = view.findViewById(R.id.emptyTextView)
        filterChipGroup = view.findViewById(R.id.chip_group_filter)

        // Set the adapter
        adapter = TaxaRecyclerViewAdapter(object :
            TaxaRecyclerViewAdapter.OnTaxaRecyclerViewAdapterListener {
            override fun onSelectedTaxon(taxon: AbstractTaxon) {
                observationRecord?.taxa?.selectedTaxonRecord
                    ?.also { observationRecord?.taxa?.delete(it.taxon.id) }

                observationRecord?.taxa?.add(taxon)

                Logger.info { "selected taxon (id: ${taxon.id}, name: '${taxon.name}', taxonomy: (kingdom='${taxon.taxonomy.kingdom}', group='${taxon.taxonomy.group}'))" }

                listener.validateCurrentPage()
            }

            override fun onNoTaxonSelected() {
                observationRecord?.taxa?.selectedTaxonRecord
                    ?.also { observationRecord?.taxa?.delete(it.taxon.id) }

                listener.validateCurrentPage()
            }

            override fun scrollToFirstSelectedItemPosition(position: Int) {
                recyclerView.scrollToPosition(position)
            }

            override fun showEmptyTextView(show: Boolean) {
                val emptyTextView = emptyTextView ?: return
                val context = context ?: return

                progressBar?.visibility = View.GONE

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
            setHasFixedSize(true)
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

        applyFilters(*getSelectedFilters().toTypedArray())
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
                savedState.putString(
                    KEY_FILTER_BY_NAME,
                    newText
                )
                loadTaxa()

                return true
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_filter -> {
                val context = context ?: return true

                taxaFilterResultLauncher.launch(
                    TaxaFilterActivity.newIntent(
                        context,
                        !savedState.getString(KEY_SELECTED_FEATURE_ID)
                            .isNullOrEmpty(),
                        arguments?.getInt(
                            ARG_AREA_OBSERVATION_DURATION,
                            AppSettings.Builder.DEFAULT_AREA_OBSERVATION_DURATION
                        ) ?: AppSettings.Builder.DEFAULT_AREA_OBSERVATION_DURATION,
                        *getSelectedFilters().toTypedArray()
                    )
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
        val context = context ?: return null

        if (progressBar?.visibility == View.VISIBLE && adapter?.itemCount == 0) {
            return null
        }

        val taxaFound = adapter?.itemCount ?: return null

        return context.resources.getQuantityString(
            R.plurals.taxa_found,
            taxaFound,
            taxaFound
        )
    }

    override fun pagingEnabled(): Boolean {
        return true
    }

    override fun validate(): Boolean {
        return this.observationRecord?.taxa?.selectedTaxonRecord != null
    }

    override fun refreshView() {
        if (observationRecord?.feature?.id.isNullOrEmpty()) savedState.remove(KEY_SELECTED_FEATURE_ID)
        else savedState.putString(
            KEY_SELECTED_FEATURE_ID,
            observationRecord?.feature?.id
        )

        // set the current taxa list ID from selected dataset or from global settings as fallback
        (observationRecord?.dataset?.dataset?.taxaListId ?: arguments?.getLong(
            KEY_TAXA_LIST_ID,
            -1L
        ))?.takeIf { it >= 0L }
            ?.also {
                savedState.putLong(
                    KEY_TAXA_LIST_ID,
                    it
                )
            }

        loadTaxa()

        val selectedTaxonRecord = this.observationRecord?.taxa?.selectedTaxonRecord

        if (selectedTaxonRecord != null) {
            Logger.info { "selected taxon (id: ${selectedTaxonRecord.taxon.id}, name: '${selectedTaxonRecord.taxon.name}', taxonomy: (kingdom='${selectedTaxonRecord.taxon.taxonomy.kingdom}', group='${selectedTaxonRecord.taxon.taxonomy.group}'))" }

            LoaderManager.getInstance(this)
                .initLoader(
                    LOADER_TAXON,
                    Bundle(savedState).apply {
                        putLong(
                            KEY_SELECTED_TAXON_ID,
                            selectedTaxonRecord.taxon.id
                        )
                    },
                    loaderCallbacks
                )
        }
    }

    private fun loadTaxa() {
        progressBar?.visibility = View.VISIBLE

        LoaderManager.getInstance(this)
            .restartLoader(
                LOADER_TAXA,
                savedState,
                loaderCallbacks
            )
    }

    private fun applyFilters(vararg filter: Filter<*>) {
        savedState.putParcelableArray(
            KEY_SELECTED_FILTERS,
            filter
        )

        val selectedAreaObservation =
            filter.filter { it.type == Filter.FilterType.AREA_OBSERVATION }
                .map { it.value as FilterAreaObservation.AreaObservation }
        val selectedTaxonomy =
            filter.find { it.type == Filter.FilterType.TAXONOMY }?.value as Taxonomy?

        filterByAreaObservation(*selectedAreaObservation.toTypedArray())
        filterByTaxonomy(selectedTaxonomy)

        loadTaxa()
    }

    @SuppressLint("DiscouragedApi")
    private fun filterByAreaObservation(vararg areaObservation: FilterAreaObservation.AreaObservation) {
        val filterChipGroup = filterChipGroup ?: return
        val context = context ?: return

        val areaObservationChipsToDelete = arrayListOf<Chip>()

        for (i in 0 until filterChipGroup.childCount) {
            with(filterChipGroup[i]) {
                if (this is Chip && tag is FilterAreaObservation.AreaObservation) {
                    areaObservationChipsToDelete.add(this)
                }
            }
        }

        areaObservationChipsToDelete.forEach {
            filterChipGroup.removeView(it)
        }

        filterChipGroup.visibility = if (filterChipGroup.childCount > 0) View.VISIBLE else View.GONE

        if (areaObservation.isEmpty()) {
            return
        }

        // nothing to do if all area observation types are selected
        if (areaObservation.size == FilterAreaObservation.AreaObservationType.values().size) {
            return
        }

        filterChipGroup.visibility = View.VISIBLE

        areaObservation.toList()
            .sortedBy { it.type }
            .forEachIndexed { index, areaObservationToAdd ->
                with(
                    LayoutInflater.from(context)
                        .inflate(
                            R.layout.chip,
                            filterChipGroup,
                            false
                        ) as Chip
                ) {
                    tag = areaObservationToAdd
                    text = areaObservationToAdd.short

                    setChipBackgroundColorResource(context.resources.getIdentifier(
                        "area_observation_${areaObservationToAdd.type.name.lowercase(Locale.ROOT)}",
                        "color",
                        context.packageName
                    )
                        .takeIf { it > 0 } ?: R.color.accent)
                    setTextColor(
                        ThemeUtils.getColor(
                            context,
                            if (areaObservationToAdd.type == FilterAreaObservation.AreaObservationType.NONE) android.R.attr.textColorPrimary else android.R.attr.textColorPrimaryInverse
                        )
                    )
                    closeIconTint = ColorStateList.valueOf(
                        ThemeUtils.getColor(
                            context,
                            if (areaObservationToAdd.type == FilterAreaObservation.AreaObservationType.NONE) android.R.attr.textColorPrimary else android.R.attr.textColorPrimaryInverse
                        )
                    )

                    setOnClickListener {
                        applyFilters(*getSelectedFilters().filter { it.type != Filter.FilterType.AREA_OBSERVATION || (it.value is FilterAreaObservation.AreaObservation && it.value.type != areaObservationToAdd.type) }
                            .toTypedArray())
                    }
                    setOnCloseIconClickListener {
                        applyFilters(*getSelectedFilters().filter { it.type != Filter.FilterType.AREA_OBSERVATION || (it.value is FilterAreaObservation.AreaObservation && it.value.type != areaObservationToAdd.type) }
                            .toTypedArray())
                    }

                    filterChipGroup.addView(
                        this,
                        index
                    )
                }
            }
    }

    private fun filterByTaxonomy(selectedTaxonomy: Taxonomy?) {
        val filterChipGroup = filterChipGroup ?: return
        val context = context ?: return

        val taxonomyChipsToDelete = arrayListOf<Chip>()

        for (i in 0 until filterChipGroup.childCount) {
            with(filterChipGroup[i]) {
                if (this is Chip && tag is Taxonomy) {
                    taxonomyChipsToDelete.add(this)
                }
            }
        }

        taxonomyChipsToDelete.forEach {
            filterChipGroup.removeView(it)
        }

        filterChipGroup.visibility = if (filterChipGroup.childCount > 0) View.VISIBLE else View.GONE

        if (selectedTaxonomy != null) {
            filterChipGroup.visibility = View.VISIBLE

            // build kingdom taxonomy filter chip
            with(
                LayoutInflater.from(context)
                    .inflate(
                        R.layout.chip,
                        filterChipGroup,
                        false
                    ) as Chip
            ) {
                tag = Taxonomy(selectedTaxonomy.kingdom)
                text = selectedTaxonomy.kingdom
                setOnClickListener {
                    applyFilters(*getSelectedFilters().filter { it.type != Filter.FilterType.TAXONOMY }
                        .toTypedArray())
                }
                setOnCloseIconClickListener {
                    applyFilters(*getSelectedFilters().filter { it.type != Filter.FilterType.TAXONOMY }
                        .toTypedArray())
                }

                filterChipGroup.addView(this)
            }

            // build group taxonomy filter chip
            if (selectedTaxonomy.group != Taxonomy.ANY) {
                with(
                    LayoutInflater.from(context)
                        .inflate(
                            R.layout.chip,
                            filterChipGroup,
                            false
                        ) as Chip
                ) {
                    tag = selectedTaxonomy
                    text = selectedTaxonomy.group
                    setOnClickListener {
                        applyFilters(*(getSelectedFilters().filter { filter -> filter.type != Filter.FilterType.TAXONOMY }
                            .toTypedArray() + mutableListOf(FilterTaxonomy(Taxonomy((it.tag as Taxonomy).kingdom)))))
                    }
                    setOnCloseIconClickListener {
                        applyFilters(*(getSelectedFilters().filter { filter -> filter.type != Filter.FilterType.TAXONOMY }
                            .toTypedArray() + mutableListOf(FilterTaxonomy(Taxonomy((it.tag as Taxonomy).kingdom)))))
                    }

                    filterChipGroup.addView(this)
                }
            }
        }
    }

    private fun getSelectedFilters(): List<Filter<*>> {
        return savedState.getParcelableArrayCompat<Filter<*>>(KEY_SELECTED_FILTERS)
            ?.toList() ?: emptyList()
    }

    companion object {

        private const val ARG_AREA_OBSERVATION_DURATION = "arg_area_observation_duration"
        private const val LOADER_TAXA = 1
        private const val LOADER_TAXON = 2
        private const val KEY_TAXA_LIST_ID = "key_taxa_list_id"
        private const val KEY_FILTER_BY_NAME = "key_filter_by_name"
        private const val KEY_SELECTED_FILTERS = "key_selected_filters"
        private const val KEY_SELECTED_FEATURE_ID = "key_selected_feature_id"
        private const val KEY_SELECTED_TAXON_ID = "key_selected_taxon_id"

        /**
         * Use this factory method to create a new instance of [TaxaFragment].
         *
         * @return A new instance of [TaxaFragment]
         */
        @JvmStatic
        fun newInstance(
            areaObservationDuration: Int = AppSettings.Builder.DEFAULT_AREA_OBSERVATION_DURATION,
            taxaListId: Long? = null
        ) =
            TaxaFragment().apply {
                arguments = Bundle().apply {
                    putInt(
                        ARG_AREA_OBSERVATION_DURATION,
                        areaObservationDuration
                    )
                    taxaListId?.also {
                        putLong(
                            KEY_TAXA_LIST_ID,
                            it
                        )
                    }
                }
            }
    }
}
