package fr.geonature.occtax.features.record.data

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import fr.geonature.commons.data.entity.Taxon
import fr.geonature.commons.data.entity.Taxonomy
import fr.geonature.commons.util.getInputsFolder
import fr.geonature.commons.util.toDate
import fr.geonature.mountpoint.util.FileUtils
import fr.geonature.occtax.CoroutineTestRule
import fr.geonature.occtax.features.record.domain.ObservationRecord
import fr.geonature.occtax.features.record.error.ObservationRecordException
import fr.geonature.occtax.features.record.io.ObservationRecordJsonReader
import fr.geonature.occtax.features.record.io.ObservationRecordJsonWriter
import io.mockk.MockKAnnotations.init
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File

/**
 * Unit tests about [IObservationRecordLocalDataSource].
 *
 * @author S. Grimault
 */
@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class ObservationRecordDataSourceTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private lateinit var application: Application
    private lateinit var observationRecordLocalDataSource: IObservationRecordLocalDataSource

    private val geoNatureModuleName = "occtax"

    @Before
    fun setUp() {
        init(this)

        application = ApplicationProvider.getApplicationContext()
        observationRecordLocalDataSource = ObservationRecordLocalDataSourceImpl(
            application,
            geoNatureModuleName,
            coroutineTestRule.testDispatcher
        )
    }

    @Test
    fun `should return an empty list when reading undefined observation records`() =
        runTest {
            // when reading non existing observation records
            val noSuchObservationRecords = observationRecordLocalDataSource.readAll()

            // then
            assertTrue(noSuchObservationRecords.isEmpty())
        }

    @Test
    fun `should save and read observation records`() =
        runTest {
            // given some observation records to save and read
            val record1 = ObservationRecord(internalId = 1234).apply {
                dates.start = toDate("2016-10-28T08:15:00Z")!!
            }
            val record2 = ObservationRecord(internalId = 1235).apply {
                dates.start = toDate("2016-10-28T10:15:00Z")!!
            }
            val record3 = ObservationRecord(internalId = 1237).apply {
                dates.start = toDate("2016-10-28T09:15:00Z")!!
            }

            observationRecordLocalDataSource.save(record1)
            observationRecordLocalDataSource.save(record2)
            observationRecordLocalDataSource.save(record3)

            // and some observation records ready to synchronize
            val record4 = ObservationRecord(
                internalId = 1236,
                status = ObservationRecord.Status.TO_SYNC
            ).apply {
                dates.start = toDate("2016-10-28T07:15:00Z")!!
            }
            File(
                FileUtils
                    .getInputsFolder(application)
                    .also { it.mkdirs() },
                "input_${record4.internalId}.json"
            )
                .bufferedWriter()
                .use { out ->
                    out.write(ObservationRecordJsonWriter().write(record4))
                    out.flush()
                    out.close()
                }

            // when reading these observation records from local data source
            val observationRecords = observationRecordLocalDataSource.readAll()

            // then
            assertArrayEquals(
                arrayOf(
                    record2.internalId to record2.status,
                    record3.internalId to record3.status,
                    record1.internalId to record1.status,
                    record4.internalId to record4.status
                ),
                observationRecords.map { it.internalId to it.status }
                    .toTypedArray()
            )
        }

    @Test
    fun `should throw NotFoundException if trying to read undefined observation record`() =
        runTest {
            val exception = runCatching { observationRecordLocalDataSource.read(1234) }.exceptionOrNull()

            assertTrue(exception is ObservationRecordException.NotFoundException)
            assertEquals(
                (exception as ObservationRecordException.NotFoundException).message,
                ObservationRecordException.NotFoundException(1234).message
            )
        }

    @Test
    fun `should save and read observation record`() =
        runTest {
            // given an observation record to save and read
            val observationRecord = ObservationRecord(
                id = 1234,
                internalId = 1234
            ).apply {
                dates.start = toDate("2016-10-28T08:15:00Z")!!
                dates.end = toDate("2016-10-29T09:00:00Z")!!
                taxa.add(
                    Taxon(
                        8L,
                        "taxon_02",
                        Taxonomy(
                            "Animalia",
                            "Ascidies"
                        )
                    )
                )
            }

            // when saving this observation record
            val savedObservationRecord = observationRecordLocalDataSource.save(observationRecord)

            // when reading this observation record from local data source
            val observationRecordFromLocalDataSource =
                observationRecordLocalDataSource.read(observationRecord.internalId)

            // then
            assertEquals(
                observationRecord,
                savedObservationRecord
            )
            assertEquals(
                savedObservationRecord,
                observationRecordFromLocalDataSource
            )
        }

    @Test
    fun `should save and delete observation record`() =
        runTest {
            // given an observation record to save and delete
            val observationRecord = ObservationRecord(
                id = 1234,
                internalId = 1234
            ).apply {
                dates.start = toDate("2016-10-28T08:15:00Z")!!
                dates.end = toDate("2016-10-29T09:00:00Z")!!
            }

            // when saving this Input
            observationRecordLocalDataSource.save(observationRecord)

            // when reading this Input from local data source
            val observationRecordFromLocalDataSource =
                observationRecordLocalDataSource.read(observationRecord.internalId)

            // then
            assertEquals(
                observationRecord,
                observationRecordFromLocalDataSource
            )

            // when deleting this Input from local data source
            val deletedObservationRecord =
                observationRecordLocalDataSource.delete(observationRecord.internalId)

            // then
            assertEquals(
                observationRecord,
                deletedObservationRecord
            )

            val exception =
                runCatching { observationRecordLocalDataSource.read(observationRecord.internalId) }.exceptionOrNull()

            assertTrue(exception is ObservationRecordException.NotFoundException)
            assertEquals(
                (exception as ObservationRecordException.NotFoundException).message,
                ObservationRecordException.NotFoundException(observationRecord.internalId).message
            )
        }

    @Test
    fun `should save and export existing observation record`() =
        runTest {
            // given an observation record to save and export
            val observationRecord = ObservationRecord(
                id = 1234,
                internalId = 1234
            ).apply {
                dates.start = toDate("2016-10-28T08:15:00Z")!!
                dates.end = toDate("2016-10-29T09:00:00Z")!!
            }

            // when saving this observation record
            observationRecordLocalDataSource.save(observationRecord)
            // and exporting this observation record
            val exportedObservationRecord =
                observationRecordLocalDataSource.export(observationRecord.internalId)

            // when reading this observation record from local data source
            val observationRecordFromLocalDataSource =
                observationRecordLocalDataSource.read(observationRecord.internalId)

            // then
            assertEquals(
                exportedObservationRecord,
                observationRecordFromLocalDataSource
            )
            assertEquals(
                ObservationRecord.Status.TO_SYNC,
                observationRecordFromLocalDataSource.status
            )
            assertEquals(
                geoNatureModuleName,
                exportedObservationRecord.module.module
            )

            val exportedJsonFile = File(
                FileUtils.getInputsFolder(application),
                "input_${observationRecord.id}.json"
            )
            assertTrue(exportedJsonFile.exists())

            val observationRecordFromExportedJsonFile = ObservationRecordJsonReader().read(
                exportedJsonFile.bufferedReader()
            )
            assertEquals(
                exportedObservationRecord,
                observationRecordFromExportedJsonFile
            )
        }

    @Test
    fun `should save an already exported observation record`() =
        runTest {
            // given an observation record to save and export
            val observationRecord = ObservationRecord(
                id = 1234,
                internalId = 1234
            ).apply {
                dates.start = toDate("2016-10-28T08:15:00Z")!!
                dates.end = toDate("2016-10-29T09:00:00Z")!!
            }

            // when saving this observation record
            observationRecordLocalDataSource.save(observationRecord)
            // and exporting this observation record
            val exportedObservationRecord =
                observationRecordLocalDataSource.export(observationRecord.internalId)

            // when reading this observation record from local data source
            var observationRecordFromLocalDataSource =
                observationRecordLocalDataSource.read(observationRecord.internalId)

            // then
            assertEquals(
                exportedObservationRecord,
                observationRecordFromLocalDataSource
            )
            assertEquals(
                ObservationRecord.Status.TO_SYNC,
                observationRecordFromLocalDataSource.status
            )
            assertEquals(
                geoNatureModuleName,
                exportedObservationRecord.module.module
            )

            var exportedJsonFile = File(
                FileUtils.getInputsFolder(application),
                "input_${observationRecord.id}.json"
            )
            assertTrue(exportedJsonFile.exists())

            // when editing again this exported observation record
            val savedObservationRecord = observationRecordLocalDataSource.save(exportedObservationRecord)

            // and reading this observation record from local data source
            observationRecordFromLocalDataSource =
                observationRecordLocalDataSource.read(observationRecord.internalId)

            // then
            assertEquals(
                savedObservationRecord,
                observationRecordFromLocalDataSource
            )
            assertEquals(
                exportedObservationRecord.copy(status = ObservationRecord.Status.DRAFT),
                observationRecordFromLocalDataSource
            )

            exportedJsonFile = File(
                FileUtils.getInputsFolder(application),
                "input_${observationRecord.id}.json"
            )
            assertFalse(exportedJsonFile.exists())
        }

    @Test
    fun `should delete and existing exported observation record`() =
        runTest {
            // given an observation record to save and export
            val observationRecord = ObservationRecord(
                id = 1234,
                internalId = 1234
            ).apply {
                dates.start = toDate("2016-10-28T08:15:00Z")!!
                dates.end = toDate("2016-10-29T09:00:00Z")!!
            }

            // when saving this observation record
            observationRecordLocalDataSource.save(observationRecord)
            // and exporting this observation record
            val exportedObservationRecord =
                observationRecordLocalDataSource.export(observationRecord.internalId)

            // when reading this observation record from local data source
            val observationRecordFromLocalDataSource =
                observationRecordLocalDataSource.read(observationRecord.internalId)

            // then
            assertEquals(
                exportedObservationRecord,
                observationRecordFromLocalDataSource
            )
            assertEquals(
                ObservationRecord.Status.TO_SYNC,
                exportedObservationRecord.status
            )
            assertTrue(
                File(
                    FileUtils.getInputsFolder(application),
                    "input_${observationRecord.id}.json"
                ).exists()
            )

            // when deleting this observation record from local data source
            observationRecordLocalDataSource.delete(observationRecord.internalId)

            // then
            val exception =
                runCatching { observationRecordLocalDataSource.read(observationRecord.internalId) }
                    .exceptionOrNull()

            assertTrue(exception is ObservationRecordException.NotFoundException)
            assertEquals(
                (exception as ObservationRecordException.NotFoundException).message,
                ObservationRecordException.NotFoundException(observationRecord.internalId).message
            )

            assertFalse(
                File(
                    FileUtils.getInputsFolder(application),
                    "input_${observationRecord.id}.json"
                ).exists()
            )
        }

    @Test
    fun `should throw NotFoundException if trying to export an undefined observation record`() =
        runTest {
            val exception =
                runCatching { observationRecordLocalDataSource.export(1234) }.exceptionOrNull()

            assertTrue(exception is ObservationRecordException.NotFoundException)
            assertEquals(
                (exception as ObservationRecordException.NotFoundException).message,
                ObservationRecordException.NotFoundException(1234).message
            )
        }
}