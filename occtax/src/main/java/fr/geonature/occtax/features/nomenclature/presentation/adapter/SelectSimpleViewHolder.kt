package fr.geonature.occtax.features.nomenclature.presentation.adapter

import android.text.Editable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.textfield.TextInputLayout
import fr.geonature.occtax.R
import fr.geonature.occtax.features.nomenclature.domain.EditableField
import fr.geonature.occtax.features.record.domain.PropertyValue

/**
 * [EditableFieldAdapter] view holder representing a simple dropdown list allowing only a single
 * selection.
 *
 * @author S. Grimault
 */
class SelectSimpleViewHolder(
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
    private var propertyValueTextAdapter = PropertyValueTextAdapter(parent.context)

    init {
        (edit.editText as? AutoCompleteTextView)?.also {
            it.setAdapter(propertyValueTextAdapter)
            it.setOnItemClickListener { _, _, position, _ ->
                editableField?.run {
                    value = PropertyValue.Text(
                        code = code,
                        propertyValueTextAdapter.getPropertyValue(position)
                            .takeIf { pv -> pv is PropertyValue.Text }
                            ?.let { pv -> pv as PropertyValue.Text }?.code
                    )
                    listener.onUpdate(this)
                }
            }
        }
    }

    override fun onBind(editableField: EditableField, lockDefaultValues: Boolean) {
        if (!lockDefaultValues) {
            editableField.locked = false
        }

        propertyValueTextAdapter.setPropertyValues(editableField.values)

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
                    ?.takeIf { it is PropertyValue.Text }
                    ?.let { it as PropertyValue.Text }
                    ?.let { pv -> editableField.values.firstOrNull { it is PropertyValue.Text && it.code == pv.value } }
                    ?.let { it as PropertyValue.Text }
                    ?.let {
                        Editable.Factory.getInstance()
                            .newEditable(it.value ?: it.code)
                    }
            }
        }
    }
}