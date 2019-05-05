package fr.geonature.occtax.ui.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import fr.geonature.occtax.BuildConfig
import fr.geonature.occtax.R
import java.text.DateFormat
import java.util.Date

/**
 * Global settings.
 *
 * @see PreferencesFragment
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class PreferencesActivity : AppCompatActivity(),
        PreferencesFragment.OnPreferencesFragmentListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Display the fragment as the main content.
        supportFragmentManager.beginTransaction()
                .replace(android.R.id.content,
                        PreferencesFragment.newInstance())
                .commit()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun getAppVersion(): String {
        return getString(R.string.app_version,
                         BuildConfig.VERSION_NAME,
                         BuildConfig.VERSION_CODE,
                         DateFormat.getDateTimeInstance()
                        .format(Date(BuildConfig.BUILD_DATE.toLong())))
    }

    companion object {

        fun newIntent(context: Context): Intent {
            return Intent(context, PreferencesActivity::class.java)
        }
    }
}
