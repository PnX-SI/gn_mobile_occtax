package fr.geonature.occtax.ui.shared.view

import android.content.Context
import android.text.Editable
import android.text.format.DateFormat
import android.util.AttributeSet
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentManager
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import fr.geonature.commons.features.input.domain.AbstractInput
import fr.geonature.commons.util.afterTextChanged
import fr.geonature.commons.util.get
import fr.geonature.commons.util.set
import fr.geonature.occtax.R
import fr.geonature.occtax.settings.InputDateSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Generic [View] about [AbstractInput] start and end date.
 *
 * @author S. Grimault
 */
class InputDateView : ConstraintLayout {

    private lateinit var titleTextView: TextView
    private lateinit var dateStartTextInputLayout: TextInputLayout
    private lateinit var dateEndTextInputLayout: TextInputLayout

    private var dateSettings: InputDateSettings = InputDateSettings.DEFAULT
    private var startDate: Date = Date()
    private var endDate: Date = startDate

    private var listener: OnInputDateViewListener? = null

    constructor(context: Context) : super(context) {
        init(
            null,
            0
        )
    }

    constructor(context: Context, attrs: AttributeSet) : super(
        context,
        attrs
    ) {
        init(
            attrs,
            0
        )
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(
            attrs,
            defStyle
        )
    }

    fun setListener(listener: OnInputDateViewListener) {
        this.listener = listener
    }

    fun setTitle(@StringRes titleResourceId: Int) {
        setTitle(if (titleResourceId == 0) null else context.getString(titleResourceId))
    }

    fun setTitle(title: String?) {
        titleTextView.text = title
        titleTextView.visibility = if (title.isNullOrBlank()) GONE else VISIBLE
    }

    fun setInputDateSettings(dateSettings: InputDateSettings) {
        this.dateSettings = dateSettings

        with(dateStartTextInputLayout) {
            visibility = if (dateSettings.startDateSettings == null) View.GONE else View.VISIBLE
            hint = context.getString(
                if (dateSettings.endDateSettings == null) R.string.input_date_hint
                else R.string.input_date_start_hint
            )
        }
        dateEndTextInputLayout.visibility =
            if (dateSettings.endDateSettings == null) View.GONE else View.VISIBLE
    }

