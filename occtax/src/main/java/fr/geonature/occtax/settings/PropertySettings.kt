package fr.geonature.occtax.settings

import android.os.Parcel
import android.os.Parcelable
import androidx.core.os.ParcelCompat

/**
 * Property settings.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
data class PropertySettings(
    val key: String,
    val visible: Boolean,
    val default: Boolean
): Parcelable {
    private constructor(source: Parcel) : this(
        source.readString()!!,
        ParcelCompat.readBoolean(source),
        ParcelCompat.readBoolean(source)
    )

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(
        dest: Parcel?,
        flags: Int
    ) {
        dest?.also {
            it.writeString(key)
            ParcelCompat.writeBoolean(it, visible)
            ParcelCompat.writeBoolean(it, default)
        }
    }

    companion object CREATOR : Parcelable.Creator<PropertySettings> {
        override fun createFromParcel(parcel: Parcel): PropertySettings {
            return PropertySettings(parcel)
        }

        override fun newArray(size: Int): Array<PropertySettings?> {
            return arrayOfNulls(size)
        }
    }
}