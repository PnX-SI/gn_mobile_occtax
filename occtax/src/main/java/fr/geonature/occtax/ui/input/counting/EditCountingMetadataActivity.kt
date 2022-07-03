package fr.geonature.occtax.ui.input.counting

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import fr.geonature.commons.data.entity.Taxonomy
import fr.geonature.occtax.R
import fr.geonature.occtax.input.CountingMetadata
import fr.geonature.occtax.settings.PropertySettings

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

    private lateinit var countingMetadata: CountingMetadata

    // whether the current counting metadata is new or not
    private var isNew: Boolean = true

    // whether the current counting metadata has been modified or not
    private var isDirty: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.run {
            setDisplayHomeAsUpEnabled(true)
        }

        countingMetadata = intent.getParcelableExtra(EXTRA_COUNTING_METADATA) ?: CountingMetadata()

        isNew = countingMetadata.isEmpty()
        setTitle(if (countingMetadata.isEmpty()) R.string.activity_counting_add_title else R.string.activity_counting_edit_title)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(
                    android.R.id.content,
                    EditCountingMetadataFragment.newInstance(
                        intent.getParcelableExtra(EXTRA_TAXONOMY) ?: Taxonomy(
                            Taxonomy.ANY,
                            Taxonomy.ANY
                        ),
                        countingMetadata,
                        *(intent.getParcelableArrayExtra(EXTRA_PROPERTIES)
                            ?.map { it as PropertySettings }
                            ?.toTypedArray() ?: emptyArray())
                    )
                )
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

    override fun onBackPressed() {
        confirmBeforeQuit()
    }

    override fun onCountingMetadata(countingMetadata: CountingMetadata) {
        this.countingMetadata = countingMetadata
        this.isDirty = true
    }

    override fun onSave(countingMetadata: CountingMetadata) {
        this.countingMetadata = countingMetadata
        sendResult()
        finish()
    }

    private fun sendResult() {
        setResult(Activity.RESULT_OK,
            Intent().apply {
                putExtra(
                    EXTRA_COUNTING_METADATA,
                    countingMetadata
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

        const val EXTRA_TAXONOMY = "extra_taxonomy"
        const val EXTRA_COUNTING_METADATA = "extra_counting_metadata"
        const val EXTRA_PROPERTIES = "extra_properties"

        fun newIntent(
            context: Context,
            taxonomy: Taxonomy,
            countingMetadata: CountingMetadata? = null,
            vararg propertySettings: PropertySettings
        ): Intent {
            return Intent(
                context,
                EditCountingMetadataActivity::class.java
            ).apply {
                putExtra(
                    EXTRA_TAXONOMY,
                    taxonomy
                )
                countingMetadata?.let {
                    putExtra(
                        EXTRA_COUNTING_METADATA,
                        countingMetadata
                    )
                }
                putExtra(
                    EXTRA_PROPERTIES,
                    propertySettings
                )
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
        }
    }
}
