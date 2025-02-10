package fr.geonature.occtax.features.nomenclature.domain

import android.os.Parcelable
import fr.geonature.occtax.features.record.domain.PropertyValue
import fr.geonature.occtax.features.settings.domain.InputDateSettings
import kotlinx.parcelize.Parcelize

/**
 * Describes a form field. A form field may or may not be editable, or may describe a particular
 * type.
 *
 * @author S. Grimault
 */
sealed interface FormField : Parcelable, Comparable<FormField> {

    /**
     * The main category of this form field.
     */
    val type: Type

    /**
     * Form field's label.
     */
    val label: String

    /**
     * Whether this form field is visible (and thus editable).
     */
    val visible: Boolean

    /**
     * Whether this form field is displayed by default.
     */
    val default: Boolean

    /**
     * The display order of this form field.
     */
    val order: Int?

    override fun compareTo(other: FormField): Int {
        return when {
            this == other -> 0
            this is Editable && other is Editable && !this.additionalField && !other.additionalField -> (order
                ?: Int.MAX_VALUE) - (other.order ?: Int.MAX_VALUE)

            this is Editable && other is Editable && this.additionalField && other.additionalField -> (order
                ?: Int.MAX_VALUE) - (other.order ?: Int.MAX_VALUE)

            this is Editable && other is Editable && !this.additionalField && other.additionalField -> -1
            this is Editable && other is Editable && this.additionalField && !other.additionalField -> 1
            else -> (order ?: Int.MAX_VALUE) - (other.order ?: Int.MAX_VALUE)
        }
    }

    /**
     * Updates this editable field by copy and alter some of its properties.
     */
    fun update(
        label: String = this.label,
        visible: Boolean = this.visible,
        default: Boolean = this.default,
        order: Int? = this.order
    ): FormField =
        when (val ff = this@FormField) {
            is Button -> ff.copy(
                label = label,
                visible = visible,
                default = default,
                order = order
            )

            is Checkbox -> ff.copy(
                label = label,
                visible = visible,
                default = default,
                order = order
            )

            is Date -> ff.copy(
                label = label,
                visible = visible,
                default = default,
                order = order
            )

            is Media -> ff.copy(
                label = label,
                visible = visible,
                default = default,
                order = order
            )

            is MinMax -> ff.copy(
                label = label,
                visible = visible,
                default = default,
                order = order
            )

            is NomenclatureType -> ff.copy(
                label = label,
                visible = visible,
                default = default,
                order = order
            )

            is Number -> ff.copy(
                label = label,
                visible = visible,
                default = default,
                order = order
            )

            is Radio -> ff.copy(
                label = label,
                visible = visible,
                default = default,
                order = order
            )

            is Section -> ff.copy(
                label = label,
                visible = visible,
                default = default,
                order = order
            )

            is Select -> ff.copy(
                label = label,
                visible = visible,
                default = default,
                order = order
            )

            is SelectMultiple -> ff.copy(
                label = label,
                visible = visible,
                default = default,
                order = order
            )

            is StartEnd -> ff.copy(
                label = label,
                visible = visible,
                default = default,
                order = order
            )

            is Text -> ff.copy(
                label = label,
                visible = visible,
                default = default,
                order = order
            )

            is TextMultiple -> ff.copy(
                label = label,
                visible = visible,
                default = default,
                order = order
            )

            is Time -> ff.copy(
                label = label,
                visible = visible,
                default = default,
                order = order
            )
        }

    /**
     * Describes the main category of a form field.
     */
    enum class Type {
        /**
         * Default category.
         */
        DEFAULT,

        /**
         * Used for main information.
         */
        INFORMATION,

        /**
         * Used for describing counting.
         */
        COUNTING
    }

    /**
     * As an editable field that embeds a [PropertyValue].
     */
    sealed class Editable : FormField {

        /**
         * The current [Section] this editable field belongs to (default: `null`).
         */
        abstract val parent: Section?

        /**
         * Whether this editable field is considered as an additional field or not.
         */
        abstract val additionalField: Boolean

        /**
         * Whether this property is mandatory (default: `false`).
         */
        abstract val mandatory: Boolean

