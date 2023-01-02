package fr.geonature.occtax.features.record.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Property value of an an observation record.
 *
 * @author S. Grimault
 */
sealed class PropertyValue : Parcelable {

    /**
     * Whether this property value is considered empty or not.
     */
    val isEmpty: () -> Boolean = {
        when (this) {
            is Text -> value.isNullOrEmpty()
            is Date -> value == null
            is Number -> value == null
            is NumberArray -> value.isEmpty()
            is Nomenclature -> value == null
            is Taxa -> value.all { taxon -> taxon.properties.all { it.value.isEmpty() } }
            is Counting -> value.all { counting -> counting.properties.all { it.value.isEmpty() } }
        }
    }

    /**
     * Returns a Pair representation of this property value.
     */
    fun toPair(): Pair<String, PropertyValue> = (when (this) {
        is Text -> code
        is Date -> code
        is Number -> code
        is NumberArray -> code
        is Nomenclature -> code
        is Taxa -> code
        is Counting -> code
    } to this)

    /**
     * As text.
     */
    @Parcelize
    data class Text(val code: String, val value: String?) : PropertyValue()

    /**
     * As date.
     */
    @Parcelize
    data class Date(val code: String, val value: java.util.Date?) : PropertyValue()

    /**
     * As number.
     */
    @Parcelize
    data class Number(val code: String, val value: kotlin.Number?) : PropertyValue()

    /**
     * As array of numbers.
     */
    @Parcelize
    data class NumberArray(val code: String, val value: Array<kotlin.Number>) : PropertyValue() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as NumberArray

            if (code != other.code) return false
            if (!value.contentEquals(other.value)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = code.hashCode()
            result = 31 * result + value.contentHashCode()
            return result
        }
    }

    /**
     * As nomenclature value.
     */
    @Parcelize
    data class Nomenclature(val code: String, val label: String?, val value: Long?) :
        PropertyValue()

    /**
     * As array of [TaxonRecord].
     */
    @Parcelize
    data class Taxa(val code: String, val value: Array<TaxonRecord>) : PropertyValue() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Taxa

            if (code != other.code) return false
            if (!value.contentEquals(other.value)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = code.hashCode()
            result = 31 * result + value.contentHashCode()
            return result
        }
    }

    /**
     * As array of [CountingRecord].
     */
    @Parcelize
    data class Counting(val code: String, val value: Array<CountingRecord>) : PropertyValue() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Counting

            if (code != other.code) return false
            if (!value.contentEquals(other.value)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = code.hashCode()
            result = 31 * result + value.contentHashCode()
            return result
        }
    }
}