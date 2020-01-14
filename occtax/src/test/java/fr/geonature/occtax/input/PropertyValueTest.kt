package fr.geonature.occtax.input

import android.os.Parcel
import fr.geonature.commons.data.Nomenclature
import fr.geonature.occtax.input.PropertyValue.Companion.fromNomenclature
import fr.geonature.occtax.input.PropertyValue.Companion.fromValue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [PropertyValue].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@RunWith(RobolectricTestRunner::class)
class PropertyValueTest {

    @Test
    fun testCreateFromNomenclature() {
        assertEquals(
            PropertyValue(
                "ETA_BIO",
                "label",
                1234L
            ),
            fromNomenclature(
                "ETA_BIO",
                Nomenclature(
                    1234L,
                    "2",
                    "1234:001",
                    "label",
                    123L
                )
            )
        )
    }

    @Test
    fun testCreateFromValue() {
        assertEquals(
            PropertyValue(
                "DETERMINER",
                null,
                "some_value"
            ),
            fromValue(
                "DETERMINER",
                "some_value"
            )
        )
        assertEquals(
            PropertyValue(
                "MIN",
                null,
                2
            ),
            fromValue(
                "MIN",
                2
            )
        )
    }

    @Test
    fun testIsEmpty() {
        assertFalse(
            fromNomenclature(
                "ETA_BIO",
                Nomenclature(
                    1234L,
                    "2",
                    "1234:001",
                    "label",
                    123L
                )
            ).isEmpty()
        )
        assertFalse(
            fromValue(
                "DETERMINER",
                "some_value"
            ).isEmpty()
        )
        assertFalse(
            fromValue(
                "MIN",
                2
            ).isEmpty()
        )
        assertTrue(
            PropertyValue(
                "ETA_BIO",
                null,
                null
            ).isEmpty()
        )
    }

    @Test
    fun testParcelableFromNomenclature() {
        // given a property value instance
        val propertyValue = fromNomenclature(
            "ETA_BIO",
            Nomenclature(
                1234L,
                "2",
                "1234:001",
                "label",
                123L
            )
        )

        // when we obtain a Parcel object to write the property value instance to it
        val parcel = Parcel.obtain()
        propertyValue.writeToParcel(
            parcel,
            0
        )

        // reset the parcel for reading
        parcel.setDataPosition(0)

        // then
        assertEquals(
            propertyValue,
            PropertyValue.CREATOR.createFromParcel(parcel)
        )
    }

    @Test
    fun testParcelableFromStringValue() {
        // given a property value instance
        val propertyValue = fromValue(
            "DETERMINER",
            "some_value"
        )

        // when we obtain a Parcel object to write the property value instance to it
        val parcel = Parcel.obtain()
        propertyValue.writeToParcel(
            parcel,
            0
        )

        // reset the parcel for reading
        parcel.setDataPosition(0)

        // then
        assertEquals(
            propertyValue,
            PropertyValue.CREATOR.createFromParcel(parcel)
        )
    }

    @Test
    fun testParcelableFromIntValue() {
        // given a property value instance
        val propertyValue = fromValue(
            "MIN",
            2
        )

        // when we obtain a Parcel object to write the property value instance to it
        val parcel = Parcel.obtain()
        propertyValue.writeToParcel(
            parcel,
            0
        )

        // reset the parcel for reading
        parcel.setDataPosition(0)

        // then
        assertEquals(
            propertyValue,
            PropertyValue.CREATOR.createFromParcel(parcel)
        )
    }
}
