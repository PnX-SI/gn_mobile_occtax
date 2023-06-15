package fr.geonature.occtax.features.nomenclature.usecase

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import fr.geonature.commons.data.entity.Taxonomy
import fr.geonature.commons.features.nomenclature.error.NomenclatureException
import fr.geonature.commons.fp.Either.Right
import fr.geonature.occtax.CoroutineTestRule
import fr.geonature.occtax.features.nomenclature.domain.EditableField
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

/**
 * Unit tests about [GetEditableFieldsUseCase].
 *
 * @author S. Grimault
 */
@ExperimentalCoroutinesApi
class GetEditableFieldsUseCaseTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

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

        getEditableFieldsUseCase = GetEditableFieldsUseCase(
            nomenclatureRepository,
            additionalFieldRepository,
            defaultPropertyValueRepository
        )
    }

    @Test
    fun `should get all editable fields with default value by nomenclature main type`() =
        runTest {
            // given some editable fields
            coEvery {
                nomenclatureRepository.getEditableFields(any())
            } returns Result.success(
                listOf(
                    EditableField(
                        EditableField.Type.INFORMATION,
                        "METH_OBS",
                        EditableField.ViewType.NOMENCLATURE_TYPE,
                        label = "Méthodes d'observation"
                    ),
                    EditableField(
                        EditableField.Type.INFORMATION,
                        "ETA_BIO",
                        EditableField.ViewType.NOMENCLATURE_TYPE,
                        label = "Etat biologique de l'observation"
                    ),
                    EditableField(
                        EditableField.Type.INFORMATION,
                        "STATUT_BIO",
                        EditableField.ViewType.NOMENCLATURE_TYPE,
                        label = "Statut biologique",
                        visible = false,
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

            // when fetching all editable fields with default value
            val result = getEditableFieldsUseCase.run(
                GetEditableFieldsUseCase.Params(type = EditableField.Type.INFORMATION)
            )

            // then
            assertEquals(
                listOf(
                    EditableField(
                        EditableField.Type.INFORMATION,
                        "METH_OBS",
                        EditableField.ViewType.NOMENCLATURE_TYPE,
                        label = "Méthodes d'observation"
                    ),
                    EditableField(
                        EditableField.Type.INFORMATION,
                        "ETA_BIO",
                        EditableField.ViewType.NOMENCLATURE_TYPE,
                        label = "Etat biologique de l'observation"
                    ),
                    EditableField(
                        EditableField.Type.INFORMATION,
                        "STATUT_BIO",
                        EditableField.ViewType.NOMENCLATURE_TYPE,
                        label = "Statut biologique",
                        visible = false,
                        value = PropertyValue.Nomenclature(
                            code = "STATUT_BIO",
                            label = "Non renseigné",
                            value = 29L
                        )
                    )
                ).sortedBy { it.code },
                result.getOrThrow()
                    .sortedBy { it.code }
            )
        }

    @Test
    fun `should get all editable fields with some property values defined by nomenclature main type`() =
        runTest {
            // given some editable fields
            coEvery {
                nomenclatureRepository.getEditableFields(any())
            } returns Result.success(
                listOf(
                    EditableField(
                        EditableField.Type.INFORMATION,
                        "METH_OBS",
                        EditableField.ViewType.NOMENCLATURE_TYPE,
                        label = "Méthodes d'observation"
                    ),
                    EditableField(
                        EditableField.Type.INFORMATION,
                        "ETA_BIO",
                        EditableField.ViewType.NOMENCLATURE_TYPE,
                        label = "Etat biologique de l'observation"
                    ),
                    EditableField(
                        EditableField.Type.INFORMATION,
                        "DETERMINER",
                        EditableField.ViewType.TEXT_SIMPLE,
                        visible = true,
                        default = false
                    ),
                    EditableField(
                        EditableField.Type.INFORMATION,
                        "STATUT_BIO",
                        EditableField.ViewType.NOMENCLATURE_TYPE,
                        label = "Statut biologique",
                        visible = false,
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
                        "DETERMINER",
                        "some_value"
                    )
                )
            )

            // when fetching all editable fields with values matching given taxonomy
            val result =
                getEditableFieldsUseCase.run(
                    GetEditableFieldsUseCase.Params(
                        type = EditableField.Type.INFORMATION,
                        taxonomy = Taxonomy(
                            kingdom = "Animalia",
                            group = "Oiseaux"
                        )
                    )
                )

            // then
            assertEquals(
                listOf(
                    EditableField(
                        EditableField.Type.INFORMATION,
                        "METH_OBS",
                        EditableField.ViewType.NOMENCLATURE_TYPE,
                        label = "Méthodes d'observation"
                    ),
                    EditableField(
                        EditableField.Type.INFORMATION,
                        "ETA_BIO",
                        EditableField.ViewType.NOMENCLATURE_TYPE,
                        label = "Etat biologique de l'observation"
                    ),
                    EditableField(
                        EditableField.Type.INFORMATION,
                        "DETERMINER",
                        EditableField.ViewType.TEXT_SIMPLE,
                        visible = true,
                        default = false,
                        value = PropertyValue.Text(
                            "DETERMINER",
                            "some_value"
                        ),
                        locked = true
                    ),
                    EditableField(
                        EditableField.Type.INFORMATION,
                        "STATUT_BIO",
                        EditableField.ViewType.NOMENCLATURE_TYPE,
                        label = "Statut biologique",
                        visible = false,
                        value = PropertyValue.Nomenclature(
                            code = "STATUT_BIO",
                            label = "Hibernation",
                            value = 33L
                        ),
                        locked = true
                    )
                ).sortedBy { it.code },
                result.getOrThrow()
                    .sortedBy { it.code }
            )
        }

    @Test
    fun `should return NoNomenclatureTypeFoundLocallyFailure if no nomenclature types was found`() =
        runTest {
            // given some failure from repository
            coEvery {
                nomenclatureRepository.getEditableFields(EditableField.Type.INFORMATION)
            } returns Result.failure(NomenclatureException.NoNomenclatureTypeFoundException)

            // when fetching all editable fields with default value
            val result = getEditableFieldsUseCase.run(
                GetEditableFieldsUseCase.Params(type = EditableField.Type.INFORMATION)
            )

            // then
            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is NomenclatureException.NoNomenclatureTypeFoundException)
        }
}