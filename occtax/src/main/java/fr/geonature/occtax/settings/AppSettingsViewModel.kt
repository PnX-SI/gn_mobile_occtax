package fr.geonature.occtax.settings

import dagger.hilt.android.lifecycle.HiltViewModel
import fr.geonature.commons.settings.IAppSettingsManager
import javax.inject.Inject
import fr.geonature.commons.settings.AppSettingsViewModel as BaseAppSettingsViewModel

/**
 * [AppSettings] view model.
 *
 * @author S. Grimault
 */
@HiltViewModel
class AppSettingsViewModel @Inject constructor(appSettingsManager: IAppSettingsManager<AppSettings>) :
    BaseAppSettingsViewModel<AppSettings>(
        appSettingsManager
    )
