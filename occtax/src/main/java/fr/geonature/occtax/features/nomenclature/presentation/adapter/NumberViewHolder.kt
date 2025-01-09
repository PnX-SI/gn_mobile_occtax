package fr.geonature.occtax.features.nomenclature.presentation.adapter

import android.text.Editable
import android.text.InputType
import android.view.ViewGroup
import fr.geonature.occtax.features.nomenclature.domain.EditableField
import fr.geonature.occtax.features.record.domain.PropertyValue

/**
 * [EditableFieldAdapter] view holder representing a numeric value.
 *
 * @see TextSimpleViewHolder
 * @author S. Grimault
 */
class NumberViewHolder(
    parent: ViewGroup,
    private val listener: EditableFieldAdapter.OnEditableFieldAdapter
) :
    TextSimpleViewHolder(
        parent,
        listener
    ) {
    init {
        edit.editText?.apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
    }

    override fun afterTextChanged(s: Editable?) {
        editableField?.run {
            value = PropertyValue.Number(
                code,
                s?.toString()
                    ?.toDoubleOrNull()
            )
            listener.onUpdate(this)
        }
    }

    override fun onBind(editableField: EditableField) {
        super.onBind(editableField)

        editableField.value
            ?.takeIf { it is PropertyValue.Number && !it.isEmpty() }
            ?.let { it as PropertyValue.Number }
            ?.value
            ?.also {
                setText(it.toString())
            }
    }
}