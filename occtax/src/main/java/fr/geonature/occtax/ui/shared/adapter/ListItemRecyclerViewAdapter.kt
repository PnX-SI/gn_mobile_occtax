package fr.geonature.occtax.ui.shared.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

/**
 * Base [RecyclerView.Adapter] that is backed by a [List] of arbitrary objects.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
abstract class ListItemRecyclerViewAdapter<T>(private val listener: OnListItemRecyclerViewAdapterListener<T>) : RecyclerView.Adapter<ListItemRecyclerViewAdapter<T>.AbstractViewHolder>() {

    private val items = mutableListOf<T>()

    init {
        this.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()

                listener.showEmptyTextView(itemCount == 0)
            }

            override fun onItemRangeChanged(positionStart: Int,
                                            itemCount: Int) {
                super.onItemRangeChanged(positionStart,
                                         itemCount)

                listener.showEmptyTextView(itemCount == 0)
            }

            override fun onItemRangeInserted(positionStart: Int,
                                             itemCount: Int) {
                super.onItemRangeInserted(positionStart,
                                          itemCount)

                listener.showEmptyTextView(false)
            }

            override fun onItemRangeRemoved(positionStart: Int,
                                            itemCount: Int) {
                super.onItemRangeRemoved(positionStart,
                                         itemCount)

                listener.showEmptyTextView(itemCount == 0)
            }
        })
    }

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): AbstractViewHolder {
        return getViewHolder(LayoutInflater.from(parent.context)
                                 .inflate(viewType,
                                          parent,
                                          false)
                             ,
                             viewType)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: AbstractViewHolder,
                                  position: Int) {
        holder.bind(position,
                    items[position])
    }

    override fun getItemViewType(position: Int): Int {
        return getLayoutResourceId(position,
                                   items[position])
    }

    /**
     * Sets new items.
     */
    fun setItems(newItems: List<T>) {
        if (this.items.isEmpty()) {
            this.items.addAll(newItems)

            if (this.items.isNotEmpty()) {
                notifyItemRangeInserted(0,
                                        this.items.size)
            }
            else {
                notifyDataSetChanged()
            }

            return
        }

        if (items.isEmpty()) {
            this.items.clear()
            notifyDataSetChanged()

            return
        }

        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int {
                return this@ListItemRecyclerViewAdapter.items.size
            }

            override fun getNewListSize(): Int {
                return newItems.size
            }

            override fun areItemsTheSame(oldItemPosition: Int,
                                         newItemPosition: Int): Boolean {
                return this@ListItemRecyclerViewAdapter.areItemsTheSame(this@ListItemRecyclerViewAdapter.items,
                                                                        newItems,
                                                                        oldItemPosition,
                                                                        newItemPosition)
            }

            override fun areContentsTheSame(oldItemPosition: Int,
                                            newItemPosition: Int): Boolean {
                return this@ListItemRecyclerViewAdapter.areContentsTheSame(this@ListItemRecyclerViewAdapter.items,
                                                                           newItems,
                                                                           oldItemPosition,
                                                                           newItemPosition)
            }
        })

        this.items.clear()
        this.items.addAll(newItems)

        diffResult.dispatchUpdatesTo(this)
    }

    /**
     * Add or insert item at given position.
     */
    fun add(item: T,
            index: Int = -1) {
        val position = if (index == -1) this.items.size - 1 else index
        this.items.add(index,
                       item)

        notifyItemInserted(position)
    }

    /**
     * Removes item from the list.
     *
     * @return item position if successfully removed, -1 otherwise
     */
    fun remove(item: T): Int {
        val itemPosition = this.items.indexOf(item)
        val removed = this.items.remove(item)

        if (removed) {
            notifyItemRemoved(itemPosition)

            if (this.items.isEmpty()) {
                notifyDataSetChanged()
            }
        }

        return if (removed) itemPosition else -1
    }

    /**
     * Gets the [AbstractViewHolder] implementation for given view type.
     */
    protected abstract fun getViewHolder(view: View,
                                         viewType: Int): AbstractViewHolder

    /**
     * Gets the layout resource Id at given position.
     */
    @LayoutRes
    protected abstract fun getLayoutResourceId(position: Int,
                                               item: T): Int

    /**
     * Called by the `DiffUtil` to decide whether two object represent the same item.
     */
    protected abstract fun areItemsTheSame(oldItems: List<T>,
                                           newItems: List<T>,
                                           oldItemPosition: Int,
                                           newItemPosition: Int): Boolean

    /**
     * Called by the `DiffUtil` when it wants to check whether two items have the same data.
     */
    protected abstract fun areContentsTheSame(oldItems: List<T>,
                                              newItems: List<T>,
                                              oldItemPosition: Int,
                                              newItemPosition: Int): Boolean

    abstract inner class AbstractViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(position: Int,
                 item: T) {
            onBind(item)

            with(itemView) {
                setOnClickListener {
                    listener.onClick(item)
                }
                setOnLongClickListener {
                    listener.onLongClicked(position,
                                           item)
                    true
                }
            }
        }

        abstract fun onBind(item: T)
    }

    /**
     * Callback used by [ListItemRecyclerViewAdapter].
     */
    interface OnListItemRecyclerViewAdapterListener<T> {

        /**
         * Called when an item has been clicked.
         *
         * @param item the selected item to edit
         */
        fun onClick(item: T)

        /**
         * Called when an item has been clicked and held.
         *
         * @param item the selected item
         */
        fun onLongClicked(position: Int,
                          item: T)

        /**
         * Whether to show an empty text view when data changed.
         */
        fun showEmptyTextView(show: Boolean)
    }
}