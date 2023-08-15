package fr.geonature.occtax.features.settings.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import fr.geonature.datasync.settings.DataSyncSettings
import fr.geonature.maps.settings.LayerSettings
import fr.geonature.maps.settings.MapSettings
import fr.geonature.occtax.CoroutineTestRule
import fr.geonature.occtax.features.settings.data.IAppSettingsLocalDataSource
import fr.geonature.occtax.features.settings.domain.AppSettings
import fr.geonature.occtax.features.settings.error.AppSettingsException
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
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.time.DurationUnit
import kotlin.time.toDuration

/**
 * Unit tests about [IAppSettingsLocalDataSource].
 *
 * @author S. Grimault
 */
@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class AppSettingsRepositoryTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    @MockK
    private lateinit var appSettingsLocalDataSource: IAppSettingsLocalDataSource

    private lateinit var appSettingsRepository: IAppSettingsRepository

    @Before
    fun setUp() {
        init(this)

        appSettingsRepository = AppSettingsRepositoryImpl(appSettingsLocalDataSource)
    }

    @Test
    fun `should load default app settings`() = runTest {
        // given app settings from local data source
        val existingAppSettings = AppSettings(
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
                .build()
        )

        coEvery {
            appSettingsLocalDataSource.load()
        } returns existingAppSettings
        // and no additional settings from user
        coEvery {
            appSettingsLocalDataSource.load(existingAppSettings)
        } throws AppSettingsException.NoAppSettingsFoundLocallyException("/absolute/path/to/settings.json")

        // when loading app settings
        val result = appSettingsRepository.loadAppSettings()

        // then
        assertTrue(result.isSuccess)
        assertEquals(
            existingAppSettings,
            result.getOrThrow()
        )
    }

    @Test
    fun `should load app settings from existing one`() = runTest {
        // given app settings from local data source
        val originalAppSettings = AppSettings(
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
                .build()
        )
        // with additional one
        val expectedAppSettings = AppSettings.Builder()
            .from(originalAppSettings)
            .mapSettings(
                MapSettings.Builder()
                    .from(originalAppSettings.mapSettings)
                    .minZoomLevel(8.0)
                    .maxZoomLevel(13.0)
                    .build()
            )
            .build()

        coEvery {
            appSettingsLocalDataSource.load()
        } returns originalAppSettings
        coEvery {
            appSettingsLocalDataSource.load(originalAppSettings)
        } returns expectedAppSettings

        // when loading app settings
        val result = appSettingsRepository.loadAppSettings()

        // then
        assertTrue(result.isSuccess)
        assertEquals(
            expectedAppSettings,
            result.getOrThrow()
        )
    }

    @Test
    fun `should return a NoAppSettingsFoundLocallyException failure if no app settings was found`() =
        runTest {
            // given no app settings loaded from remote
            coEvery {
                appSettingsLocalDataSource.load()
            } throws AppSettingsException.NoAppSettingsFoundLocallyException("/absolute/path/to/settings.json")

            // when loading app settings
            val result = appSettingsRepository.loadAppSettings()

            // then
            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is AppSettingsException.NoAppSettingsFoundLocallyException)
            assertEquals(
                AppSettingsException.NoAppSettingsFoundLocallyException("/absolute/path/to/settings.json"),
                result.exceptionOrNull()
            )
        }
}