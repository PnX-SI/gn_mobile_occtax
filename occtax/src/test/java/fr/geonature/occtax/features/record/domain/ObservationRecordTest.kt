package fr.geonature.occtax.features.record.domain

import android.os.Parcel
import fr.geonature.commons.data.entity.Taxon
import fr.geonature.commons.data.entity.Taxonomy
import fr.geonature.commons.util.add
import kotlinx.parcelize.parcelableCreator
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.robolectric.RobolectricTestRunner
import java.util.Calendar
import java.util.Date

/**
 * Unit tests about [ObservationRecord].
 *
 * @author S. Grimault
 */
@RunWith(RobolectricTestRunner::class)
class ObservationRecordTest {

    private lateinit var gf: GeometryFactory

    @Before
    fun setUp() {
        gf = GeometryFactory()
    }

    @Test
    fun `should have default ID`() {
        // given an empty observation record
        val record = ObservationRecord()

        // then
        assertTrue(record.internalId > 0)
    }

    @Test
    fun `should have default status`() {
        // given an empty observation record
        val record = ObservationRecord()

        // then
        assertTrue(record.status == ObservationRecord.Status.DRAFT)
    }

    @Test
    fun `should set comment`() {
        val record = ObservationRecord(internalId = 1234L)
        record.comment.comment = "this is a comment"

        assertEquals(
            PropertyValue.Text(
                CommentRecord.COMMENT_KEY,
                "this is a comment"
            ),
            record.properties[CommentRecord.COMMENT_KEY]
        )
        assertEquals(
            "this is a comment",
            record.comment.comment
        )
    }

    @Test
    fun `should set dataset`() {
        val record = ObservationRecord(internalId = 1234L)
        record.dataset.datasetId = 17L

        assertEquals(
            PropertyValue.Number(
                DatasetRecord.DATASET_ID_KEY,
                17L
            ),
            record.properties[DatasetRecord.DATASET_ID_KEY]
        )
        assertEquals(
            17L,
            record.dataset.datasetId
        )
    }

    @Test
    fun `should set the start date`() {
        val now = Date()

        val record = ObservationRecord(internalId = 1234L)
        record.dates.start = now

        assertEquals(
            PropertyValue.Date(
                DatesRecord.DATE_MIN_KEY,
                now
            ),
            record.properties[DatesRecord.DATE_MIN_KEY]
        )
        assertEquals(
            PropertyValue.Date(
                DatesRecord.DATE_MAX_KEY,
                now,
            ),
            record.properties[DatesRecord.DATE_MAX_KEY]
        )
        assertEquals(
            now,
            record.dates.start
        )
        assertEquals(
            now,
            record.dates.end
        )
    }

    @Test
    fun `should set the start date after the end date`() {
        val now = Date()

        val record = ObservationRecord(internalId = 1234L)
        record.dates.end = now
        record.dates.start = now.add(
            Calendar.HOUR,
            1
        )

        assertEquals(
            record.dates.start,
            record.dates.end
        )
        assertEquals(
            now.add(
                Calendar.HOUR,
                1
            ),
            record.dates.start
        )
    }

    @Test
    fun `should set the end date`() {
        val now = Date()

        val record = ObservationRecord(internalId = 1234L)
        record.dates.start = now
        record.dates.end = now.add(
            Calendar.HOUR,
            1
        )

        assertEquals(
            PropertyValue.Date(
                DatesRecord.DATE_MIN_KEY,
                now
            ),
            record.properties[DatesRecord.DATE_MIN_KEY]
        )
        assertEquals(
            PropertyValue.Date(
                DatesRecord.DATE_MAX_KEY,
                now.add(
                    Calendar.HOUR,
                    1
                )
            ),
            record.properties[DatesRecord.DATE_MAX_KEY]
        )
        assertEquals(
            now,
            record.dates.start
        )
        assertEquals(
            now.add(
                Calendar.HOUR,
                1
            ),
            record.dates.end
        )
    }

    @Test
    fun `should set the end date before the start date`() {
        val now = Date()

        val record = ObservationRecord(internalId = 1234L)
        record.dates.start = now
        record.dates.end = now.add(
            Calendar.HOUR,
            -1
        )

        assertEquals(
            record.dates.start,
            record.dates.end
        )
        assertEquals(
            now.add(
                Calendar.HOUR,
                -1
            ),
            record.dates.end
        )
    }

    @Test
    fun `should set the module name`() {
        // given an observation record
        val record = ObservationRecord(internalId = 1234L)

        // then
        assertNull(record.module.module)

        // when setting the module name
        record.module.module = "occtax"

        // then
        assertEquals(
            "occtax",
            record.module.module
        )
    }

