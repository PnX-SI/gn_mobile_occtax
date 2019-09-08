package fr.geonature.occtax.input

import android.os.Parcel
import android.os.Parcelable
import fr.geonature.commons.input.AbstractInput
import fr.geonature.commons.input.AbstractInputTaxon
import org.locationtech.jts.geom.Geometry

/**
 * Describes a current input.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class Input : AbstractInput {

    var geometry: Geometry? = null
    var selectedFeatureId: String? = null

    constructor() : super("occtax")
    constructor(source: Parcel) : super(source) {
        this.geometry = source.readSerializable() as Geometry?
    }

    override fun writeToParcel(dest: Parcel,
                               flags: Int) {
        super.writeToParcel(dest,
                            flags)

        dest.writeSerializable(geometry)
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

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (geometry?.hashCode() ?: 0)
        return result
    }

    companion object CREATOR : Parcelable.Creator<Input> {
        override fun createFromParcel(parcel: Parcel): Input {
            return Input(parcel)
        }

        override fun newArray(size: Int): Array<Input?> {
            return arrayOfNulls(size)
        }
    }
}
