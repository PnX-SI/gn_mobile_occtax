package fr.geonature.occtax.features.nomenclature.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import fr.geonature.commons.ui.adapter.AbstractListItemRecyclerViewAdapter
import fr.geonature.occtax.R
import fr.geonature.occtax.features.nomenclature.domain.EditableField
import fr.geonature.occtax.features.record.domain.PropertyValue

/**
 * [EditableFieldAdapter] view holder representing a list of checkboxes.
 *
 * @author S. Grimault
 */
class CheckboxViewHolder(
    parent: ViewGroup,
    private val listener: EditableFieldAdapter.OnEditableFieldAdapter
) :
    EditableFieldAdapter.AbstractViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(
                R.layout.view_action_list,
                parent,
                false
            )
    ) {
    private var title: TextView = itemView.findViewById(android.R.id.title)
    private var recyclerView: RecyclerView = itemView.findViewById(android.R.id.list)
    private var adapter =
        object : AbstractListItemRecyclerViewAdapter<Pair<PropertyValue.Text, Boolean>>() {
            override fun getViewHolder(view: View, viewType: Int): AbstractViewHolder {
                return ViewHolder(view)
            }

            override fun getLayoutResourceId(
                position: Int,
                item: Pair<PropertyValue.Text, Boolean>
            ): Int {
                return R.layout.list_item_checkbox
            }

            override fun areItemsTheSame(
                oldItems: List<Pair<PropertyValue.Text, Boolean>>,
                newItems: List<Pair<PropertyValue.Text, Boolean>>,
                oldItemPosition: Int,
                newItemPosition: Int
            ): Boolean {
                return oldItems[oldItemPosition] == newItems[newItemPosition]
            }

            override fun areContentsTheSame(
                oldItems: List<Pair<PropertyValue.Text, Boolean>>,
                newItems: List<Pair<PropertyValue.Text, Boolean>>,
                oldItemPosition: Int,
                newItemPosition: Int
            ): Boolean {
                return oldItems[oldItemPosition] == newItems[newItemPosition]
            }

            inner class ViewHolder(itemView: View) :
                AbstractViewHolder(itemView) {

                private val checkbox: CheckBox =
                    itemView.findViewById<CheckBox?>(android.R.id.checkbox)
                        .apply {
                            setOnClickListener { view ->
                                editableField?.run {
                                    value = value?.takeIf { it is PropertyValue.StringArray }
                                        ?.let { it as PropertyValue.StringArray }
                                        ?.let {
                                            it.copy(
                                                value = it.value.filter { value -> value != view.tag.toString() }
                                                    .toTypedArray() + (
                                                    if (isChecked) arrayOf(view.tag.toString())
                                                    else arrayOf()),
                                            )
                                        } ?: PropertyValue.StringArray(
                                        code,
                                        if (isChecked) arrayOf(view.tag.toString())
                                        else arrayOf()
                                    )
                                    listener.onUpdate(this)
                                }
                            }
                        }

                override fun onBind(item: Pair<PropertyValue.Text, Boolean>) {
                    with(checkbox) {
                        tag = item.first.code
                        text = item.first.value ?: item.first.code
                        isChecked = item.second
                    }
                }
            }
        }

    init {
        with(recyclerView) {
            layoutManager = GridLayoutManager(
                context,
                2
            )
            adapter = this@CheckboxViewHolder.adapter
        }
    }

    override fun onBind(editableField: EditableField) {
        title.text = editableField.label

        val currentValues = editableField.value
            .takeIf { it is PropertyValue.StringArray }
            ?.let { it as PropertyValue.StringArray }?.value

        adapter.setItems(editableField.values.mapNotNull { pv ->
            pv.takeIf { it is PropertyValue.Text }
                ?.let { it as PropertyValue.Text }
                ?.let {
                    Pair(
                        it,
                        currentValues?.contains(it.code) == true
                    )
                }
        })
    }
}