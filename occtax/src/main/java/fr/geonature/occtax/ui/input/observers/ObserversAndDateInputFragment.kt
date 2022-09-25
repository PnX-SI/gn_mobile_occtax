package fr.geonature.occtax.ui.input.observers

import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.text.Editable
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import fr.geonature.commons.data.ContentProviderAuthority
import fr.geonature.commons.data.GeoNatureModuleName
import fr.geonature.commons.data.entity.Dataset
import fr.geonature.commons.data.entity.DefaultNomenclature
import fr.geonature.commons.data.entity.DefaultNomenclatureWithType
import fr.geonature.commons.data.entity.InputObserver
import fr.geonature.commons.data.entity.NomenclatureType
import fr.geonature.commons.data.helper.ProviderHelper.buildUri
import fr.geonature.commons.util.afterTextChanged
import fr.geonature.occtax.R
import fr.geonature.occtax.features.nomenclature.presentation.PropertyValueModel
import fr.geonature.occtax.input.Input
import fr.geonature.occtax.input.NomenclatureTypeViewType
import fr.geonature.occtax.input.PropertyValue
import fr.geonature.occtax.settings.InputDateSettings
import fr.geonature.occtax.ui.dataset.DatasetListActivity
import fr.geonature.occtax.ui.input.AbstractInputFragment
import fr.geonature.occtax.ui.input.InputPagerFragmentActivity
import fr.geonature.occtax.ui.observers.InputObserverListActivity
import fr.geonature.occtax.ui.shared.view.ActionView
import fr.geonature.occtax.ui.shared.view.InputDateView
import fr.geonature.occtax.ui.shared.view.ListItemActionView
import fr.geonature.occtax.util.SettingsUtils.getDefaultDatasetId
import fr.geonature.occtax.util.SettingsUtils.getDefaultObserversId
import org.tinylog.kotlin.Logger
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * Selected observer and current date as first page used by [InputPagerFragmentActivity].
 *
 * @author S. Grimault
 */
@AndroidEntryPoint
class ObserversAndDateInputFragment : AbstractInputFragment() {

    @ContentProviderAuthority
    @Inject
    lateinit var authority: String

    @GeoNatureModuleName
    @Inject
    lateinit var moduleName: String

    private val propertyValueModel: PropertyValueModel by viewModels()

    private lateinit var observersResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var datasetResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var dateSettings: InputDateSettings

    private val defaultInputObservers: MutableList<InputObserver> = mutableListOf()
    private val selectedInputObservers: MutableList<InputObserver> = mutableListOf()
    private var selectedDataset: Dataset? = null

    private var selectedInputObserversActionView: ListItemActionView? = null
    private var selectedDatasetActionView: ActionView? = null
    private var inputDateView: InputDateView? = null
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
                        listener.validateCurrentPage()
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

                    listener.validateCurrentPage()

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
            view.findViewById<ActionView?>(R.id.selected_dataset_action_view)?.apply {
                setListener(object : ActionView.OnActionViewListener {
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

        inputDateView = view.findViewById<InputDateView>(R.id.input_date)?.apply {
            setInputDateSettings(dateSettings)
            setListener(object : InputDateView.OnInputDateViewListener {
                override fun fragmentManager(): FragmentManager? {
                    return activity?.supportFragmentManager
                }

                override fun onDatesChanged(startDate: Date, endDate: Date) {
                    input?.startDate = startDate
                    input?.endDate = endDate

                    listener.validateCurrentPage()
                }

                override fun hasError(message: CharSequence) {
                    listener.validateCurrentPage()
                }
            })
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
            inputDateView?.hasErrors() == false
    }

    override fun refreshView() {
        // clear any existing local property default values
        if (arguments?.getBoolean(ARG_SAVE_DEFAULT_VALUES) == false) {
            propertyValueModel.clearAllPropertyValues()
        }

        setDefaultDatasetFromSettings()

        input?.getAllInputObserverIds()?.also { selectedInputObserverIdsFromInput ->
            val selectedInputObserverIds = selectedInputObserverIdsFromInput.ifEmpty {
                context?.let { getDefaultObserversId(it) } ?: emptyList()
            }

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

        inputDateView?.setDates(
            startDate = input?.startDate ?: Date(),
            endDate = input?.endDate
        )

        commentTextInputLayout?.apply {
            hint =
                getString(
                    if (input?.comment.isNullOrBlank()) R.string.input_comment_add_hint
                    else R.string.input_comment_edit_hint
                )
            editText?.apply {
                text = input?.comment?.let { Editable.Factory.getInstance().newEditable(it) }
            }
        }
    }

    private fun updateSelectedObservers(selectedInputObservers: List<InputObserver>) {
        this.selectedInputObservers.clear()
        this.selectedInputObservers.addAll(selectedInputObservers)

        input?.also {
            it.clearAllInputObservers()
            it.setAllInputObservers(selectedInputObservers.ifEmpty { this.defaultInputObservers })
        }

        updateSelectedObserversActionView(selectedInputObservers)

        listener.validateCurrentPage()
    }

    private fun updateSelectedDataset(selectedDataset: Dataset?) {
        this.selectedDataset = selectedDataset

        input?.also {
            it.datasetId = selectedDataset?.id
        }

        listener.validateCurrentPage()

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
        selectedDatasetActionView?.getContentView()?.also { contentView ->
            contentView.isSelected = true
            contentView.findViewById<TextView>(R.id.dataset_name)?.text = selectedDataset?.name
            contentView.findViewById<TextView>(R.id.dataset_description)?.text =
                selectedDataset?.description
        }
        selectedDatasetActionView?.setContentViewVisibility(if (selectedDataset == null) View.GONE else View.VISIBLE)
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

    companion object {

        private const val ARG_DATE_SETTINGS = "arg_date_settings"
        private const val ARG_SAVE_DEFAULT_VALUES = "arg_save_default_values"

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
        fun newInstance(dateSettings: InputDateSettings, saveDefaultValues: Boolean = false) =
            ObserversAndDateInputFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(
                        ARG_DATE_SETTINGS,
                        dateSettings
                    )
                    putBoolean(
                        ARG_SAVE_DEFAULT_VALUES,
                        saveDefaultValues
                    )
                }
            }
    }
}
