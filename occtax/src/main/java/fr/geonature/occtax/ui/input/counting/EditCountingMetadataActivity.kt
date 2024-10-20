package fr.geonature.occtax.ui.input.counting

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import fr.geonature.compat.content.getParcelableArrayExtraCompat
import fr.geonature.compat.content.getParcelableExtraCompat
import fr.geonature.occtax.R
import fr.geonature.occtax.features.record.domain.CountingRecord
import fr.geonature.occtax.features.record.domain.TaxonRecord
import fr.geonature.occtax.features.settings.domain.PropertySettings

/**
 * Edit additional counting information Activity.
 *
 * @see EditCountingMetadataFragment
 *
 * @author S. Grimault
 */
@AndroidEntryPoint
class EditCountingMetadataActivity : AppCompatActivity(),
    EditCountingMetadataFragment.OnEditCountingMetadataFragmentListener {

    private lateinit var countingRecord: CountingRecord

    // whether the current counting metadata is new or not
    private var isNew: Boolean = true

    // whether the current counting metadata has been modified or not
    private var isDirty: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.run {
            setDisplayHomeAsUpEnabled(true)
        }

        val taxonRecord = intent.getParcelableExtraCompat<TaxonRecord>(EXTRA_TAXON_RECORD) ?: run {
            // TODO: show a toast message about missing taxon record
            finish()
            return
        }

        countingRecord =
            intent.getParcelableExtraCompat(EXTRA_COUNTING_RECORD) ?: taxonRecord.counting.create()

        isNew = countingRecord.isEmpty()
        setTitle(if (countingRecord.isEmpty()) R.string.activity_counting_add_title else R.string.activity_counting_edit_title)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .apply {
                    replace(
                        android.R.id.content,
                        EditCountingMetadataFragment.newInstance(
                            intent.getLongExtra(
                                EXTRA_DATASET_ID,
                                -1L
                            )
                                .takeIf { it >= 0L },
                            taxonRecord,
                            countingRecord,
                            intent.getBooleanExtra(
                                EXTRA_SAVE_DEFAULT_VALUES,
                                false
                            ),
                            intent.getBooleanExtra(
                                EXTRA_WITH_ADDITIONAL_FIELDS,
                                false
                            ),
                            *(intent.getParcelableArrayExtraCompat(EXTRA_PROPERTIES)
                                ?: emptyArray())
                        )
                    )
                }
                .commit()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                confirmBeforeQuit()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        confirmBeforeQuit()
    }

    override fun onCountingRecord(countingRecord: CountingRecord) {
        this.countingRecord = countingRecord
        this.isDirty = true
    }

    override fun onSave(countingRecord: CountingRecord) {
        this.countingRecord = countingRecord
        sendResult()
        finish()
    }

    private fun sendResult() {
        setResult(Activity.RESULT_OK,
            Intent().apply {
                putExtra(
                    EXTRA_COUNTING_RECORD,
                    countingRecord
                )
            })
    }

    private fun confirmBeforeQuit() {
        if (!isDirty) {
            finish()
            return
        }

        AlertDialog.Builder(this)
            .setTitle(if (isNew && isDirty) R.string.alert_counting_title_discard else R.string.alert_counting_title_discard_changes)
            .setPositiveButton(
                R.string.alert_counting_action_discard
            ) { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .setNegativeButton(
                R.string.alert_counting_action_keep
            ) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    companion object {

        const val EXTRA_DATASET_ID = "extra_dataset_id"
        const val EXTRA_TAXON_RECORD = "extra_taxon_record"
        const val EXTRA_COUNTING_RECORD = "extra_counting_record"
        const val EXTRA_SAVE_DEFAULT_VALUES = "extra_save_default_values"
        const val EXTRA_WITH_ADDITIONAL_FIELDS = "extra_with_additional_fields"
        const val EXTRA_PROPERTIES = "extra_properties"

        fun newIntent(
            context: Context,
            datasetId: Long? = null,
            taxonRecord: TaxonRecord,
            countingRecord: CountingRecord? = null,
            saveDefaultValues: Boolean = false,
            withAdditionalFields: Boolean = false,
            vararg propertySettings: PropertySettings
        ): Intent {
            return Intent(
                context,
                EditCountingMetadataActivity::class.java
            ).apply {
                putExtra(
                    EXTRA_DATASET_ID,
                    datasetId
                )
                putExtra(
                    EXTRA_TAXON_RECORD,
                    taxonRecord
                )
                countingRecord?.also {
                    putExtra(
                        EXTRA_COUNTING_RECORD,
                        countingRecord
                    )
                }
                putExtra(
                    EXTRA_SAVE_DEFAULT_VALUES,
                    saveDefaultValues
                )
                putExtra(
                    EXTRA_WITH_ADDITIONAL_FIELDS,
                    withAdditionalFields
                )
                putExtra(
                    EXTRA_PROPERTIES,
                    propertySettings
                )
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
        }
    }
}
