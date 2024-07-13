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

    @Test
    fun `should have additional fields with no values`() {
        // given a counting record
        val countingRecord = CountingRecord(index = 1).apply {
            properties["STADE_VIE"] = PropertyValue.Nomenclature(
                code = "STADE_VIE",
                label = "Inconnu",
                value = 1
            )
        }

        assertEquals(
            PropertyValue.AdditionalFields(
                CountingRecord.ADDITIONAL_FIELDS_KEY,
                mapOf()
            ),
            countingRecord.properties[CountingRecord.ADDITIONAL_FIELDS_KEY]
        )
    }

    @Test
    fun `should get all additional fields values`() {
        // given a counting record
        val countingRecord = CountingRecord(
            index = 1,
            properties = sortedMapOf(
                "STADE_VIE" to
                    PropertyValue.Nomenclature(
                        code = "STADE_VIE",
                        label = "Inconnu",
                        value = 1
                    ),
                CountingRecord.ADDITIONAL_FIELDS_KEY to PropertyValue.AdditionalFields(
                    CountingRecord.ADDITIONAL_FIELDS_KEY,
                    mapOf(
                        "some_key" to PropertyValue.Text(
                            "some_key",
                            "some_value"
                        )
                    )
                )
            )
        )

        assertArrayEquals(
            arrayOf(
                PropertyValue.Text(
                    "some_key",
                    "some_value"
                )
            ),
            countingRecord.additionalFields.toTypedArray()
        )
    }

    @Test
    fun `should set additional fields values`() {
        // given a counting record with some additional fields
        val countingRecord = CountingRecord(
            index = 1,
            properties = sortedMapOf(
                "STADE_VIE" to
                    PropertyValue.Nomenclature(
                        code = "STADE_VIE",
                        label = "Inconnu",
                        value = 1
                    ),
                CountingRecord.ADDITIONAL_FIELDS_KEY to PropertyValue.AdditionalFields(
                    CountingRecord.ADDITIONAL_FIELDS_KEY,
                    mapOf(
                        "some_key" to PropertyValue.Text(
                            "some_key",
                            "some_value"
                        )
                    )
                )
            )
        )

        // when updating additional fields
        countingRecord.additionalFields = listOf(
            PropertyValue.Text(
                "another_key",
                "another_value"
            )
        )

        // then
        assertArrayEquals(
            arrayOf(
                PropertyValue.Text(
                    "another_key",
                    "another_value"
                )
            ),
            countingRecord.additionalFields.toTypedArray()
        )
    }
}