package fr.geonature.occtax.ui.input.observers

import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.text.Editable
import android.text.format.DateFormat
import android.text.format.DateFormat.is24HourFormat
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import dagger.hilt.android.AndroidEntryPoint
import fr.geonature.commons.data.ContentProviderAuthority
import fr.geonature.commons.data.GeoNatureModuleName
import fr.geonature.commons.data.entity.Dataset
import fr.geonature.commons.data.entity.DefaultNomenclature
import fr.geonature.commons.data.entity.DefaultNomenclatureWithType
import fr.geonature.commons.data.entity.InputObserver
import fr.geonature.commons.data.entity.NomenclatureType
import fr.geonature.commons.data.helper.ProviderHelper.buildUri
import fr.geonature.commons.input.AbstractInput
import fr.geonature.commons.util.afterTextChanged
import fr.geonature.commons.util.get
import fr.geonature.commons.util.set
import fr.geonature.occtax.R
import fr.geonature.occtax.input.Input
import fr.geonature.occtax.input.NomenclatureTypeViewType
import fr.geonature.occtax.input.PropertyValue
import fr.geonature.occtax.settings.InputDateSettings
import fr.geonature.occtax.ui.dataset.DatasetListActivity
import fr.geonature.occtax.ui.input.IInputFragment
import fr.geonature.occtax.ui.input.InputPagerFragmentActivity
import fr.geonature.occtax.ui.observers.InputObserverListActivity
import fr.geonature.occtax.ui.shared.view.ListItemActionView
import fr.geonature.occtax.util.SettingsUtils.getDefaultDatasetId
import fr.geonature.occtax.util.SettingsUtils.getDefaultObserversId
import fr.geonature.viewpager.ui.AbstractPagerFragmentActivity
import fr.geonature.viewpager.ui.IValidateFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.tinylog.kotlin.Logger
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Selected observer and current date as first {@code Fragment} used by [InputPagerFragmentActivity].
 *
 * @author S. Grimault
 */
