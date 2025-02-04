package fr.geonature.occtax.features.nomenclature.presentation.adapter

import android.text.Editable
import android.text.format.DateFormat
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import fr.geonature.commons.util.get
import fr.geonature.occtax.features.nomenclature.domain.FormField
import fr.geonature.occtax.features.record.domain.PropertyValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * [FormFieldAdapter] [FormField.Time] view holder representing a textual value of a local time.
 *
 * @see AbstractFormFieldTextViewHolder
 * @author S. Grimault
 */
class FormFieldTimeViewHolder(
    parent: ViewGroup,
    listener: FormFieldAdapter.OnEditableFieldAdapter
) :
    AbstractFormFieldTextViewHolder<FormField.Time>(
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
                    formField?.run {
                        val selectedTime = selectTime(value)
                        setValue(selectedTime)
                        listener.onUpdate(this)
                        this@apply.text = Editable.Factory
                            .getInstance()
                            .newEditable(selectedTime.toTimeString())
                    }
                }
            }
        }
    }

    override fun getValue(formField: FormField.Time): CharSequence? {
        return formField.value.toTimeString()
    }

    override fun afterTextChanged(s: Editable?) {
        // nothing to do...
    }

    /**
     * Select a new time through time pickers.
     */
    private suspend fun selectTime(propertyValue: PropertyValue.Time): PropertyValue.Time =
        suspendCoroutine { continuation ->
            val fragmentManager = listener.fragmentManager()

            if (fragmentManager == null) {
                continuation.resume(propertyValue)

                return@suspendCoroutine
            }

            val now = Date()

            with(
                MaterialTimePicker.Builder()
                    .setTimeFormat(if (DateFormat.is24HourFormat(itemView.context)) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H)
                    .setHour(
                        propertyValue.hour
                            ?: now.get(if (DateFormat.is24HourFormat(itemView.context)) Calendar.HOUR_OF_DAY else Calendar.HOUR)
                    )
                    .setMinute(propertyValue.minute ?: now.get(Calendar.MINUTE))
                    .build()
            ) {
                addOnPositiveButtonClickListener {
                    continuation.resume(
                        propertyValue.copy(
                            hour = hour,
                            minute = minute
                        )
                    )
                }
                addOnNegativeButtonClickListener {
                    continuation.resume(propertyValue)
                }
                addOnCancelListener {
                    continuation.resume(propertyValue)
                }
                show(
                    fragmentManager,
                    "time_picker_dialog_fragment${
                        formField?.value?.code ?: UUID.randomUUID()
                            .toString()
                    }"
                )
            }
        }
}