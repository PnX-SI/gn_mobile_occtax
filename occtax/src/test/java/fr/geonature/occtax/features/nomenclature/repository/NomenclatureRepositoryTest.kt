package fr.geonature.occtax.features.nomenclature.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import fr.geonature.commons.data.entity.Nomenclature
import fr.geonature.commons.data.entity.NomenclatureType
import fr.geonature.commons.features.nomenclature.data.INomenclatureLocalDataSource
import fr.geonature.commons.features.nomenclature.error.NomenclatureException
import fr.geonature.occtax.CoroutineTestRule
import fr.geonature.occtax.features.nomenclature.data.INomenclatureSettingsLocalDataSource
import fr.geonature.occtax.features.nomenclature.domain.EditableField
import fr.geonature.occtax.features.record.domain.PropertyValue
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
        // and corresponding editable fields
        coEvery { nomenclatureSettingsLocalDataSource.getNomenclatureTypeSettings(EditableField.Type.INFORMATION) } returns listOf(
            EditableField(
                type = EditableField.Type.INFORMATION,
                code = "METH_OBS",
                viewType = EditableField.ViewType.NOMENCLATURE_TYPE,
                nomenclatureType = "METH_OBS",
                visible = true
            ),
            EditableField(
                type = EditableField.Type.INFORMATION,
                code = "ETA_BIO",
                viewType = EditableField.ViewType.NOMENCLATURE_TYPE,
                nomenclatureType = "ETA_BIO",
                visible = true
            ),
            EditableField(
                type = EditableField.Type.INFORMATION,
                code = "DETERMINER",
                viewType = EditableField.ViewType.TEXT_SIMPLE,
                visible = true,
                default = false
            ),
            EditableField(
                type = EditableField.Type.INFORMATION,
                code = "STATUT_BIO",
                viewType = EditableField.ViewType.NOMENCLATURE_TYPE,
                nomenclatureType = "STATUT_BIO",
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
            nomenclatureRepository.getEditableFields(EditableField.Type.INFORMATION)

        // then
        assertTrue(editableNomenclatureSettings.isSuccess)
        assertEquals(
            listOf(
                EditableField(
                    type = EditableField.Type.INFORMATION,
                    code = "METH_OBS",
                    viewType = EditableField.ViewType.NOMENCLATURE_TYPE,
                    nomenclatureType = "METH_OBS",
                    label = "Méthodes d'observation"
                ),
                EditableField(
                    type = EditableField.Type.INFORMATION,
                    code = "ETA_BIO",
                    viewType = EditableField.ViewType.NOMENCLATURE_TYPE,
                    nomenclatureType = "ETA_BIO",
                    label = "Etat biologique de l'observation"
                ),
                EditableField(
                    type = EditableField.Type.INFORMATION,
                    code = "DETERMINER",
                    viewType = EditableField.ViewType.TEXT_SIMPLE,
                    visible = true,
                    default = false
                ),
                EditableField(
                    type = EditableField.Type.INFORMATION,
                    code = "STATUT_BIO",
                    viewType = EditableField.ViewType.NOMENCLATURE_TYPE,
                    nomenclatureType = "STATUT_BIO",
                    label = "Statut biologique",
                    visible = false,
                    default = false,
                    value = PropertyValue.Nomenclature(
                        code = "STATUT_BIO",
                        label = "Non renseigné",
                        value = 29L
                    )
                )
            ),
            editableNomenclatureSettings.getOrThrow()
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
            // and corresponding editable fields
            coEvery { nomenclatureSettingsLocalDataSource.getNomenclatureTypeSettings(EditableField.Type.INFORMATION) } returns listOf(
                EditableField(
                    type = EditableField.Type.INFORMATION,
                    code = "METH_OBS",
                    viewType = EditableField.ViewType.NOMENCLATURE_TYPE,
                    nomenclatureType = "METH_OBS",
                    visible = true
                ),
                EditableField(
                    type = EditableField.Type.INFORMATION,
                    code = "ETA_BIO",
                    viewType = EditableField.ViewType.NOMENCLATURE_TYPE,
                    nomenclatureType = "ETA_BIO",
                    visible = true
                ),
                EditableField(
                    type = EditableField.Type.INFORMATION,
                    code = "DETERMINER",
                    viewType = EditableField.ViewType.TEXT_SIMPLE,
                    visible = true,
                    default = false
                ),
                EditableField(
                    type = EditableField.Type.INFORMATION,
                    code = "STATUT_BIO",
                    viewType = EditableField.ViewType.NOMENCLATURE_TYPE,
                    nomenclatureType = "STATUT_BIO",
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
                nomenclatureRepository.getEditableFields(EditableField.Type.INFORMATION)

            // then
            assertTrue(editableNomenclatureSettings.isSuccess)
            assertEquals(
                listOf(
                    EditableField(
                        type = EditableField.Type.INFORMATION,
                        code = "METH_OBS",
                        viewType = EditableField.ViewType.NOMENCLATURE_TYPE,
                        nomenclatureType = "METH_OBS",
                        label = "Méthodes d'observation"
                    ),
                    EditableField(
                        type = EditableField.Type.INFORMATION,
                        code = "ETA_BIO",
                        viewType = EditableField.ViewType.NOMENCLATURE_TYPE,
                        nomenclatureType = "ETA_BIO",
                        label = "Etat biologique de l'observation"
                    ),
                    EditableField(
                        type = EditableField.Type.INFORMATION,
                        code = "DETERMINER",
                        viewType = EditableField.ViewType.TEXT_SIMPLE,
                        visible = true,
                        default = false
                    )
                ),
                editableNomenclatureSettings.getOrThrow()
            )
        }

    @Test
    fun `should get other default nomenclature type settings even if no nomenclature types was found`() =
        runTest {
            // given no nomenclature types found
            coEvery { nomenclatureLocalDataSource.getAllNomenclatureTypes() } returns listOf()
            // and some editable fields
            coEvery { nomenclatureSettingsLocalDataSource.getNomenclatureTypeSettings(EditableField.Type.INFORMATION) } returns listOf(
                EditableField(
                    type = EditableField.Type.INFORMATION,
                    code = "METH_OBS",
                    viewType = EditableField.ViewType.NOMENCLATURE_TYPE,
                    nomenclatureType = "METH_OBS"
                ),
                EditableField(
                    type = EditableField.Type.INFORMATION,
                    code = "ETA_BIO",
                    viewType = EditableField.ViewType.NOMENCLATURE_TYPE,
                    nomenclatureType = "ETA_BIO"
                ),
                EditableField(
                    type = EditableField.Type.INFORMATION,
                    code = "DETERMINER",
                    viewType = EditableField.ViewType.TEXT_SIMPLE
                ),
                EditableField(
                    type = EditableField.Type.INFORMATION,
                    code = "STATUT_BIO",
                    viewType = EditableField.ViewType.NOMENCLATURE_TYPE,
                    nomenclatureType = "STATUT_BIO",
                    default = false
                ),
                EditableField(
                    type = EditableField.Type.INFORMATION,
                    code = "COMMENT",
                    viewType = EditableField.ViewType.TEXT_MULTIPLE,
                    visible = true,
                    default = false
                )
            )
            // and no default values for these types
            coEvery { nomenclatureLocalDataSource.getAllDefaultNomenclatureValues() } returns listOf()

            // when
            val editableNomenclatureSettings =
                nomenclatureRepository.getEditableFields(EditableField.Type.INFORMATION)

            // then
            assertTrue(editableNomenclatureSettings.isSuccess)
            assertEquals(
                listOf(
                    EditableField(
                        type = EditableField.Type.INFORMATION,
                        code = "DETERMINER",
                        viewType = EditableField.ViewType.TEXT_SIMPLE
                    ),
                    EditableField(
                        type = EditableField.Type.INFORMATION,
                        code = "COMMENT",
                        viewType = EditableField.ViewType.TEXT_MULTIPLE,
                        visible = true,
                        default = false
                    )
                ),
                editableNomenclatureSettings.getOrThrow()
            )
        }

    @Test
    fun `should return NoNomenclatureTypeFoundLocallyFailure if no nomenclature types was found`() =
        runTest {
            // given no nomenclature types found
            coEvery { nomenclatureLocalDataSource.getAllNomenclatureTypes() } returns listOf()
            // and some editable fields
            coEvery { nomenclatureSettingsLocalDataSource.getNomenclatureTypeSettings(EditableField.Type.INFORMATION) } returns listOf(
                EditableField(
                    type = EditableField.Type.INFORMATION,
                    code = "METH_OBS",
                    viewType = EditableField.ViewType.NOMENCLATURE_TYPE,
                    nomenclatureType = "METH_OBS"
                ),
                EditableField(
                    type = EditableField.Type.INFORMATION,
                    code = "ETA_BIO",
                    viewType = EditableField.ViewType.NOMENCLATURE_TYPE,
                    nomenclatureType = "ETA_BIO"
                ),
                EditableField(
                    type = EditableField.Type.INFORMATION,
                    code = "STATUT_BIO",
                    viewType = EditableField.ViewType.NOMENCLATURE_TYPE,
                    nomenclatureType = "STATUT_BIO",
                    visible = false,
                    default = false
                ),
            )
            // and no default values for these types
            coEvery { nomenclatureLocalDataSource.getAllDefaultNomenclatureValues() } returns listOf()

            // when
            val editableNomenclatureSettings =
                nomenclatureRepository.getEditableFields(EditableField.Type.INFORMATION)

            // then
            assertTrue(editableNomenclatureSettings.isFailure)
            assertTrue(editableNomenclatureSettings.exceptionOrNull() is NomenclatureException.NoNomenclatureTypeFoundException)
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
            // and some other editable fields
            coEvery { nomenclatureSettingsLocalDataSource.getNomenclatureTypeSettings(EditableField.Type.INFORMATION) } returns listOf(
                EditableField(
                    type = EditableField.Type.COUNTING,
                    code = "TYP_DENBR",
                    viewType = EditableField.ViewType.NOMENCLATURE_TYPE,
                    nomenclatureType = "TYP_DENBR"
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
                nomenclatureRepository.getEditableFields(EditableField.Type.INFORMATION)

            // then
            assertTrue(editableNomenclatureSettings.isFailure)
            assertTrue(editableNomenclatureSettings.exceptionOrNull() is NomenclatureException.NoNomenclatureTypeFoundException)
        }
}