        /**
         * Whether this property is locked for modification (default: `false`).
         */
        var locked: Boolean = false

        /**
         * Whether this editable field contains errors or not (default: `null`).
         */
        var error: CharSequence? = null

        /**
         * The current [PropertyValue] of this editable field.
         */
        fun getValue(): PropertyValue = when (this) {
            is Checkbox -> value
            is Date -> value
            is Media -> value
            is NomenclatureType -> value
            is Number -> value
            is Radio -> value
            is Select -> value
            is SelectMultiple -> value
            is Text -> value
            is TextMultiple -> value
            is Time -> value
        }

        /**
         * Edits the [PropertyValue].
         */
        fun setValue(value: PropertyValue) = when (val ff = this@Editable) {
            is Checkbox -> ff.value =
                if (value is PropertyValue.StringArray) value.copy(ff.value.code) else ff.value

            is Date -> ff.value =
                if (value is PropertyValue.Date) value.copy(ff.value.code) else ff.value

            is Media -> ff.value =
                if (value is PropertyValue.Media) value.copy(ff.value.code) else ff.value

            is NomenclatureType -> ff.value =
                if (value is PropertyValue.Nomenclature) value.copy(ff.value.code) else ff.value

            is Number -> ff.value =
                if (value is PropertyValue.Number) value.copy(ff.value.code) else ff.value

            is Radio -> ff.value =
                if (value is PropertyValue.Text) value.copy(ff.value.code) else ff.value

            is Select -> ff.value =
                if (value is PropertyValue.Text) value.copy(ff.value.code) else ff.value

            is SelectMultiple -> ff.value =
                if (value is PropertyValue.StringArray) value.copy(ff.value.code) else ff.value

            is Text -> ff.value =
                if (value is PropertyValue.Text) value.copy(ff.value.code) else ff.value

            is TextMultiple -> ff.value =
                if (value is PropertyValue.Text) value.copy(ff.value.code) else ff.value

            is Time -> ff.value =
                if (value is PropertyValue.Time) value.copy(ff.value.code) else ff.value
        }
    }

    /**
     * As section, to organize [Editable] by section.
     */
    @Parcelize
    data class Section(
        override val type: Type,
        override val label: String,
        override val default: Boolean = true,
        override val visible: Boolean = true,
        override val order: Int? = null,
        val code: String
    ) : FormField

    /**
     * As button.
     */
    @Parcelize
    data class Button(
        override val type: Type,
        override val label: String,
        override val default: Boolean = true,
        override val visible: Boolean = true,
        override val order: Int? = null,
    ) : FormField

    /**
     * As list of checkboxes.
     */
    @Parcelize
    data class Checkbox(
        override val parent: Section? = null,
        override val type: Type,
        override val label: String,
        override val default: Boolean = true,
        override val visible: Boolean = true,
        override val order: Int? = null,
        override val additionalField: Boolean = false,
        override val mandatory: Boolean = false,

        /**
         * Available values for this property.
         */
        val values: List<PropertyValue.Text> = emptyList(),

        var value: PropertyValue.StringArray
    ) : Editable()

    /**
     * As a date field.
     */
    @Parcelize
    data class Date(
        override val parent: Section? = null,
        override val type: Type,
        override val label: String,
        override val default: Boolean = true,
        override val visible: Boolean = true,
        override val order: Int? = null,
        override val additionalField: Boolean = false,
        override val mandatory: Boolean = false,
        var value: PropertyValue.Date
    ) : Editable()

    /**
     * As media file.
     */
    @Parcelize
    data class Media(
        override val parent: Section? = null,
        override val type: Type,
        override val label: String,
        override val default: Boolean = true,
        override val visible: Boolean = true,
        override val order: Int? = null,
        override val additionalField: Boolean = false,
        override val mandatory: Boolean = false,
        var value: PropertyValue.Media
    ) : Editable()

    /**
     * As a bounded numerical values.
     */
    @Parcelize
    data class MinMax(
        override val type: Type,
        override val label: String,
        override val default: Boolean = true,
        override val visible: Boolean = true,
        override val order: Int? = null,
        val min: Number,
        val max: Number
    ) : FormField

