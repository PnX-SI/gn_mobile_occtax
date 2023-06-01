package fr.geonature.occtax.features.nomenclature.domain

import android.os.Parcelable
import fr.geonature.occtax.features.record.domain.PropertyValue
import kotlinx.parcelize.Parcelize

/**
 * Describes an editable nomenclature type with value.
 *
 * @author S. Grimault
 */
@Parcelize
data class EditableNomenclatureType(
    val type: Type,
    val code: String,
    val viewType: ViewType,
    val visible: Boolean = true,
    val default: Boolean = true,
    val additionalField: Boolean = false,

    /**
     * Nomenclature type's label.
     */
    val label: String? = null,

    /**
     * Available values for this property.
     */
    val values: List<PropertyValue> = emptyList(),

    /**
     * The current value for this nomenclature type.
     */
    var value: PropertyValue? = null,

    /**
     * Whether this property is locked for modification (default: `false`).
     */
    var locked: Boolean = false
) : Parcelable {

    /**
     * Describes main editable nomenclature type.
     */
    enum class Type {
        /**
         * Default nomenclature types.
         */
        DEFAULT,

        /**
         * Nomenclature types used for main information.
         */
        INFORMATION,

        /**
         * Nomenclature types used for describing counting.
         */
        COUNTING
    }

    /**
     * Describes an editable nomenclature type view type.
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
         * As dropdown menu items.
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