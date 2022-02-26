package fr.geonature.occtax.settings.io

import android.util.JsonReader
import android.util.JsonToken
import fr.geonature.commons.settings.io.AppSettingsJsonReader
import fr.geonature.datasync.settings.DataSyncSettings
import fr.geonature.datasync.settings.io.DataSyncSettingsJsonReader
import fr.geonature.maps.settings.MapSettings
import fr.geonature.maps.settings.io.MapSettingsReader
import fr.geonature.occtax.settings.AppSettings
import fr.geonature.occtax.settings.NomenclatureSettings
import fr.geonature.occtax.settings.PropertySettings
import org.tinylog.Logger
import java.io.IOException

/**
 * Default implementation of [AppSettingsJsonReader.OnAppSettingsJsonReaderListener].
 *
 * @author S. Grimault
 */
class OnAppSettingsJsonReaderListenerImpl :
    AppSettingsJsonReader.OnAppSettingsJsonReaderListener<AppSettings> {

    override fun createAppSettings(): AppSettings {
        return AppSettings()
    }

    override fun readAdditionalAppSettingsData(
        reader: JsonReader,
        keyName: String,
        appSettings: AppSettings
    ) {
        when (keyName) {
            "area_observation_duration" -> appSettings.areaObservationDuration = reader.nextInt()
            "sync" -> appSettings.dataSyncSettings = readDataSyncSettings(reader)
            "map" -> appSettings.mapSettings = readMapSettings(reader)
            "nomenclature" -> appSettings.nomenclatureSettings = readNomenclatureSettings(reader)
            else -> reader.skipValue()
        }
    }

    private fun readDataSyncSettings(reader: JsonReader): DataSyncSettings? {
        if (reader.peek() != JsonToken.BEGIN_OBJECT) {
            reader.skipValue()

            return null
        }

        return DataSyncSettingsJsonReader().read(reader)
    }

    private fun readMapSettings(reader: JsonReader): MapSettings? {
        if (reader.peek() != JsonToken.BEGIN_OBJECT) {
            reader.skipValue()

            return null
        }

        return MapSettingsReader().read(reader)
    }

    private fun readNomenclatureSettings(reader: JsonReader): NomenclatureSettings? {
        if (reader.peek() != JsonToken.BEGIN_OBJECT) {
            reader.skipValue()

            return null
        }

        val information = mutableListOf<PropertySettings>()
        val counting = mutableListOf<PropertySettings>()

        reader.beginObject()

        while (reader.hasNext()) {
            when (reader.nextName()) {
                "information" -> information.addAll(readPropertySettingsAsList(reader))
                "counting" -> counting.addAll(readPropertySettingsAsList(reader))
            }
        }

        reader.endObject()

        return NomenclatureSettings(
            information,
            counting
        )
    }

    private fun readPropertySettingsAsList(reader: JsonReader): List<PropertySettings> {
        if (reader.peek() != JsonToken.BEGIN_ARRAY) {
            reader.skipValue()

            return emptyList()
        }

        val propertySettingsList = mutableListOf<PropertySettings>()

        reader.beginArray()

        while (reader.hasNext()) {
            try {
                readPropertySettings(reader)?.also {
                    propertySettingsList.add(it)
                }
            } catch (ioe: IOException) {
                Logger.error(ioe)
            }
        }

        reader.endArray()

        return propertySettingsList
    }

    @Throws(Exception::class)
    private fun readPropertySettings(reader: JsonReader): PropertySettings? {
        return when (reader.peek()) {
            JsonToken.BEGIN_OBJECT -> {
                reader.beginObject()

                var key: String? = null
                var visible = true
                var default = true

                while (reader.hasNext()) {
                    when (reader.nextName()) {
                        "key" -> key = reader.nextString()
                        "visible" -> visible = reader.nextBoolean()
                        "default" -> default = reader.nextBoolean()
                        else -> reader.skipValue()
                    }
                }

                reader.endObject()

                if (key.isNullOrEmpty()) {
                    return null
                }

                return PropertySettings(
                    key,
                    visible,
                    default
                )
            }
            JsonToken.STRING -> PropertySettings(
                reader.nextString(),
                visible = true,
                default = true
            )
            else -> {
                reader.skipValue()
                null
            }
        }
    }
}
