package fr.geonature.occtax.ui.input.map

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import fr.geonature.compat.content.getParcelableExtraCompat
import fr.geonature.maps.settings.MapSettings

/**
 * Map settings.
 *
 * @author S. Grimault
 * @see MapPreferencesFragment
 */
class MapPreferencesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val mapSettings: MapSettings? = intent.getParcelableExtraCompat(EXTRA_MAP_SETTINGS)

        // display the fragment as the main content
        with(supportFragmentManager.beginTransaction()) {
            replace(
                android.R.id.content,
                MapPreferencesFragment.newInstance(mapSettings)
            )
            commit()
        }
    }

    override fun finish() {
        setResult(Activity.RESULT_OK)
        super.finish()
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

    companion object {

        private const val EXTRA_MAP_SETTINGS = "extra_map_settings"

        fun newIntent(context: Context, mapSettings: MapSettings? = null): Intent {
            return Intent(
                context,
                MapPreferencesActivity::class.java
            ).apply {
                putExtra(
                    EXTRA_MAP_SETTINGS,
                    mapSettings
                )
            }
        }
    }
}
