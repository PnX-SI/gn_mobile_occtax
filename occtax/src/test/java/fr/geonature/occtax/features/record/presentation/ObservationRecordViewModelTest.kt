package fr.geonature.occtax.features.record.presentation

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import fr.geonature.occtax.CoroutineTestRule
import fr.geonature.occtax.features.record.domain.ObservationRecord
import fr.geonature.occtax.features.record.domain.PropertyValue
import fr.geonature.occtax.features.record.error.ObservationRecordException
import fr.geonature.occtax.features.record.usecase.DeleteObservationRecordUseCase
import fr.geonature.occtax.features.record.usecase.EditObservationRecordUseCase
import fr.geonature.occtax.features.record.usecase.ExportObservationRecordUseCase
import fr.geonature.occtax.features.record.usecase.GetAllObservationRecordsUseCase
import fr.geonature.occtax.features.record.usecase.SaveObservationRecordUseCase
import io.mockk.MockKAnnotations.init
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import io.mockk.verifyOrder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests about [ObservationRecordViewModel].
 *
 * @author S. Grimault
 */
@ExperimentalCoroutinesApi
class ObservationRecordViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    @RelaxedMockK
    private lateinit var getAllObservationRecordsUseCase: GetAllObservationRecordsUseCase

    @RelaxedMockK
    private lateinit var saveObservationRecordUseCase: SaveObservationRecordUseCase

    @RelaxedMockK
    private lateinit var editObservationRecordUseCase: EditObservationRecordUseCase

    @RelaxedMockK
    private lateinit var deleteObservationRecordUseCase: DeleteObservationRecordUseCase

    @RelaxedMockK
    private lateinit var exportObservationRecordUseCase: ExportObservationRecordUseCase

    @RelaxedMockK
    private lateinit var errorObserver: Observer<Throwable>

    @RelaxedMockK
    private lateinit var observationRecordsObserver: Observer<List<ObservationRecord>>

    @RelaxedMockK
    private lateinit var observationRecordObserver: Observer<ObservationRecord>

    private lateinit var observationRecordViewModel: ObservationRecordViewModel

    @Before
    fun setUp() {
        init(this)

        observationRecordViewModel = ObservationRecordViewModel(
            getAllObservationRecordsUseCase,
            saveObservationRecordUseCase,
            editObservationRecordUseCase,
            deleteObservationRecordUseCase,
            exportObservationRecordUseCase
        )

        observationRecordViewModel.observationRecords.observeForever(observationRecordsObserver)
        observationRecordViewModel.observationRecord.observeForever(observationRecordObserver)
        observationRecordViewModel.error.observeForever(errorObserver)

        every {
            getAllObservationRecordsUseCase(
                any(),
                any(),
                any()
            )
        } answers {
            callOriginal()
        }
        every {
            saveObservationRecordUseCase(
                any(),
                any(),
                any()
            )
        } answers {
            callOriginal()
        }
        every {
            editObservationRecordUseCase(
                any(),
                any(),
                any()
            )
        } answers {
            callOriginal()
        }
        every {
            deleteObservationRecordUseCase(
                any(),
                any(),
                any()
            )
        } answers {
            callOriginal()
        }
        every {
            exportObservationRecordUseCase(
                any(),
                any(),
                any()
            )
        } answers {
            callOriginal()
        }
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `should return an empty list when reading undefined observation records`() =
        runTest {
            // given an empty list from use case
            val expectedObservationRecords = listOf<ObservationRecord>()
            coEvery {
                getAllObservationRecordsUseCase.run(any())
            } returns Result.success(expectedObservationRecords)

            // when reading non existing observation records
            observationRecordViewModel.getAll()
            advanceUntilIdle()

            // then
            verify(atLeast = 1) { observationRecordsObserver.onChanged(expectedObservationRecords) }
            confirmVerified(observationRecordsObserver)
        }

    @Test
    fun `should read existing observation records`() =
        runTest {
            // given some observation records from use case
            val expectedObservationRecords = listOf(
                ObservationRecord(internalId = 1234),
                ObservationRecord(internalId = 1235),
                ObservationRecord(internalId = 1236)
            )
            coEvery {
                getAllObservationRecordsUseCase.run(any())
            } returns Result.success(expectedObservationRecords)

            // when reading these observation records
            observationRecordViewModel.getAll()
            advanceUntilIdle()

            // then
            verify(atLeast = 1) { observationRecordsObserver.onChanged(expectedObservationRecords) }
            confirmVerified(observationRecordsObserver)
        }

    @Test
    fun `should start editing an observation record`() = runTest {
        // given some observation record
        val observationRecord = ObservationRecord(internalId = 1234)
        coEvery {
            editObservationRecordUseCase.run(any())
        } answers {
            Result.success(firstArg<EditObservationRecordUseCase.Params>().observationRecord.apply {
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
        }

        // when loading all default nomenclature values
        observationRecordViewModel.startEdit(observationRecord)
        advanceUntilIdle()

        // then
        verify(atLeast = 1) {
            observationRecordObserver.onChanged(ObservationRecord(internalId = 1234).apply {
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
        }
        confirmVerified(observationRecordsObserver)
    }

    @Test
    fun `should save an observation record`() =
        runTest {
            // given some observation record to save
            val observationRecordToSave = ObservationRecord(
                internalId = 1234,
                status = ObservationRecord.Status.TO_SYNC
            )
            coEvery {
                saveObservationRecordUseCase.run(any())
            } returns Result.success(observationRecordToSave.copy(status = ObservationRecord.Status.DRAFT))

            // when saving this observation record
            observationRecordViewModel.save(observationRecordToSave)
            advanceUntilIdle()

            // then
            verify(atLeast = 1) {
                observationRecordObserver.onChanged(
                    observationRecordToSave.copy(
                        status = ObservationRecord.Status.DRAFT
                    )
                )
            }
            verify(atLeast = 1) {
                observationRecordsObserver.onChanged(
                    listOf(
                        observationRecordToSave.copy(
                            status = ObservationRecord.Status.DRAFT
                        )
                    )
                )
            }
            confirmVerified(
                observationRecordObserver,
                observationRecordsObserver
            )
        }

    @Test
    fun `should delete an existing observation record`() =
        runTest {
            // given some existing observation records
            val expectedObservationRecords = listOf(
                ObservationRecord(internalId = 1234),
                ObservationRecord(internalId = 1235),
                ObservationRecord(internalId = 1236)
            )
            coEvery {
                getAllObservationRecordsUseCase.run(any())
            } returns Result.success(expectedObservationRecords)
            coEvery {
                deleteObservationRecordUseCase.run(any())
            } answers { Result.success(firstArg<DeleteObservationRecordUseCase.Params>().observationRecord) }

            // when reading these observation records
            observationRecordViewModel.getAll()
            advanceUntilIdle()

            // and deleting the given observation record
            observationRecordViewModel.delete(expectedObservationRecords.first { it.internalId == 1235L })
            advanceUntilIdle()

            // then
            verifyOrder {
                observationRecordsObserver.onChanged(expectedObservationRecords)
                observationRecordsObserver.onChanged(expectedObservationRecords.filter { it.internalId != 1235L })
            }
            confirmVerified(observationRecordsObserver)
        }

    @Test
    fun `should export an existing observation record`() =
        runTest {
            // given some observation record to export
            val observationRecordToExport = ObservationRecord(internalId = 1234)
            coEvery {
                exportObservationRecordUseCase.run(any())
            } returns Result.success(observationRecordToExport.copy(status = ObservationRecord.Status.TO_SYNC))

            var exported = false
            // when exporting this observation record
            observationRecordViewModel.export(observationRecordToExport) { exported = true }
            advanceUntilIdle()

            // then
            assertTrue(exported)
            verify(atLeast = 1) {
                observationRecordObserver.onChanged(
                    observationRecordToExport.copy(
                        status = ObservationRecord.Status.TO_SYNC
                    )
                )
            }
            verify(atLeast = 1) {
                observationRecordsObserver.onChanged(
                    listOf(
                        observationRecordToExport.copy(
                            status = ObservationRecord.Status.TO_SYNC
                        )
                    )
                )
            }
            confirmVerified(
                observationRecordObserver,
                observationRecordsObserver
            )
        }

    @Test
    fun `should return NotFoundException if trying to export undefined observation record`() =
        runTest {
            // given a non existing observation record
            val observationRecordToExport = ObservationRecord(internalId = 1234)
            coEvery {
                exportObservationRecordUseCase.run(any())
            } returns Result.failure(ObservationRecordException.NotFoundException(observationRecordToExport.internalId))

            // when exporting this observation record
            observationRecordViewModel.export(observationRecordToExport)
            advanceUntilIdle()

            // then
            verify(atLeast = 1) {
                errorObserver.onChanged(ObservationRecordException.NotFoundException(observationRecordToExport.internalId))
            }
            verify(inverse = true) { observationRecordObserver.onChanged(any()) }
            verify(inverse = true) { observationRecordsObserver.onChanged(any()) }
            confirmVerified(
                errorObserver,
                observationRecordObserver,
                observationRecordsObserver
            )
        }
}