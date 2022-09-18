package fr.geonature.occtax.features.nomenclature.domain

import android.os.Parcel
import android.os.Parcelable
import fr.geonature.occtax.input.PropertyValue

/**
 * Definition of an editable nomenclature type.
 *
 * @author S. Grimault
 */
abstract class BaseEditableNomenclatureType {
    /**
     * Main nomenclature type.
     */
    abstract val type: Type

    /**
     * Mnemonic code from nomenclature type.
     */
    abstract val code: String

    /**
     * The corresponding view type.
     */
    abstract val viewType: ViewType

    /**
     * Whether this property is visible by default.
     */
    abstract val visible: Boolean

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BaseEditableNomenclatureType

        if (type != other.type) return false
        if (code != other.code) return false
        if (viewType != other.viewType) return false
        if (visible != other.visible) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + code.hashCode()
        result = 31 * result + viewType.hashCode()
        result = 31 * result + visible.hashCode()
        return result
    }

    override fun toString(): String {
        return "BaseEditableNomenclatureType(type=$type, code='$code', viewType=$viewType, visible=$visible)"
    }
    
    companion object {

        /**
         * Factory to create [BaseEditableNomenclatureType] from given properties.
         */
        fun from(
            type: Type,
            code: String,
            viewType: ViewType,
            visible: Boolean = true
        ): BaseEditableNomenclatureType = object : BaseEditableNomenclatureType() {
            override val type: Type
                get() = type
            override val code: String
                get() = code
            override val viewType: ViewType
                get() = viewType
            override val visible: Boolean
                get() = visible
        }
    }

    /**
     * Describes main editable nomenclature type.
     */
    enum class Type {
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

/**
 * Describes an editable nomenclature type with value.
 *
 * @author S. Grimault
 */
data class EditableNomenclatureType(
    override val type: Type,
    override val code: String,
    override val viewType: ViewType,
    override val visible: Boolean = true,

    /**
     * Nomenclature type's label.
     */
    val label: String? = null,

    /**
     * The current value for this nomenclature type.
     */
    var value: PropertyValue? = null
) : BaseEditableNomenclatureType(), Parcelable {

    private constructor(source: Parcel) : this(
        source.readSerializable() as Type,
        source.readString()!!,
        source.readSerializable() as ViewType,
        source.readByte() == 1.toByte(), // as boolean value
        source.readString()!!,
        source.readParcelable(PropertyValue::class.java.classLoader)
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
            it.writeSerializable(type)
            it.writeByte((if (visible) 1 else 0).toByte()) // as boolean value
            it.writeString(label)
            it.writeParcelable(
                value,
                flags
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
}