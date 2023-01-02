package fr.geonature.occtax.features.nomenclature.usecase

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import fr.geonature.commons.data.entity.Nomenclature
import fr.geonature.commons.data.entity.Taxonomy
import fr.geonature.commons.features.nomenclature.error.NomenclatureException
import fr.geonature.occtax.CoroutineTestRule
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
 * Unit tests about [GetNomenclatureValuesByTypeAndTaxonomyUseCase].
 *
 * @author S. Grimault
 */
@ExperimentalCoroutinesApi
class GetNomenclatureValuesByTypeAndTaxonomyUseCaseTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    @MockK
    private lateinit var nomenclatureRepository: INomenclatureRepository

    private lateinit var getNomenclatureValuesByTypeAndTaxonomyUseCase: GetNomenclatureValuesByTypeAndTaxonomyUseCase

    @Before
    fun setUp() {
        init(this)

        getNomenclatureValuesByTypeAndTaxonomyUseCase =
            GetNomenclatureValuesByTypeAndTaxonomyUseCase(nomenclatureRepository)
    }

    @Test
    fun `should get nomenclature values by type matching given taxonomy kingdom and group`() =
        runTest {
            // given some nomenclature values from given type
            val expectedNomenclatureValues = listOf(
                Nomenclature(
                    id = 29,
                    code = "1",
                    hierarchy = "013.001",
                    defaultLabel = "Non renseigné",
                    typeId = 13
                ),
                Nomenclature(
                    id = 31,
                    code = "3",
                    hierarchy = "013.003",
                    defaultLabel = "Reproduction",
                    typeId = 13
                ),
                Nomenclature(
                    id = 32,
                    code = "4",
                    hierarchy = "013.004",
                    defaultLabel = "Hibernation",
                    typeId = 13
                )
            )
            coEvery {
                nomenclatureRepository.getNomenclatureValuesByTypeAndTaxonomy(
                    mnemonic = "STATUT_BIO",
                    Taxonomy(
                        kingdom = "Animalia",
                        group = "Oiseaux"
                    )
                )
            } returns Result.success(expectedNomenclatureValues)

            // when getting all nomenclature values
            val response = getNomenclatureValuesByTypeAndTaxonomyUseCase.run(
                GetNomenclatureValuesByTypeAndTaxonomyUseCase.Params(
                    mnemonic = "STATUT_BIO",
                    Taxonomy(
                        kingdom = "Animalia",
                        group = "Oiseaux"
                    )
                )
            )

            // then
            assertEquals(
                expectedNomenclatureValues,
                response.getOrNull()
            )
        }

    @Test
    fun `should return NoNomenclatureValuesFoundFailure if no nomenclature values was found from given type`() =
        runTest {
            // given some failure from repository
            coEvery {
                nomenclatureRepository.getNomenclatureValuesByTypeAndTaxonomy(
                    mnemonic = "STATUT_BIO",
                    Taxonomy(
                        kingdom = "Animalia",
                        group = "Oiseaux"
                    )
                )
            } answers { Result.failure(NomenclatureException.NoNomenclatureValuesFoundException(firstArg())) }

            // when getting all nomenclature values
            val result = getNomenclatureValuesByTypeAndTaxonomyUseCase.run(
                GetNomenclatureValuesByTypeAndTaxonomyUseCase.Params(
                    mnemonic = "STATUT_BIO",
                    Taxonomy(
                        kingdom = "Animalia",
                        group = "Oiseaux"
                    )
                )
            )

            // then
            assertTrue(result.isFailure)
            assertEquals(
                NomenclatureException.NoNomenclatureValuesFoundException("STATUT_BIO"),
                result.exceptionOrNull()
            )
        }
}