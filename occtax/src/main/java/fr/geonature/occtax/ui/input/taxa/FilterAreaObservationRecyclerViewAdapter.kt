package fr.geonature.occtax.ui.input.taxa

import android.graphics.Color
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import androidx.annotation.ColorInt
import fr.geonature.commons.ui.adapter.AbstractListItemRecyclerViewAdapter
import fr.geonature.occtax.R

/**
 * Default RecyclerView Adapter for [FilterAreaObservation].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class FilterAreaObservationRecyclerViewAdapter(val listener: FilterRecyclerViewAdapterListener<FilterAreaObservation>) :
    AbstractListItemRecyclerViewAdapter<FilterAreaObservation>() {

    private val selectedFilters: MutableList<FilterAreaObservation> = mutableListOf()

    override fun getViewHolder(view: View, viewType: Int): AbstractViewHolder {
        return ViewHolder(view)
    }

    override fun getLayoutResourceId(position: Int, item: FilterAreaObservation): Int {
        return R.layout.list_item_filter_area_observation
    }

    override fun areItemsTheSame(
        oldItems: List<FilterAreaObservation>,
        newItems: List<FilterAreaObservation>,
        oldItemPosition: Int,
        newItemPosition: Int
    ): Boolean {
        return oldItems[oldItemPosition] == newItems[newItemPosition]
    }

    override fun areContentsTheSame(
        oldItems: List<FilterAreaObservation>,
        newItems: List<FilterAreaObservation>,
        oldItemPosition: Int,
        newItemPosition: Int
    ): Boolean {
        return oldItems[oldItemPosition] == newItems[newItemPosition]
    }

    fun setSelectedFilters(vararg filter: FilterAreaObservation) {
        this.selectedFilters.clear()
        this.selectedFilters.addAll(filter)

        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) :
        AbstractListItemRecyclerViewAdapter<FilterAreaObservation>.AbstractViewHolder(itemView) {

        private val label: TextView = itemView.findViewById(android.R.id.title)
        private val checkbox: CheckBox = itemView.findViewById(android.R.id.checkbox)
        private val colorView: View = itemView.findViewById(R.id.color_view)

        override fun onBind(item: FilterAreaObservation) {
            itemView.setOnClickListener {
                checkbox.isChecked = !checkbox.isChecked

                if (checkbox.isChecked) {
                    selectedFilters.add(item)
                } else {
                    selectedFilters.filter { it.value.type != item.value.type }.also {
                        selectedFilters.clear()
                        selectedFilters.addAll(it)
                    }
                }

                listener.onSelectedFilters(*selectedFilters.toTypedArray())
            }

            label.text = item.value.label
            colorView.setBackgroundColor(getColorFromAreaObservationType(item.value.type))
            checkbox.isChecked = selectedFilters.find { it.value.type == item.value.type } != null
        }

        @ColorInt
        private fun getColorFromAreaObservationType(type: FilterAreaObservation.AreaObservationType): Int {
            return when (type) {
                FilterAreaObservation.AreaObservationType.MORE_THAN_DURATION -> Color.RED
                FilterAreaObservation.AreaObservationType.LESS_THAN_DURATION -> Color.GRAY
                else -> Color.TRANSPARENT
            }
        }
    }
}