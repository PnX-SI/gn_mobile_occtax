package fr.geonature.occtax.ui.input.taxa

import android.os.Parcel
import android.os.Parcelable

/**
 * Name filter.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class FilterName(value: Name) : Filter<FilterName.Name>(
    FilterType.NAME,
    value
) {
    class Name(val type: NameType) :
        Parcelable {

        private constructor(source: Parcel) : this(
            NameType.valueOf(source.readString() ?: NameType.SCIENTIFIC.name)
        )

        override fun describeContents(): Int {
            return 0
        }

        override fun writeToParcel(dest: Parcel?, flags: Int) {
            dest?.also {
                it.writeString(type.name)
            }
        }

        companion object CREATOR : Parcelable.Creator<Name> {
            override fun createFromParcel(parcel: Parcel): Name {
                return Name(parcel)
            }

            override fun newArray(size: Int): Array<Name?> {
                return arrayOfNulls(size)
            }
        }
    }

    /**
     * Taxon name types.
     *
     * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
     */
    enum class NameType {
        SCIENTIFIC,
        COMMON
    }
}