package fr.geonature.occtax.features.nomenclature.usecase

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import fr.geonature.commons.data.entity.Taxonomy
import fr.geonature.commons.fp.Either.Left
import fr.geonature.commons.fp.Either.Right
import fr.geonature.commons.fp.identity
import fr.geonature.commons.fp.orNull
import fr.geonature.occtax.CoroutineTestRule
import fr.geonature.occtax.features.input.domain.PropertyValue
import fr.geonature.occtax.features.nomenclature.domain.EditableNomenclatureType
import fr.geonature.occtax.features.nomenclature.error.NoNomenclatureTypeFoundLocallyFailure
import fr.geonature.occtax.features.nomenclature.repository.IDefaultPropertyValueRepository
import fr.geonature.occtax.features.nomenclature.repository.INomenclatureRepository
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
 * Unit tests about [GetEditableNomenclaturesUseCase].
 *
 * @author S. Grimault
 */
@ExperimentalCoroutinesApi
class GetEditableNomenclaturesUseCaseTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    @MockK
    private lateinit var nomenclatureRepository: INomenclatureRepository

    @MockK
    private lateinit var defaultPropertyValueRepository: IDefaultPropertyValueRepository

    private lateinit var getEditableNomenclaturesUseCase: GetEditableNomenclaturesUseCase

    @Before
    fun setUp() {
        init(this)

        getEditableNomenclaturesUseCase = GetEditableNomenclaturesUseCase(
            nomenclatureRepository,
            defaultPropertyValueRepository
        )
    }

    @Test
    fun `should get all editable nomenclature types with default value by nomenclature main type`() =
        runTest {
            // given some nomenclature types
            coEvery { nomenclatureRepository.getEditableNomenclatures(EditableNomenclatureType.Type.INFORMATION) } returns Right(
                listOf(
                    EditableNomenclatureType(
                        EditableNomenclatureType.Type.INFORMATION,
                        "METH_OBS",
                        EditableNomenclatureType.ViewType.NOMENCLATURE_TYPE,
                        label = "Méthodes d'observation"
                    ),
                    EditableNomenclatureType(
                        EditableNomenclatureType.Type.INFORMATION,
                        "ETA_BIO",
                        EditableNomenclatureType.ViewType.NOMENCLATURE_TYPE,
                        label = "Etat biologique de l'observation"
                    ),
                    EditableNomenclatureType(
                        EditableNomenclatureType.Type.INFORMATION,
                        "STATUT_BIO",
                        EditableNomenclatureType.ViewType.NOMENCLATURE_TYPE,
                        label = "Statut biologique",
                        visible = false,
                        value = PropertyValue(
                            code = "STATUT_BIO",
                            label = "Non renseigné",
                            value = 29L
                        )
                    )
                )
            )
            // and no default property values
            coEvery { defaultPropertyValueRepository.getPropertyValues() } returns Right(listOf())

            // when fetching all editable nomenclature types with default value
            val response =
                getEditableNomenclaturesUseCase.run(GetEditableNomenclaturesUseCase.Params(EditableNomenclatureType.Type.INFORMATION))

            // then
            assertEquals(
                listOf(
                    EditableNomenclatureType(
                        EditableNomenclatureType.Type.INFORMATION,
                        "METH_OBS",
                        EditableNomenclatureType.ViewType.NOMENCLATURE_TYPE,
                        label = "Méthodes d'observation"
                    ),
                    EditableNomenclatureType(
                        EditableNomenclatureType.Type.INFORMATION,
                        "ETA_BIO",
                        EditableNomenclatureType.ViewType.NOMENCLATURE_TYPE,
                        label = "Etat biologique de l'observation"
                    ),
                    EditableNomenclatureType(
                        EditableNomenclatureType.Type.INFORMATION,
                        "STATUT_BIO",
                        EditableNomenclatureType.ViewType.NOMENCLATURE_TYPE,
                        label = "Statut biologique",
                        visible = false,
                        value = PropertyValue(
                            code = "STATUT_BIO",
                            label = "Non renseigné",
                            value = 29L
                        )
                    )
                ).sortedBy { it.code },
                response.orNull()?.sortedBy { it.code }
            )
        }

    @Test
    fun `should get all editable nomenclature types with some property values defined by nomenclature main type`() =
        runTest {
            // given some nomenclature types
            coEvery { nomenclatureRepository.getEditableNomenclatures(EditableNomenclatureType.Type.INFORMATION) } returns Right(
                listOf(
                    EditableNomenclatureType(
                        EditableNomenclatureType.Type.INFORMATION,
                        "METH_OBS",
                        EditableNomenclatureType.ViewType.NOMENCLATURE_TYPE,
                        label = "Méthodes d'observation"
                    ),
                    EditableNomenclatureType(
                        EditableNomenclatureType.Type.INFORMATION,
                        "ETA_BIO",
                        EditableNomenclatureType.ViewType.NOMENCLATURE_TYPE,
                        label = "Etat biologique de l'observation"
                    ),
                    EditableNomenclatureType(
                        EditableNomenclatureType.Type.INFORMATION,
                        "DETERMINER",
                        EditableNomenclatureType.ViewType.TEXT_SIMPLE,
                        visible = true,
                        default = false
                    ),
                    EditableNomenclatureType(
                        EditableNomenclatureType.Type.INFORMATION,
                        "STATUT_BIO",
                        EditableNomenclatureType.ViewType.NOMENCLATURE_TYPE,
                        label = "Statut biologique",
                        visible = false,
                        value = PropertyValue(
                            code = "STATUT_BIO",
                            label = "Non renseigné",
                            value = 29L
                        )
                    )
                )
            )
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
                    PropertyValue(
                        code = "STATUT_BIO",
                        label = "Hibernation",
                        value = 33L
                    ),
                    PropertyValue(
                        "DETERMINER",
                        null,
                        "some_value"
                    )
                )
            )

            // when fetching all editable nomenclature types with values matching given taxonomy
            val response =
                getEditableNomenclaturesUseCase.run(
                    GetEditableNomenclaturesUseCase.Params(
                        type = EditableNomenclatureType.Type.INFORMATION,
                        taxonomy = Taxonomy(
                            kingdom = "Animalia",
                            group = "Oiseaux"
                        )
                    )
                )

            // then
            assertEquals(
                listOf(
                    EditableNomenclatureType(
                        EditableNomenclatureType.Type.INFORMATION,
                        "METH_OBS",
                        EditableNomenclatureType.ViewType.NOMENCLATURE_TYPE,
                        label = "Méthodes d'observation"
                    ),
                    EditableNomenclatureType(
                        EditableNomenclatureType.Type.INFORMATION,
                        "ETA_BIO",
                        EditableNomenclatureType.ViewType.NOMENCLATURE_TYPE,
                        label = "Etat biologique de l'observation"
                    ),
                    EditableNomenclatureType(
                        EditableNomenclatureType.Type.INFORMATION,
                        "DETERMINER",
                        EditableNomenclatureType.ViewType.TEXT_SIMPLE,
                        visible = true,
                        default = false,
                        value = PropertyValue(
                            "DETERMINER",
                            null,
                            "some_value"
                        ),
                        locked = true
                    ),
                    EditableNomenclatureType(
                        EditableNomenclatureType.Type.INFORMATION,
                        "STATUT_BIO",
                        EditableNomenclatureType.ViewType.NOMENCLATURE_TYPE,
                        label = "Statut biologique",
                        visible = false,
                        value = PropertyValue(
                            code = "STATUT_BIO",
                            label = "Hibernation",
                            value = 33L
                        ),
                        locked = true
                    )
                ).sortedBy { it.code },
                response.orNull()?.sortedBy { it.code }
            )
        }

    @Test
    fun `should return NoNomenclatureTypeFoundLocallyFailure if no nomenclature types was found`() =
        runTest {
            // given some failure from repository
            coEvery { nomenclatureRepository.getEditableNomenclatures(EditableNomenclatureType.Type.INFORMATION) } returns Left(NoNomenclatureTypeFoundLocallyFailure)

            // when fetching all editable nomenclature types with default value
            val response =
                getEditableNomenclaturesUseCase.run(GetEditableNomenclaturesUseCase.Params(EditableNomenclatureType.Type.INFORMATION))

            // then
            assertTrue(response.isLeft)
            assertTrue(response.fold(::identity) {} is NoNomenclatureTypeFoundLocallyFailure)
        }
}