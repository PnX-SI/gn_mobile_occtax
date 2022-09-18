package fr.geonature.occtax.features.nomenclature.usecase

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import fr.geonature.commons.data.entity.NomenclatureType
import fr.geonature.commons.data.entity.NomenclatureWithType
import fr.geonature.commons.fp.Either.Left
import fr.geonature.commons.fp.Either.Right
import fr.geonature.commons.fp.identity
import fr.geonature.commons.fp.orNull
import fr.geonature.occtax.CoroutineTestRule
import fr.geonature.occtax.features.nomenclature.domain.BaseEditableNomenclatureType
import fr.geonature.occtax.features.nomenclature.domain.EditableNomenclatureType
import fr.geonature.occtax.features.nomenclature.error.NoNomenclatureTypeFoundLocallyFailure
import fr.geonature.occtax.features.nomenclature.repository.INomenclatureRepository
import fr.geonature.occtax.input.PropertyValue
import io.mockk.MockKAnnotations
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

    private lateinit var getEditableNomenclaturesUseCase: GetEditableNomenclaturesUseCase

    @Before
    fun setUp() {
        init(this)

        getEditableNomenclaturesUseCase = GetEditableNomenclaturesUseCase(nomenclatureRepository)
    }

    @Test
    fun `should get all editable nomenclature types with default value by nomenclature main type`() =
        runTest {
            // given some nomenclature types
            coEvery { nomenclatureRepository.getEditableNomenclatures(BaseEditableNomenclatureType.Type.INFORMATION) } returns Right(
                listOf(
                    EditableNomenclatureType(
                        BaseEditableNomenclatureType.Type.INFORMATION,
                        "METH_OBS",
                        BaseEditableNomenclatureType.ViewType.NOMENCLATURE_TYPE,
                        label = "Méthodes d'observation"
                    ),
                    EditableNomenclatureType(
                        BaseEditableNomenclatureType.Type.INFORMATION,
                        "ETA_BIO",
                        BaseEditableNomenclatureType.ViewType.NOMENCLATURE_TYPE,
                        label = "Etat biologique de l'observation"
                    ),
                    EditableNomenclatureType(
                        BaseEditableNomenclatureType.Type.INFORMATION,
                        "STATUT_BIO",
                        BaseEditableNomenclatureType.ViewType.NOMENCLATURE_TYPE,
                        label = "Statut biologique",
                        visible = false
                    )
                )
            )
            // and default nomenclature values
            coEvery { nomenclatureRepository.getAllDefaultNomenclatureValues() } returns Right(
                listOf(
                    NomenclatureWithType(
                        id = 29,
                        code = "1",
                        hierarchy = "013.001",
                        defaultLabel = "Non renseigné",
                        typeId = 13,
                        type = NomenclatureType(
                            id = 13,
                            mnemonic = "STATUT_BIO",
                            defaultLabel = "Statut biologique"
                        ),
                    ),
                )
            )

            // when fetching all editable nomenclature types with default value
            val response =
                getEditableNomenclaturesUseCase.run(GetEditableNomenclaturesUseCase.Params(BaseEditableNomenclatureType.Type.INFORMATION))

            // then
            assertEquals(
                listOf(
                    EditableNomenclatureType(
                        BaseEditableNomenclatureType.Type.INFORMATION,
                        "METH_OBS",
                        BaseEditableNomenclatureType.ViewType.NOMENCLATURE_TYPE,
                        label = "Méthodes d'observation"
                    ),
                    EditableNomenclatureType(
                        BaseEditableNomenclatureType.Type.INFORMATION,
                        "ETA_BIO",
                        BaseEditableNomenclatureType.ViewType.NOMENCLATURE_TYPE,
                        label = "Etat biologique de l'observation"
                    ),
                    EditableNomenclatureType(
                        BaseEditableNomenclatureType.Type.INFORMATION,
                        "STATUT_BIO",
                        BaseEditableNomenclatureType.ViewType.NOMENCLATURE_TYPE,
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
    fun `should return NoNomenclatureTypeFoundLocallyFailure if no nomenclature types was found`() =
        runTest {
            // given some failure from repository
            coEvery { nomenclatureRepository.getEditableNomenclatures(BaseEditableNomenclatureType.Type.INFORMATION) } returns Left(NoNomenclatureTypeFoundLocallyFailure)

            // when fetching all editable nomenclature types with default value
            val response =
                getEditableNomenclaturesUseCase.run(GetEditableNomenclaturesUseCase.Params(BaseEditableNomenclatureType.Type.INFORMATION))

            // then
            assertTrue(response.isLeft)
            assertTrue(response.fold(::identity) {} is NoNomenclatureTypeFoundLocallyFailure)
        }
}