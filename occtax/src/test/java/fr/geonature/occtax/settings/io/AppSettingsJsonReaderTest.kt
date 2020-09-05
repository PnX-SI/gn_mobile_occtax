package fr.geonature.occtax.settings.io

import fr.geonature.commons.settings.io.AppSettingsJsonReader
import fr.geonature.maps.settings.LayerSettings
import fr.geonature.maps.settings.MapSettings
import fr.geonature.occtax.FixtureHelper.getFixture
import fr.geonature.occtax.settings.AppSettings
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
                365,
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
                ),
                NomenclatureSettings(
                    arrayListOf(
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
                    arrayListOf(
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

    @Test
    fun testReadAppSettingsFromJsonStringWithNoNomenclatureSettings() {
        // when read an empty JSON as AppSettings
        val appSettings = appSettingsJsonReader.read("{\"nomenclature\":null}")

        // then
        assertNotNull(appSettings)
        assertNull(appSettings?.nomenclatureSettings)
    }
}
