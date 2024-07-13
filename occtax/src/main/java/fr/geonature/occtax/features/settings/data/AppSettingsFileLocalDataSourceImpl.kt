package fr.geonature.occtax.features.settings.data

import android.content.Context
import fr.geonature.mountpoint.model.MountPoint
import fr.geonature.mountpoint.util.FileUtils.getFile
import fr.geonature.mountpoint.util.FileUtils.getRootFolder
import fr.geonature.occtax.features.settings.domain.AppSettings
import fr.geonature.occtax.features.settings.error.AppSettingsException
import fr.geonature.occtax.features.settings.io.AppSettingsJsonReader
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tinylog.Logger
import java.io.FileReader

/**
 * Loads [AppSettings] from `JSON` file.
 *
 * @author S. Grimault
 */
class AppSettingsFileLocalDataSourceImpl(
    private val context: Context,
    private val appSettingsFilename: String,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : IAppSettingsLocalDataSource {
    override suspend fun load(appSettings: AppSettings?): AppSettings = withContext(dispatcher) {
        val appSettingsJsonFile = getFile(
            getRootFolder(
                context,
                MountPoint.StorageType.INTERNAL
            ),
            if (appSettings == null) appSettingsFilename else "${appSettingsFilename.substringBeforeLast(".json")}.local.json"
        )

        Logger.info { "loading${if (appSettings == null) " " else " additional "}settings from '${appSettingsJsonFile.absolutePath}'..." }

        if (!appSettingsJsonFile.exists()) {
            throw AppSettingsException.NoAppSettingsFoundLocallyException(appSettingsJsonFile.absolutePath)
        }

        run {
            AppSettingsJsonReader(appSettings).read(FileReader(appSettingsJsonFile))
        }
    }
}