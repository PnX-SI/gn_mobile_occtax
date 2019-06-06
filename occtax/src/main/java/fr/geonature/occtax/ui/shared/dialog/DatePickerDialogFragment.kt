package fr.geonature.occtax.ui.shared.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.DatePicker
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import java.util.Calendar
import java.util.Date

/**
 * Custom {@code Dialog} used to choose the date.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class DatePickerDialogFragment : DialogFragment() {

    private val selectedDateCalendar = Calendar.getInstance()
    private var onCalendarSetListener: OnCalendarSetListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val context = requireContext()

        val view = View.inflate(context,
                                fr.geonature.occtax.R.layout.dialog_date_picker,
                                null)

        // restore the current selected date
        if (savedInstanceState != null) {
            selectedDateCalendar.time = savedInstanceState.getSerializable(STATE_SELECTED_DATE) as Date
        }

        val dialog = AlertDialog.Builder(context,
                                         fr.geonature.occtax.R.style.DialogStyle)
                .setView(view)
                .setPositiveButton(fr.geonature.occtax.R.string.alert_dialog_ok) { _, _ -> onCalendarSetListener?.onCalendarSet(selectedDateCalendar) }
                .setNegativeButton(fr.geonature.occtax.R.string.alert_dialog_cancel,
                                   null)
                .create()

        configureDatePicker(view.findViewById(android.R.id.content) as DatePicker,
                            null,
                            // set the current date as max date selection
                            Calendar.getInstance().time)

        return dialog
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putSerializable(STATE_SELECTED_DATE,
                                 selectedDateCalendar.time)

        super.onSaveInstanceState(outState)
    }

    fun setOnCalendarSetListener(calendarSetListener: OnCalendarSetListener) {

        this.onCalendarSetListener = calendarSetListener
    }

    private fun configureDatePicker(datePicker: DatePicker,
                                    minDate: Date?,
                                    maxDate: Date?) {

        datePicker.isSaveEnabled = true

        datePicker.init(selectedDateCalendar.get(Calendar.YEAR),
                        selectedDateCalendar.get(Calendar.MONTH),
                        selectedDateCalendar.get(Calendar.DAY_OF_MONTH)
        ) { _, year, monthOfYear, dayOfMonth ->
            selectedDateCalendar.set(Calendar.YEAR,
                                     year)
            selectedDateCalendar.set(Calendar.MONTH,
                                     monthOfYear)
            selectedDateCalendar.set(Calendar.DAY_OF_MONTH,
                                     dayOfMonth)
        }

        if (minDate != null) {
            datePicker.minDate = minDate.time
        }

        if (maxDate != null) {
            datePicker.maxDate = maxDate.time
        }
    }

    companion object {

        private const val STATE_SELECTED_DATE = "selected_date"
    }

    /**
     * The callback used to indicate the user changed the date and the time.
     *
     * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
     */
    interface OnCalendarSetListener {

        /**
         * Called upon a date or time change.
         *
         * @param calendar the updated calendar
         */
        fun onCalendarSet(calendar: Calendar)
    }
}