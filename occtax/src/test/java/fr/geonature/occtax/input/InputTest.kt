package fr.geonature.occtax.input

import android.os.Parcel
import fr.geonature.commons.data.Taxon
import fr.geonature.commons.data.Taxonomy
import fr.geonature.commons.util.toDate
import java.util.Date
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [Input].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@RunWith(RobolectricTestRunner::class)
class InputTest {

    @Test
    fun testParcelable() {
        // given an Input instance to write
        val input = Input().apply {
            id = 1234
            datasetId = 17
            properties["TYP_GRP"] = PropertyValue(
                "TYP_GRP",
                null,
                133
            )
            date = toDate("2016-10-28") ?: Date()
            setPrimaryInputObserverId(1L)
            addInputObserverId(5L)
            addInputObserverId(2L)
            addInputObserverId(3L)
            comment = "Global comment"
            addInputTaxon(InputTaxon(
                Taxon(
                    10L,
                    "taxon_01",
                    Taxonomy(
                        "Animalia",
                        "Ascidies"
                    )
                )
            ).apply {
                properties["METH_OBS"] = PropertyValue(
                    "METH_OBS",
                    null,
                    41L
                )
                properties["ETA_BIO"] = PropertyValue(
                    "ETA_BIO",
                    null,
                    29L
                )
                properties["METH_DETERMIN"] = PropertyValue(
                    "METH_DETERMIN",
                    null,
                    445L
                )
                properties["DETERMINER"] = PropertyValue(
                    "DETERMINER",
                    null,
                    "Determiner value"
                )
                properties["STATUT_BIO"] = PropertyValue(
                    "STATUT_BIO",
                    null,
                    29L
                )
                properties["NATURALITE"] = PropertyValue(
                    "NATURALITE",
                    null,
                    160L
                )
                properties["PREUVE_EXIST"] = PropertyValue(
                    "PREUVE_EXIST",
                    null,
                    81L
                )
                properties["COMMENT"] = PropertyValue(
                    "COMMENT",
                    null,
                    "Some comment"
                )
                addCountingMetadata(CountingMetadata().apply {
                    properties.putAll(
                        mutableMapOf(
                            Pair(
                                "STADE_VIE",
                                PropertyValue(
                                    "STADE_VIE",
                                    null,
                                    2L
                                )
                            ),
                            Pair(
                                "SEXE",
                                PropertyValue(
                                    "SEXE",
                                    null,
                                    168L
                                )
                            ),
                            Pair(
                                "OBJ_DENBR",
                                PropertyValue(
                                    "OBJ_DENBR",
                                    null,
                                    146L
                                )
                            ),
                            Pair(
                                "TYP_DENBR",
                                PropertyValue(
                                    "TYP_DENBR",
                                    null,
                                    93L
                                )
                            )
                        )
                    )
                    min = 1
                    max = 2
                })
            })
        }

        // when we obtain a Parcel object to write the input instance to it
        val parcel = Parcel.obtain()
        input.writeToParcel(
            parcel,
            0
        )

        // reset the parcel for reading
        parcel.setDataPosition(0)

        // then
        val inputFromParcel = Input.CREATOR.createFromParcel(parcel)
        assertEquals(
            input,
            inputFromParcel
        )
    }
}
