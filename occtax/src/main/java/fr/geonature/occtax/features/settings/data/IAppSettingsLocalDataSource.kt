package fr.geonature.occtax.features.settings.data

import fr.geonature.occtax.features.settings.error.AppSettingsException
import fr.geonature.occtax.features.settings.domain.AppSettings

/**
 * [AppSettings] local data source.
 *
 * @author S. Grimault
 */
interface IAppSettingsLocalDataSource {

    /**
     * Loads [AppSettings].
     *
     * @param appSettings existing [AppSettings] to override.
     * @return [AppSettings] or throws [AppSettingsException] if something goes wrong.
     */
    suspend fun load(appSettings: AppSettings? = null): AppSettings
}