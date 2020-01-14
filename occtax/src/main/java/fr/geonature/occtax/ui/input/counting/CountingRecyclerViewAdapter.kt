package fr.geonature.occtax.ui.input.counting

import android.view.View
import android.widget.TextView
import fr.geonature.commons.ui.adapter.AbstractListItemRecyclerViewAdapter
import fr.geonature.occtax.R
import fr.geonature.occtax.input.CountingMetadata
import java.util.Locale

/**
 * Default RecyclerView Adapter used by [CountingFragment].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class CountingRecyclerViewAdapter(listener: OnListItemRecyclerViewAdapterListener<CountingMetadata>) :
    AbstractListItemRecyclerViewAdapter<CountingMetadata>(listener) {

    override fun getViewHolder(
        view: View,
        viewType: Int
    ): AbstractViewHolder {
        return ViewHolder(view)
    }

    override fun getLayoutResourceId(
        position: Int,
        item: CountingMetadata
    ): Int {
        return R.layout.list_item_2
    }

    override fun areItemsTheSame(
        oldItems: List<CountingMetadata>,
        newItems: List<CountingMetadata>,
        oldItemPosition: Int,
        newItemPosition: Int
    ): Boolean {
        return oldItems[oldItemPosition].index == newItems[newItemPosition].index
    }

    override fun areContentsTheSame(
        oldItems: List<CountingMetadata>,
        newItems: List<CountingMetadata>,
        oldItemPosition: Int,
        newItemPosition: Int
    ): Boolean {
        return oldItems[oldItemPosition] == newItems[newItemPosition]
    }

    inner class ViewHolder(itemView: View) :
        AbstractListItemRecyclerViewAdapter<CountingMetadata>.AbstractViewHolder(itemView) {
        private val text1: TextView = itemView.findViewById(android.R.id.text1)
        private val text2: TextView = itemView.findViewById(android.R.id.text2)

        override fun onBind(item: CountingMetadata) {
            text1.text = itemView.context.getString(
                R.string.counting_main_label,
                item.index
            )
            text2.text = buildDescription(item)
        }

        private fun buildDescription(countingMetadata: CountingMetadata): String {
            return (countingMetadata.properties.values.asSequence()
                .filterNot { it.isEmpty() }
                .map {
                    itemView.context.getString(
                        R.string.counting_description_separator,
                        getNomenclatureTypeLabel(it.code),
                        it.label
                    )
                } + arrayOf(
                Pair(
                    R.string.counting_min_label,
                    countingMetadata.min
                ),
                Pair(
                    R.string.counting_max_label,
                    countingMetadata.max
                )
            ).asSequence()
                .map {
                    itemView.context.getString(
                        R.string.counting_description_separator,
                        itemView.context.getString(it.first),
                        it.second.toString()
                    )
                })
                .joinToString(", ")
        }

        private fun getNomenclatureTypeLabel(mnemonic: String): String {
            val resourceId = itemView.resources.getIdentifier(
                "nomenclature_${mnemonic.toLowerCase(Locale.getDefault())}",
                "string",
                itemView.context.packageName
            )

            return if (resourceId == 0) mnemonic else itemView.context.getString(resourceId)
        }
    }
}
