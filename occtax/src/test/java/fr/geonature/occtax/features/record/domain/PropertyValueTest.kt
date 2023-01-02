package fr.geonature.occtax.features.record.domain

import android.os.Parcel
import fr.geonature.commons.data.entity.Taxon
import fr.geonature.commons.data.entity.Taxonomy
import fr.geonature.commons.util.toDate
import kotlinx.parcelize.parcelableCreator
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.Date

/**
 * Unit tests about [PropertyValue].
 *
 * @author S. Grimault
 */
@RunWith(RobolectricTestRunner::class)
class PropertyValueTest {

    @Test
    fun `is empty`() {
        assertTrue(
            PropertyValue.Text(
                "some_code",
                ""
            )
                .isEmpty()
        )
        assertTrue(
            PropertyValue.Text(
                "some_code",
                null
            )
                .isEmpty()
        )
        assertFalse(
            PropertyValue.Text(
                "some_code",
                "some value"
            )
                .isEmpty()
        )

        assertTrue(
            PropertyValue.Number(
                "some_code",
                null
            )
                .isEmpty()
        )
        assertFalse(
            PropertyValue.Number(
                "some_code",
                8L
            )
                .isEmpty()
        )

        assertTrue(
            PropertyValue.NumberArray(
                "some_code",
                emptyArray()
            )
                .isEmpty()
        )

        assertTrue(
            PropertyValue.Taxa(
                "some_code",
                emptyArray()
            )
                .isEmpty()
        )
        assertTrue(
            PropertyValue.Taxa(
                "some_code",
                arrayOf(
                    TaxonRecord(
                        recordId = 1234L,
                        taxon = Taxon(
                            1234L,
                            "taxon_01",
                            Taxonomy(
                                "Animalia",
                                "Ascidies"
                            )
                        )
                    )
                )
            )
                .isEmpty()
        )

        assertTrue(
            PropertyValue.Counting(
                "some_code",
                emptyArray()
            )
                .isEmpty()
        )
        assertTrue(
            PropertyValue.Counting(
                "some_code",
                arrayOf(CountingRecord())
            )
                .isEmpty()
        )
    }

    @Test
    fun `should create a pair representation`() {
        assertEquals(
            "some_code" to
                PropertyValue.Text(
                    "some_code",
                    "some_value"
                ),
            PropertyValue.Text(
                "some_code",
                "some_value"
            )
                .toPair()
        )

        assertEquals(
            "some_code" to
                PropertyValue.Date(
                    "some_code",
                    toDate("2016-10-28T08:15:00Z")
                ),
            PropertyValue.Date(
                "some_code",
                toDate("2016-10-28T08:15:00Z")
            )
                .toPair()
        )

        assertEquals(
            "some_code" to
                PropertyValue.Number(
                    "some_code",
                    42
                ),
            PropertyValue.Number(
                "some_code",
                42
            )
                .toPair()
        )

        assertEquals(
            "some_code" to
                PropertyValue.NumberArray(
                    "some_code",
                    arrayOf(
                        42,
                        8
                    )
                ),
            PropertyValue.NumberArray(
                "some_code",
                arrayOf(
                    42,
                    8
                )
            )
                .toPair()
        )

        assertEquals(
            "some_code" to
                PropertyValue.Nomenclature(
                    "some_code",
                    "some_label",
                    8L
                ),
            PropertyValue.Nomenclature(
                "some_code",
                "some_label",
                8L
            )
                .toPair()
        )
    }

    @Test
    fun `should create property text value from Parcel`() {
        // given a property text value instance to write
        val pv = PropertyValue.Text(
            "some_code",
            "some_value"
        )

        // when we obtain a Parcel object to write this property value to it
        val parcel = Parcel.obtain()
        pv.writeToParcel(
            parcel,
            0
        )

        // reset the parcel for reading
        parcel.setDataPosition(0)

        // then
        assertEquals(
            pv,
            parcelableCreator<PropertyValue.Text>().createFromParcel(parcel)
        )
    }

