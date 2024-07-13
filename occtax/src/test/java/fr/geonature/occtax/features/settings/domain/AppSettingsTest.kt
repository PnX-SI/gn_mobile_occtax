package fr.geonature.occtax.features.settings.domain

import android.os.Parcel
import fr.geonature.datasync.settings.DataSyncSettings
import fr.geonature.maps.settings.LayerSettings
import fr.geonature.maps.settings.MapSettings
import kotlinx.parcelize.parcelableCreator
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.robolectric.RobolectricTestRunner
import kotlin.time.DurationUnit
import kotlin.time.toDuration

/**
 * Unit tests about [AppSettings].
 *
 * @author S. Grimault
 */
@RunWith(RobolectricTestRunner::class)
class AppSettingsTest {

    @Test
    fun `should build AppSettings from Builder`() {
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
            AppSettings.Builder()
                .areaObservationDuration(365)
                .dataSyncSettings(
                    DataSyncSettings
                        .Builder()
                        .serverUrls(
                            geoNatureServerUrl = "https://demo.geonature.fr/geonature",
                            taxHubServerUrl = "https://demo.geonature.fr/taxhub"
                        )
                        .applicationId(3)
                        .usersListId(1)
                        .taxrefListId(100)
                        .codeAreaType("M10")
                        .pageSize(1000)
                        .dataSyncPeriodicity(
                            dataSyncPeriodicity = 30.toDuration(DurationUnit.MINUTES),
                            essentialDataSyncPeriodicity = 20.toDuration(DurationUnit.MINUTES)
                        )
                        .build()
                )
                .mapSettings(
                    MapSettings.Builder()
                        .showScale(true)
                        .showCompass(true)
                        .zoom(10.0)
                        .minZoomLevel(8.0)
                        .maxZoomLevel(19.0)
                        .minZoomEditing(12.0)
                        .maxBounds(
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
                        )
                        .center(
                            GeoPoint(
                                47.225827,
                                -1.554470
                            )
                        )
                        .addLayer(
                            LayerSettings(
                                "Nantes",
                                listOf("nantes.mbtiles")
                            )
                        )
                        .build()
                )
                .inputSettings(InputSettings(dateSettings = InputDateSettings.DEFAULT))
                .nomenclatureSettings(
                    NomenclatureSettings(
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
                .build()
        )
    }

    @Test
    fun `should copy AppSettings instance from Builder`() {
        // given a AppSettings instance to copy
        val expectedAppSettings = AppSettings(
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

        // when create a copy of this instance from Builder
        val appSettingsCopied = AppSettings
            .Builder()
            .from(expectedAppSettings)
            .build()

        // then
        assertEquals(
            expectedAppSettings,
            appSettingsCopied
        )
    }

    @Test
    fun `should create AppSettings from Parcelable`() {
        // given an AppSettings instance
        val expectedAppSettings = AppSettings(
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

        // when we obtain a Parcel object to write the AppSettings instance to it
        val parcel = Parcel.obtain()
        expectedAppSettings.writeToParcel(
            parcel,
            0
        )

        // reset the parcel for reading
        parcel.setDataPosition(0)

        // then
        assertEquals(
            expectedAppSettings,
            parcelableCreator<AppSettings>().createFromParcel(parcel)
        )
    }
}