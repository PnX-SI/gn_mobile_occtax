package fr.geonature.occtax.ui.home

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import fr.geonature.occtax.ui.settings.PreferencesActivity
import fr.geonature.occtax.util.IntentUtils

/**
 * Home screen Activity.
 *
 * @see HomeFragment
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class HomeActivity : AppCompatActivity(),
        HomeFragment.OnHomeFragmentFragmentListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(android.R.id.content, HomeFragment.newInstance())
                    .commit()
        }
    }

    override fun onShowSettings() {
        startActivity(PreferencesActivity.newIntent(this))
    }

    override fun onStartSync() {
        startActivity(IntentUtils.syncActivity(this))
    }

    override fun onStartInput() {
        // TODO: call input activity
        Toast.makeText(this, "Not implemented", Toast.LENGTH_SHORT)
                .show()
    }
}
