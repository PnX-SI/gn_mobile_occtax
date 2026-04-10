package fr.geonature.occtax.features.record.io

import fr.geonature.commons.data.entity.Taxon
import fr.geonature.commons.data.entity.Taxonomy
import fr.geonature.commons.util.toDate
import fr.geonature.commons.util.toMap
import fr.geonature.datasync.settings.DataSyncSettings
import fr.geonature.maps.settings.LayerSettings
import fr.geonature.maps.settings.MapSettings
import fr.geonature.occtax.FixtureHelper.getFixture
import fr.geonature.occtax.features.record.domain.ObservationRecord
import fr.geonature.occtax.features.record.domain.PropertyValue
import fr.geonature.occtax.features.settings.domain.AppSettings
import fr.geonature.occtax.features.settings.domain.InputDateSettings
import fr.geonature.occtax.features.settings.domain.InputSettings
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.robolectric.RobolectricTestRunner
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.time.DurationUnit
import kotlin.time.toDuration

/**
 * Unit tests about [ObservationRecordAPIJsonWriter].
 *
 * @author S. Grimault
 */
@RunWith(RobolectricTestRunner::class)
class ObservationRecordAPIJsonWriterTest {

    private lateinit var gf: GeometryFactory

    @Before
    fun setUp() {
        gf = GeometryFactory()
    }

    @Test
    fun `should write an empty observation record without settings`() {
        // when writing an empty observation record
        val json = ObservationRecordAPIJsonWriter().setIndent("  ")
            .write(
                ObservationRecord(
                    internalId = 1234,
                    id = 1235
                ).apply {
                    module.module = "occtax"
                    dates.start = toDate("2016-10-28T08:15:00Z")!!
                    dates.end = toDate("2016-10-29T09:00:00Z")!!
                })

        // then
        assertEquals(
            getFixture("observation_record_api_empty.json"),
            json
        )
    }

