package fr.geonature.occtax.ui.input.counting

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import fr.geonature.commons.data.entity.Nomenclature
import fr.geonature.commons.data.entity.Taxonomy
import fr.geonature.commons.lifecycle.observe
import fr.geonature.commons.util.KeyboardUtils.hideSoftKeyboard
import fr.geonature.occtax.R
import fr.geonature.occtax.features.nomenclature.domain.EditableNomenclatureType
import fr.geonature.occtax.features.nomenclature.presentation.EditableNomenclatureTypeAdapter
import fr.geonature.occtax.features.nomenclature.presentation.NomenclatureViewModel
import fr.geonature.occtax.features.nomenclature.presentation.PropertyValueModel
import fr.geonature.occtax.features.record.domain.CountingRecord
import fr.geonature.occtax.features.record.domain.MediaRecord
import fr.geonature.occtax.features.record.domain.TaxonRecord
import fr.geonature.occtax.settings.PropertySettings
import kotlinx.coroutines.launch
import org.tinylog.Logger

/**
 * [Fragment] to let the user to edit additional counting information for the given [TaxonRecord].
 *
 * @author S. Grimault
 */
@AndroidEntryPoint
class EditCountingMetadataFragment : Fragment() {

    private val nomenclatureViewModel: NomenclatureViewModel by viewModels()
    private val propertyValueModel: PropertyValueModel by viewModels()

    private lateinit var savedState: Bundle
    private var taxonRecord: TaxonRecord? = null
    private var countingRecord: CountingRecord? = null

    private lateinit var mediaResultLauncher: ActivityResultLauncher<Intent>

    private var takePhotoLifecycleObserver: TakePhotoLifecycleObserver? = null
    private var content: CoordinatorLayout? = null
    private var fab: ExtendedFloatingActionButton? = null

    private var listener: OnEditCountingMetadataFragmentListener? = null
    private var adapter: EditableNomenclatureTypeAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        savedState = savedInstanceState ?: Bundle()

        arguments?.also {
            taxonRecord = it.getParcelable(ARG_TAXON_RECORD)
            countingRecord =
                it.getParcelable(ARG_COUNTING_RECORD) ?: taxonRecord?.counting?.create()
        }

        with(nomenclatureViewModel) {
            observe(
                editableNomenclatures,
                ::handleEditableNomenclatureTypes
            )
        }

        activity?.also {
            takePhotoLifecycleObserver = TakePhotoLifecycleObserver(
                it.applicationContext,
                it.activityResultRegistry
            ).apply {
                lifecycle.addObserver(this)
            }
        }

        mediaResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
                if ((activityResult.resultCode != Activity.RESULT_OK) || (activityResult.data == null)) {
                    return@registerForActivityResult
                }

                activityResult.data?.getParcelableExtra<CountingRecord>(MediaListActivity.EXTRA_COUNTING_RECORD)
                    ?.also { countingRecord ->
                        this.countingRecord = countingRecord

                        adapter?.setPropertyValues(
                            *(countingRecord.properties.values
                                .filterNotNull()
                                .filterNot { it.isEmpty() }
                                .toTypedArray())
                        )
                    }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(
            R.layout.fragment_recycler_view_fab,
            container,
            false
        )
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        content = view.findViewById(android.R.id.content)

        val recyclerView = view.findViewById<RecyclerView>(android.R.id.list)
        val emptyTextView = view.findViewById<TextView>(android.R.id.empty)
            .apply {
                setText(if (countingRecord == null) R.string.counting_not_found else R.string.information_no_data)
            }
        val progressBar = view.findViewById<ProgressBar>(android.R.id.progress)
            .apply { visibility = View.VISIBLE }

        fab = view.findViewById<ExtendedFloatingActionButton?>(R.id.fab)
            ?.apply {
                text = getString(R.string.action_save)
                extend()

                if (countingRecord == null) {
                    hide()
                }

                setOnClickListener {
                    countingRecord?.also {
                        listener?.onSave(it)
                    }
                }
            }

