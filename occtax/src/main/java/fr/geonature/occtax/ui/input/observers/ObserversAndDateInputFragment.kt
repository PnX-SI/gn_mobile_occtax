package fr.geonature.occtax.ui.input.observers

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import fr.geonature.commons.data.ContentProviderAuthority
import fr.geonature.commons.data.entity.Dataset
import fr.geonature.commons.data.entity.InputObserver
import fr.geonature.commons.data.entity.Nomenclature
import fr.geonature.commons.lifecycle.observe
import fr.geonature.compat.content.getParcelableArrayExtraCompat
import fr.geonature.compat.content.getParcelableExtraCompat
import fr.geonature.compat.os.getParcelableCompat
import fr.geonature.occtax.R
import fr.geonature.occtax.features.nomenclature.domain.FormField
import fr.geonature.occtax.features.nomenclature.presentation.NomenclatureViewModel
import fr.geonature.occtax.features.nomenclature.presentation.adapter.FormFieldAdapter
import fr.geonature.occtax.features.record.domain.DatasetRecord
import fr.geonature.occtax.features.record.domain.MediaRecord
import fr.geonature.occtax.features.record.domain.ObservationRecord
import fr.geonature.occtax.features.record.domain.ObserversRecord
import fr.geonature.occtax.features.record.domain.PropertyValue
import fr.geonature.occtax.features.settings.domain.InputDateSettings
import fr.geonature.occtax.ui.dataset.DatasetListActivity
import fr.geonature.occtax.ui.input.AbstractInputFragment
import fr.geonature.occtax.ui.observers.InputObserverListActivity
import org.tinylog.kotlin.Logger
import java.util.Locale
import javax.inject.Inject

/**
 * [Fragment] to let the user to select observers and dataset to the given [ObservationRecord].
 *
 * @author S. Grimault
 */
@AndroidEntryPoint
class ObserversAndDateInputFragment : AbstractInputFragment() {

    @ContentProviderAuthority
    @Inject
    lateinit var authority: String

    private val nomenclatureViewModel: NomenclatureViewModel by viewModels()

    private lateinit var observersResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var datasetResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var dateSettings: InputDateSettings

    private var adapter: FormFieldAdapter? = null

    private var activeFormField: FormField? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        observersResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if ((it.resultCode != Activity.RESULT_OK) || (it.data == null)) {
                    return@registerForActivityResult
                }