    @Test
    fun `should set the primary observer`() {
        // given an observation record with some observers
        val record = ObservationRecord(internalId = 1234L).apply {
            observers.addObserverId(8L)
            observers.addObserverId(6L)
        }

        // then
        assertEquals(
            8L,
            record.observers.getPrimaryObserverId()
        )
        assertEquals(
            PropertyValue.Number(
                ObserversRecord.DIGITISER_KEY,
                8L
            ),
            record.properties[ObserversRecord.DIGITISER_KEY]
        )
        assertArrayEquals(
            arrayOf(
                8L,
                6L
            ),
            record.observers.getAllObserverIds()
                .toTypedArray()
        )

        // when we set the current primary observer
        record.observers.setPrimaryObserverId(7L)

        // then
        assertEquals(
            7L,
            record.observers.getPrimaryObserverId()
        )
        assertEquals(
            PropertyValue.Number(
                ObserversRecord.DIGITISER_KEY,
                7L
            ),
            record.properties[ObserversRecord.DIGITISER_KEY]
        )
        assertArrayEquals(
            arrayOf(
                7L,
                8L,
                6L
            ),
            record.observers.getAllObserverIds()
                .toTypedArray()
        )
    }

    @Test
    fun `should get all observers`() {
        // given an observation record with some observers
        val record = ObservationRecord(internalId = 1234L).apply {
            observers.addObserverId(8L)
            observers.addObserverId(6L)
            observers.setPrimaryObserverId(7L)
        }

        // then
        assertArrayEquals(
            arrayOf(
                7L,
                8L,
                6L
            ),
            record.observers.getAllObserverIds()
                .toTypedArray()
        )
    }

    @Test
    fun `should clear all observers`() {
        // given an observation record with some observers
        val record = ObservationRecord(internalId = 1234L).apply {
            observers.addObserverId(8L)
            observers.addObserverId(6L)
            observers.setPrimaryObserverId(7L)
        }

        // when deleting all
        record.observers.clearAll()

        // then
        assertTrue(
            record.observers.getAllObserverIds()
                .isEmpty()
        )
    }

    @Test
    fun `should set and get all taxa`() {
        // given an observation record
        val record = ObservationRecord(internalId = 1234L)
        // and a taxon record to set
        val taxonRecord = TaxonRecord(
            recordId = 1234L,
            taxon = Taxon(
                8L,
                "taxon_01",
                Taxonomy(
                    "Animalia",
                    "Ascidies"
                )
            )
        )

        // when setting taxa to this observation record
        record.taxa.taxa = listOf(taxonRecord)

        // then
        assertArrayEquals(
            arrayOf(taxonRecord),
            record.taxa.taxa.toTypedArray()
        )
    }

    @Test
    fun `should avoid duplicates when setting all taxa`() {
        // given an observation record
        val record = ObservationRecord(internalId = 1234L)
        // and some taxon records to set
        val taxonRecord1 = TaxonRecord(
            recordId = 1234L,
            taxon = Taxon(
                8L,
                "taxon_01",
                Taxonomy(
                    "Animalia",
                    "Ascidies"
                )
            )
        )
        val taxonRecord2 = TaxonRecord(
            recordId = 1234L,
            taxon = Taxon(
                8L,
                "taxon_02",
                Taxonomy(
                    "Animalia",
                    "Ascidies"
                )
            )
        )

        // when setting taxa to this observation record
        record.taxa.taxa = listOf(
            taxonRecord1,
            taxonRecord2
        )

        // then
        assertArrayEquals(
            arrayOf(taxonRecord1),
            record.taxa.taxa.toTypedArray()
        )
    }

    @Test
    fun `should add new taxon record from taxon`() {
        // given an observation record
        val record = ObservationRecord(internalId = 1234L)
        // and a taxon to add
        val taxon = Taxon(
            8L,
            "taxon_02",
            Taxonomy(
                "Animalia",
                "Ascidies"
            )
        )

        // when adding a new taxon record
        val taxonRecordAdded = record.taxa.add(taxon)

        // then
        assertEquals(
            TaxonRecord(
                recordId = record.internalId,
                taxon = taxon
            ),
            taxonRecordAdded
        )
        assertArrayEquals(
            arrayOf(
                TaxonRecord(
                    recordId = record.internalId,
                    taxon = taxon
                )
            ),
            record.taxa.taxa.toTypedArray()
        )
        assertEquals(
            taxonRecordAdded,
            record.taxa.selectedTaxonRecord
        )
    }

