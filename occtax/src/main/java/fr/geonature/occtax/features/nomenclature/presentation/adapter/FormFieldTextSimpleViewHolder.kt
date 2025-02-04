package fr.geonature.occtax.features.nomenclature.presentation.adapter

import android.text.Editable
import android.view.ViewGroup
import fr.geonature.occtax.features.nomenclature.domain.FormField
import fr.geonature.occtax.features.record.domain.PropertyValue

/**
 * [FormFieldAdapter] view holder representing a textual value.
 *
 * @see AbstractFormFieldTextViewHolder
 * @author S. Grimault
 */
open class FormFieldTextSimpleViewHolder(
    parent: ViewGroup,
    listener: FormFieldAdapter.OnEditableFieldAdapter
) : AbstractFormFieldTextViewHolder<FormField.Text>(
    parent,
    listener
) {
    override fun getValue(formField: FormField.Text): CharSequence? {
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