package fr.geonature.occtax.ui.home

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import fr.geonature.occtax.input.Input
import fr.geonature.occtax.settings.AppSettings
import fr.geonature.occtax.ui.input.InputPagerFragmentActivity
import fr.geonature.occtax.ui.settings.PreferencesActivity
import fr.geonature.occtax.util.IntentUtils.syncActivity

/**
 * Home screen Activity.
 *
 * @see HomeFragment
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class HomeActivity : AppCompatActivity(),
    HomeFragment.OnHomeFragmentListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(
                    android.R.id.content,
                    HomeFragment.newInstance()
                )
                .commit()
        }
    }

    override fun onShowSettings() {
        startActivity(PreferencesActivity.newIntent(this))
    }

    override fun onStartSync() {
        syncActivity(this)?.also {
            startActivity(it)
        }
    }

    override fun onStartInput(
        appSettings: AppSettings,
        input: Input?
    ) {
        startActivity(
            InputPagerFragmentActivity.newIntent(
                this,
                appSettings,
                input
            )
        )
    }
}
