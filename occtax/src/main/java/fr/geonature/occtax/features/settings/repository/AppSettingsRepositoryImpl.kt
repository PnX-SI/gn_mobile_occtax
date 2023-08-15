package fr.geonature.occtax.features.settings.repository

import fr.geonature.occtax.features.settings.data.IAppSettingsLocalDataSource
import fr.geonature.occtax.features.settings.domain.AppSettings
import fr.geonature.occtax.features.settings.error.AppSettingsException
import org.tinylog.Logger

/**
 * Default implementation of [IAppSettingsRepository].
 *
 * @author S. Grimault
 */
class AppSettingsRepositoryImpl(
    private val appSettingsLocalDataSource: IAppSettingsLocalDataSource
) :
    IAppSettingsRepository {
    override suspend fun loadAppSettings(): Result<AppSettings> {
        // first: tries to load locally app settings coming from remote
        val appSettingsResult = runCatching { appSettingsLocalDataSource.load() }.onFailure {
            it.message?.also { Logger.warn { it } }
        }

        if (appSettingsResult.isFailure) {
            return appSettingsResult
        }

        val appSettings = appSettingsResult.getOrNull()
            ?: return Result.failure(AppSettingsException.NotFoundException)

        // then: tries to override this app settings from additional user settings
        val additionalAppSettingsResult =
            runCatching { appSettingsLocalDataSource.load(appSettings) }.onFailure {
                when (it) {
                    is AppSettingsException.NoAppSettingsFoundLocallyException, is AppSettingsException.NotFoundException -> {
                        it.message?.also { Logger.info { it } }
                    }
                    is AppSettingsException.MissingAttributeException, is AppSettingsException.JsonParseException -> {
                        it.message?.also { Logger.warn { it } }
                    }
                }
            }

        // use default app settings as fallback
        if (additionalAppSettingsResult.isFailure) {
            Logger.info { "default app settings successfully loaded" }
            return Result.success(appSettings)
        }

        return Result.success(additionalAppSettingsResult.getOrNull() ?: appSettings)
            .also {
                Logger.info { "app settings successfully loaded" }
            }
    }
}