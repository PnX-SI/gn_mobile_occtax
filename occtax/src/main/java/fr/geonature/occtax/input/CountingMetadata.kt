package fr.geonature.occtax.input

import android.os.Parcel
import android.os.Parcelable
import java.util.SortedMap
import java.util.TreeMap

/**
 * Counting metadata.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class CountingMetadata() : Parcelable {

    var index: Int = 0
        internal set

    val properties: SortedMap<String, PropertyValue> = TreeMap<String, PropertyValue>(Comparator { o1, o2 ->
        val i1 = defaultMnemonicOrder.indexOfFirst { it == o1 }
        val i2 = defaultMnemonicOrder.indexOfFirst { it == o2 }

        when {
            i1 == -1 -> 1
            i2 == -1 -> -1
            else -> i1 - i2
        }
    })
    var min: Int = 1
    var max: Int = 1

    constructor(source: Parcel) : this() {
        index = source.readInt()
        (source.createTypedArrayList(PropertyValue.CREATOR)
                ?: emptyList<PropertyValue>())
            .forEach {
                this.properties[it.code] = it
            }
        min = source.readInt()
        max = source.readInt()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel?,
                               flags: Int) {
        dest?.also {
            it.writeInt(index)
            it.writeTypedList(this.properties.values.toList())
            it.writeInt(min)
            it.writeInt(max)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CountingMetadata) return false

        if (index != other.index) return false
        if (properties != other.properties) return false
        if (min != other.min) return false
        if (max != other.max) return false

        return true
    }

    override fun hashCode(): Int {
        var result = index
        result = 31 * result + properties.hashCode()
        result = 31 * result + min
        result = 31 * result + max

        return result
    }

    fun isEmpty(): Boolean {
        return properties.filterNot { it.value.isEmpty() }.isEmpty() && min == 0 && max == 0
    }

    companion object {

        private val defaultMnemonicOrder = arrayOf("STADE_VIE",
                                                   "SEXE",
                                                   "OBJ_DENBR",
                                                   "TYP_DENBR")

        @JvmField
        val CREATOR: Parcelable.Creator<CountingMetadata> = object : Parcelable.Creator<CountingMetadata> {

            override fun createFromParcel(source: Parcel): CountingMetadata {
                return CountingMetadata(source)
            }

            override fun newArray(size: Int): Array<CountingMetadata?> {
                return arrayOfNulls(size)
            }
        }
    }
}