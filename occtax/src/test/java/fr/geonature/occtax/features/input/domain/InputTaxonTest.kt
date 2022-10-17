package fr.geonature.occtax.features.input.domain

import android.os.Parcel
import fr.geonature.commons.data.entity.Nomenclature
import fr.geonature.commons.data.entity.Taxon
import fr.geonature.commons.data.entity.Taxonomy
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [InputTaxon].
 *
 * @author S. Grimault
 */
@RunWith(RobolectricTestRunner::class)
class InputTaxonTest {

    @Test
    fun `should add counting metadata`() {
        // given an input taxon
        val inputTaxon = InputTaxon(
            Taxon(
                1234L,
                "taxon_01",
                Taxonomy(
                    "Animalia",
                    "Ascidies"
                ),
                null
            )
        )

        // when adding an empty counting metadata
        inputTaxon.addCountingMetadata(CountingMetadata())

        // then
        assertTrue(inputTaxon.getCounting().isEmpty())

        // when adding valid counting metadata
        with(inputTaxon) {
            addCountingMetadata(CountingMetadata().apply {
                properties["STADE_VIE"] = PropertyValue(
                    "STADE_VIE",
                    null,
                    2L
                )
            })
            addCountingMetadata(CountingMetadata().apply {
                properties["SEXE"] = PropertyValue(
                    "SEXE",
                    null,
                    168L
                )
            })
        }

        assertArrayEquals(
            arrayOf(
                CountingMetadata(1).apply {
                    properties["STADE_VIE"] = PropertyValue(
                        "STADE_VIE",
                        null,
                        2L
                    )
                },
                CountingMetadata(2).apply {
                    properties["SEXE"] = PropertyValue(
                        "SEXE",
                        null,
                        168L
                    )
                }),
            inputTaxon.getCounting().toTypedArray()
        )
    }

    @Test
    fun `should delete counting metadata`() {
        // given an input taxon with counting metadata
        val inputTaxon = InputTaxon(
            Taxon(
                1234L,
                "taxon_01",
                Taxonomy(
                    "Animalia",
                    "Ascidies"
                ),
                null
            )
        ).apply {
            addCountingMetadata(CountingMetadata().apply {
                properties["STADE_VIE"] = PropertyValue(
                    "STADE_VIE",
                    null,
                    2L
                )
            })
        }

        // when deleting counting metadata
        assertNotNull(inputTaxon.deleteCountingMetadata(1))

        // when deleting non existing counting metadata
        assertNull(inputTaxon.deleteCountingMetadata(1))
    }

    @Test
    fun `should create input taxon from Parcel`() {
        // given an input taxon
        val inputTaxon = InputTaxon(
            Taxon(
                1234L,
                "taxon_01",
                Taxonomy(
                    "Animalia",
                    "Ascidies"
                ),
                null
            )
        ).apply {
            properties["ETA_BIO"] = PropertyValue.fromNomenclature(
                "ETA_BIO",
                Nomenclature(
                    1234L,
                    "2",
                    "1234:001",
                    "label",
                    123L
                )
            )
            addCountingMetadata(CountingMetadata().apply {
                properties.putAll(
                    listOf(
                        PropertyValue.fromNomenclature(
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

        // when we obtain a Parcel object to write the input taxon instance to it
        val parcel = Parcel.obtain()
        inputTaxon.writeToParcel(
            parcel,
            0
        )

        // reset the parcel for reading
        parcel.setDataPosition(0)

        // then
        val inputTaxonFromParcel = InputTaxon.CREATOR.createFromParcel(parcel)
        assertEquals(
            inputTaxon,
            inputTaxonFromParcel
        )
    }
}
