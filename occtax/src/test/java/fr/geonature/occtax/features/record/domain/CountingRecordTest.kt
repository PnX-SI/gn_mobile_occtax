package fr.geonature.occtax.features.record.domain

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [CountingRecord].
 *
 * @author S. Grimault
 */
@RunWith(RobolectricTestRunner::class)
class CountingRecordTest {

    @Test
    fun `should set the min value`() {
        val countingRecord = CountingRecord(index = 1).apply {
            min = 1
        }

        assertEquals(
            1,
            countingRecord.min
        )
        assertEquals(
            1,
            countingRecord.max
        )
    }

    @Test
    fun `should set a min value greater than max value`() {
        val countingRecord = CountingRecord(index = 1).apply {
            max = 1
        }
        countingRecord.min = 2

        assertEquals(
            2,
            countingRecord.min
        )
        assertEquals(
            2,
            countingRecord.max
        )
    }

    @Test
    fun `should set the max value`() {
        val countingRecord = CountingRecord(index = 1).apply {
            max = 1
        }

        assertEquals(
            1,
            countingRecord.min
        )
        assertEquals(
            1,
            countingRecord.max
        )
    }

    @Test
    fun `should set a max value lower than the min value`() {
        val countingRecord = CountingRecord(index = 1).apply {
            min = 2
        }
        countingRecord.max = 1

        assertEquals(
            1,
            countingRecord.min
        )
        assertEquals(
            1,
            countingRecord.max
        )
    }

    @Test
    fun `is empty`() {
        assertTrue(
            CountingRecord(index = 1).isEmpty()
        )
        assertTrue(CountingRecord(
            index = 1,
            properties = listOf(
                PropertyValue.Number(
                    "STADE_VIE",
                    null
                ),
            ).associateBy { it.code }
                .toSortedMap()
        ).isEmpty())
        assertFalse(CountingRecord(
            index = 1,
            properties = listOf(
                PropertyValue.Number(
                    "STADE_VIE",
                    2L
                ),
                PropertyValue.Number(
                    "SEXE",
                    168L
                )
            ).associateBy { it.code }
                .toSortedMap()
        ).isEmpty())
    }
}