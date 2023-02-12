package fr.geonature.occtax.features.nomenclature.presentation

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import fr.geonature.commons.data.entity.Nomenclature
import fr.geonature.commons.data.entity.Taxonomy
import fr.geonature.commons.error.Failure
import fr.geonature.commons.features.nomenclature.error.NomenclatureException
import fr.geonature.commons.fp.Either.Left
import fr.geonature.commons.fp.Either.Right
import fr.geonature.occtax.CoroutineTestRule
import fr.geonature.occtax.features.nomenclature.domain.EditableNomenclatureType
import fr.geonature.occtax.features.nomenclature.error.NoNomenclatureTypeFoundLocallyFailure
import fr.geonature.occtax.features.nomenclature.usecase.GetEditableNomenclaturesUseCase
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
    private lateinit var nomenclatureValuesObserver: Observer<List<Nomenclature>>

    @RelaxedMockK
    private lateinit var errorObserver: Observer<Throwable>

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

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `should get all editable nomenclature types with default value by nomenclature main type`() =
        runTest {
            // given some nomenclature types with default values
            val expectedEditableNomenclatures = listOf(
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
                    "STATUT_BIO",
                    EditableNomenclatureType.ViewType.NOMENCLATURE_TYPE,
                    label = "Statut biologique",
                    visible = false,
                    value = PropertyValue.Nomenclature(
                        code = "STATUT_BIO",
                        label = "Non renseigné",
                        value = 29L
                    )
                )
            )
            coEvery {
                getEditableNomenclaturesUseCase.run(
                    GetEditableNomenclaturesUseCase.Params(
                        EditableNomenclatureType.Type.INFORMATION,
                    )
                )
            } returns Right(expectedEditableNomenclatures)
            coEvery {
                getEditableNomenclaturesUseCase(
                    GetEditableNomenclaturesUseCase.Params(
                        EditableNomenclatureType.Type.INFORMATION,
                    ),
                    nomenclatureViewModel.viewModelScope,
                    any()
                )
            } answers { callOriginal() }

            // when
            nomenclatureViewModel.getEditableNomenclatures(EditableNomenclatureType.Type.INFORMATION)
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
                        EditableNomenclatureType.Type.INFORMATION,
                    )
                )
            } returns Left(NoNomenclatureTypeFoundLocallyFailure)
            coEvery {
                getEditableNomenclaturesUseCase(
                    GetEditableNomenclaturesUseCase.Params(
                        EditableNomenclatureType.Type.INFORMATION,
                    ),
                    nomenclatureViewModel.viewModelScope,
                    any()
                )
            } answers { callOriginal() }

            // when
            nomenclatureViewModel.getEditableNomenclatures(EditableNomenclatureType.Type.INFORMATION)
            nomenclatureViewModel.editableNomenclatures.observeForever(editableNomenclaturesObserver)
            nomenclatureViewModel.failure.observeForever(failureObserver)

            // then
            verify { failureObserver.onChanged(NoNomenclatureTypeFoundLocallyFailure) }
            confirmVerified(failureObserver)
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