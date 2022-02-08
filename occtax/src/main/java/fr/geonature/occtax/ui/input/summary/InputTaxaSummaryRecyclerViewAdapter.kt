package fr.geonature.occtax.ui.input.summary

import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.core.text.HtmlCompat
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import fr.geonature.commons.data.entity.Taxonomy
import fr.geonature.commons.input.AbstractInputTaxon
import fr.geonature.commons.ui.adapter.AbstractListItemRecyclerViewAdapter
import fr.geonature.occtax.R
import fr.geonature.occtax.input.InputTaxon
import fr.geonature.occtax.input.PropertyValue
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
        private val filterChipGroup: ChipGroup = itemView.findViewById(R.id.chip_group_filter)
        private val text1: TextView = itemView.findViewById(android.R.id.text1)
        private val text2: TextView = itemView.findViewById(android.R.id.text2)

        override fun onBind(item: AbstractInputTaxon) {
            title.text = item.taxon.name
            buildTaxonomyChips(item.taxon.taxonomy)
            text1.text = buildInformation(*(item as InputTaxon).properties.values.toTypedArray())
            text1.isSelected = true
            text2.text = buildCounting(item.getCounting().size)
        }

        private fun buildTaxonomyChips(taxonomy: Taxonomy) {
            filterChipGroup.removeAllViews()

            // build kingdom taxonomy filter chip
            with(
                LayoutInflater.from(itemView.context).inflate(
                    R.layout.chip,
                    filterChipGroup,
                    false
                ) as Chip
            ) {
                text = taxonomy.kingdom
                filterChipGroup.addView(this)
                isCloseIconVisible = false
                isEnabled = false
            }

            // build group taxonomy filter chip
            if (taxonomy.group != Taxonomy.ANY) {
                with(
                    LayoutInflater.from(itemView.context).inflate(
                        R.layout.chip,
                        filterChipGroup,
                        false
                    ) as Chip
                ) {
                    text = taxonomy.group
                    filterChipGroup.addView(this)
                    isCloseIconVisible = false
                    isEnabled = false
                }
            }
        }

        private fun buildInformation(vararg propertyValue: PropertyValue): Spanned {
            return HtmlCompat.fromHtml(propertyValue
                .asSequence()
                .filterNot { it.isEmpty() }
                .map {
                    itemView.context.getString(
                        R.string.summary_taxon_information,
                        getNomenclatureTypeLabel(it.code),
                        it.label
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