    @Test
    fun `should replace an existing taxon record from taxon`() {
        // given an observation record with a taxon record
        val record = ObservationRecord(internalId = 1234L).apply {
            taxa.taxa = listOf(
                TaxonRecord(
                    recordId = internalId,
                    taxon = Taxon(
                        8L,
                        "taxon_02",
                        Taxonomy(
                            "Animalia",
                            "Ascidies"
                        )
                    )
                ).apply {
                    PropertyValue.Text(
                        "some_code",
                        "some_value"
                    )
                        .also {
                            properties[it.code] = it
                        }
                }
            )
        }

        // and a taxon to reassign
        val taxon = Taxon(
            8L,
            "taxon_02",
            Taxonomy(
                "Animalia",
                "Ascidies"
            )
        )

        // when adding a new taxon record
        val taxonRecordAdded = record.taxa.add(taxon)

        // then
        assertEquals(
            TaxonRecord(
                recordId = record.internalId,
                taxon = taxon
            ),
            taxonRecordAdded
        )
        assertArrayEquals(
            arrayOf(
                TaxonRecord(
                    recordId = record.internalId,
                    taxon = taxon
                )
            ),
            record.taxa.taxa.toTypedArray()
        )
        assertEquals(
            taxonRecordAdded,
            record.taxa.selectedTaxonRecord
        )
    }

    @Test
    fun `should add new taxon record`() {
        // given an observation record
        val record = ObservationRecord(internalId = 1234L)

        // when adding a taxon record
        val taxonRecordAdded = record.taxa.addOrUpdate(
            TaxonRecord(
                recordId = record.internalId,
                taxon = Taxon(
                    8L,
                    "taxon_02",
                    Taxonomy(
                        "Animalia",
                        "Ascidies"
                    )
                )
            )
        )

        // then
        assertEquals(
            TaxonRecord(
                recordId = record.internalId,
                taxon = Taxon(
                    8L,
                    "taxon_02",
                    Taxonomy(
                        "Animalia",
                        "Ascidies"
                    )
                )
            ),
            taxonRecordAdded
        )
        assertArrayEquals(
            arrayOf(
                TaxonRecord(
                    recordId = record.internalId,
                    taxon = Taxon(
                        8L,
                        "taxon_02",
                        Taxonomy(
                            "Animalia",
                            "Ascidies"
                        )
                    )
                )
            ),
            record.taxa.taxa.toTypedArray()
        )
        assertEquals(
            taxonRecordAdded,
            record.taxa.selectedTaxonRecord
        )
    }

    @Test
    fun `should add taxon record with existing ones`() {
        // given an observation record with a taxon record
        val record = ObservationRecord(internalId = 1234L).apply {
            taxa.taxa = listOf(
                TaxonRecord(
                    recordId = internalId,
                    taxon = Taxon(
                        8L,
                        "taxon_02",
                        Taxonomy(
                            "Animalia",
                            "Ascidies"
                        )
                    )
                ).apply {
                    PropertyValue.Text(
                        "some_code",
                        "some_value"
                    )
                        .also {
                            properties[it.code] = it
                        }
                }
            )
        }

        // when adding a new taxon record
        val taxonRecordAdded = record.taxa.addOrUpdate(
            TaxonRecord(
                recordId = 1L,
                taxon = Taxon(
                    7L,
                    "taxon_01",
                    Taxonomy(
                        "Animalia",
                        "Ascidies"
                    )
                )
            )
        )

        // then
        assertEquals(
            TaxonRecord(
                recordId = record.internalId,
                taxon = Taxon(
                    7L,
                    "taxon_01",
                    Taxonomy(
                        "Animalia",
                        "Ascidies"
                    )
                )
            ),
            taxonRecordAdded
        )
        assertArrayEquals(
            arrayOf(
                TaxonRecord(
                    recordId = record.internalId,
                    taxon = Taxon(
                        8L,
                        "taxon_02",
                        Taxonomy(
                            "Animalia",
                            "Ascidies"
                        )
                    )
                ).apply {
                    PropertyValue.Text(
                        "some_code",
                        "some_value"
                    )
                        .also {
                            properties[it.code] = it
                        }
                },
                TaxonRecord(
                    recordId = record.internalId,
                    taxon = Taxon(
                        7L,
                        "taxon_01",
                        Taxonomy(
                            "Animalia",
                            "Ascidies"
                        )
                    )
                )
            ),
            record.taxa.taxa.toTypedArray()
        )
        assertEquals(
            taxonRecordAdded,
            record.taxa.selectedTaxonRecord
        )
    }

