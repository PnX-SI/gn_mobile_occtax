package fr.geonature.occtax.features.nomenclature.repository

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import fr.geonature.commons.data.entity.Nomenclature
import fr.geonature.commons.data.entity.NomenclatureType
import fr.geonature.commons.features.nomenclature.data.INomenclatureLocalDataSource
import fr.geonature.commons.features.nomenclature.error.NomenclatureException
import fr.geonature.occtax.CoroutineTestRule
import fr.geonature.occtax.R
import fr.geonature.occtax.features.nomenclature.data.INomenclatureSettingsLocalDataSource
import fr.geonature.occtax.features.nomenclature.domain.FormField
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
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [INomenclatureRepository].
 *
 * @author S. Grimault
 */
@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class NomenclatureRepositoryTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private lateinit var application: Application

    @MockK
    private lateinit var nomenclatureLocalDataSource: INomenclatureLocalDataSource

    @MockK
    private lateinit var nomenclatureSettingsLocalDataSource: INomenclatureSettingsLocalDataSource

    private lateinit var nomenclatureRepository: INomenclatureRepository

    @Before
    fun setUp() {
        init(this)

        application = ApplicationProvider.getApplicationContext()

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
        // and corresponding form fields
        coEvery { nomenclatureSettingsLocalDataSource.getNomenclatureTypeSettings(FormField.Type.INFORMATION) } returns listOf(
            FormField.NomenclatureType(
                type = FormField.Type.INFORMATION,
                label = application.getString(R.string.nomenclature_meth_obs),
                visible = true,
                nomenclatureType = "METH_OBS",
                value = PropertyValue.Nomenclature(code = "METH_OBS")
            ),
            FormField.NomenclatureType(
                type = FormField.Type.INFORMATION,
                label = application.getString(R.string.nomenclature_eta_bio),
                visible = true,
                nomenclatureType = "ETA_BIO",
                value = PropertyValue.Nomenclature(code = "ETA_BIO")
            ),
            FormField.Text(
                type = FormField.Type.INFORMATION,
                label = application.getString(R.string.nomenclature_determiner),
                default = false,
                visible = true,
                value = PropertyValue.Text(code = "determiner")
            ),
            FormField.NomenclatureType(
                type = FormField.Type.INFORMATION,
                label = application.getString(R.string.nomenclature_statut_bio),
                default = false,
                visible = false,
                nomenclatureType = "STATUT_BIO",
                value = PropertyValue.Nomenclature(code = "STATUT_BIO")
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
            nomenclatureRepository.getEditableFields(FormField.Type.INFORMATION)

        // then
        assertTrue(editableNomenclatureSettings.isSuccess)
        assertEquals(
            listOf(
                FormField.NomenclatureType(
                    type = FormField.Type.INFORMATION,
                    label = "Méthodes d'observation",
                    nomenclatureType = "METH_OBS",
                    value = PropertyValue.Nomenclature(code = "METH_OBS")
                ),
                FormField.NomenclatureType(
                    type = FormField.Type.INFORMATION,
                    label = "Etat biologique de l'observation",
                    nomenclatureType = "ETA_BIO",
                    value = PropertyValue.Nomenclature(code = "ETA_BIO")
                ),
                FormField.Text(
                    type = FormField.Type.INFORMATION,
                    label = application.getString(R.string.nomenclature_determiner),
                    default = false,
                    visible = true,
                    value = PropertyValue.Text(code = "determiner")
                ),
                FormField.NomenclatureType(
                    type = FormField.Type.INFORMATION,
                    label = "Statut biologique",
                    default = false,
                    visible = false,
                    nomenclatureType = "STATUT_BIO",
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
            // and corresponding form fields
            coEvery { nomenclatureSettingsLocalDataSource.getNomenclatureTypeSettings(FormField.Type.INFORMATION) } returns listOf(
                FormField.NomenclatureType(
                    type = FormField.Type.INFORMATION,
                    label = application.getString(R.string.nomenclature_meth_obs),
                    visible = true,
                    nomenclatureType = "METH_OBS",
                    value = PropertyValue.Nomenclature(code = "METH_OBS")
                ),
                FormField.NomenclatureType(
                    type = FormField.Type.INFORMATION,
                    label = application.getString(R.string.nomenclature_eta_bio),
                    visible = true,
                    nomenclatureType = "ETA_BIO",
                    value = PropertyValue.Nomenclature(code = "ETA_BIO")
                ),
                FormField.Text(
                    type = FormField.Type.INFORMATION,
                    label = application.getString(R.string.nomenclature_determiner),
                    default = false,
                    visible = true,
                    value = PropertyValue.Text(code = "determiner")
                ),
                FormField.NomenclatureType(
                    type = FormField.Type.INFORMATION,
                    label = application.getString(R.string.nomenclature_statut_bio),
                    default = false,
                    visible = false,
                    nomenclatureType = "STATUT_BIO",
                    value = PropertyValue.Nomenclature(code = "STATUT_BIO")
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
                nomenclatureRepository.getEditableFields(FormField.Type.INFORMATION)

            // then
            assertTrue(editableNomenclatureSettings.isSuccess)
            assertEquals(
                listOf(
                    FormField.NomenclatureType(
                        type = FormField.Type.INFORMATION,
                        label = "Méthodes d'observation",
                        nomenclatureType = "METH_OBS",
                        value = PropertyValue.Nomenclature(code = "METH_OBS")
                    ),
                    FormField.NomenclatureType(
                        type = FormField.Type.INFORMATION,
                        label = "Etat biologique de l'observation",
                        nomenclatureType = "ETA_BIO",
                        value = PropertyValue.Nomenclature(code = "ETA_BIO")
                    ),
                    FormField.Text(
                        type = FormField.Type.INFORMATION,
                        label = application.getString(R.string.nomenclature_determiner),
                        default = false,
                        visible = true,
                        value = PropertyValue.Text(code = "determiner")
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
            // and some form fields
            coEvery { nomenclatureSettingsLocalDataSource.getNomenclatureTypeSettings(FormField.Type.INFORMATION) } returns listOf(
                FormField.NomenclatureType(
                    type = FormField.Type.INFORMATION,
                    label = application.getString(R.string.nomenclature_meth_obs),
                    nomenclatureType = "METH_OBS",
                    value = PropertyValue.Nomenclature(code = "METH_OBS")
                ),
                FormField.NomenclatureType(
                    type = FormField.Type.INFORMATION,
                    label = application.getString(R.string.nomenclature_eta_bio),
                    nomenclatureType = "ETA_BIO",
                    value = PropertyValue.Nomenclature(code = "ETA_BIO")
                ),
                FormField.Text(
                    type = FormField.Type.INFORMATION,
                    label = application.getString(R.string.nomenclature_determiner),
                    value = PropertyValue.Text(code = "determiner")
                ),
                FormField.NomenclatureType(
                    type = FormField.Type.INFORMATION,
                    label = application.getString(R.string.nomenclature_statut_bio),
                    default = false,
                    nomenclatureType = "STATUT_BIO",
                    value = PropertyValue.Nomenclature(code = "STATUT_BIO")
                ),
                FormField.TextMultiple(
                    type = FormField.Type.INFORMATION,
                    label = application.getString(R.string.nomenclature_comment),
                    default = false,
                    visible = true,
                    value = PropertyValue.Text(code = "comment")
                )
            )
            // and no default values for these types
            coEvery { nomenclatureLocalDataSource.getAllDefaultNomenclatureValues() } returns listOf()

            // when
            val editableNomenclatureSettings =
                nomenclatureRepository.getEditableFields(FormField.Type.INFORMATION)

            // then
            assertTrue(editableNomenclatureSettings.isSuccess)
            assertEquals(
                listOf(
                    FormField.Text(
                        type = FormField.Type.INFORMATION,
                        label = application.getString(R.string.nomenclature_determiner),
                        value = PropertyValue.Text(code = "determiner")
                    ),
                    FormField.TextMultiple(
                        type = FormField.Type.INFORMATION,
                        label = application.getString(R.string.nomenclature_comment),
                        default = false,
                        visible = true,
                        value = PropertyValue.Text(code = "comment")
                    )
                ),
                editableNomenclatureSettings.getOrThrow()
            )
        }

    @Test
    fun `should return NoNomenclatureTypeFoundException failure if no nomenclature types was found`() =
        runTest {
            // given no nomenclature types found
            coEvery { nomenclatureLocalDataSource.getAllNomenclatureTypes() } returns listOf()
            // and some form fields
            coEvery { nomenclatureSettingsLocalDataSource.getNomenclatureTypeSettings(FormField.Type.INFORMATION) } returns listOf(
                FormField.NomenclatureType(
                    type = FormField.Type.INFORMATION,
                    label = application.getString(R.string.nomenclature_meth_obs),
                    nomenclatureType = "METH_OBS",
                    value = PropertyValue.Nomenclature(code = "METH_OBS")
                ),
                FormField.NomenclatureType(
                    type = FormField.Type.INFORMATION,
                    label = application.getString(R.string.nomenclature_eta_bio),
                    nomenclatureType = "ETA_BIO",
                    value = PropertyValue.Nomenclature(code = "ETA_BIO")
                ),
                FormField.NomenclatureType(
                    type = FormField.Type.INFORMATION,
                    label = application.getString(R.string.nomenclature_statut_bio),
                    default = false,
                    visible = false,
                    nomenclatureType = "STATUT_BIO",
                    value = PropertyValue.Nomenclature(code = "STATUT_BIO")
                )
            )
            // and no default values for these types
            coEvery { nomenclatureLocalDataSource.getAllDefaultNomenclatureValues() } returns listOf()

            // when
            val editableNomenclatureSettings =
                nomenclatureRepository.getEditableFields(FormField.Type.INFORMATION)

            // then
            assertTrue(editableNomenclatureSettings.isFailure)
            assertTrue(editableNomenclatureSettings.exceptionOrNull() is NomenclatureException.NoNomenclatureTypeFoundException)
        }

    @Test
    fun `should return NoNomenclatureTypeFoundException failure if no nomenclature type settings matches nomenclature types`() =
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
            // and some other form fields
            coEvery { nomenclatureSettingsLocalDataSource.getNomenclatureTypeSettings(FormField.Type.INFORMATION) } returns listOf(
                FormField.NomenclatureType(
                    type = FormField.Type.COUNTING,
                    label = application.getString(R.string.nomenclature_typ_denbr),
                    nomenclatureType = "TYP_DENBR",
                    value = PropertyValue.Nomenclature(code = "TYP_DENBR")
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
                nomenclatureRepository.getEditableFields(FormField.Type.INFORMATION)

            // then
            assertTrue(editableNomenclatureSettings.isFailure)
            assertTrue(editableNomenclatureSettings.exceptionOrNull() is NomenclatureException.NoNomenclatureTypeFoundException)
        }
}