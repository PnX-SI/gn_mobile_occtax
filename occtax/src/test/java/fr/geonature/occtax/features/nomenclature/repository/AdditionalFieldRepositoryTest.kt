package fr.geonature.occtax.features.nomenclature.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import fr.geonature.commons.data.entity.AdditionalField
import fr.geonature.commons.data.entity.AdditionalFieldWithValues
import fr.geonature.commons.data.entity.CodeObject
import fr.geonature.commons.data.entity.FieldValue
import fr.geonature.commons.features.nomenclature.data.IAdditionalFieldLocalDataSource
import fr.geonature.commons.util.toDate
import fr.geonature.occtax.CoroutineTestRule
import fr.geonature.occtax.features.nomenclature.domain.AdditionalFieldType
import fr.geonature.occtax.features.nomenclature.domain.FormField
import fr.geonature.occtax.features.record.domain.PropertyValue
import io.mockk.MockKAnnotations.init
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests about [IAdditionalFieldRepository].
 *
 * @author S. Grimault
 */
@ExperimentalCoroutinesApi
class AdditionalFieldRepositoryTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    @MockK
    private lateinit var additionalFieldLocalDataSource: IAdditionalFieldLocalDataSource

    private lateinit var additionalFieldRepository: IAdditionalFieldRepository

    @Before
    fun setUp() {
        init(this)

        additionalFieldRepository = AdditionalFieldRepositoryImpl(additionalFieldLocalDataSource)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `should return an empty list if no additional fields was found`() = runTest {
        // given no additional fields
        coEvery {
            additionalFieldLocalDataSource.getAdditionalFields(
                any(),
                any()
            )
        } returns listOf()

        // when
        val result = additionalFieldRepository.getAllAdditionalFields(
            null,
            FormField.Type.DEFAULT
        )

        // then
        assertTrue(result.isSuccess)
        assertTrue(
            result.getOrNull()
                ?.isEmpty() == true
        )
    }

    @Test
    fun `should return a list of additional fields matching form field of type 'INFORMATION' with default value`() =
        runTest {
            // given some additional fields
            coEvery {
                additionalFieldLocalDataSource.getAdditionalFields(
                    any(),
                    AdditionalFieldType.INFORMATION.type
                )
            } returns listOf(
                AdditionalFieldWithValues(
                    additionalField = AdditionalField(
                        id = 1,
                        fieldType = AdditionalField.FieldType.CHECKBOX,
                        name = "checkbox_occ",
                        label = "As checkbox",
                        description = "Some description",
                        defaultValue = "k1"
                    ),
                    codeObjects = listOf(
                        CodeObject(
                            additionalFieldId = 1,
                            key = "OCCTAX_OCCURENCE"
                        )
                    ),
                    values = listOf(
                        FieldValue(
                            additionalFieldId = 1,
                            value = "k1",
                            label = "value 1"
                        ),
                        FieldValue(
                            additionalFieldId = 1,
                            value = "k2",
                            label = "value 2"
                        )
                    )
                ),
                AdditionalFieldWithValues(
                    additionalField = AdditionalField(
                        id = 2,
                        fieldType = AdditionalField.FieldType.DATE,
                        name = "date_occ",
                        label = "As date",
                        description = "Some description",
                        defaultValue = "2024-11-19"
                    ),
                    codeObjects = listOf(
                        CodeObject(
                            additionalFieldId = 2,
                            key = "OCCTAX_OCCURENCE"
                        )
                    )
                ),
                AdditionalFieldWithValues(
                    additionalField = AdditionalField(
                        id = 3,
                        fieldType = AdditionalField.FieldType.MULTISELECT,
                        name = "multi_select_occ",
                        label = "As multiselect",
                        description = "Some description",
                        defaultValue = "k2"
                    ),
                    codeObjects = listOf(
                        CodeObject(
                            additionalFieldId = 3,
                            key = "OCCTAX_OCCURENCE"
                        )
                    ),
                    values = listOf(
                        FieldValue(
                            additionalFieldId = 1,
                            value = "k1",
                            label = "value 1"
                        ),
                        FieldValue(
                            additionalFieldId = 1,
                            value = "k2",
                            label = "value 2"
                        )
                    )
                ),
                AdditionalFieldWithValues(
                    additionalField = AdditionalField(
                        id = 4,
                        fieldType = AdditionalField.FieldType.NOMENCLATURE,
                        name = "nomenclature_occ",
                        label = "From nomenclature",
                        description = "Some description",
                        defaultValue = "24"
                    ),
                    nomenclatureTypeMnemonic = "SOME_CODE",
                    codeObjects = listOf(
                        CodeObject(
                            additionalFieldId = 4,
                            key = "OCCTAX_OCCURENCE"
                        )
                    ),
                    values = listOf(
                        FieldValue(
                            additionalFieldId = 4,
                            value = "17",
                            label = "value 1"
                        ),
                        FieldValue(
                            additionalFieldId = 4,
                            value = "24",
                            label = "value 2"
                        )
                    )
                ),
                AdditionalFieldWithValues(
                    additionalField = AdditionalField(
                        id = 5,
                        fieldType = AdditionalField.FieldType.NUMBER,
                        name = "number_occ",
                        label = "As number",
                        description = "Some description",
                        defaultValue = "17"
                    ),
                    codeObjects = listOf(
                        CodeObject(
                            additionalFieldId = 5,
                            key = "OCCTAX_OCCURENCE"
                        )
                    )
                ),
                AdditionalFieldWithValues(
                    additionalField = AdditionalField(
                        id = 6,
                        fieldType = AdditionalField.FieldType.RADIO,
                        name = "radio_occ",
                        label = "As radio",
                        description = "Some description",
                        defaultValue = "k2"
                    ),
                    codeObjects = listOf(
                        CodeObject(
                            additionalFieldId = 6,
                            key = "OCCTAX_OCCURENCE"
                        )
                    ),
                    values = listOf(
                        FieldValue(
                            additionalFieldId = 6,
                            value = "k1",
                            label = "value 1"
                        ),
                        FieldValue(
                            additionalFieldId = 6,
                            value = "k2",
                            label = "value 2"
                        )
                    )
                ),
                AdditionalFieldWithValues(
                    additionalField = AdditionalField(
                        id = 7,
                        fieldType = AdditionalField.FieldType.SELECT,
                        name = "select_occ",
                        label = "As select",
                        description = "Some description",
                        defaultValue = "k2"
                    ),
                    codeObjects = listOf(
                        CodeObject(
                            additionalFieldId = 7,
                            key = "OCCTAX_OCCURENCE"
                        )
                    ),
                    values = listOf(
                        FieldValue(
                            additionalFieldId = 7,
                            value = "k1",
                            label = "value 1"
                        ),
                        FieldValue(
                            additionalFieldId = 7,
                            value = "k2",
                            label = "value 2"
                        )
                    )
                ),
                AdditionalFieldWithValues(
                    additionalField = AdditionalField(
                        id = 8,
                        fieldType = AdditionalField.FieldType.TEXT,
                        name = "text_occ",
                        label = "As text",
                        description = "Some description",
                        mandatory = true,
                        order = 8,
                        defaultValue = "default value"
                    ),
                    codeObjects = listOf(
                        CodeObject(
                            additionalFieldId = 8,
                            key = "OCCTAX_OCCURENCE"
                        )
                    )
                ),
                AdditionalFieldWithValues(
                    additionalField = AdditionalField(
                        id = 9,
                        fieldType = AdditionalField.FieldType.TEXTAREA,
                        name = "textarea_occ",
                        label = "As text",
                        description = "Some description",
                        defaultValue = "default value"
                    ),
                    codeObjects = listOf(
                        CodeObject(
                            additionalFieldId = 9,
                            key = "OCCTAX_OCCURENCE"
                        )
                    )
                ),
                AdditionalFieldWithValues(
                    additionalField = AdditionalField(
                        id = 10,
                        fieldType = AdditionalField.FieldType.TIME,
                        name = "time_occ",
                        label = "As time",
                        description = "Some description",
                        defaultValue = "08:15"
                    ),
                    codeObjects = listOf(
                        CodeObject(
                            additionalFieldId = 10,
                            key = "OCCTAX_OCCURENCE"
                        )
                    )
                )
            )

            // when
            val result = additionalFieldRepository.getAllAdditionalFields(
                null,
                FormField.Type.INFORMATION
            )

            // then
            assertTrue(result.isSuccess)
            assertEquals(
                listOf(
                    FormField.Checkbox(
                        type = FormField.Type.INFORMATION,
                        label = "As checkbox",
                        additionalField = true,
                        values = listOf(
                            PropertyValue.Text(
                                code = "k1",
                                value = "value 1"
                            ),
                            PropertyValue.Text(
                                code = "k2",
                                value = "value 2"
                            )
                        ),
                        value = PropertyValue.StringArray(
                            code = "checkbox_occ",
                            value = arrayOf("k1")
                        )
                    ),
                    FormField.Date(
                        type = FormField.Type.INFORMATION,
                        label = "As date",
                        additionalField = true,
                        value = PropertyValue.Date(
                            code = "date_occ",
                            value = toDate("2024-11-19")
                        )
                    ),
                    FormField.SelectMultiple(
                        type = FormField.Type.INFORMATION,
                        label = "As multiselect",
                        additionalField = true,
                        values = listOf(
                            PropertyValue.Text(
                                code = "k1",
                                value = "value 1"
                            ),
                            PropertyValue.Text(
                                code = "k2",
                                value = "value 2"
                            )
                        ),
                        value = PropertyValue.StringArray(
                            code = "multi_select_occ",
                            value = arrayOf("k2")
                        )
                    ),
                    FormField.NomenclatureType(
                        type = FormField.Type.INFORMATION,
                        label = "From nomenclature",
                        additionalField = true,
                        nomenclatureType = "SOME_CODE",
                        value = PropertyValue.Nomenclature(
                            code = "nomenclature_occ",
                            value = 24L,
                        )
                    ),
                    FormField.Number(
                        type = FormField.Type.INFORMATION,
                        label = "As number",
                        additionalField = true,
                        value = PropertyValue.Number(
                            code = "number_occ",
                            value = 17L
                        )
                    ),
                    FormField.Radio(
                        type = FormField.Type.INFORMATION,
                        label = "As radio",
                        additionalField = true,
                        values = listOf(
                            PropertyValue.Text(
                                code = "k1",
                                value = "value 1"
                            ),
                            PropertyValue.Text(
                                code = "k2",
                                value = "value 2"
                            )
                        ),
                        value = PropertyValue.Text(
                            code = "radio_occ",
                            value = "k2"
                        )
                    ),
                    FormField.Select(
                        type = FormField.Type.INFORMATION,
                        label = "As select",
                        additionalField = true,
                        values = listOf(
                            PropertyValue.Text(
                                code = "k1",
                                value = "value 1"
                            ),
                            PropertyValue.Text(
                                code = "k2",
                                value = "value 2"
                            )
                        ),
                        value = PropertyValue.Text(
                            code = "select_occ",
                            value = "k2"
                        )
                    ),
                    FormField.Text(
                        type = FormField.Type.INFORMATION,
                        label = "As text",
                        order = 8,
                        additionalField = true,
                        mandatory = true,
                        value = PropertyValue.Text(
                            code = "text_occ",
                            value = "default value"
                        )
                    ),
                    FormField.TextMultiple(
                        type = FormField.Type.INFORMATION,
                        label = "As text",
                        additionalField = true,
                        value = PropertyValue.Text(
                            code = "textarea_occ",
                            value = "default value"
                        )
                    ),
                    FormField.Time(
                        type = FormField.Type.INFORMATION,
                        label = "As time",
                        additionalField = true,
                        value = PropertyValue.Time(
                            code = "time_occ",
                            hour = 8,
                            minute = 15
                        )
                    )
                ),
                result.getOrNull()
            )
        }
}