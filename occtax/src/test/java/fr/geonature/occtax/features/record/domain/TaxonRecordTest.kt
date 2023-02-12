package fr.geonature.occtax.features.record.domain

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import fr.geonature.commons.data.entity.Taxon
import fr.geonature.commons.data.entity.Taxonomy
import fr.geonature.occtax.BuildConfig
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [TaxonRecord].
 *
 * @author S. Grimault
 */
@RunWith(RobolectricTestRunner::class)
class TaxonRecordTest {

    private lateinit var application: Application

    @Before
    fun setUp() {
        application = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun `should set and get all counting`() {
        // given some counting
        val counting = CountingRecord(
            index = 1,
            properties = listOf(
                PropertyValue.Number(
                    "STADE_VIE",
                    2L
                ),
                PropertyValue.Number(
                    "SEXE",
                    168L
                ),
                PropertyValue.Number(
                    "OBJ_DENBR",
                    146L
                ),
                PropertyValue.Number(
                    "TYP_DENBR",
                    93L
                )
            ).associateBy { it.code }
                .toSortedMap()
        ).apply {
            min = 1
            max = 2
        }

        // to add to this taxon record
        val taxonRecord = TaxonRecord(
            recordId = 1234L,
            taxon = Taxon(
                1234L,
                "taxon_01",
                Taxonomy(
                    "Animalia",
                    "Ascidies"
                ),
                null
            )
        )

        // when setting counting to this taxon record
        taxonRecord.counting.counting = listOf(counting)

        // then
        assertArrayEquals(
            arrayOf(counting),
            taxonRecord.counting.counting.toTypedArray()
        )
    }

    @Test
    fun `should create new counting`() {
        // given a taxon record with existing counting
        val existingCounting = CountingRecord(
            index = 1,
            properties = listOf(
                PropertyValue.Number(
                    "STADE_VIE",
                    2L
                ),
                PropertyValue.Number(
                    "SEXE",
                    168L
                ),
                PropertyValue.Number(
                    "OBJ_DENBR",
                    146L
                ),
                PropertyValue.Number(
                    "TYP_DENBR",
                    93L
                )
            ).associateBy { it.code }
                .toSortedMap()
        )
            .apply {
                min = 1
                max = 2
            }
        val taxonRecord = TaxonRecord(
            recordId = 1234L,
            taxon = Taxon(
                8L,
                "taxon_01",
                Taxonomy(
                    "Animalia",
                    "Ascidies"
                ),
                null
            )
        ).apply {
            counting.counting = listOf(existingCounting)
        }

        // when creating a new one
        val countingCreated = taxonRecord.counting.create()

        // then
        assertEquals(
            CountingRecord(index = 2),
            countingCreated
        )
    }

    @Test
    fun `should add counting to taxon record with no counting`() {
        // given a taxon record with no counting
        val taxonRecord = TaxonRecord(
            recordId = 1234L,
            taxon = Taxon(
                8L,
                "taxon_01",
                Taxonomy(
                    "Animalia",
                    "Ascidies"
                ),
                null
            )
        )

        // and some counting to add
        val counting = CountingRecord(
            properties = listOf(
                PropertyValue.Number(
                    "STADE_VIE",
                    2L
                ),
                PropertyValue.Number(
                    "SEXE",
                    168L
                ),
                PropertyValue.Number(
                    "OBJ_DENBR",
                    146L
                ),
                PropertyValue.Number(
                    "TYP_DENBR",
                    93L
                )
            ).associateBy { it.code }
                .toSortedMap()
        ).apply {
            min = 1
            max = 2
        }

        // when adding this counting to this taxon record
        val countingAdded = taxonRecord.counting.addOrUpdate(counting)

        // then
        assertEquals(
            counting.copy(index = 1),
            countingAdded
        )
        assertArrayEquals(
            arrayOf(counting.copy(index = 1)),
            taxonRecord.counting.counting.toTypedArray()
        )
    }

    @Test
    fun `should add counting to taxon record with existing ones`() {
        // given a taxon record with existing counting
        val existingCounting = CountingRecord(
            index = 1,
            properties = listOf(
                PropertyValue.Number(
                    "STADE_VIE",
                    2L
                ),
                PropertyValue.Number(
                    "SEXE",
                    168L
                ),
                PropertyValue.Number(
                    "OBJ_DENBR",
                    146L
                ),
                PropertyValue.Number(
                    "TYP_DENBR",
                    93L
                )
            ).associateBy { it.code }
                .toSortedMap()
        ).apply {
            min = 1
            max = 2
        }
        val taxonRecord = TaxonRecord(
            recordId = 1234L,
            taxon = Taxon(
                8L,
                "taxon_01",
                Taxonomy(
                    "Animalia",
                    "Ascidies"
                ),
                null
            )
        ).apply {
            counting.counting = listOf(existingCounting)
        }

        // and some counting to add
        val counting = CountingRecord(
            properties = listOf(
                PropertyValue.Number(
                    "STADE_VIE",
                    2L
                ),
                PropertyValue.Number(
                    "SEXE",
                    168L
                ),
                PropertyValue.Number(
                    "OBJ_DENBR",
                    146L
                ),
                PropertyValue.Number(
                    "TYP_DENBR",
                    93L
                )
            ).associateBy { it.code }
                .toSortedMap()
        ).apply {
            min = 1
            max = 3
        }

        // when adding this counting to this taxon record
        val countingAdded = taxonRecord.counting.addOrUpdate(counting)

        // then
        assertEquals(
            counting.copy(index = 2),
            countingAdded
        )
        assertArrayEquals(
            arrayOf(
                existingCounting,
                counting.copy(index = 2)
            ),
            taxonRecord.counting.counting.toTypedArray()
        )
    }

    @Test
    fun `should not add empty counting`() {
        // given a taxon record with existing counting
        val existingCounting = CountingRecord(
            index = 1,
            properties = listOf(
                PropertyValue.Number(
                    "STADE_VIE",
                    2L
                ),
                PropertyValue.Number(
                    "SEXE",
                    168L
                ),
                PropertyValue.Number(
                    "OBJ_DENBR",
                    146L
                ),
                PropertyValue.Number(
                    "TYP_DENBR",
                    93L
                ),
            ).associateBy { it.code }
                .toSortedMap()
        ).apply {
            min = 1
            max = 2
        }
        val taxonRecord = TaxonRecord(
            recordId = 1234L,
            taxon = Taxon(
                8L,
                "taxon_01",
                Taxonomy(
                    "Animalia",
                    "Ascidies"
                ),
                null
            )
        ).apply {
            counting.counting = listOf(existingCounting)
        }

        // when adding a newly created counting
        val countingCreated = taxonRecord.counting.create()
        val noSuchCountingAdded = taxonRecord.counting.addOrUpdate(countingCreated)

        // then
        assertNull(noSuchCountingAdded)
        assertArrayEquals(
            arrayOf(
                existingCounting
            ),
            taxonRecord.counting.counting.toTypedArray()
        )
    }

    @Test
    fun `should update existing counting`() {
        // given a taxon record with existing counting
        val existingCounting = CountingRecord(
            index = 1,
            properties = listOf(
                PropertyValue.Number(
                    "STADE_VIE",
                    2L
                ),
                PropertyValue.Number(
                    "SEXE",
                    168L
                ),
                PropertyValue.Number(
                    "OBJ_DENBR",
                    146L
                ),
                PropertyValue.Number(
                    "TYP_DENBR",
                    93L
                )
            ).associateBy { it.code }
                .toSortedMap()
        ).apply {
            min = 1
            max = 2
        }
        val taxonRecord = TaxonRecord(
            recordId = 1234L,
            taxon = Taxon(
                8L,
                "taxon_01",
                Taxonomy(
                    "Animalia",
                    "Ascidies"
                ),
                null
            )
        ).apply {
            counting.counting = listOf(existingCounting)
        }

        // when updating this counting
        val countingToUpdate = existingCounting.copy(properties = listOf(
            PropertyValue.Number(
                "STADE_VIE",
                2L
            ),
            PropertyValue.Number(
                "SEXE",
                168L
            ),
            PropertyValue.Number(
                "OBJ_DENBR",
                146L
            ),
            PropertyValue.Number(
                "TYP_DENBR",
                93L
            )
        ).associateBy { it.code }
            .toSortedMap()
        )
            .apply {
                min = 1
                max = 3
            }
        val countingUpdated = taxonRecord.counting.addOrUpdate(countingToUpdate)

        // then
        assertEquals(
            countingToUpdate,
            countingUpdated
        )
        assertArrayEquals(
            arrayOf(
                countingToUpdate
            ),
            taxonRecord.counting.counting.toTypedArray()
        )
    }

    @Test
    fun `should delete existing counting`() {
        // given a taxon record with existing counting
        val existingCounting = CountingRecord(
            index = 1,
            properties = listOf(
                PropertyValue.Number(
                    "STADE_VIE",
                    2L
                ),
                PropertyValue.Number(
                    "SEXE",
                    168L
                ),
                PropertyValue.Number(
                    "OBJ_DENBR",
                    146L
                ),
                PropertyValue.Number(
                    "TYP_DENBR",
                    93L
                )
            ).associateBy { it.code }
                .toSortedMap()
        ).apply {
            min = 1
            max = 2
        }
        val taxonRecord = TaxonRecord(
            recordId = 1234L,
            taxon = Taxon(
                8L,
                "taxon_01",
                Taxonomy(
                    "Animalia",
                    "Ascidies"
                ),
                null
            )
        ).apply {
            counting.counting = listOf(existingCounting)
        }

        // when deleting an existing counting
        val countingDeleted = taxonRecord.counting.delete(
            application,
            1
        )

        // then
        assertEquals(
            existingCounting,
            countingDeleted
        )
        assertArrayEquals(
            arrayOf(),
            taxonRecord.counting.counting.toTypedArray()
        )
    }

    @Test
    fun `should get the media base path from given counting`() {
        // given a counting record
        val countingRecord = CountingRecord(index = 1)

        // and a taxon record
        val taxonRecord = TaxonRecord(
            recordId = 1234L,
            taxon = Taxon(
                8L,
                "taxon_01",
                Taxonomy(
                    "Animalia",
                    "Ascidies"
                ),
                null
            )
        )

        assertEquals(
            "/Android/data/${BuildConfig.APPLICATION_ID}/inputs/${taxonRecord.recordId}/taxon/${taxonRecord.taxon.id}/counting/${countingRecord.index}",
            taxonRecord.counting.mediaBasePath(
                application,
                countingRecord
            ).absolutePath.substringAfterLast("external-files")
        )
    }

    @Test
    fun `should get the media base path from newly created counting`() {
        // given a taxon record
        val taxonRecord = TaxonRecord(
            recordId = 1234L,
            taxon = Taxon(
                8L,
                "taxon_01",
                Taxonomy(
                    "Animalia",
                    "Ascidies"
                ),
                null
            )
        )

        assertEquals(
            "/Android/data/${BuildConfig.APPLICATION_ID}/inputs/${taxonRecord.recordId}/taxon/${taxonRecord.taxon.id}/counting/1",
            taxonRecord.counting.mediaBasePath(
                application,
                taxonRecord.counting.create()
            ).absolutePath.substringAfterLast("external-files")
        )
    }
}