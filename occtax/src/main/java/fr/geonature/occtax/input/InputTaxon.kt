package fr.geonature.occtax.input

import android.os.Parcel
import android.os.Parcelable
import fr.geonature.commons.data.AbstractTaxon
import fr.geonature.commons.input.AbstractInputTaxon
import java.util.SortedMap
import java.util.TreeMap

/**
 * Describes an input taxon.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class InputTaxon : AbstractInputTaxon {

    val properties: SortedMap<String, PropertyValue> =
        TreeMap<String, PropertyValue>(Comparator { o1, o2 ->
            val i1 = defaultPropertiesMnemonic.indexOfFirst { it.first == o1 }
            val i2 = defaultPropertiesMnemonic.indexOfFirst { it.first == o2 }

            when {
                i1 == -1 -> 1
                i2 == -1 -> -1
                else -> i1 - i2
            }
        })
    private val counting: SortedMap<Int, CountingMetadata> = TreeMap<Int, CountingMetadata>()

    constructor(taxon: AbstractTaxon) : super(taxon)
    constructor(source: Parcel) : super(source) {
        (source.createTypedArrayList(PropertyValue.CREATOR)
            ?: emptyList<PropertyValue>())
            .forEach {
                this.properties[it.code] = it
            }

        val countLingAsList = mutableListOf<CountingMetadata>()
        source.readTypedList(
            countLingAsList,
            CountingMetadata.CREATOR
        )
        countLingAsList.forEach { counting[it.index] = it }
    }

    override fun writeToParcel(
        dest: Parcel?,
        flags: Int
    ) {
        super.writeToParcel(
            dest,
            flags
        )

        dest?.also {
            it.writeTypedList(this.properties.values.toList())
            it.writeTypedList(getCounting())
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is InputTaxon) return false
        if (!super.equals(other)) return false

        if (properties != other.properties) return false
        if (counting != other.counting) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + properties.hashCode()
        result = 31 * result + counting.hashCode()

        return result
    }

    fun getCounting(): List<CountingMetadata> {
        return counting.values.toList()
    }

    fun addCountingMetadata(countingMetadata: CountingMetadata) {
        if (countingMetadata.isEmpty()) return

        val index = if (countingMetadata.index > 0) countingMetadata.index
        else this.counting.keys.max()?.plus(1) ?: 1

        counting[index] = countingMetadata.apply { this.index = index }
    }

    fun deleteCountingMetadata(index: Int): CountingMetadata? {
        return counting.remove(index)
    }

    companion object {

        /**
         * default properties as triple:
         *
         * * first value: mnemonic code from nomenclature type
         * * second value: the corresponding view type
         * * third value: if this property is visible by default
         */
        val defaultPropertiesMnemonic = arrayOf(
            Triple(
                "METH_OBS",
                NomenclatureTypeViewType.NOMENCLATURE_TYPE,
                true
            ),
            Triple(
                "ETA_BIO",
                NomenclatureTypeViewType.NOMENCLATURE_TYPE,
                true
            ),
            Triple(
                "METH_DETERMIN",
                NomenclatureTypeViewType.NOMENCLATURE_TYPE,
                false
            ),
            Triple(
                "DETERMINER",
                NomenclatureTypeViewType.TEXT_SIMPLE,
                false
            ),
            Triple(
                "STATUT_BIO",
                NomenclatureTypeViewType.NOMENCLATURE_TYPE,
                false
            ),
            Triple(
                "OCC_COMPORTEMENT",
                NomenclatureTypeViewType.NOMENCLATURE_TYPE,
                false
            ),
            Triple(
                "NATURALITE",
                NomenclatureTypeViewType.NOMENCLATURE_TYPE,
                false
            ),
            Triple(
                "PREUVE_EXIST",
                NomenclatureTypeViewType.NOMENCLATURE_TYPE,
                false
            ),
            Triple(
                "COMMENT",
                NomenclatureTypeViewType.TEXT_MULTIPLE,
                false
            )
        )

        @JvmField
        val CREATOR: Parcelable.Creator<InputTaxon> = object : Parcelable.Creator<InputTaxon> {

            override fun createFromParcel(source: Parcel): InputTaxon {
                return InputTaxon(source)
            }

            override fun newArray(size: Int): Array<InputTaxon?> {
                return arrayOfNulls(size)
            }
        }
    }
}
