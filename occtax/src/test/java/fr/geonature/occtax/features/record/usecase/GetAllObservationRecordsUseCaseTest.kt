package fr.geonature.occtax.features.record.usecase

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import fr.geonature.commons.interactor.BaseResultUseCase
import fr.geonature.occtax.CoroutineTestRule
import fr.geonature.occtax.features.record.domain.ObservationRecord
import fr.geonature.occtax.features.record.repository.IObservationRecordRepository
import io.mockk.MockKAnnotations.init
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests about [GetAllObservationRecordsUseCase].
 *
 * @author S. Grimault
 */
@ExperimentalCoroutinesApi
class GetAllObservationRecordsUseCaseTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    @MockK
    private lateinit var observationRecordRepository: IObservationRecordRepository

    private lateinit var getAllObservationRecordsUseCase: GetAllObservationRecordsUseCase

    @Before
    fun setUp() {
        init(this)

        getAllObservationRecordsUseCase =
            GetAllObservationRecordsUseCase(
                observationRecordRepository
            )
    }

    @Test
    fun `should return an empty list when reading undefined observation records`() =
        runTest {
            // given an empty list from repository
            coEvery { observationRecordRepository.readAll() } returns Result.success(emptyList())

            // when reading non existing observation records from use case
            val result = getAllObservationRecordsUseCase.run(BaseResultUseCase.None())

            // then
            assertTrue(result.isSuccess)
            assertTrue(
                result.getOrNull()
                    ?.isEmpty() == true
            )
        }

    @Test
    fun `should get all observation records`() =
        runTest {
            // given some observation records from repository
            val observationRecords = listOf(
                ObservationRecord(internalId = 1234),
                ObservationRecord(internalId = 1235),
                ObservationRecord(internalId = 1236),
            )
            coEvery { observationRecordRepository.readAll() } returns Result.success(observationRecords)

            // when reading these observation records from use case
            val result = getAllObservationRecordsUseCase.run(BaseResultUseCase.None())

            // then
            assertTrue(result.isSuccess)
            assertArrayEquals(
                observationRecords.toTypedArray(),
                result
                    .getOrNull()
                    ?.toTypedArray()
            )
        }
}