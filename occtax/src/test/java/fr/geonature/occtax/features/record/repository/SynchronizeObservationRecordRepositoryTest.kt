package fr.geonature.occtax.features.record.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import fr.geonature.commons.data.entity.Taxon
import fr.geonature.commons.data.entity.Taxonomy
import fr.geonature.commons.features.nomenclature.data.INomenclatureLocalDataSource
import fr.geonature.commons.util.add
import fr.geonature.datasync.api.IGeoNatureAPIClient
import fr.geonature.datasync.api.model.AuthLogin
import fr.geonature.datasync.api.model.AuthUser
import fr.geonature.datasync.auth.IAuthManager
import fr.geonature.datasync.auth.error.AuthException
import fr.geonature.datasync.settings.DataSyncSettings
import fr.geonature.datasync.settings.error.DataSyncSettingsNotFoundException
import fr.geonature.maps.settings.LayerSettings
import fr.geonature.maps.settings.MapSettings
import fr.geonature.occtax.CoroutineTestRule
import fr.geonature.occtax.features.record.data.IMediaRecordLocalDataSource
import fr.geonature.occtax.features.record.data.IObservationRecordLocalDataSource
import fr.geonature.occtax.features.record.data.IObservationRecordRemoteDataSource
import fr.geonature.occtax.features.record.domain.ObservationRecord
import fr.geonature.occtax.features.record.domain.PropertyValue
import fr.geonature.occtax.features.record.error.ObservationRecordException
import fr.geonature.occtax.features.settings.domain.AppSettings
import fr.geonature.occtax.features.settings.domain.InputDateSettings
import fr.geonature.occtax.features.settings.domain.InputSettings
import fr.geonature.occtax.features.settings.error.AppSettingsException
import fr.geonature.occtax.features.settings.repository.IAppSettingsRepository
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
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.Calendar
import java.util.Date
import kotlin.time.DurationUnit
import kotlin.time.toDuration

/**
 * Unit tests about [ISynchronizeObservationRecordRepository].
 *
 * @author S. Grimault
 */
