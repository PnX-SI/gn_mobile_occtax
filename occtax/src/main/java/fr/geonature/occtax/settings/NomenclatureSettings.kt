package fr.geonature.occtax.settings

import android.os.Parcel
import android.os.Parcelable
import androidx.core.os.ParcelCompat
import androidx.core.os.ParcelCompat.readBoolean

/**
 * Nomenclature settings.
 *
 * @author S. Grimault
 */
data class NomenclatureSettings(
    val saveDefaultValues: Boolean = false,
    val information: List<PropertySettings>,
    val counting: List<PropertySettings>
) : Parcelable {
    private constructor(source: Parcel) : this(
        readBoolean(source),
        mutableListOf(),
        mutableListOf()
    ) {
        source.readTypedList(
            information,
            PropertySettings.CREATOR
        )
        source.readTypedList(
            counting,
            PropertySettings.CREATOR
        )
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(
        dest: Parcel?,
        flags: Int
    ) {
        dest?.also {
            ParcelCompat.writeBoolean(
                it,
                saveDefaultValues
            )
            it.writeTypedList(information)
            it.writeTypedList(counting)
        }
    }

    companion object CREATOR : Parcelable.Creator<NomenclatureSettings> {
        override fun createFromParcel(parcel: Parcel): NomenclatureSettings {
            return NomenclatureSettings(parcel)
        }

        override fun newArray(size: Int): Array<NomenclatureSettings?> {
            return arrayOfNulls(size)
        }
    }
}