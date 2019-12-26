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

    val properties: SortedMap<String, PropertyValue> =
        TreeMap<String, PropertyValue>(Comparator { o1, o2 ->
            val i1 = defaultMnemonic.indexOfFirst { it.first == o1 }
            val i2 = defaultMnemonic.indexOfFirst { it.first == o2 }

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

    override fun writeToParcel(
        dest: Parcel?,
        flags: Int
    ) {
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
        return properties.filterNot { it.value.isEmpty() }
            .isEmpty()
    }

    companion object {

        val defaultMnemonic = arrayOf(
            Pair(
                "STADE_VIE",
                NomenclatureTypeViewType.NOMENCLATURE_TYPE
            ),
            Pair(
                "SEXE",
                NomenclatureTypeViewType.NOMENCLATURE_TYPE
            ),
            Pair(
                "OBJ_DENBR",
                NomenclatureTypeViewType.NOMENCLATURE_TYPE
            ),
            Pair(
                "TYP_DENBR",
                NomenclatureTypeViewType.NOMENCLATURE_TYPE
            ),
            Pair(
                "MIN",
                NomenclatureTypeViewType.MIN_MAX
            ),
            Pair(
                "MAX",
                NomenclatureTypeViewType.MIN_MAX
            )
        )

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
