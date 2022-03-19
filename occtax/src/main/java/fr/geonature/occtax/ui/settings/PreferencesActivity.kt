package fr.geonature.occtax.ui.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import fr.geonature.commons.fp.getOrElse
import fr.geonature.datasync.api.IGeoNatureAPIClient
import fr.geonature.datasync.settings.DataSyncSettingsViewModel
import fr.geonature.occtax.BuildConfig
import fr.geonature.occtax.R
import fr.geonature.occtax.settings.AppSettings
import java.text.DateFormat
import java.util.Date

/**
 * Global settings.
 *
 * @see PreferencesFragment
 *
 * @author S. Grimault
 */
@AndroidEntryPoint
class PreferencesActivity : AppCompatActivity(),
    PreferencesFragment.OnPreferencesFragmentListener {

    private val dataSyncSettingsViewModel: DataSyncSettingsViewModel by viewModels()

    private var serverUrls: IGeoNatureAPIClient.ServerUrls? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val appSettings: AppSettings? = intent.getParcelableExtra(EXTRA_APP_SETTINGS)

        serverUrls = dataSyncSettingsViewModel
            .getServerBaseUrls()
            .getOrElse(null)

        // Display the fragment as the main content.
        supportFragmentManager.beginTransaction()
            .replace(
                android.R.id.content,
                PreferencesFragment.newInstance(
                    serverUrls,
                    appSettings?.mapSettings
                )
            )
            .commit()
    }

    override fun finish() {
        val currentServerUrls = dataSyncSettingsViewModel
            .getServerBaseUrls()
            .getOrElse(null)

        if (currentServerUrls != null) {
            dataSyncSettingsViewModel.setServerBaseUrls(
                geoNatureServerUrl = currentServerUrls.geoNatureBaseUrl,
                taxHubServerUrl = currentServerUrls.taxHubBaseUrl
            )
        }

        setResult(
            if (serverUrls != currentServerUrls) RESULT_OK
            else RESULT_CANCELED
        )

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

    override fun getAppVersion(): String {
        return getString(
            R.string.app_version,
            BuildConfig.VERSION_NAME,
            BuildConfig.VERSION_CODE,
            DateFormat.getDateTimeInstance()
                .format(Date(BuildConfig.BUILD_DATE.toLong()))
        )
    }

    companion object {

        private const val EXTRA_APP_SETTINGS = "extra_app_settings"

        fun newIntent(context: Context, appSettings: AppSettings? = null): Intent {
            return Intent(
                context,
                PreferencesActivity::class.java
            ).apply {
                putExtra(
                    EXTRA_APP_SETTINGS,
                    appSettings
                )
            }
        }
    }
}
