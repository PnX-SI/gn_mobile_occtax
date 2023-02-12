package fr.geonature.occtax.features.nomenclature.usecase

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import fr.geonature.commons.data.entity.Taxonomy
import fr.geonature.commons.fp.Either.Left
import fr.geonature.commons.fp.Either.Right
import fr.geonature.commons.fp.identity
import fr.geonature.occtax.CoroutineTestRule
import fr.geonature.occtax.features.nomenclature.error.PropertyValueFailure
import fr.geonature.occtax.features.nomenclature.repository.IDefaultPropertyValueRepository
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
 * Unit tests about [SetDefaultPropertyValueUseCase].
 *
 * @author S. Grimault
 */
@ExperimentalCoroutinesApi
class SetDefaultPropertyValueUseCaseTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    @MockK
    private lateinit var defaultPropertyValueRepository: IDefaultPropertyValueRepository

    private lateinit var setDefaultPropertyValueUseCase: SetDefaultPropertyValueUseCase

    @Before
    fun setUp() {
        init(this)

        setDefaultPropertyValueUseCase =
            SetDefaultPropertyValueUseCase(defaultPropertyValueRepository)
    }

    @Test
    fun `should set new property value`() = runTest {
        coEvery {
            defaultPropertyValueRepository.setPropertyValue(
                Taxonomy(
                    kingdom = "Animalia",
                    group = "Oiseaux"
                ),
                PropertyValue.Nomenclature(
                    code = "STATUT_BIO",
                    label = "Hibernation",
                    value = 33L
                )
            )
        } returns Right(Unit)

        // when setting new property value
        val result = setDefaultPropertyValueUseCase.run(
            SetDefaultPropertyValueUseCase.Params(
                taxonomy = Taxonomy(
                    kingdom = "Animalia",
                    group = "Oiseaux"
                ),
                propertyValue = PropertyValue.Nomenclature(
                    code = "STATUT_BIO",
                    label = "Hibernation",
                    value = 33L
                )
            )
        )

        // then
        assertTrue(result.isRight)
    }

    @Test
    fun `should return PropertyValueFailure if something goes wrong`() = runTest {
        coEvery {
            defaultPropertyValueRepository.setPropertyValue(
                Taxonomy(
                    kingdom = "Animalia",
                    group = "Oiseaux"
                ),
                PropertyValue.Nomenclature(
                    code = "STATUT_BIO",
                    label = "Hibernation",
                    value = 33L
                )
            )
        } returns Left(PropertyValueFailure("STATUT_BIO"))

        // when setting new property value
        val result = setDefaultPropertyValueUseCase.run(
            SetDefaultPropertyValueUseCase.Params(
                taxonomy = Taxonomy(
                    kingdom = "Animalia",
                    group = "Oiseaux"
                ),
                propertyValue = PropertyValue.Nomenclature(
                    code = "STATUT_BIO",
                    label = "Hibernation",
                    value = 33L
                )
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