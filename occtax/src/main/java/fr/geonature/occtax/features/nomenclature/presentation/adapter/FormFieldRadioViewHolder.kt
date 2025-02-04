package fr.geonature.occtax.features.nomenclature.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import fr.geonature.commons.ui.adapter.AbstractListItemRecyclerViewAdapter
import fr.geonature.occtax.R
import fr.geonature.occtax.features.nomenclature.domain.FormField
import fr.geonature.occtax.features.record.domain.PropertyValue

/**
 * [FormFieldAdapter] view holder representing a list of radio buttons.
 *
 * @author S. Grimault
 */
class FormFieldRadioViewHolder(
    parent: ViewGroup,
    private val listener: FormFieldAdapter.OnEditableFieldAdapter
) : FormFieldAdapter.AbstractFormFieldViewHolder<FormField.Radio>(
    LayoutInflater.from(parent.context)
        .inflate(
            R.layout.view_action_list,
            parent,
            false
        )
) {
    private var title: TextView = itemView.findViewById(android.R.id.title)
    private var errorHint: TextView = itemView.findViewById(android.R.id.hint)
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
                return R.layout.list_item_radio
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

                private val radioButton: RadioButton =
                    itemView.findViewById<RadioButton?>(android.R.id.checkbox)
                        .apply {
                            setOnClickListener { view ->
                                formField?.run {
                                    setValue(
                                        PropertyValue.Text(
                                            code = getValue().code,
                                            value = (view as CompoundButton).takeIf { it.isChecked }?.tag.toString()
                                        )
                                    )
                                    this@FormFieldRadioViewHolder.setError(this)
                                    listener.onUpdate(this)
                                }

                                if (isChecked) {
                                    setItems(items.map {
                                        Pair(
                                            it.first,
                                            it.first.code == view.tag.toString()
                                        )
                                    })
                                }
                            }
                        }

                override fun onBind(item: Pair<PropertyValue.Text, Boolean>) {
                    with(radioButton) {
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
            adapter = this@FormFieldRadioViewHolder.adapter
        }
    }

    override fun onBind(formField: FormField.Radio) {
        title.text = formField.label

        setError(formField)

        adapter.setItems(formField.values.map { pv ->
            pv.let {
                Pair(
                    it,
                    formField.value.let { value -> value.value == it.code }
                )
            }
        })
    }

    private fun setError(formField: FormField.Radio) {
        with(errorHint) {
            text = if (formField.mandatory && formField.getValue()
                    .isEmpty()
            ) context.getString(R.string.form_field_error_mandatory) else null
        }
    }
}