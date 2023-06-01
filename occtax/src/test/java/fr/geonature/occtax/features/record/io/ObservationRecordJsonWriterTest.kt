package fr.geonature.occtax.features.record.io

import fr.geonature.commons.data.entity.Taxon
import fr.geonature.commons.data.entity.Taxonomy
import fr.geonature.commons.util.toDate
import fr.geonature.commons.util.toIsoDateString
import fr.geonature.commons.util.toMap
import fr.geonature.occtax.FixtureHelper.getFixture
import fr.geonature.occtax.features.record.domain.ModuleRecord
import fr.geonature.occtax.features.record.domain.ObservationRecord
import fr.geonature.occtax.features.record.domain.PropertyValue
import fr.geonature.occtax.settings.AppSettings
import fr.geonature.occtax.settings.InputDateSettings
import fr.geonature.occtax.settings.InputSettings
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.robolectric.RobolectricTestRunner
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Unit tests about [ObservationRecordJsonWriter].
 *
 * @author S. Grimault
 */
@RunWith(RobolectricTestRunner::class)
class ObservationRecordJsonWriterTest {

    private lateinit var gf: GeometryFactory

    @Before
    fun setUp() {
        gf = GeometryFactory()
    }

    @Test
    fun `should write an empty observation record`() {
        // when writing an empty observation record
        val json = ObservationRecordJsonWriter().setIndent("  ")
            .write(ObservationRecord(internalId = 1234).apply {
                dates.start = toDate("2016-10-28T08:15:00Z")!!
                dates.end = toDate("2016-10-29T09:00:00Z")!!
            })

        // then
        assertEquals(
            getFixture("observation_record_empty.json"),
            json
        )
    }

    @Test
    fun `should write an observation record`() {
        // when writing an observation record
        val json = ObservationRecordJsonWriter().setIndent("  ")
            .write(ObservationRecord(
                internalId = 1234L,
                id = 1234L,
                geometry = gf.createPoint(
                    Coordinate(
                        -1.554476,
                        47.225782
                    )
                ),
                status = ObservationRecord.Status.DRAFT
            ).apply {
                comment.comment = "Global comment"
                dataset.datasetId = 17L
                dates.start = toDate("2016-10-28T08:15:00Z")!!
                dates.end = toDate("2016-10-29T09:00:00Z")!!

                PropertyValue.Nomenclature(
                    "TYP_GRP",
                    label = null,
                    133L
                )
                    .toPair()
                    .also {
                        properties[it.first] = it.second
                    }

                with(observers) {
                    setPrimaryObserverId(1L)
                    addObserverId(5)
                    addObserverId(2)
                    addObserverId(3)
                }
                taxa.add(
                    Taxon(
                        10L,
                        "taxon_01",
                        Taxonomy(
                            "Animalia",
                            "Ascidies"
                        )
                    )
                )
                    .apply {
                        listOf(
                            PropertyValue.Text(
                                "comment",
                                "Some comment"
                            ),
                            PropertyValue.Text(
                                "determiner",
                                "Determiner value"
                            ),
                            PropertyValue.Nomenclature(
                                "ETA_BIO",
                                null,
                                29
                            ),
                            PropertyValue.Nomenclature(
                                "METH_DETERMIN",
                                null,
                                445
                            ),
                            PropertyValue.Nomenclature(
                                "METH_OBS",
                                null,
                                41
                            ),
                            PropertyValue.Nomenclature(
                                "NATURALITE",
                                null,
                                160
                            ),
                            PropertyValue.Nomenclature(
                                "OCC_COMPORTEMENT",
                                null,
                                580
                            ),
                            PropertyValue.Nomenclature(
                                "PREUVE_EXIST",
                                null,
                                81
                            ),
                            PropertyValue.Nomenclature(
                                "STATUT_BIO",
                                null,
                                29
                            )
                        ).map { it.toPair() }
                            .forEach { properties[it.first] = it.second }

                        additionalFields = listOf(
                            PropertyValue.Text(
                                "some_field_text",
                                "some_value"
                            ),
                            PropertyValue.Number(
                                "some_field_number",
                                42L
                            ),
                            PropertyValue.StringArray(
                                "some_field_array_string",
                                arrayOf(
                                    "val1",
                                    "val2"
                                )
                            ),
                            PropertyValue.NumberArray(
                                "some_field_array_number",
                                arrayOf(
                                    3L,
                                    8L
                                )
                            )
                        )

                        counting.addOrUpdate(counting.create()
                            .apply {
                                listOf(
                                    PropertyValue.Number(
                                        "count_min",
                                        1
                                    ),
                                    PropertyValue.Number(
                                        "count_max",
                                        2
                                    ),
                                    PropertyValue.Nomenclature(
                                        "OBJ_DENBR",
                                        null,
                                        146
                                    ),
                                    PropertyValue.Nomenclature(
                                        "SEXE",
                                        null,
                                        168
                                    ),
                                    PropertyValue.Nomenclature(
                                        "STADE_VIE",
                                        null,
                                        2
                                    ),
                                    PropertyValue.Nomenclature(
                                        "TYP_DENBR",
                                        null,
                                        93
                                    )
                                ).map { it.toPair() }
                                    .forEach { properties[it.first] = it.second }

                                additionalFields = listOf(
                                    PropertyValue.Text(
                                        "some_field_text",
                                        "some_value"
                                    )
                                )
                            }
                        )
                    }
                taxa.selectedTaxonRecord = null
            })

        // then
        assertEquals(
            getFixture("observation_record_complete.json"),
            json
        )
    }

