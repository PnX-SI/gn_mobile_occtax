package fr.geonature.occtax.settings

import android.os.Parcel
import android.os.Parcelable
import fr.geonature.occtax.features.input.domain.Input

/**
 * [Input] settings.
 *
 * @author S. Grimault
 */
data class InputSettings(val dateSettings: InputDateSettings) : Parcelable {
    private constructor(source: Parcel) : this(
        source.readParcelable(InputDateSettings::class.java.classLoader) as InputDateSettings?
            ?: InputDateSettings.DEFAULT
    )

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(
        dest: Parcel?,
        flags: Int
    ) {
        dest?.also {
            it.writeParcelable(
                dateSettings,
                0
            )
        }
    }

    companion object CREATOR : Parcelable.Creator<InputSettings> {
        override fun createFromParcel(parcel: Parcel): InputSettings {
            return InputSettings(parcel)
        }

        override fun newArray(size: Int): Array<InputSettings?> {
            return arrayOfNulls(size)
        }
    }
}
