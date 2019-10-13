package fr.geonature.occtax.input.io

import fr.geonature.commons.data.Taxon
import fr.geonature.commons.data.Taxonomy
import fr.geonature.commons.input.io.InputJsonWriter
import fr.geonature.commons.util.IsoDateUtils.toDate
import fr.geonature.occtax.FixtureHelper.getFixture
import fr.geonature.occtax.input.Input
import fr.geonature.occtax.input.InputTaxon
import fr.geonature.occtax.input.SelectedProperty
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.Date

/**
 * Unit tests about [InputJsonWriter].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@RunWith(RobolectricTestRunner::class)
class InputJsonWriterTest {

    private lateinit var inputJsonWriter: InputJsonWriter<Input>

    @Before
    fun setUp() {
        inputJsonWriter = InputJsonWriter(OnInputJsonWriterListenerImpl())
    }

    @Test
    fun testWriteInput() {
        // given an Input instance to write
        val input = Input().apply {
            id = 1234
            date = toDate("2016-10-28") ?: Date()
            setPrimaryInputObserverId(1L)
            addInputObserverId(5L)
            addInputObserverId(2L)
            addInputObserverId(3L)
            addInputTaxon(InputTaxon(Taxon(10L,
                                           "taxon_01",
                                           Taxonomy("Animalia",
                                                    "Ascidies"))).apply {
                this.properties["METH_OBS"] = SelectedProperty(SelectedProperty.PropertyType.NOMENCLATURE,
                                                               "METH_OBS",
                                                               41,
                                                               null)
                this.properties["ETA_BIO"] = SelectedProperty(SelectedProperty.PropertyType.NOMENCLATURE,
                                                              "ETA_BIO",
                                                              29,
                                                              null)
                this.properties["METH_DETERMIN"] = SelectedProperty(SelectedProperty.PropertyType.NOMENCLATURE,
                                                                    "METH_DETERMIN",
                                                                    445,
                                                                    null)
                this.properties["DETERMINER"] = SelectedProperty(SelectedProperty.PropertyType.TEXT,
                                                                 "DETERMINER",
                                                                 null,
                                                                 "Determiner value")
                this.properties["STATUT_BIO"] = SelectedProperty(SelectedProperty.PropertyType.NOMENCLATURE,
                                                                 "STATUT_BIO",
                                                                 29,
                                                                 null)
                this.properties["NATURALITE"] = SelectedProperty(SelectedProperty.PropertyType.NOMENCLATURE,
                                                                 "NATURALITE",
                                                                 160,
                                                                 null)
                this.properties["PREUVE_EXIST"] = SelectedProperty(SelectedProperty.PropertyType.NOMENCLATURE,
                                                                   "PREUVE_EXIST",
                                                                   81,
                                                                   null)
                this.properties["COMMENT"] = SelectedProperty(SelectedProperty.PropertyType.TEXT,
                                                              "COMMENT",
                                                              null,
                                                              "Some comment")
            })
        }

        // when write this Input as JSON string
        val json = inputJsonWriter.setIndent("  ")
            .write(input)

        // then
        assertNotNull(json)
        assertEquals(getFixture("input_simple.json"),
                     json)
    }
}