package fr.geonature.occtax.ui.home

import android.annotation.SuppressLint
import android.text.format.DateFormat
import android.view.View
import android.widget.TextView
import com.google.android.material.chip.Chip
import com.google.android.material.progressindicator.CircularProgressIndicator
import fr.geonature.commons.ui.adapter.AbstractListItemRecyclerViewAdapter
import fr.geonature.commons.util.ThemeUtils.getColor
import fr.geonature.occtax.R
import fr.geonature.occtax.features.record.domain.ObservationRecord

/**
 * Default RecyclerView Adapter used by [HomeActivity].
 * Default RecyclerView Adapter about [ObservationRecord].
 *
 * @author S. Grimault
 *
 * @see ObservationRecordsListFragment
 */
class ObservationRecordRecyclerViewAdapter(listener: OnListItemRecyclerViewAdapterListener<ObservationRecord>) :
    AbstractListItemRecyclerViewAdapter<ObservationRecord>(listener) {

    override fun getViewHolder(
        view: View,
        viewType: Int
    ): AbstractViewHolder {
        return ViewHolder(view)
    }

    override fun getLayoutResourceId(
        position: Int,
        item: ObservationRecord
    ): Int {
        return R.layout.list_item_input
    }

    override fun areItemsTheSame(
        oldItems: List<ObservationRecord>,
        newItems: List<ObservationRecord>,
        oldItemPosition: Int,
        newItemPosition: Int
    ): Boolean {
        return oldItems[oldItemPosition].id == newItems[newItemPosition].id
    }

    override fun areContentsTheSame(
        oldItems: List<ObservationRecord>,
        newItems: List<ObservationRecord>,
        oldItemPosition: Int,
        newItemPosition: Int
    ): Boolean {
        return oldItems[oldItemPosition] == newItems[newItemPosition]
    }

    inner class ViewHolder(itemView: View) :
        AbstractListItemRecyclerViewAdapter<ObservationRecord>.AbstractViewHolder(itemView) {
        private val text1: TextView = itemView.findViewById(android.R.id.text1)
        private val text2: TextView = itemView.findViewById(android.R.id.text2)
        private val chip: Chip = itemView.findViewById(R.id.chip_status)

        override fun onBind(item: ObservationRecord) {
            itemView.isEnabled = item.status != ObservationRecord.Status.SYNC_SUCCESSFUL
            text1.text = itemView.context.getString(
                R.string.home_input_created_at,
                DateFormat.format(
                    itemView.context.getString(R.string.home_input_date),
                    item.dates.start
                )
            )
            text2.text = if (item.taxa.taxa.isNotEmpty())
                itemView.resources.getQuantityString(
                    R.plurals.home_input_taxa_count,
                    item.taxa.taxa.size,
                    item.taxa.taxa.size
                ) else itemView.context.getString(R.string.home_input_taxa_count_empty)
            buildChipStatus(item)
        }

        @SuppressLint("DiscouragedApi")
        private fun buildChipStatus(item: ObservationRecord) {
            with(chip) {
                isDuplicateParentStateEnabled = true
                text = itemView.resources.getIdentifier(
                    "home_input_status_${item.status.name.lowercase()}",
                    "string",
                    itemView.context.packageName
                )
                    .takeIf { it > 0 }
                    ?.let { itemView.context.getString(it) }
                    ?: itemView.context.getString(R.string.home_input_status_draft)
                isCloseIconVisible = false
                isEnabled = false
                setChipBackgroundColorResource(
                    itemView.resources.getIdentifier(
                        "input_status_${item.status.name.lowercase()}",
                        "color",
                        itemView.context.packageName
                    )
                        .takeIf { it > 0 } ?: R.color.input_status_draft
                )
                chipIcon =
                    if (item.status == ObservationRecord.Status.SYNC_IN_PROGRESS) CircularProgressIndicator(context).apply {
                        isIndeterminate = true
                        indicatorSize = 44
                        trackThickness = 4
                        indicatorInset = 4
                        setIndicatorColor(
                            getColor(
                                context,
                                R.attr.colorOnPrimary
                            )
                        )
                    }.indeterminateDrawable?.apply {
                        start()
                        chip.invalidate()
                    } else null

                setTextColor(
                    getColor(
                        context,
                        R.attr.colorOnPrimary
                    )
                )
            }
        }
    }
}
