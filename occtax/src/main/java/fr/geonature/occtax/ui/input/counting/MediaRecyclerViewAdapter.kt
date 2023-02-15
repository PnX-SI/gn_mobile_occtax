package fr.geonature.occtax.ui.input.counting

import android.net.Uri
import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import fr.geonature.commons.ui.adapter.AbstractListItemRecyclerViewAdapter
import fr.geonature.occtax.R
import java.io.File

/**
 * Default RecyclerView Adapter used by [MediaListActivity].
 *
 * @author S. Grimault
 */
class MediaRecyclerViewAdapter(listener: OnMediaRecyclerViewAdapterListener) :
    AbstractListItemRecyclerViewAdapter<File>(listener) {

    init {
        this.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()

                listener.onUpdate(items)
            }

            override fun onItemRangeChanged(
                positionStart: Int,
                itemCount: Int
            ) {
                super.onItemRangeChanged(
                    positionStart,
                    itemCount
                )

                listener.onUpdate(items)
            }

            override fun onItemRangeInserted(
                positionStart: Int,
                itemCount: Int
            ) {
                super.onItemRangeInserted(
                    positionStart,
                    itemCount
                )

                listener.onUpdate(items)
            }

            override fun onItemRangeRemoved(
                positionStart: Int,
                itemCount: Int
            ) {
                super.onItemRangeRemoved(
                    positionStart,
                    itemCount
                )

                listener.onUpdate(items)
            }
        })
    }

    override fun getViewHolder(view: View, viewType: Int): AbstractViewHolder {
        return ViewHolder(view)
    }

    override fun getLayoutResourceId(position: Int, item: File): Int {
        return R.layout.list_item_media
    }

    override fun areItemsTheSame(
        oldItems: List<File>,
        newItems: List<File>,
        oldItemPosition: Int,
        newItemPosition: Int
    ): Boolean {
        return oldItems[oldItemPosition] == newItems[newItemPosition]
    }

    override fun areContentsTheSame(
        oldItems: List<File>,
        newItems: List<File>,
        oldItemPosition: Int,
        newItemPosition: Int
    ): Boolean {
        return oldItems[oldItemPosition] == newItems[newItemPosition]
    }

    inner class ViewHolder(itemView: View) :
        AbstractListItemRecyclerViewAdapter<File>.AbstractViewHolder(itemView) {
        override fun onBind(item: File) {
            itemView.findViewById<ImageView>(R.id.image).setImageURI(Uri.fromFile(item))
        }
    }

    interface OnMediaRecyclerViewAdapterListener : OnListItemRecyclerViewAdapterListener<File> {
        fun onUpdate(medias: List<File>)
    }
}