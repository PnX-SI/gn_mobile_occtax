package fr.geonature.occtax.features.settings.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.geonature.commons.interactor.BaseResultUseCase
import fr.geonature.commons.lifecycle.BaseViewModel
import fr.geonature.occtax.features.settings.domain.AppSettings
import fr.geonature.occtax.features.settings.usecase.LoadAppSettingsLocallyUseCase
import javax.inject.Inject

/**
 * [AppSettings] view model.
 *
 * @author S. Grimault
 */
@HiltViewModel
class AppSettingsViewModel @Inject constructor(
    private val loadAppSettingsLocallyUseCase: LoadAppSettingsLocallyUseCase
) : BaseViewModel() {

    private val _appSettings = MutableLiveData<AppSettings>()
    val appSettings: LiveData<AppSettings> = _appSettings

    fun loadAppSettings() {
        loadAppSettingsLocallyUseCase(
            BaseResultUseCase.None(),
            viewModelScope
        ) {
            it.fold(
                onSuccess = { appSettings -> _appSettings.value = appSettings },
                ::handleError
            )
        }
    }
}