package fr.geonature.occtax.settings

import android.os.Parcel
import android.os.Parcelable
import fr.geonature.occtax.input.Input

/**
 * [Input] date settings.
 *
 * @author S. Grimault
 */
data class InputDateSettings(
    val startDateSettings: DateSettings? = null,
    val endDateSettings: DateSettings? = null
) : Parcelable {
    private constructor(parcel: Parcel) : this(
        parcel.readString().let { dateSettingsAsString ->
            DateSettings
                .values()
                .firstOrNull { it.name == dateSettingsAsString }
        },
        parcel.readString().let { dateSettingsAsString ->
            DateSettings
                .values()
                .firstOrNull { it.name == dateSettingsAsString }
        }
    )

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.also {
            it.writeString(startDateSettings?.name)
            it.writeString(endDateSettings?.name)
        }
    }

    companion object {
        val DEFAULT = InputDateSettings(startDateSettings = DateSettings.DATE)
        
        @JvmField
        val CREATOR: Parcelable.Creator<InputDateSettings> =
            object : Parcelable.Creator<InputDateSettings> {
                override fun createFromParcel(parcel: Parcel): InputDateSettings {
                    return InputDateSettings(parcel)
                }

                override fun newArray(size: Int): Array<InputDateSettings?> {
                    return arrayOfNulls(size)
                }
            }
    }

    enum class DateSettings {
        DATE,
        DATETIME
    }
}
