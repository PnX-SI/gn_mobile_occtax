package fr.geonature.occtax.settings

import android.app.Application
import fr.geonature.commons.settings.AppSettingsViewModel as BaseAppSettingsViewModel
import fr.geonature.occtax.settings.io.OnAppSettingsJsonReaderListenerImpl

/**
 * [AppSettings] view model.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class AppSettingsViewModel(application: Application) : BaseAppSettingsViewModel<AppSettings>(
    application,
    OnAppSettingsJsonReaderListenerImpl()
)
