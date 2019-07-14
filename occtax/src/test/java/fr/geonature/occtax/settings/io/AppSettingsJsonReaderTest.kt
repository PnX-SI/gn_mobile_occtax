package fr.geonature.occtax.settings.io

import fr.geonature.commons.settings.io.AppSettingsJsonReader
import fr.geonature.maps.settings.LayerSettings
import fr.geonature.maps.settings.MapSettings
import fr.geonature.occtax.FixtureHelper.getFixture
import fr.geonature.occtax.settings.AppSettings
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
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@RunWith(RobolectricTestRunner::class)
class AppSettingsJsonReaderTest {
    lateinit var appSettingsJsonReader: AppSettingsJsonReader<AppSettings>

    @Before
    fun setUp() {
        appSettingsJsonReader = AppSettingsJsonReader(OnAppSettingsJsonReaderListenerImpl())
    }

    @Test
    fun testReadAppSettingsFromJsonString() {
        // given a JSON settings
        val json = getFixture("settings_occtax.json")

        // when read the JSON as AppSettings
        val appSettings = appSettingsJsonReader.read(json)

        // then
        assertNotNull(appSettings)
        assertEquals(
                AppSettings(
                        MapSettings(
                                arrayListOf(
                                        LayerSettings(
                                                "Nantes",
                                                "nantes.mbtiles"
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
                        )),
                appSettings
        )
    }

    @Test
    fun testReadAppSettingsFromInvalidJsonString() {
        // when read an invalid JSON as AppSettings
        val appSettings = appSettingsJsonReader.read("")

        // then
        assertNull(appSettings)
    }

    @Test
    fun testReadAppSettingsFromJsonStringWithNoMapSettings() {
        // when read an empty JSON as AppSettings
        val appSettings = appSettingsJsonReader.read("{\"map\":null}")

        // then
        assertNotNull(appSettings)
        assertNull(appSettings?.mapSettings)
    }
}