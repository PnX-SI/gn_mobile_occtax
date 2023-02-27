package fr.geonature.occtax.features.record.usecase

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import fr.geonature.commons.data.entity.Taxon
import fr.geonature.commons.data.entity.Taxonomy
import fr.geonature.commons.settings.IAppSettingsManager
import fr.geonature.commons.settings.error.AppSettingsException
import fr.geonature.datasync.settings.DataSyncSettings
import fr.geonature.datasync.settings.error.DataSyncSettingsNotFoundException
import fr.geonature.occtax.CoroutineTestRule
import fr.geonature.occtax.features.record.data.IObservationRecordRemoteDataSource
import fr.geonature.occtax.features.record.domain.ObservationRecord
import fr.geonature.occtax.features.record.domain.PropertyValue
import fr.geonature.occtax.features.record.error.ObservationRecordException
import fr.geonature.occtax.features.record.repository.IMediaRecordRepository
import fr.geonature.occtax.features.record.repository.IObservationRecordRepository
import fr.geonature.occtax.settings.AppSettings
import fr.geonature.occtax.settings.InputDateSettings
import fr.geonature.occtax.settings.InputSettings
import io.mockk.MockKAnnotations.init
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifySequence
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests about [SynchronizeObservationRecordUseCase].
 *
 * @author S. Grimault
 */