@AndroidEntryPoint
class ObserversAndDateInputFragment : Fragment(),
    IValidateFragment,
    IInputFragment {

    @ContentProviderAuthority
    @Inject
    lateinit var authority: String

    @GeoNatureModuleName
    @Inject
    lateinit var moduleName: String

    private lateinit var observersResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var datasetResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var dateSettings: InputDateSettings

    private var input: Input? = null
    private val defaultInputObservers: MutableList<InputObserver> = mutableListOf()
    private val selectedInputObservers: MutableList<InputObserver> = mutableListOf()
    private var selectedDataset: Dataset? = null

    private var selectedInputObserversActionView: ListItemActionView? = null
    private var selectedDatasetActionView: ListItemActionView? = null
    private var dateStartTextInputLayout: TextInputLayout? = null
    private var dateEndTextInputLayout: TextInputLayout? = null
    private var commentTextInputLayout: TextInputLayout? = null

    private val loaderCallbacks = object : LoaderManager.LoaderCallbacks<Cursor> {
        override fun onCreateLoader(
            id: Int,
            args: Bundle?
        ): Loader<Cursor> {

            return when (id) {
                LOADER_OBSERVERS_IDS -> CursorLoader(
                    requireContext(),
                    buildUri(
                        authority,
                        InputObserver.TABLE_NAME,
                        args?.getLongArray(KEY_SELECTED_INPUT_OBSERVER_IDS)?.joinToString(",")
                            ?: ""
                    ),
                    null,
                    null,
                    null,
                    null
                )
                LOADER_DATASET_ID -> CursorLoader(
                    requireContext(),
                    buildUri(
                        authority,
                        Dataset.TABLE_NAME,
                        args?.getString(Dataset.COLUMN_MODULE) ?: "",
                        args?.getLong(
                            Dataset.COLUMN_ID,
                            -1
                        ).toString()
                    ),
                    null,
                    null,
                    null,
                    null
                )
                LOADER_DEFAULT_NOMENCLATURE_VALUES -> CursorLoader(
                    requireContext(),
                    buildUri(
                        authority,
                        NomenclatureType.TABLE_NAME,
                        args?.getString(Dataset.COLUMN_MODULE) ?: "",
                        "default"
                    ),
                    null,
                    null,
                    null,
                    null
                )
                else -> throw IllegalArgumentException()
            }
        }

        override fun onLoadFinished(
            loader: Loader<Cursor>,
            data: Cursor?
        ) {
            if (data == null) {
                Logger.warn { "failed to load data from '${(loader as CursorLoader).uri}'" }

                return
            }

            when (loader.id) {
                LOADER_OBSERVERS_IDS -> {
                    val inputObserversLoaded = data.let { cursor ->
                        mutableListOf<InputObserver>().let {
                            if (cursor.moveToFirst()) {
                                while (!cursor.isAfterLast) {
                                    InputObserver.fromCursor(cursor)?.run {
                                        it.add(this)
                                    }

                                    cursor.moveToNext()
                                }
                            }
                            it
                        }
                    }

                    val defaultObserversId =
                        context?.let { getDefaultObserversId(it) } ?: emptyList()

                    if (defaultInputObservers.isEmpty() && defaultObserversId.isNotEmpty()) {
                        defaultInputObservers.addAll(inputObserversLoaded.filter { inputObserver -> defaultObserversId.any { it == inputObserver.id } })
                    }

                    selectedInputObservers.apply {
                        clear()
                        addAll(inputObserversLoaded)
                    }

                    updateSelectedObservers(inputObserversLoaded)
                }
                LOADER_DATASET_ID -> {
                    if (data.count == 0) {
                        selectedDataset = null
                        input?.datasetId = null
                        (activity as AbstractPagerFragmentActivity?)?.validateCurrentPage()
                    }

                    if (data.moveToFirst()) {
                        selectedDataset = Dataset.fromCursor(data)
                    }

                    updateSelectedDatasetActionView(selectedDataset)
                }
                LOADER_DEFAULT_NOMENCLATURE_VALUES -> {
                    data.moveToFirst()

                    val defaultMnemonicFilter = Input.defaultPropertiesMnemonic.asSequence()
                        .filter { it.second == NomenclatureTypeViewType.NOMENCLATURE_TYPE }
                        .map { it.first }
                        .toList()

                    while (!data.isAfterLast) {
                        val defaultNomenclatureValue = DefaultNomenclatureWithType.fromCursor(data)
                        val mnemonic =
                            defaultNomenclatureValue?.nomenclatureWithType?.type?.mnemonic

                        if (defaultNomenclatureValue != null && !mnemonic.isNullOrBlank() && defaultMnemonicFilter.contains(
                                mnemonic
                            )
                        ) {
                            input?.properties?.set(
                                mnemonic,
                                PropertyValue.fromNomenclature(
                                    mnemonic,
                                    defaultNomenclatureValue.nomenclatureWithType
                                )
                            )
                        }

                        data.moveToNext()
                    }

                    (activity as AbstractPagerFragmentActivity?)?.validateCurrentPage()

                    if (input?.properties?.isNotEmpty() == false) {
                        val context = context ?: return

                        Toast.makeText(
                            context,
                            R.string.toast_input_default_properties_loading_failed,
                            Toast.LENGTH_LONG
                        )
                            .show()
                    }
                }
            }

            LoaderManager.getInstance(this@ObserversAndDateInputFragment)
                .destroyLoader(loader.id)
        }

        override fun onLoaderReset(loader: Loader<Cursor>) {
            when (loader.id) {
                LOADER_OBSERVERS_IDS -> selectedInputObservers.clear()
                LOADER_DATASET_ID -> selectedDataset = null
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        observersResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if ((it.resultCode != Activity.RESULT_OK) || (it.data == null)) {
                    return@registerForActivityResult
                }

                updateSelectedObservers(
                    it.data?.getParcelableArrayListExtra(
                        InputObserverListActivity.EXTRA_SELECTED_INPUT_OBSERVERS
                    ) ?: ArrayList()
                )
            }
        datasetResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if ((it.resultCode != Activity.RESULT_OK) || (it.data == null)) {
                    return@registerForActivityResult
                }

                updateSelectedDataset(
                    it.data?.getParcelableExtra(
                        DatasetListActivity.EXTRA_SELECTED_DATASET
                    )
                )
            }
        dateSettings = arguments?.getParcelable(ARG_DATE_SETTINGS) ?: InputDateSettings.DEFAULT
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(
            R.layout.fragment_observers_and_date_input,
            container,
            false
        )

        selectedInputObserversActionView =
            view.findViewById<ListItemActionView?>(R.id.selected_observers_action_view)?.apply {
                setListener(object : ListItemActionView.OnListItemActionViewListener {
                    override fun onAction() {
                        val context = context ?: return

                        observersResultLauncher.launch(
                            InputObserverListActivity.newIntent(
                                context,
                                ListView.CHOICE_MODE_MULTIPLE,
                                selectedInputObservers
                            )
                        )
                    }
                })
            }

        selectedDatasetActionView =
            view.findViewById<ListItemActionView?>(R.id.selected_dataset_action_view)?.apply {
                setListener(object : ListItemActionView.OnListItemActionViewListener {
                    override fun onAction() {
                        val context = context ?: return

                        datasetResultLauncher.launch(
                            DatasetListActivity.newIntent(
                                context,
                                selectedDataset
                            )
                        )
                    }
                })
            }

        dateStartTextInputLayout = view.findViewById<TextInputLayout?>(R.id.dateStart)?.apply {
            hint = getString(
                if (dateSettings.endDateSettings == null) R.string.observers_and_date_date_hint
                else R.string.observers_and_date_date_start_hint
            )
            editText?.afterTextChanged {
                error = checkStartDateConstraints()
                dateEndTextInputLayout?.error = checkEndDateConstraints()
            }
            editText?.setOnClickListener {
                CoroutineScope(Dispatchers.Main).launch {
                    val startDate = selectDateTime(
                        CalendarConstraints
                            .Builder()
                            .setValidator(DateValidatorPointBackward.now())
                            .build(),
                        dateSettings.startDateSettings == InputDateSettings.DateSettings.DATETIME,
                        input?.startDate ?: Date()
                    )

                    input?.startDate = startDate

                    if (dateSettings.endDateSettings == null) {
                        input?.endDate = startDate
                    }

                    dateStartTextInputLayout?.editText?.apply {
                        updateDateEditText(
                            this,
                            dateSettings.startDateSettings ?: InputDateSettings.DateSettings.DATE,
                            startDate
                        )
                    }
                    dateEndTextInputLayout?.editText?.apply {
                        updateDateEditText(
                            this,
                            dateSettings.endDateSettings ?: InputDateSettings.DateSettings.DATE,
                            input?.endDate
                        )
                    }

                    (activity as AbstractPagerFragmentActivity?)?.validateCurrentPage()
                }
            }
        }

        dateEndTextInputLayout = view.findViewById<TextInputLayout?>(R.id.dateEnd)?.apply {
            visibility = if (dateSettings.endDateSettings == null) View.GONE else View.VISIBLE
            editText?.afterTextChanged {
                error = checkEndDateConstraints()
                dateStartTextInputLayout?.error = checkStartDateConstraints()
            }
            editText?.setOnClickListener {
                CoroutineScope(Dispatchers.Main).launch {
                    val endDate = selectDateTime(
                        CalendarConstraints
                            .Builder()
                            .setValidator(
                                DateValidatorPointForward.from(
                                    (input?.startDate ?: Date())
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
                        input?.endDate ?: input?.startDate ?: Date()
                    )

                    input?.endDate = endDate
                    dateStartTextInputLayout?.editText?.apply {
                        updateDateEditText(
                            this,
                            dateSettings.startDateSettings ?: InputDateSettings.DateSettings.DATE,
                            input?.startDate
                        )
                    }
                    dateEndTextInputLayout?.editText?.apply {
                        updateDateEditText(
                            this,
                            dateSettings.endDateSettings ?: InputDateSettings.DateSettings.DATE,
                            endDate
                        )
                    }

                    (activity as AbstractPagerFragmentActivity?)?.validateCurrentPage()
                }
            }
        }

        commentTextInputLayout = view.findViewById<TextInputLayout?>(android.R.id.edit)?.apply {
            editText?.afterTextChanged {
                input?.comment = it?.toString()?.ifEmpty { null }?.ifBlank { null }
            }
        }

        return view
    }

    override fun getResourceTitle(): Int {
        return R.string.pager_fragment_observers_and_date_input_title
    }

    override fun getSubtitle(): CharSequence? {
        return null
    }

    override fun pagingEnabled(): Boolean {
        return true
    }

    override fun validate(): Boolean {
        return this.input?.getAllInputObserverIds()
            ?.isNotEmpty() ?: false &&
            this.input?.datasetId != null &&
            this.input?.properties?.isNotEmpty() == true &&
            checkStartDateConstraints() == null &&
            checkEndDateConstraints() == null
    }

    override fun refreshView() {
        setDefaultDatasetFromSettings()

        val selectedInputObserverIds =
            input?.getAllInputObserverIds() ?: context?.let { getDefaultObserversId(it) }
            ?: emptyList()

        if (selectedInputObserverIds.isNotEmpty()) {
            LoaderManager.getInstance(this)
                .initLoader(
                    LOADER_OBSERVERS_IDS,
                    bundleOf(
                        kotlin.Pair(
                            KEY_SELECTED_INPUT_OBSERVER_IDS,
                            selectedInputObserverIds.toTypedArray().toLongArray()
                        )
                    ),
                    loaderCallbacks
                )
        } else {
            updateSelectedObserversActionView(emptyList())
        }

        val selectedDatasetId = input?.datasetId

        if (selectedDatasetId != null) {
            LoaderManager.getInstance(this)
                .initLoader(
                    LOADER_DATASET_ID,
                    bundleOf(
                        kotlin.Pair(
                            Dataset.COLUMN_MODULE,
                            moduleName
                        ),
                        kotlin.Pair(
                            Dataset.COLUMN_ID,
                            selectedDatasetId
                        )
                    ),
                    loaderCallbacks
                )
        } else {
            updateSelectedDatasetActionView(null)
        }

        LoaderManager.getInstance(this)
            .initLoader(
                LOADER_DEFAULT_NOMENCLATURE_VALUES,
                bundleOf(
                    kotlin.Pair(
                        DefaultNomenclature.COLUMN_MODULE,
                        moduleName
                    )
                ),
                loaderCallbacks
            )

        dateStartTextInputLayout?.editText?.apply {
            updateDateEditText(
                this,
                dateSettings.startDateSettings ?: InputDateSettings.DateSettings.DATE,
                input?.startDate ?: Date()
            )
        }
        dateEndTextInputLayout?.editText?.apply {
            updateDateEditText(
                this,
                dateSettings.endDateSettings ?: InputDateSettings.DateSettings.DATE,
                input?.endDate
            )
        }
        commentTextInputLayout?.hint =
            getString(
                if (input?.comment.isNullOrBlank()) R.string.observers_and_date_comment_add_hint
                else R.string.observers_and_date_comment_edit_hint
            )
        commentTextInputLayout?.editText?.apply {
            text = input?.comment?.let { Editable.Factory.getInstance().newEditable(it) }
        }
    }

    override fun setInput(input: AbstractInput) {
        this.input = input as Input
    }

    private fun updateSelectedObservers(selectedInputObservers: List<InputObserver>) {
        this.selectedInputObservers.clear()
        this.selectedInputObservers.addAll(selectedInputObservers)

        input?.also {
            it.clearAllInputObservers()
            it.setAllInputObservers(selectedInputObservers.ifEmpty { this.defaultInputObservers })
        }

        updateSelectedObserversActionView(selectedInputObservers)

        (activity as AbstractPagerFragmentActivity?)?.validateCurrentPage()
    }

    private fun updateSelectedDataset(selectedDataset: Dataset?) {
        this.selectedDataset = selectedDataset

        input?.also {
            it.datasetId = selectedDataset?.id
        }

        (activity as AbstractPagerFragmentActivity?)?.validateCurrentPage()

        updateSelectedDatasetActionView(selectedDataset)
    }

    private fun updateSelectedObserversActionView(selectedInputObservers: List<InputObserver>) {
        selectedInputObserversActionView?.setTitle(
            resources.getQuantityString(
                R.plurals.observers_and_date_selected_observers,
                selectedInputObservers.size,
                selectedInputObservers.size
            )
        )
        selectedInputObserversActionView?.setItems(selectedInputObservers.map { inputObserver ->
            Pair.create(
                inputObserver.lastname?.uppercase(Locale.getDefault()) ?: "",
                inputObserver.firstname
            )
        })
    }

    private fun updateSelectedDatasetActionView(selectedDataset: Dataset?) {
        selectedDatasetActionView?.setItems(
            if (selectedDataset == null) emptyList()
            else listOf(
                Pair.create(
                    selectedDataset.name,
                    selectedDataset.description
                )
            )
        )
    }

    private fun setDefaultDatasetFromSettings() {
        input?.run {
            if (datasetId == null) {
                val context = context ?: return
                getDefaultDatasetId(context).also { defaultDatasetId ->
                    if (defaultDatasetId != null) this.datasetId = defaultDatasetId
                }
            }
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
        suspendCoroutine { continuation ->
            val supportFragmentManager =
                activity?.supportFragmentManager

            if (supportFragmentManager == null) {
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
                            .setTimeFormat(if (is24HourFormat(context)) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H)
                            .setHour(selectedDate.get(if (is24HourFormat(context)) Calendar.HOUR_OF_DAY else Calendar.HOUR))
                            .setMinute(selectedDate.get(Calendar.MINUTE))
                            .build()
                    ) {
                        addOnPositiveButtonClickListener {
                            continuation.resume(
                                selectedDate.set(
                                    if (is24HourFormat(context)) Calendar.HOUR_OF_DAY else Calendar.HOUR,
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
                            supportFragmentManager,
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
                    supportFragmentManager,
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
                        getString(
                            if (dateSettings == InputDateSettings.DateSettings.DATETIME) R.string.observers_and_date_datetime_format
                            else R.string.observers_and_date_date_format
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
        if (input == null) {
            return null
        }

        val startDate = input?.startDate
            ?: return getString(R.string.observers_and_date_error_date_start_not_set)

        if (startDate.after(Date())) {
            return getString(R.string.observers_and_date_error_date_start_after_now)
        }

        return null
    }

    /**
     * Checks end date constraints from current [AbstractInput].
     *
     * @return `null` if all constraints are valid, or an error message
     */
    private fun checkEndDateConstraints(): CharSequence? {
        if (input == null) {
            return null
        }

        val endDate = input?.endDate

        if (dateSettings.endDateSettings == null) {
            return null
        }

        if (endDate == null) {
            return getString(R.string.observers_and_date_error_date_end_not_set)
        }

        if ((input?.startDate ?: Date()).after(endDate)) {
            return getString(R.string.observers_and_date_error_date_end_before_start_date)
        }

        return null
    }

    companion object {

        private const val ARG_DATE_SETTINGS = "arg_date_settings"

        private const val DATE_PICKER_DIALOG_FRAGMENT = "date_picker_dialog_fragment"
        private const val TIME_PICKER_DIALOG_FRAGMENT = "time_picker_dialog_fragment"
        private const val LOADER_OBSERVERS_IDS = 1
        private const val LOADER_DATASET_ID = 2
        private const val LOADER_DEFAULT_NOMENCLATURE_VALUES = 3
        private const val KEY_SELECTED_INPUT_OBSERVER_IDS = "selected_input_observer_ids"

        /**
         * Use this factory method to create a new instance of [ObserversAndDateInputFragment].
         *
         * @return A new instance of [ObserversAndDateInputFragment]
         */
        @JvmStatic
        fun newInstance(dateSettings: InputDateSettings) = ObserversAndDateInputFragment().apply {
            arguments = Bundle().apply {
                putParcelable(
                    ARG_DATE_SETTINGS,
                    dateSettings
                )
            }
        }
    }
}
