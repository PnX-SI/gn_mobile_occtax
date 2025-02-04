package fr.geonature.occtax.features.nomenclature.presentation.adapter

import android.text.Editable
import android.text.InputType
import android.view.ViewGroup
import fr.geonature.occtax.features.nomenclature.domain.FormField
import fr.geonature.occtax.features.record.domain.PropertyValue

/**
 * [FormFieldAdapter] view holder representing a textual value across several lines.
 *
 * @see FormFieldTextSimpleViewHolder
 * @author S. Grimault
 */
class FormFieldTextMultipleViewHolder(
    parent: ViewGroup,
    listener: FormFieldAdapter.OnEditableFieldAdapter
) : AbstractFormFieldTextViewHolder<FormField.TextMultiple>(
    parent,
    listener
) {
    init {
        edit.isCounterEnabled = true
        edit.editText?.apply {
            isSingleLine = false
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
            minLines = 2
            maxLines = 4
        }
    }

    override fun getValue(formField: FormField.TextMultiple): CharSequence? {
        return formField.value.value
    }

    override fun afterTextChanged(s: Editable?) {
        formField?.run {
            setValue(PropertyValue.Text(code = getValue().code,
                value = s?.toString()
                    ?.ifEmpty { null }
                    ?.ifBlank { null })
            )
            listener.onUpdate(this)
        }
    }
}