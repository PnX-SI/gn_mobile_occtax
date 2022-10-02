package fr.geonature.occtax.features.nomenclature.presentation

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import fr.geonature.commons.data.entity.Nomenclature

/**
 * Default Adapter about [Nomenclature] values.
 *
 * @author S. Grimault
 */
class NomenclatureValueAdapter(context: Context) : BaseAdapter(), Filterable {
    private val nomenclatureValues = mutableListOf<Nomenclature>()
    private val filteredNomenclatureValues = mutableListOf<Nomenclature>()
    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)
    private val defaultFilter = DefaultFilter()

    override fun getCount(): Int {
        return filteredNomenclatureValues.size
    }

    override fun getItem(position: Int): String {
        return filteredNomenclatureValues[position].defaultLabel
    }

    override fun getItemId(position: Int): Long {
        return filteredNomenclatureValues[position].id
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView
            ?: layoutInflater.inflate(
                android.R.layout.simple_list_item_1,
                parent,
                false
            )
        view.findViewById<TextView>(android.R.id.text1).text =
            filteredNomenclatureValues[position].defaultLabel

        return view
    }

    override fun getFilter(): Filter {
        return defaultFilter
    }

    fun getNomenclatureValue(position: Int): Nomenclature {
        return filteredNomenclatureValues[position]
    }

    fun setNomenclatureValues(nomenclatureValues: List<Nomenclature>) {
        with(this.nomenclatureValues) {
            clear()
            addAll(nomenclatureValues)
        }
        with(this.filteredNomenclatureValues) {
            clear()
            addAll(nomenclatureValues)
        }

        notifyDataSetChanged()
    }

    inner class DefaultFilter : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            return filteredNomenclatureValues.filter { if (constraint == null) false else it.defaultLabel.contains(constraint) }
                .let {
                    FilterResults().apply {
                        values = it
                        count = it.size
                    }
                }
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            with(filteredNomenclatureValues) {
                clear()
                @Suppress("UNCHECKED_CAST")
                addAll((results?.values as List<Nomenclature>?) ?: emptyList())
            }

            if (results?.count == 0) notifyDataSetInvalidated() else notifyDataSetChanged()
        }
    }
}