package fr.geonature.occtax.input.io

import fr.geonature.commons.input.io.InputJsonWriter
import fr.geonature.commons.util.IsoDateUtils.toDate
import fr.geonature.occtax.FixtureHelper.getFixture
import fr.geonature.occtax.input.Input
import fr.geonature.occtax.input.InputTaxon
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

    private lateinit var inputJsonWriter: InputJsonWriter

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
            addInputTaxon(InputTaxon().apply { id = 10L })
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