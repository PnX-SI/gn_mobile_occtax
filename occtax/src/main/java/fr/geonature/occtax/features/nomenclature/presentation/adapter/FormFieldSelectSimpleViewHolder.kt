package fr.geonature.occtax.features.nomenclature.presentation.adapter

import android.text.Editable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.textfield.TextInputLayout
import fr.geonature.occtax.R
import fr.geonature.occtax.features.nomenclature.domain.FormField
import fr.geonature.occtax.features.record.domain.PropertyValue

/**
 * [FormFieldAdapter] view holder representing a simple dropdown list allowing only a single
 * selection.
 *
 * @author S. Grimault
 */
class FormFieldSelectSimpleViewHolder(
    parent: ViewGroup,
    private val listener: OnFormFieldSelectSimpleViewHolderListener
) : FormFieldAdapter.AbstractLockableViewHolder<FormField.Select>(
    LayoutInflater.from(parent.context)
        .inflate(
            R.layout.view_action_select_simple,
            parent,
            false
        )
) {
    private var edit: TextInputLayout = itemView.findViewById(android.R.id.edit)
    private var propertyValueTextAdapter = PropertyValueTextAdapter(parent.context)

    init {
        (edit.editText as? AutoCompleteTextView)?.also {
            it.setAdapter(propertyValueTextAdapter)
            it.setOnItemClickListener { _, _, position, _ ->
                formField?.run {
                    edit.error = null
                    error = edit.error
                    setValue(PropertyValue.Text(code = getValue().code,
                        value = propertyValueTextAdapter.getPropertyValue(position)
                            .takeIf { pv -> pv is PropertyValue.Text }
                            ?.let { pv -> pv as PropertyValue.Text }?.code
                    )
                    )
                    listener.onUpdate(this)
                }
            }
        }
    }

    override fun onBind(formField: FormField.Select, lockDefaultValues: Boolean) {
        if (!lockDefaultValues) {
            formField.locked = false
        }

        if (formField.mandatory && formField.getValue()
                .isEmpty()
        ) {
            edit.error = itemView.context.getString(R.string.form_field_error_mandatory)
            formField.error = edit.error
            listener.onUpdate(formField)
        }

        propertyValueTextAdapter.setPropertyValues(formField.values)

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
            (editText as? AutoCompleteTextView)?.apply {
                text = formField.value
                    .let { pv -> formField.values.firstOrNull { it.code == pv.value } }
                    ?.let {
                        Editable.Factory.getInstance()
                            .newEditable(it.value ?: it.code)
                    }
            }
        }
    }

    /**
     * Callback used by [FormFieldSelectSimpleViewHolder].
     */
    interface OnFormFieldSelectSimpleViewHolderListener {

        /**
         * Called when an [FormField.Editable] has been updated.
         *
         * @param editableField the [FormField.Editable] updated
         */
        fun onUpdate(editableField: FormField.Select)
    }
}