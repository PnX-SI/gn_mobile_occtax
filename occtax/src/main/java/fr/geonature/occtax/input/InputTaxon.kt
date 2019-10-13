package fr.geonature.occtax.input

import android.os.Parcel
import android.os.Parcelable
import fr.geonature.commons.data.AbstractTaxon
import fr.geonature.commons.input.AbstractInputTaxon

/**
 * Describes an input taxon.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class InputTaxon : AbstractInputTaxon {

    val properties: MutableMap<String, SelectedProperty> = HashMap()

    constructor(taxon: AbstractTaxon) : super(taxon)
    constructor(source: Parcel) : super(source) {
        (source.createTypedArrayList(SelectedProperty.CREATOR)
                ?: emptyList<SelectedProperty>())
            .forEach {
                this.properties[it.code] = it
            }
    }

    override fun writeToParcel(dest: Parcel?,
                               flags: Int) {
        super.writeToParcel(dest,
                            flags)

        dest?.writeTypedList(this.properties.values.toList())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is InputTaxon) return false
        if (!super.equals(other)) return false

        if (properties != other.properties) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + properties.hashCode()

        return result
    }

    companion object CREATOR : Parcelable.Creator<InputTaxon> {
        override fun createFromParcel(parcel: Parcel): InputTaxon {
            return InputTaxon(parcel)
        }

        override fun newArray(size: Int): Array<InputTaxon?> {
            return arrayOfNulls(size)
        }
    }
}