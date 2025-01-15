package fr.geonature.occtax.features.nomenclature.presentation.adapter

import android.text.Editable
import android.text.InputType
import android.view.ViewGroup
import fr.geonature.occtax.features.nomenclature.domain.FormField
import fr.geonature.occtax.features.record.domain.PropertyValue

/**
 * [EditableFieldAdapter] view holder representing a numeric value.
 *
 * @see AbstractFormFieldTextViewHolder
 * @author S. Grimault
 */
class NumberViewHolder(
    parent: ViewGroup,
    listener: EditableFieldAdapter.OnEditableFieldAdapter
) :
    AbstractFormFieldTextViewHolder<FormField.Number>(
        parent,
        listener
    ) {

    init {
        edit.editText?.apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
    }

    override fun getValue(formField: FormField.Number): String? {
        return formField.value.value?.toString()
    }

    override fun afterTextChanged(s: Editable?) {
        formField?.run {
            setValue(
                PropertyValue.Number(
                    code = getValue().code,
                    value = s?.toString()
                        ?.toDoubleOrNull()
                )
            )
            listener.onUpdate(this)
        }
    }
}