    fun setDates(startDate: Date, endDate: Date?) {
        this.startDate = startDate
        this.endDate = endDate ?: startDate

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

    fun hasErrors(): Boolean {
        return checkStartDateConstraints() != null ||
            checkEndDateConstraints() != null
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        View.inflate(
            context,
            R.layout.view_input_date,
            this
        )

        titleTextView = findViewById(android.R.id.title)

        dateStartTextInputLayout = findViewById<TextInputLayout>(R.id.dateStart).apply {
            visibility = if (dateSettings.startDateSettings == null) View.GONE else View.VISIBLE
            hint = context.getString(
                if (dateSettings.endDateSettings == null) R.string.input_date_hint
                else R.string.input_date_start_hint
            )
            editText?.afterTextChanged {
                error = checkStartDateConstraints()
                dateEndTextInputLayout.error = checkEndDateConstraints()

                error?.also {
                    listener?.hasError(it)
                }
            }
            editText?.setOnClickListener {
                CoroutineScope(Dispatchers.Main).launch {
                    val startDate = selectDateTime(
                        CalendarConstraints
                            .Builder()
                            .setValidator(DateValidatorPointBackward.now())
                            .build(),
                        dateSettings.startDateSettings == InputDateSettings.DateSettings.DATETIME,
                        startDate
                    )

                    this@InputDateView.startDate = startDate

                    if (dateSettings.endDateSettings == null) {
                        this@InputDateView.endDate = startDate
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

                    if (error == null && dateEndTextInputLayout.editText?.error == null) {
                        listener?.onDatesChanged(
                            startDate,
                            endDate
                        )
                    }
                }
            }
        }

        dateEndTextInputLayout = findViewById<TextInputLayout>(R.id.dateEnd).apply {
            visibility = if (dateSettings.endDateSettings == null) View.GONE else View.VISIBLE
            editText?.afterTextChanged {
                error = checkEndDateConstraints()
                dateStartTextInputLayout.error = checkStartDateConstraints()

                error?.also {
                    listener?.hasError(it)
                }
            }
            editText?.setOnClickListener {
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
                                        ).set(
                                            Calendar.MINUTE,
                                            0
                                        ).set(
                                            Calendar.SECOND,
                                            0
                                        ).set(
                                            Calendar.MILLISECOND,
                                            0
                                        ).time
                                )
                            )
                            .build(),
                        dateSettings.endDateSettings == InputDateSettings.DateSettings.DATETIME,
                        endDate
                    )

                    this@InputDateView.endDate = endDate
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

                    if (error == null && dateStartTextInputLayout.editText?.error == null) {
                        listener?.onDatesChanged(
                            startDate,
                            endDate
                        )
                    }
                }
            }
        }

        // Load attributes
        val ta = context.obtainStyledAttributes(
            attrs,
            R.styleable.InputDateView,
            defStyle,
            0
        )

        ta.getString(R.styleable.InputDateView_title)?.also {
            setTitle(it)
        }
        setTitle(
            ta.getResourceId(
                R.styleable.InputDateView_title,
                0
            )
        )

        ta.recycle()
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
        suspendCoroutine { continuation ->
            val fragmentManager = listener?.fragmentManager()

            if (fragmentManager == null) {
                continuation.resume(from)

                return@suspendCoroutine
            }

            val context = context

            if (context == null) {
                continuation.resume(from)

                return@suspendCoroutine
            }

            with(
                MaterialDatePicker.Builder
                    .datePicker()
                    .setSelection(from.time)
                    .setCalendarConstraints(bounds)
                    .build()
            ) {
                addOnPositiveButtonClickListener {
                    val selectedDate = Date(it).set(
                        Calendar.HOUR_OF_DAY,
                        from.get(Calendar.HOUR_OF_DAY)
                    ).set(
                        Calendar.MINUTE,
                        from.get(Calendar.MINUTE)
                    )

                    if (!withTime) {
                        continuation.resume(selectedDate)

                        return@addOnPositiveButtonClickListener
                    }

                    with(
                        MaterialTimePicker.Builder()
                            .setTimeFormat(if (DateFormat.is24HourFormat(context)) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H)
                            .setHour(selectedDate.get(if (DateFormat.is24HourFormat(context)) Calendar.HOUR_OF_DAY else Calendar.HOUR))
                            .setMinute(selectedDate.get(Calendar.MINUTE))
                            .build()
                    ) {
                        addOnPositiveButtonClickListener {
                            continuation.resume(
                                selectedDate.set(
                                    if (DateFormat.is24HourFormat(context)) Calendar.HOUR_OF_DAY else Calendar.HOUR,
                                    hour
                                ).set(
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
                            TIME_PICKER_DIALOG_FRAGMENT
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
                    DATE_PICKER_DIALOG_FRAGMENT
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
                        context.getString(
                            if (dateSettings == InputDateSettings.DateSettings.DATETIME) R.string.input_datetime_format
                            else R.string.input_date_format
                        ),
                        it
                    ).toString()
                )
        }
    }

    /**
     * Checks start date constraints from current [AbstractInput].
     *
     * @return `null` if all constraints are valid, or an error message
     */
    private fun checkStartDateConstraints(): CharSequence? {
        if (startDate.after(Date())) {
            return context.getString(R.string.input_error_date_start_after_now)
        }

        return null
    }

    /**
     * Checks end date constraints from current [AbstractInput].
     *
     * @return `null` if all constraints are valid, or an error message
     */
    private fun checkEndDateConstraints(): CharSequence? {
        if (dateSettings.endDateSettings == null) {
            return null
        }

        if (startDate.after(endDate)) {
            return context.getString(R.string.input_error_date_end_before_start_date)
        }

        return null
    }

    /**
     * Callback used by [InputDateView].
     */
    interface OnInputDateViewListener {

        /**
         * Return the FragmentManager for interacting with fragments associated with this view.
         */
        fun fragmentManager(): FragmentManager?

        /**
         * Called when the start and end dates have been changed.
         */
        fun onDatesChanged(startDate: Date, endDate: Date)

        /**
         * Called when the current start or end dates is not valid.
         */
        fun hasError(message: CharSequence)
    }

    companion object {
        private const val DATE_PICKER_DIALOG_FRAGMENT = "date_picker_dialog_fragment"
        private const val TIME_PICKER_DIALOG_FRAGMENT = "time_picker_dialog_fragment"
    }
}