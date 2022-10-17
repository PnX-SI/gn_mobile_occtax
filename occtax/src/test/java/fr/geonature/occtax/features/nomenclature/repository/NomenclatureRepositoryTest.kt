package fr.geonature.occtax.features.nomenclature.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import fr.geonature.commons.data.entity.Nomenclature
import fr.geonature.commons.data.entity.NomenclatureType
import fr.geonature.commons.data.entity.Taxonomy
import fr.geonature.commons.fp.identity
import fr.geonature.commons.fp.orNull
import fr.geonature.occtax.CoroutineTestRule
import fr.geonature.occtax.features.input.domain.PropertyValue
import fr.geonature.occtax.features.nomenclature.data.INomenclatureLocalDataSource
import fr.geonature.occtax.features.nomenclature.data.INomenclatureSettingsLocalDataSource
import fr.geonature.occtax.features.nomenclature.domain.EditableNomenclatureType
import fr.geonature.occtax.features.nomenclature.error.NoNomenclatureTypeFoundLocallyFailure
import fr.geonature.occtax.features.nomenclature.error.NoNomenclatureValuesFoundFailure
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
 * Unit tests about [INomenclatureRepository].
 *
 * @author S. Grimault
 */
@ExperimentalCoroutinesApi
class NomenclatureRepositoryTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    @MockK
    private lateinit var nomenclatureLocalDataSource: INomenclatureLocalDataSource

    @MockK
    private lateinit var nomenclatureSettingsLocalDataSource: INomenclatureSettingsLocalDataSource

    private lateinit var nomenclatureRepository: INomenclatureRepository

    @Before
    fun setUp() {
        init(this)

        nomenclatureRepository = NomenclatureRepositoryImpl(
            nomenclatureLocalDataSource,
            nomenclatureSettingsLocalDataSource
        )
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `should get default nomenclature type settings by nomenclature main type`() = runTest {
        // given no nomenclature types found
        coEvery { nomenclatureLocalDataSource.getAllNomenclatureTypes() } returns listOf(
            NomenclatureType(
                id = 7,
                mnemonic = "ETA_BIO",
                defaultLabel = "Etat biologique de l'observation"
            ),
            NomenclatureType(
                id = 13,
                mnemonic = "STATUT_BIO",
                defaultLabel = "Statut biologique"
            ),
            NomenclatureType(
                id = 14,
                mnemonic = "METH_OBS",
                defaultLabel = "Méthodes d'observation"
            )
        )
        // and corresponding editable nomenclature types
        coEvery { nomenclatureSettingsLocalDataSource.getNomenclatureTypeSettings(EditableNomenclatureType.Type.INFORMATION) } returns listOf(
            EditableNomenclatureType(
                EditableNomenclatureType.Type.INFORMATION,
                "METH_OBS",
                EditableNomenclatureType.ViewType.NOMENCLATURE_TYPE,
                true
            ),
            EditableNomenclatureType(

                EditableNomenclatureType.Type.INFORMATION,
                "ETA_BIO",
                EditableNomenclatureType.ViewType.NOMENCLATURE_TYPE,
                true
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
                visible = false,
                default = false
            ),
        )
        // and some default values for these types
        coEvery { nomenclatureLocalDataSource.getAllDefaultNomenclatureValues() } returns listOf(
            Nomenclature(
                id = 29,
                code = "1",
                hierarchy = "013.001",
                defaultLabel = "Non renseigné",
                typeId = 13
            ),
        )

        // when
        val editableNomenclatureSettings =
            nomenclatureRepository.getEditableNomenclatures(EditableNomenclatureType.Type.INFORMATION)

        // then
        assertTrue(editableNomenclatureSettings.isRight)
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
                    default = false
                ),
                EditableNomenclatureType(
                    EditableNomenclatureType.Type.INFORMATION,
                    "STATUT_BIO",
                    EditableNomenclatureType.ViewType.NOMENCLATURE_TYPE,
                    label = "Statut biologique",
                    visible = false,
                    default = false,
                    value = PropertyValue(
                        code = "STATUT_BIO",
                        label = "Non renseigné",
                        value = 29L
                    )
                )
            ),
            editableNomenclatureSettings.orNull()
        )
    }

    @Test
    fun `should get nomenclature type settings with no default nomenclature values if no corresponding nomenclature types was found`() =
        runTest {
            // given no nomenclature types found
            coEvery { nomenclatureLocalDataSource.getAllNomenclatureTypes() } returns listOf(
                NomenclatureType(
                    id = 7,
                    mnemonic = "ETA_BIO",
                    defaultLabel = "Etat biologique de l'observation"
                ),
                NomenclatureType(
                    id = 14,
                    mnemonic = "METH_OBS",
                    defaultLabel = "Méthodes d'observation"
                )
            )
            // and corresponding editable nomenclature types
            coEvery { nomenclatureSettingsLocalDataSource.getNomenclatureTypeSettings(EditableNomenclatureType.Type.INFORMATION) } returns listOf(
                EditableNomenclatureType(
                    EditableNomenclatureType.Type.INFORMATION,
                    "METH_OBS",
                    EditableNomenclatureType.ViewType.NOMENCLATURE_TYPE,
                    true
                ),
                EditableNomenclatureType(

                    EditableNomenclatureType.Type.INFORMATION,
                    "ETA_BIO",
                    EditableNomenclatureType.ViewType.NOMENCLATURE_TYPE,
                    true
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
                    visible = false,
                    default = false
                ),
            )
            // and some default values for these types
            coEvery { nomenclatureLocalDataSource.getAllDefaultNomenclatureValues() } returns listOf(
                Nomenclature(
                    id = 29,
                    code = "1",
                    hierarchy = "013.001",
                    defaultLabel = "Non renseigné",
                    typeId = 13
                ),
            )

            // when
            val editableNomenclatureSettings =
                nomenclatureRepository.getEditableNomenclatures(EditableNomenclatureType.Type.INFORMATION)

            // then
            assertTrue(editableNomenclatureSettings.isRight)
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
                        default = false
                    )
                ),
                editableNomenclatureSettings.orNull()
            )
        }

    @Test
    fun `should get other default nomenclature type settings even if no nomenclature types was found`() =
        runTest {
            // given no nomenclature types found
            coEvery { nomenclatureLocalDataSource.getAllNomenclatureTypes() } returns listOf()
            // and some editable nomenclature types
            coEvery { nomenclatureSettingsLocalDataSource.getNomenclatureTypeSettings(EditableNomenclatureType.Type.INFORMATION) } returns listOf(
                EditableNomenclatureType(
                    EditableNomenclatureType.Type.INFORMATION,
                    "METH_OBS",
                    EditableNomenclatureType.ViewType.NOMENCLATURE_TYPE
                ),
                EditableNomenclatureType(

                    EditableNomenclatureType.Type.INFORMATION,
                    "ETA_BIO",
                    EditableNomenclatureType.ViewType.NOMENCLATURE_TYPE
                ),
                EditableNomenclatureType(
                    EditableNomenclatureType.Type.INFORMATION,
                    "DETERMINER",
                    EditableNomenclatureType.ViewType.TEXT_SIMPLE
                ),
                EditableNomenclatureType(
                    EditableNomenclatureType.Type.INFORMATION,
                    "STATUT_BIO",
                    EditableNomenclatureType.ViewType.NOMENCLATURE_TYPE,
                    default = false
                ),
                EditableNomenclatureType(
                    EditableNomenclatureType.Type.INFORMATION,
                    "COMMENT",
                    EditableNomenclatureType.ViewType.TEXT_MULTIPLE,
                    visible = true,
                    default = false
                )
            )
            // and no default values for these types
            coEvery { nomenclatureLocalDataSource.getAllDefaultNomenclatureValues() } returns listOf()

            // when
            val editableNomenclatureSettings =
                nomenclatureRepository.getEditableNomenclatures(EditableNomenclatureType.Type.INFORMATION)

            // then
            assertTrue(editableNomenclatureSettings.isRight)
            assertEquals(
                listOf(
                    EditableNomenclatureType(
                        EditableNomenclatureType.Type.INFORMATION,
                        "DETERMINER",
                        EditableNomenclatureType.ViewType.TEXT_SIMPLE
                    ),
                    EditableNomenclatureType(
                        EditableNomenclatureType.Type.INFORMATION,
                        "COMMENT",
                        EditableNomenclatureType.ViewType.TEXT_MULTIPLE,
                        visible = true,
                        default = false
                    )
                ),
                editableNomenclatureSettings.orNull()
            )
        }

    @Test
    fun `should return NoNomenclatureTypeFoundLocallyFailure if no nomenclature types was found`() =
        runTest {
            // given no nomenclature types found
            coEvery { nomenclatureLocalDataSource.getAllNomenclatureTypes() } returns listOf()
            // and some editable nomenclature types
            coEvery { nomenclatureSettingsLocalDataSource.getNomenclatureTypeSettings(EditableNomenclatureType.Type.INFORMATION) } returns listOf(
                EditableNomenclatureType(
                    EditableNomenclatureType.Type.INFORMATION,
                    "METH_OBS",
                    EditableNomenclatureType.ViewType.NOMENCLATURE_TYPE
                ),
                EditableNomenclatureType(
                    EditableNomenclatureType.Type.INFORMATION,
                    "ETA_BIO",
                    EditableNomenclatureType.ViewType.NOMENCLATURE_TYPE
                ),
                EditableNomenclatureType(
                    EditableNomenclatureType.Type.INFORMATION,
                    "STATUT_BIO",
                    EditableNomenclatureType.ViewType.NOMENCLATURE_TYPE,
                    visible = false,
                    default = false
                ),
            )
            // and no default values for these types
            coEvery { nomenclatureLocalDataSource.getAllDefaultNomenclatureValues() } returns listOf()

            // when
            val editableNomenclatureSettings =
                nomenclatureRepository.getEditableNomenclatures(EditableNomenclatureType.Type.INFORMATION)

            // then
            assertTrue(editableNomenclatureSettings.isLeft)
            assertTrue(editableNomenclatureSettings.fold(::identity) {} is NoNomenclatureTypeFoundLocallyFailure)
        }

    @Test
    fun `should return NoNomenclatureTypeFoundLocallyFailure if no nomenclature type settings matches nomenclature types`() =
        runTest {
            // given some nomenclature types
            coEvery { nomenclatureLocalDataSource.getAllNomenclatureTypes() } returns listOf(
                NomenclatureType(
                    id = 7,
                    mnemonic = "ETA_BIO",
                    defaultLabel = "Etat biologique de l'observation"
                ),
                NomenclatureType(
                    id = 13,
                    mnemonic = "STATUT_BIO",
                    defaultLabel = "Statut biologique"
                ),
                NomenclatureType(
                    id = 14,
                    mnemonic = "METH_OBS",
                    defaultLabel = "Méthodes d'observation"
                )
            )
            // and some other editable nomenclature types
            coEvery { nomenclatureSettingsLocalDataSource.getNomenclatureTypeSettings(EditableNomenclatureType.Type.INFORMATION) } returns listOf(
                EditableNomenclatureType(
                    EditableNomenclatureType.Type.COUNTING,
                    "TYP_DENBR",
                    EditableNomenclatureType.ViewType.NOMENCLATURE_TYPE
                )
            )
            // and some default values for these types
            coEvery { nomenclatureLocalDataSource.getAllDefaultNomenclatureValues() } returns listOf(
                Nomenclature(
                    id = 29,
                    code = "1",
                    hierarchy = "013.001",
                    defaultLabel = "Non renseigné",
                    typeId = 13
                ),
            )

            // when
            val editableNomenclatureSettings =
                nomenclatureRepository.getEditableNomenclatures(EditableNomenclatureType.Type.INFORMATION)

            // then
            assertTrue(editableNomenclatureSettings.isLeft)
            assertTrue(editableNomenclatureSettings.fold(::identity) {} is NoNomenclatureTypeFoundLocallyFailure)
        }

    @Test
    fun `should get nomenclature values by type matching given taxonomy kingdom and group`() =
        runTest {
            coEvery {
                // given some nomenclature values from given type
                nomenclatureLocalDataSource.getNomenclatureValuesByTypeAndTaxonomy(
                    mnemonic = "STATUT_BIO",
                    Taxonomy(
                        kingdom = "Animalia",
                        group = "Oiseaux"
                    )
                )
            } returns listOf(
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

            // when
            val nomenclatures = nomenclatureRepository.getNomenclatureValuesByTypeAndTaxonomy(
                mnemonic = "STATUT_BIO",
                Taxonomy(
                    kingdom = "Animalia",
                    group = "Oiseaux"
                )
            )

            // then
            assertTrue(nomenclatures.isRight)
            assertEquals(
                listOf(
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
                ),
                nomenclatures.orNull()
            )
        }

    @Test
    fun `should return NoNomenclatureValuesFoundFailure if no nomenclature values was found from given type`() =
        runTest {
            // given no nomenclature values from given type
            coEvery {
                nomenclatureLocalDataSource.getNomenclatureValuesByTypeAndTaxonomy(
                    "STATUT_BIO",
                    Taxonomy(
                        kingdom = "Animalia",
                        group = "Oiseaux"
                    )
                )
            } returns emptyList()

            // when
            val nomenclatures = nomenclatureRepository.getNomenclatureValuesByTypeAndTaxonomy(
                mnemonic = "STATUT_BIO",
                Taxonomy(
                    kingdom = "Animalia",
                    group = "Oiseaux"
                )
            )

            // then
            assertTrue(nomenclatures.isLeft)
            assertEquals(
                nomenclatures.fold(::identity) {},
                NoNomenclatureValuesFoundFailure("STATUT_BIO")
            )
        }
}