package fr.geonature.occtax.ui.input.summary

import android.view.View
import android.widget.TextView
import fr.geonature.commons.input.AbstractInputTaxon
import fr.geonature.occtax.R
import fr.geonature.occtax.ui.shared.adapter.ListItemRecyclerViewAdapter

/**
 * Default RecyclerView Adapter used by [InputTaxaSummaryFragment].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class InputTaxaSummaryRecyclerViewAdapter(listener: OnListItemRecyclerViewAdapterListener<AbstractInputTaxon>) : ListItemRecyclerViewAdapter<AbstractInputTaxon>(listener) {

    override fun getViewHolder(view: View,
                               viewType: Int): AbstractViewHolder {
        return ViewHolder(view)
    }

    override fun getLayoutResourceId(position: Int,
                                     item: AbstractInputTaxon): Int {
        return R.layout.list_item_2
    }

    override fun areItemsTheSame(oldItems: List<AbstractInputTaxon>,
                                 newItems: List<AbstractInputTaxon>,
                                 oldItemPosition: Int,
                                 newItemPosition: Int): Boolean {
        return oldItems[oldItemPosition].taxon.id == newItems[newItemPosition].taxon.id
    }

    override fun areContentsTheSame(oldItems: List<AbstractInputTaxon>,
                                    newItems: List<AbstractInputTaxon>,
                                    oldItemPosition: Int,
                                    newItemPosition: Int): Boolean {
        return oldItems[oldItemPosition] == newItems[newItemPosition]
    }

    inner class ViewHolder(itemView: View) : ListItemRecyclerViewAdapter<AbstractInputTaxon>.AbstractViewHolder(itemView) {
        private val text1: TextView = itemView.findViewById(android.R.id.text1)

        override fun onBind(item: AbstractInputTaxon) {
            text1.text = item.taxon.name
        }
    }
}