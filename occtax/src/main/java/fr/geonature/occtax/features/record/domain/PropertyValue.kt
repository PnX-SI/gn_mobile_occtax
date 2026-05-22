package fr.geonature.occtax.features.record.domain

import android.os.Parcelable
import fr.geonature.commons.data.entity.InputObserver
import kotlinx.parcelize.Parcelize

/**
 * Property value of an observation record.
 *
 * @author S. Grimault
 */
sealed class PropertyValue : Parcelable {

    /**
     * The unique identifier of this [PropertyValue].
     */
    abstract val code: String

    /**
     * Whether this property value is considered empty or not.
     */
    val isEmpty: () -> Boolean = {
        when (this) {
            is Text -> value.isNullOrEmpty()
            is Date -> value == null
            is Time -> hour == null && minute == null
            is StringArray -> value.isEmpty()
            is Number -> value == null
            is NumberArray -> value.isEmpty()
            is Observers -> value.isEmpty()
            is Dataset -> value == null
            is Nomenclature -> value == null
            is AdditionalFields -> value.all { it.value.isEmpty() }
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
        is Time -> code
        is StringArray -> code
        is Number -> code
        is NumberArray -> code
        is Observers -> code
        is Dataset -> code
        is Nomenclature -> code
        is AdditionalFields -> code
        is Taxa -> code
        is Counting -> code
        is Media -> code
    } to this)

    /**
     * As text.
     */
    @Parcelize
    data class Text(override val code: String, val value: String? = null) : PropertyValue()

    /**
     * As date.
     */
    @Parcelize
    data class Date(override val code: String, val value: java.util.Date? = null) : PropertyValue()

    /**
     * As time.
     */
    @Parcelize
    data class Time(
        override val code: String,
        val hour: Int? = null,
        val minute: Int? = null
    ) : PropertyValue() {

        /**
         * Returns a string representation of the local time.
         */
        fun toTimeString(): String? {
            if (isEmpty()) return null

            return "${"%02d".format(hour ?: 0)}:${"%02d".format(minute ?: 0)}"
        }

        companion object {

            /**
             * Tries to parse a string representation of a local time as [PropertyValue.Time].
             */
            fun parse(code: String, time: String?): Time {
                if (time.isNullOrBlank()) return Time(code)

                val matchResult = """^(?<hour>\d{1,2}):(?<minute>\d{1,2})$""".toRegex()
                    .find(time)
                val hour = matchResult?.groups?.get("hour")?.value?.toInt()
                    ?.coerceIn(
                        0,
                        23
                    )

                val minute = matchResult?.groups?.get("minute")?.value?.toInt()
                    ?.coerceIn(
                        0,
                        59
                    )

                if (hour == null || minute == null) return Time(code)

                return Time(
                    code,
                    hour,
                    minute
                )
            }
        }
    }

    /**
     * As array of strings.
     */
    @Parcelize
    data class StringArray(override val code: String, val value: Array<String> = emptyArray()) :
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
    data class Number(override val code: String, val value: kotlin.Number? = null) : PropertyValue()

    /**
     * As array of numbers.
     */
    @Parcelize
    data class NumberArray(
        override val code: String,
        val value: Array<kotlin.Number> = emptyArray()
    ) :
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
     * As observer.
     */
    @Parcelize
    data class Observers(
        override val code: String,
        val value: Array<InputObserver> = emptyArray()
    ) :
        PropertyValue() {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Observers

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
    data class Dataset(
        override val code: String,
        val value: fr.geonature.commons.data.entity.Dataset? = null
    ) :
        PropertyValue()

    /**
     * As nomenclature value.
     */
    @Parcelize
    data class Nomenclature(
        override val code: String,
        val label: String? = null,
        val value: Long? = null
    ) :
        PropertyValue()

    /**
     * As additional fields.
     */
    @Parcelize
    data class AdditionalFields(
        override val code: String,
        val value: Map<String, PropertyValue> = emptyMap()
    ) :
        PropertyValue()

    /**
     * As array of [TaxonRecord].
     */
    @Parcelize
    data class Taxa(override val code: String, val value: Array<TaxonRecord> = emptyArray()) :
        PropertyValue() {
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
    data class Counting(
        override val code: String,
        val value: Array<CountingRecord> = emptyArray()
    ) :
        PropertyValue() {
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
    data class Media(override val code: String, val value: Array<MediaRecord> = emptyArray()) :
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