package fr.geonature.occtax.ui.home

import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import fr.geonature.commons.ui.adapter.AbstractListItemRecyclerViewAdapter
import fr.geonature.commons.util.ThemeUtils
import fr.geonature.occtax.R
import fr.geonature.occtax.features.input.domain.Input

/**
 * Default RecyclerView Adapter used by [HomeActivity].
 *
 * @author S. Grimault
 */
class InputRecyclerViewAdapter(listener: OnListItemRecyclerViewAdapterListener<Input>) :
    AbstractListItemRecyclerViewAdapter<Input>(listener) {
    override fun getViewHolder(
        view: View,
        viewType: Int
    ): AbstractViewHolder {
        return ViewHolder(view)
    }

    override fun getLayoutResourceId(
        position: Int,
        item: Input
    ): Int {
        return R.layout.list_item_input
    }

    override fun areItemsTheSame(
        oldItems: List<Input>,
        newItems: List<Input>,
        oldItemPosition: Int,
        newItemPosition: Int
    ): Boolean {
        return oldItems[oldItemPosition].id == newItems[newItemPosition].id
    }

    override fun areContentsTheSame(
        oldItems: List<Input>,
        newItems: List<Input>,
        oldItemPosition: Int,
        newItemPosition: Int
    ): Boolean {
        return oldItems[oldItemPosition] == newItems[newItemPosition]
    }

    inner class ViewHolder(itemView: View) :
        AbstractListItemRecyclerViewAdapter<Input>.AbstractViewHolder(itemView) {
        private val text1: TextView = itemView.findViewById(android.R.id.text1)
        private val text2: TextView = itemView.findViewById(android.R.id.text2)
        private val chipGroup: ChipGroup = itemView.findViewById(R.id.chip_group_status)

        override fun onBind(item: Input) {
            text1.text = itemView.context.getString(
                R.string.home_input_created_at,
                DateFormat.format(
                    itemView.context.getString(R.string.home_input_date),
                    item.startDate
                )
            )
            text2.text = if (item.getInputTaxa().isNotEmpty())
                itemView.resources.getQuantityString(
                    R.plurals.home_input_taxa_count,
                    item.getInputTaxa().size,
                    item.getInputTaxa().size
                ) else itemView.context.getString(R.string.home_input_taxa_count_empty)
            buildChipStatus(item)
        }

        private fun buildChipStatus(item: Input) {
            chipGroup.removeAllViews()

            with(
                LayoutInflater.from(itemView.context).inflate(
                    R.layout.chip,
                    chipGroup,
                    false
                ) as Chip
            ) {
                text = itemView.resources.getIdentifier(
                    "home_input_status_${item.status.name.lowercase()}",
                    "string",
                    itemView.context.packageName
                ).takeIf { it > 0 }?.let { itemView.context.getString(it) }
                    ?: itemView.context.getString(R.string.home_input_status_draft)
                isCloseIconVisible = false
                isEnabled = false
                setChipBackgroundColorResource(
                    itemView.resources.getIdentifier(
                        "input_status_${item.status.name.lowercase()}",
                        "color",
                        itemView.context.packageName
                    ).takeIf { it > 0 } ?: R.color.input_status_draft
                )
                setTextColor(
                    ThemeUtils.getColor(
                        context,
                        R.attr.colorOnPrimary
                    )
                )
                chipGroup.addView(this)
            }
        }
    }
}
