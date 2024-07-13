package fr.geonature.occtax.features.settings.usecase

import fr.geonature.commons.interactor.BaseResultUseCase
import fr.geonature.occtax.features.settings.domain.AppSettings
import fr.geonature.occtax.features.settings.repository.IAppSettingsRepository
import javax.inject.Inject

/**
 * Loads [AppSettings] locally.
 *
 * @author S. Grimault
 */
class LoadAppSettingsLocallyUseCase @Inject constructor(
    private val appSettingsRepository: IAppSettingsRepository
) : BaseResultUseCase<AppSettings, BaseResultUseCase.None>() {
    override suspend fun run(params: None): Result<AppSettings> {
        return appSettingsRepository.loadAppSettings()
    }
}