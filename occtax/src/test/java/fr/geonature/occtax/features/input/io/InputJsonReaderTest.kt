package fr.geonature.occtax.features.input.io

import fr.geonature.commons.data.entity.Taxon
import fr.geonature.commons.data.entity.Taxonomy
import fr.geonature.commons.features.input.domain.AbstractInputTaxon
import fr.geonature.commons.features.input.io.InputJsonReader
import fr.geonature.commons.util.toDate
import fr.geonature.occtax.FixtureHelper.getFixture
import fr.geonature.occtax.features.input.domain.CountingMetadata
import fr.geonature.occtax.features.input.domain.Input
import fr.geonature.occtax.features.input.domain.InputTaxon
import fr.geonature.occtax.features.input.domain.PropertyValue
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [InputJsonReader].
 *
 * @author S. Grimault
 */
@RunWith(RobolectricTestRunner::class)
class InputJsonReaderTest {

    private lateinit var inputJsonReader: InputJsonReader<Input>

    @Before
    fun setUp() {
        inputJsonReader = InputJsonReader(OnInputJsonReaderListenerImpl())
    }

    @Test
    fun `should read null input from invalid json string`() {
        // when read an invalid JSON as Input
        val input = inputJsonReader.read("")

        // then
        assertNull(input)
    }

    @Test
    fun `should read empty input`() {
        // given an input file to read
        val json = getFixture("input_empty.json")

        // when parsing this file as Input
        val input = inputJsonReader.read(json)

        assertNotNull(input)
        assertEquals(
            1234L,
            input?.id
        )
        assertEquals(
            Input().module,
            input?.module
        )
        assertNull(input?.datasetId)
    }

    @Test
    fun `should read input`() {
        // given an input file to read
        val json = getFixture("input_simple.json")

        // when parsing this file as Input
        val input = inputJsonReader.read(json)

        // then
        assertNotNull(input)
        assertEquals(
            1234L,
            input!!.id
        )
        assertEquals(
            Input().module,
            input.module
        )
        assertEquals(
            17L,
            input.datasetId
        )
        assertEquals(
            toDate("2016-10-28T08:15:00Z"),
            input.startDate
        )
        assertEquals(
            toDate("2016-10-29T09:00:00Z"),
            input.endDate
        )
        assertEquals(
            1L,
            input.getPrimaryObserverId()
        )
        assertArrayEquals(
            longArrayOf(
                1,
                5,
                2,
                3
            ),
            input.getAllInputObserverIds().toLongArray()
        )
        assertArrayEquals(
            longArrayOf(
                5,
                2,
                3
            ),
            input.getInputObserverIds()
                .toLongArray()
        )
        assertEquals(
            "Global comment",
            input.comment
        )
        assertEquals(
            mapOf(
                Pair(
                    "TYP_GRP",
                    PropertyValue(
                        "TYP_GRP",
                        null,
                        133L
                    )
                )
            ),
            input.properties
        )
        assertArrayEquals(
            listOf(
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
                                "OCC_COMPORTEMENT",
                                null,
                                580L
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
                                    1L
                                ),
                                PropertyValue.fromValue(
                                    "MAX",
                                    2L
                                )
                            ).associateBy { it.code }
                        )
                    })
                }).toTypedArray(),
            input.getInputTaxa().toTypedArray()
        )
    }

    @Test
    fun `should read input with no observer and no taxon`() {
        // given an input file to read
        val json = getFixture("input_no_observer_no_taxon.json")

        // when parsing this file as Input
        val input = inputJsonReader.read(json)

        // then
        assertNotNull(input)
        assertEquals(
            1234L,
            input!!.id
        )
        assertEquals(
            Input().module,
            input.module
        )
        assertNull(input.datasetId)
        assertEquals(
            toDate("2016-10-28"),
            input.startDate
        )
        assertNull(input.getPrimaryObserverId())
        assertArrayEquals(
            longArrayOf(),
            input.getAllInputObserverIds().toLongArray()
        )
        assertArrayEquals(
            longArrayOf(),
            input.getInputObserverIds()
                .toLongArray()
        )
        assertNull(input.geometry)
        assertEquals(
            listOf<AbstractInputTaxon>(),
            input.getInputTaxa()
        )
    }
}
