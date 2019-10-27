package fr.geonature.occtax.ui.input.dialog

import android.app.Dialog
import android.database.Cursor
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import fr.geonature.commons.data.Nomenclature
import fr.geonature.commons.data.NomenclatureType
import fr.geonature.commons.data.Provider
import fr.geonature.commons.data.Taxonomy
import fr.geonature.occtax.R
import java.util.Locale

/**
 * [DialogFragment] to let the user to choose a nomenclature value for a given [NomenclatureType].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class ChooseNomenclatureDialogFragment : DialogFragment() {

    private var listener: OnChooseNomenclatureDialogFragmentListener? = null
    private var adapter: NomenclatureRecyclerViewAdapter? = null

    private val loaderCallbacks = object : LoaderManager.LoaderCallbacks<Cursor> {
        override fun onCreateLoader(id: Int,
                                    args: Bundle?): Loader<Cursor> {
            when (id) {
                LOADER_NOMENCLATURES -> {
                    val nomenclatureType = args?.getString(ARG_NOMENCLATURE_TYPE,
                                                           "") ?: ""
                    val taxonomy = args?.getParcelable(ARG_TAXONOMY)
                            ?: Taxonomy(Taxonomy.ANY,
                                        Taxonomy.ANY)

                    return CursorLoader(requireContext(),
                                        Provider.buildUri(NomenclatureType.TABLE_NAME,
                                                          nomenclatureType,
                                                          "items",
                                                          taxonomy.kingdom,
                                                          taxonomy.group),
                                        null,
                                        null,
                                        null,
                                        null)
                }
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
                LOADER_NOMENCLATURES -> adapter?.bind(data)
            }
        }

        override fun onLoaderReset(loader: Loader<Cursor>) {
            when (loader.id) {
                LOADER_NOMENCLATURES -> adapter?.bind(null)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (parentFragment is OnChooseNomenclatureDialogFragmentListener) {
            listener = parentFragment as OnChooseNomenclatureDialogFragmentListener
        }
        else {
            throw RuntimeException("$parentFragment must implement OnChooseNomenclatureDialogFragmentListener")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = requireContext()

        val recyclerView = View.inflate(context,
                                        R.layout.recycler_view,
                                        null)

        // Set the adapter
        adapter = NomenclatureRecyclerViewAdapter(object : NomenclatureRecyclerViewAdapter.OnNomenclatureRecyclerViewAdapterListener {
            override fun onSelectedNomenclature(nomenclature: Nomenclature) {
                val nomenclatureType = arguments?.getString(ARG_NOMENCLATURE_TYPE)
                        ?: return
                listener?.onSelectedNomenclature(nomenclatureType,
                                                 nomenclature)
                dismiss()
            }
        })

        with(recyclerView as RecyclerView) {
            layoutManager = LinearLayoutManager(context)
            adapter = this@ChooseNomenclatureDialogFragment.adapter

            val dividerItemDecoration = DividerItemDecoration(context,
                                                              (layoutManager as LinearLayoutManager).orientation)
            addItemDecoration(dividerItemDecoration)
        }

        return AlertDialog.Builder(context)
            .setView(recyclerView)
            .setNegativeButton(R.string.alert_dialog_cancel,
                               null)
            .also { builder ->
                val nomenclatureType = arguments?.getString(ARG_NOMENCLATURE_TYPE) ?: return@also
                getNomenclatureTypeLabel(nomenclatureType)?.also {
                    builder.setTitle(it)
                }
            }
            .create()
    }

    override fun onResume() {
        super.onResume()

        LoaderManager.getInstance(this)
            .initLoader(LOADER_NOMENCLATURES,
                        arguments,
                        loaderCallbacks)
    }

    private fun getNomenclatureTypeLabel(mnemonic: String): String? {
        val context = context ?: return null
        val resourceId = resources.getIdentifier("nomenclature_${mnemonic.toLowerCase(Locale.getDefault())}",
                                                 "string",
                                                 context.packageName)

        return if (resourceId == 0) null else context.getString(resourceId)
    }

    /**
     * Callback used by [ChooseNomenclatureDialogFragment].
     */
    interface OnChooseNomenclatureDialogFragmentListener {

        /**
         * Called when a [Nomenclature] value has been selected.
         *
         * @param nomenclatureType mnemonic of the nomenclature type
         * @param nomenclature the selected [Nomenclature] value
         */
        fun onSelectedNomenclature(nomenclatureType: String,
                                   nomenclature: Nomenclature)
    }

    companion object {
        private val TAG = ChooseNomenclatureDialogFragment::class.java.name

        const val ARG_NOMENCLATURE_TYPE = "arg_nomenclature_type"
        const val ARG_TAXONOMY = "arg_taxonomy"

        private const val LOADER_NOMENCLATURES = 1

        /**
         * Use this factory method to create a new instance of [ChooseNomenclatureDialogFragment].
         *
         * @return A new instance of [ChooseNomenclatureDialogFragment]
         */
        @JvmStatic
        fun newInstance(nomenclatureType: String,
                        taxonomy: Taxonomy) = ChooseNomenclatureDialogFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_NOMENCLATURE_TYPE,
                          nomenclatureType)
                putParcelable(ARG_TAXONOMY,
                              taxonomy)
            }
        }
    }
}