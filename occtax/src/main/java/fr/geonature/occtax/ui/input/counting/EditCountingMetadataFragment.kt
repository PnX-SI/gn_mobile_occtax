package fr.geonature.occtax.ui.input.counting

import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import fr.geonature.commons.data.Nomenclature
import fr.geonature.commons.data.NomenclatureType
import fr.geonature.commons.data.Taxonomy
import fr.geonature.commons.data.helper.Provider.buildUri
import fr.geonature.commons.util.KeyboardUtils.hideSoftKeyboard
import fr.geonature.occtax.R
import fr.geonature.occtax.input.CountingMetadata
import fr.geonature.occtax.input.Input
import fr.geonature.occtax.input.PropertyValue
import fr.geonature.occtax.ui.input.dialog.ChooseNomenclatureDialogFragment

/**
 * [Fragment] to let the user to edit additional counting information for the given [Input].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class EditCountingMetadataFragment : Fragment(),
                                     ChooseNomenclatureDialogFragment.OnChooseNomenclatureDialogFragmentListener {

    private var listener: OnEditCountingMetadataFragmentListener? = null
    private var adapter: NomenclatureTypesRecyclerViewAdapter? = null
    private lateinit var taxonomy: Taxonomy
    private lateinit var countingMetadata: CountingMetadata

    private val loaderCallbacks = object : LoaderManager.LoaderCallbacks<Cursor> {
        override fun onCreateLoader(id: Int,
                                    args: Bundle?): Loader<Cursor> {
            return when (id) {
                LOADER_NOMENCLATURE_TYPES -> CursorLoader(requireContext(),
                                                          buildUri(NomenclatureType.TABLE_NAME),
                                                          null,
                                                          null,
                                                          null,
                                                          null)
                else -> throw IllegalArgumentException()
            }
        }

        override fun onLoadFinished(loader: Loader<Cursor>,
                                    data: Cursor?) {
            if (data == null) {
                Log.w(TAG,
                      "Failed to load data from '${(loader as CursorLoader).uri}'")

                return
            }

            when (loader.id) {
                LOADER_NOMENCLATURE_TYPES -> {
                    adapter?.also {
                        it.bind(data)
                        it.setCountingMetata(countingMetadata)
                    }
                }
            }
        }

        override fun onLoaderReset(loader: Loader<Cursor>) {
            when (loader.id) {
                LOADER_NOMENCLATURE_TYPES -> adapter?.bind(null)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.also {
            taxonomy = it.getParcelable(ARG_TAXONOMY) ?: Taxonomy(Taxonomy.ANY,
                                                                  Taxonomy.ANY)
            countingMetadata = it.getParcelable(ARG_COUNTING_METADATA) ?: CountingMetadata()
        }
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val recyclerView = inflater.inflate(R.layout.recycler_view,
                                            container,
                                            false)
        // Set the adapter
        adapter = NomenclatureTypesRecyclerViewAdapter(object : NomenclatureTypesRecyclerViewAdapter.OnNomenclatureTypesRecyclerViewAdapterListener {

            override fun onAction(nomenclatureTypeMnemonic: String) {
                // workaround to force hide the soft keyboard
                view?.rootView?.also {
                    hideSoftKeyboard(it)
                }

                val chooseNomenclatureDialogFragment = ChooseNomenclatureDialogFragment.newInstance(nomenclatureTypeMnemonic,
                                                                                                    taxonomy)
                chooseNomenclatureDialogFragment.show(childFragmentManager,
                                                      CHOOSE_NOMENCLATURE_DIALOG_FRAGMENT)
            }

            override fun onMinMaxValues(min: Int,
                                        max: Int) {
                countingMetadata.apply {
                    this.min = min
                    this.max = max
                }

                listener?.onCountingMetadata(countingMetadata)
            }
        })

        with(recyclerView as RecyclerView) {
            layoutManager = LinearLayoutManager(context)
            adapter = this@EditCountingMetadataFragment.adapter
        }

        return recyclerView
    }

    override fun onViewCreated(view: View,
                               savedInstanceState: Bundle?) {
        LoaderManager.getInstance(this)
            .initLoader(LOADER_NOMENCLATURE_TYPES,
                        null,
                        loaderCallbacks)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is OnEditCountingMetadataFragmentListener) {
            listener = context
        }
        else {
            throw RuntimeException("$context must implement OnEditCountingMetadataFragmentListener")
        }
    }

    override fun onSelectedNomenclature(nomenclatureType: String,
                                        nomenclature: Nomenclature) {
        countingMetadata.properties[nomenclatureType] = PropertyValue.fromNomenclature(nomenclatureType,
                                                                                       nomenclature)
        adapter?.setCountingMetata(countingMetadata)
    }

    /**
     * Callback used by [EditCountingMetadataFragment].
     */
    interface OnEditCountingMetadataFragmentListener {
        fun onCountingMetadata(countingMetadata: CountingMetadata)
    }

    companion object {
        private val TAG = EditCountingMetadataFragment::class.java.name

        const val ARG_TAXONOMY = "arg_taxonomy"
        const val ARG_COUNTING_METADATA = "arg_counting_metadata"

        private const val LOADER_NOMENCLATURE_TYPES = 1
        private const val CHOOSE_NOMENCLATURE_DIALOG_FRAGMENT = "choose_nomenclature_dialog_fragment"

        /**
         * Use this factory method to create a new instance of [EditCountingMetadataFragment].
         *
         * @return A new instance of [EditCountingMetadataFragment]
         */
        @JvmStatic
        fun newInstance(taxonomy: Taxonomy,
                        countingMetadata: CountingMetadata? = null) = EditCountingMetadataFragment().apply {
            arguments = Bundle().apply {
                putParcelable(ARG_TAXONOMY,
                              taxonomy)
                countingMetadata?.let {
                    putParcelable(ARG_COUNTING_METADATA,
                                  countingMetadata)
                }
            }
        }
    }
}