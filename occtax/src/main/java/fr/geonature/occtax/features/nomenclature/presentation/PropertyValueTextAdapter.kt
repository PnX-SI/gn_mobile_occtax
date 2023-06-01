package fr.geonature.occtax.features.nomenclature.presentation

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import fr.geonature.occtax.features.record.domain.PropertyValue

/**
 * Default Adapter about [PropertyValue.Text].
 *
 * @author S. Grimault
 */
class PropertyValueTextAdapter(context: Context) : BaseAdapter(), Filterable {

    private val fieldValues = mutableListOf<PropertyValue.Text>()
    private val filteredFieldValues = mutableListOf<PropertyValue.Text>()
    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)
    private val defaultFilter = DefaultFilter()

    override fun getCount(): Int {
        return filteredFieldValues.size
    }

    override fun getItem(position: Int): Any {
        return filteredFieldValues[position].value ?: filteredFieldValues[position].code
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView
            ?: layoutInflater.inflate(
                android.R.layout.simple_list_item_1,
                parent,
                false
            )
        view.findViewById<TextView>(android.R.id.text1).text =
            filteredFieldValues[position].value ?: filteredFieldValues[position].code

        return view
    }

    override fun getFilter(): Filter {
        return defaultFilter
    }

    fun getPropertyValue(position: Int): PropertyValue {
        return filteredFieldValues[position]
    }

    fun setPropertyValues(propertyValues: List<PropertyValue>) {
        with(this.fieldValues) {
            clear()
            addAll(propertyValues.filterIsInstance<PropertyValue.Text>())
        }
        with(this.filteredFieldValues) {
            clear()
            addAll(propertyValues.filterIsInstance<PropertyValue.Text>())
        }

        notifyDataSetChanged()
    }

    inner class DefaultFilter : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            return fieldValues
                .let {
                    FilterResults().apply {
                        values = it
                        count = it.size
                    }
                }
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            with(filteredFieldValues) {
                clear()
                @Suppress("UNCHECKED_CAST")
                addAll((results?.values as List<PropertyValue.Text>?) ?: emptyList())
            }

            if (results?.count == 0) notifyDataSetInvalidated() else notifyDataSetChanged()
        }
    }
}