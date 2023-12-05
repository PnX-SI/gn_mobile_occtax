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
            is StringArray -> value.isEmpty()
            is Number -> value == null
            is NumberArray -> value.isEmpty()
            is Dataset -> datasetId == null
            is Nomenclature -> value == null
            is AdditionalField -> value.all { it.value.isEmpty() }
            is Taxa -> value.all { taxon -> taxon.properties.all { it.value.isEmpty() } }
            is Counting -> value.all { counting -> counting.properties.all { it.value.isEmpty() } }
            is Media -> value.isEmpty()
        }
    }

    /**
     * Returns a Pair representation of this property value.
     */
    fun toPair(): Pair<String, PropertyValue> = (when (this) {
        is Text -> code
        is Date -> code
        is StringArray -> code
        is Number -> code
        is NumberArray -> code
        is Dataset -> code
        is Nomenclature -> code
        is AdditionalField -> code
        is Taxa -> code
        is Counting -> code
        is Media -> code
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
     * As array of strings.
     */
    @Parcelize
    data class StringArray(val code: String, val value: Array<String> = emptyArray()) :
        PropertyValue() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as StringArray

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
     * As number.
     */
    @Parcelize
    data class Number(val code: String, val value: kotlin.Number?) : PropertyValue()

    /**
     * As array of numbers.
     */
    @Parcelize
    data class NumberArray(val code: String, val value: Array<kotlin.Number> = emptyArray()) :
        PropertyValue() {
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
     * As dataset.
     */
    @Parcelize
    data class Dataset(val code: String, val datasetId: Long?, val taxaListId: Long? = null) :
        PropertyValue()

    /**
     * As nomenclature value.
     */
    @Parcelize
    data class Nomenclature(val code: String, val label: String?, val value: Long?) :
        PropertyValue()

    /**
     * As additional field.
     */
    @Parcelize
    data class AdditionalField(val code: String, val value: Map<String, PropertyValue>) :
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

    /**
     * As array of [MediaRecord].
     */
    @Parcelize
    data class Media(val code: String, val value: Array<MediaRecord> = emptyArray()) :
        PropertyValue() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Media

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