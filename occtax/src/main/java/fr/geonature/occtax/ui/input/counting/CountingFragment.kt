package fr.geonature.occtax.ui.input.counting

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import fr.geonature.commons.ui.adapter.AbstractListItemRecyclerViewAdapter
import fr.geonature.compat.content.getParcelableExtraCompat
import fr.geonature.compat.os.getParcelableArrayCompat
import fr.geonature.occtax.R
import fr.geonature.occtax.features.record.domain.CountingRecord
import fr.geonature.occtax.features.record.domain.TaxonRecord
import fr.geonature.occtax.settings.PropertySettings
import fr.geonature.occtax.ui.input.AbstractInputFragment

/**
 * [Fragment] to let the user to add additional counting information for the given [TaxonRecord].
 *
 * @author S. Grimault
 */
class CountingFragment : AbstractInputFragment() {

    private lateinit var editCountingResultLauncher: ActivityResultLauncher<Intent>

    private var adapter: CountingRecyclerViewAdapter? = null
    private var recyclerView: RecyclerView? = null
    private var emptyTextView: TextView? = null
    private var fab: ExtendedFloatingActionButton? = null
    private var redirectToEditCountingMetadataActivity = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        editCountingResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if ((it.resultCode != Activity.RESULT_OK) || (it.data == null)) {
                    return@registerForActivityResult
                }

                updateCountingMetadata(it.data?.getParcelableExtraCompat(EditCountingMetadataActivity.EXTRA_COUNTING_RECORD))
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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
        super.onViewCreated(
            view,
            savedInstanceState
        )

        recyclerView = view.findViewById(android.R.id.list)

        emptyTextView = view.findViewById(android.R.id.empty)
        emptyTextView?.text = getString(R.string.counting_no_data)

        fab = view.findViewById(R.id.fab)
        fab?.apply {
            setText(R.string.action_new_counting)
            extend()
            setOnClickListener {
                launchEditCountingMetadataActivity()
            }
        }

        adapter = CountingRecyclerViewAdapter(object :
            AbstractListItemRecyclerViewAdapter.OnListItemRecyclerViewAdapterListener<CountingRecord> {
            override fun onClick(item: CountingRecord) {
                launchEditCountingMetadataActivity(item)
            }

            override fun onLongClicked(
                position: Int,
                item: CountingRecord
            ) {
                context?.run {
                    getSystemService(
                        this,
                        Vibrator::class.java
                    )?.vibrate(
                        VibrationEffect.createOneShot(
                            100,
                            VibrationEffect.DEFAULT_AMPLITUDE
                        )
                    )

                    AlertDialog.Builder(this)
                        .setTitle(R.string.alert_dialog_counting_delete_title)
                        .setPositiveButton(
                            R.string.alert_dialog_ok
                        ) { dialog, _ ->
                            adapter?.remove(item)
                            observationRecord?.taxa?.selectedTaxonRecord?.counting?.delete(
                                this,
                                item.index
                            )
                            listener.validateCurrentPage()

                            dialog.dismiss()
                        }
                        .setNegativeButton(
                            R.string.alert_dialog_cancel
                        ) { dialog, _ -> dialog.dismiss() }
                        .show()
                }
            }

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
        })

        with(recyclerView as RecyclerView) {
            layoutManager = LinearLayoutManager(context)
            adapter = this@CountingFragment.adapter

            val dividerItemDecoration = DividerItemDecoration(
                context,
                (layoutManager as LinearLayoutManager).orientation
            )
            addItemDecoration(dividerItemDecoration)
        }
    }

    override fun getResourceTitle(): Int {
        return R.string.pager_fragment_counting_title
    }

    override fun getSubtitle(): CharSequence? {
        return observationRecord?.taxa?.selectedTaxonRecord?.taxon?.name
    }

    override fun pagingEnabled(): Boolean {
        return true
    }

    override fun validate(): Boolean {
        return observationRecord?.taxa?.selectedTaxonRecord?.counting?.counting?.isNotEmpty()
            ?: false
    }

    override fun refreshView() {
        if (observationRecord == null) {
            return
        }

        val counting =
            observationRecord?.taxa?.selectedTaxonRecord?.counting?.counting
                ?: emptyList()

        adapter?.setItems(counting)

        if (counting.isEmpty() && redirectToEditCountingMetadataActivity) {
            redirectToEditCountingMetadataActivity = false
            launchEditCountingMetadataActivity()
        }
    }

    private fun launchEditCountingMetadataActivity(countingRecord: CountingRecord? = null) {
        val context = context ?: return
        val datasetId = observationRecord?.dataset?.datasetId
        val taxonRecord = observationRecord?.taxa?.selectedTaxonRecord ?: return

        editCountingResultLauncher.launch(
            EditCountingMetadataActivity.newIntent(
                context,
                datasetId,
                taxonRecord,
                countingRecord,
                arguments?.getBoolean(
                    ARG_SAVE_DEFAULT_VALUES,
                    false
                ) ?: false,
                arguments?.getBoolean(
                    ARG_WITH_ADDITIONAL_FIELDS,
                    false
                ) ?: false,
                *(arguments?.getParcelableArrayCompat(ARG_PROPERTIES) ?: emptyArray())
            )
        )
    }

    private fun updateCountingMetadata(countingRecord: CountingRecord?) {
        val context = context ?: return

        if (countingRecord == null) {
            Toast.makeText(
                context,
                R.string.counting_toast_empty,
                Toast.LENGTH_LONG
            )
                .show()

            return
        }

        if (countingRecord.isEmpty()) {
            observationRecord?.taxa?.selectedTaxonRecord?.counting?.delete(
                context,
                countingRecord.index
            )
            Toast.makeText(
                context,
                R.string.counting_toast_empty,
                Toast.LENGTH_LONG
            )
                .show()

            return
        }

        // keep only selected media from updated CountingMetadata
        val selectedMedia = countingRecord.medias.files
        observationRecord?.taxa?.selectedTaxonRecord?.counting?.mediaBasePath?.let {
            it(
                context,
                countingRecord
            )
        }
            ?.walkTopDown()
            ?.asSequence()
            ?.filter { file ->
                file.isFile && file.canWrite()
            }
            ?.filter { file ->
                selectedMedia.none { path -> path == file.absolutePath }
            }
            ?.forEach { file ->
                file.delete()
            }

        observationRecord?.taxa?.selectedTaxonRecord?.counting?.addOrUpdate(countingRecord)

        adapter?.setItems(
            observationRecord?.taxa?.selectedTaxonRecord?.counting?.counting ?: emptyList()
        )
    }

    companion object {

        private const val ARG_SAVE_DEFAULT_VALUES = "arg_save_default_values"
        private const val ARG_WITH_ADDITIONAL_FIELDS = "arg_with_additional_fields"
        private const val ARG_PROPERTIES = "arg_properties"

        /**
         * Use this factory method to create a new instance of [CountingFragment].
         *
         * @return A new instance of [CountingFragment]
         */
        @JvmStatic
        fun newInstance(
            saveDefaultValues: Boolean = false,
            withAdditionalFields: Boolean = false,
            vararg propertySettings: PropertySettings
        ) = CountingFragment().apply {
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