                updateSelectedObservers(
                    it.data?.getParcelableArrayExtraCompat<InputObserver>(
                        InputObserverListActivity.EXTRA_SELECTED_INPUT_OBSERVERS
                    )
                        ?.toList() ?: emptyList()
                )
            }
        datasetResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if ((it.resultCode != Activity.RESULT_OK) || (it.data == null)) {
                    return@registerForActivityResult
                }

                updateSelectedDataset(
                    it.data?.getParcelableExtraCompat(DatasetListActivity.EXTRA_SELECTED_DATASET)
                )
            }
        dateSettings =
            arguments?.getParcelableCompat(ARG_DATE_SETTINGS) ?: InputDateSettings.DEFAULT

        with(nomenclatureViewModel) {
            observe(
                editableNomenclatures,
                ::handleEditableFields
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(
            R.layout.fragment_recycler_view_loader,
            container,
            false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(
            view,
            savedInstanceState
        )

        val recyclerView = view.findViewById<RecyclerView>(android.R.id.list)
        val emptyTextView = view.findViewById<TextView>(android.R.id.empty)
            .apply {
                text = getString(R.string.information_no_data)
            }
        val progressBar = view.findViewById<ProgressBar>(android.R.id.progress)
            .apply { visibility = View.VISIBLE }

        adapter = FormFieldAdapter(object :
            FormFieldAdapter.OnEditableFieldAdapter {
            override fun getContext(): Context {
                return requireContext()
            }

            override fun getLifecycleOwner(): LifecycleOwner {
                return this@ObserversAndDateInputFragment
            }

            override fun getCoordinatorLayout(): CoordinatorLayout? {
                return null
            }

            override fun fragmentManager(): FragmentManager? {
                return activity?.supportFragmentManager
            }

            override fun showEmptyTextView(show: Boolean) {
                progressBar?.visibility = View.GONE

                if (emptyTextView?.visibility == View.VISIBLE == show) {
                    return
                }

                if (show) {
                    emptyTextView?.startAnimation(
                        AnimationUtils.loadAnimation(
                            context,
                            android.R.anim.fade_in
                        )
                    )
                    emptyTextView?.visibility = View.VISIBLE
                } else {
                    emptyTextView?.startAnimation(
                        AnimationUtils.loadAnimation(
                            context,
                            android.R.anim.fade_out
                        )
                    )
                    emptyTextView?.visibility = View.GONE
                }
            }

            override fun showMore() {
                // nothing to do...
            }

            override fun getNomenclatureValues(nomenclatureTypeMnemonic: String): LiveData<List<Nomenclature>> {
                return nomenclatureViewModel.getNomenclatureValuesByTypeAndTaxonomy(nomenclatureTypeMnemonic)
            }

            override fun onUpdate(editableField: FormField.Editable) {
                listener.validateCurrentPage()

                if (editableField.additionalField) {
                    // as additional field
                    observationRecord?.also {
                        it.additionalFields = it.additionalFields.filter { pv ->
                            pv.toPair().first != editableField.getValue().code
                        } + listOfNotNull(editableField.getValue())
                    }
                } else {
                    // as editable field
                    editableField.getValue()
                        .toPair()
                        .also {
                            if (it.second.isEmpty()) observationRecord?.properties?.remove(editableField.getValue().code)
                            else observationRecord?.properties?.set(
                                editableField.getValue().code,
                                it.second
                            )
                        }
                }
            }

            override fun onAction(formField: FormField) {
                when (formField) {
                    is FormField.Editable -> {
                        when (formField.getValue().code) {
                            ObserversRecord.OBSERVERS_KEY -> {
                                context?.also {
                                    activeFormField = formField
                                    observersResultLauncher.launch(
                                        InputObserverListActivity.newIntent(
                                            it,
                                            ListView.CHOICE_MODE_MULTIPLE,
                                            observationRecord?.observers?.observers?.value?.toList()
                                                ?: emptyList()
                                        )
                                    )
                                }
                            }

                            DatasetRecord.DATASET_ID_KEY -> {
                                context?.also {
                                    activeFormField = formField
                                    datasetResultLauncher.launch(
                                        DatasetListActivity.newIntent(
                                            it,
                                            observationRecord?.dataset?.dataset?.value
                                        )
                                    )
                                }
                            }
                        }
                    }

                    else -> {}
                }
            }

            override fun onAddMedia(nomenclatureTypeMnemonic: String) {
                // nothing to do…
            }

            override fun onMediaSelected(mediaRecord: MediaRecord.File) {
                // nothing to do…
            }
        })

        with(recyclerView) {
            layoutManager = LinearLayoutManager(context)
            adapter = this@ObserversAndDateInputFragment.adapter
        }
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
        return (adapter?.hasErrors() == false)
    }

    override fun refreshView() {
        if (adapter?.itemCount == 0) {
            loadEditableFields()
        }
    }

    private fun loadEditableFields() {
        nomenclatureViewModel.getEditableFields(
            datasetId = observationRecord?.dataset?.dataset?.value?.id,
            withAdditionalFields = arguments?.getBoolean(
                ARG_WITH_ADDITIONAL_FIELDS,
                false
            ) ?: false,
            type = FormField.Type.DEFAULT,
            dateSettings = dateSettings
        )
    }

    private fun handleEditableFields(editableFields: List<FormField>) {
        mapDefaultValueToObservationRecord(editableFields)

        adapter?.bind(
            editableFields.map {
                when (it) {
                    is FormField.Modal -> {
                        when (it.value.code) {
                            DatasetRecord.DATASET_ID_KEY -> {
                                val selectedDataset =
                                    observationRecord?.properties?.get(it.value.code)
                                        ?.takeIf { pv -> pv is PropertyValue.Dataset }
                                        ?.let { pv -> pv as PropertyValue.Dataset }?.value

                                it.also {
                                    it.value =
                                        it.value.copy(value = selectedDataset?.id)
                                    it.item =
                                        selectedDataset?.let { dataset -> dataset.name to dataset.description }
                                }
                            }

                            else -> it
                        }
                    }

                    is FormField.ModalMultiple -> {
                        when (it.value.code) {
                            ObserversRecord.OBSERVERS_KEY -> {
                                val selectedObservers =
                                    observationRecord?.properties?.get(it.value.code)
                                        ?.takeIf { pv -> pv is PropertyValue.Observers }
                                        ?.let { pv -> pv as PropertyValue.Observers }?.value?.toList()
                                        ?: emptyList()

                                it.copy(
                                    label = resources.getQuantityString(
                                        R.plurals.observers_and_date_selected_observers,
                                        selectedObservers.size,
                                        selectedObservers.size
                                    )
                                )
                                    .also { ff ->
                                        ff.value =
                                            it.value.copy(value = selectedObservers.map { inputObserver -> inputObserver.id }
                                                .toTypedArray())
                                        ff.items = selectedObservers.map { inputObserver ->
                                            (inputObserver.lastname?.uppercase(Locale.getDefault())
                                                ?: "") to inputObserver.firstname
                                        }
                                    }
                            }

                            else -> it
                        }
                    }

                    else -> it
                }
            },
            *((observationRecord?.properties?.values
                ?.filterNotNull()
                ?.filterNot { it.isEmpty() }
                ?.filterNot { it is PropertyValue.AdditionalFields }
                ?: emptyList()) + (observationRecord?.additionalFields
                ?: emptyList())).toTypedArray()
        )

        listener.validateCurrentPage()
    }

    private fun mapDefaultValueToObservationRecord(editableFields: List<FormField>) {
        // map editable form fields existing values to the given observation record
        editableFields
            .filterIsInstance<FormField.Editable>()
            .forEach { ff ->
                // if we have existing value from observation record, do nothing
                if (!ff.additionalField && observationRecord?.properties?.containsKey(ff.getValue().code) == true) return
                if (ff.additionalField && observationRecord?.additionalFields?.associateBy { pv -> pv.code }
                        ?.containsKey(ff.getValue().code) == true) return

                // set default value from editable field to the given observation record
                if (!ff.additionalField) {
                    observationRecord?.properties?.set(
                        ff.getValue().code,
                        ff.getValue()
                    )
                }

                if (ff.additionalField) {
                    observationRecord?.also { record ->
                        record.additionalFields = record.additionalFields.filter { pv ->
                            pv.toPair().first != ff.getValue().code
                        } + listOfNotNull(ff.getValue())
                    }
                }
            }
    }

    private fun updateSelectedObservers(selectedInputObservers: List<InputObserver>) {
        observationRecord?.observers?.setObservers(selectedInputObservers)

        activeFormField?.takeIf { it is FormField.ModalMultiple }
            ?.let { it as FormField.ModalMultiple }
            ?.copy(
                label = resources.getQuantityString(
                    R.plurals.observers_and_date_selected_observers,
                    selectedInputObservers.size,
                    selectedInputObservers.size
                )
            )
            ?.also {
                it.value =
                    it.value.copy(value = selectedInputObservers.map { inputObserver -> inputObserver.id }
                        .toTypedArray())
                it.items = selectedInputObservers.map { inputObserver ->
                    (inputObserver.lastname?.uppercase(Locale.getDefault())
                        ?: "") to inputObserver.firstname
                }
            }
            ?.also {
                adapter?.updateEditableField(it)
            }

        listener.validateCurrentPage()
    }

    private fun updateSelectedDataset(selectedDataset: Dataset?) {
        selectedDataset?.also {
            Logger.info { "selected dataset: ${it.id}, taxa list ID: ${it.taxaListId}" }
        }

        observationRecord?.dataset?.setDataset(selectedDataset)

        activeFormField?.takeIf { it is FormField.Modal }
            ?.let { it as FormField.Modal }
            ?.copy()
            ?.also {
                it.value =
                    it.value.copy(value = selectedDataset?.id)
                it.item = selectedDataset?.let { dataset -> dataset.name to dataset.description }
            }
            ?.also {
                adapter?.updateEditableField(it)
            }

        loadEditableFields()
        listener.validateCurrentPage()
    }

    companion object {

        private const val ARG_DATE_SETTINGS = "arg_date_settings"
        private const val ARG_WITH_ADDITIONAL_FIELDS = "arg_with_additional_fields"

        /**
         * Use this factory method to create a new instance of [ObserversAndDateInputFragment].
         *
         * @return A new instance of [ObserversAndDateInputFragment]
         */
        @JvmStatic
        fun newInstance(
            dateSettings: InputDateSettings,
            withAdditionalFields: Boolean = false
        ) =
            ObserversAndDateInputFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(
                        ARG_DATE_SETTINGS,
                        dateSettings
                    )
                    putBoolean(
                        ARG_WITH_ADDITIONAL_FIELDS,
                        withAdditionalFields
                    )
                }
            }
    }
}
