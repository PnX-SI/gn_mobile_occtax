package fr.geonature.occtax.ui.input.taxa

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import fr.geonature.occtax.settings.AppSettings

/**
 * Apply filters on taxa list.
 *
 * @see TaxaFilterFragment
 *
 * @author S. Grimault
 */
@AndroidEntryPoint
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
                    TaxaFilterFragment.newInstance(
                        intent.getBooleanExtra(
                            EXTRA_WITH_AREA_OBSERVATION,
                            false
                        ),
                        intent.getIntExtra(
                            EXTRA_AREA_OBSERVATION_DURATION,
                            AppSettings.DEFAULT_AREA_OBSERVATION_DURATION
                        ),
                        * selectedFilters.toTypedArray()
                    )
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
        const val EXTRA_WITH_AREA_OBSERVATION = "extra_with_area_observation"
        const val EXTRA_AREA_OBSERVATION_DURATION = "extra_area_observation_duration"

        fun newIntent(
            context: Context,
            withAreaObservation: Boolean = false,
            areaObservationDuration: Int = AppSettings.DEFAULT_AREA_OBSERVATION_DURATION,
            vararg filter: Filter<*>
        ): Intent {
            return Intent(
                context,
                TaxaFilterActivity::class.java
            ).apply {
                putExtra(
                    EXTRA_WITH_AREA_OBSERVATION,
                    withAreaObservation
                )
                putExtra(
                    EXTRA_AREA_OBSERVATION_DURATION,
                    areaObservationDuration
                )
                putExtra(
                    EXTRA_SELECTED_FILTERS,
                    filter
                )
            }
        }
    }
}