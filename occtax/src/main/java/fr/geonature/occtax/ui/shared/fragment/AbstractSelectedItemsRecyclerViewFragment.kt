package fr.geonature.occtax.ui.shared.fragment

import android.os.Bundle
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import fr.geonature.occtax.R
import fr.geonature.occtax.ui.shared.view.ListItemActionView

/**
 * Edit a list of selected items.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
abstract class AbstractSelectedItemsRecyclerViewFragment : Fragment() {

    private var adapter: SelectedItemsRecyclerViewAdapter? = null

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_selected_items_recycler_view,
                                    container,
                                    false)

        // Set the adapter
        if (view is RecyclerView) {
            adapter = SelectedItemsRecyclerViewAdapter()
            view.adapter = adapter
        }

        return view
    }

    fun notifyItemChanged(position: Int) {
        adapter?.notifyItemChanged(position)
    }

    abstract fun getSelectedItemsCount(): Int

    @StringRes
    abstract fun getSelectedItemsTitle(position: Int): Int

    @StringRes
    abstract fun getSelectedItemsEmptyText(position: Int): Int

    @StringRes
    abstract fun getActionText(position: Int): Int

    @StringRes
    abstract fun getActionEmptyText(position: Int): Int

    abstract fun getVisibleItems(position: Int): Int

    abstract fun getSelectedItems(position: Int): Collection<Pair<String, String?>>

    /**
     * Called when the action button has been clicked at given position.
     */
    abstract fun onSelectedItemsAction(position: Int)

    private inner class SelectedItemsRecyclerViewAdapter : RecyclerView.Adapter<SelectedItemsRecyclerViewAdapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup,
                                        viewType: Int): ViewHolder {
            return ListItemActionViewHolder(parent,
                                            viewType)
        }

        override fun getItemCount(): Int {
            return getSelectedItemsCount()
        }

        override fun onBindViewHolder(holder: ViewHolder,
                                      position: Int) {
            holder.bind(position)
        }

        override fun getItemViewType(position: Int): Int {
            return position
        }

        abstract inner class ViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(
                R.layout.card_view,
                parent,
                false)) {

            abstract fun bind(position: Int)
        }

        inner class ListItemActionViewHolder(parent: ViewGroup,
                                             viewType: Int) : ViewHolder(parent) {

            private val listItemActionView: ListItemActionView = ListItemActionView(itemView.context)

            init {
                (itemView as CardView).addView(listItemActionView)

                listItemActionView.setListener(object : ListItemActionView.OnListItemActionViewListener {
                    override fun onAction() {
                        onSelectedItemsAction(viewType)
                    }
                })

                listItemActionView.setTitle(getSelectedItemsTitle(viewType))
                listItemActionView.setEmptyText(getSelectedItemsEmptyText(viewType))
                listItemActionView.setActionText(getActionText(viewType))
                listItemActionView.setActionEmptyText(getActionEmptyText(viewType))
                listItemActionView.setVisibleItems(getVisibleItems(viewType))
            }

            override fun bind(position: Int) {
                listItemActionView.setItems(getSelectedItems(position))
            }
        }
    }
}