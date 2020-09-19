package fr.geonature.occtax.ui.input.counting

import android.text.Spanned
import android.view.View
import android.widget.TextView
import androidx.core.text.HtmlCompat
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
        return R.layout.list_item_counting
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
        private val title: TextView = itemView.findViewById(android.R.id.title)
        private val text1: TextView = itemView.findViewById(android.R.id.text1)
        private val text2: TextView = itemView.findViewById(android.R.id.text2)

        override fun onBind(item: CountingMetadata) {
            title.text = itemView.context.getString(
                R.string.counting_main_label,
                item.index
            )
            text1.text = buildCountingDescription(item)
            text2.text = buildDescription(item)
            text2.isSelected = true
        }

        private fun buildCountingDescription(countingMetadata: CountingMetadata): Spanned {
            return HtmlCompat.fromHtml(arrayOf(
                Pair(
                    itemView.context.getString(R.string.counting_min_label),
                    countingMetadata.min.toString()
                ),
                Pair(
                    itemView.context.getString(R.string.counting_max_label),
                    countingMetadata.max.toString()
                )
            ).asSequence()
                .map {
                    itemView.context.getString(
                        R.string.counting_description_separator,
                        it.first,
                        it.second
                    )
                }
                .joinToString(", "),
                HtmlCompat.FROM_HTML_MODE_LEGACY)
        }

        private fun buildDescription(countingMetadata: CountingMetadata): Spanned {
            return HtmlCompat.fromHtml(countingMetadata.properties.values
                .asSequence()
                .filterNot { it.isEmpty() }
                .map {
                    itemView.context.getString(
                        R.string.counting_description_separator,
                        getNomenclatureTypeLabel(it.code),
                        it.label
                    )
                }
                .joinToString(", "),
                HtmlCompat.FROM_HTML_MODE_LEGACY)
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
