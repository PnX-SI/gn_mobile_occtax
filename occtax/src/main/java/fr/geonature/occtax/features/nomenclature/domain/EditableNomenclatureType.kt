package fr.geonature.occtax.features.nomenclature.domain

import android.os.Parcel
import android.os.Parcelable
import androidx.core.os.ParcelCompat.readBoolean
import androidx.core.os.ParcelCompat.writeBoolean
import fr.geonature.occtax.features.input.domain.PropertyValue


/**
 * Describes an editable nomenclature type with value.
 *
 * @author S. Grimault
 */
data class EditableNomenclatureType(
    val type: Type,
    val code: String,
    val viewType: ViewType,
    val visible: Boolean = true,
    val default: Boolean = true,

    /**
     * Nomenclature type's label.
     */
    val label: String? = null,

    /**
     * The current value for this nomenclature type.
     */
    var value: PropertyValue? = null,

    /**
     * Whether this property is locked for modification (default: `false`).
     */
    var locked: Boolean = false
) : Parcelable {

    private constructor(source: Parcel) : this(
        source.readSerializable() as Type,
        source.readString()!!,
        source.readSerializable() as ViewType,
        readBoolean(source),
        readBoolean(source),
        source.readString(),
        source.readParcelable(PropertyValue::class.java.classLoader),
        readBoolean(source)
    )

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(
        dest: Parcel?,
        flags: Int
    ) {
        dest?.also {
            it.writeSerializable(type)
            it.writeString(code)
            it.writeSerializable(viewType)
            writeBoolean(
                it,
                visible
            )
            writeBoolean(
                it,
                default
            )
            it.writeString(label)
            it.writeParcelable(
                value,
                flags
            )
            writeBoolean(
                it,
                locked
            )
        }
    }

    companion object CREATOR : Parcelable.Creator<EditableNomenclatureType> {
        override fun createFromParcel(parcel: Parcel): EditableNomenclatureType {
            return EditableNomenclatureType(parcel)
        }

        override fun newArray(size: Int): Array<EditableNomenclatureType?> {
            return arrayOfNulls(size)
        }
    }

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
         * As dropdown menu items.
         */
        NOMENCLATURE_TYPE,

        /**
         * As a simple text field.
         */
        TEXT_SIMPLE,

        /**
         * As multi-lines text field.
         */
        TEXT_MULTIPLE,

        /**
         * As a bounded numerical value.
         */
        MIN_MAX
    }
}