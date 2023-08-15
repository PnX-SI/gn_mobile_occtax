package fr.geonature.occtax.ui.shared.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import fr.geonature.compat.os.getParcelableCompat
import fr.geonature.compat.os.getSerializableCompat
import fr.geonature.occtax.R
import fr.geonature.occtax.features.settings.domain.InputDateSettings
import fr.geonature.occtax.ui.shared.view.InputDateView
import java.util.Date

/**
 * Custom [Dialog] used to edit input date.
 *
 * @author S. Grimault
 */
class InputDateDialogFragment : DialogFragment() {

    private var onInputDateDialogFragmentListener: OnInputDateDialogFragmentListener? = null

    private var dateSettings: InputDateSettings =
        InputDateSettings(endDateSettings = InputDateSettings.DateSettings.DATE)
    private var startDate: Date = Date()
    private var endDate: Date = startDate
    private var buttonValidate: Button? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = requireContext()

        val view = View.inflate(
            context,
            R.layout.dialog_date,
            null
        )

        // restore the previous state if any
        dateSettings = (savedInstanceState?.getParcelableCompat(KEY_DATE_SETTINGS)
            ?: arguments?.getParcelableCompat(KEY_DATE_SETTINGS)
            ?: InputDateSettings(endDateSettings = InputDateSettings.DateSettings.DATE))
        startDate = savedInstanceState?.getSerializableCompat(KEY_DATE_START)
            ?: arguments?.getSerializableCompat(KEY_DATE_START) ?: Date()
        endDate = savedInstanceState?.getSerializableCompat(KEY_DATE_END)
            ?: arguments?.getSerializableCompat(KEY_DATE_END) ?: startDate

        view.findViewById<InputDateView>(R.id.input_date)?.also {
            it.setInputDateSettings(dateSettings)
            it.setDates(
                startDate,
                endDate
            )
            it.setListener(object : InputDateView.OnInputDateViewListener {
                override fun fragmentManager(): FragmentManager? {
                    return activity?.supportFragmentManager
                }

                override fun onDatesChanged(startDate: Date, endDate: Date) {
                    this@InputDateDialogFragment.startDate = startDate
                    this@InputDateDialogFragment.endDate = endDate

                    buttonValidate?.isEnabled = true
                }

                override fun hasError(message: CharSequence) {
                    // disable validate button unless start and end date are valid
                    buttonValidate?.isEnabled = false
                }
            })
        }

        val alertDialog = AlertDialog.Builder(context)
            .setTitle(
                if (dateSettings.startDateSettings != null && dateSettings.endDateSettings == null) R.string.input_date_start_hint
                else if (dateSettings.startDateSettings == null && dateSettings.endDateSettings != null) R.string.input_date_end_hint
                else R.string.input_date_hint
            )
            .setView(view)
            .setPositiveButton(R.string.alert_dialog_ok) { _, _ ->
                onInputDateDialogFragmentListener?.onDatesChanged(
                    startDate,
                    endDate
                )
            }
            .setNegativeButton(
                R.string.alert_dialog_cancel,
                null
            )
            .create()

        alertDialog.setOnShowListener {
            buttonValidate = (it as AlertDialog).getButton(DialogInterface.BUTTON_POSITIVE)
        }

        return alertDialog
    }

    override fun onSaveInstanceState(outState: Bundle) {
        with(outState) {
            putParcelable(
                KEY_DATE_SETTINGS,
                dateSettings
            )
            putSerializable(
                KEY_DATE_START,
                startDate
            )
            putSerializable(
                KEY_DATE_END,
                endDate
            )
        }

        super.onSaveInstanceState(outState)
    }

    fun setOnInputDateDialogFragmentListenerListener(onInputDateDialogFragmentListener: OnInputDateDialogFragmentListener) {
        this.onInputDateDialogFragmentListener = onInputDateDialogFragmentListener
    }

    companion object {

        const val KEY_DATE_SETTINGS = "key_settings"
        const val KEY_DATE_START = "key_date_start"
        const val KEY_DATE_END = "key_date_end"

        /**
         * Use this factory method to create a new instance of [InputDateDialogFragment].
         *
         * @return A new instance of [InputDateDialogFragment]
         */
        @JvmStatic
        fun newInstance(dateSettings: InputDateSettings, startDate: Date, endDate: Date?) =
            InputDateDialogFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(
                        KEY_DATE_SETTINGS,
                        dateSettings
                    )
                    putSerializable(
                        KEY_DATE_START,
                        startDate
                    )
                    putSerializable(
                        KEY_DATE_END,
                        endDate ?: startDate
                    )
                }
            }
    }

    /**
     * The callback used by [InputDateDialogFragment].
     *
     * @author S. Grimault
     */
    interface OnInputDateDialogFragmentListener {

        /**
         * Invoked when the positive button of the dialog is pressed.
         *
         * @param startDate the start date edited from this dialog
         * @param endDate the end date edited from this dialog
         */
        fun onDatesChanged(startDate: Date, endDate: Date)
    }
}