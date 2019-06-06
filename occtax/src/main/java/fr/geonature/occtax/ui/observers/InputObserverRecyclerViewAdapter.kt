package fr.geonature.occtax.ui.observers

import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ListView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.l4digital.fastscroll.FastScroller
import fr.geonature.commons.data.InputObserver

/**
 * Default RecyclerView Adapter used by [InputObserverListFragment].
 *
 * @see InputObserverListFragment
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class InputObserverRecyclerViewAdapter(private val listener: OnInputObserverRecyclerViewAdapterListener) : RecyclerView.Adapter<InputObserverRecyclerViewAdapter.ViewHolder>(),
                                                                                                           FastScroller.SectionIndexer {
    private var cursor: Cursor? = null
    private var choiceMode: Int = ListView.CHOICE_MODE_SINGLE
    private val selectedInputObservers: MutableList<InputObserver> = ArrayList()
    private val onClickListener: View.OnClickListener

    init {
        onClickListener = View.OnClickListener { v ->
            val checkbox: CheckBox = v.findViewById(android.R.id.checkbox)

            val inputObserver = v.tag as InputObserver

            if (isSingleChoice()) {
                selectedInputObservers.clear()
            }

            if (selectedInputObservers.contains(inputObserver)) {
                selectedInputObservers.remove(inputObserver)
                checkbox.isChecked = false
            }
            else {
                selectedInputObservers.add(inputObserver)
                checkbox.isChecked = true
            }

            listener.onSelectedInputObservers(selectedInputObservers)
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
        val inputObserver = InputObserver.fromCursor(cursor) ?: return ""
        val lastname = inputObserver.lastname ?: return ""

        return lastname.elementAt(0)
                .toString()
    }

    fun setChoiceMode(choiceMode: Int = ListView.CHOICE_MODE_SINGLE) {
        this.choiceMode = choiceMode
    }

    fun isSingleChoice(): Boolean {
        return choiceMode == ListView.CHOICE_MODE_SINGLE
    }

    fun setSelectedInputObservers(selectedInputObservers: List<InputObserver>) {
        this.selectedInputObservers.clear()
        this.selectedInputObservers.addAll(selectedInputObservers)
        notifyDataSetChanged()
    }

    fun getSelectedInputObservers(): List<InputObserver> {
        return this.selectedInputObservers
    }

    fun bind(cursor: Cursor?) {
        this.cursor = cursor
        scrollToFirstItemSelected()
        notifyDataSetChanged()
    }

    private fun scrollToFirstItemSelected() {
        val cursor = cursor ?: return

        // try to find the first selected item position
        if (selectedInputObservers.size > 0) {
            cursor.moveToFirst()
            var foundFirstItemSelected = false

            while (!cursor.isAfterLast && !foundFirstItemSelected) {
                val currentInputObserver = InputObserver.fromCursor(cursor)

                if (selectedInputObservers.contains(currentInputObserver)) {
                    foundFirstItemSelected = true
                    listener.scrollToFirstSelectedItemPosition(cursor.position)
                }

                cursor.moveToNext()
            }

            cursor.moveToFirst()
        }
    }

    inner class ViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(
            fr.geonature.occtax.R.layout.list_title_item_2,
            parent,
            false)) {

        private val title: TextView = itemView.findViewById(android.R.id.title)
        private val text1: TextView = itemView.findViewById(android.R.id.text1)
        private val text2: TextView = itemView.findViewById(android.R.id.text2)
        private val checkbox: CheckBox = itemView.findViewById(android.R.id.checkbox)

        fun bind(position: Int) {
            val cursor = cursor ?: return

            cursor.moveToPosition(position)

            val inputObserver = InputObserver.fromCursor(cursor)

            val previousTitle = if (position > 0) {
                cursor.moveToPosition(position - 1)
                InputObserver.fromCursor(cursor)
                        ?.lastname?.elementAt(0)
                        .toString()
            }
            else {
                ""
            }

            if (inputObserver != null) {
                val currentTitle = inputObserver.lastname?.elementAt(0)
                        .toString()
                title.text = if (previousTitle == currentTitle) "" else currentTitle
                text1.text = inputObserver.lastname?.toUpperCase()
                text2.text = inputObserver.firstname
                checkbox.isChecked = selectedInputObservers.contains(inputObserver)

                with(itemView) {
                    tag = inputObserver
                    setOnClickListener(onClickListener)
                }
            }
        }
    }

    /**
     * Callback used by [InputObserverRecyclerViewAdapter].
     */
    interface OnInputObserverRecyclerViewAdapterListener {

        /**
         * Called when [InputObserver]s were been selected.
         *
         * @param inputObservers the selected [InputObserver]s
         */
        fun onSelectedInputObservers(inputObservers: List<InputObserver>)

        /**
         * Called if we want to scroll to the first selected item
         *
         * @param position the current position of the first selected item
         */
        fun scrollToFirstSelectedItemPosition(position: Int)
    }
}