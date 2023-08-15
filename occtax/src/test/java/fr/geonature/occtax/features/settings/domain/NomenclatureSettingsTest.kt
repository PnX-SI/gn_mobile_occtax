package fr.geonature.occtax.features.settings.domain

import android.os.Parcel
import kotlinx.parcelize.parcelableCreator
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [NomenclatureSettings].
 *
 * @author S. Grimault
 */
@RunWith(RobolectricTestRunner::class)
class NomenclatureSettingsTest {

    @Test
    fun `should create NomenclatureSettings from Parcelable`() {
        // given a NomenclatureSettings instance
        val nomenclatureSettings = NomenclatureSettings(
            saveDefaultValues = true,
            information = listOf(
                PropertySettings(
                    "METH_OBS",
                    visible = true,
                    default = true
                ),
                PropertySettings(
                    "ETA_BIO",
                    visible = true,
                    default = true
                ),
                PropertySettings(
                    "METH_DETERMIN",
                    visible = true,
                    default = false
                ),
                PropertySettings(
                    "STATUT_BIO",
                    visible = true,
                    default = false
                ),
                PropertySettings(
                    "NATURALITE",
                    visible = true,
                    default = false
                ),
                PropertySettings(
                    "PREUVE_EXIST",
                    visible = true,
                    default = false
                )
            ),
            counting = listOf(
                PropertySettings(
                    "STADE_VIE",
                    visible = true,
                    default = true
                ),
                PropertySettings(
                    "SEXE",
                    visible = true,
                    default = true
                ),
                PropertySettings(
                    "OBJ_DENBR",
                    visible = true,
                    default = true
                ),
                PropertySettings(
                    "TYP_DENBR",
                    visible = true,
                    default = true
                )
            )
        )

        // when we obtain a Parcel object to write NomenclatureSettings instance to it
        val parcel = Parcel.obtain()
        nomenclatureSettings.writeToParcel(
            parcel,
            0
        )

        // reset the parcel for reading
        parcel.setDataPosition(0)

        // then
        assertEquals(
            nomenclatureSettings,
            parcelableCreator<NomenclatureSettings>().createFromParcel(parcel)
        )
    }
}