        // Set the adapter
        adapter = EditableNomenclatureTypeAdapter(object :
            EditableNomenclatureTypeAdapter.OnEditableNomenclatureTypeAdapter {

            override fun getLifecycleOwner(): LifecycleOwner {
                return this@EditCountingMetadataFragment
            }

            override fun getCoordinatorLayout(): CoordinatorLayout? {
                return this@EditCountingMetadataFragment.content
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
                    taxonRecord?.taxon?.taxonomy
                )
            }

            override fun onUpdate(editableNomenclatureType: EditableNomenclatureType) {
                val countingRecord = countingRecord ?: return

                editableNomenclatureType.value?.toPair()
                    .also {
                        if (it == null) countingRecord.properties.remove(editableNomenclatureType.code)
                        else countingRecord.properties[editableNomenclatureType.code] = it.second
                    }

                listener?.onCountingRecord(countingRecord)

                val taxonomy = taxonRecord?.taxon?.taxonomy ?: Taxonomy(
                    Taxonomy.ANY,
                    Taxonomy.ANY
                )
                val propertyValue = editableNomenclatureType.value

                if (propertyValue !== null && editableNomenclatureType.locked) propertyValueModel.setPropertyValue(
                    taxonomy,
                    propertyValue
                ) else propertyValueModel.clearPropertyValue(
                    taxonomy,
                    editableNomenclatureType.code
                )
            }

            override fun onAddMedia(nomenclatureTypeMnemonic: String) {
                val context = context ?: run {
                    Logger.warn { "missing context to pick media: abort" }
                    null
                } ?: return
                val taxonRecord = taxonRecord ?: run {
                    Logger.warn { "missing taxon record argument: abort" }
                    null
                } ?: return
                val countingRecord = countingRecord ?: run {
                    Logger.warn { "missing counting record: abort" }
                    null
                } ?: return

                // workaround to force hide the soft keyboard
                view.rootView?.also {
                    hideSoftKeyboard(it)
                }

                AddPhotoBottomSheetDialogFragment().apply {
                    setOnAddPhotoBottomSheetDialogFragmentListener(object :
                        AddPhotoBottomSheetDialogFragment.OnAddPhotoBottomSheetDialogFragmentListener {
                        override fun onSelectMenuItem(menuItem: AddPhotoBottomSheetDialogFragment.MenuItem) {
                            lifecycleScope.launch {
                                val imageFile =
                                    takePhotoLifecycleObserver?.invoke(
                                        if (menuItem.iconResourceId == R.drawable.ic_add_photo) TakePhotoLifecycleObserver.ImagePicker.CAMERA else TakePhotoLifecycleObserver.ImagePicker.GALLERY,
                                        taxonRecord.counting.mediaBasePath(
                                            context,
                                            countingRecord
                                        ).absolutePath
                                    )

                                if (imageFile != null) {
                                    Logger.info { "add image from file '${imageFile.absolutePath}'" }

                                    countingRecord.medias.addFile(imageFile.absolutePath)

                                    adapter?.setPropertyValues(
                                        *(countingRecord.properties.values
                                            .filterNotNull()
                                            .filterNot { it.isEmpty() }
                                            .toTypedArray())
                                    )
                                }

                                dismiss()
                            }
                        }
                    })
                }
                    .show(
                        childFragmentManager,
                        ADD_PHOTO_DIALOG_FRAGMENT
                    )
            }

            override fun onMediaSelected(mediaRecord: MediaRecord.File) {
                launchMediaActivity(mediaRecord)
            }
        })

        adapter?.lockDefaultValues(arguments?.getBoolean(ARG_SAVE_DEFAULT_VALUES) == true)

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
        val taxonRecord = taxonRecord ?: run {
            Logger.warn { "missing taxon record argument: abort" }
            null
        } ?: return

        nomenclatureViewModel.getEditableNomenclatures(
            EditableNomenclatureType.Type.COUNTING,
            (arguments?.getParcelableArray(ARG_PROPERTIES)
                ?.map { it as PropertySettings }
                ?.toList() ?: emptyList()),
            taxonRecord.taxon.taxonomy
        )
    }

    private fun handleEditableNomenclatureTypes(editableNomenclatureTypes: List<EditableNomenclatureType>) {
        editableNomenclatureTypes.filter { it.value != null }
            .forEach {
                if (countingRecord?.properties?.containsKey(it.code) == true) return@forEach

                it.value?.toPair()
                    .also { pair ->
                        if (pair == null) countingRecord?.properties?.remove(it.code)
                        else countingRecord?.properties?.set(
                            pair.first,
                            pair.second
                        )
                    }
            }

        adapter?.bind(
            editableNomenclatureTypes,
            *(countingRecord?.properties?.values
                ?.filterNotNull()
                ?.filterNot { it.isEmpty() }
                ?.toTypedArray() ?: emptyArray())
        )
    }

    private fun launchMediaActivity(selectedMedia: MediaRecord? = null) {
        val context = context ?: run {
            Logger.warn { "missing context to launch activity '${MediaListActivity::class.java.simpleName}': abort" }
            null
        } ?: return
        val taxonRecord = taxonRecord ?: run {
            Logger.warn { "missing taxon record argument: abort" }
            null
        } ?: return
        val countingRecord = countingRecord ?: run {
            Logger.warn { "missing counting record: abort" }
            null
        } ?: return

        mediaResultLauncher.launch(
            MediaListActivity.newIntent(
                context,
                taxonRecord,
                countingRecord,
                selectedMedia?.let {
                    when (it) {
                        is MediaRecord.File -> it.path
                        else -> null
                    }
                }
            )
        )
    }

    /**
     * Callback used by [EditCountingMetadataFragment].
     */
    interface OnEditCountingMetadataFragmentListener {
        fun onCountingRecord(countingRecord: CountingRecord)
        fun onSave(countingRecord: CountingRecord)
    }

    companion object {

        private const val ARG_TAXON_RECORD = "arg_taxon_record"
        private const val ARG_COUNTING_RECORD = "arg_counting_record"
        private const val ARG_SAVE_DEFAULT_VALUES = "arg_save_default_values"
        private const val ARG_PROPERTIES = "arg_properties"

        private const val ADD_PHOTO_DIALOG_FRAGMENT = "add_photo_dialog_fragment"
        private const val KEY_SHOW_ALL_NOMENCLATURE_TYPES = "show_all_nomenclature_types"

        /**
         * Use this factory method to create a new instance of [EditCountingMetadataFragment].
         *
         * @return A new instance of [EditCountingMetadataFragment]
         */
        @JvmStatic
        fun newInstance(
            taxonRecord: TaxonRecord,
            countingRecord: CountingRecord? = null,
            saveDefaultValues: Boolean = false,
            vararg propertySettings: PropertySettings
        ) = EditCountingMetadataFragment().apply {
            arguments = Bundle().apply {
                putParcelable(
                    ARG_TAXON_RECORD,
                    taxonRecord
                )
                countingRecord?.also {
                    putParcelable(
                        ARG_COUNTING_RECORD,
                        countingRecord
                    )
                }
                putBoolean(
                    ARG_SAVE_DEFAULT_VALUES,
                    saveDefaultValues
                )
                putParcelableArray(
                    ARG_PROPERTIES,
                    propertySettings
                )
            }
        }
    }
}
