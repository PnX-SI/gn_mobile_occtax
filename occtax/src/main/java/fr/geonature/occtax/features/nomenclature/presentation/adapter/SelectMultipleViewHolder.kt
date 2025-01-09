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
import fr.geonature.occtax.features.nomenclature.domain.EditableField
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
) : EditableFieldAdapter.AbstractLockableViewHolder(
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
                editableField?.also {
                    showSelectionDialog(
                        itemView.context,
                        it,
                        editText
                    )
                }
            }
            edit.setEndIconOnClickListener {
                editableField?.also {
                    showSelectionDialog(
                        itemView.context,
                        it,
                        editText
                    )
                }
            }
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
            hint = editableField.label ?: getDefaultLabel(editableField)
            (editText as? AutoCompleteTextView)?.apply {
                text = editableField.value
                    ?.takeIf { it is PropertyValue.StringArray }
                    ?.let { it as PropertyValue.StringArray }
                    ?.let { stringArray ->
                        editableField.values.mapNotNull { pv ->
                            pv.takeIf { it is PropertyValue.Text }
                                ?.let { it as PropertyValue.Text }
                                ?.let { text ->
                                    stringArray.value.firstOrNull { it == text.code }
                                        ?.let { text }
                                }
                        }
                    }
                    ?.joinToString(", ") { it.value ?: it.code }
                    ?.let {
                        Editable.Factory.getInstance()
                            .newEditable(it)
                    }
            }
        }
    }

    private fun showSelectionDialog(
        context: Context,
        editableField: EditableField,
        editText: EditText
    ) {
        val items = editableField.values.filterIsInstance<PropertyValue.Text>()
            .map { it.value ?: it.code }
            .toTypedArray()
        val selectedItems = editableField.values.filterIsInstance<PropertyValue.Text>()
            .associateWith { false }
            .let {
                val selectedItems =
                    editableField.value?.takeIf { v -> v is PropertyValue.StringArray }
                        ?.let { v -> v as PropertyValue.StringArray }?.value ?: emptyArray()
                it.map { item -> item.key to selectedItems.any { selectedItem -> selectedItem == item.key.code } }
            }
            .map { it.second }
            .toBooleanArray()

        AlertDialog.Builder(context)
            .setTitle(
                editableField.label ?: getDefaultLabel(editableField)
            )
            .setNegativeButton(context.getString(R.string.alert_dialog_cancel)) { _, _ ->
                // nothing to do...
            }
            .setPositiveButton(context.getString(R.string.alert_dialog_ok)) { _, _ ->
                PropertyValue.StringArray(
                    code = editableField.code,
                    value = editableField.values.filterIsInstance<PropertyValue.Text>()
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
                        editableField.value = propertyValue
                        editText.text = propertyValue
                            .let { stringArray ->
                                editableField.values.mapNotNull { pv ->
                                    pv.takeIf { it is PropertyValue.Text }
                                        ?.let { it as PropertyValue.Text }
                                        ?.let { text ->
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