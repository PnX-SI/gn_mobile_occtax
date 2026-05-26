package fr.geonature.occtax.features.nomenclature.presentation.adapter

import android.annotation.SuppressLint
import android.text.Editable
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import fr.geonature.commons.util.afterTextChanged
import fr.geonature.commons.util.get
import fr.geonature.commons.util.set
import fr.geonature.occtax.R
import fr.geonature.occtax.features.nomenclature.domain.FormField
import fr.geonature.occtax.features.record.domain.ObservationRecord
import fr.geonature.occtax.features.record.domain.PropertyValue
import fr.geonature.occtax.features.settings.domain.InputDateSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Calendar
import java.util.Date
import java.util.TimeZone
import java.util.UUID
import kotlin.coroutines.resume

/**
 * [FormFieldAdapter] view holder representing a group of start and end dates.
 *
 * @author S. Grimault
 */
@SuppressLint("ClickableViewAccessibility")
class FormFieldStartEndViewHolder(
    parent: ViewGroup,
    private val listener: OnFormFieldStartEndViewHolderListener
) : FormFieldAdapter.AbstractFormFieldViewHolder<FormField.StartEnd>(
    LayoutInflater.from(parent.context)
        .inflate(
            R.layout.list_item_formfield_startend,
            parent,
            false
        )
) {
    private val dateStartTextInputLayout: TextInputLayout = itemView.findViewById(R.id.dateStart)
    private val dateEndTextInputLayout: TextInputLayout = itemView.findViewById(R.id.dateEnd)

    private var dateSettings: InputDateSettings = InputDateSettings.DEFAULT
    private var startDate: Date = Date()
    private var endDate: Date = startDate

    init {
        with(dateStartTextInputLayout) {
            editText?.apply {
                showSoftInputOnFocus = false
                keyListener = null
                afterTextChanged {
                    error = checkStartDateConstraints()
                    dateEndTextInputLayout.error = checkEndDateConstraints()

                    formField?.also {
                        it.start.error = error
                        it.end.error = dateEndTextInputLayout.error
                        listener.onUpdate(it)
                    }
                }
                // workaround to request focus on a non-editable field...
                setOnTouchListener { v, event ->
                    if (MotionEvent.ACTION_UP == event.action) {
                        v.requestFocus()
                    }
                    false
                }
                setOnClickListener {
                    CoroutineScope(Dispatchers.Main).launch {
                        val startDate = selectDateTime(
                            CalendarConstraints
                                .Builder()
                                .setValidator(DateValidatorPointBackward.now())
                                .build(),
                            dateSettings.startDateSettings == InputDateSettings.DateSettings.DATETIME,
                            startDate
                        )

                        this@FormFieldStartEndViewHolder.startDate = startDate

                        if (dateSettings.endDateSettings == null) {
                            this@FormFieldStartEndViewHolder.endDate = startDate
                        }

                        dateStartTextInputLayout.editText?.apply {
                            updateDateEditText(
                                this,
                                dateSettings.startDateSettings
                                    ?: InputDateSettings.DateSettings.DATE,
                                startDate
                            )
                        }
                        dateEndTextInputLayout.editText?.apply {
                            updateDateEditText(
                                this,
                                dateSettings.endDateSettings ?: InputDateSettings.DateSettings.DATE,
                                endDate
                            )
                        }

                        formField?.run {
                            if (start.error == null && dateEndTextInputLayout.editText?.error == null) {
                                start.setValue(
                                    PropertyValue.Date(
                                        code = start.getValue().code,
                                        value = startDate
                                    )
                                )

                                if (dateSettings.endDateSettings == null) {
                                    end.setValue(
                                        PropertyValue.Date(
                                            code = end.getValue().code,
                                            value = startDate
                                        )
                                    )
                                }
                            }
                            listener.onUpdate(this)
                        }
                    }
                }
            }
        }

        with(dateEndTextInputLayout) {
            editText?.apply {
                showSoftInputOnFocus = false
                keyListener = null
                afterTextChanged {
                    error = checkEndDateConstraints()
                    dateStartTextInputLayout.error = checkStartDateConstraints()

                    formField?.also {
                        it.start.error = dateStartTextInputLayout.error
                        it.end.error = error
                        listener.onUpdate(it)
                    }
                }
                // workaround to request focus on a non editable field...
                setOnTouchListener { v, event ->
                    if (MotionEvent.ACTION_UP == event.action) {
                        v.requestFocus()
                    }
                    false
                }
                setOnClickListener {
                    CoroutineScope(Dispatchers.Main).launch {
                        val endDate = selectDateTime(
                            CalendarConstraints
                                .Builder()
                                .setValidator(
                                    DateValidatorPointForward.from(
                                        startDate
                                            .set(
                                                Calendar.HOUR_OF_DAY,
                                                0
                                            )
                                            .set(
                                                Calendar.MINUTE,
                                                0
                                            )
                                            .set(
                                                Calendar.SECOND,
                                                0
                                            )
                                            .set(
                                                Calendar.MILLISECOND,
                                                0
                                            ).time
                                    )
                                )
                                .build(),
                            dateSettings.endDateSettings == InputDateSettings.DateSettings.DATETIME,
                            endDate
                        )

                        this@FormFieldStartEndViewHolder.endDate = endDate
                        dateStartTextInputLayout.editText?.apply {
                            updateDateEditText(
                                this,
                                dateSettings.startDateSettings
                                    ?: InputDateSettings.DateSettings.DATE,
                                startDate
                            )
                        }
                        dateEndTextInputLayout.editText?.apply {
                            updateDateEditText(
                                this,
                                dateSettings.endDateSettings ?: InputDateSettings.DateSettings.DATE,
                                endDate
                            )
                        }

                        formField?.run {
                            if (end.error == null && dateStartTextInputLayout.editText?.error == null) {
                                end.setValue(
                                    PropertyValue.Date(
                                        code = end.getValue().code,
                                        value = endDate
                                    )
                                )
                            }
                            listener.onUpdate(this)
                        }
                    }
                }
            }
        }
    }

    override fun onBind(formField: FormField.StartEnd) {
        this.dateSettings = formField.settings
        this.startDate = formField.start.value.value ?: Date()
        this.endDate = formField.end.value.value ?: this.startDate

        itemView.findViewById<TextView>(android.R.id.title).text = formField.label

        with(dateStartTextInputLayout) {
            visibility = if (formField.start.visible) View.VISIBLE else View.GONE
            hint = formField.start.label
        }

        with(dateEndTextInputLayout) {
            visibility =
                if (formField.end.visible && formField.settings.endDateSettings != null) View.VISIBLE else View.GONE
            hint = formField.end.label
        }

        dateStartTextInputLayout.editText?.apply {
            updateDateEditText(
                this,
                dateSettings.startDateSettings ?: InputDateSettings.DateSettings.DATE,
                startDate
            )
        }
        dateEndTextInputLayout.editText?.apply {
            updateDateEditText(
                this,
                dateSettings.endDateSettings ?: InputDateSettings.DateSettings.DATE,
                endDate
            )
        }
    }

    /**
     * Select a new date from given optional date through date/time pickers.
     * If no date was given, use the current date.
     */
    private suspend fun selectDateTime(
        bounds: CalendarConstraints,
        withTime: Boolean = false,
        from: Date = Date()
    ): Date =
        suspendCancellableCoroutine { continuation ->
            val fragmentManager = listener.fragmentManager()

            if (fragmentManager == null) {
                continuation.resume(from)

                return@suspendCancellableCoroutine
            }

            with(
                MaterialDatePicker.Builder
                    .datePicker()
                    .setSelection(from.time)
                    .setCalendarConstraints(bounds)
                    .build()
            ) {
                addOnPositiveButtonClickListener {
                    val selectedDate = Date.from(
                        LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(it),
                            TimeZone.getTimeZone("UTC")
                                .toZoneId()
                        )
                            .atZone(ZoneId.systemDefault())
                            .toInstant()
                    )
                        .set(
                            Calendar.MINUTE,
                            from.get(Calendar.MINUTE)
                        )

                    if (!withTime) {
                        continuation.resume(selectedDate)

                        return@addOnPositiveButtonClickListener
                    }

                    with(
                        MaterialTimePicker.Builder()
                            .setTimeFormat(if (DateFormat.is24HourFormat(itemView.context)) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H)
                            .setHour(selectedDate.get(if (DateFormat.is24HourFormat(itemView.context)) Calendar.HOUR_OF_DAY else Calendar.HOUR))
                            .setMinute(selectedDate.get(Calendar.MINUTE))
                            .build()
                    ) {
                        addOnPositiveButtonClickListener {
                            continuation.resume(
                                selectedDate.set(
                                    if (DateFormat.is24HourFormat(itemView.context)) Calendar.HOUR_OF_DAY else Calendar.HOUR,
                                    hour
                                )
                                    .set(
                                        Calendar.MINUTE,
                                        minute
                                    )
                            )
                        }
                        addOnNegativeButtonClickListener {
                            continuation.resume(selectedDate)
                        }
                        addOnCancelListener {
                            continuation.resume(selectedDate)
                        }
                        show(
                            fragmentManager,
                            "time_picker_dialog_fragment_${
                                formField?.start?.value?.code ?: UUID.randomUUID()
                                    .toString()
                            }"
                        )
                    }
                }
                addOnNegativeButtonClickListener {
                    continuation.resume(from)
                }
                addOnCancelListener {
                    continuation.resume(from)
                }
                show(
                    fragmentManager,
                    "date_picker_dialog_fragment_${
                        formField?.start?.value?.code ?: UUID.randomUUID()
                            .toString()
                    }"
                )
            }
        }

    private fun updateDateEditText(
        editText: EditText,
        dateSettings: InputDateSettings.DateSettings,
        date: Date?
    ) {
        editText.text = date?.let {
            Editable.Factory
                .getInstance()
                .newEditable(
                    DateFormat.format(
                        editText.context.getString(
                            if (dateSettings == InputDateSettings.DateSettings.DATETIME) R.string.input_datetime_format
                            else R.string.input_date_format
                        ),
                        it
                    )
                        .toString()
                )
        }
    }

    /**
     * Checks start date constraints from current [ObservationRecord].
     *
     * @return `null` if all constraints are valid, or an error message
     */
    private fun checkStartDateConstraints(): CharSequence? {
        if (startDate.after(Date())) {
            return itemView.context.getString(R.string.input_error_date_start_after_now)
        }

        return null
    }

    /**
     * Checks end date constraints from current [ObservationRecord].
     *
     * @return `null` if all constraints are valid, or an error message
     */
    private fun checkEndDateConstraints(): CharSequence? {
        if (dateSettings.endDateSettings == null) {
            return null
        }

        if (startDate.after(endDate)) {
            return itemView.context.getString(R.string.input_error_date_end_before_start_date)
        }

        return null
    }

    /**
     * Callback used by [FormFieldStartEndViewHolder].
     */
    interface OnFormFieldStartEndViewHolderListener {

        /**
         * Return the FragmentManager for interacting with fragments associated with this adapter views.
         */
        fun fragmentManager(): FragmentManager?

        /**
         * Called when an [FormField.StartEnd] has been updated.
         *
         * @param formField the [FormField.StartEnd] updated
         */
        fun onUpdate(formField: FormField.StartEnd)
    }
}