    @Test
    fun `should create property date value from Parcel`() {
        // given a property date value instance to write
        val pv = PropertyValue.Date(
            "some_code",
            Date()
        )

        // when we obtain a Parcel object to write this property value to it
        val parcel = Parcel.obtain()
        pv.writeToParcel(
            parcel,
            0
        )

        // reset the parcel for reading
        parcel.setDataPosition(0)

        // then
        assertEquals(
            pv,
            parcelableCreator<PropertyValue.Date>().createFromParcel(parcel)
        )
    }

    @Test
    fun `should create property int value from Parcel`() {
        // given a property int value instance to write
        val pv = PropertyValue.Number(
            "some_code",
            8
        )

        // when we obtain a Parcel object to write this property value to it
        val parcel = Parcel.obtain()
        pv.writeToParcel(
            parcel,
            0
        )

        // reset the parcel for reading
        parcel.setDataPosition(0)

        // then
        assertEquals(
            pv,
            parcelableCreator<PropertyValue.Number>().createFromParcel(parcel)
        )
    }

    @Test
    fun `should create property long value from Parcel`() {
        // given a property long value instance to write
        val pv = PropertyValue.Number(
            "some_code",
            8L
        )

        // when we obtain a Parcel object to write this property value to it
        val parcel = Parcel.obtain()
        pv.writeToParcel(
            parcel,
            0
        )

        // reset the parcel for reading
        parcel.setDataPosition(0)

        // then
        assertEquals(
            pv,
            parcelableCreator<PropertyValue.Number>().createFromParcel(parcel)
        )
    }

    @Test
    fun `should create property number values from Parcel`() {
        // given a property number values instance to write
        val pv = PropertyValue.NumberArray(
            "some_code",
            arrayOf(
                8L,
                12L
            )
        )

        // when we obtain a Parcel object to write this property value to it
        val parcel = Parcel.obtain()
        pv.writeToParcel(
            parcel,
            0
        )

        // reset the parcel for reading
        parcel.setDataPosition(0)

        // then
        assertEquals(
            pv,
            parcelableCreator<PropertyValue.NumberArray>().createFromParcel(parcel)
        )
    }

    @Test
    fun `should create property nomenclature value from Parcel`() {
        // given a property nomenclature value instance to write
        val pv = PropertyValue.Nomenclature(
            "some_code",
            "some_label",
            8L
        )

        // when we obtain a Parcel object to write this property value to it
        val parcel = Parcel.obtain()
        pv.writeToParcel(
            parcel,
            0
        )

        // reset the parcel for reading
        parcel.setDataPosition(0)

        // then
        assertEquals(
            pv,
            parcelableCreator<PropertyValue.Nomenclature>().createFromParcel(parcel)
        )
    }

    @Test
    fun `should create property taxa values from Parcel`() {
        // given a property taxa values instance to write
        val pv = PropertyValue.Taxa(
            "some_code",
            arrayOf(
                TaxonRecord(
                    recordId = 1234L,
                    taxon = Taxon(
                        1234L,
                        "taxon_01",
                        Taxonomy(
                            "Animalia",
                            "Ascidies"
                        )
                    )
                )
            )
        )

        // when we obtain a Parcel object to write this property value to it
        val parcel = Parcel.obtain()
        pv.writeToParcel(
            parcel,
            0
        )

        // reset the parcel for reading
        parcel.setDataPosition(0)

        // then
        assertEquals(
            pv,
            parcelableCreator<PropertyValue.Taxa>().createFromParcel(parcel)
        )
    }

    @Test
    fun `should create property counting values from Parcel`() {
        // given a property counting values instance to write
        val pv = PropertyValue.Counting(
            "some_code",
            arrayOf(CountingRecord(index = 1))
        )

        // when we obtain a Parcel object to write this property value to it
        val parcel = Parcel.obtain()
        pv.writeToParcel(
            parcel,
            0
        )

        // reset the parcel for reading
        parcel.setDataPosition(0)

        // then
        assertEquals(
            pv,
            parcelableCreator<PropertyValue.Counting>().createFromParcel(parcel)
        )
    }
}