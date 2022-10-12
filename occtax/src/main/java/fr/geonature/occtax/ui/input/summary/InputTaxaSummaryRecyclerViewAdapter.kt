package fr.geonature.occtax.ui.input.summary

import android.text.Spanned
import android.text.SpannedString
import android.view.View
import android.widget.TextView
import androidx.core.text.HtmlCompat
import fr.geonature.commons.features.input.domain.AbstractInputTaxon
import fr.geonature.commons.ui.adapter.AbstractListItemRecyclerViewAdapter
import fr.geonature.occtax.R
import fr.geonature.occtax.features.input.domain.InputTaxon
import fr.geonature.occtax.features.input.domain.PropertyValue
import java.util.Locale

/**
 * Default RecyclerView Adapter used by [InputTaxaSummaryFragment].
 *
 * @author S. Grimault
 */
class InputTaxaSummaryRecyclerViewAdapter(listener: OnListItemRecyclerViewAdapterListener<AbstractInputTaxon>) :
    AbstractListItemRecyclerViewAdapter<AbstractInputTaxon>(listener) {

    override fun getViewHolder(
        view: View,
        viewType: Int
    ): AbstractViewHolder {
        return ViewHolder(view)
    }

    override fun getLayoutResourceId(
        position: Int,
        item: AbstractInputTaxon
    ): Int {
        return R.layout.list_item_taxon_summary
    }

    override fun areItemsTheSame(
        oldItems: List<AbstractInputTaxon>,
        newItems: List<AbstractInputTaxon>,
        oldItemPosition: Int,
        newItemPosition: Int
    ): Boolean {
        return oldItems[oldItemPosition].taxon.id == newItems[newItemPosition].taxon.id
    }

    override fun areContentsTheSame(
        oldItems: List<AbstractInputTaxon>,
        newItems: List<AbstractInputTaxon>,
        oldItemPosition: Int,
        newItemPosition: Int
    ): Boolean {
        return oldItems[oldItemPosition] == newItems[newItemPosition]
    }

    inner class ViewHolder(itemView: View) :
        AbstractListItemRecyclerViewAdapter<AbstractInputTaxon>.AbstractViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(android.R.id.title)
        private val text1: TextView = itemView.findViewById(android.R.id.text1)
        private val summary: TextView = itemView.findViewById(android.R.id.summary)
        private val text2: TextView = itemView.findViewById(android.R.id.text2)

        override fun onBind(item: AbstractInputTaxon) {
            title.text = item.taxon.name
            text1.text = item.taxon.commonName
            summary.text = buildInformation(*(item as InputTaxon).properties.values.toTypedArray())
            summary.isSelected = true
            text2.text = buildCounting(item.getCounting().size)
        }

        private fun buildInformation(vararg propertyValue: PropertyValue): Spanned {
            return if (propertyValue.isEmpty()) SpannedString(itemView.context.getString(R.string.summary_taxon_information_empty))
            else HtmlCompat.fromHtml(propertyValue
                .asSequence()
                .filterNot { it.isEmpty() }
                .map {
                    itemView.context.getString(
                        R.string.summary_taxon_information,
                        getNomenclatureTypeLabel(it.code),
                        it.label ?: it.value
                    )
                }
                .joinToString(", "),
                HtmlCompat.FROM_HTML_MODE_LEGACY)
        }

        private fun buildCounting(count: Int = 0): String {
            return if (count == 0) itemView.context.getString(R.string.summary_taxon_counting_zero) else itemView.resources.getQuantityString(
                R.plurals.summary_taxon_counting,
                count,
                count
            )
        }

        private fun getNomenclatureTypeLabel(mnemonic: String): String {
            val resourceId = itemView.resources.getIdentifier(
                "nomenclature_${mnemonic.lowercase(Locale.getDefault())}",
                "string",
                itemView.context.packageName
            )

            return if (resourceId == 0) mnemonic else itemView.context.getString(resourceId)
        }
    }
}
