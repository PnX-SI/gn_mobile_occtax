package fr.geonature.occtax.input

import android.os.Parcel
import android.os.Parcelable
import fr.geonature.commons.data.InputObserver
import fr.geonature.commons.data.Taxon
import java.util.ArrayList
import java.util.Date
import java.util.TreeMap

/**
 * Describes a current input.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class Input : Parcelable {

    var type: InputType
        internal set
    var id: Long? = null
        internal set
    var date: Date? = null
    private val inputObservers = TreeMap<Long, InputObserver>()
    private val taxa = TreeMap<Long, Taxon>()

    constructor(id: Long) {
        this.type = InputType.OCCTAX
        this.id = id
    }

    private constructor(source: Parcel) {
        type = source.readSerializable() as InputType
        id = source.readLong()
        date = source.readSerializable() as Date

        if (date == null) {
            date = Date()
        }

        val inputObservers = ArrayList<InputObserver>()
        source.readTypedList(inputObservers,
                             InputObserver.CREATOR)

        this.setInputObservers(inputObservers)

        val taxa = ArrayList<Taxon>()
        source.readTypedList(taxa,
                             Taxon.CREATOR)

        this.setTaxa(taxa)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel,
                               flags: Int) {
        dest.writeSerializable(this.type)
        dest.writeLong(this.id!!)
        dest.writeSerializable(this.date)
        dest.writeTypedList(ArrayList(this.inputObservers.values))
        dest.writeTypedList(ArrayList(this.taxa.values))
    }

    fun getInputObservers(): List<InputObserver> {
        return ArrayList(this.inputObservers.values)
    }

    fun setInputObservers(inputObservers: List<InputObserver>) {
        this.inputObservers.clear()

        for (inputObserver in inputObservers) {
            this.inputObservers[inputObserver.id] = inputObserver
        }
    }

    fun getTaxa(): List<Taxon> {
        return ArrayList(this.taxa.values)
    }

    fun setTaxa(taxa: List<Taxon>) {
        this.taxa.clear()

        for (taxon in taxa) {
            this.taxa[taxon.id] = taxon
        }
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
