package fr.geonature.occtax.features.nomenclature.domain

import android.os.Parcelable
import fr.geonature.occtax.features.record.domain.PropertyValue
import kotlinx.parcelize.Parcelize

/**
 * Describes a form field. A form field may or may not be editable, or may describe a particular
 * type.
 *
 * @author S. Grimault
 */
sealed interface FormField : Parcelable {

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
     * Updates this editable field by copy and alter some of its properties.
     */
    fun update(
        label: String = this.label,
        visible: Boolean = this.visible,
        default: Boolean = this.default
    ): FormField =
        when (val ff = this@FormField) {
            is Button -> ff.copy(
                label = label,
                visible = visible,
                default = default
            )

            is Checkbox -> ff.copy(
                label = label,
                visible = visible,
                default = default
            )

            is Date -> ff.copy(
                label = label,
                visible = visible,
                default = default
            )

            is Media -> ff.copy(
                label = label,
                visible = visible,
                default = default
            )

            is MinMax -> ff.copy(
                label = label,
                visible = visible,
                default = default
            )

            is NomenclatureType -> ff.copy(
                label = label,
                visible = visible,
                default = default
            )

            is Number -> ff.copy(
                label = label,
                visible = visible,
                default = default
            )

            is Radio -> ff.copy(
                label = label,
                visible = visible,
                default = default
            )

            is Section -> ff.copy(
                label = label,
                visible = visible,
                default = default
            )

            is Select -> ff.copy(
                label = label,
                visible = visible,
                default = default
            )

            is SelectMultiple -> ff.copy(
                label = label,
                visible = visible,
                default = default
            )

            is Text -> ff.copy(
                label = label,
                visible = visible,
                default = default
            )

            is TextMultiple -> ff.copy(
                label = label,
                visible = visible,
                default = default
            )

            is Time -> ff.copy(
                label = label,
                visible = visible,
                default = default
            )
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
         * Whether this property is locked for modification (default: `false`).
         */
        var locked: Boolean = false

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
        override val visible: Boolean = true
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
        override val additionalField: Boolean = false,

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
        override val additionalField: Boolean = false,
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
        override val additionalField: Boolean = false,
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
        override val additionalField: Boolean = false,

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
        override val additionalField: Boolean = false,
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
        override val additionalField: Boolean = false,

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
        override val additionalField: Boolean = false,

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
        override val additionalField: Boolean = false,

        /**
         * Available values for this property.
         */
        val values: List<PropertyValue.Text> = emptyList(),

        var value: PropertyValue.StringArray
    ) : Editable()

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
        override val additionalField: Boolean = false,

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
        override val additionalField: Boolean = false,

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
        override val additionalField: Boolean = false,

        var value: PropertyValue.Time
    ) : Editable()
}