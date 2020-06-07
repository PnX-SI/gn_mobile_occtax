package fr.geonature.occtax.ui.input.taxa

import android.os.Parcel
import android.os.Parcelable

/**
 * Describes a filter entry.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
open class Filter<T : Parcelable>(
    val type: FilterType,
    val value: T
) : Parcelable {

    private constructor(source: Parcel) : this(
        enumValues<FilterType>()[source.readInt()],
        source.readParcelable(Filter<T>::value.javaClass.classLoader)!!
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Filter<*>) return false

        if (type != other.type) return false
        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + value.hashCode()

        return result
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(
        dest: Parcel?,
        flags: Int
    ) {
        dest?.also {
            it.writeInt(type.ordinal)
            it.writeParcelable(
                value,
                0
            )
        }
    }

    /**
     * Describes a filter type.
     *
     * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
     */
    enum class FilterType {
        AREA_OBSERVATION,
        TAXONOMY
    }

    companion object CREATOR : Parcelable.Creator<Filter<Parcelable>> {
        override fun createFromParcel(parcel: Parcel): Filter<Parcelable> {
            return Filter(parcel)
        }

        override fun newArray(size: Int): Array<Filter<Parcelable>?> {
            return arrayOfNulls(size)
        }
    }
}

