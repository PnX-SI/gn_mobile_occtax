package fr.geonature.occtax.ui.input.informations

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
import fr.geonature.commons.data.DefaultNomenclatureWithType
import fr.geonature.commons.data.Nomenclature
import fr.geonature.commons.data.NomenclatureType
import fr.geonature.commons.data.Taxonomy
import fr.geonature.commons.data.helper.Provider.buildUri
import fr.geonature.commons.input.AbstractInput
import fr.geonature.occtax.R
import fr.geonature.occtax.input.Input
import fr.geonature.occtax.input.InputTaxon
import fr.geonature.occtax.input.PropertyValue
import fr.geonature.occtax.ui.input.IInputFragment
import fr.geonature.occtax.ui.input.dialog.ChooseNomenclatureDialogFragment
import fr.geonature.viewpager.ui.IValidateFragment

/**
 * [Fragment] to let the user to add additional information for the given [Input].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class InformationFragment : Fragment(),
    IValidateFragment,
    IInputFragment,
    ChooseNomenclatureDialogFragment.OnChooseNomenclatureDialogFragmentListener {

    private var input: Input? = null
    private var adapter: NomenclatureTypesRecyclerViewAdapter? = null

    private val loaderCallbacks = object : LoaderManager.LoaderCallbacks<Cursor> {
        override fun onCreateLoader(
            id: Int,
            args: Bundle?
        ): Loader<Cursor> {
            return when (id) {
                LOADER_NOMENCLATURE_TYPES -> CursorLoader(
                    requireContext(),
                    buildUri(NomenclatureType.TABLE_NAME),
                    null,
                    null,
                    null,
                    null
                )
                LOADER_DEFAULT_NOMENCLATURE_VALUES -> CursorLoader(
                    requireContext(),
                    buildUri(
                        NomenclatureType.TABLE_NAME,
                        "occtax",
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
                Log.w(
                    TAG,
                    "Failed to load data from '${(loader as CursorLoader).uri}'"
                )

                return
            }

            when (loader.id) {
                LOADER_NOMENCLATURE_TYPES -> {
                    adapter?.bind(data)
                    loadDefaultNomenclatureValues()
                }
                LOADER_DEFAULT_NOMENCLATURE_VALUES -> {
                    val defaultMnemonicFilter = adapter?.defaultMnemonicFilter() ?: emptyList()
                    val defaultNomenclatureValues = mutableListOf<DefaultNomenclatureWithType>()
                    data.moveToFirst()

                    while (!data.isAfterLast) {
                        val defaultNomenclatureValue = DefaultNomenclatureWithType.fromCursor(data)

                        if (defaultNomenclatureValue != null && defaultMnemonicFilter.contains(
                                defaultNomenclatureValue.nomenclatureWithType?.type?.mnemonic
                            )
                        ) {
                            defaultNomenclatureValues.add(defaultNomenclatureValue)
                        }

                        data.moveToNext()
                    }

                    setPropertyValues(defaultNomenclatureValues)
                }
            }
        }

        override fun onLoaderReset(loader: Loader<Cursor>) {
            when (loader.id) {
                LOADER_NOMENCLATURE_TYPES -> adapter?.bind(null)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val recyclerView = inflater.inflate(
            R.layout.recycler_view,
            container,
            false
        )
        // Set the adapter
        adapter = NomenclatureTypesRecyclerViewAdapter(object :
            NomenclatureTypesRecyclerViewAdapter.OnNomenclatureTypesRecyclerViewAdapterListener {
            override fun showMore() {
                setPropertyValues()
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

        with(recyclerView as RecyclerView) {
            layoutManager = LinearLayoutManager(context)
            adapter = this@InformationFragment.adapter
        }

        return recyclerView
    }

    override fun getResourceTitle(): Int {
        return R.string.pager_fragment_information_title
    }

    override fun pagingEnabled(): Boolean {
        return true
    }

    override fun validate(): Boolean {
        return this.input?.getCurrentSelectedInputTaxon() != null
    }

    override fun refreshView() {
        LoaderManager.getInstance(this)
            .restartLoader(
                LOADER_NOMENCLATURE_TYPES,
                null,
                loaderCallbacks
            )
    }

    override fun setInput(input: AbstractInput) {
        this.input = input as Input
    }

    override fun onSelectedNomenclature(
        nomenclatureType: String,
        nomenclature: Nomenclature
    ) {

        (input?.getCurrentSelectedInputTaxon() as InputTaxon?)?.properties?.set(
            nomenclatureType,
            PropertyValue.fromNomenclature(
                nomenclatureType,
                nomenclature
            )
        )
        setPropertyValues()
    }

    private fun loadDefaultNomenclatureValues() {
        LoaderManager.getInstance(this)
            .restartLoader(
                LOADER_DEFAULT_NOMENCLATURE_VALUES,
                null,
                loaderCallbacks
            )
    }

    private fun setPropertyValues(defaultNomenclatureValues: List<DefaultNomenclatureWithType> = emptyList()) {
        defaultNomenclatureValues.forEach {
            val nomenclatureType = it.nomenclatureWithType?.type?.mnemonic ?: return@forEach

            if ((input?.getCurrentSelectedInputTaxon() as InputTaxon?)?.properties?.containsKey(
                    nomenclatureType
                ) == true
            ) {
                return@forEach
            }

            (input?.getCurrentSelectedInputTaxon() as InputTaxon?)?.properties?.set(
                nomenclatureType,
                PropertyValue.fromNomenclature(
                    nomenclatureType,
                    it.nomenclatureWithType
                )
            )
        }

        adapter?.setPropertyValues(
            (input?.getCurrentSelectedInputTaxon() as InputTaxon?)?.properties?.values?.toList()
                ?: emptyList()
        )
    }

    companion object {
        private val TAG = InformationFragment::class.java.name

        private const val LOADER_NOMENCLATURE_TYPES = 1
        private const val LOADER_DEFAULT_NOMENCLATURE_VALUES = 2
        private const val CHOOSE_NOMENCLATURE_DIALOG_FRAGMENT =
            "choose_nomenclature_dialog_fragment"

        /**
         * Use this factory method to create a new instance of [InformationFragment].
         *
         * @return A new instance of [InformationFragment]
         */
        @JvmStatic
        fun newInstance() = InformationFragment()
    }
}
