package fr.geonature.occtax.features.input.domain

import android.os.Parcel
import fr.geonature.commons.data.entity.Taxon
import fr.geonature.commons.data.entity.Taxonomy
import fr.geonature.commons.util.toDate
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.Date

/**
 * Unit tests about [Input].
 *
 * @author S. Grimault
 */
@RunWith(RobolectricTestRunner::class)
class InputTest {

    @Test
    fun `should create input from Parcel`() {
        // given an Input instance to write
        val input = Input().apply {
            id = 1234
            datasetId = 17
            properties["TYP_GRP"] = PropertyValue(
                "TYP_GRP",
                null,
                133
            )
            startDate = toDate("2016-10-28") ?: Date()
            setPrimaryInputObserverId(1L)
            addInputObserverId(5L)
            addInputObserverId(2L)
            addInputObserverId(3L)
            comment = "Global comment"
            addInputTaxon(
                InputTaxon(
                    Taxon(
                        10L,
                        "taxon_01",
                        Taxonomy(
                            "Animalia",
                            "Ascidies"
                        )
                    )
                ).apply {
                    properties.putAll(
                        listOf(
                            PropertyValue(
                                "METH_OBS",
                                null,
                                41L
                            ),
                            PropertyValue(
                                "ETA_BIO",
                                null,
                                29L
                            ),
                            PropertyValue(
                                "METH_DETERMIN",
                                null,
                                445L
                            ),
                            PropertyValue(
                                "DETERMINER",
                                null,
                                "Determiner value"
                            ),
                            PropertyValue(
                                "STATUT_BIO",
                                null,
                                29L
                            ),
                            PropertyValue(
                                "NATURALITE",
                                null,
                                160L
                            ),
                            PropertyValue(
                                "PREUVE_EXIST",
                                null,
                                81L
                            ),
                            PropertyValue(
                                "COMMENT",
                                null,
                                "Some comment"
                            )
                        ).associateBy { it.code }
                    )
                    addCountingMetadata(CountingMetadata().apply {
                        properties.putAll(
                            listOf(
                                PropertyValue(
                                    "STADE_VIE",
                                    null,
                                    2L
                                ),
                                PropertyValue(
                                    "SEXE",
                                    null,
                                    168L
                                ),
                                PropertyValue(
                                    "OBJ_DENBR",
                                    null,
                                    146L
                                ),
                                PropertyValue(
                                    "TYP_DENBR",
                                    null,
                                    93L
                                ),
                                PropertyValue.fromValue(
                                    "MIN",
                                    1
                                ),
                                PropertyValue.fromValue(
                                    "MAX",
                                    2
                                )
                            ).associateBy { it.code }
                        )
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
