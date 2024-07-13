package fr.geonature.occtax.features.nomenclature.domain

import android.os.Parcelable
import fr.geonature.occtax.features.record.domain.PropertyValue
import kotlinx.parcelize.Parcelize

/**
 * Describes an editable field type with value.
 *
 * @author S. Grimault
 */
@Parcelize
data class EditableField(
    val type: Type,
    val code: String,
    val viewType: ViewType,

    /**
     * Whether this editable field is linked with a nomenclature type.
     */
    val nomenclatureType: String? = null,

    val visible: Boolean = true,
    val default: Boolean = true,

    /**
     * Whether this editable field is considered as an additional field or not.
     */
    val additionalField: Boolean = false,

    /**
     * Editable field's label.
     */
    val label: String? = null,

    /**
     * Available values for this property.
     */
    val values: List<PropertyValue> = emptyList(),

    /**
     * The current value for this editable field.
     */
    var value: PropertyValue? = null,

    /**
     * Whether this property is locked for modification (default: `false`).
     */
    var locked: Boolean = false
) : Parcelable {

    /**
     * Describes the main category of an editable field.
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
     * Describes an editable field's view type.
     */
    enum class ViewType {
        /**
         * No specific view type.
         */
        NONE,

        /**
         * As list of checkboxes.
         */
        CHECKBOX,

        /**
         * As dropdown nomenclature items.
         */
        NOMENCLATURE_TYPE,

        /**
         * As radio group.
         */
        RADIO,

        /**
         * As a single select.
         */
        SELECT_SIMPLE,

        /**
         * As multiselect.
         */
        SELECT_MULTIPLE,

        /**
         * As a simple text field.
         */
        TEXT_SIMPLE,

        /**
         * As multi-lines text field.
         */
        TEXT_MULTIPLE,

        /**
         * As number text field.
         */
        NUMBER,

        /**
         * As a bounded numerical value.
         */
        MIN_MAX,

        /**
         * As media file.
         */
        MEDIA
    }
}