package fr.geonature.occtax.ui.input.informations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import fr.geonature.commons.data.ContentProviderAuthority
import fr.geonature.commons.data.entity.Nomenclature
import fr.geonature.commons.data.entity.Taxonomy
import fr.geonature.commons.lifecycle.observe
import fr.geonature.occtax.R
import fr.geonature.occtax.features.nomenclature.domain.BaseEditableNomenclatureType
import fr.geonature.occtax.features.nomenclature.domain.EditableNomenclatureType
import fr.geonature.occtax.features.nomenclature.presentation.EditableNomenclatureTypeAdapter
import fr.geonature.occtax.features.nomenclature.presentation.NomenclatureViewModel
import fr.geonature.occtax.input.Input
import fr.geonature.occtax.input.InputTaxon
import fr.geonature.occtax.input.PropertyValue
import fr.geonature.occtax.settings.PropertySettings
import fr.geonature.occtax.ui.input.AbstractInputFragment
import fr.geonature.occtax.ui.input.dialog.ChooseNomenclatureDialogFragment
import org.tinylog.kotlin.Logger
import javax.inject.Inject

/**
 * [Fragment] to let the user to add additional information for the given [Input].
 *
 * @author S. Grimault
 */
@AndroidEntryPoint
class InformationFragment : AbstractInputFragment(),
    ChooseNomenclatureDialogFragment.OnChooseNomenclatureDialogFragmentListener {

    @ContentProviderAuthority
    @Inject
    lateinit var authority: String

    private val nomenclatureViewModel: NomenclatureViewModel by viewModels()

    private lateinit var savedState: Bundle

    private var adapter: EditableNomenclatureTypeAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        savedState = savedInstanceState ?: Bundle()

        with(nomenclatureViewModel) {
            observe(
                editableNomenclatures,
                ::handleEditableNomenclatureTypes
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(
            R.layout.fragment_information,
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

        adapter = EditableNomenclatureTypeAdapter(object :
            EditableNomenclatureTypeAdapter.OnEditableNomenclatureTypeAdapter {
            override fun showEmptyTextView(show: Boolean) {
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

            override fun onAction(nomenclatureTypeMnemonic: String) {

                val taxonomy = input?.getCurrentSelectedInputTaxon()?.taxon?.taxonomy
                    ?: Taxonomy(
                        Taxonomy.ANY,
                        Taxonomy.ANY
                    )

                val chooseNomenclatureDialogFragment = ChooseNomenclatureDialogFragment.newInstance(
                    nomenclatureTypeMnemonic,
                    taxonomy
                )
                chooseNomenclatureDialogFragment.show(
                    childFragmentManager,
                    CHOOSE_NOMENCLATURE_DIALOG_FRAGMENT
                )
            }

            override fun onEdit(
                nomenclatureTypeMnemonic: String,
                value: String?
            ) {
                (input?.getCurrentSelectedInputTaxon() as InputTaxon?)?.properties?.set(
                    nomenclatureTypeMnemonic,
                    PropertyValue.fromValue(
                        nomenclatureTypeMnemonic,
                        value
                    )
                )
            }
        })

        if (savedState.getBoolean(
                KEY_SHOW_ALL_NOMENCLATURE_TYPES,
                false
            )) adapter?.showAllNomenclatureTypes() else adapter?.showDefaultNomenclatureTypes()

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
        return input?.getCurrentSelectedInputTaxon()?.taxon?.name
    }

    override fun pagingEnabled(): Boolean {
        return true
    }

    override fun validate(): Boolean {
        return this.input?.getCurrentSelectedInputTaxon() != null
    }

    override fun refreshView() {
        nomenclatureViewModel.getEditableNomenclatures(
            BaseEditableNomenclatureType.Type.INFORMATION,
            *(arguments?.getParcelableArray(ARG_PROPERTIES)
                ?.map { it as PropertySettings }
                ?.toTypedArray() ?: emptyArray())
        )
    }

    override fun onSelectedNomenclature(
        nomenclatureType: String,
        nomenclature: Nomenclature
    ) {
        with(
            PropertyValue.fromNomenclature(
                nomenclatureType,
                nomenclature
            )
        ) {
            Logger.debug { "selected nomenclature value: $nomenclatureType:${nomenclature.code} (id: ${nomenclature.id})" }

            (input?.getCurrentSelectedInputTaxon() as InputTaxon?)?.properties?.set(
                nomenclatureType,
                this
            )
            adapter?.setPropertyValue(this)
        }
    }

    private fun handleEditableNomenclatureTypes(editableNomenclatureTypes: List<EditableNomenclatureType>) {
        adapter?.bind(
            editableNomenclatureTypes,
            *((input?.getCurrentSelectedInputTaxon() as InputTaxon?)?.properties?.values?.toTypedArray()
                ?: emptyArray())
        )
    }

    companion object {

        private const val ARG_PROPERTIES = "arg_properties"
        private const val CHOOSE_NOMENCLATURE_DIALOG_FRAGMENT =
            "choose_nomenclature_dialog_fragment"

        private const val KEY_SHOW_ALL_NOMENCLATURE_TYPES = "show_all_nomenclature_types"

        /**
         * Use this factory method to create a new instance of [InformationFragment].
         *
         * @return A new instance of [InformationFragment]
         */
        @JvmStatic
        fun newInstance(vararg propertySettings: PropertySettings) = InformationFragment().apply {
            arguments = Bundle().apply {
                putParcelableArray(
                    ARG_PROPERTIES,
                    propertySettings
                )
            }
        }
    }
}
