package fr.geonature.occtax.features.nomenclature.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.net.toUri
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import fr.geonature.occtax.R
import fr.geonature.occtax.features.nomenclature.domain.EditableField
import fr.geonature.occtax.features.record.domain.MediaRecord
import fr.geonature.occtax.features.record.domain.PropertyValue
import java.io.File

/**
 * [EditableFieldAdapter] view holder representing a list of media (i.e. a list of images or photos
 * taken).
 *
 * @author S. Grimault
 */
class MediaViewHolder(
    parent: ViewGroup,
    private val listener: EditableFieldAdapter.OnEditableFieldAdapter
) : EditableFieldAdapter.AbstractViewHolder(
    LayoutInflater.from(parent.context)
        .inflate(
            R.layout.view_action_media,
            parent,
            false
        )
) {
    private var title: TextView = itemView.findViewById(android.R.id.title)
    private var recyclerView: RecyclerView = itemView.findViewById(android.R.id.list)
    private var adapter: MediaAdapter = MediaAdapter()

    init {
        with(recyclerView) {
            layoutManager = GridLayoutManager(
                context,
                2
            )
            adapter = this@MediaViewHolder.adapter
        }
    }

    override fun onBind(editableField: EditableField) {
        title.text = editableField.label ?: getDefaultLabel(editableField)
        adapter.setItems(editableField.value
            .takeIf { it is PropertyValue.Media }
            ?.let { it as PropertyValue.Media }?.value
            ?.filterIsInstance<MediaRecord.File>()
            ?.map { it.path }
            ?.mapNotNull { runCatching { File(it) }.getOrNull() }
            ?: emptyList())
    }

    private inner class MediaAdapter :
        RecyclerView.Adapter<MediaAdapter.AbstractViewHolder>() {

        private val items = mutableListOf<File>()

        init {
            this.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                override fun onChanged() {
                    super.onChanged()

                    onUpdate()
                }

                override fun onItemRangeChanged(
                    positionStart: Int,
                    itemCount: Int
                ) {
                    super.onItemRangeChanged(
                        positionStart,
                        itemCount
                    )

                    onUpdate()
                }

                override fun onItemRangeInserted(
                    positionStart: Int,
                    itemCount: Int
                ) {
                    super.onItemRangeInserted(
                        positionStart,
                        itemCount
                    )

                    onUpdate()
                }

                override fun onItemRangeRemoved(
                    positionStart: Int,
                    itemCount: Int
                ) {
                    super.onItemRangeRemoved(
                        positionStart,
                        itemCount
                    )

                    onUpdate()
                }
            })
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AbstractViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(
                    viewType,
                    parent,
                    false
                )

            return when (viewType) {
                R.layout.list_item_media_add -> AddImageViewHolder(view)
                else -> ImageViewHolder(view)
            }
        }

        override fun onBindViewHolder(holder: AbstractViewHolder, position: Int) {
            holder.onBind(if ((itemCount - 1) == position) null else items[position])
        }

        override fun getItemCount(): Int {
            return items.size + 1
        }

        override fun getItemViewType(position: Int): Int {
            return if ((itemCount - 1) == position) R.layout.list_item_media_add
            else R.layout.list_item_media_thumbnail
        }

        fun setItems(items: List<File>) {
            val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun getOldListSize(): Int {
                    return this@MediaAdapter.items.size
                }

                override fun getNewListSize(): Int {
                    return items.size
                }

                override fun areItemsTheSame(
                    oldItemPosition: Int,
                    newItemPosition: Int
                ): Boolean {
                    return this@MediaAdapter.items[oldItemPosition] == items[newItemPosition]
                }

                override fun areContentsTheSame(
                    oldItemPosition: Int,
                    newItemPosition: Int
                ): Boolean {
                    return this@MediaAdapter.items[oldItemPosition] == items[newItemPosition]
                }
            })

            with(this.items) {
                clear()
                addAll(items)
            }

            diffResult.dispatchUpdatesTo(this)
        }

        fun onUpdate() {
            editableField?.also {
                listener.onUpdate(it.apply {
                    value = PropertyValue.Media(
                        it.code,
                        items.map { file -> MediaRecord.File(file.absolutePath) }
                            .toTypedArray()
                    )
                })
            }
        }

        abstract inner class AbstractViewHolder(itemView: View) :
            RecyclerView.ViewHolder(itemView) {

            abstract fun onBind(file: File?)
        }

        inner class ImageViewHolder(itemView: View) : AbstractViewHolder(itemView) {
            override fun onBind(file: File?) {
                itemView.findViewById<ShapeableImageView>(R.id.image)
                    .apply {
                        setImageURI(file?.toUri())
                        setOnClickListener {
                            file?.absolutePath?.also {
                                listener.onMediaSelected(MediaRecord.File(it))
                            }
                        }
                        setOnLongClickListener {
                            val currentPosition = items.indexOf(file)

                            this@MediaAdapter.setItems(items.filter { it.absolutePath != file?.absolutePath })

                            makeSnackbar(itemView.context.getString(R.string.counting_media_deleted))?.setAction(R.string.counting_media_action_undo) {
                                file?.also {
                                    this@MediaAdapter.setItems(
                                        items.toMutableList()
                                            .apply {
                                                add(
                                                    currentPosition,
                                                    it
                                                )
                                            })
                                }
                            }
                                ?.show()

                            true
                        }
                    }
            }
        }

        inner class AddImageViewHolder(itemView: View) : AbstractViewHolder(itemView) {
            override fun onBind(file: File?) {
                itemView.findViewById<MaterialButton>(android.R.id.button1)
                    .setOnClickListener {
                        editableField?.code?.also {
                            listener.onAddMedia(it)
                        }
                    }
            }
        }
    }

    private fun makeSnackbar(
        text: CharSequence,
    ): Snackbar? {
        val view = listener.getCoordinatorLayout() ?: return null

        return Snackbar.make(
            view,
            text,
            BaseTransientBottomBar.LENGTH_LONG
        )
    }
}