package fr.geonature.occtax.ui.dataset

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import fr.geonature.commons.data.Dataset
import fr.geonature.occtax.R

/**
 * Let the user to choose a [Dataset] from the list.
 *
 * @see DatasetListFragment
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class DatasetListActivity : AppCompatActivity(),
                            DatasetListFragment.OnDatasetListFragmentListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_toolbar)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Display the fragment as the main content.
        supportFragmentManager.beginTransaction()
            .replace(R.id.container,
                     DatasetListFragment.newInstance(intent.getParcelableExtra(EXTRA_SELECTED_DATASET)))
            .commit()
    }

    override fun onSelectedDataset(dataset: Dataset?) {
        val intent = Intent().apply {
            putExtra(EXTRA_SELECTED_DATASET,
                     dataset)
        }

        setResult(Activity.RESULT_OK,
                  intent)

        finish()
    }

    companion object {

        const val EXTRA_SELECTED_DATASET = "extra_selected_dataset"

        fun newIntent(context: Context,
                      selectedDataset: Dataset? = null): Intent {
            return Intent(context,
                          DatasetListActivity::class.java).apply {
                putExtra(EXTRA_SELECTED_DATASET,
                         selectedDataset)
            }
        }
    }
}
