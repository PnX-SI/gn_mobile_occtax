package fr.geonature.occtax.ui.input.counting

import android.annotation.SuppressLint
import android.text.Spanned
import android.view.View
import android.widget.TextView
import androidx.core.text.HtmlCompat
import fr.geonature.commons.ui.adapter.AbstractListItemRecyclerViewAdapter
import fr.geonature.occtax.R
import fr.geonature.occtax.features.record.domain.CountingRecord
import fr.geonature.occtax.features.record.domain.PropertyValue

/**
 * Default RecyclerView Adapter used by [CountingFragment]. Shows a list of [CountingRecord].
 *
 * @author S. Grimault
 */
class CountingRecyclerViewAdapter(listener: OnListItemRecyclerViewAdapterListener<CountingRecord>) :
    AbstractListItemRecyclerViewAdapter<CountingRecord>(listener) {

    override fun getViewHolder(
        view: View,
        viewType: Int
    ): AbstractViewHolder {
        return ViewHolder(view)
    }

    override fun getLayoutResourceId(
        position: Int,
        item: CountingRecord
    ): Int {
        return R.layout.list_item_counting
    }

    override fun areItemsTheSame(
        oldItems: List<CountingRecord>,
        newItems: List<CountingRecord>,
        oldItemPosition: Int,
        newItemPosition: Int
    ): Boolean {
        return oldItems[oldItemPosition].index == newItems[newItemPosition].index
    }

    override fun areContentsTheSame(
        oldItems: List<CountingRecord>,
        newItems: List<CountingRecord>,
        oldItemPosition: Int,
        newItemPosition: Int
    ): Boolean {
        return oldItems[oldItemPosition] == newItems[newItemPosition]
    }

    inner class ViewHolder(itemView: View) :
        AbstractListItemRecyclerViewAdapter<CountingRecord>.AbstractViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(android.R.id.title)
        private val text1: TextView = itemView.findViewById(android.R.id.text1)
        private val text2: TextView = itemView.findViewById(android.R.id.text2)
        private val summary: TextView = itemView.findViewById(android.R.id.summary)

        override fun onBind(item: CountingRecord) {
            title.text = itemView.context.getString(
                R.string.counting_main_label,
                item.index
            )
            text1.text = buildCountingDescription(item)
            text2.text = buildMediaDescription(item)
            summary.text = buildDescription(item)
            summary.isSelected = true
        }

        private fun buildCountingDescription(countingRecord: CountingRecord): Spanned {
            return HtmlCompat.fromHtml(arrayOf(
                countingRecord.properties[CountingRecord.MIN_KEY],
                countingRecord.properties[CountingRecord.MAX_KEY],
            ).asSequence()
                .filterNotNull()
                .map { it.toPair() }
                .map { it.first to it.second as PropertyValue.Number }
                .map { getNomenclatureTypeLabel(it.first) to it.second }
                .map {
                    itemView.context.getString(
                        R.string.counting_description_separator,
                        it.first,
                        it.second.value
                    )
                }
                .joinToString(", "),
                HtmlCompat.FROM_HTML_MODE_LEGACY)
        }

        private fun buildMediaDescription(countingRecord: CountingRecord): CharSequence {
            return countingRecord.medias.files.size.let {
                itemView.resources.getQuantityString(
                    R.plurals.counting_media,
                    it,
                    it
                )
            }
        }

        private fun buildDescription(countingRecord: CountingRecord): Spanned {
            return HtmlCompat.fromHtml(countingRecord.properties.values
                .asSequence()
                .filterNot { it.isEmpty() }
                .map { it.toPair() }
                .filterNot {
                    listOf(
                        CountingRecord.MIN_KEY,
                        CountingRecord.MAX_KEY
                    ).contains(it.first)
                }
                .map {
                    it.first to when (it.second) {
                        is PropertyValue.Number -> (it.second as PropertyValue.Number).value
                        is PropertyValue.Text -> (it.second as PropertyValue.Text).value
                        is PropertyValue.Nomenclature -> (it.second as PropertyValue.Nomenclature).label
                        else -> null
                    }
                }
                .filterNot { it.second === null }
                .map {
                    itemView.context.getString(
                        R.string.counting_description_separator,
                        getNomenclatureTypeLabel(it.first),
                        it.second
                    )
                }
                .joinToString(", "),
                HtmlCompat.FROM_HTML_MODE_LEGACY)
        }

        @SuppressLint("DiscouragedApi")
        private fun getNomenclatureTypeLabel(mnemonic: String): String {
            val resourceId = itemView.resources.getIdentifier(
                "nomenclature_${mnemonic.lowercase()}",
                "string",
                itemView.context.packageName
            )

            return if (resourceId == 0) mnemonic else itemView.context.getString(resourceId)
        }
    }
}
