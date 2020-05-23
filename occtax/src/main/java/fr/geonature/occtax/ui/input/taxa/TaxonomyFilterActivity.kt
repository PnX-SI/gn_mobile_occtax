package fr.geonature.occtax.ui.input.taxa

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import fr.geonature.commons.data.Taxonomy

/**
 * Apply filters on taxa list.
 *
 * @see TaxonomyFilterFragment
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class TaxonomyFilterActivity : AppCompatActivity(),
    TaxonomyFilterFragment.OnTaxaFilterFragmentListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(
                    android.R.id.content,
                    TaxonomyFilterFragment.newInstance(
                        intent.getParcelableExtra(
                            EXTRA_SELECTED_TAXONOMY
                        )
                    )
                )
                .commit()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSelectedTaxonomy(taxonomy: Taxonomy) {
        setResultAndFinish(taxonomy)
    }

    override fun onNoTaxonomySelected() {
        setResultAndFinish(null)
    }

    private fun setResultAndFinish(taxonomy: Taxonomy?) {
        val intent = Intent().also {
            if (taxonomy != null) {
                it.putExtra(EXTRA_SELECTED_TAXONOMY, taxonomy)
            }
        }

        setResult(
            Activity.RESULT_OK,
            intent
        )

        finish()
    }

    companion object {

        const val EXTRA_SELECTED_TAXONOMY = "extra_selected_taxonomy"

        fun newIntent(context: Context, selectedTaxonomy: Taxonomy? = null): Intent {
            return Intent(
                context,
                TaxonomyFilterActivity::class.java
            ).apply { putExtra(EXTRA_SELECTED_TAXONOMY, selectedTaxonomy) }
        }
    }
}