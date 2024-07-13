package fr.geonature.occtax.features.settings.data

import android.app.Application
import android.os.Environment
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import fr.geonature.datasync.settings.DataSyncSettings
import fr.geonature.maps.settings.LayerSettings
import fr.geonature.maps.settings.MapSettings
import fr.geonature.mountpoint.model.MountPoint
import fr.geonature.mountpoint.util.FileUtils.getFile
import fr.geonature.mountpoint.util.FileUtils.getRootFolder
import fr.geonature.occtax.CoroutineTestRule
import fr.geonature.occtax.FixtureHelper
import fr.geonature.occtax.features.settings.domain.AppSettings
import fr.geonature.occtax.features.settings.domain.InputDateSettings
import fr.geonature.occtax.features.settings.domain.InputSettings
import fr.geonature.occtax.features.settings.domain.NomenclatureSettings
import fr.geonature.occtax.features.settings.domain.PropertySettings
import fr.geonature.occtax.features.settings.error.AppSettingsException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowEnvironment
import java.io.File
import kotlin.time.DurationUnit
import kotlin.time.toDuration

/**
 * Unit tests about [IAppSettingsLocalDataSource].
 *
 * @author S. Grimault
 */
@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class AppSettingsFileLocalDataSourceTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private lateinit var application: Application
    private lateinit var appSettingsLocalDataSource: IAppSettingsLocalDataSource
    private lateinit var appSettingsFilename: String

    @Before
    fun setUp() {
        application = ApplicationProvider.getApplicationContext()
        ShadowEnvironment.setExternalStorageState(
            File("/"),
            Environment.MEDIA_MOUNTED
        )
        appSettingsFilename = "settings_occtax.json"
        appSettingsLocalDataSource = AppSettingsFileLocalDataSourceImpl(
            application,
            appSettingsFilename
        )
    }

    @Test
    fun `should load default app settings`() = runTest {
        // given some existing valid JSON settings from storage
        getRootFolder(
            application,
            MountPoint.StorageType.INTERNAL
        )
            .apply {
                FixtureHelper.getFixtureAsFile(appSettingsFilename)
                    .copyTo(
                        getFile(
                            this,
                            appSettingsFilename
                        ),
                        overwrite = true
                    )
            }

        // when loading app settings from local data source
        val appSettings = appSettingsLocalDataSource.load()

        // then
        assertEquals(
            AppSettings(
                areaObservationDuration = 365,
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
                mapSettings = MapSettings(
                    arrayListOf(
                        LayerSettings(
                            "Nantes",
                            listOf("nantes.mbtiles")
                        )
                    ),
                    null,
                    showScale = true,
                    showCompass = true,
                    zoom = 10.0,
                    minZoomLevel = 8.0,
                    maxZoomLevel = 19.0,
                    minZoomEditing = 12.0,
                    maxBounds = BoundingBox.fromGeoPoints(
                        arrayListOf(
                            GeoPoint(
                                47.253369,
                                -1.605721
                            ),
                            GeoPoint(
                                47.173845,
                                -1.482811
                            )
                        )
                    ),
                    center = GeoPoint(
                        47.225827,
                        -1.554470
                    )
                ),
                inputSettings = InputSettings(dateSettings = InputDateSettings.DEFAULT),
                nomenclatureSettings = NomenclatureSettings(
                    saveDefaultValues = true,
                    withAdditionalFields = true,
                    information = arrayListOf(
                        PropertySettings(
                            "METH_OBS",
                            visible = true,
                            default = true
                        ),
                        PropertySettings(
                            "ETA_BIO",
                            visible = true,
                            default = true
                        ),
                        PropertySettings(
                            "METH_DETERMIN",
                            visible = true,
                            default = false
                        ),
                        PropertySettings(
                            "STATUT_BIO",
                            visible = true,
                            default = false
                        ),
                        PropertySettings(
                            "NATURALITE",
                            visible = true,
                            default = false
                        ),
                        PropertySettings(
                            "PREUVE_EXIST",
                            visible = true,
                            default = false
                        )
                    ),
                    counting = arrayListOf(
                        PropertySettings(
                            "STADE_VIE",
                            visible = true,
                            default = true
                        ),
                        PropertySettings(
                            "SEXE",
                            visible = true,
                            default = true
                        ),
                        PropertySettings(
                            "OBJ_DENBR",
                            visible = true,
                            default = true
                        ),
                        PropertySettings(
                            "TYP_DENBR",
                            visible = true,
                            default = true
                        )
                    )
                )
            ),
            appSettings
        )
    }

    @Test
    fun `should load app settings from existing one`() = runTest {
        // given additional app settings from storage
        getFile(
            getRootFolder(
                application,
                MountPoint.StorageType.INTERNAL
            ).apply { mkdirs() },
            "${appSettingsFilename.substringBeforeLast(".json")}.local.json"
        )
            .apply {
                writeText(
                    """{
                        "map": {
                            "layers": [
                                {
                                  "label": "OSM",
                                  "source": "https://a.tile.openstreetmap.org"
                                }
                            ]
                        }
                    }""".trimIndent()
                )
            }

        // when loading app settings from local data source using existing one
        val appSettings = appSettingsLocalDataSource.load(
            AppSettings(
                areaObservationDuration = 365,
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
                mapSettings = MapSettings(
                    arrayListOf(
                        LayerSettings(
                            "Nantes",
                            listOf("nantes.mbtiles")
                        )
                    ),
                    null,
                    showScale = true,
                    showCompass = true,
                    zoom = 10.0,
                    minZoomLevel = 8.0,
                    maxZoomLevel = 19.0,
                    minZoomEditing = 12.0,
                    maxBounds = BoundingBox.fromGeoPoints(
                        arrayListOf(
                            GeoPoint(
                                47.253369,
                                -1.605721
                            ),
                            GeoPoint(
                                47.173845,
                                -1.482811
                            )
                        )
                    ),
                    center = GeoPoint(
                        47.225827,
                        -1.554470
                    )
                ),
                inputSettings = InputSettings(dateSettings = InputDateSettings.DEFAULT),
                nomenclatureSettings = NomenclatureSettings(
                    saveDefaultValues = true,
                    withAdditionalFields = true,
                    information = arrayListOf(
                        PropertySettings(
                            "METH_OBS",
                            visible = true,
                            default = true
                        ),
                        PropertySettings(
                            "ETA_BIO",
                            visible = true,
                            default = true
                        ),
                        PropertySettings(
                            "METH_DETERMIN",
                            visible = true,
                            default = false
                        ),
                        PropertySettings(
                            "STATUT_BIO",
                            visible = true,
                            default = false
                        ),
                        PropertySettings(
                            "NATURALITE",
                            visible = true,
                            default = false
                        ),
                        PropertySettings(
                            "PREUVE_EXIST",
                            visible = true,
                            default = false
                        )
                    ),
                    counting = arrayListOf(
                        PropertySettings(
                            "STADE_VIE",
                            visible = true,
                            default = true
                        ),
                        PropertySettings(
                            "SEXE",
                            visible = true,
                            default = true
                        ),
                        PropertySettings(
                            "OBJ_DENBR",
                            visible = true,
                            default = true
                        ),
                        PropertySettings(
                            "TYP_DENBR",
                            visible = true,
                            default = true
                        )
                    )
                )
            )
        )

        // then
        assertEquals(
            AppSettings(
                areaObservationDuration = 365,
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
                mapSettings = MapSettings(
                    arrayListOf(
                        LayerSettings.Builder.newInstance()
                            .label("OSM")
                            .addSource("https://a.tile.openstreetmap.org")
                            .build()
                    ),
                    null,
                    showScale = true,
                    showCompass = true,
                    zoom = 10.0,
                    minZoomLevel = 8.0,
                    maxZoomLevel = 19.0,
                    minZoomEditing = 12.0,
                    maxBounds = BoundingBox.fromGeoPoints(
                        arrayListOf(
                            GeoPoint(
                                47.253369,
                                -1.605721
                            ),
                            GeoPoint(
                                47.173845,
                                -1.482811
                            )
                        )
                    ),
                    center = GeoPoint(
                        47.225827,
                        -1.554470
                    )
                ),
                inputSettings = InputSettings(dateSettings = InputDateSettings.DEFAULT),
                nomenclatureSettings = NomenclatureSettings(
                    saveDefaultValues = true,
                    withAdditionalFields = true,
                    information = arrayListOf(
                        PropertySettings(
                            "METH_OBS",
                            visible = true,
                            default = true
                        ),
                        PropertySettings(
                            "ETA_BIO",
                            visible = true,
                            default = true
                        ),
                        PropertySettings(
                            "METH_DETERMIN",
                            visible = true,
                            default = false
                        ),
                        PropertySettings(
                            "STATUT_BIO",
                            visible = true,
                            default = false
                        ),
                        PropertySettings(
                            "NATURALITE",
                            visible = true,
                            default = false
                        ),
                        PropertySettings(
                            "PREUVE_EXIST",
                            visible = true,
                            default = false
                        )
                    ),
                    counting = arrayListOf(
                        PropertySettings(
                            "STADE_VIE",
                            visible = true,
                            default = true
                        ),
                        PropertySettings(
                            "SEXE",
                            visible = true,
                            default = true
                        ),
                        PropertySettings(
                            "OBJ_DENBR",
                            visible = true,
                            default = true
                        ),
                        PropertySettings(
                            "TYP_DENBR",
                            visible = true,
                            default = true
                        )
                    )
                )
            ),
            appSettings
        )
    }

    @Test
    fun `should throw NoAppSettingsFoundLocallyException if trying to read no existing app settings`() =
        runTest {
            val exception = runCatching { appSettingsLocalDataSource.load() }.exceptionOrNull()

            assertTrue(exception is AppSettingsException.NoAppSettingsFoundLocallyException)
            assertEquals(
                (exception as AppSettingsException.NoAppSettingsFoundLocallyException).message,
                AppSettingsException.NoAppSettingsFoundLocallyException(
                    getFile(
                        getRootFolder(
                            application,
                            MountPoint.StorageType.INTERNAL
                        ),
                        appSettingsFilename
                    ).absolutePath
                ).message
            )
        }
}