package fr.geonature.occtax.settings.io

import fr.geonature.commons.settings.io.AppSettingsJsonReader
import fr.geonature.maps.settings.LayerSettings
import fr.geonature.maps.settings.MapSettings
import fr.geonature.occtax.FixtureHelper.getFixture
import fr.geonature.occtax.settings.AppSettings
import fr.geonature.occtax.settings.InputDateSettings
import fr.geonature.occtax.settings.InputSettings
import fr.geonature.occtax.settings.NomenclatureSettings
import fr.geonature.occtax.settings.PropertySettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [AppSettingsJsonReader].
 *
 * @author S. Grimault
 */
@RunWith(RobolectricTestRunner::class)
class AppSettingsJsonReaderTest {
    private lateinit var appSettingsJsonReader: AppSettingsJsonReader<AppSettings>

    @Before
    fun setUp() {
        appSettingsJsonReader = AppSettingsJsonReader(OnAppSettingsJsonReaderListenerImpl())
    }

    @Test
    fun `should read app settings from valid JSON file`() {
        // given a JSON settings
        val json = getFixture("settings_occtax.json")

        // when read the JSON as AppSettings
        val appSettings = appSettingsJsonReader.read(json)

        // then
        assertNotNull(appSettings)
        assertEquals(
            AppSettings(
                areaObservationDuration = 365,
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
    fun `should fail to read app settings from invalid empty JSON`() {
        // when read an invalid JSON as AppSettings
        val appSettings = appSettingsJsonReader.read("")

        // then
        assertNull(appSettings)
    }

    @Test
    fun `should read app settings from JSON with no map settings`() {
        // when read an empty JSON as AppSettings
        val appSettings = appSettingsJsonReader.read("{\"map\":null}")

        // then
        assertNotNull(appSettings)
        assertNull(appSettings?.mapSettings)
    }

    @Test
    fun `should read app settings from JSON with no input settings`() {
        // when read an empty JSON as AppSettings
        val appSettings = appSettingsJsonReader.read("{\"input\":null}")

        // then
        assertNotNull(appSettings)
        assertEquals(
            InputSettings(dateSettings = InputDateSettings.DEFAULT),
            appSettings?.inputSettings
        )
    }

    @Test
    fun `should read app settings from JSON with input settings and no date settings`() {
        assertEquals(
            InputSettings(dateSettings = InputDateSettings.DEFAULT),
            appSettingsJsonReader.read("{\"input\":{\"date\":null}}")?.inputSettings
        )
        assertEquals(
            InputSettings(dateSettings = InputDateSettings.DEFAULT),
            appSettingsJsonReader.read("{\"input\":{\"date\":{\"enable_end_date\":null,\"enable_hours\":null}}}")?.inputSettings
        )
    }

    @Test
    fun `should read app settings from JSON with input settings and invalid date settings`() {
        assertEquals(
            InputSettings(dateSettings = InputDateSettings.DEFAULT),
            appSettingsJsonReader.read("{\"input\":{\"date\":{\"enable_end_date\":\"no_such_settings\",\"enable_hours\":\"no_such_settings\"}}}")?.inputSettings
        )
        assertEquals(
            InputSettings(
                dateSettings = InputDateSettings(
                    startDateSettings = InputDateSettings.DateSettings.DATE,
                    endDateSettings = InputDateSettings.DateSettings.DATE
                )
            ),
            appSettingsJsonReader.read("{\"input\":{\"date\":{\"enable_end_date\":true,\"enable_hours\":\"no_such_settings\"}}}")?.inputSettings
        )
        assertEquals(
            InputSettings(
                dateSettings = InputDateSettings(
                    startDateSettings = InputDateSettings.DateSettings.DATETIME
                )
            ),
            appSettingsJsonReader.read("{\"input\":{\"date\":{\"enable_end_date\":\"no_such_settings\",\"enable_hours\":true}}}")?.inputSettings
        )
    }

    @Test
    fun `should read app settings from JSON with input settings and valid date settings`() {
        assertEquals(
            InputSettings(
                dateSettings = InputDateSettings(
                    startDateSettings = InputDateSettings.DateSettings.DATE
                )
            ),
            appSettingsJsonReader.read("{\"input\":{\"date\":{\"enable_end_date\":false,\"enable_hours\":false}}}")?.inputSettings
        )
        assertEquals(
            InputSettings(
                dateSettings = InputDateSettings(
                    startDateSettings = InputDateSettings.DateSettings.DATETIME
                )
            ),
            appSettingsJsonReader.read("{\"input\":{\"date\":{\"enable_end_date\":false,\"enable_hours\":true}}}")?.inputSettings
        )
        assertEquals(
            InputSettings(
                dateSettings = InputDateSettings(
                    startDateSettings = InputDateSettings.DateSettings.DATE,
                    endDateSettings = InputDateSettings.DateSettings.DATE
                )
            ),
            appSettingsJsonReader.read("{\"input\":{\"date\":{\"enable_end_date\":true,\"enable_hours\":false}}}")?.inputSettings
        )
        assertEquals(
            InputSettings(
                dateSettings = InputDateSettings(
                    startDateSettings = InputDateSettings.DateSettings.DATETIME,
                    endDateSettings = InputDateSettings.DateSettings.DATETIME
                )
            ),
            appSettingsJsonReader.read("{\"input\":{\"date\":{\"enable_end_date\":true,\"enable_hours\":true}}}")?.inputSettings
        )
    }

    @Test
    fun `should read app settings from JSON with no nomenclature settings`() {
        // when read an empty JSON as AppSettings
        val appSettings = appSettingsJsonReader.read("{\"nomenclature\":null}")

        // then
        assertNotNull(appSettings)
        assertNull(appSettings?.nomenclatureSettings)
    }
}
