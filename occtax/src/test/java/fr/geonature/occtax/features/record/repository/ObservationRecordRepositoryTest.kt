package fr.geonature.occtax.features.record.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import fr.geonature.commons.data.entity.Taxon
import fr.geonature.commons.data.entity.Taxonomy
import fr.geonature.commons.features.taxon.data.ITaxonLocalDataSource
import fr.geonature.occtax.CoroutineTestRule
import fr.geonature.occtax.features.record.data.IObservationRecordLocalDataSource
import fr.geonature.occtax.features.record.domain.ObservationRecord
import fr.geonature.occtax.features.record.domain.PropertyValue
import fr.geonature.occtax.features.record.error.ObservationRecordException
import io.mockk.MockKAnnotations.init
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests about [IObservationRecordRepository].
 *
 * @author S. Grimault
 */
@ExperimentalCoroutinesApi
class ObservationRecordRepositoryTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    @MockK
    private lateinit var observationRecordLocalDataSource: IObservationRecordLocalDataSource

    @MockK
    private lateinit var taxonLocalDataSource: ITaxonLocalDataSource

    private lateinit var observationRecordRepository: IObservationRecordRepository

    private val taxaFromLocalDataSource = listOf(
        Taxon(
            84L,
            "Salamandra fusca",
            Taxonomy(
                kingdom = "Animalia",
                group = "Amphibiens"
            ),
            null,
            "Salamandra atra atra (Laurenti, 1768)"
        ),
        Taxon(
            324L,
            "Rana alpina",
            Taxonomy(
                kingdom = "Animalia",
                group = "Amphibiens"
            ),
            "Grenouille rousse (La)",
            "Rana temporaria Linnaeus, 1758"
        )
    )

    @Before
    fun setUp() {
        init(this)

        observationRecordRepository = ObservationRecordRepositoryImpl(
            observationRecordLocalDataSource,
            taxonLocalDataSource
        )
    }

    @Test
    fun `should return an empty list when reading undefined observation records`() =
        runTest {
            // given an empty list from data source
            coEvery { observationRecordLocalDataSource.readAll() } returns emptyList()
            coEvery { taxonLocalDataSource.findTaxaByIds(any()) } returns taxaFromLocalDataSource

            // when reading non existing observation records
            val result = observationRecordRepository.readAll()

            // then
            assertTrue(result.isSuccess)
            assertTrue(
                result.getOrNull()
                    ?.isEmpty() == true
            )
        }

    @Test
    fun `should read existing observation records`() =
        runTest {
            // given some observation records from data source
            val observationRecords = listOf(
                ObservationRecord(internalId = 1234),
                ObservationRecord(internalId = 1235),
                ObservationRecord(internalId = 1236),
            )
            coEvery { observationRecordLocalDataSource.readAll() } returns observationRecords
            coEvery { taxonLocalDataSource.findTaxaByIds() } returns emptyList()

            // when reading these observation records from repository
            val result = observationRecordRepository.readAll()

            // then
            assertTrue(result.isSuccess)
            assertArrayEquals(
                observationRecords.toTypedArray(),
                result
                    .getOrNull()
                    ?.toTypedArray()
            )
        }

    @Test
    fun `should read existing observation record`() =
        runTest {
            val observationRecord = ObservationRecord(internalId = 1234)
            coEvery { observationRecordLocalDataSource.read(observationRecord.internalId) } returns observationRecord
            coEvery { taxonLocalDataSource.findTaxaByIds() } returns emptyList()

            // when reading existing observation record from repository
            val result = observationRecordRepository.read(observationRecord.internalId)

            // then
            assertTrue(result.isSuccess)
            assertEquals(
                observationRecord,
                result.getOrNull()
            )
        }

    @Test
    fun `should read existing observation record with some taxa`() =
        runTest {
            val observationRecord = ObservationRecord(internalId = 1234).apply {
                taxa.add(
                    Taxon(
                        84L,
                        "Salamandra fusca",
                        Taxonomy(
                            kingdom = Taxonomy.ANY,
                            group = Taxonomy.ANY
                        ),
                        null,
                        null
                    )
                )
                    .apply {
                        listOf(
                            PropertyValue.Text(
                                "comment",
                                "Some comment"
                            )
                        ).map { it.toPair() }
                            .forEach {
                                properties[it.first] = it.second
                            }
                    }
            }
            coEvery { observationRecordLocalDataSource.read(observationRecord.internalId) } returns observationRecord
            coEvery { taxonLocalDataSource.findTaxaByIds(any()) } returns taxaFromLocalDataSource

            // when reading existing observation record from repository
            val result = observationRecordRepository.read(observationRecord.internalId)

            // then
            assertTrue(result.isSuccess)
            assertEquals(
                observationRecord.copy()
                    .apply {
                        taxa.taxa = taxa.taxa.map {
                            it.copy(taxon = taxaFromLocalDataSource.firstOrNull { taxon -> taxon.id == it.taxon.id }
                                ?: it.taxon)
                        }
                    },
                result.getOrNull()
            )
        }

    @Test
    fun `should return a NotFoundException failure if trying to read an undefined observation record`() =
        runTest {
            coEvery { observationRecordLocalDataSource.read(any()) } answers { throw ObservationRecordException.NotFoundException(firstArg()) }

            // when reading a non existing observation record from repository
            val result = observationRecordRepository.read(1234)

            // then
            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is ObservationRecordException.NotFoundException)
        }

    @Test
    fun `should return a ReadException failure if failed to read an existing observation record`() =
        runTest {
            coEvery { observationRecordLocalDataSource.read(any()) } answers { throw ObservationRecordException.ReadException(firstArg()) }

            // when reading a non existing observation record from repository
            val result = observationRecordRepository.read(1234)

            // then
            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is ObservationRecordException.ReadException)
        }

    @Test
    fun `should save an observation record`() =
        runTest {
            coEvery { observationRecordLocalDataSource.save(any()) } answers { firstArg() }

            // when saving an observation record
            val observationRecordToSave = ObservationRecord(internalId = 1234)
            val result = observationRecordRepository.save(observationRecordToSave)

            // then
            assertTrue(result.isSuccess)
            coVerify { observationRecordLocalDataSource.save(observationRecordToSave) }
            assertEquals(
                observationRecordToSave,
                result.getOrNull()
            )
        }

    @Test
    fun `should return a ReadException failure if failed to save an observation record`() =
        runTest {
            coEvery { observationRecordLocalDataSource.save(any()) } answers { throw ObservationRecordException.ReadException(firstArg<ObservationRecord>().internalId) }

            // when saving an observation record
            val result = observationRecordRepository.save(ObservationRecord(internalId = 1234))

            // then
            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is ObservationRecordException.ReadException)
        }

    @Test
    fun `should delete an existing observation record`() =
        runTest {
            val observationRecordToDelete = ObservationRecord(internalId = 1234)
            coEvery { observationRecordLocalDataSource.delete(any()) } returns observationRecordToDelete

            // when deleting existing an observation record from repository
            val result = observationRecordRepository.delete(observationRecordToDelete.internalId)

            // then
            assertTrue(result.isSuccess)
            coVerify { observationRecordLocalDataSource.delete(1234) }
            assertEquals(
                observationRecordToDelete,
                result.getOrNull()
            )
        }

    @Test
    fun `should return a WriteException failure if failed to delete an observation record`() =
        runTest {
            coEvery { observationRecordLocalDataSource.delete(any()) } answers { throw ObservationRecordException.WriteException(firstArg()) }

            // when saving an observation record
            val result = observationRecordRepository.delete(1234)

            // then
            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is ObservationRecordException.WriteException)
        }

    @Test
    fun `should export an existing observation record from given ID`() =
        runTest {
            val exportedObservationRecord = ObservationRecord(
                internalId = 1234,
                status = ObservationRecord.Status.TO_SYNC
            )
            coEvery { observationRecordLocalDataSource.export(any<Long>()) } answers { exportedObservationRecord }

            // when exporting an observation record
            val observationRecordToExport = ObservationRecord(internalId = 1234)
            val result = observationRecordRepository.export(observationRecordToExport.internalId)

            // then
            assertTrue(result.isSuccess)
            coVerify { observationRecordLocalDataSource.export(observationRecordToExport.internalId) }
            assertEquals(
                exportedObservationRecord,
                result.getOrNull()
            )
        }

    @Test
    fun `should export an existing observation record`() =
        runTest {
            coEvery { observationRecordLocalDataSource.export(any<ObservationRecord>()) } answers {
                firstArg<ObservationRecord>().copy(status = ObservationRecord.Status.TO_SYNC)
            }

            // when exporting an observation record
            val observationRecordToExport = ObservationRecord(internalId = 1234)
            val result = observationRecordRepository.export(observationRecordToExport)

            // then
            assertTrue(result.isSuccess)
            coVerify { observationRecordLocalDataSource.export(observationRecordToExport) }
            assertEquals(
                observationRecordToExport.copy(status = ObservationRecord.Status.TO_SYNC),
                result.getOrNull()
            )
        }

    @Test
    fun `should return a NotFoundException failure if trying to export undefined observation record`() =
        runTest {
            coEvery { observationRecordLocalDataSource.export(any<Long>()) } answers { throw ObservationRecordException.NotFoundException(firstArg()) }

            // when exporting a non existing observation record
            val result = observationRecordRepository.export(1234)

            // then
            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is ObservationRecordException.NotFoundException)
        }
}