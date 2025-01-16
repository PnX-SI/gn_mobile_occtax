package fr.geonature.occtax.features.nomenclature.presentation.adapter

import android.text.Editable
import android.text.format.DateFormat
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import com.google.android.material.datepicker.MaterialDatePicker
import fr.geonature.occtax.R
import fr.geonature.occtax.features.nomenclature.domain.FormField
import fr.geonature.occtax.features.record.domain.PropertyValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * [EditableFieldAdapter] [FormField.Date] view holder representing a textual value of a date.
 *
 * @see AbstractFormFieldTextViewHolder
 * @author S. Grimault
 */
class FormFieldDateViewHolder(
    parent: ViewGroup,
    listener: EditableFieldAdapter.OnEditableFieldAdapter
) :
    AbstractFormFieldTextViewHolder<FormField.Date>(
        parent,
        listener
    ) {

    init {
        edit.editText?.apply {
            isClickable = false
            imeOptions = EditorInfo.IME_NULL
            focusable = View.NOT_FOCUSABLE
            isFocusableInTouchMode = false

            setOnClickListener {
                CoroutineScope(Dispatchers.Main).launch {
                    val selectedDate = selectDateTime(formField?.value?.value ?: Date())

                    formField?.run {
                        setValue(
                            PropertyValue.Date(
                                code = getValue().code,
                                value = selectedDate
                            )
                        )
                        listener.onUpdate(this)
                    }

                    updateDateEditText(
                        this@apply,
                        selectedDate
                    )
                }
            }
        }
    }

    override fun getValue(formField: FormField.Date): CharSequence? {
        return formField.value.value?.let {
            Editable.Factory
                .getInstance()
                .newEditable(
                    DateFormat.format(
                        itemView.context.getString(R.string.input_date_format),
                        it
                    )
                        .toString()
                )
        }
    }

    override fun afterTextChanged(s: Editable?) {
        // nothing to do...
    }

    private fun updateDateEditText(
        editText: EditText,
        date: Date?
    ) {
        editText.text = date?.let {
            Editable.Factory
                .getInstance()
                .newEditable(
                    DateFormat.format(
                        editText.context.getString(R.string.input_date_format),
                        it
                    )
                        .toString()
                )
        }
    }

    /**
     * Select a new date from given optional date through date picker.
     * If no date was given, use the current date.
     */
    private suspend fun selectDateTime(from: Date = Date()): Date =
        suspendCoroutine { continuation ->
            val fragmentManager = listener.fragmentManager()

            if (fragmentManager == null) {
                continuation.resume(from)

                return@suspendCoroutine
            }

            with(
                MaterialDatePicker.Builder
                    .datePicker()
                    .setSelection(from.time)
                    .build()
            ) {
                addOnPositiveButtonClickListener {
                    continuation.resume(Date(it))
                }
                addOnNegativeButtonClickListener {
                    continuation.resume(from)
                }
                addOnCancelListener {
                    continuation.resume(from)
                }
                show(
                    fragmentManager,
                    "date_picker_dialog_fragment${
                        formField?.value?.code ?: UUID.randomUUID()
                            .toString()
                    }"
                )
            }
        }
}