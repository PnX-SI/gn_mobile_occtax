package fr.geonature.occtax.features.record.usecase

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import fr.geonature.occtax.CoroutineTestRule
import fr.geonature.occtax.features.record.domain.ObservationRecord
import fr.geonature.occtax.features.record.error.ObservationRecordException
import fr.geonature.occtax.features.record.repository.IObservationRecordRepository
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

/**
 * Unit tests about [SaveObservationRecordUseCase].
 *
 * @author S. Grimault
 */
@ExperimentalCoroutinesApi
class SaveObservationRecordUseCaseTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    @MockK
    private lateinit var observationRecordRepository: IObservationRecordRepository

    private lateinit var saveObservationRecordUseCase: SaveObservationRecordUseCase

    @Before
    fun setUp() {
        init(this)

        saveObservationRecordUseCase = SaveObservationRecordUseCase(observationRecordRepository)
    }

    @Test
    fun `should save an observation record`() =
        runTest {
            coEvery { observationRecordRepository.save(any()) } answers { Result.success(firstArg()) }

            // when saving an observation record
            val observationRecordToSave = ObservationRecord(
                internalId = 1234,
                status = ObservationRecord.Status.TO_SYNC
            )
            val result =
                saveObservationRecordUseCase.run(SaveObservationRecordUseCase.Params(observationRecordToSave))

            // then
            assertTrue(result.isSuccess)
            assertEquals(
                observationRecordToSave.copy(status = ObservationRecord.Status.DRAFT),
                result.getOrNull()
            )
        }

    @Test
    fun `should return a WriteException failure if failed to save an observation record`() =
        runTest {
            coEvery { observationRecordRepository.save(any()) } answers { Result.failure(ObservationRecordException.WriteException(firstArg<ObservationRecord>().internalId)) }

            // when saving an observation record
            val observationRecordToSave = ObservationRecord(
                internalId = 1234,
                status = ObservationRecord.Status.TO_SYNC
            )
            val result =
                saveObservationRecordUseCase.run(SaveObservationRecordUseCase.Params(observationRecordToSave))

            // then
            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is ObservationRecordException.WriteException)
        }
}