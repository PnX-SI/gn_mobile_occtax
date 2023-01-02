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
 * Unit tests about [ExportObservationRecordUseCase].
 *
 * @author S. Grimault
 */
@ExperimentalCoroutinesApi
class ExportObservationRecordUseCaseTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    @MockK
    private lateinit var observationRecordRepository: IObservationRecordRepository

    private lateinit var exportObservationRecordUseCase: ExportObservationRecordUseCase

    @Before
    fun setUp() {
        init(this)

        exportObservationRecordUseCase = ExportObservationRecordUseCase(observationRecordRepository)
    }

    @Test
    fun `should export an existing observation record`() =
        runTest {
            coEvery { observationRecordRepository.export(any<ObservationRecord>()) } answers {
                Result.success(firstArg<ObservationRecord>().copy(status = ObservationRecord.Status.TO_SYNC))
            }

            // when exporting an observation record
            val observationRecordToExport = ObservationRecord(internalId = 1234)
            val result =
                exportObservationRecordUseCase.run(ExportObservationRecordUseCase.Params(observationRecordToExport))

            // then
            assertTrue(result.isSuccess)
            assertEquals(
                observationRecordToExport.copy(status = ObservationRecord.Status.TO_SYNC),
                result.getOrNull()
            )
        }

    @Test
    fun `should return a NotFoundException failure if trying to export undefined observation record`() =
        runTest {
            coEvery { observationRecordRepository.export(any<ObservationRecord>()) } answers { Result.failure(ObservationRecordException.NotFoundException(firstArg<ObservationRecord>().internalId)) }

            // when exporting a non existing observation record
            val result =
                exportObservationRecordUseCase.run(ExportObservationRecordUseCase.Params(ObservationRecord()))

            // then
            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is ObservationRecordException.NotFoundException)
        }
}