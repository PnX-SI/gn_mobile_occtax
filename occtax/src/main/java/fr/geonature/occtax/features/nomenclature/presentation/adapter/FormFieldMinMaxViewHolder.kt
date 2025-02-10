package fr.geonature.occtax.features.nomenclature.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import android.widget.TextView
import fr.geonature.occtax.R
import fr.geonature.occtax.features.nomenclature.domain.FormField
import fr.geonature.occtax.features.record.domain.PropertyValue
import fr.geonature.occtax.ui.shared.view.setOnValueChangedListener
import kotlin.math.ceil

/**
 * [FormFieldAdapter] view holder representing a bounded numerical value.
 *
 * @author S. Grimault
 */
class FormFieldMinMaxViewHolder(
    parent: ViewGroup,
    private val listener: FormFieldAdapter.OnEditableFieldAdapter
) : FormFieldAdapter.AbstractFormFieldViewHolder<FormField.MinMax>(
    LayoutInflater.from(parent.context)
        .inflate(
            R.layout.view_action_min_max,
            parent,
            false
        )
) {
    private val defaultMaxValueOffset = 50
    private var editMinLabel: TextView = itemView.findViewById(R.id.editMinLabel)
    private var editMaxLabel: TextView = itemView.findViewById(R.id.editMaxLabel)
    private var editMinPicker: NumberPicker = itemView.findViewById(R.id.editMinPicker)
    private var editMaxPicker: NumberPicker = itemView.findViewById(R.id.editMaxPicker)

    init {
        with(editMinPicker) {
            minValue = 0
            maxValue = defaultMaxValueOffset
            setOnValueChangedListener(defaultMaxValueOffset) { oldValue, newValue ->
                if (editMaxPicker.value < newValue) {
                    editMaxPicker.maxValue = editMinPicker.maxValue
                    editMaxPicker.value = newValue
                }

                if (editMaxPicker.value == oldValue) {
                    editMaxPicker.value = newValue
                }

                formField?.min?.also { editableField ->
                    editableField.setValue(
                        PropertyValue.Number(
                            code = editableField.value.code,
                            value = newValue
                        )
                    )
                    listener.onUpdate(editableField)
                }
                formField?.max?.also { editableField ->
                    editableField.setValue(
                        PropertyValue.Number(
                            code = editableField.value.code,
                            value = editMaxPicker.value
                        )
                    )
                    listener.onUpdate(editableField)
                }
            }
        }

        with(editMaxPicker) {
            minValue = 0
            maxValue = defaultMaxValueOffset
            setOnValueChangedListener(defaultMaxValueOffset) { _, newValue ->
                editMinPicker.maxValue = editMaxPicker.maxValue

                if (editMinPicker.value > newValue) editMinPicker.value = newValue

                formField?.min?.also { editableField ->
                    editableField.setValue(
                        PropertyValue.Number(
                            code = editableField.value.code,
                            value = editMinPicker.value
                        )
                    )
                    listener.onUpdate(editableField)
                }
                formField?.max?.also { editableField ->
                    editableField.setValue(
                        PropertyValue.Number(
                            code = editableField.value.code,
                            value = newValue
                        )
                    )
                    listener.onUpdate(editableField)
                }
            }
        }
    }

    override fun onBind(formField: FormField.MinMax) {
        with(if (formField.min.visible) View.VISIBLE else View.GONE) {
            editMinLabel.visibility = this
            editMinPicker.visibility = this
        }

        with(if (formField.max.visible) View.VISIBLE else View.GONE) {
            editMaxLabel.visibility = this
            editMaxPicker.visibility = this
        }

        formField.min.value.value?.toInt()
            ?.also {
                if (it > editMinPicker.maxValue) {
                    editMinPicker.maxValue =
                        (ceil((it.toDouble() / defaultMaxValueOffset)) * defaultMaxValueOffset).toInt()
                    editMaxPicker.maxValue = editMinPicker.maxValue
                }

                editMinPicker.value = it
            }

        formField.max.value.value?.toInt()
            ?.also {
                if (it > editMaxPicker.maxValue) {
                    editMaxPicker.maxValue =
                        (ceil((it.toDouble() / defaultMaxValueOffset)) * defaultMaxValueOffset).toInt()
                    editMinPicker.maxValue = editMaxPicker.maxValue
                }

                editMaxPicker.value = it
            }
    }
}