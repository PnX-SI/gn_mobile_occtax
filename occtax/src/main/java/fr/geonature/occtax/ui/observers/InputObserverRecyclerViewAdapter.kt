package fr.geonature.occtax.ui.observers

import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
class InputObserverRecyclerViewAdapter(private val listener: InputObserverListFragment.OnInputObserverListFragmentListener?) : RecyclerView.Adapter<InputObserverRecyclerViewAdapter.ViewHolder>(),
                                                                                                                                FastScroller.SectionIndexer {
    private var cursor: Cursor? = null

    private val onClickListener: View.OnClickListener

    init {
        onClickListener = View.OnClickListener { v ->
            val inputObserver = v.tag as InputObserver
            listener?.onSelectedObserver(inputObserver)
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

    fun bind(cursor: Cursor?) {
        this.cursor = cursor
        notifyDataSetChanged()
    }

    inner class ViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(
        fr.geonature.occtax.R.layout.list_item,
        parent,
        false)) {

        private val title: TextView = itemView.findViewById(android.R.id.title)
        private val text1: TextView = itemView.findViewById(android.R.id.text1)
        private val text2: TextView = itemView.findViewById(android.R.id.text2)

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
                text1.text = inputObserver.lastname
                text2.text = inputObserver.firstname

                with(itemView) {
                    tag = inputObserver
                    setOnClickListener(onClickListener)
                }
            }
        }
    }
}