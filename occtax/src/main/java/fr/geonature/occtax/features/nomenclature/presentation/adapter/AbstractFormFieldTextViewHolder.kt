package fr.geonature.occtax.features.nomenclature.presentation.adapter

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.textfield.TextInputLayout
import fr.geonature.commons.util.KeyboardUtils.hideSoftKeyboard
import fr.geonature.occtax.R
import fr.geonature.occtax.features.nomenclature.domain.FormField

/**
 * [FormFieldAdapter] view holder representing a textual value.
 *
 * @author S. Grimault
 */
abstract class AbstractFormFieldTextViewHolder<FF : FormField.Editable>(
    parent: ViewGroup,
    internal val listener: FormFieldAdapter.OnEditableFieldAdapter
) : FormFieldAdapter.AbstractLockableViewHolder<FF>(
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
            this@AbstractFormFieldTextViewHolder.afterTextChanged(s)

            edit.error = null

            formField?.also {
                edit.error = if (it.mandatory && (s == null || s.toString()
                        .isBlank())
                ) itemView.context.getString(R.string.form_field_error_mandatory) else null
            }
        }
    }

    init {
        with(edit) {
            editText?.addTextChangedListener(textWatcher)
            setOnFocusChangeListener { v, hasFocus ->
                if (!hasFocus) {
                    // workaround to force hide the soft keyboard
                    hideSoftKeyboard(v)
                }
            }
        }
    }

    override fun onBind(formField: FF, lockDefaultValues: Boolean) {
        if (!lockDefaultValues) {
            formField.locked = false
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

        edit.post {
            getValue(formField)?.also { setText(it) }
        }

        if (formField.mandatory && formField.getValue()
                .isEmpty()
        ) {
            edit.error = itemView.context.getString(R.string.form_field_error_mandatory)
        }
    }

    abstract fun getValue(formField: FF): CharSequence?
    abstract fun afterTextChanged(s: Editable?)

    private fun setText(charSequence: CharSequence) {
        edit.editText?.removeTextChangedListener(textWatcher)
        edit.editText?.text = Editable.Factory.getInstance()
            .newEditable(charSequence)
        edit.editText?.addTextChangedListener(textWatcher)
    }
}