@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class SynchronizeObservationRecordRepositoryTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    @MockK
    private lateinit var authManager: IAuthManager

    @MockK
    private lateinit var geoNatureAPIClient: IGeoNatureAPIClient

    @MockK
    private lateinit var appSettingsRepository: IAppSettingsRepository

    @MockK
    private lateinit var nomenclatureLocalDataSource: INomenclatureLocalDataSource

    @MockK
    private lateinit var observationRecordLocalDataSource: IObservationRecordLocalDataSource

    @MockK
    private lateinit var observationRecordRemoteDataSource: IObservationRecordRemoteDataSource

    @MockK
    private lateinit var mediaRecordLocalDataSource: IMediaRecordLocalDataSource

    private lateinit var synchronizeObservationRecordRepository: ISynchronizeObservationRecordRepository

    @Before
    fun setUp() {
        init(this)

        synchronizeObservationRecordRepository = SynchronizeObservationRecordRepositoryImpl(
            ApplicationProvider.getApplicationContext(),
            geoNatureAPIClient,
            authManager,
            appSettingsRepository,
            nomenclatureLocalDataSource,
            observationRecordLocalDataSource,
            observationRecordRemoteDataSource,
            mediaRecordLocalDataSource
        )
    }

    @Test
    fun `should return a NotConnectedException failure if no login information was found from auth manager`() =
        runTest {
            // given no login information from auth manager
            coEvery { authManager.getAuthLogin() } returns null

            // when trying to synchronize an observation record from ID
            val result = synchronizeObservationRecordRepository.synchronize(
                ObservationRecord(internalId = 1240L).apply {
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
            )

            // then
            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is AuthException.NotConnectedException)
        }

    @Test
    fun `should return a NoAppSettingsFoundLocallyException failure if no app settings was found`() =
        runTest {
            // given some login information from auth manager
            coEvery { authManager.getAuthLogin() } returns AuthLogin(
                AuthUser(
                    1234,
                    "Grimault",
                    "Sebastien",
                    2,
                    8,
                    "sgr"
                ),
                Date().add(
                    Calendar.HOUR,
                    1
                )
            )

            // and no app settings
            coEvery {
                appSettingsRepository.loadAppSettings()
            } returns Result.failure(
                AppSettingsException.NoAppSettingsFoundLocallyException("/absolute/path/to/settings.json ")
            )

            // when trying to synchronize an observation record from ID
            val result = synchronizeObservationRecordRepository.synchronize(
                ObservationRecord(internalId = 1240L).apply {
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
            )

            // then
            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is AppSettingsException.NoAppSettingsFoundLocallyException)
        }

    @Test
    fun `should return a DataSyncSettingsNotFoundException failure if no data sync settings was found`() =
        runTest {
            // given some login information from auth manager
            coEvery { authManager.getAuthLogin() } returns AuthLogin(
                AuthUser(
                    1234,
                    "Grimault",
                    "Sebastien",
                    2,
                    8,
                    "sgr"
                ),
                Date().add(
                    Calendar.HOUR,
                    1
                )
            )

            // and an incomplete app settings
            coEvery { appSettingsRepository.loadAppSettings() } returns Result.failure(AppSettingsException.MissingAttributeException("sync"))

            // when trying to synchronize an observation record from ID
            val result = synchronizeObservationRecordRepository.synchronize(
                ObservationRecord(internalId = 1240L).apply {
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
            )

            // then
            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is DataSyncSettingsNotFoundException)
        }

    @Test
    fun `should return an InvalidStatusException failure if trying to send an observation record with wrong status`() =
        runTest {
            // given some login information from auth manager
            coEvery { authManager.getAuthLogin() } returns AuthLogin(
                AuthUser(
                    1234,
                    "Grimault",
                    "Sebastien",
                    2,
                    8,
                    "sgr"
                ),
                Date().add(
                    Calendar.HOUR,
                    1
                )
            )

            // and an app settings
            val appSettings = AppSettings(
                dataSyncSettings = DataSyncSettings(
                    geoNatureServerUrl = "https://demo.geonature.fr/geonature",
                    taxHubServerUrl = "https://demo.geonature.fr/taxhub",
                    applicationId = 3,
                    usersListId = 1,
                    taxrefListId = 100,
                    codeAreaType = "M10",
                    pageSize = 1000,
                    dataSyncPeriodicity = 30.toDuration(DurationUnit.MINUTES),
                    essentialDataSyncPeriodicity = 20.toDuration(DurationUnit.MINUTES)
                ),
                mapSettings = MapSettings.Builder()
                    .addLayer(
                        LayerSettings.Builder.newInstance()
                            .label("OSM")
                            .addSource("https://a.tile.openstreetmap.org")
                            .build()
                    )
                    .build(),
                inputSettings = InputSettings(
                    dateSettings = InputDateSettings(
                        startDateSettings = InputDateSettings.DateSettings.DATETIME,
                        endDateSettings = InputDateSettings.DateSettings.DATETIME
                    )
                )
            )
            coEvery { appSettingsRepository.loadAppSettings() } returns Result.success(appSettings)
            coEvery {
                observationRecordRemoteDataSource.setBaseUrl(
                    appSettings.dataSyncSettings.geoNatureServerUrl
                )
            } returns Unit

            // and an existing observation record with wrong status
            val observationRecord = ObservationRecord(internalId = 1240L).apply {
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
            val result = synchronizeObservationRecordRepository.synchronize(observationRecord)

            // then
            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is ObservationRecordException.InvalidStatusException)
        }

    @Test
    fun `should synchronize successfully an observation record with all taxa`() = runTest {
        // given some login information from auth manager
        coEvery { authManager.getAuthLogin() } returns AuthLogin(
            AuthUser(
                1234,
                "Grimault",
                "Sebastien",
                2,
                8,
                "sgr"
            ),
            Date().add(
                Calendar.HOUR,
                1
            )
        )

        // and an app settings
        val appSettings = AppSettings(
            dataSyncSettings = DataSyncSettings(
                geoNatureServerUrl = "https://demo.geonature.fr/geonature",
                taxHubServerUrl = "https://demo.geonature.fr/taxhub",
                applicationId = 3,
                usersListId = 1,
                taxrefListId = 100,
                codeAreaType = "M10",
                pageSize = 1000,
                dataSyncPeriodicity = 30.toDuration(DurationUnit.MINUTES),
                essentialDataSyncPeriodicity = 20.toDuration(DurationUnit.MINUTES)
            ),
            mapSettings = MapSettings.Builder()
                .addLayer(
                    LayerSettings.Builder.newInstance()
                        .label("OSM")
                        .addSource("https://a.tile.openstreetmap.org")
                        .build()
                )
                .build(),
            inputSettings = InputSettings(
                dateSettings = InputDateSettings(
                    startDateSettings = InputDateSettings.DateSettings.DATETIME,
                    endDateSettings = InputDateSettings.DateSettings.DATETIME
                )
            )
        )
        coEvery { appSettingsRepository.loadAppSettings() } returns Result.success(appSettings)
        coEvery {
            observationRecordRemoteDataSource.setBaseUrl(
                appSettings.dataSyncSettings.geoNatureServerUrl
            )
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
        coEvery {
            observationRecordRemoteDataSource.sendTaxaRecords(
                any(),
                any()
            )
        } answers { firstArg() }
        coEvery { observationRecordRemoteDataSource.deleteObservationRecord(any()) } returns Unit
        coEvery {
            observationRecordLocalDataSource.delete(expectedObservationRecordToSend.internalId)
        } returns expectedObservationRecordToSend

        // when trying to synchronize an observation record from ID
        val result =
            synchronizeObservationRecordRepository.synchronize(expectedObservationRecordToSend)

        // then
        assertTrue(result.isSuccess)
        coVerifySequence {
            observationRecordRemoteDataSource.setBaseUrl(
                appSettings.dataSyncSettings.geoNatureServerUrl
            )
            observationRecordRemoteDataSource.sendObservationRecord(
                expectedObservationRecordToSend,
                appSettings
            )
            observationRecordRemoteDataSource.sendTaxaRecords(
                expectedObservationRecordToSend.copy(id = 1234L),
                appSettings
            )
            observationRecordLocalDataSource.delete(expectedObservationRecordToSend.internalId)
        }
        coVerify(inverse = true) {
            observationRecordRemoteDataSource.deleteObservationRecord(any())
        }
        confirmVerified(observationRecordLocalDataSource)
        confirmVerified(observationRecordRemoteDataSource)
    }

    @Test
    fun `should delete remotely a partially synchronized observation record`() = runTest {
        // given some login information from auth manager
        coEvery { authManager.getAuthLogin() } returns AuthLogin(
            AuthUser(
                1234,
                "Grimault",
                "Sebastien",
                2,
                8,
                "sgr"
            ),
            Date().add(
                Calendar.HOUR,
                1
            )
        )

        // and an app settings
        val appSettings = AppSettings(
            dataSyncSettings = DataSyncSettings(
                geoNatureServerUrl = "https://demo.geonature.fr/geonature",
                taxHubServerUrl = "https://demo.geonature.fr/taxhub",
                applicationId = 3,
                usersListId = 1,
                taxrefListId = 100,
                codeAreaType = "M10",
                pageSize = 1000,
                dataSyncPeriodicity = 30.toDuration(DurationUnit.MINUTES),
                essentialDataSyncPeriodicity = 20.toDuration(DurationUnit.MINUTES)
            ),
            mapSettings = MapSettings.Builder()
                .addLayer(
                    LayerSettings.Builder.newInstance()
                        .label("OSM")
                        .addSource("https://a.tile.openstreetmap.org")
                        .build()
                )
                .build(),
            inputSettings = InputSettings(
                dateSettings = InputDateSettings(
                    startDateSettings = InputDateSettings.DateSettings.DATETIME,
                    endDateSettings = InputDateSettings.DateSettings.DATETIME
                )
            )
        )
        coEvery { appSettingsRepository.loadAppSettings() } returns Result.success(appSettings)
        coEvery {
            observationRecordRemoteDataSource.setBaseUrl(
                appSettings.dataSyncSettings.geoNatureServerUrl
            )
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
        // and some error occurred when synchronizing all taxa records
        coEvery {
            observationRecordRemoteDataSource.sendTaxaRecords(
                any(),
                any()
            )
        } answers {
            throw ObservationRecordException.SynchronizeException(
                firstArg<ObservationRecord>().internalId
            )
        }
        coEvery { observationRecordRemoteDataSource.deleteObservationRecord(any()) } returns Unit

        // when trying to synchronize an observation record from ID
        val result = synchronizeObservationRecordRepository.synchronize(
            expectedObservationRecordToSend
        )

        // then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ObservationRecordException.SynchronizeException)
        assertEquals(
            expectedObservationRecordToSend.internalId,
            (result.exceptionOrNull() as ObservationRecordException.SynchronizeException).id
        )
        coVerifySequence {
            observationRecordRemoteDataSource.setBaseUrl(
                appSettings.dataSyncSettings.geoNatureServerUrl
            )
            observationRecordRemoteDataSource.sendObservationRecord(
                expectedObservationRecordToSend,
                appSettings
            )
            observationRecordRemoteDataSource.sendTaxaRecords(
                expectedObservationRecordToSend.copy(id = 1234L),
                appSettings
            )
            observationRecordRemoteDataSource.deleteObservationRecord(
                expectedObservationRecordToSend.copy(id = 1234L)
            )
        }
        coVerify(inverse = true) {
            observationRecordLocalDataSource.delete(expectedObservationRecordToSend.internalId)
        }
        confirmVerified(observationRecordLocalDataSource)
        confirmVerified(observationRecordRemoteDataSource)
    }
}