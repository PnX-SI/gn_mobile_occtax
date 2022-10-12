package fr.geonature.occtax.features.nomenclature.domain

import android.os.Bundle
import android.os.Parcel
import fr.geonature.occtax.features.input.domain.PropertyValue
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [EditableNomenclatureType].
 *
 * @author S. Grimault
 */
@RunWith(RobolectricTestRunner::class)
class EditableNomenclatureTypeTest {

    @Test
    fun `should be the same editable nomenclature type`() {
        assertEquals(
            BaseEditableNomenclatureType.from(
                BaseEditableNomenclatureType.Type.INFORMATION,
                "ETA_BIO",
                BaseEditableNomenclatureType.ViewType.NOMENCLATURE_TYPE,
                visible = true,
                default = false
            ),
            BaseEditableNomenclatureType.from(
                BaseEditableNomenclatureType.Type.INFORMATION,
                "ETA_BIO",
                BaseEditableNomenclatureType.ViewType.NOMENCLATURE_TYPE,
                visible = true,
                default = false
            ),
        )

        assertEquals(
            EditableNomenclatureType(
                BaseEditableNomenclatureType.Type.INFORMATION,
                "STATUT_BIO",
                BaseEditableNomenclatureType.ViewType.NOMENCLATURE_TYPE,
                label = "Statut biologique",
                visible = false,
                default = false
            ),
            EditableNomenclatureType(
                BaseEditableNomenclatureType.Type.INFORMATION,
                "STATUT_BIO",
                BaseEditableNomenclatureType.ViewType.NOMENCLATURE_TYPE,
                label = "Statut biologique",
                visible = false,
                default = false
            )
        )
    }

    @Test
    fun `should create EditableNomenclatureType from Parcelable`() {
        // given an editable nomenclature type instance
        val editableNomenclatureType = EditableNomenclatureType(
            BaseEditableNomenclatureType.Type.INFORMATION,
            "STATUT_BIO",
            BaseEditableNomenclatureType.ViewType.NOMENCLATURE_TYPE,
            label = "Statut biologique",
            visible = false,
            default = false,
            locked = true
        )

        // when we obtain a Parcel object to write the editable nomenclature type instance to it
        val parcel = Parcel.obtain()
        editableNomenclatureType.writeToParcel(
            parcel,
            0
        )

        // reset the parcel for reading
        parcel.setDataPosition(0)

        // then
        assertEquals(
            editableNomenclatureType,
            EditableNomenclatureType.CREATOR.createFromParcel(parcel)
        )
    }

    @Test
    fun `should create a list of EditableNomenclatureType from Parcelable array`() {
        // given a list of editable nomenclature types
        val expectedEditableNomenclatureTypes = listOf(
            EditableNomenclatureType(
                BaseEditableNomenclatureType.Type.INFORMATION,
                "DETERMINER",
                BaseEditableNomenclatureType.ViewType.TEXT_SIMPLE,
                visible = true,
                default = false
            ),
            EditableNomenclatureType(
                BaseEditableNomenclatureType.Type.INFORMATION,
                "STATUT_BIO",
                BaseEditableNomenclatureType.ViewType.NOMENCLATURE_TYPE,
                label = "Statut biologique",
                visible = false,
                default = false,
                value = PropertyValue(
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
                expectedEditableNomenclatureTypes.toTypedArray()
            )
        }

        // then
        assertArrayEquals(
            expectedEditableNomenclatureTypes.toTypedArray(),
            bundle.getParcelableArray("editable_nomenclature_types")
        )
    }
}