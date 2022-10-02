package fr.geonature.occtax.input

import android.os.Parcel
import android.os.Parcelable
import fr.geonature.commons.data.entity.Nomenclature
import fr.geonature.commons.input.AbstractInputTaxon
import org.tinylog.Logger
import java.io.Serializable

/**
 * Property value for [AbstractInputTaxon]
 *
 * @author S. Grimault
 */
data class PropertyValue(
    val code: String,
    val label: String?,
    val value: Serializable?
) : Parcelable {

    private constructor(source: Parcel) : this(
        source.readString()!!,
        source.readString(),
        source.readSerializable()
    )

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(
        dest: Parcel?,
        flags: Int
    ) {
        dest?.also {
            it.writeString(code)
            it.writeString(label)
            it.writeSerializable(value)
        }
    }

    fun isEmpty(): Boolean {
        return value == null
    }

    companion object {

        /**
         * Creates a [PropertyValue] instance from given [Nomenclature].
         */
        fun fromNomenclature(
            code: String,
            nomenclature: Nomenclature?
        ): PropertyValue {
            if (nomenclature?.defaultLabel.isNullOrEmpty()) {
                Logger.warn { "no label found for nomenclature '$code:${nomenclature?.code}'" }
            }

            return PropertyValue(
                code,
                nomenclature?.defaultLabel,
                nomenclature?.id
            )
        }

        /**
         * Creates a [PropertyValue] instance from given value.
         */
        fun fromValue(
            code: String,
            value: Serializable?
        ): PropertyValue {
            return PropertyValue(
                code,
                null,
                value
            )
        }

        @JvmField
        val CREATOR: Parcelable.Creator<PropertyValue> =
            object : Parcelable.Creator<PropertyValue> {

                override fun createFromParcel(source: Parcel): PropertyValue {
                    return PropertyValue(source)
                }

                override fun newArray(size: Int): Array<PropertyValue?> {
                    return arrayOfNulls(size)
                }
            }
    }
}
