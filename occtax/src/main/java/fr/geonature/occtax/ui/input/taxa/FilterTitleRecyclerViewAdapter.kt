package fr.geonature.occtax.ui.input.taxa

import android.view.View
import android.widget.TextView
import fr.geonature.commons.ui.adapter.AbstractListItemRecyclerViewAdapter
import fr.geonature.occtax.R

/**
 * Default RecyclerView Adapter to add a section title.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class FilterTitleRecyclerViewAdapter :
    AbstractListItemRecyclerViewAdapter<String>(object :
        OnListItemRecyclerViewAdapterListener<String> {
        override fun onClick(item: String) {
            // nothing to do...
        }

        override fun onLongClicked(position: Int, item: String) {
            // nothing to do...
        }

        override fun showEmptyTextView(show: Boolean) {
            // nothing to do...
        }
    }) {

    override fun getViewHolder(view: View, viewType: Int): AbstractViewHolder {
        return ViewHolder(view)
    }

    override fun getLayoutResourceId(position: Int, item: String): Int {
        return R.layout.list_header_item_1
    }

    override fun areItemsTheSame(
        oldItems: List<String>,
        newItems: List<String>,
        oldItemPosition: Int,
        newItemPosition: Int
    ): Boolean {
        return oldItems[oldItemPosition] == newItems[newItemPosition]
    }

    override fun areContentsTheSame(
        oldItems: List<String>,
        newItems: List<String>,
        oldItemPosition: Int,
        newItemPosition: Int
    ): Boolean {
        return oldItems[oldItemPosition] == newItems[newItemPosition]
    }

    inner class ViewHolder(itemView: View) :
        AbstractListItemRecyclerViewAdapter<String>.AbstractViewHolder(itemView) {

        private val label: TextView = itemView.findViewById(android.R.id.title)

        override fun onBind(item: String) {
            label.text = item
        }
    }
}