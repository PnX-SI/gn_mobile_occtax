package fr.geonature.occtax.features.input.io

import fr.geonature.commons.data.entity.Taxon
import fr.geonature.commons.data.entity.Taxonomy
import fr.geonature.commons.features.input.io.InputJsonWriter
import fr.geonature.commons.util.toDate
import fr.geonature.commons.util.toIsoDateString
import fr.geonature.commons.util.toMap
import fr.geonature.occtax.FixtureHelper.getFixture
import fr.geonature.occtax.features.input.domain.CountingMetadata
import fr.geonature.occtax.features.input.domain.Input
import fr.geonature.occtax.features.input.domain.InputTaxon
import fr.geonature.occtax.features.input.domain.PropertyValue
import fr.geonature.occtax.settings.AppSettings
import fr.geonature.occtax.settings.InputDateSettings
import fr.geonature.occtax.settings.InputSettings
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Unit tests about [InputJsonWriter].
 *
 * @author S. Grimault
 */
@RunWith(RobolectricTestRunner::class)
class InputJsonWriterTest {

    private lateinit var inputJsonWriter: InputJsonWriter<Input, AppSettings>

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
            startDate = toDate("2016-10-28T08:15:00Z")!!
            endDate = toDate("2016-10-29T09:00:00Z")!!
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
            startDate = toDate("2016-10-28T08:15:00Z")!!
        }

        // when write this Input as JSON
        val json =
            inputJsonWriter.write(input)?.let { JSONObject(it).toMap() } ?: mapOf<String, Any>()

        // then
        assertEquals(
            "2016-10-28T08:15:00Z",
            (json["properties"] as Map<*, *>)["date_min"]
        )
        assertNull((json["properties"] as Map<*, *>)["hour_min"])
        assertEquals(
            input.endDate.toIsoDateString(),
            (json["properties"] as Map<*, *>)["date_max"]
        )
        assertNull((json["properties"] as Map<*, *>)["hour_max"])
    }

    @Test
    fun `should write input with only start date defined following settings startDateSettings=DATE`() {
        // given an Input instance to write
        val input = Input().apply {
            startDate = toDate("2016-10-28T08:15:00Z")!!
        }

        // when write this Input as JSON
        val json = inputJsonWriter.write(
            input,
            AppSettings(
                inputSettings = InputSettings(
                    dateSettings = InputDateSettings(
                        startDateSettings = InputDateSettings.DateSettings.DATE,
                    )
                )
            )
        )?.let { JSONObject(it).toMap() } ?: mapOf<String, Any>()

        // then
        assertEquals(
            DateTimeFormatter.ofPattern("yyyy-MM-dd")
                .withZone(ZoneId.systemDefault())
                .format(input.startDate.toInstant()),
            (json["properties"] as Map<*, *>)["date_min"]
        )
        assertNull((json["properties"] as Map<*, *>)["hour_min"])
        assertEquals(
            DateTimeFormatter.ofPattern("yyyy-MM-dd")
                .withZone(ZoneId.systemDefault())
                .format(input.startDate.toInstant()),
            (json["properties"] as Map<*, *>)["date_max"]
        )
        assertNull((json["properties"] as Map<*, *>)["hour_max"])
    }

    @Test
    fun `should write input with only start date defined following settings startDateSettings=DATETIME`() {
        // given an Input instance to write
        val input = Input().apply {
            startDate = toDate("2016-10-28T08:15:00Z")!!
        }

        // when write this Input as JSON
        val json = inputJsonWriter.write(
            input,
            AppSettings(
                inputSettings = InputSettings(
                    dateSettings = InputDateSettings(
                        startDateSettings = InputDateSettings.DateSettings.DATETIME,
                    )
                )
            )
        )?.let { JSONObject(it).toMap() } ?: mapOf<String, Any>()

        // then
        assertEquals(
            DateTimeFormatter.ofPattern("yyyy-MM-dd")
                .withZone(ZoneId.systemDefault())
                .format(input.startDate.toInstant()),
            (json["properties"] as Map<*, *>)["date_min"]
        )
        assertEquals(
            DateTimeFormatter.ofPattern("HH:mm")
                .withZone(ZoneId.systemDefault())
                .format(input.startDate.toInstant()),
            (json["properties"] as Map<*, *>)["hour_min"]
        )
        assertEquals(
            DateTimeFormatter.ofPattern("yyyy-MM-dd")
                .withZone(ZoneId.systemDefault())
                .format(input.startDate.toInstant()),
            (json["properties"] as Map<*, *>)["date_max"]
        )
        assertEquals(
            DateTimeFormatter.ofPattern("HH:mm")
                .withZone(ZoneId.systemDefault())
                .format(input.startDate.toInstant()),
            (json["properties"] as Map<*, *>)["hour_max"]
        )
    }

    @Test
    fun `should write input with start and end date defined following settings startDateSettings=DATE, endDateSettings=DATE`() {
        // given an Input instance to write
        val input = Input().apply {
            startDate = toDate("2016-10-28T08:15:00Z")!!
            endDate = toDate("2016-10-29T09:00:00Z")!!
        }

        // when write this Input as JSON
        val json = inputJsonWriter.write(
            input,
            AppSettings(
                inputSettings = InputSettings(
                    dateSettings = InputDateSettings(
                        startDateSettings = InputDateSettings.DateSettings.DATE,
                        endDateSettings = InputDateSettings.DateSettings.DATE
                    )
                )
            )
        )?.let { JSONObject(it).toMap() } ?: mapOf<String, Any>()

        // then
        assertEquals(
            DateTimeFormatter.ofPattern("yyyy-MM-dd")
                .withZone(ZoneId.systemDefault())
                .format(input.startDate.toInstant()),
            (json["properties"] as Map<*, *>)["date_min"]
        )
        assertNull((json["properties"] as Map<*, *>)["hour_min"])
        assertEquals(
            DateTimeFormatter.ofPattern("yyyy-MM-dd")
                .withZone(ZoneId.systemDefault())
                .format(input.endDate.toInstant()),
            (json["properties"] as Map<*, *>)["date_max"]
        )
        assertNull((json["properties"] as Map<*, *>)["hour_max"])
    }

    @Test
    fun `should write input with start and end date defined following settings startDateSettings=DATETIME, endDateSettings=DATETIME`() {
        // given an Input instance to write
        val input = Input().apply {
            startDate = toDate("2016-10-28T08:15:00Z")!!
            endDate = toDate("2016-10-29T09:00:00Z")!!
        }

        // when write this Input as JSON
        val json = inputJsonWriter.write(
            input,
            AppSettings(
                inputSettings = InputSettings(
                    dateSettings = InputDateSettings(
                        startDateSettings = InputDateSettings.DateSettings.DATETIME,
                        endDateSettings = InputDateSettings.DateSettings.DATETIME
                    )
                )
            )
        )?.let { JSONObject(it).toMap() } ?: mapOf<String, Any>()

        // then
        assertEquals(
            DateTimeFormatter.ofPattern("yyyy-MM-dd")
                .withZone(ZoneId.systemDefault())
                .format(input.startDate.toInstant()),
            (json["properties"] as Map<*, *>)["date_min"]
        )
        assertEquals(
            DateTimeFormatter.ofPattern("HH:mm")
                .withZone(ZoneId.systemDefault())
                .format(input.startDate.toInstant()),
            (json["properties"] as Map<*, *>)["hour_min"]
        )
        assertEquals(
            DateTimeFormatter.ofPattern("yyyy-MM-dd")
                .withZone(ZoneId.systemDefault())
                .format(input.endDate.toInstant()),
            (json["properties"] as Map<*, *>)["date_max"]
        )
        assertEquals(
            DateTimeFormatter.ofPattern("HH:mm")
                .withZone(ZoneId.systemDefault())
                .format(input.endDate.toInstant()),
            (json["properties"] as Map<*, *>)["hour_max"]
        )
    }
}
