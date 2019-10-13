package fr.geonature.occtax.input

import android.os.Parcel
import fr.geonature.commons.data.Nomenclature
import fr.geonature.occtax.input.SelectedProperty.Companion.fromNomenclature
import fr.geonature.occtax.input.SelectedProperty.Companion.fromValue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [SelectedProperty].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@RunWith(RobolectricTestRunner::class)
class SelectedPropertyTest {

    @Test
    fun testCreateFromNomenclature() {
        assertEquals(SelectedProperty(SelectedProperty.PropertyType.NOMENCLATURE,
                                      "ETA_BIO",
                                      1234L,
                                      "label"),
                     fromNomenclature("ETA_BIO",
                                      Nomenclature(1234L,
                                                   "2",
                                                   "1234:001",
                                                   "label",
                                                   123L)))
    }

    @Test
    fun testCreateFromValue() {
        assertEquals(SelectedProperty(SelectedProperty.PropertyType.TEXT,
                                      "DETERMINER",
                                      null,
                                      "label"),
                     fromValue("DETERMINER",
                               "label"))
    }

    @Test
    fun testIsEmpty() {
        assertFalse(fromNomenclature("ETA_BIO",
                                     Nomenclature(1234L,
                                                  "2",
                                                  "1234:001",
                                                  "label",
                                                  123L)).isEmpty())
        assertFalse(fromValue("DETERMINER",
                              "label").isEmpty())
        assertTrue(SelectedProperty(SelectedProperty.PropertyType.NOMENCLATURE,
                                    "ETA_BIO",
                                    null,
                                    null).isEmpty())
    }

    @Test
    fun testPropertyTypeFromValue() {
        assertEquals(SelectedProperty.PropertyType.NOMENCLATURE,
                     SelectedProperty.PropertyType.fromString("nomenclature"))
        assertEquals(SelectedProperty.PropertyType.NOMENCLATURE,
                     SelectedProperty.PropertyType.fromString("NOMENCLATURE"))
        assertEquals(SelectedProperty.PropertyType.TEXT,
                     SelectedProperty.PropertyType.fromString("text"))
        assertEquals(SelectedProperty.PropertyType.TEXT,
                     SelectedProperty.PropertyType.fromString("TEXT"))
        assertNull(SelectedProperty.PropertyType.fromString(null))
        assertNull(SelectedProperty.PropertyType.fromString(""))
        assertNull(SelectedProperty.PropertyType.fromString("no_such_value"))
    }

    @Test
    fun testParcelableFromNomenclature() {
        // given a selected property instance
        val selectedProperty = fromNomenclature("ETA_BIO",
                                                Nomenclature(1234L,
                                                             "2",
                                                             "1234:001",
                                                             "label",
                                                             123L))

        // when we obtain a Parcel object to write the selected property instance to it
        val parcel = Parcel.obtain()
        selectedProperty.writeToParcel(parcel,
                                       0)

        // reset the parcel for reading
        parcel.setDataPosition(0)

        // then
        assertEquals(selectedProperty,
                     SelectedProperty.CREATOR.createFromParcel(parcel))
    }

    @Test
    fun testParcelableFromText() {
        // given a selected property instance
        val selectedProperty = fromValue("DETERMINER",
                                         "label")

        // when we obtain a Parcel object to write the selected property instance to it
        val parcel = Parcel.obtain()
        selectedProperty.writeToParcel(parcel,
                                       0)

        // reset the parcel for reading
        parcel.setDataPosition(0)

        // then
        assertEquals(selectedProperty,
                     SelectedProperty.CREATOR.createFromParcel(parcel))
    }
}