    @Test
    fun `should write an observation record with only start date defined`() {
        // given an observation record instance to write
        val observationRecord = ObservationRecord(
            internalId = 1234L
        ).apply {
            dates.start = toDate("2016-10-28T08:15:00Z")!!
        }

        // when writing this observation record as JSON
        val json =
            ObservationRecordJsonWriter().write(observationRecord)
                .let { JSONObject(it).toMap() }

        // then
        assertEquals(
            "2016-10-28T08:15:00Z",
            (json["properties"] as Map<*, *>)["date_min"]
        )
        assertNull((json["properties"] as Map<*, *>)["hour_min"])
        assertEquals(
            observationRecord.dates.end.toIsoDateString(),
            (json["properties"] as Map<*, *>)["date_max"]
        )
        assertNull((json["properties"] as Map<*, *>)["hour_max"])
    }

    @Test
    fun `should write an observation record with only start date defined following settings startDateSettings=DATE`() {
        // given an observation record instance to write
        val observationRecord = ObservationRecord(
            internalId = 1234L
        ).apply {
            dates.start = toDate("2016-10-28T08:15:00Z")!!
        }

        // when writing this observation record as JSON
        val json = ObservationRecordJsonWriter().write(
            observationRecord,
            AppSettings(
                inputSettings = InputSettings(
                    dateSettings = InputDateSettings(
                        startDateSettings = InputDateSettings.DateSettings.DATE,
                    )
                )
            )
        )
            .let { JSONObject(it).toMap() }

        // then
        assertEquals(
            DateTimeFormatter.ofPattern("yyyy-MM-dd")
                .withZone(ZoneId.systemDefault())
                .format(observationRecord.dates.start.toInstant()),
            (json["properties"] as Map<*, *>)["date_min"]
        )
        assertNull((json["properties"] as Map<*, *>)["hour_min"])
        assertEquals(
            DateTimeFormatter.ofPattern("yyyy-MM-dd")
                .withZone(ZoneId.systemDefault())
                .format(observationRecord.dates.start.toInstant()),
            (json["properties"] as Map<*, *>)["date_max"]
        )
        assertNull((json["properties"] as Map<*, *>)["hour_max"])
    }

    @Test
    fun `should write an observation record with only start date defined following settings startDateSettings=DATETIME`() {
        // given an observation record instance to write
        val observationRecord = ObservationRecord(
            internalId = 1234L
        ).apply {
            dates.start = toDate("2016-10-28T08:15:00Z")!!
        }

        // when writing this observation record as JSON
        val json = ObservationRecordJsonWriter().write(
            observationRecord,
            AppSettings(
                inputSettings = InputSettings(
                    dateSettings = InputDateSettings(
                        startDateSettings = InputDateSettings.DateSettings.DATETIME,
                    )
                )
            )
        )
            .let { JSONObject(it).toMap() }

        // then
        assertEquals(
            DateTimeFormatter.ofPattern("yyyy-MM-dd")
                .withZone(ZoneId.systemDefault())
                .format(observationRecord.dates.start.toInstant()),
            (json["properties"] as Map<*, *>)["date_min"]
        )
        assertEquals(
            DateTimeFormatter.ofPattern("HH:mm")
                .withZone(ZoneId.systemDefault())
                .format(observationRecord.dates.start.toInstant()),
            (json["properties"] as Map<*, *>)["hour_min"]
        )
        assertEquals(
            DateTimeFormatter.ofPattern("yyyy-MM-dd")
                .withZone(ZoneId.systemDefault())
                .format(observationRecord.dates.start.toInstant()),
            (json["properties"] as Map<*, *>)["date_max"]
        )
        assertEquals(
            DateTimeFormatter.ofPattern("HH:mm")
                .withZone(ZoneId.systemDefault())
                .format(observationRecord.dates.start.toInstant()),
            (json["properties"] as Map<*, *>)["hour_max"]
        )
    }

