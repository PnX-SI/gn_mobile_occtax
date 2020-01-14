package fr.geonature.occtax.ui.input.taxa

import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import fr.geonature.commons.data.Taxonomy
import fr.geonature.commons.ui.adapter.AbstractStickyRecyclerViewAdapter
import fr.geonature.occtax.R

/**
 * Default RecyclerView Adapter used by [TaxonomyFilterFragment].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class TaxonomyRecyclerViewAdapter(private val listener: OnTaxonomyRecyclerViewAdapterListener) :
    AbstractStickyRecyclerViewAdapter<TaxonomyRecyclerViewAdapter.HeaderViewHolder, TaxonomyRecyclerViewAdapter.AbstractViewHolder>() {

    private var cursor: Cursor? = null
    private var selectedTaxonomy: Taxonomy? = null
    private val onClickListener: View.OnClickListener = View.OnClickListener { v ->
        val previousSelectedItemPosition = getItemPosition(selectedTaxonomy)

        val checkbox: CheckBox = v.findViewById(android.R.id.checkbox)
        checkbox.isChecked = !checkbox.isChecked

        val taxonomy = v.tag as Taxonomy

        if (checkbox.isChecked) {
            selectedTaxonomy = taxonomy
            listener.onSelectedTaxonomy(taxonomy)
        } else {
            selectedTaxonomy = null
            listener.onNoTaxonomySelected()
        }

        if (previousSelectedItemPosition >= 0) {
            notifyItemChanged(previousSelectedItemPosition)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AbstractViewHolder {
        return when (viewType) {
            ViewType.HEADER.ordinal -> HeaderViewHolder(parent)
            else -> ViewHolder(parent)
        }
    }

    override fun getItemCount(): Int {
        return cursor?.count ?: 0
    }

    override fun getItemViewType(position: Int): Int {
        val cursor = cursor ?: return ViewType.VIEW.ordinal
        cursor.moveToPosition(position)
        val currentTaxonomyAtPosition = Taxonomy.fromCursor(cursor)

        return (if (currentTaxonomyAtPosition?.group == Taxonomy.ANY) ViewType.HEADER else ViewType.VIEW).ordinal
    }

    override fun onBindViewHolder(holder: AbstractViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getHeaderPositionForItem(itemPosition: Int): Int {
        var headerPosition = -1
        val cursor = cursor ?: return itemPosition

        cursor.moveToPosition(itemPosition)
        val currentTaxonomyAtPosition = Taxonomy.fromCursor(cursor)

        cursor.moveToFirst()

        while (!cursor.isAfterLast && headerPosition < 0) {
            val currentTaxonomy = Taxonomy.fromCursor(cursor)

            if (currentTaxonomy?.group == Taxonomy.ANY && currentTaxonomy.kingdom == currentTaxonomyAtPosition?.kingdom) {
                headerPosition = cursor.position
            }

            cursor.moveToNext()
        }

        cursor.moveToFirst()

        return if (headerPosition == -1) itemPosition else headerPosition
    }

    override fun onBindHeaderViewHolder(holder: HeaderViewHolder, headerPosition: Int) {
        holder.bind(headerPosition)
    }

    override fun onCreateHeaderViewHolder(parent: ViewGroup): HeaderViewHolder {
        return HeaderViewHolder(parent)
    }

    fun bind(cursor: Cursor?) {
        this.cursor = cursor
        scrollToFirstItemSelected()
        notifyDataSetChanged()
    }

    fun setSelectedTaxonomy(selectedTaxonomy: Taxonomy?) {
        this.selectedTaxonomy = selectedTaxonomy
        notifyDataSetChanged()
    }

    private fun getItemPosition(taxonomy: Taxonomy?): Int {
        var itemPosition = -1
        val cursor = cursor ?: return itemPosition
        if (taxonomy == null) return itemPosition

        cursor.moveToFirst()

        while (!cursor.isAfterLast && itemPosition < 0) {
            val currentTaxonomy = Taxonomy.fromCursor(cursor)

            if (taxonomy == currentTaxonomy) {
                itemPosition = cursor.position
            }

            cursor.moveToNext()
        }

        cursor.moveToFirst()

        return itemPosition
    }

    private fun scrollToFirstItemSelected() {
        val cursor = cursor ?: return
        val selectedTaxonomy = selectedTaxonomy ?: return

        // try to find the first selected item position
        cursor.moveToFirst()
        var foundFirstItemSelected = false

        while (!cursor.isAfterLast && !foundFirstItemSelected) {
            val currentTaxonomy = Taxonomy.fromCursor(cursor)

            if (selectedTaxonomy == currentTaxonomy) {
                foundFirstItemSelected = true
                listener.scrollToFirstSelectedItemPosition(cursor.position)
            }

            cursor.moveToNext()
        }

        cursor.moveToFirst()
    }

    abstract inner class AbstractViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private var taxonomy: Taxonomy? = null

        fun bind(position: Int) {
            val cursor = cursor ?: return

            cursor.moveToPosition(position)
            val taxonomy = Taxonomy.fromCursor(cursor) ?: return

            this.taxonomy = taxonomy

            with(itemView) {
                tag = taxonomy
                setOnClickListener(onClickListener)
            }

            onBind(taxonomy)
        }

        abstract fun onBind(taxonomy: Taxonomy)
    }

    inner class HeaderViewHolder(parent: ViewGroup) : AbstractViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(
                R.layout.list_selectable_header_item_1,
                parent,
                false
            )
    ) {
        private val title: TextView = itemView.findViewById(android.R.id.title)
        private val checkbox: CheckBox = itemView.findViewById(android.R.id.checkbox)

        override fun onBind(taxonomy: Taxonomy) {
            title.text = taxonomy.kingdom
            checkbox.isChecked = selectedTaxonomy == taxonomy
        }
    }

    inner class ViewHolder(parent: ViewGroup) : AbstractViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(
                R.layout.list_selectable_item_1,
                parent,
                false
            )
    ) {
        private val title: TextView = itemView.findViewById(android.R.id.title)
        private val checkbox: CheckBox = itemView.findViewById(android.R.id.checkbox)

        override fun onBind(taxonomy: Taxonomy) {
            title.text = taxonomy.group
            checkbox.isChecked = selectedTaxonomy == taxonomy
        }
    }

    enum class ViewType {
        HEADER,
        VIEW
    }

    /**
     * Callback used by [TaxonomyRecyclerViewAdapter].
     */
    interface OnTaxonomyRecyclerViewAdapterListener {

        /**
         * Called when a [Taxonomy] has been selected.
         *
         * @param taxonomy the selected [Taxonomy]
         */
        fun onSelectedTaxonomy(taxonomy: Taxonomy)

        /**
         * Called when no [Taxonomy] has been selected.
         */
        fun onNoTaxonomySelected()

        /**
         * Called if we want to scroll to the first selected item
         *
         * @param position the current position of the first selected item
         */
        fun scrollToFirstSelectedItemPosition(position: Int)
    }
}
