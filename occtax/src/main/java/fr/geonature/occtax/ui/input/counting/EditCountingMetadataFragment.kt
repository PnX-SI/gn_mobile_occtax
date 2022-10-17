package fr.geonature.occtax.ui.input.counting

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import fr.geonature.commons.data.entity.Nomenclature
import fr.geonature.commons.data.entity.Taxonomy
import fr.geonature.commons.lifecycle.observe
import fr.geonature.commons.util.KeyboardUtils.hideSoftKeyboard
import fr.geonature.occtax.R
import fr.geonature.occtax.features.input.domain.CountingMetadata
import fr.geonature.occtax.features.input.domain.Input
import fr.geonature.occtax.features.nomenclature.domain.EditableNomenclatureType
import fr.geonature.occtax.features.nomenclature.presentation.EditableNomenclatureTypeAdapter
import fr.geonature.occtax.features.nomenclature.presentation.NomenclatureViewModel
import fr.geonature.occtax.features.nomenclature.presentation.PropertyValueModel
import fr.geonature.occtax.settings.PropertySettings

/**
 * [Fragment] to let the user to edit additional counting information for the given [Input].
 *
 * @author S. Grimault
 */
@AndroidEntryPoint
class EditCountingMetadataFragment : Fragment() {

    private val nomenclatureViewModel: NomenclatureViewModel by viewModels()
    private val propertyValueModel: PropertyValueModel by viewModels()

    private lateinit var taxonomy: Taxonomy
    private lateinit var countingMetadata: CountingMetadata
    private lateinit var savedState: Bundle

    private var fab: ExtendedFloatingActionButton? = null

    private var listener: OnEditCountingMetadataFragmentListener? = null
    private var adapter: EditableNomenclatureTypeAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        savedState = savedInstanceState ?: Bundle()

        arguments?.also {
            taxonomy = it.getParcelable(ARG_TAXONOMY) ?: Taxonomy(
                Taxonomy.ANY,
                Taxonomy.ANY
            )
            countingMetadata = it.getParcelable(ARG_COUNTING_METADATA) ?: CountingMetadata()
        }

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
            R.layout.fragment_counting_edit,
            container,
            false
        )
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        val recyclerView = view.findViewById<RecyclerView>(android.R.id.list)
        val emptyTextView = view.findViewById<TextView>(android.R.id.empty)
        val progressBar = view.findViewById<ProgressBar>(android.R.id.progress)
            .apply { visibility = View.VISIBLE }

        fab = view.findViewById(R.id.fab)
        fab?.apply {
            setOnClickListener {
                listener?.onSave(countingMetadata)
            }
        }

        // Set the adapter
        adapter = EditableNomenclatureTypeAdapter(object :
            EditableNomenclatureTypeAdapter.OnEditableNomenclatureTypeAdapter {

            override fun getLifecycleOwner(): LifecycleOwner {
                return this@EditCountingMetadataFragment
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
                // workaround to force hide the soft keyboard
                view.rootView?.also {
                    hideSoftKeyboard(it)
                }

                return nomenclatureViewModel.getNomenclatureValuesByTypeAndTaxonomy(
                    nomenclatureTypeMnemonic,
                    taxonomy
                )
            }

            override fun onUpdate(editableNomenclatureType: EditableNomenclatureType) {
                countingMetadata.properties[editableNomenclatureType.code] =
                    editableNomenclatureType.value

                listener?.onCountingMetadata(countingMetadata)

                val propertyValue = editableNomenclatureType.value

                if (propertyValue !== null && editableNomenclatureType.locked) propertyValueModel.setPropertyValue(
                    taxonomy,
                    propertyValue
                ) else propertyValueModel.clearPropertyValue(
                    taxonomy,
                    editableNomenclatureType.code
                )
            }
        })

        with(recyclerView) {
            layoutManager = LinearLayoutManager(context)
            adapter = this@EditCountingMetadataFragment.adapter
        }

        loadNomenclatureTypes()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is OnEditCountingMetadataFragmentListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnEditCountingMetadataFragmentListener")
        }
    }

    private fun loadNomenclatureTypes() {
        nomenclatureViewModel.getEditableNomenclatures(
            EditableNomenclatureType.Type.COUNTING,
            (arguments?.getParcelableArray(ARG_PROPERTIES)
                ?.map { it as PropertySettings }
                ?.toList() ?: emptyList()),
            taxonomy
        )
    }

    private fun handleEditableNomenclatureTypes(editableNomenclatureTypes: List<EditableNomenclatureType>) {
        editableNomenclatureTypes.filter { it.value != null }.forEach {
            if (!countingMetadata.properties.containsKey(it.code)) {
                countingMetadata.properties[it.code] = it.value
            }
        }

        adapter?.bind(
            editableNomenclatureTypes,
            *countingMetadata.properties.values.filterNotNull().toTypedArray()
        )
    }

    /**
     * Callback used by [EditCountingMetadataFragment].
     */
    interface OnEditCountingMetadataFragmentListener {
        fun onCountingMetadata(countingMetadata: CountingMetadata)
        fun onSave(countingMetadata: CountingMetadata)
    }

    companion object {

        const val ARG_TAXONOMY = "arg_taxonomy"
        const val ARG_COUNTING_METADATA = "arg_counting_metadata"
        const val ARG_PROPERTIES = "arg_properties"

        private const val KEY_SHOW_ALL_NOMENCLATURE_TYPES = "show_all_nomenclature_types"

        /**
         * Use this factory method to create a new instance of [EditCountingMetadataFragment].
         *
         * @return A new instance of [EditCountingMetadataFragment]
         */
        @JvmStatic
        fun newInstance(
            taxonomy: Taxonomy,
            countingMetadata: CountingMetadata? = null,
            vararg propertySettings: PropertySettings
        ) = EditCountingMetadataFragment().apply {
            arguments = Bundle().apply {
                putParcelable(
                    ARG_TAXONOMY,
                    taxonomy
                )
                countingMetadata?.let {
                    putParcelable(
                        ARG_COUNTING_METADATA,
                        countingMetadata
                    )
                }
                putParcelableArray(
                    ARG_PROPERTIES,
                    propertySettings
                )
            }
        }
    }
}
