package fr.geonature.occtax.features.nomenclature.domain

import android.os.Bundle
import android.os.Parcel
import fr.geonature.compat.os.getParcelableArrayCompat
import fr.geonature.occtax.features.record.domain.PropertyValue
import kotlinx.parcelize.parcelableCreator
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [EditableField].
 *
 * @author S. Grimault
 */
@RunWith(RobolectricTestRunner::class)
class EditableFieldTest {

    @Test
    fun `should be the same editable nomenclature type`() {
        assertEquals(
            EditableField(
                EditableField.Type.INFORMATION,
                "ETA_BIO",
                EditableField.ViewType.NOMENCLATURE_TYPE,
                visible = true,
                default = false
            ),
            EditableField(
                EditableField.Type.INFORMATION,
                "ETA_BIO",
                EditableField.ViewType.NOMENCLATURE_TYPE,
                visible = true,
                default = false
            ),
        )

        assertEquals(
            EditableField(
                EditableField.Type.INFORMATION,
                "STATUT_BIO",
                EditableField.ViewType.NOMENCLATURE_TYPE,
                label = "Statut biologique",
                visible = false,
                default = false
            ),
            EditableField(
                EditableField.Type.INFORMATION,
                "STATUT_BIO",
                EditableField.ViewType.NOMENCLATURE_TYPE,
                label = "Statut biologique",
                visible = false,
                default = false
            )
        )
    }

    @Test
    fun `should create EditableNomenclatureType from Parcelable`() {
        // given an editable nomenclature type instance
        val editableField = EditableField(
            EditableField.Type.INFORMATION,
            "STATUT_BIO",
            EditableField.ViewType.NOMENCLATURE_TYPE,
            label = "Statut biologique",
            visible = false,
            default = false,
            locked = true
        )

        // when we obtain a Parcel object to write the editable nomenclature type instance to it
        val parcel = Parcel.obtain()
        editableField.writeToParcel(
            parcel,
            0
        )

        // reset the parcel for reading
        parcel.setDataPosition(0)

        // then
        assertEquals(
            editableField,
            parcelableCreator<EditableField>().createFromParcel(parcel)
        )
    }

    @Test
    fun `should create a list of EditableNomenclatureType from Parcelable array`() {
        // given a list of editable nomenclature types
        val expectedEditableFields = listOf(
            EditableField(
                EditableField.Type.INFORMATION,
                "DETERMINER",
                EditableField.ViewType.TEXT_SIMPLE,
                visible = true,
                default = false
            ),
            EditableField(
                EditableField.Type.INFORMATION,
                "STATUT_BIO",
                EditableField.ViewType.NOMENCLATURE_TYPE,
                label = "Statut biologique",
                visible = false,
                default = false,
                value = PropertyValue.Nomenclature(
                    code = "STATUT_BIO",
                    label = "Non renseigné",
                    value = 29L
                ),
                locked = true
            )
        )

        // when creating a bundle of them
        val bundle = Bundle().apply {
            putParcelableArray(
                "editable_nomenclature_types",
                expectedEditableFields.toTypedArray()
            )
        }

        // then
        assertArrayEquals(
            expectedEditableFields.toTypedArray(),
            bundle.getParcelableArrayCompat("editable_nomenclature_types")
        )
    }
}