package fr.geonature.occtax.features.nomenclature.domain

import android.os.Parcel
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
            default = false
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
}