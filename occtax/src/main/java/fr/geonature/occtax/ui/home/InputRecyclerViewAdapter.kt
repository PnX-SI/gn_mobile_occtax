package fr.geonature.occtax.ui.home

import android.text.format.DateFormat
import android.view.View
import android.widget.TextView
import fr.geonature.commons.ui.adapter.AbstractListItemRecyclerViewAdapter
import fr.geonature.occtax.R
import fr.geonature.occtax.input.Input

/**
 * Default RecyclerView Adapter used by [HomeActivity].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class InputRecyclerViewAdapter(listener: OnListItemRecyclerViewAdapterListener<Input>) :
    AbstractListItemRecyclerViewAdapter<Input>(listener) {
    override fun getViewHolder(
        view: View,
        viewType: Int
    ): AbstractViewHolder {
        return ViewHolder(view)
    }

    override fun getLayoutResourceId(
        position: Int,
        item: Input
    ): Int {
        return R.layout.list_item_2
    }

    override fun areItemsTheSame(
        oldItems: List<Input>,
        newItems: List<Input>,
        oldItemPosition: Int,
        newItemPosition: Int
    ): Boolean {
        return oldItems[oldItemPosition].id == newItems[newItemPosition].id
    }

    override fun areContentsTheSame(
        oldItems: List<Input>,
        newItems: List<Input>,
        oldItemPosition: Int,
        newItemPosition: Int
    ): Boolean {
        return oldItems[oldItemPosition] == newItems[newItemPosition]
    }

    inner class ViewHolder(itemView: View) :
        AbstractListItemRecyclerViewAdapter<Input>.AbstractViewHolder(itemView) {
        private val text1: TextView = itemView.findViewById(android.R.id.text1)
        private val text2: TextView = itemView.findViewById(android.R.id.text2)

        override fun onBind(item: Input) {
            text1.text = itemView.context.getString(
                R.string.home_input_created_at,
                DateFormat.format(
                    itemView.context.getString(R.string.home_input_date),
                    item.date
                )
            )
            text2.text = if (item.getInputTaxa().isNotEmpty())
                itemView.resources.getQuantityString(
                    R.plurals.home_input_taxa_count,
                    item.getInputTaxa().size,
                    item.getInputTaxa().size
                ) else itemView.context.getString(R.string.home_input_taxa_count_empty)
        }
    }
}
