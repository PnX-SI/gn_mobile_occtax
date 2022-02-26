package fr.geonature.occtax.ui.observers

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import fr.geonature.commons.data.entity.InputObserver
import fr.geonature.occtax.R

/**
 * Let the user to choose an [InputObserver] from the list.
 *
 * @see InputObserverListFragment
 *
 * @author S. Grimault
 */
@AndroidEntryPoint
class InputObserverListActivity : AppCompatActivity(),
    InputObserverListFragment.OnInputObserverListFragmentListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_toolbar)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Display the fragment as the main content.
        supportFragmentManager.beginTransaction()
            .replace(
                R.id.container,
                InputObserverListFragment.newInstance(
                    intent.getIntExtra(
                        EXTRA_CHOICE_MODE,
                        ListView.CHOICE_MODE_SINGLE
                    ),
                    intent.getParcelableArrayListExtra(EXTRA_SELECTED_INPUT_OBSERVERS)
                        ?: emptyList()
                )
            )
            .commit()
    }

    override fun onSelectedInputObservers(inputObservers: List<InputObserver>) {
        val intent = Intent().apply {
            putParcelableArrayListExtra(
                EXTRA_SELECTED_INPUT_OBSERVERS,
                ArrayList(inputObservers)
            )
        }

        setResult(
            Activity.RESULT_OK,
            intent
        )

        finish()
    }

    companion object {

        const val EXTRA_CHOICE_MODE = "extra_choice_mode"
        const val EXTRA_SELECTED_INPUT_OBSERVERS = "extra_selected_input_observers"

        fun newIntent(
            context: Context,
            choiceMode: Int = ListView.CHOICE_MODE_SINGLE,
            selectedObservers: List<InputObserver> = listOf()
        ): Intent {
            return Intent(
                context,
                InputObserverListActivity::class.java
            ).apply {
                putExtra(
                    EXTRA_CHOICE_MODE,
                    choiceMode
                )
                putParcelableArrayListExtra(
                    EXTRA_SELECTED_INPUT_OBSERVERS,
                    ArrayList(selectedObservers)
                )
            }
        }
    }
}
