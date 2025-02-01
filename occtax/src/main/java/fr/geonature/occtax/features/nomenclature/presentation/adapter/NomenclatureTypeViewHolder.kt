package fr.geonature.occtax.features.nomenclature.presentation.adapter

import android.text.Editable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.textfield.TextInputLayout
import fr.geonature.commons.lifecycle.observeOnce
import fr.geonature.occtax.R
import fr.geonature.occtax.features.nomenclature.domain.FormField
import fr.geonature.occtax.features.record.domain.PropertyValue

/**
 * [EditableFieldAdapter] view holder representing a nomenclature values as simple dropdown list.
 *
 * @author S. Grimault
 */
class NomenclatureTypeViewHolder(
    parent: ViewGroup,
    private val listener: EditableFieldAdapter.OnEditableFieldAdapter
) : EditableFieldAdapter.AbstractLockableViewHolder<FormField.NomenclatureType>(
    LayoutInflater.from(parent.context)
        .inflate(
            R.layout.view_action_select_simple,
            parent,
            false
        )
) {
    private var edit: TextInputLayout = itemView.findViewById(android.R.id.edit)
    private val nomenclatureAdapter = NomenclatureValueAdapter(parent.context)

    init {
        (edit.editText as? AutoCompleteTextView)?.also {
            it.setAdapter(nomenclatureAdapter)
            it.setOnItemClickListener { _, _, position, _ ->
                formField
                    ?.run {
                        edit.error = null
                        with(nomenclatureAdapter.getNomenclatureValue(position)) {
                            setValue(
                                PropertyValue.Nomenclature(
                                    code = getValue().code,
                                    label = defaultLabel,
                                    value = id
                                )
                            )
                        }

                        listener.onUpdate(this)
                    }
            }
        }
    }

    override fun onBind(formField: FormField.NomenclatureType, lockDefaultValues: Boolean) {
        if (!lockDefaultValues) {
            formField.locked = false
        }

        if (formField.mandatory && formField.getValue()
                .isEmpty()
        ) {
            edit.error = itemView.context.getString(R.string.form_field_error_mandatory)
        }

        listener.getNomenclatureValues(formField.nomenclatureType)
            .observeOnce(listener.getLifecycleOwner()) { nomenclatureValues ->
                nomenclatureAdapter.setNomenclatureValues(nomenclatureValues ?: listOf())
                (edit.editText as AutoCompleteTextView?)?.text = formField.value
                    .let { pv -> nomenclatureValues?.firstOrNull { it.id == pv.value } }
                    ?.let { nomenclatureValue ->
                        Editable.Factory.getInstance()
                            .newEditable(nomenclatureValue.defaultLabel)
                    }
            }

        with(edit) {
            startIconDrawable = if (lockDefaultValues) ResourcesCompat.getDrawable(
                itemView.resources,
                if (formField.locked) R.drawable.ic_lock else R.drawable.ic_lock_open,
                itemView.context.theme
            ) else null
            setStartIconOnClickListener {
                if (!lockDefaultValues) return@setStartIconOnClickListener

                formField.locked = !formField.locked
                startIconDrawable = ResourcesCompat.getDrawable(
                    itemView.resources,
                    if (formField.locked) R.drawable.ic_lock else R.drawable.ic_lock_open,
                    itemView.context.theme
                )
                listener.onUpdate(formField)
            }
            hint = formField.label
        }
    }
}