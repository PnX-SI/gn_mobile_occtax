package fr.geonature.occtax.features.input.domain

import android.os.Parcel
import fr.geonature.commons.data.entity.Nomenclature
import fr.geonature.occtax.features.input.domain.PropertyValue.Companion.fromNomenclature
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
    fun `should be the same`() {
        assertEquals(
            CountingMetadata().apply {
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
            },
            CountingMetadata().apply {
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
    }

    @Test
    fun `is empty`() {
        assertTrue(CountingMetadata().isEmpty())

        assertTrue(CountingMetadata().apply {
            properties.putAll(
                listOf(
                    fromNomenclature(
                        "SEXE",
                        null
                    ),
                    PropertyValue(
                        "MIN",
                        null,
                        null
                    ),
                    PropertyValue(
                        "MAX",
                        null,
                        null
                    )
                ).associateBy { it.code }
            )
        }.isEmpty())

        assertFalse(CountingMetadata().apply {
            properties.putAll(
                listOf(
                    fromNomenclature(
                        "SEXE",
                        Nomenclature(
                            168L,
                            "Femelle",
                            "009.002",
                            "Femelle",
                            9L
                        )
                    ),
                    PropertyValue(
                        "MIN",
                        null,
                        null
                    ),
                    PropertyValue(
                        "MAX",
                        null,
                        null
                    )
                ).associateBy { it.code }
            )
        }.isEmpty())
    }

    @Test
    fun `should copy counting metadata`() {
        assertEquals(
            CountingMetadata().apply {
                properties.putAll(
                    listOf(
                        fromNomenclature(
                            "SEXE",
                            Nomenclature(
                                168L,
                                "Femelle",
                                "009.002",
                                "Femelle",
                                9L
                            )
                        ),
                        PropertyValue(
                            "MIN",
                            null,
                            1
                        ),
                        PropertyValue(
                            "MAX",
                            null,
                            2
                        )
                    ).associateBy { it.code }
                )
            }.copy(index = 1),
            CountingMetadata(1).apply {
                properties.putAll(
                    listOf(
                        fromNomenclature(
                            "SEXE",
                            Nomenclature(
                                168L,
                                "Femelle",
                                "009.002",
                                "Femelle",
                                9L
                            )
                        ),
                        PropertyValue(
                            "MIN",
                            null,
                            1
                        ),
                        PropertyValue(
                            "MAX",
                            null,
                            2
                        )
                    ).associateBy { it.code }
                )
            })
    }

    @Test
    fun `should update counting metadata`() {
        assertEquals(
            CountingMetadata(
                properties = listOf(
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
                ).associateBy { it.code }.toSortedMap()
            ).apply {
                properties["MIN"] = PropertyValue(
                    "MIN",
                    null,
                    1
                )
                properties["MAX"] = PropertyValue(
                    "MAX",
                    null,
                    2
                )
            },
            CountingMetadata(
                properties = listOf(
                    fromNomenclature(
                        "SEXE",
                        Nomenclature(
                            168L,
                            "Femelle",
                            "009.002",
                            "Femelle",
                            9L
                        )
                    ),
                    PropertyValue(
                        "MIN",
                        null,
                        1
                    ),
                    PropertyValue(
                        "MAX",
                        null,
                        2
                    )
                ).associateBy { it.code }.toSortedMap()
            )
        )
    }

    @Test
    fun `should create counting metadata from Parcel`() {
        // given counting metadata
        val countingMetadata = CountingMetadata().apply {
            properties.putAll(
                listOf(
                    fromNomenclature(
                        "SEXE",
                        Nomenclature(
                            168L,
                            "Femelle",
                            "009.002",
                            "Femelle",
                            9L
                        )
                    ),
                    PropertyValue(
                        "MIN",
                        null,
                        1
                    ),
                    PropertyValue(
                        "MAX",
                        null,
                        2
                    )
                ).associateBy { it.code }
            )
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
