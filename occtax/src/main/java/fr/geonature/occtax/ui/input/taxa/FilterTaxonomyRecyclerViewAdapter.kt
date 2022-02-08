package fr.geonature.occtax.ui.input.taxa

import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import fr.geonature.commons.data.entity.Taxonomy
import fr.geonature.commons.ui.adapter.AbstractListItemRecyclerViewAdapter
import fr.geonature.occtax.R

/**
 * Default RecyclerView Adapter for [FilterTaxonomy].
 *
 * @author S. Grimault
 */
class FilterTaxonomyRecyclerViewAdapter(val listener: FilterRecyclerViewAdapterListener<FilterTaxonomy>) :
    AbstractListItemRecyclerViewAdapter<FilterTaxonomy>() {

    private var selectedFilter: FilterTaxonomy? = null

    override fun getViewHolder(view: View, viewType: Int): AbstractViewHolder {
        return ViewHolder(view)
    }

    override fun getLayoutResourceId(position: Int, item: FilterTaxonomy): Int {
        return if (item.value.group == Taxonomy.ANY) R.layout.list_selectable_header_item_1 else R.layout.list_selectable_item_1
    }

    override fun areItemsTheSame(
        oldItems: List<FilterTaxonomy>,
        newItems: List<FilterTaxonomy>,
        oldItemPosition: Int,
        newItemPosition: Int
    ): Boolean {
        return oldItems[oldItemPosition] == newItems[newItemPosition]
    }

    override fun areContentsTheSame(
        oldItems: List<FilterTaxonomy>,
        newItems: List<FilterTaxonomy>,
        oldItemPosition: Int,
        newItemPosition: Int
    ): Boolean {
        return oldItems[oldItemPosition] == newItems[newItemPosition]
    }

    fun setSelectedFilter(filter: FilterTaxonomy) {
        clearSelection()
        selectedFilter = filter

        val selectedItemPosition = selectedFilter?.let { items.indexOf(it) } ?: -1

        if (selectedItemPosition >= 0) {
            notifyItemChanged(selectedItemPosition)
        }
    }

    fun clearSelection() {
        val previousSelectedItemPosition = selectedFilter?.let { items.indexOf(it) } ?: -1

        selectedFilter = null

        if (previousSelectedItemPosition >= 0) {
            notifyItemChanged(previousSelectedItemPosition)
        }
    }

    inner class ViewHolder(itemView: View) :
        AbstractListItemRecyclerViewAdapter<FilterTaxonomy>.AbstractViewHolder(itemView) {

        private val title: TextView = itemView.findViewById(android.R.id.title)
        private val checkbox: CheckBox = itemView.findViewById(android.R.id.checkbox)

        override fun onBind(item: FilterTaxonomy) {
            itemView.setOnClickListener {
                val previousSelectedItemPosition = selectedFilter?.let { items.indexOf(it) } ?: -1

                checkbox.isChecked = !checkbox.isChecked

                if (checkbox.isChecked) {
                    selectedFilter = item
                    listener.onSelectedFilters(*mutableListOf(item).toTypedArray())
                } else {
                    selectedFilter = null
                    listener.onSelectedFilters()
                }

                if (previousSelectedItemPosition >= 0) {
                    notifyItemChanged(previousSelectedItemPosition)
                }
            }

            title.text =
                if (item.value.group == Taxonomy.ANY) item.value.kingdom else item.value.group
            checkbox.isChecked = selectedFilter?.value == item.value
        }
    }
}