    @Test
    fun `should write an observation record with start and end date defined following settings startDateSettings=DATE, endDateSettings=DATE`() {
        // given an observation record instance to write
        val observationRecord = ObservationRecord(
            internalId = 1234L
        ).apply {
            dates.start = toDate("2016-10-28T08:15:00Z")!!
            dates.end = toDate("2016-10-29T09:00:00Z")!!
        }

        // when writing this observation record as JSON
        val json = ObservationRecordJsonWriter().write(
            observationRecord,
            AppSettings(
                inputSettings = InputSettings(
                    dateSettings = InputDateSettings(
                        startDateSettings = InputDateSettings.DateSettings.DATE,
                        endDateSettings = InputDateSettings.DateSettings.DATE
                    )
                )
            )
        )
            .let { JSONObject(it).toMap() }

        // then
        assertEquals(
            DateTimeFormatter.ofPattern("yyyy-MM-dd")
                .withZone(ZoneId.systemDefault())
                .format(observationRecord.dates.start.toInstant()),
            (json["properties"] as Map<*, *>)["date_min"]
        )
        assertNull((json["properties"] as Map<*, *>)["hour_min"])
        assertEquals(
            DateTimeFormatter.ofPattern("yyyy-MM-dd")
                .withZone(ZoneId.systemDefault())
                .format(observationRecord.dates.end.toInstant()),
            (json["properties"] as Map<*, *>)["date_max"]
        )
        assertNull((json["properties"] as Map<*, *>)["hour_max"])
    }

    @Test
    fun `should write an observation record with start and end date defined following settings startDateSettings=DATETIME, endDateSettings=DATETIME`() {
        // given an observation record instance to write
        val observationRecord = ObservationRecord(
            internalId = 1234L
        ).apply {
            dates.start = toDate("2016-10-28T08:15:00Z")!!
            dates.end = toDate("2016-10-29T09:00:00Z")!!
        }

        // when writing this observation record as JSON
        val json = ObservationRecordJsonWriter().write(
            observationRecord,
            AppSettings(
                inputSettings = InputSettings(
                    dateSettings = InputDateSettings(
                        startDateSettings = InputDateSettings.DateSettings.DATETIME,
                        endDateSettings = InputDateSettings.DateSettings.DATETIME
                    )
                )
            )
        )
            .let { JSONObject(it).toMap() }

        // then
        assertEquals(
            DateTimeFormatter.ofPattern("yyyy-MM-dd")
                .withZone(ZoneId.systemDefault())
                .format(observationRecord.dates.start.toInstant()),
            (json["properties"] as Map<*, *>)["date_min"]
        )
        assertEquals(
            DateTimeFormatter.ofPattern("HH:mm")
                .withZone(ZoneId.systemDefault())
                .format(observationRecord.dates.start.toInstant()),
            (json["properties"] as Map<*, *>)["hour_min"]
        )
        assertEquals(
            DateTimeFormatter.ofPattern("yyyy-MM-dd")
                .withZone(ZoneId.systemDefault())
                .format(observationRecord.dates.end.toInstant()),
            (json["properties"] as Map<*, *>)["date_max"]
        )
        assertEquals(
            DateTimeFormatter.ofPattern("HH:mm")
                .withZone(ZoneId.systemDefault())
                .format(observationRecord.dates.end.toInstant()),
            (json["properties"] as Map<*, *>)["hour_max"]
        )
    }

    @Test
    fun `should write an observation record with module name defined`() {
        // given an observation record instance to write
        val observationRecord = ObservationRecord(
            internalId = 1234L
        ).apply {
            module.module = "occtax"
        }

        // when writing this observation record as JSON
        val json = ObservationRecordJsonWriter().write(observationRecord)
            .let { JSONObject(it).toMap() }

        // then
        assertEquals(
            observationRecord.module.module,
            (json["properties"] as Map<*, *>)[ModuleRecord.MODULE_KEY]
        )
    }
}