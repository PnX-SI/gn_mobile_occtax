package fr.geonature.occtax.features.record.io

import fr.geonature.commons.data.entity.Taxon
import fr.geonature.commons.data.entity.Taxonomy
import fr.geonature.commons.util.toDate
import fr.geonature.occtax.FixtureHelper.getFixture
import fr.geonature.occtax.features.record.domain.PropertyValue
import fr.geonature.occtax.features.record.domain.TaxonRecord
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [TaxonRecordAPIJsonWriter].
 *
 * @author S. Grimault
 */
@RunWith(RobolectricTestRunner::class)
class TaxonRecordAPIJsonWriterTest {

    @Test
    fun `should write an observation record`() {
        // when writing a taxon record
        val json = TaxonRecordAPIJsonWriter().setIndent("  ")
            .write(
                TaxonRecord(
                    recordId = 1234L,
                    taxon = Taxon(
                        10L,
                        "taxon_01",
                        Taxonomy(
                            "Animalia",
                            "Ascidies"
                        )
                    )
                ).apply {
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
                })

        // then
        assertEquals(
            getFixture("taxon_record_api_complete.json"),
            json
        )
    }
}