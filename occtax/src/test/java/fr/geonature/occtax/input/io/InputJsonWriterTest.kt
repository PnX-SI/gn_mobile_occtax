package fr.geonature.occtax.input.io

import fr.geonature.commons.data.entity.Taxon
import fr.geonature.commons.data.entity.Taxonomy
import fr.geonature.commons.input.io.InputJsonWriter
import fr.geonature.commons.util.toDate
import fr.geonature.occtax.FixtureHelper.getFixture
import fr.geonature.occtax.input.CountingMetadata
import fr.geonature.occtax.input.Input
import fr.geonature.occtax.input.InputTaxon
import fr.geonature.occtax.input.PropertyValue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.Date

/**
 * Unit tests about [InputJsonWriter].
 *
 * @author S. Grimault
 */
@RunWith(RobolectricTestRunner::class)
class InputJsonWriterTest {

    private lateinit var inputJsonWriter: InputJsonWriter<Input>

    @Before
    fun setUp() {
        inputJsonWriter = InputJsonWriter(OnInputJsonWriterListenerImpl())
    }

    @Test
    fun `should write input with start date and end date defined`() {
        // given an Input instance to write
        val input = Input().apply {
            id = 1234
            datasetId = 17
            properties["TYP_GRP"] = PropertyValue(
                "TYP_GRP",
                null,
                133L
            )
            startDate = toDate("2016-10-28T08:15:00Z") ?: Date()
            endDate = toDate("2016-10-29T09:00:00Z")
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
                properties["OCC_COMPORTEMENT"] = PropertyValue(
                    "OCC_COMPORTEMENT",
                    null,
                    580L
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

        // when write this Input as JSON string
        val json = inputJsonWriter.setIndent("  ")
            .write(input)

        // then
        assertNotNull(json)
        assertEquals(
            getFixture("input_simple.json"),
            json
        )
    }

    @Test
    fun `should write input with only start date defined`() {
        // given an Input instance to write
        val input = Input().apply {
            id = 1234
            datasetId = 17
            properties["TYP_GRP"] = PropertyValue(
                "TYP_GRP",
                null,
                133L
            )
            startDate = toDate("2016-10-28T08:15:00Z") ?: Date()
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
                properties["OCC_COMPORTEMENT"] = PropertyValue(
                    "OCC_COMPORTEMENT",
                    null,
                    580L
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

        // when write this Input as JSON string
        val json = inputJsonWriter.setIndent("  ")
            .write(input)

        // then
        assertNotNull(json)
        assertEquals(
            getFixture("input_simple_date_start.json"),
            json
        )
    }
}
