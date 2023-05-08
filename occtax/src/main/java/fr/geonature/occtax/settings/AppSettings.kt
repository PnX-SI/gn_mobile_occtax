package fr.geonature.occtax.settings

import fr.geonature.commons.settings.IAppSettings
import fr.geonature.datasync.settings.DataSyncSettings
import fr.geonature.maps.settings.MapSettings
import kotlinx.parcelize.Parcelize

/**
 * Global internal settings.
 *
 * @author S. Grimault
 */
@Parcelize
data class AppSettings(
    var areaObservationDuration: Int = DEFAULT_AREA_OBSERVATION_DURATION,
    var inputSettings: InputSettings = InputSettings(dateSettings = InputDateSettings.DEFAULT),
    var dataSyncSettings: DataSyncSettings? = null,
    var mapSettings: MapSettings? = null,
    var nomenclatureSettings: NomenclatureSettings? = null
) : IAppSettings {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AppSettings) return false

        if (areaObservationDuration != other.areaObservationDuration) return false
        if (inputSettings != other.inputSettings) return false
        if (dataSyncSettings != other.dataSyncSettings) return false
        if (mapSettings != other.mapSettings) return false
        if (nomenclatureSettings != other.nomenclatureSettings) return false

        return true
    }

    override fun hashCode(): Int {
        var result = areaObservationDuration
        result = 31 * result + (inputSettings.hashCode())
        result = 31 * result + (dataSyncSettings.hashCode())
        result = 31 * result + (mapSettings.hashCode())
        result = 31 * result + (nomenclatureSettings.hashCode())

        return result
    }

    companion object {
        const val DEFAULT_AREA_OBSERVATION_DURATION = 365
    }
}
