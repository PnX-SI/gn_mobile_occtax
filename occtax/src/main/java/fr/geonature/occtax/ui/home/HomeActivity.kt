package fr.geonature.occtax.ui.home

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import fr.geonature.commons.input.InputManager
import fr.geonature.commons.settings.AppSettingsManager
import fr.geonature.occtax.input.Input
import fr.geonature.occtax.input.io.OnInputJsonReaderListenerImpl
import fr.geonature.occtax.input.io.OnInputJsonWriterListenerImpl
import fr.geonature.occtax.settings.AppSettings
import fr.geonature.occtax.settings.io.OnAppSettingsJsonReaderListenerImpl
import fr.geonature.occtax.ui.input.InputPagerFragmentActivity
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
                     HomeFragment.OnHomeFragmentListener {

    private lateinit var inputManager: InputManager<Input>
    private lateinit var appSettingsManager: AppSettingsManager<AppSettings>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        inputManager = InputManager(application,
                                    OnInputJsonReaderListenerImpl(),
                                    OnInputJsonWriterListenerImpl())
        appSettingsManager = AppSettingsManager(application,
                                                OnAppSettingsJsonReaderListenerImpl())

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(android.R.id.content,
                         HomeFragment.newInstance())
                .commit()
        }
    }

    override fun getInputManager(): InputManager<Input> {
        return inputManager
    }

    override fun getAppSettingsManager(): AppSettingsManager<AppSettings> {
        return appSettingsManager
    }

    override fun onShowSettings() {
        startActivity(PreferencesActivity.newIntent(this))
    }

    override fun onStartSync() {
        startActivity(IntentUtils.syncActivity(this))
    }

    override fun onStartInput(input: Input?) {
        startActivity(InputPagerFragmentActivity.newIntent(this,
                                                           input))
    }
}
