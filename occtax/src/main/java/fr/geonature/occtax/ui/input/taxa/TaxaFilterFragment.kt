package fr.geonature.occtax.ui.input.taxa

import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import fr.geonature.commons.data.Taxonomy
import fr.geonature.commons.data.helper.Provider
import fr.geonature.occtax.R
import fr.geonature.occtax.settings.AppSettings

/**
 * [Fragment] to let the user to apply filters on taxa list.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class TaxaFilterFragment : Fragment() {

    private var listener: OnTaxaFilterFragmentListener? = null
    private var adapter: FilterRecyclerViewAdapter? = null
    private var progressBar: ProgressBar? = null

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
                    data.moveToFirst()

                    val filterTaxonomyList = mutableListOf<FilterTaxonomy>()

                    while (!data.isAfterLast) {
                        Taxonomy.fromCursor(data)?.also {
                            filterTaxonomyList.add(FilterTaxonomy(it))
                        }

                        data.moveToNext()
                    }

                    progressBar?.visibility = View.GONE
                    adapter?.setFilterTaxonomy(
                        getString(R.string.taxa_filter_taxonomy_title),
                        *filterTaxonomyList.toTypedArray()
                    )
                }
            }
        }

        override fun onLoaderReset(loader: Loader<Cursor>) {
            when (loader.id) {
                LOADER_TAXONOMY -> adapter?.setFilterTaxonomy(getString(R.string.taxa_filter_taxonomy_title))
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(
            R.layout.fragment_taxa_filter,
            container,
            false
        )

        val recyclerView = view.findViewById<RecyclerView>(android.R.id.list)
        progressBar = view.findViewById(android.R.id.progress)

        // Set the adapter
        adapter = FilterRecyclerViewAdapter(object : FilterRecyclerViewAdapterListener<Filter<*>> {
            override fun onSelectedFilters(vararg filter: Filter<*>) {
                listener?.onSelectedFilters(*filter)
            }
        })

        arguments?.getParcelableArray(ARG_FILTERS)?.map { it as Filter<*> }?.toTypedArray()?.also {
            adapter?.setSelectedFilters(*it)
        }

        with(recyclerView) {
            val layoutManager = LinearLayoutManager(context)
            this.layoutManager = layoutManager
            addItemDecoration(
                DividerItemDecoration(
                    context,
                    layoutManager.orientation
                )
            )
            adapter = this@TaxaFilterFragment.adapter
        }

        return view
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        progressBar?.visibility = View.VISIBLE

        if (arguments?.getBoolean(
                ARG_WITH_AREA_OBSERVATION,
                false
            ) == true
        ) {
            loadFilterAreaObservation(
                arguments?.getInt(
                    ARG_AREA_OBSERVATION_DURATION,
                    AppSettings.DEFAULT_AREA_OBSERVATION_DURATION
                ) ?: AppSettings.DEFAULT_AREA_OBSERVATION_DURATION
            )
        }

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

    private fun loadFilterAreaObservation(duration: Int) {
        val context = context ?: return

        val formatDuration: (Int) -> String = {
            when {
                (it % 365 == 0) -> context.resources.getQuantityString(
                    R.plurals.duration_year,
                    (it / 365),
                    (it / 365)
                )
                (it % 30 == 0) -> context.resources.getQuantityString(
                    R.plurals.duration_month,
                    (it / 30),
                    (it / 30)
                )
                else -> context.resources.getQuantityString(
                    R.plurals.duration_days,
                    it,
                    it
                )
            }
        }

        val filterAreaObservationList = mutableListOf(
            FilterAreaObservation(
                FilterAreaObservation.AreaObservation(
                    FilterAreaObservation.AreaObservationType.MORE_THAN_DURATION,
                    getString(
                        R.string.taxa_filter_area_observation_more_than_duration,
                        formatDuration(duration)
                    ),
                    getString(
                        R.string.taxa_filter_area_observation_short_more_than_duration,
                        formatDuration(duration)
                    )
                )
            ),
            FilterAreaObservation(
                FilterAreaObservation.AreaObservation(
                    FilterAreaObservation.AreaObservationType.LESS_THAN_DURATION,
                    getString(
                        R.string.taxa_filter_area_observation_less_than_duration,
                        formatDuration(duration)
                    ),
                    getString(
                        R.string.taxa_filter_area_observation_short_less_than_duration,
                        formatDuration(duration)
                    )
                )
            ),
            FilterAreaObservation(
                FilterAreaObservation.AreaObservation(
                    FilterAreaObservation.AreaObservationType.NONE,
                    getString(R.string.taxa_filter_area_observation_none),
                    getString(R.string.taxa_filter_area_observation_short_none)
                )
            )
        )
        adapter?.setFilterAreaObservation(
            getString(R.string.taxa_filter_area_observation_title),
            *filterAreaObservationList.toTypedArray()
        )
    }

    /**
     * Callback used by [TaxaFilterFragment].
     */
    interface OnTaxaFilterFragmentListener {

        /**
         * Called when a set of [Filter]s have been selected.
         *
         * @param filter the selected [Filter]s
         */
        fun onSelectedFilters(vararg filter: Filter<*>)
    }

    companion object {

        private val TAG = TaxaFilterFragment::class.java.name

        private const val ARG_WITH_AREA_OBSERVATION = "arg_with_area_observation"
        private const val ARG_AREA_OBSERVATION_DURATION = "arg_area_observation_duration"
        private const val ARG_FILTERS = "arg_filters"
        private const val LOADER_TAXONOMY = 1

        /**
         * Use this factory method to create a new instance of [TaxaFilterFragment].
         *
         * @return A new instance of [TaxaFilterFragment]
         */
        @JvmStatic
        fun newInstance(
            withAreaObservation: Boolean = false,
            areaObservationDuration: Int = AppSettings.DEFAULT_AREA_OBSERVATION_DURATION,
            vararg filter: Filter<*>
        ) =
            TaxaFilterFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(
                        ARG_WITH_AREA_OBSERVATION,
                        withAreaObservation
                    )
                    putInt(
                        ARG_AREA_OBSERVATION_DURATION,
                        areaObservationDuration
                    )
                    putParcelableArray(
                        ARG_FILTERS,
                        filter
                    )
                }
            }
    }
}