    @Test
    fun `should write an observation record with taxa`() {
        // when writing an observation record with taxa
        val json = ObservationRecordAPIJsonWriter().setIndent("  ")
            .write(
                ObservationRecord(
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
                    dataset.setDatasetId(17L)
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
                        ),
                        internalId = 12341L
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
                                    "STATUT_OBS",
                                    null,
                                    84
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
                                PropertyValue.Date(
                                    "some_field_date",
                                    toDate("2016-10-28")
                                ),
                                PropertyValue.Time(
                                    code = "some_field_time",
                                    hour = 8,
                                    minute = 15
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

                            counting.addOrUpdate(
                                counting.create()
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
                                            ),
                                            PropertyValue.Date(
                                                "some_field_date_counting",
                                                toDate("2009-01-03")
                                            ),
                                            PropertyValue.Time(
                                                code = "some_field_time_counting",
                                                hour = 13,
                                                minute = 0
                                            ),
                                            PropertyValue.Number(
                                                "some_field_number",
                                                3.14
                                            )
                                        )
                                    }
                            )
                        }
                    taxa.selectedTaxonRecord = null
                },
                AppSettings(
                    dataSyncSettings = DataSyncSettings(
                        geoNatureServerUrl = "https://demo.geonature.fr/geonature",
                        taxHubServerUrl = "https://demo.geonature.fr/taxhub",
                        applicationId = 3,
                        usersListId = 1,
                        taxrefListId = 100,
                        codeAreaType = "M10",
                        pageSize = 1000,
                        dataSyncPeriodicity = 30.toDuration(DurationUnit.MINUTES),
                        essentialDataSyncPeriodicity = 20.toDuration(DurationUnit.MINUTES)
                    ),
                    mapSettings = MapSettings.Builder()
                        .addLayer(
                            LayerSettings.Builder.newInstance()
                                .label("OSM")
                                .addSource("https://a.tile.openstreetmap.org")
                                .build()
                        )
                        .build(),
                    inputSettings = InputSettings(
                        dateSettings = InputDateSettings(
                            startDateSettings = InputDateSettings.DateSettings.DATE,
                            endDateSettings = InputDateSettings.DateSettings.DATE
                        )
                    )
                )
            )

        // then
        assertEquals(
            getFixture("observation_record_api_complete.json"),
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
            ObservationRecordAPIJsonWriter().write(observationRecord)
                .let { JSONObject(it).toMap() }

        // then
        assertEquals(
            "2016-10-28",
            (json["properties"] as Map<*, *>)["date_min"]
        )
        assertNull((json["properties"] as Map<*, *>)["hour_min"])
        assertEquals(
            "2016-10-28",
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
        val json = ObservationRecordAPIJsonWriter().write(
            observationRecord,
            AppSettings(
                dataSyncSettings = DataSyncSettings(
                    geoNatureServerUrl = "https://demo.geonature.fr/geonature",
                    taxHubServerUrl = "https://demo.geonature.fr/taxhub",
                    applicationId = 3,
                    usersListId = 1,
                    taxrefListId = 100,
                    codeAreaType = "M10",
                    pageSize = 1000,
                    dataSyncPeriodicity = 30.toDuration(DurationUnit.MINUTES),
                    essentialDataSyncPeriodicity = 20.toDuration(DurationUnit.MINUTES)
                ),
                mapSettings = MapSettings.Builder()
                    .addLayer(
                        LayerSettings.Builder.newInstance()
                            .label("OSM")
                            .addSource("https://a.tile.openstreetmap.org")
                            .build()
                    )
                    .build(),
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
        val json = ObservationRecordAPIJsonWriter().write(
            observationRecord,
            AppSettings(
                dataSyncSettings = DataSyncSettings(
                    geoNatureServerUrl = "https://demo.geonature.fr/geonature",
                    taxHubServerUrl = "https://demo.geonature.fr/taxhub",
                    applicationId = 3,
                    usersListId = 1,
                    taxrefListId = 100,
                    codeAreaType = "M10",
                    pageSize = 1000,
                    dataSyncPeriodicity = 30.toDuration(DurationUnit.MINUTES),
                    essentialDataSyncPeriodicity = 20.toDuration(DurationUnit.MINUTES)
                ),
                mapSettings = MapSettings.Builder()
                    .addLayer(
                        LayerSettings.Builder.newInstance()
                            .label("OSM")
                            .addSource("https://a.tile.openstreetmap.org")
                            .build()
                    )
                    .build(),
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
        val json = ObservationRecordAPIJsonWriter().write(
            observationRecord,
            AppSettings(
                dataSyncSettings = DataSyncSettings(
                    geoNatureServerUrl = "https://demo.geonature.fr/geonature",
                    taxHubServerUrl = "https://demo.geonature.fr/taxhub",
                    applicationId = 3,
                    usersListId = 1,
                    taxrefListId = 100,
                    codeAreaType = "M10",
                    pageSize = 1000,
                    dataSyncPeriodicity = 30.toDuration(DurationUnit.MINUTES),
                    essentialDataSyncPeriodicity = 20.toDuration(DurationUnit.MINUTES)
                ),
                mapSettings = MapSettings.Builder()
                    .addLayer(
                        LayerSettings.Builder.newInstance()
                            .label("OSM")
                            .addSource("https://a.tile.openstreetmap.org")
                            .build()
                    )
                    .build(),
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
        val json = ObservationRecordAPIJsonWriter().write(
            observationRecord,
            AppSettings(
                dataSyncSettings = DataSyncSettings(
                    geoNatureServerUrl = "https://demo.geonature.fr/geonature",
                    taxHubServerUrl = "https://demo.geonature.fr/taxhub",
                    applicationId = 3,
                    usersListId = 1,
                    taxrefListId = 100,
                    codeAreaType = "M10",
                    pageSize = 1000,
                    dataSyncPeriodicity = 30.toDuration(DurationUnit.MINUTES),
                    essentialDataSyncPeriodicity = 20.toDuration(DurationUnit.MINUTES)
                ),
                mapSettings = MapSettings.Builder()
                    .addLayer(
                        LayerSettings.Builder.newInstance()
                            .label("OSM")
                            .addSource("https://a.tile.openstreetmap.org")
                            .build()
                    )
                    .build(),
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
}