@ExperimentalCoroutinesApi
internal class SynchronizeObservationRecordUseCaseTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    @MockK
    private lateinit var appSettingsManager: IAppSettingsManager<AppSettings>

    @MockK
    private lateinit var observationRecordRemoteDataSource: IObservationRecordRemoteDataSource

    @MockK
    private lateinit var observationRecordRepository: IObservationRecordRepository

    @MockK
    private lateinit var mediaRecordRepository: IMediaRecordRepository

    private lateinit var synchronizeObservationRecordUseCase: SynchronizeObservationRecordUseCase

    @Before
    fun setUp() {
        init(this)

        synchronizeObservationRecordUseCase =
            SynchronizeObservationRecordUseCase(
                appSettingsManager,
                observationRecordRemoteDataSource,
                observationRecordRepository,
                mediaRecordRepository
            )
    }

    @Test
    fun `should return a NoAppSettingsFoundLocallyException failure if no app settings was found`() =
        runTest {
            // given no app settings
            coEvery { appSettingsManager.loadAppSettings() } returns null

            // and an existing observation to synchronize
            val expectedObservationRecordToSend = ObservationRecord(
                internalId = 1240L,
                status = ObservationRecord.Status.TO_SYNC
            ).apply {
                comment.comment = "some comment"
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
                    .apply {
                        PropertyValue.Text(
                            "some_code",
                            "some_value"
                        )
                            .also {
                                properties[it.code] = it
                            }
                    }
            }

            // when trying to synchronize an observation record from ID
            val result =
                synchronizeObservationRecordUseCase.run(SynchronizeObservationRecordUseCase.Params(expectedObservationRecordToSend))

            // then
            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is AppSettingsException.NoAppSettingsFoundLocallyException)
        }

    @Test
    fun `should return a DataSyncSettingsNotFoundException failure if no data sync settings was found`() =
        runTest {
            // given an incomplete app settings
            coEvery { appSettingsManager.loadAppSettings() } returns AppSettings(
                inputSettings = InputSettings(
                    dateSettings = InputDateSettings(
                        startDateSettings = InputDateSettings.DateSettings.DATETIME,
                        endDateSettings = InputDateSettings.DateSettings.DATETIME
                    )
                )
            )

            // and an existing observation to synchronize
            val expectedObservationRecordToSend = ObservationRecord(
                internalId = 1240L,
                status = ObservationRecord.Status.TO_SYNC
            ).apply {
                comment.comment = "some comment"
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
                    .apply {
                        PropertyValue.Text(
                            "some_code",
                            "some_value"
                        )
                            .also {
                                properties[it.code] = it
                            }
                    }
            }

            // when trying to synchronize an observation record from ID
            val result =
                synchronizeObservationRecordUseCase.run(SynchronizeObservationRecordUseCase.Params(expectedObservationRecordToSend))

            // then
            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is DataSyncSettingsNotFoundException)
        }

    @Test
    fun `should return an InvalidStatusException failure if trying to send an observation record with wrong status`() =
        runTest {
            // given an app settings
            val appSettings = AppSettings(
                inputSettings = InputSettings(
                    dateSettings = InputDateSettings(
                        startDateSettings = InputDateSettings.DateSettings.DATETIME,
                        endDateSettings = InputDateSettings.DateSettings.DATETIME
                    )
                ),
                dataSyncSettings = DataSyncSettings(
                    geoNatureServerUrl = "https://demo.geonature.fr/geonature",
                    taxHubServerUrl = "https://demo.geonature.fr/taxhub",
                    applicationId = 3,
                    usersListId = 1,
                    taxrefListId = 100,
                    codeAreaType = "M10"
                )
            )
            coEvery { appSettingsManager.loadAppSettings() } returns appSettings
            coEvery {
                observationRecordRemoteDataSource.setBaseUrl(appSettings.dataSyncSettings?.geoNatureServerUrl!!)
            } returns Unit

            // and an existing observation record with wrong status
            val expectedObservationRecordToSend = ObservationRecord(
                internalId = 1240L
            ).apply {
                comment.comment = "some comment"
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
                    .apply {
                        PropertyValue.Text(
                            "some_code",
                            "some_value"
                        )
                            .also {
                                properties[it.code] = it
                            }
                    }
            }

            // when trying to synchronize an observation record from ID
            val result =
                synchronizeObservationRecordUseCase.run(SynchronizeObservationRecordUseCase.Params(expectedObservationRecordToSend))

            // then
            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is ObservationRecordException.InvalidStatusException)
        }

    @Test
    fun `should synchronize successfully an observation record with all taxa`() = runTest {
        // given an app settings
        val appSettings = AppSettings(
            inputSettings = InputSettings(
                dateSettings = InputDateSettings(
                    startDateSettings = InputDateSettings.DateSettings.DATETIME,
                    endDateSettings = InputDateSettings.DateSettings.DATETIME
                )
            ),
            dataSyncSettings = DataSyncSettings(
                geoNatureServerUrl = "https://demo.geonature.fr/geonature",
                taxHubServerUrl = "https://demo.geonature.fr/taxhub",
                applicationId = 3,
                usersListId = 1,
                taxrefListId = 100,
                codeAreaType = "M10"
            )
        )
        coEvery { appSettingsManager.loadAppSettings() } returns appSettings
        coEvery {
            observationRecordRemoteDataSource.setBaseUrl(appSettings.dataSyncSettings?.geoNatureServerUrl!!)
        } returns Unit

        // and an existing observation to synchronize
        val expectedObservationRecordToSend = ObservationRecord(
            internalId = 1240L,
            status = ObservationRecord.Status.TO_SYNC
        ).apply {
            comment.comment = "some comment"
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
                .apply {
                    PropertyValue.Text(
                        "some_code",
                        "some_value"
                    )
                        .also {
                            properties[it.code] = it
                        }
                }
        }
        coEvery {
            observationRecordRemoteDataSource.sendObservationRecord(
                any(),
                any()
            )
        } answers { firstArg<ObservationRecord>().copy(id = 1234L) }
        coEvery { mediaRecordRepository.synchronizeMediaFiles(any()) } answers {
            Result.success(firstArg())
        }
        coEvery {
            observationRecordRemoteDataSource.sendTaxaRecords(
                any(),
                any()
            )
        } answers { firstArg() }
        coEvery { observationRecordRemoteDataSource.deleteObservationRecord(any()) } returns Unit
        coEvery { observationRecordRepository.delete(expectedObservationRecordToSend.internalId) } returns Result.success(expectedObservationRecordToSend)

        // when trying to synchronize an observation record from ID
        val result =
            synchronizeObservationRecordUseCase.run(SynchronizeObservationRecordUseCase.Params(expectedObservationRecordToSend))

        // then
        assertTrue(result.isSuccess)
        coVerifySequence {
            observationRecordRemoteDataSource.setBaseUrl(appSettings.dataSyncSettings?.geoNatureServerUrl!!)
            observationRecordRemoteDataSource.sendObservationRecord(
                expectedObservationRecordToSend,
                appSettings
            )
            mediaRecordRepository.synchronizeMediaFiles(expectedObservationRecordToSend.taxa.taxa[0])
            observationRecordRemoteDataSource.sendTaxaRecords(
                expectedObservationRecordToSend.copy(id = 1234L),
                appSettings
            )
            observationRecordRepository.delete(expectedObservationRecordToSend.internalId)
        }
        coVerify(inverse = true) {
            observationRecordRemoteDataSource.deleteObservationRecord(any())
        }
        confirmVerified(observationRecordRepository)
        confirmVerified(observationRecordRemoteDataSource)
    }

    @Test
    fun `should delete remotely a partially synchronized observation record`() = runTest {
        // given an app settings
        val appSettings = AppSettings(
            inputSettings = InputSettings(
                dateSettings = InputDateSettings(
                    startDateSettings = InputDateSettings.DateSettings.DATETIME,
                    endDateSettings = InputDateSettings.DateSettings.DATETIME
                )
            ),
            dataSyncSettings = DataSyncSettings(
                geoNatureServerUrl = "https://demo.geonature.fr/geonature",
                taxHubServerUrl = "https://demo.geonature.fr/taxhub",
                applicationId = 3,
                usersListId = 1,
                taxrefListId = 100,
                codeAreaType = "M10"
            )
        )
        coEvery { appSettingsManager.loadAppSettings() } returns appSettings
        coEvery {
            observationRecordRemoteDataSource.setBaseUrl(appSettings.dataSyncSettings?.geoNatureServerUrl!!)
        } returns Unit

        // and an existing observation to synchronize
        val expectedObservationRecordToSend = ObservationRecord(
            internalId = 1240L,
            status = ObservationRecord.Status.TO_SYNC
        ).apply {
            comment.comment = "some comment"
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
                .apply {
                    PropertyValue.Text(
                        "some_code",
                        "some_value"
                    )
                        .also {
                            properties[it.code] = it
                        }
                }
        }

        coEvery {
            observationRecordRemoteDataSource.sendObservationRecord(
                any(),
                any()
            )
        } answers { firstArg<ObservationRecord>().copy(id = 1234L) }
        coEvery { mediaRecordRepository.synchronizeMediaFiles(any()) } answers { Result.success(firstArg()) }
        coEvery { mediaRecordRepository.deleteAllMediaFiles(any()) } answers { Result.success(firstArg()) }
        // and some error occurred when synchronizing all taxa records
        coEvery {
            observationRecordRemoteDataSource.sendTaxaRecords(
                any(),
                any()
            )
        } answers {
            throw ObservationRecordException.SynchronizeException(firstArg<ObservationRecord>().internalId)
        }
        coEvery { observationRecordRemoteDataSource.deleteObservationRecord(any()) } returns Unit

        // when trying to synchronize an observation record from ID
        val result =
            synchronizeObservationRecordUseCase.run(SynchronizeObservationRecordUseCase.Params(expectedObservationRecordToSend))

        // then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ObservationRecordException.SynchronizeException)
        assertEquals(
            expectedObservationRecordToSend.internalId,
            (result.exceptionOrNull() as ObservationRecordException.SynchronizeException).id
        )
        coVerifySequence {
            observationRecordRemoteDataSource.setBaseUrl(appSettings.dataSyncSettings?.geoNatureServerUrl!!)
            observationRecordRemoteDataSource.sendObservationRecord(
                expectedObservationRecordToSend,
                appSettings
            )
            mediaRecordRepository.synchronizeMediaFiles(expectedObservationRecordToSend.taxa.taxa[0])
            observationRecordRemoteDataSource.sendTaxaRecords(
                expectedObservationRecordToSend.copy(id = 1234L),
                appSettings
            )
            mediaRecordRepository.deleteAllMediaFiles(expectedObservationRecordToSend.taxa.taxa[0])
            observationRecordRemoteDataSource.deleteObservationRecord(expectedObservationRecordToSend.copy(id = 1234L))
        }
        coVerify(inverse = true) {
            observationRecordRepository.delete(expectedObservationRecordToSend.internalId)
        }
        confirmVerified(observationRecordRepository)
        confirmVerified(observationRecordRemoteDataSource)
    }
}