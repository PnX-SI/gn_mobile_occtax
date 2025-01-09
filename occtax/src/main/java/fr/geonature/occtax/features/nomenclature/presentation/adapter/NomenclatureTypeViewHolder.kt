package fr.geonature.occtax.features.nomenclature.presentation.adapter

import android.text.Editable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.textfield.TextInputLayout
import fr.geonature.commons.lifecycle.observeOnce
import fr.geonature.occtax.R
import fr.geonature.occtax.features.nomenclature.domain.EditableField
import fr.geonature.occtax.features.record.domain.PropertyValue

/**
 * [EditableFieldAdapter] view holder representing a nomenclature values as simple dropdown list.
 *
 * @author S. Grimault
 */
class NomenclatureTypeViewHolder(
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
    private val nomenclatureAdapter = NomenclatureValueAdapter(parent.context)

    init {
        (edit.editText as? AutoCompleteTextView)?.also {
            it.setAdapter(nomenclatureAdapter)
            it.setOnItemClickListener { _, _, position, _ ->
                editableField?.run {
                    value = nomenclatureAdapter.getNomenclatureValue(position)
                        .let { nomenclature ->
                            PropertyValue.Nomenclature(
                                code,
                                nomenclature.defaultLabel,
                                nomenclature.id
                            )
                        }
                    listener.onUpdate(this)
                }
            }
        }
    }

    override fun onBind(editableField: EditableField, lockDefaultValues: Boolean) {
        if (!lockDefaultValues) {
            editableField.locked = false
        }

        listener.getNomenclatureValues(editableField.nomenclatureType ?: editableField.code)
            .observeOnce(listener.getLifecycleOwner()) { nomenclatureValues ->
                nomenclatureAdapter.setNomenclatureValues(nomenclatureValues ?: listOf())
                (edit.editText as AutoCompleteTextView?)?.text = editableField.value
                    ?.takeIf { it is PropertyValue.Nomenclature }
                    ?.let { it as PropertyValue.Nomenclature }
                    ?.let { pv -> nomenclatureValues?.firstOrNull { it.id == pv.value } }
                    ?.let { nomenclatureValue ->
                        Editable.Factory.getInstance()
                            .newEditable(nomenclatureValue.defaultLabel)
                    }
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
        }
    }
}