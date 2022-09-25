package fr.geonature.occtax.input

import android.os.Parcel
import android.os.Parcelable
import fr.geonature.commons.input.AbstractInput
import fr.geonature.commons.input.AbstractInputTaxon
import org.locationtech.jts.geom.Geometry
import java.util.SortedMap
import java.util.TreeMap

/**
 * Describes a current input.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class Input : AbstractInput {

    var geometry: Geometry? = null
    var selectedFeatureId: String? = null
    var comment: String? = null
    val properties: SortedMap<String, PropertyValue> =
        TreeMap { o1, o2 ->
            val i1 = defaultPropertiesMnemonic.indexOfFirst { it.first == o1 }
            val i2 = defaultPropertiesMnemonic.indexOfFirst { it.first == o2 }

            when {
                i1 == -1 -> 1
                i2 == -1 -> -1
                else -> i1 - i2
            }
        }

    constructor() : super("occtax")
    constructor(source: Parcel) : super(source) {
        this.geometry = source.readSerializable() as Geometry?
        this.comment = source.readString()
        (source.createTypedArrayList(PropertyValue.CREATOR)
            ?: emptyList<PropertyValue>())
            .forEach {
                this.properties[it.code] = it
            }
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
            it.writeSerializable(geometry)
            it.writeString(comment)
            it.writeTypedList(this.properties.values.toList())
        }
    }

    override fun getTaxaFromParcel(source: Parcel): List<AbstractInputTaxon> {
        val inputTaxa = source.createTypedArrayList(InputTaxon.CREATOR)
        return inputTaxa ?: emptyList()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Input) return false
        if (!super.equals(other)) return false

        if (geometry != other.geometry) return false
        if (comment != other.comment) return false
        if (properties != other.properties) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (geometry?.hashCode() ?: 0)
        result = 31 * result + (comment?.hashCode() ?: 0)
        result = 31 * result + properties.hashCode()

        return result
    }

    companion object {

        @Deprecated("see: INomenclatureSettingsLocalDataSource")
        val defaultPropertiesMnemonic = arrayOf(
            Pair(
                "TYP_GRP",
                NomenclatureTypeViewType.NOMENCLATURE_TYPE
            )
        )

        @JvmField
        val CREATOR: Parcelable.Creator<Input> = object : Parcelable.Creator<Input> {

            override fun createFromParcel(source: Parcel): Input {
                return Input(source)
            }

            override fun newArray(size: Int): Array<Input?> {
                return arrayOfNulls(size)
            }
        }
    }
}
