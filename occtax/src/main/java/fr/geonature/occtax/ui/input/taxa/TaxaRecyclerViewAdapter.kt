package fr.geonature.occtax.ui.input.taxa

import android.database.Cursor
import android.graphics.Color
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import com.l4digital.fastscroll.FastScroller
import fr.geonature.commons.data.AbstractTaxon
import fr.geonature.commons.data.Taxon
import fr.geonature.commons.data.TaxonWithArea
import fr.geonature.occtax.R
import java.text.NumberFormat

/**
 * Default RecyclerView Adapter used by [TaxaFragment].
 *
 * @see TaxaFragment
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class TaxaRecyclerViewAdapter(private val listener: OnTaxaRecyclerViewAdapterListener) :
    RecyclerView.Adapter<TaxaRecyclerViewAdapter.ViewHolder>(),
    FastScroller.SectionIndexer {
    private var cursor: Cursor? = null
    private var selectedTaxon: AbstractTaxon? = null
    private val onClickListener: View.OnClickListener

    init {
        this.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()

                listener.showEmptyTextView(itemCount == 0)
            }

            override fun onItemRangeChanged(
                positionStart: Int,
                itemCount: Int
            ) {
                super.onItemRangeChanged(
                    positionStart,
                    itemCount
                )

                listener.showEmptyTextView(itemCount == 0)
            }

            override fun onItemRangeInserted(
                positionStart: Int,
                itemCount: Int
            ) {
                super.onItemRangeInserted(
                    positionStart,
                    itemCount
                )

                listener.showEmptyTextView(false)
            }

            override fun onItemRangeRemoved(
                positionStart: Int,
                itemCount: Int
            ) {
                super.onItemRangeRemoved(
                    positionStart,
                    itemCount
                )

                listener.showEmptyTextView(itemCount == 0)
            }
        })

        onClickListener = View.OnClickListener { v ->
            val previousSelectedItemPosition = getItemPosition(selectedTaxon)

            val checkbox: CheckBox = v.findViewById(android.R.id.checkbox)
            checkbox.isChecked = !checkbox.isChecked

            val taxon = v.tag as AbstractTaxon

            if (checkbox.isChecked) {
                selectedTaxon = taxon
                listener.onSelectedTaxon(taxon)
            } else {
                selectedTaxon = null
                listener.onNoTaxonSelected()
            }

            if (previousSelectedItemPosition >= 0) {
                notifyItemChanged(previousSelectedItemPosition)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(parent)
    }

    override fun getItemCount(): Int {
        return cursor?.count ?: 0
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {

        holder.bind(position)
    }

    override fun getSectionText(position: Int): CharSequence {
        val cursor = cursor ?: return ""
        cursor.moveToPosition(position)
        val taxon = Taxon.fromCursor(cursor) ?: return ""
        val name = taxon.name

        return name.elementAt(0)
            .toString()
    }

    fun setSelectedTaxon(selectedTaxon: Taxon) {
        this.selectedTaxon = selectedTaxon
        scrollToFirstItemSelected()
        notifyDataSetChanged()
    }

    fun bind(cursor: Cursor?) {
        this.cursor = cursor
        scrollToFirstItemSelected()
        notifyDataSetChanged()
    }

    private fun getItemPosition(taxon: AbstractTaxon?): Int {
        var itemPosition = -1
        val cursor = cursor ?: return itemPosition
        if (taxon == null) return itemPosition

        cursor.moveToFirst()

        while (!cursor.isAfterLast && itemPosition < 0) {
            val currentTaxon = Taxon.fromCursor(cursor)

            if (taxon.id == currentTaxon?.id) {
                itemPosition = cursor.position
            }

            cursor.moveToNext()
        }

        cursor.moveToFirst()

        return itemPosition
    }

    private fun scrollToFirstItemSelected() {
        val selectedTaxon = selectedTaxon ?: return
        val selectedItemPosition = getItemPosition(selectedTaxon)

        if (selectedItemPosition >= 0) {
            listener.scrollToFirstSelectedItemPosition(selectedItemPosition)
        }
    }

    inner class ViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(
            R.layout.list_item_taxon,
            parent,
            false
        )
    ) {

        private val title: TextView = itemView.findViewById(android.R.id.title)
        private val text1: TextView = itemView.findViewById(android.R.id.text1)
        private val text2: TextView = itemView.findViewById(android.R.id.text2)
        private val checkbox: CheckBox = itemView.findViewById(android.R.id.checkbox)
        private val taxonColorView: View = itemView.findViewById(R.id.taxon_color_view)
        private val taxonObserversView: TextView = itemView.findViewById(R.id.taxon_observers_view)
        private val taxonLastUpdatedAtView: TextView =
            itemView.findViewById(R.id.taxon_last_updated_at_view)

        fun bind(position: Int) {
            val cursor = cursor ?: return

            cursor.moveToPosition(position)

            val taxon = TaxonWithArea.fromCursor(cursor)

            val previousTitle = if (position > 0) {
                cursor.moveToPosition(position - 1)
                TaxonWithArea.fromCursor(cursor)
                    ?.name?.elementAt(0)
                    .toString()
            } else {
                ""
            }

            if (taxon != null) {
                val currentTitle = taxon.name.elementAt(0)
                    .toString()
                title.text = if (previousTitle == currentTitle) "" else currentTitle
                text1.text = taxon.name
                text2.text = HtmlCompat.fromHtml(
                    taxon.description ?: "",
                    HtmlCompat.FROM_HTML_MODE_COMPACT
                )
                checkbox.isChecked = selectedTaxon?.id == taxon.id

                taxon.taxonArea?.run {
                    taxonColorView.setBackgroundColor(
                        if (color.isNullOrBlank()) Color.TRANSPARENT else Color.parseColor(
                            color
                        )
                    )

                    taxonObserversView.text = NumberFormat.getNumberInstance()
                        .format(numberOfObservers)

                    if (lastUpdatedAt != null) {
                        taxonLastUpdatedAtView.text = DateFormat.getDateFormat(itemView.context)
                            .format(lastUpdatedAt)
                    }
                }

                with(itemView) {
                    tag = taxon
                    setOnClickListener(onClickListener)
                }
            }
        }
    }

    /**
     * Callback used by [TaxaRecyclerViewAdapter].
     */
    interface OnTaxaRecyclerViewAdapterListener {

        /**
         * Called when a [AbstractTaxon] has been selected.
         *
         * @param taxon the selected [AbstractTaxon]
         */
        fun onSelectedTaxon(taxon: AbstractTaxon)

        /**
         * Called when no [Taxon] has been selected.
         */
        fun onNoTaxonSelected()

        /**
         * Called if we want to scroll to the first selected item
         *
         * @param position the current position of the first selected item
         */
        fun scrollToFirstSelectedItemPosition(position: Int)

        /**
         * Whether to show an empty text view when data changed.
         */
        fun showEmptyTextView(show: Boolean)
    }
}
