package fr.geonature.occtax.features.nomenclature.domain

import android.app.Application
import android.os.Bundle
import android.os.Parcel
import androidx.test.core.app.ApplicationProvider
import fr.geonature.commons.util.toDate
import fr.geonature.compat.os.getParcelableArrayCompat
import fr.geonature.occtax.R
import fr.geonature.occtax.features.record.domain.MediaRecord
import fr.geonature.occtax.features.record.domain.PropertyValue
import kotlinx.parcelize.parcelableCreator
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [FormField].
 *
 * @author S. Grimault
 */
@RunWith(RobolectricTestRunner::class)
class FormFieldTest {

    private lateinit var application: Application

    @Before
    fun setUp() {
        application = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun `should compare two form fields`() {
        assertTrue(
            FormField.Button(
                type = FormField.Type.INFORMATION,
                label = "some label"
            ) ==
                FormField.Button(
                    type = FormField.Type.INFORMATION,
                    label = "some label"
                )
        )
        assertTrue(
            FormField.Button(
                type = FormField.Type.INFORMATION,
                label = "some label",
                order = 1
            ) <
                FormField.Button(
                    type = FormField.Type.INFORMATION,
                    label = "some label"
                )
        )
        assertTrue(
            FormField.Button(
                type = FormField.Type.INFORMATION,
                label = "some label"
            ) >
                FormField.Button(
                    type = FormField.Type.INFORMATION,
                    label = "some label",
                    order = 1
                )
        )
        assertTrue(
            FormField.Button(
                type = FormField.Type.INFORMATION,
                label = "some label",
                order = 1
            ) <
                FormField.Button(
                    type = FormField.Type.INFORMATION,
                    label = "some label",
                    order = 2
                )
        )

        assertTrue(
            FormField.Text(
                type = FormField.Type.INFORMATION,
                label = "some label",
                value = PropertyValue.Text(code = "field_text")
            ) == FormField.Text(
                type = FormField.Type.INFORMATION,
                label = "some label",
                value = PropertyValue.Text(code = "field_text")
            )
        )
        assertTrue(
            FormField.Text(
                type = FormField.Type.INFORMATION,
                label = "some label",
                order = 1,
                value = PropertyValue.Text(code = "field_text")
            ) < FormField.Text(
                type = FormField.Type.INFORMATION,
                label = "some label",
                value = PropertyValue.Text(code = "field_text")
            )
        )
        assertTrue(
            FormField.Text(
                type = FormField.Type.INFORMATION,
                label = "some label",
                value = PropertyValue.Text(code = "field_text")
            ) > FormField.Text(
                type = FormField.Type.INFORMATION,
                label = "some label",
                order = 1,
                value = PropertyValue.Text(code = "field_text")
            )
        )
        assertTrue(
            FormField.Text(
                type = FormField.Type.INFORMATION,
                label = "some label",
                order = 1,
                value = PropertyValue.Text(code = "field_text")
            ) < FormField.Text(
                type = FormField.Type.INFORMATION,
                label = "some label",
                order = 2,
                value = PropertyValue.Text(code = "field_text")
            )
        )

        assertTrue(
            FormField.Text(
                type = FormField.Type.INFORMATION,
                label = "some label",
                value = PropertyValue.Text(code = "field_text")
            ) < FormField.Text(
                type = FormField.Type.INFORMATION,
                label = "some label",
                additionalField = true,
                value = PropertyValue.Text(code = "field_text")
            )
        )
        assertTrue(
            FormField.Text(
                type = FormField.Type.INFORMATION,
                label = "some label",
                value = PropertyValue.Text(code = "field_text")
            ) < FormField.Text(
                type = FormField.Type.INFORMATION,
                label = "some label",
                order = 1,
                additionalField = true,
                value = PropertyValue.Text(code = "field_text")
            )
        )
        assertTrue(
            FormField.Text(
                type = FormField.Type.INFORMATION,
                label = "some label",
                order = 2,
                value = PropertyValue.Text(code = "field_text")
            ) < FormField.Text(
                type = FormField.Type.INFORMATION,
                label = "some label",
                order = 1,
                additionalField = true,
                value = PropertyValue.Text(code = "field_text")
            )
        )

        assertTrue(
            FormField.Text(
                type = FormField.Type.INFORMATION,
                label = "some label",
                additionalField = true,
                value = PropertyValue.Text(code = "field_text")
            ) == FormField.Text(
                type = FormField.Type.INFORMATION,
                label = "some label",
                additionalField = true,
                value = PropertyValue.Text(code = "field_text")
            )
        )
        assertTrue(
            FormField.Text(
                type = FormField.Type.INFORMATION,
                label = "some label",
                order = 1,
                additionalField = true,
                value = PropertyValue.Text(code = "field_text")
            ) < FormField.Text(
                type = FormField.Type.INFORMATION,
                label = "some label",
                additionalField = true,
                value = PropertyValue.Text(code = "field_text")
            )
        )
        assertTrue(
            FormField.Text(
                type = FormField.Type.INFORMATION,
                label = "some label",
                additionalField = true,
                value = PropertyValue.Text(code = "field_text")
            ) > FormField.Text(
                type = FormField.Type.INFORMATION,
                label = "some label",
                order = 1,
                additionalField = true,
                value = PropertyValue.Text(code = "field_text")
            )
        )
        assertTrue(
            FormField.Text(
                type = FormField.Type.INFORMATION,
                label = "some label",
                order = 1,
                additionalField = true,
                value = PropertyValue.Text(code = "field_text")
            ) < FormField.Text(
                type = FormField.Type.INFORMATION,
                label = "some label",
                order = 2,
                additionalField = true,
                value = PropertyValue.Text(code = "field_text")
            )
        )
    }

    @Test
    fun `should update existing form field`() {
        assertEquals(
            FormField.Button(
                type = FormField.Type.INFORMATION,
                label = "updated label"
            ),
            FormField.Button(
                type = FormField.Type.INFORMATION,
                label = "some label"
            )
                .update(label = "updated label")
        )
        assertEquals(
            FormField.Button(
                type = FormField.Type.INFORMATION,
                label = "some label",
                visible = false,
                order = 1
            ),
            FormField.Button(
                type = FormField.Type.INFORMATION,
                label = "some label",
                visible = true
            )
                .update(
                    visible = false,
                    order = 1
                )
        )
        assertEquals(
            FormField.Button(
                type = FormField.Type.INFORMATION,
                label = "some label",
                default = false,
                order = 2
            ),
            FormField.Button(
                type = FormField.Type.INFORMATION,
                label = "some label",
                default = true,
                order = 1
            )
                .update(
                    default = false,
                    order = 2
                )
        )

        assertEquals(
            FormField.Checkbox(
                type = FormField.Type.INFORMATION,
                label = "updated label",
                value = PropertyValue.StringArray(code = "some_code")
            ),
            FormField.Checkbox(
                type = FormField.Type.INFORMATION,
                label = "some label",
                value = PropertyValue.StringArray(code = "some_code")
            )
                .update(label = "updated label")
        )
        assertEquals(
            FormField.Checkbox(
                type = FormField.Type.INFORMATION,
                label = "some label",
                visible = false,
                order = 1,
                value = PropertyValue.StringArray(code = "some_code")
            ),
            FormField.Checkbox(
                type = FormField.Type.INFORMATION,
                label = "some label",
                visible = true,
                value = PropertyValue.StringArray(code = "some_code")
            )
                .update(
                    visible = false,
                    order = 1
                )
        )
        assertEquals(
            FormField.Checkbox(
                type = FormField.Type.INFORMATION,
                label = "some label",
                default = false,
                order = 2,
                value = PropertyValue.StringArray(code = "some_code")
            ),
            FormField.Checkbox(
                type = FormField.Type.INFORMATION,
                label = "some label",
                default = true,
                order = 1,
                value = PropertyValue.StringArray(code = "some_code")
            )
                .update(
                    default = false,
                    order = 2
                )
        )

        assertEquals(
            FormField.Date(
                type = FormField.Type.INFORMATION,
                label = "updated label",
                value = PropertyValue.Date(code = "some_code")
            ),
            FormField.Date(
                type = FormField.Type.INFORMATION,
                label = "some label",
                value = PropertyValue.Date(code = "some_code")
            )
                .update(label = "updated label")
        )
        assertEquals(
            FormField.Date(
                type = FormField.Type.INFORMATION,
                label = "some label",
                visible = false,
                order = 1,
                value = PropertyValue.Date(code = "some_code")
            ),
            FormField.Date(
                type = FormField.Type.INFORMATION,
                label = "some label",
                visible = true,
                value = PropertyValue.Date(code = "some_code")
            )
                .update(
                    visible = false,
                    order = 1
                )
        )
        assertEquals(
            FormField.Date(
                type = FormField.Type.INFORMATION,
                label = "some label",
                default = false,
                order = 2,
                value = PropertyValue.Date(code = "some_code")
            ),
            FormField.Date(
                type = FormField.Type.INFORMATION,
                label = "some label",
                default = true,
                order = 1,
                value = PropertyValue.Date(code = "some_code")
            )
                .update(
                    default = false,
                    order = 2
                )
        )

        assertEquals(
            FormField.Media(
                type = FormField.Type.INFORMATION,
                label = "updated label",
                value = PropertyValue.Media(code = "some_code")
            ),
            FormField.Media(
                type = FormField.Type.INFORMATION,
                label = "some label",
                value = PropertyValue.Media(code = "some_code")
            )
                .update(label = "updated label")
        )
        assertEquals(
            FormField.Media(
                type = FormField.Type.INFORMATION,
                label = "some label",
                visible = false,
                order = 1,
                value = PropertyValue.Media(code = "some_code")
            ),
            FormField.Media(
                type = FormField.Type.INFORMATION,
                label = "some label",
                visible = true,
                value = PropertyValue.Media(code = "some_code")
            )
                .update(
                    visible = false,
                    order = 1
                )
        )
        assertEquals(
            FormField.Media(
                type = FormField.Type.INFORMATION,
                label = "some label",
                default = false,
                order = 2,
                value = PropertyValue.Media(code = "some_code")
            ),
            FormField.Media(
                type = FormField.Type.INFORMATION,
                label = "some label",
                default = true,
                order = 1,
                value = PropertyValue.Media(code = "some_code")
            )
                .update(
                    default = false,
                    order = 2
                )
        )

        assertEquals(
            FormField.MinMax(
                type = FormField.Type.INFORMATION,
                label = "updated label",
                min = FormField.Number(
                    type = FormField.Type.INFORMATION,
                    label = "Min",
                    value = PropertyValue.Number(code = "min")
                ),
                max = FormField.Number(
                    type = FormField.Type.INFORMATION,
                    label = "Max",
                    value = PropertyValue.Number(code = "max")
                ),
            ),
            FormField.MinMax(
                type = FormField.Type.INFORMATION,
                label = "some label",
                min = FormField.Number(
                    type = FormField.Type.INFORMATION,
                    label = "Min",
                    value = PropertyValue.Number(code = "min")
                ),
                max = FormField.Number(
                    type = FormField.Type.INFORMATION,
                    label = "Max",
                    value = PropertyValue.Number(code = "max")
                ),
            )
                .update(label = "updated label")
        )
        assertEquals(
            FormField.MinMax(
                type = FormField.Type.INFORMATION,
                label = "some label",
                visible = false,
                min = FormField.Number(
                    type = FormField.Type.INFORMATION,
                    label = "Min",
                    value = PropertyValue.Number(code = "min")
                ),
                max = FormField.Number(
                    type = FormField.Type.INFORMATION,
                    label = "Max",
                    value = PropertyValue.Number(code = "max")
                ),
            ),
            FormField.MinMax(
                type = FormField.Type.INFORMATION,
                label = "some label",
                visible = true,
                min = FormField.Number(
                    type = FormField.Type.INFORMATION,
                    label = "Min",
                    value = PropertyValue.Number(code = "min")
                ),
                max = FormField.Number(
                    type = FormField.Type.INFORMATION,
                    label = "Max",
                    value = PropertyValue.Number(code = "max")
                ),
            )
                .update(visible = false)
        )
        assertEquals(
            FormField.MinMax(
                type = FormField.Type.INFORMATION,
                label = "some label",
                default = false,
                min = FormField.Number(
                    type = FormField.Type.INFORMATION,
                    label = "Min",
                    value = PropertyValue.Number(code = "min")
                ),
                max = FormField.Number(
                    type = FormField.Type.INFORMATION,
                    label = "Max",
                    value = PropertyValue.Number(code = "max")
                ),
            ),
            FormField.MinMax(
                type = FormField.Type.INFORMATION,
                label = "some label",
                default = true,
                min = FormField.Number(
                    type = FormField.Type.INFORMATION,
                    label = "Min",
                    value = PropertyValue.Number(code = "min")
                ),
                max = FormField.Number(
                    type = FormField.Type.INFORMATION,
                    label = "Max",
                    value = PropertyValue.Number(code = "max")
                ),
            )
                .update(default = false)
        )

        assertEquals(
            FormField.NomenclatureType(
                type = FormField.Type.INFORMATION,
                label = "updated label",
                nomenclatureType = "some_nomenclature_type",
                value = PropertyValue.Nomenclature(code = "some_code")
            ),
            FormField.NomenclatureType(
                type = FormField.Type.INFORMATION,
                label = "some label",
                nomenclatureType = "some_nomenclature_type",
                value = PropertyValue.Nomenclature(code = "some_code")
            )
                .update(label = "updated label")
        )
        assertEquals(
            FormField.NomenclatureType(
                type = FormField.Type.INFORMATION,
                label = "some label",
                visible = false,
                nomenclatureType = "some_nomenclature_type",
                value = PropertyValue.Nomenclature(code = "some_code")
            ),
            FormField.NomenclatureType(
                type = FormField.Type.INFORMATION,
                label = "some label",
                visible = true,
                nomenclatureType = "some_nomenclature_type",
                value = PropertyValue.Nomenclature(code = "some_code")
            )
                .update(visible = false)
        )
        assertEquals(
            FormField.NomenclatureType(
                type = FormField.Type.INFORMATION,
                label = "some label",
                default = false,
                nomenclatureType = "some_nomenclature_type",
                value = PropertyValue.Nomenclature(code = "some_code")
            ),
            FormField.NomenclatureType(
                type = FormField.Type.INFORMATION,
                label = "some label",
                default = true,
                nomenclatureType = "some_nomenclature_type",
                value = PropertyValue.Nomenclature(code = "some_code")
            )
                .update(default = false)
        )

        assertEquals(
            FormField.Number(
                type = FormField.Type.INFORMATION,
                label = "updated label",
                value = PropertyValue.Number(code = "some_code")
            ),
            FormField.Number(
                type = FormField.Type.INFORMATION,
                label = "some label",
                value = PropertyValue.Number(code = "some_code")
            )
                .update(label = "updated label")
        )
        assertEquals(
            FormField.Number(
                type = FormField.Type.INFORMATION,
                label = "some label",
                visible = false,
                value = PropertyValue.Number(code = "some_code")
            ),
            FormField.Number(
                type = FormField.Type.INFORMATION,
                label = "some label",
                visible = true,
                value = PropertyValue.Number(code = "some_code")
            )
                .update(visible = false)
        )
        assertEquals(
            FormField.Number(
                type = FormField.Type.INFORMATION,
                label = "some label",
                default = false,
                value = PropertyValue.Number(code = "some_code")
            ),
            FormField.Number(
                type = FormField.Type.INFORMATION,
                label = "some label",
                default = true,
                value = PropertyValue.Number(code = "some_code")
            )
                .update(default = false)
        )

        assertEquals(
            FormField.Radio(
                type = FormField.Type.INFORMATION,
                label = "updated label",
                value = PropertyValue.Text(code = "some_code")
            ),
            FormField.Radio(
                type = FormField.Type.INFORMATION,
                label = "some label",
                value = PropertyValue.Text(code = "some_code")
            )
                .update(label = "updated label")
        )
        assertEquals(
            FormField.Radio(
                type = FormField.Type.INFORMATION,
                label = "some label",
                visible = false,
                value = PropertyValue.Text(code = "some_code")
            ),
            FormField.Radio(
                type = FormField.Type.INFORMATION,
                label = "some label",
                visible = true,
                value = PropertyValue.Text(code = "some_code")
            )
                .update(visible = false)
        )
        assertEquals(
            FormField.Radio(
                type = FormField.Type.INFORMATION,
                label = "some label",
                default = false,
                value = PropertyValue.Text(code = "some_code")
            ),
            FormField.Radio(
                type = FormField.Type.INFORMATION,
                label = "some label",
                default = true,
                value = PropertyValue.Text(code = "some_code")
            )
                .update(default = false)
        )

        assertEquals(
            FormField.Section(
                type = FormField.Type.INFORMATION,
                label = "updated label",
                code = "some_code"
            ),
            FormField.Section(
                type = FormField.Type.INFORMATION,
                label = "some label",
                code = "some_code"
            )
                .update(label = "updated label")
        )
        assertEquals(
            FormField.Section(
                type = FormField.Type.INFORMATION,
                label = "some label",
                visible = false,
                code = "some_code"
            ),
            FormField.Section(
                type = FormField.Type.INFORMATION,
                label = "some label",
                visible = true,
                code = "some_code"
            )
                .update(visible = false)
        )
        assertEquals(
            FormField.Section(
                type = FormField.Type.INFORMATION,
                label = "some label",
                default = false,
                code = "some_code"
            ),
            FormField.Section(
                type = FormField.Type.INFORMATION,
                label = "some label",
                default = true,
                code = "some_code"
            )
                .update(default = false)
        )

        assertEquals(
            FormField.Select(
                type = FormField.Type.INFORMATION,
                label = "updated label",
                value = PropertyValue.Text(code = "some_code")
            ),
            FormField.Select(
                type = FormField.Type.INFORMATION,
                label = "some label",
                value = PropertyValue.Text(code = "some_code")
            )
                .update(label = "updated label")
        )
        assertEquals(
            FormField.Select(
                type = FormField.Type.INFORMATION,
                label = "some label",
                visible = false,
                value = PropertyValue.Text(code = "some_code")
            ),
            FormField.Select(
                type = FormField.Type.INFORMATION,
                label = "some label",
                visible = true,
                value = PropertyValue.Text(code = "some_code")
            )
                .update(visible = false)
        )
        assertEquals(
            FormField.Select(
                type = FormField.Type.INFORMATION,
                label = "some label",
                default = false,
                value = PropertyValue.Text(code = "some_code")
            ),
            FormField.Select(
                type = FormField.Type.INFORMATION,
                label = "some label",
                default = true,
                value = PropertyValue.Text(code = "some_code")
            )
                .update(default = false)
        )

        assertEquals(
            FormField.SelectMultiple(
                type = FormField.Type.INFORMATION,
                label = "updated label",
                value = PropertyValue.StringArray(code = "some_code")
            ),
            FormField.SelectMultiple(
                type = FormField.Type.INFORMATION,
                label = "some label",
                value = PropertyValue.StringArray(code = "some_code")
            )
                .update(label = "updated label")
        )
        assertEquals(
            FormField.SelectMultiple(
                type = FormField.Type.INFORMATION,
                label = "some label",
                visible = false,
                value = PropertyValue.StringArray(code = "some_code")
            ),
            FormField.SelectMultiple(
                type = FormField.Type.INFORMATION,
                label = "some label",
                visible = true,
                value = PropertyValue.StringArray(code = "some_code")
            )
                .update(visible = false)
        )
        assertEquals(
            FormField.SelectMultiple(
                type = FormField.Type.INFORMATION,
                label = "some label",
                default = false,
                value = PropertyValue.StringArray(code = "some_code")
            ),
            FormField.SelectMultiple(
                type = FormField.Type.INFORMATION,
                label = "some label",
                default = true,
                value = PropertyValue.StringArray(code = "some_code")
            )
                .update(default = false)
        )

        assertEquals(
            FormField.Text(
                type = FormField.Type.INFORMATION,
                label = "updated label",
                value = PropertyValue.Text(code = "some_code")
            ),
            FormField.Text(
                type = FormField.Type.INFORMATION,
                label = "some label",
                value = PropertyValue.Text(code = "some_code")
            )
                .update(label = "updated label")
        )
        assertEquals(
            FormField.Text(
                type = FormField.Type.INFORMATION,
                label = "some label",
                visible = false,
                value = PropertyValue.Text(code = "some_code")
            ),
            FormField.Text(
                type = FormField.Type.INFORMATION,
                label = "some label",
                visible = true,
                value = PropertyValue.Text(code = "some_code")
            )
                .update(visible = false)
        )
        assertEquals(
            FormField.Text(
                type = FormField.Type.INFORMATION,
                label = "some label",
                default = false,
                value = PropertyValue.Text(code = "some_code")
            ),
            FormField.Text(
                type = FormField.Type.INFORMATION,
                label = "some label",
                default = true,
                value = PropertyValue.Text(code = "some_code")
            )
                .update(default = false)
        )

        assertEquals(
            FormField.TextMultiple(
                type = FormField.Type.INFORMATION,
                label = "updated label",
                value = PropertyValue.Text(code = "some_code")
            ),
            FormField.TextMultiple(
                type = FormField.Type.INFORMATION,
                label = "some label",
                value = PropertyValue.Text(code = "some_code")
            )
                .update(label = "updated label")
        )
        assertEquals(
            FormField.TextMultiple(
                type = FormField.Type.INFORMATION,
                label = "some label",
                visible = false,
                value = PropertyValue.Text(code = "some_code")
            ),
            FormField.TextMultiple(
                type = FormField.Type.INFORMATION,
                label = "some label",
                visible = true,
                value = PropertyValue.Text(code = "some_code")
            )
                .update(visible = false)
        )
        assertEquals(
            FormField.TextMultiple(
                type = FormField.Type.INFORMATION,
                label = "some label",
                default = false,
                value = PropertyValue.Text(code = "some_code")
            ),
            FormField.TextMultiple(
                type = FormField.Type.INFORMATION,
                label = "some label",
                default = true,
                value = PropertyValue.Text(code = "some_code")
            )
                .update(default = false)
        )

        assertEquals(
            FormField.Time(
                type = FormField.Type.INFORMATION,
                label = "updated label",
                value = PropertyValue.Time(code = "some_code")
            ),
            FormField.Time(
                type = FormField.Type.INFORMATION,
                label = "some label",
                value = PropertyValue.Time(code = "some_code")
            )
                .update(label = "updated label")
        )
        assertEquals(
            FormField.Time(
                type = FormField.Type.INFORMATION,
                label = "some label",
                visible = false,
                value = PropertyValue.Time(code = "some_code")
            ),
            FormField.Time(
                type = FormField.Type.INFORMATION,
                label = "some label",
                visible = true,
                value = PropertyValue.Time(code = "some_code")
            )
                .update(visible = false)
        )
        assertEquals(
            FormField.Time(
                type = FormField.Type.INFORMATION,
                label = "some label",
                default = false,
                value = PropertyValue.Time(code = "some_code")
            ),
            FormField.Time(
                type = FormField.Type.INFORMATION,
                label = "some label",
                default = true,
                value = PropertyValue.Time(code = "some_code")
            )
                .update(default = false)
        )
    }

    @Test
    fun `should edit an editable form field`() {
        assertEquals(
            FormField.Checkbox(
                type = FormField.Type.INFORMATION,
                label = "some label",
                value = PropertyValue.StringArray(
                    code = "some_code",
                    value = arrayOf(
                        "value1",
                        "value2"
                    )
                )
            ),
            FormField.Checkbox(
                type = FormField.Type.INFORMATION,
                label = "some label",
                value = PropertyValue.StringArray(code = "some_code")
            )
                .apply {
                    setValue(
                        PropertyValue.StringArray(
                            "some_code_wrong_code",
                            value = arrayOf(
                                "value1",
                                "value2"
                            )
                        )
                    )
                }
        )

        assertEquals(
            FormField.Date(
                type = FormField.Type.INFORMATION,
                label = "some label",
                value = PropertyValue.Date(
                    code = "some_code",
                    value = toDate("2016-10-28T10:15:00Z")
                )
            ),
            FormField.Date(
                type = FormField.Type.INFORMATION,
                label = "some label",
                value = PropertyValue.Date(code = "some_code")
            )
                .apply {
                    setValue(
                        PropertyValue.Date(
                            code = "some_code",
                            value = toDate("2016-10-28T10:15:00Z")
                        )
                    )
                }
        )

        assertEquals(
            FormField.Media(
                type = FormField.Type.INFORMATION,
                label = "some label",
                value = PropertyValue.Media(
                    code = "some_code",
                    value = arrayOf(MediaRecord.File(path = "/some/path/to/file"))
                )
            ),
            FormField.Media(
                type = FormField.Type.INFORMATION,
                label = "some label",
                value = PropertyValue.Media(code = "some_code")
            )
                .apply {
                    setValue(
                        PropertyValue.Media(
                            code = "some_code",
                            value = arrayOf(MediaRecord.File(path = "/some/path/to/file"))
                        )
                    )
                }
        )

        assertEquals(
            FormField.NomenclatureType(
                type = FormField.Type.INFORMATION,
                label = "some label",
                nomenclatureType = "some_code",
                value = PropertyValue.Nomenclature(
                    code = "some_code",
                    value = 8L
                )
            ),
            FormField.NomenclatureType(
                type = FormField.Type.INFORMATION,
                label = "some label",
                nomenclatureType = "some_code",
                value = PropertyValue.Nomenclature(code = "some_code")
            )
                .apply {
                    setValue(
                        PropertyValue.Nomenclature(
                            code = "some_code",
                            value = 8L
                        )
                    )
                }
        )

        assertEquals(
            FormField.Number(
                type = FormField.Type.INFORMATION,
                label = "some label",
                value = PropertyValue.Number(
                    code = "some_code",
                    value = 8L
                )
            ),
            FormField.Number(
                type = FormField.Type.INFORMATION,
                label = "some label",
                value = PropertyValue.Number(code = "some_code")
            )
                .apply {
                    setValue(
                        PropertyValue.Number(
                            code = "some_code",
                            value = 8L
                        )
                    )
                }
        )

        assertEquals(
            FormField.Radio(
                type = FormField.Type.INFORMATION,
                label = "some label",
                value = PropertyValue.Text(
                    code = "some_code",
                    value = "some_value"
                )
            ),
            FormField.Radio(
                type = FormField.Type.INFORMATION,
                label = "some label",
                value = PropertyValue.Text(code = "some_code")
            )
                .apply {
                    setValue(
                        PropertyValue.Text(
                            code = "some_code",
                            value = "some_value"
                        )
                    )
                }
        )

        assertEquals(
            FormField.Select(
                type = FormField.Type.INFORMATION,
                label = "some label",
                value = PropertyValue.Text(
                    code = "some_code",
                    value = "some_value"
                )
            ),
            FormField.Select(
                type = FormField.Type.INFORMATION,
                label = "some label",
                value = PropertyValue.Text(code = "some_code")
            )
                .apply {
                    setValue(
                        PropertyValue.Text(
                            code = "some_code",
                            value = "some_value"
                        )
                    )
                }
        )

        assertEquals(
            FormField.SelectMultiple(
                type = FormField.Type.INFORMATION,
                label = "some label",
                value = PropertyValue.StringArray(
                    code = "some_code",
                    value = arrayOf("some_value")
                )
            ),
            FormField.SelectMultiple(
                type = FormField.Type.INFORMATION,
                label = "some label",
                value = PropertyValue.StringArray(code = "some_code")
            )
                .apply {
                    setValue(
                        PropertyValue.StringArray(
                            code = "some_code",
                            value = arrayOf("some_value")
                        )
                    )
                }
        )

        assertEquals(
            FormField.Text(
                type = FormField.Type.INFORMATION,
                label = "some label",
                value = PropertyValue.Text(
                    code = "some_code",
                    value = "some_value"
                )
            ),
            FormField.Text(
                type = FormField.Type.INFORMATION,
                label = "some label",
                value = PropertyValue.Text(code = "some_code")
            )
                .apply {
                    setValue(
                        PropertyValue.Text(
                            code = "some_code",
                            value = "some_value"
                        )
                    )
                }
        )

        assertEquals(
            FormField.TextMultiple(
                type = FormField.Type.INFORMATION,
                label = "some label",
                value = PropertyValue.Text(
                    code = "some_code",
                    value = "some_value"
                )
            ),
            FormField.TextMultiple(
                type = FormField.Type.INFORMATION,
                label = "some label",
                value = PropertyValue.Text(code = "some_code")
            )
                .apply {
                    setValue(
                        PropertyValue.Text(
                            code = "some_code",
                            value = "some_value"
                        )
                    )
                }
        )

        assertEquals(
            FormField.Time(
                type = FormField.Type.INFORMATION,
                label = "some label",
                value = PropertyValue.Time(
                    code = "some_code",
                    hour = 8,
                    minute = 15
                )
            ),
            FormField.Time(
                type = FormField.Type.INFORMATION,
                label = "some label",
                value = PropertyValue.Time(code = "some_code")
            )
                .apply {
                    setValue(
                        PropertyValue.Time(
                            code = "some_code",
                            hour = 8,
                            minute = 15
                        )
                    )
                }
        )
    }

    @Test
    fun `should be the same editable nomenclature type`() {
        assertEquals(
            FormField.NomenclatureType(
                type = FormField.Type.INFORMATION,
                label = application.getString(R.string.nomenclature_eta_bio),
                visible = true,
                default = false,
                nomenclatureType = "ETA_BIO",
                value = PropertyValue.Nomenclature(code = "ETA_BIO")
            ),
            FormField.NomenclatureType(
                type = FormField.Type.INFORMATION,
                label = application.getString(R.string.nomenclature_eta_bio),
                visible = true,
                default = false,
                nomenclatureType = "ETA_BIO",
                value = PropertyValue.Nomenclature(code = "ETA_BIO")
            )
        )

        assertEquals(
            FormField.NomenclatureType(
                type = FormField.Type.INFORMATION,
                label = application.getString(R.string.nomenclature_statut_bio),
                visible = false,
                default = false,
                nomenclatureType = "STATUT_BIO",
                value = PropertyValue.Nomenclature(code = "STATUT_BIO")
            ),
            FormField.NomenclatureType(
                type = FormField.Type.INFORMATION,
                label = application.getString(R.string.nomenclature_statut_bio),
                visible = false,
                default = false,
                nomenclatureType = "STATUT_BIO",
                value = PropertyValue.Nomenclature(code = "STATUT_BIO")
            )
        )
    }

    @Test
    fun `should create NomenclatureType from Parcelable`() {
        // given an editable nomenclature type instance
        val formField = FormField.NomenclatureType(
            type = FormField.Type.INFORMATION,
            label = application.getString(R.string.nomenclature_statut_bio),
            visible = false,
            default = false,
            nomenclatureType = "STATUT_BIO",
            value = PropertyValue.Nomenclature(code = "STATUT_BIO")
        )
            .apply { locked = true }

        // when we obtain a Parcel object to write the editable nomenclature type instance to it
        val parcel = Parcel.obtain()
        formField.writeToParcel(
            parcel,
            0
        )

        // reset the parcel for reading
        parcel.setDataPosition(0)

        // then
        assertEquals(
            formField,
            parcelableCreator<FormField.NomenclatureType>().createFromParcel(parcel)
        )
    }

    @Test
    fun `should create a list of NomenclatureType from Parcelable array`() {
        // given a list of editable nomenclature types
        val expectedFormFields = listOf(
            FormField.Text(
                type = FormField.Type.INFORMATION,
                label = application.getString(R.string.nomenclature_determiner),
                visible = true,
                default = false,
                value = PropertyValue.Text(code = "determiner")
            ),
            FormField.NomenclatureType(
                type = FormField.Type.INFORMATION,
                label = application.getString(R.string.nomenclature_statut_bio),
                visible = false,
                default = false,
                nomenclatureType = "STATUT_BIO",
                value = PropertyValue.Nomenclature(
                    code = "STATUT_BIO",
                    label = "Non renseigné",
                    value = 29L
                )
            )
                .apply { locked = true }
        )

        // when creating a bundle of them
        val bundle = Bundle().apply {
            putParcelableArray(
                "editable_nomenclature_types",
                expectedFormFields.toTypedArray()
            )
        }

        // then
        assertArrayEquals(
            expectedFormFields.toTypedArray(),
            bundle.getParcelableArrayCompat("editable_nomenclature_types")
        )
    }
}