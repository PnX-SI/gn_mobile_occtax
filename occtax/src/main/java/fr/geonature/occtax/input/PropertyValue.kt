package fr.geonature.occtax.input

import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils
import fr.geonature.commons.data.Nomenclature
import fr.geonature.commons.input.AbstractInputTaxon
import java.io.Serializable

/**
 * Property value for [AbstractInputTaxon]
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
data class PropertyValue(val code: String,
                         val label: String?,
                         val value: Serializable?) : Parcelable {

    internal constructor(source: Parcel) : this(source.readString()!!,
                                                source.readString(),
                                                source.readSerializable())

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel?,
                               flags: Int) {
        dest?.also {
            it.writeString(code)
            it.writeString(label)
            it.writeSerializable(value)
        }
    }

    fun isEmpty(): Boolean {
        return TextUtils.isEmpty(label) && value == null
    }

    companion object {

        /**
         * Creates a [PropertyValue] instance from given [Nomenclature].
         */
        fun fromNomenclature(code: String,
                             nomenclature: Nomenclature?): PropertyValue {
            return PropertyValue(code,
                                 nomenclature?.defaultLabel,
                                 nomenclature?.id)
        }

        /**
         * Creates a [PropertyValue] instance from given String value.
         */
        fun fromValue(code: String,
                      value: String?): PropertyValue {
            return PropertyValue(code,
                                 null,
                                 value)
        }

        /**
         * Creates a [PropertyValue] instance from given Int value.
         */
        fun fromValue(code: String,
                      value: Int?): PropertyValue {
            return PropertyValue(code,
                                 null,
                                 value)
        }

        @JvmField
        val CREATOR: Parcelable.Creator<PropertyValue> = object : Parcelable.Creator<PropertyValue> {

            override fun createFromParcel(source: Parcel): PropertyValue {
                return PropertyValue(source)
            }

            override fun newArray(size: Int): Array<PropertyValue?> {
                return arrayOfNulls(size)
            }
        }
    }
}