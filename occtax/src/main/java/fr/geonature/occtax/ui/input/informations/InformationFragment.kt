package fr.geonature.occtax.ui.input.informations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ProgressBar
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import fr.geonature.commons.data.entity.Nomenclature
import fr.geonature.commons.data.entity.Taxonomy
import fr.geonature.commons.lifecycle.observe
import fr.geonature.compat.os.getParcelableArrayCompat
import fr.geonature.occtax.R
import fr.geonature.occtax.features.nomenclature.domain.EditableField
import fr.geonature.occtax.features.nomenclature.presentation.EditableFieldAdapter
import fr.geonature.occtax.features.nomenclature.presentation.NomenclatureViewModel
import fr.geonature.occtax.features.nomenclature.presentation.PropertyValueModel
import fr.geonature.occtax.features.record.domain.MediaRecord
import fr.geonature.occtax.features.record.domain.PropertyValue
import fr.geonature.occtax.features.record.domain.TaxonRecord
import fr.geonature.occtax.features.settings.domain.PropertySettings
import fr.geonature.occtax.ui.input.AbstractInputFragment

/**
 * [Fragment] to let the user to add additional information for the given [TaxonRecord].
 *
 * @author S. Grimault
 */
@AndroidEntryPoint
class InformationFragment : AbstractInputFragment() {

    private val nomenclatureViewModel: NomenclatureViewModel by viewModels()
    private val propertyValueModel: PropertyValueModel by viewModels()

    private lateinit var savedState: Bundle

    private var adapter: EditableFieldAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        savedState = savedInstanceState ?: Bundle()

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
    ): View {
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

        adapter = EditableFieldAdapter(object :
            EditableFieldAdapter.OnEditableFieldAdapter {
            override fun getLifecycleOwner(): LifecycleOwner {
                return this@InformationFragment
            }

            override fun getCoordinatorLayout(): CoordinatorLayout? {
                return null
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
                savedState.putBoolean(
                    KEY_SHOW_ALL_NOMENCLATURE_TYPES,
                    true
                )
            }

            override fun getNomenclatureValues(nomenclatureTypeMnemonic: String): LiveData<List<Nomenclature>> {
                return nomenclatureViewModel.getNomenclatureValuesByTypeAndTaxonomy(
                    nomenclatureTypeMnemonic,
                    observationRecord?.taxa?.selectedTaxonRecord?.taxon?.taxonomy
                        ?: Taxonomy(
                            Taxonomy.ANY,
                            Taxonomy.ANY
                        )
                )
            }

            override fun onUpdate(editableField: EditableField) {
                if (editableField.additionalField) {
                    // as additional field
                    observationRecord?.taxa?.selectedTaxonRecord?.also {
                        it.additionalFields = it.additionalFields.filter { pv ->
                            pv.toPair().first != editableField.code
                        } + listOfNotNull(editableField.value)
                    }
                } else {
                    // as editable field
                    editableField.value?.toPair()
                        .also {
                            if (it == null) observationRecord?.taxa?.selectedTaxonRecord?.properties?.remove(editableField.code)
                            else observationRecord?.taxa?.selectedTaxonRecord?.properties?.set(
                                editableField.code,
                                it.second
                            )
                        }
                }

                val propertyValue = editableField.value

                if (propertyValue !== null && editableField.locked) propertyValueModel.setPropertyValue(
                    observationRecord?.taxa?.selectedTaxonRecord?.taxon?.taxonomy
                        ?: Taxonomy(
                            Taxonomy.ANY,
                            Taxonomy.ANY
                        ),
                    propertyValue
                ) else propertyValueModel.clearPropertyValue(
                    observationRecord?.taxa?.selectedTaxonRecord?.taxon?.taxonomy
                        ?: Taxonomy(
                            Taxonomy.ANY,
                            Taxonomy.ANY
                        ),
                    editableField.code
                )
            }

            override fun onAddMedia(nomenclatureTypeMnemonic: String) {
                // nothing to do…
            }

            override fun onMediaSelected(mediaRecord: MediaRecord.File) {
                // nothing to do…
            }
        })

        if (savedState.getBoolean(
                KEY_SHOW_ALL_NOMENCLATURE_TYPES,
                false
            )
        ) adapter?.showAllEditableFields() else adapter?.showDefaultEditableFields()
        adapter?.lockDefaultValues(arguments?.getBoolean(ARG_SAVE_DEFAULT_VALUES) == true)

        with(recyclerView) {
            layoutManager = LinearLayoutManager(context)
            adapter = this@InformationFragment.adapter
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(savedState.apply { putAll(outState) })
    }

    override fun getResourceTitle(): Int {
        return R.string.pager_fragment_information_title
    }

    override fun getSubtitle(): CharSequence? {
        return observationRecord?.taxa?.selectedTaxonRecord?.taxon?.name
    }

    override fun pagingEnabled(): Boolean {
        return true
    }

    override fun validate(): Boolean {
        return this.observationRecord?.taxa?.selectedTaxonRecord != null
    }

    override fun refreshView() {
        nomenclatureViewModel.getEditableFields(
            observationRecord?.dataset?.dataset?.datasetId,
            arguments?.getBoolean(
                ARG_WITH_ADDITIONAL_FIELDS,
                false
            ) ?: false,
            EditableField.Type.INFORMATION,
            (arguments?.getParcelableArrayCompat<PropertySettings>(ARG_PROPERTIES)
                ?.toList() ?: emptyList()),
            observationRecord?.taxa?.selectedTaxonRecord?.taxon?.taxonomy
        )
    }

    private fun handleEditableFields(editableFields: List<EditableField>) {
        editableFields.filter { it.value != null }
            .forEach {
                if (observationRecord?.taxa?.selectedTaxonRecord?.properties?.containsKey(it.code) == true) return@forEach

                it.value?.toPair()
                    .also { pair ->
                        if (pair == null) observationRecord?.taxa?.selectedTaxonRecord?.properties?.remove(it.code)
                        else observationRecord?.taxa?.selectedTaxonRecord?.properties?.set(
                            pair.first,
                            pair.second
                        )
                    }
            }

        adapter?.bind(
            editableFields,
            *((observationRecord?.taxa?.selectedTaxonRecord?.properties?.values
                ?.filterNotNull()
                ?.filterNot { it.isEmpty() }
                ?.filterNot { it is PropertyValue.AdditionalField }
                ?: emptyList()) + (observationRecord?.taxa?.selectedTaxonRecord?.additionalFields
                ?: emptyList())).toTypedArray()
        )
    }

    companion object {

        private const val ARG_SAVE_DEFAULT_VALUES = "arg_save_default_values"
        private const val ARG_WITH_ADDITIONAL_FIELDS = "arg_with_additional_fields"
        private const val ARG_PROPERTIES = "arg_properties"
        private const val KEY_SHOW_ALL_NOMENCLATURE_TYPES = "show_all_nomenclature_types"

        /**
         * Use this factory method to create a new instance of [InformationFragment].
         *
         * @return A new instance of [InformationFragment]
         */
        @JvmStatic
        fun newInstance(
            saveDefaultValues: Boolean = false,
            withAdditionalFields: Boolean = false,
            vararg propertySettings: PropertySettings
        ) = InformationFragment().apply {
            arguments = Bundle().apply {
                putBoolean(
                    ARG_SAVE_DEFAULT_VALUES,
                    saveDefaultValues
                )
                putBoolean(
                    ARG_WITH_ADDITIONAL_FIELDS,
                    withAdditionalFields
                )
                putParcelableArray(
                    ARG_PROPERTIES,
                    propertySettings
                )
            }
        }
    }
}
