package fr.geonature.occtax.ui.observers

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import fr.geonature.commons.data.InputObserver
import fr.geonature.occtax.R

/**
 * Let the user to choose an [InputObserver] from the list.
 *
 * @see InputObserverListFragment
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class InputObserverListActivity : AppCompatActivity(),
        InputObserverListFragment.OnInputObserverListFragmentListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_toolbar)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Display the fragment as the main content.
        supportFragmentManager.beginTransaction()
                .replace(R.id.container, InputObserverListFragment.newInstance())
                .commit()
    }

    override fun onSelectedObserver(inputObserver: InputObserver?) {
        val intent = Intent().apply {
            putExtra(EXTRA_SELECTED_OBSERVER, inputObserver)
        }

        setResult(Activity.RESULT_OK, intent)

        finish()
    }

    companion object {
        const val EXTRA_SELECTED_OBSERVER = "EXTRA_SELECTED_OBSERVER"

        fun newIntent(context: Context): Intent {
            return Intent(context, InputObserverListActivity::class.java)
        }
    }
}
