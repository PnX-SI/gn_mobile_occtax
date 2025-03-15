package fr.geonature.occtax.features.nomenclature.presentation.adapter

import android.text.Editable
import android.text.InputType
import android.view.ViewGroup
import fr.geonature.occtax.features.nomenclature.domain.FormField
import fr.geonature.occtax.features.record.domain.PropertyValue

/**
 * [FormFieldAdapter] view holder representing a numeric value.
 *
 * @see AbstractFormFieldTextViewHolder
 * @author S. Grimault
 */
class FormFieldNumberViewHolder(
    parent: ViewGroup,
    listener: OnAbstractFormFieldTextViewHolderViewHolderListener<FormField.Number>
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

    override fun getValue(formField: FormField.Number): CharSequence? {
        return formField.value.value?.toString()
    }

    override fun afterTextChanged(s: Editable?) {
        formField?.run {
            setValue(
                PropertyValue.Number(
                    code = getValue().code,
                    value = s?.toString()
                        ?.toLongOrNull()
                )
            )
            listener.onUpdate(this)
        }
    }
}