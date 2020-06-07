package fr.geonature.occtax.ui.input.taxa

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity

/**
 * Apply filters on taxa list.
 *
 * @see TaxaFilterFragment
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class TaxaFilterActivity : AppCompatActivity(), TaxaFilterFragment.OnTaxaFilterFragmentListener {

    private val selectedFilters: MutableList<Filter<*>> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (savedInstanceState == null) {
            selectedFilters.addAll(
                intent.getParcelableArrayExtra(EXTRA_SELECTED_FILTERS)?.map { it as Filter<*> }
                    ?.toTypedArray() ?: emptyArray()
            )

            supportFragmentManager.beginTransaction()
                .replace(
                    android.R.id.content,
                    TaxaFilterFragment.newInstance(*selectedFilters.toTypedArray())
                )
                .commit()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                setResultAndFinish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        setResultAndFinish()
    }

    override fun onSelectedFilters(vararg filter: Filter<*>) {
        selectedFilters.clear()
        selectedFilters.addAll(filter)
    }

    private fun setResultAndFinish() {
        val intent = Intent().also {
            it.putExtra(
                EXTRA_SELECTED_FILTERS,
                selectedFilters.toTypedArray()
            )
        }

        setResult(
            Activity.RESULT_OK,
            intent
        )

        finish()
    }

    companion object {

        const val EXTRA_SELECTED_FILTERS = "extra_selected_filters"

        fun newIntent(context: Context, vararg filter: Filter<*>): Intent {
            return Intent(
                context,
                TaxaFilterActivity::class.java
            ).apply {
                putExtra(
                    EXTRA_SELECTED_FILTERS,
                    filter
                )
            }
        }
    }
}