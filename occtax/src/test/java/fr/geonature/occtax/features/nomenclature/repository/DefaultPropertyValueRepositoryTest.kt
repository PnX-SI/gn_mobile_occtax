package fr.geonature.occtax.features.nomenclature.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import fr.geonature.commons.data.entity.Nomenclature
import fr.geonature.commons.data.entity.Taxonomy
import fr.geonature.commons.fp.orNull
import fr.geonature.occtax.CoroutineTestRule
import fr.geonature.occtax.features.nomenclature.data.INomenclatureLocalDataSource
import fr.geonature.occtax.features.nomenclature.data.IPropertyValueLocalDataSource
import fr.geonature.occtax.features.input.domain.PropertyValue
import io.mockk.MockKAnnotations.init
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
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
 * Unit tests about [IDefaultPropertyValueRepository].
 *
 * @author S. Grimault
 */
@ExperimentalCoroutinesApi
class DefaultPropertyValueRepositoryTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    @MockK
    private lateinit var propertyValueLocalDataSource: IPropertyValueLocalDataSource

    @MockK
    private lateinit var nomenclatureLocalDataSource: INomenclatureLocalDataSource

    private lateinit var defaultPropertyValueRepository: IDefaultPropertyValueRepository

    @Before
    fun setUp() {
        init(this)

        defaultPropertyValueRepository =
            DefaultPropertyValueRepositoryImpl(
                propertyValueLocalDataSource,
                nomenclatureLocalDataSource
            )
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `should return an empty list if no property values was set`() = runTest {
        // given no property values added
        coEvery { propertyValueLocalDataSource.getPropertyValues() } returns listOf()

        // when
        val result = defaultPropertyValueRepository.getPropertyValues()

        // then
        assertTrue(result.isRight)
        assertTrue(result.orNull()?.isEmpty() == true)
    }

    @Test
    fun `should return a list of property values added`() = runTest {
        // given some property values added
        val expectedPropertyValues = listOf(
            PropertyValue(
                code = "STATUT_BIO",
                label = "Non renseigné",
                value = 29L
            ),
            PropertyValue(
                "DETERMINER",
                null,
                "some_value"
            )
        )
        coEvery { propertyValueLocalDataSource.getPropertyValues() } returns expectedPropertyValues
        coEvery { nomenclatureLocalDataSource.getNomenclatureValuesByTypeAndTaxonomy("STATUT_BIO") } returns listOf()
        coEvery { nomenclatureLocalDataSource.getNomenclatureValuesByTypeAndTaxonomy("DETERMINER") } returns listOf()

        // when
        val result = defaultPropertyValueRepository.getPropertyValues()

        // then
        assertTrue(result.isRight)
        assertEquals(
            expectedPropertyValues,
            result.orNull()
        )
    }

    @Test
    fun `should return a list of property values added matching given taxonomy rank`() = runTest {
        // given some property values added
        val expectedPropertyValues = listOf(
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
        coEvery {
            propertyValueLocalDataSource.getPropertyValues(
                Taxonomy(
                    kingdom = "Animalia",
                    group = "Oiseaux"
                )
            )
        } returns expectedPropertyValues
        coEvery {
            nomenclatureLocalDataSource.getNomenclatureValuesByTypeAndTaxonomy(
                "STATUT_BIO",
                Taxonomy(
                    kingdom = "Animalia",
                    group = "Oiseaux"
                )
            )
        } returns listOf(
            Nomenclature(
                id = 33,
                code = "4",
                hierarchy = "013.004",
                defaultLabel = "Hibernation",
                typeId = 13
            ),
            Nomenclature(
                id = 34,
                code = "5",
                hierarchy = "013.005",
                defaultLabel = "Estivation",
                typeId = 13
            )
        )
        coEvery {
            nomenclatureLocalDataSource.getNomenclatureValuesByTypeAndTaxonomy(
                "DETERMINER",
                Taxonomy(
                    kingdom = "Animalia",
                    group = "Oiseaux"
                )
            )
        } returns listOf()

        // when
        val result = defaultPropertyValueRepository.getPropertyValues(
            Taxonomy(
                kingdom = "Animalia",
                group = "Oiseaux"
            )
        )

        // then
        assertTrue(result.isRight)
        assertEquals(
            expectedPropertyValues,
            result.orNull()
        )
    }

    @Test
    fun `should set new property value`() = runTest {
        coEvery {
            propertyValueLocalDataSource.setPropertyValue(
                any(),
                any()
            )
        } returns Unit

        // when
        val result = defaultPropertyValueRepository.setPropertyValue(
            Taxonomy(
                kingdom = "Animalia",
                group = "Oiseaux"
            ),
            PropertyValue(
                code = "STATUT_BIO",
                label = "Hibernation",
                value = 33L
            )
        )

        // then
        assertTrue(result.isRight)
        coVerify {
            propertyValueLocalDataSource.setPropertyValue(
                Taxonomy(
                    kingdom = "Animalia",
                    group = "Oiseaux"
                ),
                PropertyValue(
                    code = "STATUT_BIO",
                    label = "Hibernation",
                    value = 33L
                )
            )
        }
        confirmVerified(propertyValueLocalDataSource)
    }

    @Test
    fun `should clear existing property value`() = runTest {
        coEvery {
            propertyValueLocalDataSource.clearPropertyValue(
                any(),
                any()
            )
        } returns Unit

        // when
        val result = defaultPropertyValueRepository.clearPropertyValue(
            Taxonomy(
                kingdom = "Animalia",
                group = "Oiseaux"
            ),
            "STATUT_BIO"
        )

        // then
        assertTrue(result.isRight)
        coVerify {
            propertyValueLocalDataSource.clearPropertyValue(
                Taxonomy(
                    kingdom = "Animalia",
                    group = "Oiseaux"
                ),
                "STATUT_BIO"
            )
        }
        confirmVerified(propertyValueLocalDataSource)
    }
}