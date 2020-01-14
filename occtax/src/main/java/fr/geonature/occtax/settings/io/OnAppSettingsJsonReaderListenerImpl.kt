package fr.geonature.occtax.settings.io

import android.util.JsonReader
import android.util.JsonToken
import fr.geonature.commons.settings.io.AppSettingsJsonReader
import fr.geonature.maps.settings.io.MapSettingsReader
import fr.geonature.occtax.settings.AppSettings

/**
 * Default implementation of [AppSettingsJsonReader.OnAppSettingsJsonReaderListener].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class OnAppSettingsJsonReaderListenerImpl : AppSettingsJsonReader.OnAppSettingsJsonReaderListener<AppSettings> {

    override fun createAppSettings(): AppSettings {
        return AppSettings()
    }

    override fun readAdditionalAppSettingsData(
        reader: JsonReader,
        keyName: String,
        appSettings: AppSettings
    ) {
        when (keyName) {
            "map" -> {
                if (reader.peek() == JsonToken.BEGIN_OBJECT) {
                    readMapSettings(reader,
                                    appSettings)
                } else {
                    reader.skipValue()
                }
            }
            else -> reader.skipValue()
        }
    }

    private fun readMapSettings(
        reader: JsonReader,
        appSettings: AppSettings
    ) {
        appSettings.mapSettings = MapSettingsReader().read(reader)
    }
}
