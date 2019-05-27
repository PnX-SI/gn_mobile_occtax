package fr.geonature.occtax.input

import android.os.Parcel
import android.os.Parcelable
import fr.geonature.commons.input.AbstractInput
import fr.geonature.commons.input.AbstractInputTaxon

/**
 * Describes a current input.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class Input : AbstractInput {

    constructor() : super("occtax")
    constructor(source: Parcel) : super(source)

    override fun getTaxaFromParcel(source: Parcel): List<AbstractInputTaxon> {
        val inputTaxa = source.createTypedArrayList(InputTaxon.CREATOR)
        return inputTaxa?: emptyList()
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
