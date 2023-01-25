package fr.geonature.occtax.features.record.io

import fr.geonature.commons.data.entity.Taxon
import fr.geonature.commons.data.entity.Taxonomy
import fr.geonature.commons.util.toDate
import fr.geonature.occtax.FixtureHelper.getFixture
import fr.geonature.occtax.features.record.domain.ObservationRecord
import fr.geonature.occtax.features.record.domain.PropertyValue
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.robolectric.RobolectricTestRunner
import java.io.IOException

/**
 * Unit tests about [ObservationRecordJsonReader].
 *
 * @author S. Grimault
 */
@RunWith(RobolectricTestRunner::class)
class ObservationRecordJsonReaderTest {

    private lateinit var gf: GeometryFactory

    @Before
    fun setUp() {
        gf = GeometryFactory()
    }

    @Test
    fun `should throw IOException if trying to read an observation record from invalid json string`() {
        // when reading an invalid JSON as observation record
        assertThrows(IOException::class.java) {
            ObservationRecordJsonReader().read("")
        }
    }

    @Test
    fun `should read an empty observation record`() {
        // when reading an observation record from an empty JSON object
        val observationRecord = ObservationRecordJsonReader().read("{}")

        assertNotNull(observationRecord.internalId)
        assertTrue(observationRecord.taxa.taxa.isEmpty())
    }

    @Test
    fun `should read an observation record from a legacy JSON string`() {
        // given an input file to read
        val json = getFixture("input_simple.json")

        // when parsing this file as ObservationRecord
        val observationRecord = ObservationRecordJsonReader().read(json)

        // then
        assertEquals(
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

                        counting.addOrUpdate(counting.create()
                            .apply {
                                listOf(
                                    PropertyValue.Number(
                                        "count_min",
                                        1L
                                    ),
                                    PropertyValue.Number(
                                        "count_max",
                                        2L
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
                            }
                        )
                    }
            },
            observationRecord
        )
    }

    @Test
    fun `should read an observation record from a JSON string`() {
        // given an observation record to read
        val json = getFixture("observation_record_simple.json")

        // when parsing this file as ObservationRecord
        val observationRecord = ObservationRecordJsonReader().read(json)

        // then
        assertEquals(
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

                        counting.addOrUpdate(counting.create()
                            .apply {
                                listOf(
                                    PropertyValue.Number(
                                        "count_min",
                                        1L
                                    ),
                                    PropertyValue.Number(
                                        "count_max",
                                        2L
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
                            }
                        )
                    }
            },
            observationRecord
        )
    }

    @Test
    fun `should read an observation record with an empty taxon from a JSON string`() {
        // given an observation record with an empty taxon to read
        val json = getFixture("observation_record_taxa_empty.json")

        // when parsing this file as ObservationRecord
        val observationRecord = ObservationRecordJsonReader().read(json)

        // then
        assertEquals(
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
            },
            observationRecord
        )
    }

    @Test
    fun `should be the same observation record read from a legacy JSON string or from a new JSON string schema`() {
        // when parsing an input file to read as ObservationRecord
        val observationRecordFromLegacy =
            ObservationRecordJsonReader().read(getFixture("input_simple.json"))

        // and parsing an observation record from JSON string
        val observationRecord =
            ObservationRecordJsonReader().read(getFixture("observation_record_simple.json"))

        assertEquals(
            observationRecordFromLegacy,
            observationRecord
        )
    }
}