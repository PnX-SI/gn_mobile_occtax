package fr.geonature.occtax.input

import android.os.Parcel
import android.os.Parcelable
import fr.geonature.commons.input.AbstractInputTaxon

/**
 * Describes an input taxon.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class InputTaxon : AbstractInputTaxon {

    constructor()
    constructor(source: Parcel) : super(source)

    companion object CREATOR : Parcelable.Creator<InputTaxon> {
        override fun createFromParcel(parcel: Parcel): InputTaxon {
            return InputTaxon(parcel)
        }

        override fun newArray(size: Int): Array<InputTaxon?> {
            return arrayOfNulls(size)
        }
    }
}