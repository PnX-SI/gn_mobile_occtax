package fr.geonature.occtax.features.settings.data

import android.content.Context
import android.os.Environment
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import fr.geonature.commons.util.getFile
import fr.geonature.commons.util.getPrimaryExternalStorage
import fr.geonature.datasync.settings.DataSyncSettings
import fr.geonature.maps.settings.LayerSettings
import fr.geonature.maps.settings.MapSettings
import fr.geonature.occtax.CoroutineTestRule
import fr.geonature.occtax.FixtureHelper
import fr.geonature.occtax.features.settings.domain.AppSettings
import fr.geonature.occtax.features.settings.domain.InputDateSettings
import fr.geonature.occtax.features.settings.domain.InputSettings
import fr.geonature.occtax.features.settings.domain.NomenclatureSettings
import fr.geonature.occtax.features.settings.domain.PropertySettings
import fr.geonature.occtax.features.settings.error.AppSettingsException
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.robolectric.RobolectricTestRunner
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

    @get:Rule
    var temporaryFolder: TemporaryFolder = TemporaryFolder()

    private lateinit var context: Context
    private lateinit var appSettingsLocalDataSource: IAppSettingsLocalDataSource
    private lateinit var appSettingsFilename: String

    private lateinit var primaryExternalStorage: File
    private lateinit var internalFilesDir: File

    @Before
    fun setUp() {
        primaryExternalStorage = temporaryFolder.newFolder("primary_external_storage")
        internalFilesDir = temporaryFolder.newFolder("internal_files_dir")

        context = buildMockContext(
            primaryExternalStorage,
            internalFilesDir
        )

        mockkStatic(Environment::class)
        every { Environment.getExternalStorageDirectory() } returns primaryExternalStorage
        // default: external storage mounted
        every { Environment.getExternalStorageState() } returns Environment.MEDIA_MOUNTED
        every { Environment.getExternalStorageState(any()) } returns Environment.MEDIA_MOUNTED

        appSettingsFilename = "settings_occtax.json"
        appSettingsLocalDataSource = AppSettingsFileLocalDataSourceImpl(
            context,
            appSettingsFilename
        )
    }

    @After
    fun tearDown() {
        unmockkStatic(Environment::class)
    }

    @Test
    fun `should load default app settings`() = runTest {
        // given some existing valid JSON settings from storage
        context.getPrimaryExternalStorage()
            .apply {
                FixtureHelper.getFixtureAsFile(appSettingsFilename)
                    .copyTo(
                        getFile(appSettingsFilename),
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
                    taxHubServerUrl = "https://demo.geonature.fr/geonature/api/taxhub",
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
        context
            .getPrimaryExternalStorage()
            .getFile("${appSettingsFilename.substringBeforeLast(".json")}.local.json")
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
                    context
                        .getPrimaryExternalStorage()
                        .getFile(appSettingsFilename).absolutePath
                ).message
            )
        }

    /**
     * Builds a mock [Context] whose [Context.getExternalFilesDir] and [Context.filesDir]
     * return the given values.
     */
    private fun buildMockContext(
        externalFilesDir: File?,
        filesDir: File = internalFilesDir,
    ): Context = mockk {
        every { packageName } returns "fr.geonature.sync"
        every { getExternalFilesDir(null) } returns externalFilesDir
        every { this@mockk.filesDir } returns filesDir
    }
}