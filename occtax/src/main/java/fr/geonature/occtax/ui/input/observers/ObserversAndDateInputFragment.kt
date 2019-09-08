package fr.geonature.occtax.ui.input.observers

import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import fr.geonature.commons.data.InputObserver
import fr.geonature.commons.data.Provider
import fr.geonature.commons.input.AbstractInput
import fr.geonature.occtax.R
import fr.geonature.occtax.input.Input
import fr.geonature.occtax.ui.input.IInputFragment
import fr.geonature.occtax.ui.input.InputPagerFragmentActivity
import fr.geonature.occtax.ui.observers.InputObserverListActivity
import fr.geonature.occtax.ui.shared.dialog.DatePickerDialogFragment
import fr.geonature.occtax.ui.shared.view.ListItemActionView
import fr.geonature.occtax.util.SettingsUtils.getDefaultObserverId
import fr.geonature.viewpager.ui.IValidateFragment
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Selected observer and current date as first {@code Fragment} used by [InputPagerFragmentActivity].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class ObserversAndDateInputFragment : Fragment(),
                                      IValidateFragment,
                                      IInputFragment {

    private var input: Input? = null
    private val selectedInputObservers: MutableList<InputObserver> = mutableListOf()

    private var selectedInputObserversActionView: ListItemActionView? = null
    private var inputDateActionView: ListItemActionView? = null

    private val loaderCallbacks = object : LoaderManager.LoaderCallbacks<Cursor> {
        override fun onCreateLoader(
                id: Int,
                args: Bundle?): Loader<Cursor> {

            when (id) {
                LOADER_OBSERVERS_IDS -> {
                    return CursorLoader(requireContext(),
                                        Provider.buildUri(InputObserver.TABLE_NAME,
                                                          args?.getLongArray(KEY_SELECTED_INPUT_OBSERVER_IDS)?.joinToString(",")
                                                                  ?: ""),
                                        arrayOf(InputObserver.COLUMN_ID,
                                                InputObserver.COLUMN_LASTNAME,
                                                InputObserver.COLUMN_FIRSTNAME),
                                        null,
                                        null,
                                        null)
                }

                else -> throw IllegalArgumentException()
            }
        }

        override fun onLoadFinished(
                loader: Loader<Cursor>,
                data: Cursor?) {

            if (data == null) {
                Log.w(TAG,
                      "Failed to load data from '${(loader as CursorLoader).uri}'")

                return
            }

            when (loader.id) {
                LOADER_OBSERVERS_IDS -> {
                    if (data.moveToFirst()) {
                        selectedInputObservers.clear()

                        while (!data.isAfterLast) {
                            val selectedInputObserver = InputObserver.fromCursor(data)

                            if (selectedInputObserver != null) {
                                selectedInputObservers.add(selectedInputObserver)
                            }

                            data.moveToNext()
                        }

                        updateSelectedObserversActionView(selectedInputObservers)
                    }
                }
            }
        }

        override fun onLoaderReset(loader: Loader<Cursor>) {
            when (loader.id) {
                LOADER_OBSERVERS_IDS -> selectedInputObservers.clear()
            }
        }
    }

    private val onCalendarSetListener = object : DatePickerDialogFragment.OnCalendarSetListener {
        override fun onCalendarSet(calendar: Calendar) {
            input?.date = calendar.time
            inputDateActionView?.setItems(listOf(Pair.create(DateFormat.format(getString(R.string.observers_and_date_date_format),
                                                                               calendar.time).toString(),
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

        selectedInputObserversActionView = view.findViewById(R.id.selected_observers_action_view)
        selectedInputObserversActionView?.setListener(object : ListItemActionView.OnListItemActionViewListener {
            override fun onAction() {
                val context = context ?: return

                startActivityForResult(InputObserverListActivity.newIntent(context,
                                                                           ListView.CHOICE_MODE_MULTIPLE,
                                                                           selectedInputObservers),
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

        return view
    }

    override fun onActivityResult(
            requestCode: Int,
            resultCode: Int,
            data: Intent?) {
        if ((resultCode == Activity.RESULT_OK) && (data != null)) {
            selectedInputObservers.clear()
            selectedInputObservers.addAll(data.getParcelableArrayListExtra(InputObserverListActivity.EXTRA_SELECTED_INPUT_OBSERVERS))

            input?.also {
                it.clearAllInputObservers()

                if (selectedInputObservers.isEmpty()) {
                    val context = context ?: return
                    getDefaultObserverId(context).also { defaultObserverId -> if (defaultObserverId != null) it.setPrimaryInputObserverId(defaultObserverId) }
                }

                it.setAllInputObservers(selectedInputObservers)
            }

            updateSelectedObserversActionView(selectedInputObservers)
        }
    }

    override fun getResourceTitle(): Int {
        return R.string.pager_fragment_observers_and_date_input_title
    }

    override fun pagingEnabled(): Boolean {
        return true
    }

    override fun validate(): Boolean {
        return this.input?.getAllInputObserverIds()?.isNotEmpty() ?: false
    }

    override fun refreshView() {
        setDefaultObserverFromSettings()
        val selectedInputObserverIds = input?.getAllInputObserverIds() ?: emptySet()

        if (selectedInputObserverIds.isNotEmpty()) {
            LoaderManager.getInstance(this)
                .restartLoader(LOADER_OBSERVERS_IDS,
                               bundleOf(kotlin.Pair(KEY_SELECTED_INPUT_OBSERVER_IDS,
                                                    selectedInputObserverIds.toTypedArray().toLongArray())),
                               loaderCallbacks)
        }

        inputDateActionView?.setItems(listOf(Pair.create(DateFormat.format(getString(R.string.observers_and_date_date_format),
                                                                           input?.date
                                                                                   ?: Date()).toString(),
                                                         "")))
    }

    override fun setInput(input: AbstractInput) {
        this.input = input as Input
    }

    private fun updateSelectedObserversActionView(selectedInputObservers: List<InputObserver>) {
        selectedInputObserversActionView?.setTitle(resources.getQuantityString(R.plurals.observers_and_date_selected_observers,
                                                                               selectedInputObservers.size,
                                                                               selectedInputObservers.size))
        selectedInputObserversActionView?.setItems(selectedInputObservers.map { inputObserver ->
            Pair.create(inputObserver.lastname?.toUpperCase(Locale.getDefault()) ?: "",
                        inputObserver.firstname)
        })
    }

    private fun setDefaultObserverFromSettings() {
        input?.run {
            if (this.getAllInputObserverIds().isEmpty()) {
                val context = context ?: return
                getDefaultObserverId(context).also { defaultObserverId -> if (defaultObserverId != null) this.setPrimaryInputObserverId(defaultObserverId) }
            }
        }
    }

    companion object {

        private val TAG = ObserversAndDateInputFragment::class.java.name
        private const val DATE_PICKER_DIALOG_FRAGMENT = "date_picker_dialog_fragment"
        private const val LOADER_OBSERVERS_IDS = 1
        private const val KEY_SELECTED_INPUT_OBSERVER_IDS = "selected_input_observer_ids"

        /**
         * Use this factory method to create a new instance of [ObserversAndDateInputFragment].
         *
         * @return A new instance of [ObserversAndDateInputFragment]
         */
        @JvmStatic
        fun newInstance() = ObserversAndDateInputFragment()
    }
}