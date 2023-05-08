package fr.geonature.occtax.ui.input.taxa

import android.os.Parcel
import android.os.Parcelable

/**
 * Area observation filter.
 *
 * @author S. Grimault
 */
class FilterAreaObservation(value: AreaObservation) : Filter<FilterAreaObservation.AreaObservation>(
    FilterType.AREA_OBSERVATION,
    value
) {

    class AreaObservation(val type: AreaObservationType, val label: String, val short: String) :
        Parcelable {

        private constructor(source: Parcel) : this(
            AreaObservationType.valueOf(source.readString() ?: AreaObservationType.NONE.name),
            source.readString()!!,
            source.readString()!!
        )

        override fun describeContents(): Int {
            return 0
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            dest.also {
                it.writeString(type.name)
                it.writeString(label)
                it.writeString(short)
            }
        }

        companion object CREATOR : Parcelable.Creator<AreaObservation> {
            override fun createFromParcel(parcel: Parcel): AreaObservation {
                return AreaObservation(parcel)
            }

            override fun newArray(size: Int): Array<AreaObservation?> {
                return arrayOfNulls(size)
            }
        }
    }

    /**
     * Describes an observation type.
     *
     * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
     */
    enum class AreaObservationType {
        MORE_THAN_DURATION,
        LESS_THAN_DURATION,
        NONE
    }
}