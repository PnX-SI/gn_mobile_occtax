package fr.geonature.occtax.input

import android.os.Parcel
import fr.geonature.commons.data.entity.Nomenclature
import fr.geonature.occtax.input.PropertyValue.Companion.fromNomenclature
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [CountingMetadata].
 *
 * @author S. Grimault
 */
@RunWith(RobolectricTestRunner::class)
class CountingMetadataTest {

    @Test
    fun testIsEmpty() {
        assertFalse(CountingMetadata().apply {
            properties.putAll(
                mutableMapOf(
                    Pair(
                        "SEXE",
                        fromNomenclature(
                            "SEXE",
                            Nomenclature(
                                168L,
                                "Femelle",
                                "009.002",
                                "Femelle",
                                9L
                            )
                        )
                    )
                )
            )
            min = 1
            max = 2
        }.isEmpty())

        assertFalse(CountingMetadata().apply {
            properties.putAll(
                mutableMapOf(
                    Pair(
                        "SEXE",
                        fromNomenclature(
                            "SEXE",
                            Nomenclature(
                                168L,
                                "Femelle",
                                "009.002",
                                "Femelle",
                                9L
                            )
                        )
                    )
                )
            )
        }.isEmpty())

        assertTrue(CountingMetadata().isEmpty())
        assertTrue(CountingMetadata().apply {
            properties["MIN"] = PropertyValue(
                "MIN",
                null,
                null
            )
        }.isEmpty())
    }

    @Test
    fun testParcelable() {
        // given counting metadata
        val countingMetadata = CountingMetadata().apply {
            properties.putAll(
                mutableMapOf(
                    Pair(
                        "SEXE",
                        fromNomenclature(
                            "SEXE",
                            Nomenclature(
                                168L,
                                "Femelle",
                                "009.002",
                                "Femelle",
                                9L
                            )
                        )
                    )
                )
            )
            min = 1
            max = 2
        }

        // when we obtain a Parcel object to write the selected counting metadata instance to it
        val parcel = Parcel.obtain()
        countingMetadata.writeToParcel(
            parcel,
            0
        )

        // reset the parcel for reading
        parcel.setDataPosition(0)

        // then
        assertEquals(
            countingMetadata,
            CountingMetadata.CREATOR.createFromParcel(parcel)
        )
    }
}
