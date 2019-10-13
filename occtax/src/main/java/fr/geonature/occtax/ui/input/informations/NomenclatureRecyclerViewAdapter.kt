package fr.geonature.occtax.ui.input.informations

import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import fr.geonature.commons.data.Nomenclature
import fr.geonature.commons.data.NomenclatureWithTaxonomy
import fr.geonature.occtax.R

/**
 * Default RecyclerView Adapter used by [ChooseNomenclatureDialogFragment].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class NomenclatureRecyclerViewAdapter(private val listener: OnNomenclatureRecyclerViewAdapterListener) : RecyclerView.Adapter<NomenclatureRecyclerViewAdapter.ViewHolder>() {

    private var cursor: Cursor? = null
    private val onClickListener: View.OnClickListener

    init {
        onClickListener = View.OnClickListener { v ->
            val nomenclature = v.tag as Nomenclature
            listener.onSelectedNomenclature(nomenclature)
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

    fun bind(cursor: Cursor?) {
        this.cursor = cursor
        notifyDataSetChanged()
    }

    inner class ViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_2,
                                                                                                                    parent,
                                                                                                                    false)) {
        private val text1: TextView = itemView.findViewById(android.R.id.text1)

        fun bind(position: Int) {
            val cursor = cursor ?: return

            cursor.moveToPosition(position)

            NomenclatureWithTaxonomy.fromCursor(cursor)?.also {
                text1.text = it.defaultLabel

                with(itemView) {
                    tag = it
                    setOnClickListener(onClickListener)
                }
            }
        }
    }

    /**
     * Callback used by [NomenclatureRecyclerViewAdapter].
     */
    interface OnNomenclatureRecyclerViewAdapterListener {

        /**
         * Called when a [Nomenclature] value has been selected.
         *
         * @param nomenclature the selected [Nomenclature] value
         */
        fun onSelectedNomenclature(nomenclature: Nomenclature)
    }
}