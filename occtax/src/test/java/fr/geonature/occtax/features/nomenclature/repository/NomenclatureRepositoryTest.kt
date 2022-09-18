package fr.geonature.occtax.features.nomenclature.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import fr.geonature.commons.data.entity.Nomenclature
import fr.geonature.commons.data.entity.NomenclatureType
import fr.geonature.commons.data.entity.NomenclatureWithType
import fr.geonature.commons.data.entity.Taxonomy
import fr.geonature.commons.fp.identity
import fr.geonature.commons.fp.orNull
import fr.geonature.occtax.CoroutineTestRule
import fr.geonature.occtax.features.nomenclature.data.INomenclatureLocalDataSource
import fr.geonature.occtax.features.nomenclature.data.INomenclatureSettingsLocalDataSource
import fr.geonature.occtax.features.nomenclature.domain.BaseEditableNomenclatureType
import fr.geonature.occtax.features.nomenclature.domain.EditableNomenclatureType
import fr.geonature.occtax.features.nomenclature.error.NoNomenclatureTypeFoundLocallyFailure
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
        coEvery { nomenclatureSettingsLocalDataSource.getNomenclatureTypeSettings(BaseEditableNomenclatureType.Type.INFORMATION) } returns listOf(
            BaseEditableNomenclatureType.from(
                BaseEditableNomenclatureType.Type.INFORMATION,
                "METH_OBS",
                BaseEditableNomenclatureType.ViewType.NOMENCLATURE_TYPE,
                true
            ),
            BaseEditableNomenclatureType.from(

                BaseEditableNomenclatureType.Type.INFORMATION,
                "ETA_BIO",
                BaseEditableNomenclatureType.ViewType.NOMENCLATURE_TYPE,
                true
            ),
            BaseEditableNomenclatureType.from(
                BaseEditableNomenclatureType.Type.INFORMATION,
                "STATUT_BIO",
                BaseEditableNomenclatureType.ViewType.NOMENCLATURE_TYPE,
                false
            ),
        )

        val editableNomenclatureSettings =
            nomenclatureRepository.getEditableNomenclatures(BaseEditableNomenclatureType.Type.INFORMATION)

        assertTrue(editableNomenclatureSettings.isRight)
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
                    visible = false
                )
            ).sortedBy { it.code },
            editableNomenclatureSettings.orNull()?.sortedBy { it.code }
        )
    }

    @Test
    fun `should return NoNomenclatureTypeFoundLocallyFailure if no nomenclature types was found`() =
        runTest {
            coEvery { nomenclatureLocalDataSource.getAllNomenclatureTypes() } returns listOf()
            coEvery { nomenclatureSettingsLocalDataSource.getNomenclatureTypeSettings(BaseEditableNomenclatureType.Type.INFORMATION) } returns listOf(
                BaseEditableNomenclatureType.from(
                    BaseEditableNomenclatureType.Type.INFORMATION,
                    "METH_OBS",
                    BaseEditableNomenclatureType.ViewType.NOMENCLATURE_TYPE,
                    true
                ),
                BaseEditableNomenclatureType.from(

                    BaseEditableNomenclatureType.Type.INFORMATION,
                    "ETA_BIO",
                    BaseEditableNomenclatureType.ViewType.NOMENCLATURE_TYPE,
                    true
                ),
                BaseEditableNomenclatureType.from(
                    BaseEditableNomenclatureType.Type.INFORMATION,
                    "STATUT_BIO",
                    BaseEditableNomenclatureType.ViewType.NOMENCLATURE_TYPE,
                    false
                ),
            )

            val editableNomenclatureSettings =
                nomenclatureRepository.getEditableNomenclatures(BaseEditableNomenclatureType.Type.INFORMATION)

            assertTrue(editableNomenclatureSettings.isLeft)
            assertTrue(editableNomenclatureSettings.fold(::identity) {} is NoNomenclatureTypeFoundLocallyFailure)
        }

    @Test
    fun `should return NoNomenclatureTypeFoundLocallyFailure if no nomenclature type settings matches nomenclature types`() =
        runTest {
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
            coEvery { nomenclatureSettingsLocalDataSource.getNomenclatureTypeSettings(BaseEditableNomenclatureType.Type.INFORMATION) } returns listOf(
                BaseEditableNomenclatureType.from(
                    BaseEditableNomenclatureType.Type.COUNTING,
                    "TYP_DENBR",
                    BaseEditableNomenclatureType.ViewType.NOMENCLATURE_TYPE
                ),
                BaseEditableNomenclatureType.from(
                    BaseEditableNomenclatureType.Type.COUNTING,
                    "MIN",
                    BaseEditableNomenclatureType.ViewType.MIN_MAX
                ),
                BaseEditableNomenclatureType.from(
                    BaseEditableNomenclatureType.Type.COUNTING,
                    "MAX",
                    BaseEditableNomenclatureType.ViewType.MIN_MAX
                )
            )

            val editableNomenclatureSettings =
                nomenclatureRepository.getEditableNomenclatures(BaseEditableNomenclatureType.Type.INFORMATION)

            assertTrue(editableNomenclatureSettings.isLeft)
            assertTrue(editableNomenclatureSettings.fold(::identity) {} is NoNomenclatureTypeFoundLocallyFailure)
        }

    @Test
    fun `should get all default nomenclature values with type`() = runTest {
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
        coEvery { nomenclatureLocalDataSource.getAllDefaultNomenclatureValues() } returns listOf(
            Nomenclature(
                id = 29,
                code = "1",
                hierarchy = "013.001",
                defaultLabel = "Non renseigné",
                typeId = 13
            ),
        )

        val defaultNomenclatureValues = nomenclatureRepository.getAllDefaultNomenclatureValues()

        assertTrue(defaultNomenclatureValues.isRight)
        assertEquals(
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
            ),
            defaultNomenclatureValues.orNull()
        )
    }

    @Test
    fun `should return an empty list if no nomenclature types was found`() = runTest {
        coEvery { nomenclatureLocalDataSource.getAllNomenclatureTypes() } returns listOf()
        coEvery { nomenclatureLocalDataSource.getAllDefaultNomenclatureValues() } returns listOf(
            Nomenclature(
                id = 29,
                code = "1",
                hierarchy = "013.001",
                defaultLabel = "Non renseigné",
                typeId = 13
            ),
        )

        val defaultNomenclatureValues = nomenclatureRepository.getAllDefaultNomenclatureValues()

        assertTrue(defaultNomenclatureValues.isRight)
        assertTrue(defaultNomenclatureValues.orNull()?.isEmpty() ?: false)
    }

    @Test
    fun `should return an empty list if no default nomenclature values was found`() = runTest {
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
        coEvery { nomenclatureLocalDataSource.getAllDefaultNomenclatureValues() } returns listOf()

        val defaultNomenclatureValues = nomenclatureRepository.getAllDefaultNomenclatureValues()

        assertTrue(defaultNomenclatureValues.isRight)
        assertTrue(defaultNomenclatureValues.orNull()?.isEmpty() ?: false)
    }

    @Test
    fun `should get nomenclatures by type matching given taxonomy kingdom and group`() = runTest {
        coEvery {
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

        val nomenclatures = nomenclatureRepository.getNomenclatureValuesByTypeAndTaxonomy(
            mnemonic = "STATUT_BIO",
            Taxonomy(
                kingdom = "Animalia",
                group = "Oiseaux"
            )
        )

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
}