package fr.geonature.occtax.features.settings.domain

import android.os.Parcelable
import fr.geonature.datasync.settings.DataSyncSettings
import fr.geonature.maps.settings.MapSettings
import fr.geonature.occtax.features.settings.error.AppSettingsException
import kotlinx.parcelize.Parcelize

/**
 * Global internal settings.
 *
 * @author S. Grimault
 */
@Parcelize
data class AppSettings(
    val areaObservationDuration: Int = Builder().areaObservationDuration,
    val inputSettings: InputSettings = Builder().inputSettings,
    val dataSyncSettings: DataSyncSettings,
    val mapSettings: MapSettings,
    val nomenclatureSettings: NomenclatureSettings? = null
) : Parcelable {

    class Builder {

        /**
         * Area observation duration period (in days). Default: 365 days.
         */
        internal var areaObservationDuration: Int = DEFAULT_AREA_OBSERVATION_DURATION
            private set

        /**
         * Observation record form settings.
         */
        internal var inputSettings: InputSettings =
            InputSettings(dateSettings = InputDateSettings.DEFAULT)
            private set

        /**
         * Data synchronization settings.
         */
        private var dataSyncSettings: DataSyncSettings? = null

        /**
         * Maps settings.
         */
        private var mapSettings: MapSettings? = null

        /**
         * Nomenclature settings.
         */
        private var nomenclatureSettings: NomenclatureSettings? = null

        /**
         * Makes a copy of given [AppSettings].
         */
        fun from(appSettings: AppSettings?) =
            apply {
                if (appSettings == null) return@apply

                this.areaObservationDuration = appSettings.areaObservationDuration
                this.inputSettings = appSettings.inputSettings
                this.dataSyncSettings = appSettings.dataSyncSettings
                this.mapSettings = appSettings.mapSettings
                this.nomenclatureSettings = appSettings.nomenclatureSettings
            }

        fun areaObservationDuration(areaObservationDuration: Int) =
            apply { this.areaObservationDuration = areaObservationDuration }

        fun inputSettings(inputSettings: InputSettings) =
            apply { this.inputSettings = inputSettings }

        fun dataSyncSettings(dataSyncSettings: DataSyncSettings) =
            apply { this.dataSyncSettings = dataSyncSettings }

        fun mapSettings(mapSettings: MapSettings) =
            apply { this.mapSettings = mapSettings }

        fun nomenclatureSettings(nomenclatureSettings: NomenclatureSettings) =
            apply { this.nomenclatureSettings = nomenclatureSettings }

        /**
         * Builds a new instance of [AppSettings].
         */
        fun build(): AppSettings {
            val dataSyncSettings = this.dataSyncSettings
                ?: throw AppSettingsException.MissingAttributeException("sync")
            val mapSettings =
                this.mapSettings ?: throw AppSettingsException.MissingAttributeException("map")

            return AppSettings(
                this.areaObservationDuration,
                this.inputSettings,
                dataSyncSettings,
                mapSettings,
                this.nomenclatureSettings
            )
        }

        companion object {
            const val DEFAULT_AREA_OBSERVATION_DURATION = 365
        }
    }
}
