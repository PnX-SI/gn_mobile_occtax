package fr.geonature.occtax.ui.input.observers

import android.os.Bundle
import android.text.format.DateFormat
import android.util.Pair
import android.widget.Toast
import fr.geonature.occtax.R
import fr.geonature.occtax.ui.input.InputPagerFragmentActivity
import fr.geonature.occtax.ui.shared.dialog.DatePickerDialogFragment
import fr.geonature.occtax.ui.shared.fragment.AbstractSelectedItemsRecyclerViewFragment
import fr.geonature.viewpager.ui.IValidateFragment
import java.util.Calendar
import java.util.Date

/**
 * Selected observer and current date as first {@code Fragment} used by [InputPagerFragmentActivity].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class ObserversAndDateInputFragment : AbstractSelectedItemsRecyclerViewFragment(),
                                      IValidateFragment {

    private var inputDate = Date()

    private val onCalendarSetListener = object : DatePickerDialogFragment.OnCalendarSetListener {
        override fun onCalendarSet(calendar: Calendar) {
            inputDate = calendar.time
            notifyItemChanged(1)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val supportFragmentManager = activity?.supportFragmentManager ?: return

        val dialogFragment = supportFragmentManager.findFragmentByTag(DATE_PICKER_DIALOG_FRAGMENT) as DatePickerDialogFragment?
        dialogFragment?.setOnCalendarSetListener(onCalendarSetListener)
    }

    override fun getResourceTitle(): Int {
        return R.string.fragment_observers_and_date_input_title
    }

    override fun pagingEnabled(): Boolean {
        return true
    }

    override fun validate(): Boolean {
        return true
    }

    override fun refreshView() {}

    override fun getSelectedItemsCount(): Int {
        return 2
    }

    override fun getSelectedItemsTitle(position: Int): Int {
        return when (position) {
            0 -> R.string.observers_and_date_selected_observers
            1 -> R.string.observers_and_date_date
            else -> throw IllegalArgumentException()
        }
    }

    override fun getSelectedItemsEmptyText(position: Int): Int {
        return when (position) {
            0 -> R.string.observers_and_date_selected_observers_no_data
            else -> R.string.no_data
        }
    }

    override fun getActionText(position: Int): Int {
        return when (position) {
            0 -> R.string.action_edit
            1 -> R.string.action_edit
            else -> throw IllegalArgumentException()
        }
    }

    override fun getActionEmptyText(position: Int): Int {
        return when (position) {
            0 -> R.string.action_add
            1 -> R.string.action_edit
            else -> throw IllegalArgumentException()
        }
    }

    override fun getVisibleItems(position: Int): Int {
        return when (position) {
            0 -> 2
            1 -> 1
            else -> throw IllegalArgumentException()
        }
    }

    override fun getSelectedItems(position: Int): Collection<Pair<String, String?>> {
        return when (position) {
            0 -> emptyList()
            1 -> listOf(Pair.create(DateFormat.format(getString(R.string.observers_and_date_date_format),
                                                      inputDate).toString(),
                                    ""))
            else -> throw IllegalArgumentException()
        }
    }

    override fun onSelectedItemsAction(position: Int) {
        val context = context ?: return

        when (position) {
            0 -> // TODO: manage action
                Toast.makeText(context,
                               "Not implemented",
                               Toast.LENGTH_SHORT)
                        .show()
            1 -> {
                val supportFragmentManager = activity?.supportFragmentManager ?: return
                val datePickerDialogFragment = DatePickerDialogFragment()
                datePickerDialogFragment.setOnCalendarSetListener(onCalendarSetListener)
                datePickerDialogFragment.show(supportFragmentManager,
                                              DATE_PICKER_DIALOG_FRAGMENT)
            }
        }
    }

    companion object {

        private const val DATE_PICKER_DIALOG_FRAGMENT = "date_picker_dialog_fragment"

        /**
         * Use this factory method to create a new instance of [ObserversAndDateInputFragment].
         *
         * @return A new instance of [ObserversAndDateInputFragment]
         */
        @JvmStatic
        fun newInstance() = ObserversAndDateInputFragment()
    }
}