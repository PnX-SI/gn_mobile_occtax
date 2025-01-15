package fr.geonature.occtax.features.nomenclature.usecase

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import fr.geonature.commons.data.entity.Taxonomy
import fr.geonature.commons.features.nomenclature.error.NomenclatureException
import fr.geonature.commons.fp.Either.Right
import fr.geonature.occtax.CoroutineTestRule
import fr.geonature.occtax.R
import fr.geonature.occtax.features.nomenclature.domain.FormField
import fr.geonature.occtax.features.nomenclature.repository.IAdditionalFieldRepository
import fr.geonature.occtax.features.nomenclature.repository.IDefaultPropertyValueRepository
import fr.geonature.occtax.features.nomenclature.repository.INomenclatureRepository
import fr.geonature.occtax.features.record.domain.PropertyValue
import io.mockk.MockKAnnotations.init
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [GetEditableFieldsUseCase].
 *
 * @author S. Grimault
 */
@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class GetEditableFieldsUseCaseTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private lateinit var application: Application

    @MockK
    private lateinit var nomenclatureRepository: INomenclatureRepository

    @MockK
    private lateinit var additionalFieldRepository: IAdditionalFieldRepository

    @MockK
    private lateinit var defaultPropertyValueRepository: IDefaultPropertyValueRepository

    private lateinit var getEditableFieldsUseCase: GetEditableFieldsUseCase

    @Before
    fun setUp() {
        init(this)

        application = ApplicationProvider.getApplicationContext()

        getEditableFieldsUseCase = GetEditableFieldsUseCase(
            nomenclatureRepository,
            additionalFieldRepository,
            defaultPropertyValueRepository
        )
    }

    @Test
    fun `should get all form fields with default value by nomenclature main type`() =
        runTest {
            // given some form fields
            coEvery {
                nomenclatureRepository.getEditableFields(any())
            } returns Result.success(
                listOf(
                    FormField.NomenclatureType(
                        type = FormField.Type.INFORMATION,
                        label = "Méthodes d'observation",
                        nomenclatureType = "METH_OBS",
                        value = PropertyValue.Nomenclature(code = "METH_OBS")
                    ),
                    FormField.NomenclatureType(
                        type = FormField.Type.INFORMATION,
                        label = "Etat biologique de l'observation",
                        nomenclatureType = "ETA_BIO",
                        value = PropertyValue.Nomenclature(code = "ETA_BIO")
                    ),
                    FormField.NomenclatureType(
                        type = FormField.Type.INFORMATION,
                        label = "Statut biologique",
                        default = false,
                        visible = false,
                        nomenclatureType = "STATUT_BIO",
                        value = PropertyValue.Nomenclature(
                            code = "STATUT_BIO",
                            label = "Non renseigné",
                            value = 29L
                        )
                    )
                )
            )

            // and no additional fields
            coEvery {
                additionalFieldRepository.getAllAdditionalFields(
                    any(),
                    any()
                )
            } returns Result.success(emptyList())

            // and no default property values
            coEvery { defaultPropertyValueRepository.getPropertyValues() } returns Right(listOf())

            // when fetching all form fields with default value
            val result = getEditableFieldsUseCase.run(
                GetEditableFieldsUseCase.Params(type = FormField.Type.INFORMATION)
            )

            // then
            assertEquals(
                listOf(
                    FormField.NomenclatureType(
                        type = FormField.Type.INFORMATION,
                        label = "Méthodes d'observation",
                        nomenclatureType = "METH_OBS",
                        value = PropertyValue.Nomenclature(code = "METH_OBS")
                    ),
                    FormField.NomenclatureType(
                        type = FormField.Type.INFORMATION,
                        label = "Etat biologique de l'observation",
                        nomenclatureType = "ETA_BIO",
                        value = PropertyValue.Nomenclature(code = "ETA_BIO")
                    ),
                    FormField.NomenclatureType(
                        type = FormField.Type.INFORMATION,
                        label = "Statut biologique",
                        default = false,
                        visible = false,
                        nomenclatureType = "STATUT_BIO",
                        value = PropertyValue.Nomenclature(
                            code = "STATUT_BIO",
                            label = "Non renseigné",
                            value = 29L
                        )
                    )
                ),
                result.getOrThrow()
            )
        }

    @Test
    fun `should get all form fields with some property values defined by nomenclature main type`() =
        runTest {
            // given some form fields
            coEvery {
                nomenclatureRepository.getEditableFields(any())
            } returns Result.success(
                listOf(
                    FormField.NomenclatureType(
                        type = FormField.Type.INFORMATION,
                        label = "Méthodes d'observation",
                        nomenclatureType = "METH_OBS",
                        value = PropertyValue.Nomenclature(code = "METH_OBS")
                    ),
                    FormField.NomenclatureType(
                        type = FormField.Type.INFORMATION,
                        label = "Etat biologique de l'observation",
                        nomenclatureType = "ETA_BIO",
                        value = PropertyValue.Nomenclature(code = "ETA_BIO")
                    ),
                    FormField.Text(
                        type = FormField.Type.INFORMATION,
                        label = application.getString(R.string.nomenclature_determiner),
                        default = false,
                        visible = true,
                        value = PropertyValue.Text(code = "determiner")
                    ),
                    FormField.NomenclatureType(
                        type = FormField.Type.INFORMATION,
                        label = "Statut biologique",
                        visible = false,
                        nomenclatureType = "STATUT_BIO",
                        value = PropertyValue.Nomenclature(
                            code = "STATUT_BIO",
                            label = "Non renseigné",
                            value = 29L
                        )
                    )
                )
            )

            // and no additional fields
            coEvery {
                additionalFieldRepository.getAllAdditionalFields(
                    any(),
                    any()
                )
            } returns Result.success(emptyList())

            // and some default property values matching taxonomy
            coEvery {
                defaultPropertyValueRepository.getPropertyValues(
                    Taxonomy(
                        kingdom = "Animalia",
                        group = "Oiseaux"
                    )
                )
            } returns Right(
                listOf(
                    PropertyValue.Nomenclature(
                        code = "STATUT_BIO",
                        label = "Hibernation",
                        value = 33L
                    ),
                    PropertyValue.Text(
                        "determiner",
                        "some_value"
                    )
                )
            )

            // when fetching all form fields with values matching given taxonomy
            val result =
                getEditableFieldsUseCase.run(
                    GetEditableFieldsUseCase.Params(
                        type = FormField.Type.INFORMATION,
                        taxonomy = Taxonomy(
                            kingdom = "Animalia",
                            group = "Oiseaux"
                        )
                    )
                )

            // then
            assertEquals(
                listOf(
                    FormField.NomenclatureType(
                        type = FormField.Type.INFORMATION,
                        label = "Méthodes d'observation",
                        nomenclatureType = "METH_OBS",
                        value = PropertyValue.Nomenclature(code = "METH_OBS")
                    ),
                    FormField.NomenclatureType(
                        type = FormField.Type.INFORMATION,
                        label = "Etat biologique de l'observation",
                        nomenclatureType = "ETA_BIO",
                        value = PropertyValue.Nomenclature(code = "ETA_BIO")
                    ),
                    FormField.Text(
                        type = FormField.Type.INFORMATION,
                        label = application.getString(R.string.nomenclature_determiner),
                        default = false,
                        visible = true,
                        value = PropertyValue.Text(
                            code = "determiner",
                            value = "some_value"
                        )
                    )
                        .apply { locked = true },
                    FormField.NomenclatureType(
                        type = FormField.Type.INFORMATION,
                        label = "Statut biologique",
                        visible = false,
                        nomenclatureType = "STATUT_BIO",
                        value = PropertyValue.Nomenclature(
                            code = "STATUT_BIO",
                            label = "Hibernation",
                            value = 33L
                        )
                    )
                        .apply { locked = true }
                ),
                result.getOrThrow()
            )
        }

    @Test
    fun `should get all form fields with additional fields`() =
        runTest {
            // given some form fields
            coEvery {
                nomenclatureRepository.getEditableFields(any())
            } returns Result.success(
                listOf(
                    FormField.NomenclatureType(
                        type = FormField.Type.INFORMATION,
                        label = "Méthodes d'observation",
                        nomenclatureType = "METH_OBS",
                        value = PropertyValue.Nomenclature(code = "METH_OBS")
                    ),
                    FormField.NomenclatureType(
                        type = FormField.Type.INFORMATION,
                        label = "Etat biologique de l'observation",
                        visible = false,
                        nomenclatureType = "ETA_BIO",
                        value = PropertyValue.Nomenclature(code = "ETA_BIO")
                    )
                )
            )

            // with additional fields
            coEvery {
                additionalFieldRepository.getAllAdditionalFields(
                    any(),
                    any()
                )
            } returns Result.success(
                listOf(
                    FormField.Text(
                        type = FormField.Type.INFORMATION,
                        additionalField = true,
                        label = "As text",
                        value = PropertyValue.Text(code = "as_text")
                    )
                )
            )

            // and no default property values
            coEvery { defaultPropertyValueRepository.getPropertyValues() } returns Right(listOf())

            // when fetching all form fields with default value
            val result = getEditableFieldsUseCase.run(
                GetEditableFieldsUseCase.Params(
                    withAdditionalFields = true,
                    type = FormField.Type.INFORMATION
                )
            )

            // then
            assertEquals(
                listOf(
                    FormField.NomenclatureType(
                        type = FormField.Type.INFORMATION,
                        label = "Méthodes d'observation",
                        nomenclatureType = "METH_OBS",
                        value = PropertyValue.Nomenclature(code = "METH_OBS")
                    ),
                    FormField.NomenclatureType(
                        type = FormField.Type.INFORMATION,
                        label = "Etat biologique de l'observation",
                        visible = false,
                        nomenclatureType = "ETA_BIO",
                        value = PropertyValue.Nomenclature(code = "ETA_BIO")
                    ),
                    FormField.Text(
                        type = FormField.Type.INFORMATION,
                        additionalField = true,
                        label = "As text",
                        value = PropertyValue.Text(code = "as_text")
                    )
                ),
                result.getOrThrow()
            )
        }

    @Test
    fun `should get all form fields with no additional fields`() =
        runTest {
            // given some form fields
            coEvery {
                nomenclatureRepository.getEditableFields(any())
            } returns Result.success(
                listOf(
                    FormField.NomenclatureType(
                        type = FormField.Type.INFORMATION,
                        label = "Méthodes d'observation",
                        nomenclatureType = "METH_OBS",
                        value = PropertyValue.Nomenclature(code = "METH_OBS")
                    ),
                    FormField.NomenclatureType(
                        type = FormField.Type.INFORMATION,
                        label = "Etat biologique de l'observation",
                        visible = false,
                        nomenclatureType = "ETA_BIO",
                        value = PropertyValue.Nomenclature(code = "ETA_BIO")
                    )
                )
            )

            // with additional fields
            coEvery {
                additionalFieldRepository.getAllAdditionalFields(
                    any(),
                    any()
                )
            } returns Result.success(
                listOf(
                    FormField.Text(
                        type = FormField.Type.INFORMATION,
                        additionalField = true,
                        label = "As text",
                        value = PropertyValue.Text(code = "as_text")
                    )
                )
            )

            // and no default property values
            coEvery { defaultPropertyValueRepository.getPropertyValues() } returns Right(listOf())

            // when fetching all form fields with default value
            val result = getEditableFieldsUseCase.run(
                GetEditableFieldsUseCase.Params(type = FormField.Type.INFORMATION)
            )

            // then
            assertEquals(
                listOf(
                    FormField.NomenclatureType(
                        type = FormField.Type.INFORMATION,
                        label = "Méthodes d'observation",
                        nomenclatureType = "METH_OBS",
                        value = PropertyValue.Nomenclature(code = "METH_OBS")
                    ),
                    FormField.NomenclatureType(
                        type = FormField.Type.INFORMATION,
                        label = "Etat biologique de l'observation",
                        visible = false,
                        nomenclatureType = "ETA_BIO",
                        value = PropertyValue.Nomenclature(code = "ETA_BIO")
                    )
                ),
                result.getOrThrow()
            )
        }

    @Test
    fun `should return NoNomenclatureTypeFoundException failure if no nomenclature types was found`() =
        runTest {
            // given some failure from repository
            coEvery {
                nomenclatureRepository.getEditableFields(FormField.Type.INFORMATION)
            } returns Result.failure(NomenclatureException.NoNomenclatureTypeFoundException)

            // when fetching all form fields with default value
            val result = getEditableFieldsUseCase.run(
                GetEditableFieldsUseCase.Params(type = FormField.Type.INFORMATION)
            )

            // then
            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is NomenclatureException.NoNomenclatureTypeFoundException)
        }
}