    @Test
    fun `should update existing taxon record`() {
        // given an observation record with a taxon record
        val record = ObservationRecord(internalId = 1234L)
        val taxonRecord = TaxonRecord(
            recordId = record.internalId,
            taxon = Taxon(
                8L,
                "taxon_02",
                Taxonomy(
                    "Animalia",
                    "Ascidies"
                )
            )
        ).apply {
            PropertyValue.Text(
                "some_code",
                "some_value"
            )
                .also {
                    properties[it.code] = it
                }
        }
        record.taxa.taxa = listOf(taxonRecord)

        // when updating an existing taxon record
        val taxonRecordUpdated = record.taxa.addOrUpdate(
            taxonRecord.apply {
                PropertyValue.Number(
                    "some_another_code",
                    42
                )
                    .also {
                        properties[it.code] = it
                    }
            }
        )

        // then
        assertEquals(
            TaxonRecord(
                recordId = record.internalId,
                taxon = Taxon(
                    8L,
                    "taxon_02",
                    Taxonomy(
                        "Animalia",
                        "Ascidies"
                    )
                ),
                properties = listOf(
                    PropertyValue.Text(
                        "some_code",
                        "some_value"
                    ),
                    PropertyValue.Number(
                        "some_another_code",
                        42
                    )
                ).associate { it.toPair() }
                    .toSortedMap()
            ),
            taxonRecordUpdated
        )
        assertArrayEquals(
            arrayOf(
                TaxonRecord(
                    recordId = record.internalId,
                    taxon = Taxon(
                        8L,
                        "taxon_02",
                        Taxonomy(
                            "Animalia",
                            "Ascidies"
                        )
                    ),
                    properties = listOf(
                        PropertyValue.Text(
                            "some_code",
                            "some_value"
                        ),
                        PropertyValue.Number(
                            "some_another_code",
                            42
                        )
                    ).associate { it.toPair() }
                        .toSortedMap()
                )
            ),
            record.taxa.taxa.toTypedArray()
        )
        assertEquals(
            taxonRecordUpdated,
            record.taxa.selectedTaxonRecord
        )
    }

    @Test
    fun `should delete existing taxon record`() {
        // given an observation record with a taxon record
        val record = ObservationRecord(internalId = 1234L)
        val taxonRecord = TaxonRecord(
            recordId = record.internalId,
            taxon = Taxon(
                8L,
                "taxon_02",
                Taxonomy(
                    "Animalia",
                    "Ascidies"
                )
            )
        ).apply {
            PropertyValue.Text(
                "some_code",
                "some_value"
            )
                .also {
                    properties[it.code] = it
                }
        }
        record.taxa.taxa = listOf(taxonRecord)

        // when deleting an existing taonx record
        val taxonRecordDeleted = record.taxa.delete(8L)

        // then
        assertEquals(
            taxonRecord,
            taxonRecordDeleted
        )
        assertTrue(record.taxa.taxa.isEmpty())
        assertNull(record.taxa.selectedTaxonRecord)
    }

    @Test
    fun `should copy the same observation record`() {
        assertEquals(
            ObservationRecord(
                internalId = 1234L,
                status = ObservationRecord.Status.TO_SYNC
            ).apply {
                taxa.add(
                    Taxon(
                        8L,
                        "taxon_02",
                        Taxonomy(
                            "Animalia",
                            "Ascidies"
                        )
                    )
                )
            },
            ObservationRecord(internalId = 1234L).apply {
                taxa.add(
                    Taxon(
                        8L,
                        "taxon_02",
                        Taxonomy(
                            "Animalia",
                            "Ascidies"
                        )
                    )
                )
            }
                .copy(status = ObservationRecord.Status.TO_SYNC)
        )
    }

    @Test
    fun `should be the same`() {
        assertEquals(
            ObservationRecord(
                internalId = 1234L,
                geometry = gf.createPoint(
                    Coordinate(
                        -1.554476,
                        47.225782
                    )
                ),
                properties = sortedMapOf(
                    PropertyValue.Number(
                        "TYP_GRP",
                        133
                    )
                        .let {
                            it.code to it
                        }
                )
            ),
            ObservationRecord(
                internalId = 1234L,
                geometry = gf.createPoint(
                    Coordinate(
                        -1.554476,
                        47.225782
                    )
                ),
                properties = sortedMapOf(
                    PropertyValue.Number(
                        "TYP_GRP",
                        133
                    )
                        .let {
                            it.code to it
                        }
                )
            )
        )
    }

    @Test
    fun `should create observation record from Parcel`() {
        // given an observation record instance to write
        val record = ObservationRecord(
            internalId = 1234L,
            geometry = gf.createPoint(
                Coordinate(
                    -1.554476,
                    47.225782
                )
            ),
            properties = sortedMapOf(
                PropertyValue.Number(
                    "TYP_GRP",
                    133
                )
                    .let {
                        it.code to it
                    }
            )
        )

        // when we obtain a Parcel object to write this observation record to it
        val parcel = Parcel.obtain()
        record.writeToParcel(
            parcel,
            0
        )

        // reset the parcel for reading
        parcel.setDataPosition(0)

        // then
        assertEquals(
            record,
            parcelableCreator<ObservationRecord>().createFromParcel(parcel)
        )
    }
}