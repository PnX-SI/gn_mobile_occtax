package fr.geonature.occtax.features.nomenclature.presentation

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import androidx.test.core.app.ApplicationProvider
import fr.geonature.commons.data.entity.Nomenclature
import fr.geonature.commons.data.entity.Taxonomy
import fr.geonature.commons.error.Failure
import fr.geonature.commons.features.nomenclature.error.NomenclatureException
import fr.geonature.occtax.CoroutineTestRule
import fr.geonature.occtax.R
import fr.geonature.occtax.features.nomenclature.domain.FormField
import fr.geonature.occtax.features.nomenclature.usecase.GetEditableFieldsUseCase
import fr.geonature.occtax.features.nomenclature.usecase.GetNomenclatureValuesByTypeAndTaxonomyUseCase
import fr.geonature.occtax.features.record.domain.PropertyValue
import io.mockk.MockKAnnotations.init
import io.mockk.coEvery
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [NomenclatureViewModel].
 *
 * @author S. Grimault
 */
@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class NomenclatureViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private lateinit var application: Application

    @RelaxedMockK
    private lateinit var getEditableFieldsUseCase: GetEditableFieldsUseCase

    @MockK
    private lateinit var getNomenclatureValuesByTypeAndTaxonomyUseCase: GetNomenclatureValuesByTypeAndTaxonomyUseCase

    @RelaxedMockK
    private lateinit var editableNomenclaturesObserver: Observer<List<FormField>>

    @RelaxedMockK
    private lateinit var nomenclatureValuesObserver: Observer<List<Nomenclature>>

    @RelaxedMockK
    private lateinit var errorObserver: Observer<Throwable>

    @RelaxedMockK
    private lateinit var failureObserver: Observer<Failure>

    private lateinit var nomenclatureViewModel: NomenclatureViewModel

    @Before
    fun setUp() {
        init(this)

        application = ApplicationProvider.getApplicationContext()

        nomenclatureViewModel = NomenclatureViewModel(
            getEditableFieldsUseCase,
            getNomenclatureValuesByTypeAndTaxonomyUseCase
        )
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `should get all editable nomenclature types with default value by nomenclature main type`() =
        runTest {
            // given some nomenclature types with default values
            val expectedEditableNomenclatures = listOf(
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
                    nomenclatureType = "STATUT_BIO",
                    value = PropertyValue.Nomenclature(
                        code = "STATUT_BIO",
                        label = "Non renseigné",
                        value = 29L
                    )
                )
            )
            coEvery {
                getEditableFieldsUseCase.run(
                    GetEditableFieldsUseCase.Params(
                        type = FormField.Type.INFORMATION,
                    )
                )
            } returns Result.success(expectedEditableNomenclatures)
            coEvery {
                getEditableFieldsUseCase(
                    GetEditableFieldsUseCase.Params(
                        type = FormField.Type.INFORMATION,
                    ),
                    nomenclatureViewModel.viewModelScope,
                    any()
                )
            } answers { callOriginal() }

            // when
            nomenclatureViewModel.getEditableFields(type = FormField.Type.INFORMATION)
            nomenclatureViewModel.editableNomenclatures.observeForever(editableNomenclaturesObserver)
            nomenclatureViewModel.error.observeForever(errorObserver)

            // then
            verify(atLeast = 1) { editableNomenclaturesObserver.onChanged(expectedEditableNomenclatures) }
            confirmVerified(editableNomenclaturesObserver)
        }

    @Test
    fun `should get NoNomenclatureTypeFoundException if no nomenclature types was found`() =
        runTest {
            // given some failure from usecase
            coEvery {
                getEditableFieldsUseCase.run(
                    GetEditableFieldsUseCase.Params(
                        type = FormField.Type.INFORMATION,
                    )
                )
            } returns Result.failure(NomenclatureException.NoNomenclatureTypeFoundException)
            coEvery {
                getEditableFieldsUseCase(
                    GetEditableFieldsUseCase.Params(
                        type = FormField.Type.INFORMATION,
                    ),
                    nomenclatureViewModel.viewModelScope,
                    any()
                )
            } answers { callOriginal() }

            // when
            nomenclatureViewModel.getEditableFields(type = FormField.Type.INFORMATION)
            nomenclatureViewModel.editableNomenclatures.observeForever(editableNomenclaturesObserver)
            nomenclatureViewModel.error.observeForever(errorObserver)

            // then
            verify { errorObserver.onChanged(NomenclatureException.NoNomenclatureTypeFoundException) }
            confirmVerified(errorObserver)
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
            // from usecase
            coEvery {
                getNomenclatureValuesByTypeAndTaxonomyUseCase.run(
                    GetNomenclatureValuesByTypeAndTaxonomyUseCase.Params(
                        mnemonic = "STATUT_BIO",
                        Taxonomy(
                            kingdom = "Animalia",
                            group = "Oiseaux"
                        )
                    )
                )
            } returns Result.success(expectedNomenclatureValues)
            coEvery {
                getNomenclatureValuesByTypeAndTaxonomyUseCase(
                    GetNomenclatureValuesByTypeAndTaxonomyUseCase.Params(
                        mnemonic = "STATUT_BIO",
                        Taxonomy(
                            kingdom = "Animalia",
                            group = "Oiseaux"
                        )
                    ),
                    nomenclatureViewModel.viewModelScope,
                    any()
                )
            } answers { callOriginal() }

            // when
            nomenclatureViewModel.getNomenclatureValuesByTypeAndTaxonomy(
                mnemonic = "STATUT_BIO",
                Taxonomy(
                    kingdom = "Animalia",
                    group = "Oiseaux"
                )
            )
                .observeForever(nomenclatureValuesObserver)
            nomenclatureViewModel.failure.observeForever(failureObserver)

            // then
            verify(atLeast = 1) { nomenclatureValuesObserver.onChanged(expectedNomenclatureValues) }
            confirmVerified(nomenclatureValuesObserver)
        }

    @Test
    fun `should return NoNomenclatureValuesFoundFailure if no nomenclature values was found from given type`() =
        runTest {
            // given some failure from usecase
            coEvery {
                getNomenclatureValuesByTypeAndTaxonomyUseCase.run(
                    GetNomenclatureValuesByTypeAndTaxonomyUseCase.Params(
                        mnemonic = "STATUT_BIO",
                        Taxonomy(
                            kingdom = "Animalia",
                            group = "Oiseaux"
                        )
                    )
                )
            } returns Result.failure(NomenclatureException.NoNomenclatureValuesFoundException("STATUT_BIO"))
            coEvery {
                getNomenclatureValuesByTypeAndTaxonomyUseCase(
                    GetNomenclatureValuesByTypeAndTaxonomyUseCase.Params(
                        mnemonic = "STATUT_BIO",
                        Taxonomy(
                            kingdom = "Animalia",
                            group = "Oiseaux"
                        )
                    ),
                    nomenclatureViewModel.viewModelScope,
                    any()
                )
            } answers { callOriginal() }

            // when
            nomenclatureViewModel.getNomenclatureValuesByTypeAndTaxonomy(
                mnemonic = "STATUT_BIO",
                Taxonomy(
                    kingdom = "Animalia",
                    group = "Oiseaux"
                )
            )
                .observeForever(nomenclatureValuesObserver)
            nomenclatureViewModel.error.observeForever(errorObserver)
            nomenclatureViewModel.failure.observeForever(failureObserver)

            // then
            verify(atLeast = 1) {
                errorObserver.onChanged(NomenclatureException.NoNomenclatureValuesFoundException("STATUT_BIO"))
            }
            confirmVerified(failureObserver)
        }
}