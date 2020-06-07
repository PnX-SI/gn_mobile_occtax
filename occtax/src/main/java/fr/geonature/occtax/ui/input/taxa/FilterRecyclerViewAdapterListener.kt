package fr.geonature.occtax.ui.input.taxa

/**
 * default Callback used by [FilterRecyclerViewAdapter].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
interface FilterRecyclerViewAdapterListener<T : Filter<*>> {

    /**
     * Called when a set of [Filter]s have been selected.
     *
     * @param filter the selected [Filter]s
     */
    fun onSelectedFilters(vararg filter: T)
}