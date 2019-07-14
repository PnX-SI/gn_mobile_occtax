package fr.geonature.occtax.input.io

import fr.geonature.commons.input.AbstractInputTaxon
import fr.geonature.commons.input.io.InputJsonReader
import fr.geonature.commons.util.IsoDateUtils.toDate
import fr.geonature.occtax.FixtureHelper.getFixture
import fr.geonature.occtax.input.Input
import fr.geonature.occtax.input.InputTaxon
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [InputJsonReader].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@RunWith(RobolectricTestRunner::class)
class InputJsonReaderTest {

    private lateinit var inputJsonReader: InputJsonReader<Input>

    @Before
    fun setUp() {
        inputJsonReader = InputJsonReader(OnInputJsonReaderListenerImpl())
    }

    @Test
    fun testReadInputFromInvalidJsonString() {
        // when read an invalid JSON as Input
        val input = inputJsonReader.read("")

        // then
        assertNull(input)
    }

    @Test
    fun testReadEmptyInput() {
        // given an input file to read
        val json = getFixture("input_empty.json")

        // when parsing this file as Input
        val input = inputJsonReader.read(json)

        assertNotNull(input)
        assertEquals(1234L,
                     input?.id)
        assertEquals(Input().module,
                     input?.module)
    }

    @Test
    fun testReadInput() {
        // given an input file to read
        val json = getFixture("input_simple.json")

        // when parsing this file as Input
        val input = inputJsonReader.read(json)

        // then
        assertNotNull(input)
        assertEquals(1234L,
                     input!!.id)
        assertEquals(Input().module,
                     input.module)
        assertEquals(toDate("2016-10-28"),
                     input.date)
        assertEquals(1L,
                     input.getPrimaryObserverId())
        assertArrayEquals(longArrayOf(1,
                                      5,
                                      2,
                                      3),
                          input.getAllInputObserverIds().toLongArray())
        assertArrayEquals(longArrayOf(5,
                                      2,
                                      3),
                          input.getInputObserverIds()
                              .toLongArray())
        assertEquals(listOf(InputTaxon().apply { id = 10 }),
                     input.getInputTaxa())
    }

    @Test
    fun testReadInputWithNoObserverAndNoTaxon() {
        // given an input file to read
        val json = getFixture("input_no_observer_no_taxon.json")

        // when parsing this file as Input
        val input = inputJsonReader.read(json)

        // then
        assertNotNull(input)
        assertEquals(1234L,
                     input!!.id)
        assertEquals(Input().module,
                     input.module)
        assertEquals(toDate("2016-10-28"),
                     input.date)
        assertNull(input.getPrimaryObserverId())
        assertArrayEquals(longArrayOf(),
                          input.getAllInputObserverIds().toLongArray())
        assertArrayEquals(longArrayOf(),
                          input.getInputObserverIds()
                              .toLongArray())
        assertNull(input.geometry)
        assertEquals(listOf<AbstractInputTaxon>(),
                     input.getInputTaxa())
    }
}