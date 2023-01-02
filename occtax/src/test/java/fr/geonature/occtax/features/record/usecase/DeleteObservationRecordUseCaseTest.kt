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
 * Unit tests about [DeleteObservationRecordUseCase].
 *
 * @author S. Grimault
 */
@ExperimentalCoroutinesApi
class DeleteObservationRecordUseCaseTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    @MockK
    private lateinit var observationRecordRepository: IObservationRecordRepository

    private lateinit var deleteObservationRecordUseCase: DeleteObservationRecordUseCase

    @Before
    fun setUp() {
        init(this)

        deleteObservationRecordUseCase = DeleteObservationRecordUseCase(observationRecordRepository)
    }

    @Test
    fun `should delete an existing observation record`() =
        runTest {
            val observationRecordToDelete = ObservationRecord(internalId = 1234)
            coEvery { observationRecordRepository.delete(any()) } answers { Result.success(observationRecordToDelete) }

            // when deleting existing an observation record from repository
            val result =
                deleteObservationRecordUseCase.run(DeleteObservationRecordUseCase.Params(observationRecordToDelete))

            // then
            assertTrue(result.isSuccess)
            assertEquals(
                observationRecordToDelete,
                result.getOrNull()
            )
        }

    @Test
    fun `should return a WriteException failure if failed to delete an observation record`() =
        runTest {
            coEvery { observationRecordRepository.delete(any()) } answers { Result.failure(ObservationRecordException.WriteException(firstArg())) }

            // when saving an observation record
            val result = deleteObservationRecordUseCase.run(DeleteObservationRecordUseCase.Params(ObservationRecord()))

            // then
            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is ObservationRecordException.WriteException)
        }
}