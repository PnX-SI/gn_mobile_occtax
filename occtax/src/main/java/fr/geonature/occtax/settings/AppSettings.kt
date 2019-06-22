package fr.geonature.occtax.settings

import android.os.Parcel
import android.os.Parcelable
import fr.geonature.commons.settings.IAppSettings
import fr.geonature.maps.settings.MapSettings

/**
 * Global internal settings.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
data class AppSettings(var mapSettings: MapSettings? = null) : IAppSettings {

    private constructor(source: Parcel) : this(source.readParcelable(MapSettings::class.java.classLoader) as MapSettings)

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel?,
                               flags: Int) {
        dest?.writeParcelable(
                mapSettings,
                0
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AppSettings

        if (mapSettings != other.mapSettings) return false

        return true
    }

    override fun hashCode(): Int {
        return mapSettings.hashCode()
    }

    companion object CREATOR : Parcelable.Creator<AppSettings> {
        override fun createFromParcel(parcel: Parcel): AppSettings {
            return AppSettings(parcel)
        }

        override fun newArray(size: Int): Array<AppSettings?> {
            return arrayOfNulls(size)
        }
    }
}