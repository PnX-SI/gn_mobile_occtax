package fr.geonature.occtax.ui.input.counting

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import fr.geonature.commons.data.Taxonomy
import fr.geonature.occtax.R
import fr.geonature.occtax.input.CountingMetadata

/**
 * Edit additional counting information Activity.
 *
 * @see EditCountingMetadataFragment
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class EditCountingMetadataActivity : AppCompatActivity(),
    EditCountingMetadataFragment.OnEditCountingMetadataFragmentListener {

    private lateinit var countingMetadata: CountingMetadata

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        countingMetadata = intent.getParcelableExtra(EXTRA_COUNTING_METADATA) ?: CountingMetadata()

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
                        countingMetadata
                    )
                )
                .commit()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                sendResult()
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        sendResult()

        super.onBackPressed()
    }

    override fun onCountingMetadata(countingMetadata: CountingMetadata) {
        this.countingMetadata = countingMetadata
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

    companion object {

        const val EXTRA_TAXONOMY = "extra_taxonomy"
        const val EXTRA_COUNTING_METADATA = "extra_counting_metadata"

        fun newIntent(
            context: Context,
            taxonomy: Taxonomy,
            countingMetadata: CountingMetadata? = null
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
            }
        }
    }
}
