package fr.geonature.occtax.features.settings.repository

import fr.geonature.occtax.features.settings.domain.AppSettings

/**
 * Reads [AppSettings] locally.
 *
 * @author S. Grimault
 */
interface IAppSettingsRepository {

    /**
     * Loads [AppSettings] from JSON` file.
     *
     * @return [AppSettings] or failure if something goes wrong.
     */
    suspend fun loadAppSettings(): Result<AppSettings>
}