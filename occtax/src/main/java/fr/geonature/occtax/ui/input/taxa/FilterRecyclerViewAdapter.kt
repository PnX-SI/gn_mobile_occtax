package fr.geonature.occtax.ui.input.taxa

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.MergeAdapter
import androidx.recyclerview.widget.RecyclerView
import fr.geonature.commons.data.Taxonomy
import fr.geonature.commons.ui.adapter.IStickyRecyclerViewAdapter
import fr.geonature.commons.ui.adapter.StickyHeaderItemDecorator
import fr.geonature.occtax.R

/**
 * Default RecyclerView Adapter used by [TaxaFilterFragment], combining [FilterNameRecyclerViewAdapter],
 * [FilterAreaObservationRecyclerViewAdapter] and [FilterTaxonomyRecyclerViewAdapter].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 *
 * @see FilterNameRecyclerViewAdapter
 * @see FilterAreaObservationRecyclerViewAdapter
 * @see FilterTaxonomyRecyclerViewAdapter
 */
class FilterRecyclerViewAdapter(val listener: FilterRecyclerViewAdapterListener<Filter<*>>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(),
    IStickyRecyclerViewAdapter<FilterRecyclerViewAdapter.HeaderViewHolder> {

    private val filterNameRecyclerViewAdapter: FilterNameRecyclerViewAdapter =
        FilterNameRecyclerViewAdapter(object : FilterRecyclerViewAdapterListener<FilterName> {
            override fun onSelectedFilters(vararg filter: FilterName) {
                val existingFilters =
                    selectedFilters.filter { it.type != Filter.FilterType.NAME }
                selectedFilters.clear()
                selectedFilters.addAll(existingFilters + filter)
                listener.onSelectedFilters(*selectedFilters.toTypedArray())
            }
        })
    private val filterTitleAreaObservationRecyclerViewAdapter = FilterTitleRecyclerViewAdapter()
    private val filterAreaObservationRecyclerViewAdapter: FilterAreaObservationRecyclerViewAdapter =
        FilterAreaObservationRecyclerViewAdapter(object :
            FilterRecyclerViewAdapterListener<FilterAreaObservation> {
            override fun onSelectedFilters(vararg filter: FilterAreaObservation) {
                val existingFilters =
                    selectedFilters.filter { it.type != Filter.FilterType.AREA_OBSERVATION }
                selectedFilters.clear()
                selectedFilters.addAll(existingFilters + filter)
                listener.onSelectedFilters(*selectedFilters.toTypedArray())
            }
        })
    private val filterTitleTaxonomyRecyclerViewAdapter = FilterTitleRecyclerViewAdapter()
    private val filterTaxonomyRecyclerViewAdapter: FilterTaxonomyRecyclerViewAdapter =
        FilterTaxonomyRecyclerViewAdapter(object :
            FilterRecyclerViewAdapterListener<FilterTaxonomy> {
            override fun onSelectedFilters(vararg filter: FilterTaxonomy) {
                val existingFilters =
                    selectedFilters.filter { it.type != Filter.FilterType.TAXONOMY }
                selectedFilters.clear()
                selectedFilters.addAll(existingFilters + filter)
                listener.onSelectedFilters(*selectedFilters.toTypedArray())
            }
        })
    private val mergeAdapter = MergeAdapter(
        filterNameRecyclerViewAdapter,
        filterTitleAreaObservationRecyclerViewAdapter,
        filterAreaObservationRecyclerViewAdapter,
        filterTitleTaxonomyRecyclerViewAdapter,
        filterTaxonomyRecyclerViewAdapter
    )
    private val selectedFilters: MutableList<Filter<*>> = mutableListOf()

    init {
        setHasStableIds(mergeAdapter.hasStableIds())
        mergeAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                this@FilterRecyclerViewAdapter.notifyDataSetChanged()
            }

            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                this@FilterRecyclerViewAdapter.notifyItemRangeRemoved(
                    positionStart,
                    itemCount
                )
            }

            override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
                this@FilterRecyclerViewAdapter.notifyItemMoved(
                    fromPosition,
                    toPosition
                )
            }

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                this@FilterRecyclerViewAdapter.notifyItemRangeInserted(
                    positionStart,
                    itemCount
                )
            }

            override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
                this@FilterRecyclerViewAdapter.notifyItemRangeChanged(
                    positionStart,
                    itemCount
                )
            }

            override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
                this@FilterRecyclerViewAdapter.notifyItemRangeChanged(
                    positionStart,
                    itemCount,
                    payload
                )
            }
        })
    }

    override fun onFailedToRecycleView(holder: RecyclerView.ViewHolder): Boolean {
        return mergeAdapter.onFailedToRecycleView(holder)
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        mergeAdapter.onViewAttachedToWindow(holder)
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        mergeAdapter.onViewDetachedFromWindow(holder)
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        mergeAdapter.onViewRecycled(holder)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        mergeAdapter.onAttachedToRecyclerView(recyclerView)

        recyclerView.addItemDecoration(
            StickyHeaderItemDecorator(
                this,
                recyclerView
            )
        )
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        mergeAdapter.onDetachedFromRecyclerView(recyclerView)

        for (i in 0 until recyclerView.itemDecorationCount) {
            val decorator = recyclerView.getItemDecorationAt(i)

            if (decorator is StickyHeaderItemDecorator<*>) {
                recyclerView.removeItemDecoration(decorator)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return mergeAdapter.getItemViewType(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return mergeAdapter.onCreateViewHolder(
            parent,
            viewType
        )
    }

    override fun getItemId(position: Int): Long {
        return mergeAdapter.getItemId(position)
    }

    override fun getItemCount(): Int {
        return mergeAdapter.itemCount
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        mergeAdapter.onBindViewHolder(
            holder,
            position
        )
    }

    override fun getHeaderPositionForItem(itemPosition: Int): Int {
        // the first adapter has no header
        if (itemPosition < filterNameRecyclerViewAdapter.itemCount) {

            return -1
        }

        // if filter area observation have some items
        if (filterAreaObservationRecyclerViewAdapter.itemCount > 0 && itemPosition < (filterNameRecyclerViewAdapter.itemCount + filterAreaObservationRecyclerViewAdapter.itemCount + filterTitleAreaObservationRecyclerViewAdapter.itemCount)) {
            return filterNameRecyclerViewAdapter.itemCount
        }

        if (itemPosition == filterNameRecyclerViewAdapter.itemCount + filterAreaObservationRecyclerViewAdapter.itemCount + filterTitleAreaObservationRecyclerViewAdapter.itemCount + filterTitleTaxonomyRecyclerViewAdapter.itemCount - 1) {
            return itemPosition
        }

        val currentFilterTaxonomy =
            filterTaxonomyRecyclerViewAdapter.items.getOrNull(itemPosition - (filterNameRecyclerViewAdapter.itemCount + filterAreaObservationRecyclerViewAdapter.itemCount + filterTitleAreaObservationRecyclerViewAdapter.itemCount + filterTitleTaxonomyRecyclerViewAdapter.itemCount))
                ?: return filterNameRecyclerViewAdapter.itemCount + filterAreaObservationRecyclerViewAdapter.itemCount + filterTitleAreaObservationRecyclerViewAdapter.itemCount + filterTitleTaxonomyRecyclerViewAdapter.itemCount

        return filterTaxonomyRecyclerViewAdapter.items.indexOfFirst {
            it.value.kingdom == currentFilterTaxonomy.value.kingdom && it.value.group == Taxonomy.ANY
        }.takeIf { it >= 0 }
            ?.plus(filterNameRecyclerViewAdapter.itemCount + filterAreaObservationRecyclerViewAdapter.itemCount + filterTitleAreaObservationRecyclerViewAdapter.itemCount + filterTitleTaxonomyRecyclerViewAdapter.itemCount)
            ?: filterNameRecyclerViewAdapter.itemCount + filterAreaObservationRecyclerViewAdapter.itemCount + filterTitleAreaObservationRecyclerViewAdapter.itemCount + filterTitleTaxonomyRecyclerViewAdapter.itemCount - 1
    }

    override fun onBindHeaderViewHolder(holder: HeaderViewHolder, headerPosition: Int) {
        holder.bind(headerPosition)
    }

    override fun onCreateHeaderViewHolder(parent: ViewGroup): HeaderViewHolder {
        return HeaderViewHolder(parent)
    }

    fun setFilterAreaObservation(
        sectionTitle: String,
        vararg filterAreaObservation: FilterAreaObservation
    ) {
        filterTitleAreaObservationRecyclerViewAdapter.setItems(if (filterAreaObservation.isEmpty()) emptyList() else mutableListOf(sectionTitle))
        filterAreaObservationRecyclerViewAdapter.setItems(filterAreaObservation.asList())
    }

    fun setFilterTaxonomy(sectionTitle: String, vararg filterTaxonomy: FilterTaxonomy) {
        filterTitleTaxonomyRecyclerViewAdapter.setItems(if (filterTaxonomy.isEmpty()) emptyList() else mutableListOf(sectionTitle))
        filterTaxonomyRecyclerViewAdapter.setItems(filterTaxonomy.asList())
    }

    fun setSelectedFilters(vararg filter: Filter<*>) {
        this.selectedFilters.clear()
        this.selectedFilters.addAll(filter)

        (filter.find { it.type == Filter.FilterType.NAME } as FilterName?)?.also {
            this.filterNameRecyclerViewAdapter.setSelectedFilter(it)
        }

        this.filterAreaObservationRecyclerViewAdapter.setSelectedFilters(*filter.filter { it.type == Filter.FilterType.AREA_OBSERVATION }
            .map { FilterAreaObservation(it.value as FilterAreaObservation.AreaObservation) }
            .toTypedArray())

        (filter.find { it.type == Filter.FilterType.TAXONOMY }?.value as Taxonomy?)?.also {
            this.filterTaxonomyRecyclerViewAdapter.setSelectedFilter(FilterTaxonomy(it))
        }
    }

    inner class HeaderViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(
                R.layout.list_header_item_1,
                parent,
                false
            )
    ) {
        private val label: TextView = itemView.findViewById(android.R.id.title)

        fun bind(position: Int) {
            itemView.setBackgroundColor(
                ContextCompat.getColor(
                    itemView.context,
                    R.color.grey_300
                )
            )
            label.setTypeface(
                label.typeface,
                Typeface.BOLD
            )

            if (filterAreaObservationRecyclerViewAdapter.itemCount > 0 && position < (filterNameRecyclerViewAdapter.itemCount + filterAreaObservationRecyclerViewAdapter.itemCount + filterTitleAreaObservationRecyclerViewAdapter.itemCount)) {
                label.text = filterTitleAreaObservationRecyclerViewAdapter.items.getOrNull(0)
                return
            }

            if (position == filterNameRecyclerViewAdapter.itemCount + filterAreaObservationRecyclerViewAdapter.itemCount + filterTitleAreaObservationRecyclerViewAdapter.itemCount + filterTitleTaxonomyRecyclerViewAdapter.itemCount - 1) {
                label.text = filterTitleTaxonomyRecyclerViewAdapter.items.getOrNull(0)
                return
            }

            val taxonomyAsHeader =
                filterTaxonomyRecyclerViewAdapter.items.getOrNull(position - (filterNameRecyclerViewAdapter.itemCount + filterAreaObservationRecyclerViewAdapter.itemCount + filterTitleAreaObservationRecyclerViewAdapter.itemCount + filterTitleTaxonomyRecyclerViewAdapter.itemCount))

            if (taxonomyAsHeader == null) {
                label.text = filterTitleTaxonomyRecyclerViewAdapter.items.getOrNull(0)
                return
            }

            itemView.setBackgroundColor(
                ContextCompat.getColor(
                    itemView.context,
                    R.color.grey_200
                )
            )
            label.text = taxonomyAsHeader.value.kingdom
            label.setTypeface(
                label.typeface,
                Typeface.NORMAL
            )
        }
    }
}