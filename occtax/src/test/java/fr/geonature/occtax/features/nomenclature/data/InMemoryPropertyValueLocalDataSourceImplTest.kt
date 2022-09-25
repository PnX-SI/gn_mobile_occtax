package fr.geonature.occtax.features.nomenclature.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import fr.geonature.commons.data.entity.Taxonomy
import fr.geonature.occtax.CoroutineTestRule
import fr.geonature.occtax.input.PropertyValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests about [InMemoryPropertyValueLocalDataSourceImpl].
 *
 * @author S. Grimault
 */
@ExperimentalCoroutinesApi
class InMemoryPropertyValueLocalDataSourceImplTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private lateinit var propertyValueLocalDataSource: IPropertyValueLocalDataSource

    @Before
    fun setUp() {
        propertyValueLocalDataSource = InMemoryPropertyValueLocalDataSourceImpl()
    }

    @Test
    fun `should return an empty list if no property values was set`() = runTest {
        assertTrue(propertyValueLocalDataSource.getPropertyValues().isEmpty())
    }

    @Test
    fun `should return a list of property values added`() = runTest {
        propertyValueLocalDataSource.setPropertyValue(
            Taxonomy(
                kingdom = Taxonomy.ANY,
                group = Taxonomy.ANY
            ),
            PropertyValue(
                code = "STATUT_BIO",
                label = "Non renseigné",
                value = 29L
            )
        )
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
        propertyValueLocalDataSource.setPropertyValue(
            Taxonomy(
                kingdom = "Animalia",
                group = "Oiseaux"
            ),
            PropertyValue(
                code = "DETERMINER",
                label = null,
                value = "some value"
            )
        )

        assertEquals(
            listOf(
                PropertyValue(
                    code = "STATUT_BIO",
                    label = "Non renseigné",
                    value = 29L
                )
            ),
            propertyValueLocalDataSource.getPropertyValues()
        )
        assertEquals(
            listOf(
                PropertyValue(
                    code = "STATUT_BIO",
                    label = "Hibernation",
                    value = 33L
                ),
                PropertyValue(
                    code = "DETERMINER",
                    label = null,
                    value = "some value"
                )
            ),
            propertyValueLocalDataSource.getPropertyValues(
                Taxonomy(
                    kingdom = "Animalia",
                    group = "Oiseaux"
                )
            )
        )
    }

    @Test
    fun `should remove a non existing property value`() = runTest {
        propertyValueLocalDataSource.setPropertyValue(
            Taxonomy(
                kingdom = Taxonomy.ANY,
                group = Taxonomy.ANY
            ),
            PropertyValue(
                code = "STATUT_BIO",
                label = "Non renseigné",
                value = 29L
            )
        )
        propertyValueLocalDataSource.clearPropertyValue(
            Taxonomy(
                kingdom = Taxonomy.ANY,
                group = Taxonomy.ANY
            ),
            "DETERMINER"
        )
        propertyValueLocalDataSource.clearPropertyValue(
            Taxonomy(
                kingdom = "Animalia",
                group = "Oiseaux"
            ),
            "STATUT_BIO"
        )

        assertEquals(
            listOf(
                PropertyValue(
                    code = "STATUT_BIO",
                    label = "Non renseigné",
                    value = 29L
                )
            ),
            propertyValueLocalDataSource.getPropertyValues()
        )
    }

    @Test
    fun `should remove an existing property value`() = runTest {
        propertyValueLocalDataSource.setPropertyValue(
            Taxonomy(
                kingdom = Taxonomy.ANY,
                group = Taxonomy.ANY
            ),
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
        propertyValueLocalDataSource.clearPropertyValue(
            Taxonomy(
                kingdom = Taxonomy.ANY,
                group = Taxonomy.ANY
            ),
            "DETERMINER"
        )

        assertEquals(
            listOf(
                PropertyValue(
                    code = "STATUT_BIO",
                    label = "Non renseigné",
                    value = 29L
                )
            ),
            propertyValueLocalDataSource.getPropertyValues()
        )
    }

    @Test
    fun `should clear all property values`() = runTest {
        propertyValueLocalDataSource.setPropertyValue(
            Taxonomy(
                kingdom = Taxonomy.ANY,
                group = Taxonomy.ANY
            ),
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
        propertyValueLocalDataSource.clearAllPropertyValues()

        assertTrue(propertyValueLocalDataSource.getPropertyValues().isEmpty())
    }
}