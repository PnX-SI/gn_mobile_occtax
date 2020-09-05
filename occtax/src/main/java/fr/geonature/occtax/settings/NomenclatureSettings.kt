package fr.geonature.occtax.settings

import android.os.Parcel
import android.os.Parcelable

/**
 * Nomenclature settings.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
data class NomenclatureSettings(
    val information: List<PropertySettings>,
    val counting: List<PropertySettings>
) : Parcelable {
    private constructor(source: Parcel) : this(
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