    /**
     * As dropdown nomenclature items.
     */
    @Parcelize
    data class NomenclatureType(
        override val parent: Section? = null,
        override val type: Type,
        override val label: String,
        override val default: Boolean = true,
        override val visible: Boolean = true,
        override val order: Int? = null,
        override val additionalField: Boolean = false,
        override val mandatory: Boolean = false,

        /**
         * The nomenclature type code on which to retrieve all available nomenclature values on
         * request.
         */
        val nomenclatureType: String,

        var value: PropertyValue.Nomenclature
    ) : Editable()

    /**
     * As number text field.
     */
    @Parcelize
    data class Number(
        override val parent: Section? = null,
        override val type: Type,
        override val label: String,
        override val default: Boolean = true,
        override val visible: Boolean = true,
        override val order: Int? = null,
        override val additionalField: Boolean = false,
        override val mandatory: Boolean = false,
        var value: PropertyValue.Number
    ) : Editable()

    /**
     * As radio group.
     */
    @Parcelize
    data class Radio(
        override val parent: Section? = null,
        override val type: Type,
        override val label: String,
        override val default: Boolean = true,
        override val visible: Boolean = true,
        override val order: Int? = null,
        override val additionalField: Boolean = false,
        override val mandatory: Boolean = false,

        /**
         * Available values for this property.
         */
        val values: List<PropertyValue.Text> = emptyList(),

        var value: PropertyValue.Text
    ) : Editable()

    /**
     * As a single select.
     */
    @Parcelize
    data class Select(
        override val parent: Section? = null,
        override val type: Type,
        override val label: String,
        override val default: Boolean = true,
        override val visible: Boolean = true,
        override val order: Int? = null,
        override val additionalField: Boolean = false,
        override val mandatory: Boolean = false,

        /**
         * Available values for this property.
         */
        val values: List<PropertyValue.Text> = emptyList(),

        var value: PropertyValue.Text
    ) : Editable()

    /**
     * As multiselect.
     */
    @Parcelize
    data class SelectMultiple(
        override val parent: Section? = null,
        override val type: Type,
        override val label: String,
        override val default: Boolean = true,
        override val visible: Boolean = true,
        override val order: Int? = null,
        override val additionalField: Boolean = false,
        override val mandatory: Boolean = false,

        /**
         * Available values for this property.
         */
        val values: List<PropertyValue.Text> = emptyList(),

        var value: PropertyValue.StringArray
    ) : Editable()

    /**
     * As a group of start and end dates.
     */
    @Parcelize
    data class StartEnd(
        override val type: Type,
        override val label: String,
        override val default: Boolean = true,
        override val visible: Boolean = true,
        override val order: Int? = null,
        val settings: InputDateSettings = InputDateSettings.DEFAULT,
        val start: Date,
        val end: Date
    ) : FormField

    /**
     * As a single text field.
     */
    @Parcelize
    data class Text(
        override val parent: Section? = null,
        override val type: Type,
        override val label: String,
        override val default: Boolean = true,
        override val visible: Boolean = true,
        override val order: Int? = null,
        override val additionalField: Boolean = false,
        override val mandatory: Boolean = false,

        var value: PropertyValue.Text
    ) : Editable()

    /**
     * As multi-lines text field.
     */
    @Parcelize
    data class TextMultiple(
        override val parent: Section? = null,
        override val type: Type,
        override val label: String,
        override val default: Boolean = true,
        override val visible: Boolean = true,
        override val order: Int? = null,
        override val additionalField: Boolean = false,
        override val mandatory: Boolean = false,

        var value: PropertyValue.Text
    ) : Editable()

    /**
     * As a time field.
     */
    @Parcelize
    data class Time(
        override val parent: Section? = null,
        override val type: Type,
        override val label: String,
        override val default: Boolean = true,
        override val visible: Boolean = true,
        override val order: Int? = null,
        override val additionalField: Boolean = false,
        override val mandatory: Boolean = false,

        var value: PropertyValue.Time
    ) : Editable()
}