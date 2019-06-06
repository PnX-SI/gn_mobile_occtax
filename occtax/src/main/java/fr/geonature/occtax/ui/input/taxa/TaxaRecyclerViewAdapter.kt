package fr.geonature.occtax.ui.input.taxa

import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.l4digital.fastscroll.FastScroller
import fr.geonature.commons.data.Taxon
import fr.geonature.occtax.R

/**
 * Default RecyclerView Adapter used by [TaxaFragment].
 *
 * @see TaxaFragment
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class TaxaRecyclerViewAdapter(private val listener: OnTaxaRecyclerViewAdapterListener) : RecyclerView.Adapter<TaxaRecyclerViewAdapter.ViewHolder>(),
                                                                                         FastScroller.SectionIndexer {
    private var cursor: Cursor? = null
    private var selectedTaxon: Taxon? = null
    private val onClickListener: View.OnClickListener

    init {
        onClickListener = View.OnClickListener { v ->
            val previousSelectedItemPosition = getItemPosition(selectedTaxon)

            val checkbox: CheckBox = v.findViewById(android.R.id.checkbox)
            checkbox.isChecked = !checkbox.isChecked

            val taxon = v.tag as Taxon

            if (checkbox.isChecked) {
                selectedTaxon = taxon
                listener.onSelectedTaxon(taxon)
            }
            else {
                selectedTaxon = null
                listener.onNoTaxonSelected()
            }

            if (previousSelectedItemPosition >= 0) {
                notifyItemChanged(previousSelectedItemPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): ViewHolder {
        return ViewHolder(parent)
    }

    override fun getItemCount(): Int {
        return cursor?.count ?: 0
    }

    override fun onBindViewHolder(holder: ViewHolder,
                                  position: Int) {

        holder.bind(position)
    }

    override fun getSectionText(position: Int): CharSequence {
        val cursor = cursor ?: return ""
        cursor.moveToPosition(position)
        val taxon = Taxon.fromCursor(cursor) ?: return ""
        val name = taxon.name ?: return ""

        return name.elementAt(0)
                .toString()
    }

    fun setSelectedTaxon(selectedTaxon: Taxon) {
        this.selectedTaxon = selectedTaxon
        notifyDataSetChanged()
    }

    fun getSelectedTaxon(): Taxon? {
        return this.selectedTaxon
    }

    fun bind(cursor: Cursor?) {
        this.cursor = cursor
        scrollToFirstItemSelected()
        notifyDataSetChanged()
    }

    private fun getItemPosition(taxon: Taxon?): Int {
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

    inner class ViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_title_taxon,
                                                                                                                    parent,
                                                                                                                    false)) {

        private val title: TextView = itemView.findViewById(android.R.id.title)
        private val text1: TextView = itemView.findViewById(android.R.id.text1)
        private val text2: TextView = itemView.findViewById(android.R.id.text2)
        private val checkbox: CheckBox = itemView.findViewById(android.R.id.checkbox)

        fun bind(position: Int) {
            val cursor = cursor ?: return

            cursor.moveToPosition(position)

            val taxon = Taxon.fromCursor(cursor)

            val previousTitle = if (position > 0) {
                cursor.moveToPosition(position - 1)
                Taxon.fromCursor(cursor)
                        ?.name?.elementAt(0)
                        .toString()
            }
            else {
                ""
            }

            if (taxon != null) {
                val currentTitle = taxon.name?.elementAt(0)
                        .toString()
                title.text = if (previousTitle == currentTitle) "" else currentTitle
                text1.text = taxon.name
                text2.text = taxon.description
                checkbox.isChecked = selectedTaxon?.id == taxon.id

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
         * Called when a [Taxon] has been selected.
         *
         * @param taxon the selected [Taxon]
         */
        fun onSelectedTaxon(taxon: Taxon)

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
    }
}