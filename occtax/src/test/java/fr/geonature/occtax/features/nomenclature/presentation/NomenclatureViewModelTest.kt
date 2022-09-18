package fr.geonature.occtax.features.nomenclature.presentation

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import fr.geonature.commons.error.Failure
import fr.geonature.commons.fp.Either
import fr.geonature.commons.fp.Either.Left
import fr.geonature.commons.fp.Either.Right
import fr.geonature.datasync.settings.DataSyncSettings
import fr.geonature.datasync.settings.DataSyncSettingsViewModel
import fr.geonature.occtax.CoroutineTestRule
import fr.geonature.occtax.features.nomenclature.domain.BaseEditableNomenclatureType
import fr.geonature.occtax.features.nomenclature.domain.EditableNomenclatureType
import fr.geonature.occtax.features.nomenclature.error.NoNomenclatureTypeFoundLocallyFailure
import fr.geonature.occtax.features.nomenclature.repository.INomenclatureRepository
import fr.geonature.occtax.features.nomenclature.usecase.GetEditableNomenclaturesUseCase
import fr.geonature.occtax.features.nomenclature.usecase.GetNomenclatureValuesByTypeAndTaxonomyUseCase
import fr.geonature.occtax.input.PropertyValue
import io.mockk.Call
import io.mockk.Called
import io.mockk.MockKAnnotations
import io.mockk.MockKAnnotations.init
import io.mockk.OfTypeMatcher
import io.mockk.coEvery
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import io.mockk.verifyOrder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.core.AnyOf
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests about [NomenclatureViewModel].
 *
 * @author S. Grimault
 */
@ExperimentalCoroutinesApi
class NomenclatureViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    @RelaxedMockK
    private lateinit var getEditableNomenclaturesUseCase: GetEditableNomenclaturesUseCase

    @MockK
    private lateinit var getNomenclatureValuesByTypeAndTaxonomyUseCase: GetNomenclatureValuesByTypeAndTaxonomyUseCase

    @RelaxedMockK
    private lateinit var editableNomenclaturesObserver: Observer<List<EditableNomenclatureType>>

    @RelaxedMockK
    private lateinit var failureObserver: Observer<Failure>

    private lateinit var nomenclatureViewModel: NomenclatureViewModel

    @Before
    fun setUp() {
        init(this)

        nomenclatureViewModel = NomenclatureViewModel(
            getEditableNomenclaturesUseCase,
            getNomenclatureValuesByTypeAndTaxonomyUseCase
        )
    }

    @Test
    fun `should get all editable nomenclature types with default value by nomenclature main type`() =
        runTest {
            // given some nomenclature types with default values
            val expectedEditableNomenclatures = listOf(
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
                    visible = false,
                    value = PropertyValue(
                        code = "STATUT_BIO",
                        label = "Non renseigné",
                        value = 29L
                    )
                )
            )
            coEvery {
                getEditableNomenclaturesUseCase.run(
                    GetEditableNomenclaturesUseCase.Params(
                        BaseEditableNomenclatureType.Type.INFORMATION,
                    )
                )
            } returns Right(expectedEditableNomenclatures)
            coEvery {
                getEditableNomenclaturesUseCase(
                    GetEditableNomenclaturesUseCase.Params(
                        BaseEditableNomenclatureType.Type.INFORMATION,
                    ),
                    nomenclatureViewModel.viewModelScope,
                    any()
                )
            } answers { callOriginal() }

            // when
            nomenclatureViewModel.getEditableNomenclatures(BaseEditableNomenclatureType.Type.INFORMATION)
            nomenclatureViewModel.editableNomenclatures.observeForever(editableNomenclaturesObserver)
            nomenclatureViewModel.failure.observeForever(failureObserver)

            // then
            verify(atLeast = 1) { editableNomenclaturesObserver.onChanged(expectedEditableNomenclatures) }
            confirmVerified(editableNomenclaturesObserver)
        }

    @Test
    fun `should get NoNomenclatureTypeFoundLocallyFailure if no nomenclature types was found`() =
        runTest {
            // given some failure from usecase
            coEvery {
                getEditableNomenclaturesUseCase.run(
                    GetEditableNomenclaturesUseCase.Params(
                        BaseEditableNomenclatureType.Type.INFORMATION,
                    )
                )
            } returns Left(NoNomenclatureTypeFoundLocallyFailure)
            coEvery {
                getEditableNomenclaturesUseCase(
                    GetEditableNomenclaturesUseCase.Params(
                        BaseEditableNomenclatureType.Type.INFORMATION,
                    ),
                    nomenclatureViewModel.viewModelScope,
                    any()
                )
            } answers { callOriginal() }

            // when
            nomenclatureViewModel.getEditableNomenclatures(BaseEditableNomenclatureType.Type.INFORMATION)
            nomenclatureViewModel.editableNomenclatures.observeForever(editableNomenclaturesObserver)
            nomenclatureViewModel.failure.observeForever(failureObserver)

            // then
            verify(atLeast = 1) { failureObserver.onChanged(NoNomenclatureTypeFoundLocallyFailure) }
            confirmVerified(failureObserver)
        }
}