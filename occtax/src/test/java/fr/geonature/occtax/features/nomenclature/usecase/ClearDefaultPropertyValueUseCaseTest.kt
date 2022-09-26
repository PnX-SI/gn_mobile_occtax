package fr.geonature.occtax.features.nomenclature.usecase

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import fr.geonature.commons.data.entity.Taxonomy
import fr.geonature.commons.fp.Either.Left
import fr.geonature.commons.fp.Either.Right
import fr.geonature.commons.fp.identity
import fr.geonature.occtax.CoroutineTestRule
import fr.geonature.occtax.features.nomenclature.error.PropertyValueFailure
import fr.geonature.occtax.features.nomenclature.repository.IDefaultPropertyValueRepository
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
 * Unit tests about [ClearDefaultPropertyValueUseCase].
 *
 * @author S. Grimault
 */
@ExperimentalCoroutinesApi
class ClearDefaultPropertyValueUseCaseTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    @MockK
    private lateinit var defaultPropertyValueRepository: IDefaultPropertyValueRepository

    private lateinit var clearDefaultPropertyValueUseCase: ClearDefaultPropertyValueUseCase

    @Before
    fun setUp() {
        init(this)

        clearDefaultPropertyValueUseCase =
            ClearDefaultPropertyValueUseCase(defaultPropertyValueRepository)
    }

    @Test
    fun `should clear existing property value`() = runTest {
        coEvery {
            defaultPropertyValueRepository.clearPropertyValue(
                Taxonomy(
                    kingdom = "Animalia",
                    group = "Oiseaux"
                ),
                "STATUT_BIO"
            )
        } returns Right(Unit)

        // when remove existing property value
        val result = clearDefaultPropertyValueUseCase.run(
            ClearDefaultPropertyValueUseCase.Params.Params(
                taxonomy = Taxonomy(
                    kingdom = "Animalia",
                    group = "Oiseaux"
                ),
                "STATUT_BIO"
            )
        )

        // then
        assertTrue(result.isRight)
    }

    @Test
    fun `should clear all property values`() = runTest {
        coEvery {
            defaultPropertyValueRepository.clearAllPropertyValues()
        } returns Right(Unit)

        // when remove existing property value
        val result =
            clearDefaultPropertyValueUseCase.run(ClearDefaultPropertyValueUseCase.Params.None)

        // then
        assertTrue(result.isRight)
    }

    @Test
    fun `should return PropertyValueFailure if something goes wrong`() = runTest {
        coEvery {
            defaultPropertyValueRepository.clearPropertyValue(
                Taxonomy(
                    kingdom = "Animalia",
                    group = "Oiseaux"
                ),
                "STATUT_BIO"
            )
        } returns Left(PropertyValueFailure("STATUT_BIO"))

        // when remove a property value
        val result = clearDefaultPropertyValueUseCase.run(
            ClearDefaultPropertyValueUseCase.Params.Params(
                taxonomy = Taxonomy(
                    kingdom = "Animalia",
                    group = "Oiseaux"
                ),
                "STATUT_BIO"
            )
        )

        // then
        assertTrue(result.isLeft)
        assertEquals(
            result.fold(::identity) {},
            PropertyValueFailure("STATUT_BIO")
        )
    }
}