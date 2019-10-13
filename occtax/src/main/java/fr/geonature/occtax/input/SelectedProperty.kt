package fr.geonature.occtax.input

import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils
import fr.geonature.commons.data.Nomenclature
import fr.geonature.commons.input.AbstractInputTaxon
import java.util.Locale

/**
 * Selected property value for [AbstractInputTaxon]
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
data class SelectedProperty(val type: PropertyType,
                            val code: String,
                            val id: Long?,
                            val label: String?) : Parcelable {

    internal constructor(source: Parcel) : this(source.readSerializable() as PropertyType,
                                                source.readString()!!,
                                                source.readLong().takeIf { it > 0L },
                                                source.readString())

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel?,
                               flags: Int) {
        dest?.also {
            it.writeSerializable(type)
            it.writeString(code)
            it.writeLong(id ?: 0L)
            it.writeString(label)
        }
    }

    fun isEmpty(): Boolean {
        return id == null && TextUtils.isEmpty(label)
    }

    enum class PropertyType {
        NOMENCLATURE,
        TEXT;

        companion object {
            fun fromString(value: String?): PropertyType? {
                val sanitizeValue = value ?: return null

                return try {
                    valueOf(sanitizeValue.toUpperCase(Locale.ROOT))
                }
                catch (iae: IllegalArgumentException) {
                    null
                }
            }
        }
    }

    companion object {

        /**
         * Creates a SelectedProperty instance from given [Nomenclature]
         */
        fun fromNomenclature(code: String,
                             nomenclature: Nomenclature?): SelectedProperty {
            return SelectedProperty(PropertyType.NOMENCLATURE,
                                    code,
                                    nomenclature?.id,
                                    nomenclature?.defaultLabel)
        }

        /**
         * Creates a SelectedProperty instance from given String value
         */
        fun fromValue(code: String,
                      value: String?): SelectedProperty {
            return SelectedProperty(PropertyType.TEXT,
                                    code,
                                    null,
                                    value)
        }

        @JvmField
        val CREATOR: Parcelable.Creator<SelectedProperty> = object : Parcelable.Creator<SelectedProperty> {

            override fun createFromParcel(source: Parcel): SelectedProperty {
                return SelectedProperty(source)
            }

            override fun newArray(size: Int): Array<SelectedProperty?> {
                return arrayOfNulls(size)
            }
        }
    }
}