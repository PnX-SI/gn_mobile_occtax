package fr.geonature.occtax.ui.dataset

import android.database.Cursor
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.l4digital.fastscroll.FastScroller
import fr.geonature.commons.data.Dataset
import fr.geonature.occtax.R

/**
 * Default RecyclerView Adapter used by [DatasetListFragment].
 *
 * @see DatasetListFragment
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class DatasetRecyclerViewAdapter(private val listener: OnDatasetRecyclerViewAdapterListener) : RecyclerView.Adapter<DatasetRecyclerViewAdapter.ViewHolder>(),
                                                                                               FastScroller.SectionIndexer {
    private var cursor: Cursor? = null
    private var selectedDataset: Dataset? = null
    private val onClickListener: View.OnClickListener

    init {
        onClickListener = View.OnClickListener { v ->
            val previousSelectedItemPosition = getItemPosition(selectedDataset)

            val checkbox: CheckBox = v.findViewById(android.R.id.checkbox)
            checkbox.isChecked = !checkbox.isChecked

            val dataset = v.tag as Dataset

            if (checkbox.isChecked) {
                selectedDataset = dataset
                listener.onSelectedDataset(dataset)
            }

            if (previousSelectedItemPosition >= 0) {
                notifyItemChanged(previousSelectedItemPosition)
            }
        }
    }

    override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int): ViewHolder {
        return ViewHolder(parent)
    }

    override fun getItemCount(): Int {
        return cursor?.count ?: 0
    }

    override fun onBindViewHolder(
            holder: ViewHolder,
            position: Int) {

        holder.bind(position)
    }

    override fun getSectionText(position: Int): CharSequence {
        val cursor = cursor ?: return ""
        cursor.moveToPosition(position)
        val dataset = Dataset.fromCursor(cursor) ?: return ""

        return dataset.name.elementAt(0)
            .toString()
    }

    fun setSelectedDataset(selectedDataset: Dataset?) {
        this.selectedDataset = selectedDataset

        if (selectedDataset != null) {
            scrollToFirstItemSelected()
        }

        notifyDataSetChanged()
    }

    fun getSelectedDataset(): Dataset? {
        return this.selectedDataset
    }

    fun bind(cursor: Cursor?) {
        this.cursor = cursor
        scrollToFirstItemSelected()
        notifyDataSetChanged()
    }

    private fun getItemPosition(dataset: Dataset?): Int {
        var itemPosition = -1
        val cursor = cursor ?: return itemPosition
        if (dataset == null) return itemPosition

        cursor.moveToFirst()

        while (!cursor.isAfterLast && itemPosition < 0) {
            val currentDataset = Dataset.fromCursor(cursor)

            if (dataset.id == currentDataset?.id) {
                itemPosition = cursor.position
            }

            cursor.moveToNext()
        }

        cursor.moveToFirst()

        return itemPosition
    }

    private fun scrollToFirstItemSelected() {
        val selectedDataset = selectedDataset ?: return
        val selectedItemPosition = getItemPosition(selectedDataset)

        if (selectedItemPosition >= 0) {
            listener.scrollToFirstSelectedItemPosition(selectedItemPosition)
        }
    }

    inner class ViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(
            R.layout.list_selectable_item_3,
            parent,
            false)) {

        private val title: TextView = itemView.findViewById(android.R.id.title)
        private val text1: TextView = itemView.findViewById(android.R.id.text1)
        private val text2: TextView = itemView.findViewById(android.R.id.text2)
        private val checkbox: CheckBox = itemView.findViewById(android.R.id.checkbox)

        fun bind(position: Int) {
            val cursor = cursor ?: return

            cursor.moveToPosition(position)

            val dataset = Dataset.fromCursor(cursor)

            if (dataset != null) {
                title.text = dataset.name
                text1.text = dataset.description
                text2.text = itemView.context.getString(R.string.dataset_created_at,
                                                        DateFormat.format(itemView.context.getString(R.string.dataset_date),
                                                                          dataset.createdAt))
                checkbox.isChecked = selectedDataset?.id == dataset.id

                with(itemView) {
                    tag = dataset
                    setOnClickListener(onClickListener)
                }
            }
        }
    }

    /**
     * Callback used by [DatasetRecyclerViewAdapter].
     */
    interface OnDatasetRecyclerViewAdapterListener {

        /**
         * Called when [Dataset] has been selected.
         *
         * @param dataset the selected [Dataset]
         */
        fun onSelectedDataset(dataset: Dataset)

        /**
         * Called if we want to scroll to the first selected item
         *
         * @param position the current position of the first selected item
         */
        fun scrollToFirstSelectedItemPosition(position: Int)
    }
}