package fr.geonature.occtax.ui.input.taxa

import android.view.View
import android.widget.Switch
import fr.geonature.commons.ui.adapter.AbstractListItemRecyclerViewAdapter
import fr.geonature.occtax.R

/**
 * Default RecyclerView Adapter for [FilterName].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class FilterNameRecyclerViewAdapter(val listener: FilterRecyclerViewAdapterListener<FilterName>) :
    AbstractListItemRecyclerViewAdapter<FilterName>() {

    init {
        setItems(listOf(FilterName(FilterName.Name(FilterName.NameType.SCIENTIFIC))))
    }

    override fun getViewHolder(view: View, viewType: Int): AbstractViewHolder {
        return ViewHolder(view)
    }

    override fun getLayoutResourceId(position: Int, item: FilterName): Int {
        return R.layout.list_item_filter_name
    }

    override fun areItemsTheSame(
        oldItems: List<FilterName>,
        newItems: List<FilterName>,
        oldItemPosition: Int,
        newItemPosition: Int
    ): Boolean {
        return oldItems[oldItemPosition] == newItems[newItemPosition]
    }

    override fun areContentsTheSame(
        oldItems: List<FilterName>,
        newItems: List<FilterName>,
        oldItemPosition: Int,
        newItemPosition: Int
    ): Boolean {
        return oldItems[oldItemPosition] == newItems[newItemPosition]
    }

    fun setSelectedFilter(filter: FilterName) {
        this.setItems(listOf(filter))

        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) :
        AbstractListItemRecyclerViewAdapter<FilterName>.AbstractViewHolder(itemView) {

        private val switch: Switch = itemView.findViewById(R.id.switch_name)

        override fun onBind(item: FilterName) {
            switch.setOnClickListener {
                listener.onSelectedFilters(if (switch.isChecked) FilterName(FilterName.Name(FilterName.NameType.COMMON)) else FilterName(FilterName.Name(FilterName.NameType.SCIENTIFIC)))
            }

            switch.isChecked = item.value.type == FilterName.NameType.COMMON
        }
    }
}