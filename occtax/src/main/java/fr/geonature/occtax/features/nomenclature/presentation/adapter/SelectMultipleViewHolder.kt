package fr.geonature.occtax.features.nomenclature.presentation.adapter

import android.app.AlertDialog
import android.content.Context
import android.text.Editable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.EditText
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.textfield.TextInputLayout
import fr.geonature.occtax.R
import fr.geonature.occtax.features.nomenclature.domain.FormField
import fr.geonature.occtax.features.record.domain.PropertyValue

/**
 * [EditableFieldAdapter] view holder representing a simple dropdown list allowing multiple
 * selection.
 *
 * @author S. Grimault
 */
class SelectMultipleViewHolder(
    parent: ViewGroup,
    private val listener: EditableFieldAdapter.OnEditableFieldAdapter
) : EditableFieldAdapter.AbstractLockableViewHolder<FormField.SelectMultiple>(
    LayoutInflater.from(parent.context)
        .inflate(
            R.layout.view_action_select_simple,
            parent,
            false
        )
) {
    private var edit: TextInputLayout = itemView.findViewById(android.R.id.edit)

    init {
        (edit.editText as? AutoCompleteTextView)?.also { editText ->
            editText.setOnClickListener {
                formField?.also {
                    showSelectionDialog(
                        itemView.context,
                        it,
                        editText
                    )
                }
            }
            edit.setEndIconOnClickListener {
                formField?.also {
                    showSelectionDialog(
                        itemView.context,
                        it,
                        editText
                    )
                }
            }
        }
    }

    override fun onBind(formField: FormField.SelectMultiple, lockDefaultValues: Boolean) {
        if (!lockDefaultValues) {
            formField.locked = false
        }

        if (formField.mandatory && formField.getValue()
                .isEmpty()
        ) {
            edit.error = itemView.context.getString(R.string.form_field_error_mandatory)
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
            (editText as? AutoCompleteTextView)?.apply {
                text = formField.value
                    .let { stringArray ->
                        formField.values.mapNotNull { pv ->
                            pv.let { text ->
                                stringArray.value.firstOrNull { it == text.code }
                                    ?.let { text }
                            }
                        }
                    }
                    .joinToString(", ") { it.value ?: it.code }
                    .let {
                        Editable.Factory.getInstance()
                            .newEditable(it)
                    }
            }
        }
    }

    private fun showSelectionDialog(
        context: Context,
        editableField: FormField.SelectMultiple,
        editText: EditText
    ) {
        val items = editableField.values
            .map { it.value ?: it.code }
            .toTypedArray()
        val selectedItems = editableField.values
            .associateWith { false }
            .let {
                val selectedItems =
                    editableField.value.value
                it.map { item -> item.key to selectedItems.any { selectedItem -> selectedItem == item.key.code } }
            }
            .map { it.second }
            .toBooleanArray()

        AlertDialog.Builder(context)
            .setTitle(
                editableField.label
            )
            .setNegativeButton(context.getString(R.string.alert_dialog_cancel)) { _, _ ->
                // nothing to do...
            }
            .setPositiveButton(context.getString(R.string.alert_dialog_ok)) { _, _ ->
                PropertyValue.StringArray(
                    code = editableField.value.code,
                    value = editableField.values
                        .mapIndexed { index, v ->
                            v to selectedItems[index]
                        }
                        .filter {
                            it.second
                        }
                        .map { it.first.code }
                        .toTypedArray()
                )
                    .also { propertyValue ->
                        edit.error =
                            if (editableField.mandatory && propertyValue.isEmpty()) itemView.context.getString(R.string.form_field_error_mandatory) else null

                        editableField.setValue(propertyValue)
                        editText.text = propertyValue
                            .let { stringArray ->
                                editableField.values.mapNotNull { pv ->
                                    pv.let { text ->
                                        stringArray.value.firstOrNull { it == text.code }
                                            ?.let { text }
                                    }
                                }
                            }
                            .joinToString(", ") { it.value ?: it.code }
                            .let {
                                Editable.Factory.getInstance()
                                    .newEditable(it)
                            }
                    }
                listener.onUpdate(editableField)
            }
            .setMultiChoiceItems(
                items,
                selectedItems
            ) { _, which, checked ->
                selectedItems[which] = checked
            }
            .show()
    }
}