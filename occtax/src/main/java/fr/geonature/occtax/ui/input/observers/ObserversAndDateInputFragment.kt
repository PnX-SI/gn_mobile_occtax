package fr.geonature.occtax.ui.input.observers

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment
import fr.geonature.commons.data.InputObserver
import fr.geonature.occtax.R
import fr.geonature.occtax.ui.input.InputPagerFragmentActivity
import fr.geonature.occtax.ui.observers.InputObserverListActivity
import fr.geonature.occtax.ui.shared.dialog.DatePickerDialogFragment
import fr.geonature.occtax.ui.shared.view.ListItemActionView
import fr.geonature.viewpager.ui.IValidateFragment
import java.util.Calendar
import java.util.Date

/**
 * Selected observer and current date as first {@code Fragment} used by [InputPagerFragmentActivity].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class ObserversAndDateInputFragment : Fragment(),
                                      IValidateFragment {

    private val inputObservers: MutableList<InputObserver> = ArrayList()
    private var inputDate = Date()

    private var selectedObserversActionView: ListItemActionView? = null
    private var inputDateActionView: ListItemActionView? = null

    private val onCalendarSetListener = object : DatePickerDialogFragment.OnCalendarSetListener {
        override fun onCalendarSet(calendar: Calendar) {
            inputDate = calendar.time
            inputDateActionView?.setItems(listOf(Pair.create(DateFormat.format(getString(R.string.observers_and_date_date_format),
                                                                               inputDate).toString(),
                                                             "")))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val supportFragmentManager = activity?.supportFragmentManager ?: return

        val dialogFragment = supportFragmentManager.findFragmentByTag(DATE_PICKER_DIALOG_FRAGMENT) as DatePickerDialogFragment?
        dialogFragment?.setOnCalendarSetListener(onCalendarSetListener)
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_observers_and_date_input,
                                    container,
                                    false)

        selectedObserversActionView = view.findViewById(R.id.selected_observers_action_view)
        selectedObserversActionView?.setListener(object : ListItemActionView.OnListItemActionViewListener {
            override fun onAction() {
                val context = context ?: return

                startActivityForResult(InputObserverListActivity.newIntent(context,
                                                                           ListView.CHOICE_MODE_MULTIPLE,
                                                                           inputObservers),
                                       0)
            }
        })

        inputDateActionView = view.findViewById(R.id.input_date_action_view)
        inputDateActionView?.setListener(object : ListItemActionView.OnListItemActionViewListener {
            override fun onAction() {
                val supportFragmentManager = activity?.supportFragmentManager ?: return
                val datePickerDialogFragment = DatePickerDialogFragment()
                datePickerDialogFragment.setOnCalendarSetListener(onCalendarSetListener)
                datePickerDialogFragment.show(supportFragmentManager,
                                              DATE_PICKER_DIALOG_FRAGMENT)
            }
        })
        inputDateActionView?.setItems(listOf(Pair.create(DateFormat.format(getString(R.string.observers_and_date_date_format),
                                                                           inputDate).toString(),
                                                         "")))

        return view
    }

    override fun onActivityResult(
            requestCode: Int,
            resultCode: Int,
            data: Intent?) {
        if ((resultCode == Activity.RESULT_OK) && (data != null)) {
            val selectedInputObservers = data.getParcelableArrayListExtra<InputObserver>(InputObserverListActivity.EXTRA_SELECTED_INPUT_OBSERVERS)
            inputObservers.clear()
            inputObservers.addAll(selectedInputObservers)
            selectedObserversActionView?.setTitle(resources.getQuantityString(R.plurals.observers_and_date_selected_observers,
                                                                              selectedInputObservers.size,
                                                                              selectedInputObservers.size))
            selectedObserversActionView?.setItems(inputObservers.map { inputObserver ->
                Pair.create(inputObserver.lastname?.toUpperCase() ?: "",
                            inputObserver.firstname)
            })
        }
    }

    override fun getResourceTitle(): Int {
        return R.string.pager_fragment_observers_and_date_input_title
    }

    override fun pagingEnabled(): Boolean {
        return true
    }

    override fun validate(): Boolean {
        return true
    }

    override fun refreshView() {}

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