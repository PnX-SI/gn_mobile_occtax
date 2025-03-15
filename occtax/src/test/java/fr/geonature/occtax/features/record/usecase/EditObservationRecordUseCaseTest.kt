package fr.geonature.occtax.features.record.usecase

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import fr.geonature.commons.data.entity.Dataset
import fr.geonature.commons.data.entity.InputObserver
import fr.geonature.commons.features.inputObservers.error.InputObserverException
import fr.geonature.occtax.CoroutineTestRule
import fr.geonature.occtax.features.record.domain.ObservationRecord
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
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Instant
import java.util.Date

/**
 * Unit tests about [EditObservationRecordUseCase].
 *
 * @author S. Grimault
 */
@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class EditObservationRecordUseCaseTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private lateinit var application: Application

    @MockK
    private lateinit var setDefaultDatasetUseCase: SetDefaultDatasetUseCase

    @MockK
    private lateinit var setDefaultInputObserversUseCase: SetDefaultInputObserversUseCase

    @MockK
    private lateinit var setDefaultNomenclatureValuesUseCase: SetDefaultNomenclatureValuesUseCase

    @MockK
    private lateinit var loadAllMediaRecordUseCase: LoadAllMediaRecordUseCase

    private lateinit var editObservationRecordUseCase: EditObservationRecordUseCase

    @Before
    fun setUp() {
        init(this)

        application = ApplicationProvider.getApplicationContext()

        editObservationRecordUseCase =
            EditObservationRecordUseCase(
                setDefaultDatasetUseCase,
                setDefaultInputObserversUseCase,
                setDefaultNomenclatureValuesUseCase,
                loadAllMediaRecordUseCase
            )
    }

    @Test
    fun `should return an observation record with everything loaded`() =
        runTest {
            // given some empty observation record
            val observationRecord = ObservationRecord(internalId = 1234)

            coEvery {
                setDefaultDatasetUseCase.run(any())
            } returns Result.success(observationRecord.apply {
                dataset.setDataset(
                    Dataset(
                        id = 7,
                        name = "Ablettes du PNE",
                        description = "Observations d'ablettes par le PNE",
                        active = true,
                        createdAt = Date.from(Instant.parse("2020-03-28T10:00:00Z")),
                        null,
                        100
                    )
                )
            })

            coEvery {
                setDefaultInputObserversUseCase.run(any())
            } returns Result.success(observationRecord.apply {
                observers.setObservers(
                    listOf(
                        InputObserver(
                            id = 2L,
                            lastname = "Li",
                            firstname = "Andy"
                        ),
                        InputObserver(
                            id = 4L,
                            lastname = "Jenkins",
                            firstname = "Noor"
                        )
                    )
                )
            })

            coEvery {
                setDefaultNomenclatureValuesUseCase.run(any())
            } returns Result.success(observationRecord.apply {
                listOf(
                    PropertyValue.Nomenclature(
                        "TYP_GRP",
                        "NSP",
                        129
                    )
                ).map { it.toPair() }
                    .forEach {
                        properties[it.first] = it.second
                    }
            })

            coEvery {
                loadAllMediaRecordUseCase.run(any())
            } returns Result.success(observationRecord)

            // when start editing observation record from use case
            val result =
                editObservationRecordUseCase.run(EditObservationRecordUseCase.Params(observationRecord))

            // then
            assertTrue(result.isSuccess)
            assertEquals(
                observationRecord.apply {
                    dataset.setDataset(
                        Dataset(
                            id = 7,
                            name = "Ablettes du PNE",
                            description = "Observations d'ablettes par le PNE",
                            active = true,
                            createdAt = Date.from(Instant.parse("2020-03-28T10:00:00Z")),
                            null,
                            100
                        )
                    )
                    observers.setObservers(
                        listOf(
                            InputObserver(
                                id = 2L,
                                lastname = "Li",
                                firstname = "Andy"
                            ),
                            InputObserver(
                                id = 4L,
                                lastname = "Jenkins",
                                firstname = "Noor"
                            )
                        )
                    )
                    listOf(
                        PropertyValue.Nomenclature(
                            "TYP_GRP",
                            "NSP",
                            129
                        )
                    ).map { it.toPair() }
                        .forEach {
                            properties[it.first] = it.second
                        }
                },
                result.getOrThrow()
            )
        }

    @Test
    fun `should return anyway an observation record if something goes wrong while loading some existing values`() =
        runTest {
            // given some empty observation record
            val observationRecord = ObservationRecord(internalId = 1234)

            coEvery {
                setDefaultDatasetUseCase.run(any())
            } returns Result.success(observationRecord.apply {
                dataset.setDataset(
                    Dataset(
                        id = 7,
                        name = "Ablettes du PNE",
                        description = "Observations d'ablettes par le PNE",
                        active = true,
                        createdAt = Date.from(Instant.parse("2020-03-28T10:00:00Z")),
                        null,
                        100
                    )
                )
            })

            coEvery {
                setDefaultInputObserversUseCase.run(any())
            } returns Result.failure(InputObserverException.NoInputObserversFoundException(listOf(2L)))

            coEvery {
                setDefaultNomenclatureValuesUseCase.run(any())
            } returns Result.success(observationRecord.apply {
                listOf(
                    PropertyValue.Nomenclature(
                        "TYP_GRP",
                        "NSP",
                        129
                    )
                ).map { it.toPair() }
                    .forEach {
                        properties[it.first] = it.second
                    }
            })

            coEvery {
                loadAllMediaRecordUseCase.run(any())
            } returns Result.success(observationRecord)

            // when start editing observation record from use case
            val result =
                editObservationRecordUseCase.run(EditObservationRecordUseCase.Params(observationRecord))

            // then
            assertTrue(result.isSuccess)
            assertEquals(
                observationRecord.apply {
                    dataset.setDataset(
                        Dataset(
                            id = 7,
                            name = "Ablettes du PNE",
                            description = "Observations d'ablettes par le PNE",
                            active = true,
                            createdAt = Date.from(Instant.parse("2020-03-28T10:00:00Z")),
                            null,
                            100
                        )
                    )
                    listOf(
                        PropertyValue.Nomenclature(
                            "TYP_GRP",
                            "NSP",
                            129
                        )
                    ).map { it.toPair() }
                        .forEach {
                            properties[it.first] = it.second
                        }
                },
                result.getOrThrow()
            )
        }
}