package fr.geonature.occtax.features.record.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import fr.geonature.commons.data.entity.Taxon
import fr.geonature.commons.data.entity.Taxonomy
import fr.geonature.commons.settings.IAppSettingsManager
import fr.geonature.commons.settings.error.AppSettingsException
import fr.geonature.commons.util.add
import fr.geonature.datasync.api.model.AuthLogin
import fr.geonature.datasync.api.model.AuthUser
import fr.geonature.datasync.auth.IAuthManager
import fr.geonature.datasync.auth.error.AuthException
import fr.geonature.datasync.packageinfo.ISynchronizeObservationRecordRepository
import fr.geonature.datasync.settings.DataSyncSettings
import fr.geonature.datasync.settings.error.DataSyncSettingsNotFoundException
import fr.geonature.occtax.CoroutineTestRule
import fr.geonature.occtax.features.record.data.IObservationRecordDataSource
import fr.geonature.occtax.features.record.data.IObservationRecordRemoteDataSource
import fr.geonature.occtax.features.record.domain.ObservationRecord
import fr.geonature.occtax.features.record.domain.PropertyValue
import fr.geonature.occtax.features.record.error.ObservationRecordException
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
import java.util.Calendar
import java.util.Date

/**
 * Unit tests about [ISynchronizeObservationRecordRepository].
 *
 * @author S. Grimault
 */
@ExperimentalCoroutinesApi
class SynchronizeObservationRecordRepositoryTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    @MockK
    private lateinit var authManager: IAuthManager

    @MockK
    private lateinit var appSettingsManager: IAppSettingsManager<AppSettings>

    @MockK
    private lateinit var observationRecordDataSource: IObservationRecordDataSource

    @MockK
    private lateinit var observationRecordRemoteDataSource: IObservationRecordRemoteDataSource

    private lateinit var synchronizeObservationRecordRepository: ISynchronizeObservationRecordRepository

    @Before
    fun setUp() {
        init(this)

        synchronizeObservationRecordRepository = SynchronizeObservationRecordRepositoryImpl(
            authManager,
            appSettingsManager,
            observationRecordDataSource,
            observationRecordRemoteDataSource
        )
    }

    @Test
    fun `should return a NotConnectedException failure if no login information was found from auth manager`() =
        runTest {
            // given no login information from auth manager
            coEvery { authManager.getAuthLogin() } returns null

            // when trying to synchronize an observation record from ID
            val result = synchronizeObservationRecordRepository(1234L)

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
            coEvery { appSettingsManager.loadAppSettings() } returns null

            // when trying to synchronize an observation record from ID
            val result = synchronizeObservationRecordRepository(1234L)

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
            coEvery { appSettingsManager.loadAppSettings() } returns AppSettings(
                inputSettings = InputSettings(
                    dateSettings = InputDateSettings(
                        startDateSettings = InputDateSettings.DateSettings.DATETIME,
                        endDateSettings = InputDateSettings.DateSettings.DATETIME
                    )
                )
            )

            // when trying to synchronize an observation record from ID
            val result = synchronizeObservationRecordRepository(1234L)

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
            coEvery { observationRecordDataSource.read(1240L) } returns ObservationRecord(internalId = 1240L).apply {
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
            val result = synchronizeObservationRecordRepository(1240L)

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
        coEvery { observationRecordDataSource.read(1240L) } returns expectedObservationRecordToSend
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
        coEvery { observationRecordDataSource.delete(expectedObservationRecordToSend.internalId) } returns expectedObservationRecordToSend

        // when trying to synchronize an observation record from ID
        val result = synchronizeObservationRecordRepository(1240L)

        // then
        assertTrue(result.isSuccess)
        coVerifySequence {
            observationRecordRemoteDataSource.setBaseUrl(appSettings.dataSyncSettings?.geoNatureServerUrl!!)
            observationRecordDataSource.read(expectedObservationRecordToSend.internalId)
            observationRecordRemoteDataSource.sendObservationRecord(
                expectedObservationRecordToSend,
                appSettings
            )
            observationRecordRemoteDataSource.sendTaxaRecords(
                expectedObservationRecordToSend.copy(id = 1234L),
                appSettings
            )
            observationRecordDataSource.delete(expectedObservationRecordToSend.internalId)
        }
        coVerify(inverse = true) {
            observationRecordRemoteDataSource.deleteObservationRecord(any())
        }
        confirmVerified(observationRecordDataSource)
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

        coEvery { observationRecordDataSource.read(1240L) } returns expectedObservationRecordToSend
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
            throw ObservationRecordException.SynchronizeException(firstArg<ObservationRecord>().internalId)
        }
        coEvery { observationRecordRemoteDataSource.deleteObservationRecord(any()) } returns Unit

        // when trying to synchronize an observation record from ID
        val result = synchronizeObservationRecordRepository(1240L)

        // then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ObservationRecordException.SynchronizeException)
        assertEquals(
            expectedObservationRecordToSend.internalId,
            (result.exceptionOrNull() as ObservationRecordException.SynchronizeException).id
        )
        coVerifySequence {
            observationRecordRemoteDataSource.setBaseUrl(appSettings.dataSyncSettings?.geoNatureServerUrl!!)
            observationRecordDataSource.read(expectedObservationRecordToSend.internalId)
            observationRecordRemoteDataSource.sendObservationRecord(
                expectedObservationRecordToSend,
                appSettings
            )
            observationRecordRemoteDataSource.sendTaxaRecords(
                expectedObservationRecordToSend.copy(id = 1234L),
                appSettings
            )
            observationRecordRemoteDataSource.deleteObservationRecord(expectedObservationRecordToSend.copy(id = 1234L))
        }
        coVerify(inverse = true) {
            observationRecordDataSource.delete(expectedObservationRecordToSend.internalId)
        }
        confirmVerified(observationRecordDataSource)
        confirmVerified(observationRecordRemoteDataSource)
    }
}