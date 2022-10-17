package fr.geonature.occtax.features.input.domain

import android.os.Parcel
import android.os.Parcelable
import okhttp3.internal.toImmutableList
import java.util.SortedMap
import java.util.TreeMap

/**
 * Counting metadata.
 *
 * @author S. Grimault
 */
data class CountingMetadata(
    val index: Int = 0,
    val properties: SortedMap<String, PropertyValue> = TreeMap { o1, o2 -> o1.compareTo(o2) }
) : Parcelable {

    constructor(source: Parcel) : this(
        source.readInt(),
        (source.createTypedArrayList(PropertyValue.CREATOR)
            ?: listOf<PropertyValue>()).toImmutableList().associateBy { it.code }.toSortedMap()
    )

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(
        dest: Parcel?,
        flags: Int
    ) {
        dest?.also {
            it.writeInt(index)
            it.writeTypedList(this.properties.values.toList())
        }
    }

    fun isEmpty(): Boolean {
        return properties.filterNot { it.value.isEmpty() }
            .isEmpty()
    }

    companion object {

        @JvmField
        val CREATOR: Parcelable.Creator<CountingMetadata> =
            object : Parcelable.Creator<CountingMetadata> {

                override fun createFromParcel(source: Parcel): CountingMetadata {
                    return CountingMetadata(source)
                }

                override fun newArray(size: Int): Array<CountingMetadata?> {
                    return arrayOfNulls(size)
                }
            }
    }
}
