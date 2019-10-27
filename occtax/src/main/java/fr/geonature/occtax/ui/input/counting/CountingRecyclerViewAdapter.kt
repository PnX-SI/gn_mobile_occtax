package fr.geonature.occtax.ui.input.counting

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import fr.geonature.occtax.R
import fr.geonature.occtax.input.CountingMetadata
import java.util.Locale

/**
 * Default RecyclerView Adapter used by [CountingFragment].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class CountingRecyclerViewAdapter(private val listener: OnCountingRecyclerViewAdapterListener) : RecyclerView.Adapter<CountingRecyclerViewAdapter.ViewHolder>() {

    private val counting = mutableListOf<CountingMetadata>()
    private val onClickListener: View.OnClickListener

    init {
        registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
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

        onClickListener = View.OnClickListener { v ->
            val counting = v.tag as CountingMetadata
            listener.onClick(counting)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): ViewHolder {
        return ViewHolder(parent)
    }

    override fun getItemCount(): Int {
        return counting.size
    }

    override fun onBindViewHolder(holder: ViewHolder,
                                  position: Int) {
        holder.bind(position)
    }

    fun setCounting(counting: List<CountingMetadata>) {
        val newList = counting.distinct()

        if (this.counting.isEmpty()) {
            this.counting.addAll(newList)

            if (this.counting.isNotEmpty()) {
                notifyItemRangeInserted(0,
                                        this.counting.size)
            }

            return
        }

        if (counting.isEmpty()) {
            this.counting.clear()
            notifyDataSetChanged()

            return
        }

        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int {
                return this@CountingRecyclerViewAdapter.counting.size
            }

            override fun getNewListSize(): Int {
                return newList.size
            }

            override fun areItemsTheSame(oldItemPosition: Int,
                                         newItemPosition: Int): Boolean {
                return this@CountingRecyclerViewAdapter.counting.toList()[oldItemPosition].index == newList[newItemPosition].index
            }

            override fun areContentsTheSame(oldItemPosition: Int,
                                            newItemPosition: Int): Boolean {
                return this@CountingRecyclerViewAdapter.counting.toList()[oldItemPosition] == newList[newItemPosition]
            }
        })

        this.counting.clear()
        this.counting.addAll(newList)

        diffResult.dispatchUpdatesTo(this)
    }

    inner class ViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_2,
                                                                                                                    parent,
                                                                                                                    false)) {
        private val text1: TextView = itemView.findViewById(android.R.id.text1)
        private val text2: TextView = itemView.findViewById(android.R.id.text2)

        fun bind(position: Int) {
            val countingMetadata = counting[position]

            text1.text = itemView.context.getString(R.string.counting_main_label,
                                                    countingMetadata.index)
            text2.text = buildDescription(countingMetadata)

            with(itemView) {
                tag = countingMetadata
                setOnClickListener(onClickListener)
                setOnLongClickListener {
                    listener.onLongClicked(counting.indexOf(countingMetadata),
                                           countingMetadata)
                    true
                }
            }
        }

        private fun buildDescription(countingMetadata: CountingMetadata): String {
            return (countingMetadata.properties.values.asSequence()
                .filterNot { it.isEmpty() }
                .map {
                    itemView.context.getString(R.string.counting_description_separator,
                                               getNomenclatureTypeLabel(it.code),
                                               it.label)
                } + arrayOf(Pair(R.string.counting_min_label,
                                 countingMetadata.min),
                            Pair(R.string.counting_max_label,
                                 countingMetadata.max)).asSequence()
                .map {
                    itemView.context.getString(R.string.counting_description_separator,
                                               itemView.context.getString(it.first),
                                               it.second.toString())
                })
                .joinToString(", ")
        }

        private fun getNomenclatureTypeLabel(mnemonic: String): String {
            val resourceId = itemView.resources.getIdentifier("nomenclature_${mnemonic.toLowerCase(Locale.getDefault())}",
                                                              "string",
                                                              itemView.context.packageName)

            return if (resourceId == 0) mnemonic else itemView.context.getString(resourceId)
        }
    }

    /**
     * Callback used by [CountingRecyclerViewAdapter].
     */
    interface OnCountingRecyclerViewAdapterListener {

        /**
         * Called when a [CountingMetadata] has been clicked.
         *
         * @param countingMetadata the selected CountingMetadata to edit
         */
        fun onClick(countingMetadata: CountingMetadata)

        /**
         * Called when a [CountingMetadata] has been clicked and held.
         *
         * @param countingMetadata the selected [CountingMetadata]
         */
        fun onLongClicked(position: Int,
                          countingMetadata: CountingMetadata)

        /**
         * Whether to show an empty text view when data changed.
         */
        fun showEmptyTextView(show: Boolean)
    }
}