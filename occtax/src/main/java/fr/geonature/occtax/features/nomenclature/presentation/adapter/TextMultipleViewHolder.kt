package fr.geonature.occtax.features.nomenclature.presentation.adapter

import android.text.InputType
import android.view.ViewGroup

/**
 * [EditableFieldAdapter] view holder representing a textual value across several lines.
 *
 * @see TextSimpleViewHolder
 * @author S. Grimault
 */
class TextMultipleViewHolder(
    parent: ViewGroup,
    listener: EditableFieldAdapter.OnEditableFieldAdapter
) : TextSimpleViewHolder(
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
}