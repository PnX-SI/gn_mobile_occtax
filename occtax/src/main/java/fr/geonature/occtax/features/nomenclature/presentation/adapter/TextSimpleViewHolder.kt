package fr.geonature.occtax.features.nomenclature.presentation.adapter

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.textfield.TextInputLayout
import fr.geonature.commons.util.KeyboardUtils
import fr.geonature.occtax.R
import fr.geonature.occtax.features.nomenclature.domain.EditableField
import fr.geonature.occtax.features.record.domain.PropertyValue

/**
 * [EditableFieldAdapter] view holder representing a textual value.
 *
 * @author S. Grimault
 */
open class TextSimpleViewHolder(
    parent: ViewGroup,
    private val listener: EditableFieldAdapter.OnEditableFieldAdapter
) : EditableFieldAdapter.AbstractLockableViewHolder(
    LayoutInflater.from(parent.context)
        .inflate(
            R.layout.view_action_edit_text,
            parent,
            false
        )
) {
    internal var edit: TextInputLayout = itemView.findViewById(android.R.id.edit)
    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(
            s: CharSequence?,
            start: Int,
            count: Int,
            after: Int
        ) {
        }

        override fun onTextChanged(
            s: CharSequence?,
            start: Int,
            before: Int,
            count: Int
        ) {
        }

        override fun afterTextChanged(s: Editable?) {
            this@TextSimpleViewHolder.afterTextChanged(s)
        }
    }

    init {
        with(edit) {
            editText?.addTextChangedListener(textWatcher)
            setOnFocusChangeListener { v, hasFocus ->
                if (!hasFocus) {
                    // workaround to force hide the soft keyboard
                    KeyboardUtils.hideSoftKeyboard(v)
                }
            }
        }
    }

    internal open fun afterTextChanged(s: Editable?) {
        editableField?.run {
            value = PropertyValue.Text(
                code,
                s?.toString()
                    ?.ifEmpty { null }
                    ?.ifBlank { null }
            )
            listener.onUpdate(this)
        }
    }

    override fun onBind(editableField: EditableField, lockDefaultValues: Boolean) {
        if (!lockDefaultValues) {
            editableField.locked = false
        }

        with(edit) {
            startIconDrawable = if (lockDefaultValues) ResourcesCompat.getDrawable(
                itemView.resources,
                if (editableField.locked) R.drawable.ic_lock else R.drawable.ic_lock_open,
                itemView.context.theme
            ) else null
            setStartIconOnClickListener {
                if (!lockDefaultValues) return@setStartIconOnClickListener

                editableField.locked = !editableField.locked
                startIconDrawable = ResourcesCompat.getDrawable(
                    itemView.resources,
                    if (editableField.locked) R.drawable.ic_lock else R.drawable.ic_lock_open,
                    itemView.context.theme
                )
                listener.onUpdate(editableField)
            }
            hint = getDefaultLabel(editableField)
        }

        editableField.value
            ?.takeIf { it is PropertyValue.Text && !it.isEmpty() }
            ?.let { it as PropertyValue.Text }
            ?.value
            ?.also {
                setText(it)
            }
    }

    internal fun setText(charSequence: CharSequence) {
        edit.editText?.removeTextChangedListener(textWatcher)
        edit.editText?.text = Editable.Factory.getInstance()
            .newEditable(charSequence)
        edit.editText?.addTextChangedListener(textWatcher)
    }
}