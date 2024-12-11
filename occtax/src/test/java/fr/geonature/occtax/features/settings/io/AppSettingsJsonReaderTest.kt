package fr.geonature.occtax.features.settings.io

import fr.geonature.datasync.settings.DataSyncSettings
import fr.geonature.maps.settings.LayerSettings
import fr.geonature.maps.settings.MapSettings
import fr.geonature.occtax.FixtureHelper.getFixture
import fr.geonature.occtax.features.settings.domain.AppSettings
import fr.geonature.occtax.features.settings.domain.InputDateSettings
import fr.geonature.occtax.features.settings.domain.InputSettings
import fr.geonature.occtax.features.settings.domain.NomenclatureSettings
import fr.geonature.occtax.features.settings.domain.PropertySettings
import fr.geonature.occtax.features.settings.error.AppSettingsException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.robolectric.RobolectricTestRunner
import java.io.StringReader
import kotlin.time.DurationUnit
import kotlin.time.toDuration

/**
 * Unit tests about [AppSettingsJsonReader].
 *
 * @author S. Grimault
 */
@RunWith(RobolectricTestRunner::class)
class AppSettingsJsonReaderTest {

    @Test
    fun `should read app settings from valid JSON file`() {
        // given a JSON settings
        val json = getFixture("settings_occtax.json")

        // when read the JSON as AppSettings
        val appSettings = AppSettingsJsonReader().read(json)

        // then
        assertNotNull(appSettings)
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
    fun `should read app settings from JSON file with unknown properties`() {
        // when read a JSON as AppSettings with unknown properties
        val appSettings = AppSettingsJsonReader().read(
            """{
                "sync": {
                    "geonature_url": "https://demo.geonature.fr/geonature",
                    "taxhub_url": "https://demo.geonature.fr/taxhub",
                    "gn_application_id": 3,
                    "observers_list_id": 1,
                    "taxa_list_id": 100,
                    "code_area_type": "M10",
                    "page_size": 1000,
                    "sync_periodicity_data_essential": "20m",
                    "sync_periodicity_data": "30m"
                },
                "map": {
                    "layers": [
                        {
                          "label": "OSM",
                          "source": "https://a.tile.openstreetmap.org"
                        }
                    ]
                },
                "no_such_property": "no_such_value"
            }""".trimIndent()
        )

        // then
        assertEquals(
            AppSettings(
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
            ),
            appSettings
        )
    }

    @Test
    fun `should throw JsonParseException if trying to read app settings from empty JSON`() {
        assertEquals(
            "End of input",
            assertThrows(
                AppSettingsException.JsonParseException::class.java
            ) {
                AppSettingsJsonReader().read(StringReader(""))
            }.message
        )
    }

    @Test
    fun `should throw MissingAttributeException if trying to read app settings from empty JSON`() {
        assertEquals(
            "missing 'sync' attribute configuration",
            assertThrows(
                AppSettingsException.MissingAttributeException::class.java
            ) {
                AppSettingsJsonReader().read(StringReader("{}"))
            }.message
        )
    }

    @Test
    fun `should throw MissingAttributeException if trying to read app settings from JSON with no 'sync' settings`() {
        assertEquals(
            "missing 'sync' attribute configuration",
            assertThrows(
                AppSettingsException.MissingAttributeException::class.java
            ) {
                AppSettingsJsonReader().read(StringReader("{}"))
            }.message
        )
    }

    @Test
    fun `should read app settings from JSON with no 'map' settings`() {
        assertEquals(
            "missing 'map' attribute configuration",
            assertThrows(
                AppSettingsException.MissingAttributeException::class.java
            ) {
                AppSettingsJsonReader().read(
                    """{
                        "sync": {
                            "geonature_url": "https://demo.geonature.fr/geonature",
                            "taxhub_url": "https://demo.geonature.fr/taxhub",
                            "gn_application_id": 3,
                            "observers_list_id": 1,
                            "taxa_list_id": 100,
                            "code_area_type": "M10",
                            "page_size": 1000,
                            "sync_periodicity_data_essential": "20m",
                            "sync_periodicity_data": "30m"
                        }
                    }""".trimIndent()
                )
            }.message
        )
        assertEquals(
            "Expected BEGIN_OBJECT but was NULL",
            assertThrows(
                AppSettingsException.JsonParseException::class.java
            ) {
                AppSettingsJsonReader().read(
                    """{
                        "sync": {
                            "geonature_url": "https://demo.geonature.fr/geonature",
                            "taxhub_url": "https://demo.geonature.fr/taxhub",
                            "gn_application_id": 3,
                            "observers_list_id": 1,
                            "taxa_list_id": 100,
                            "code_area_type": "M10",
                            "page_size": 1000,
                            "sync_periodicity_data_essential": "20m",
                            "sync_periodicity_data": "30m"
                        },
                        "map": null
                    }""".trimIndent()
                )
            }.message
        )
    }

    @Test
    fun `should read app settings from JSON with no input settings`() {
        // when read a JSON as AppSettings
        val appSettings = AppSettingsJsonReader().read(
            """{
                "sync": {
                    "geonature_url": "https://demo.geonature.fr/geonature",
                    "taxhub_url": "https://demo.geonature.fr/taxhub",
                    "gn_application_id": 3,
                    "observers_list_id": 1,
                    "taxa_list_id": 100,
                    "code_area_type": "M10",
                    "page_size": 1000,
                    "sync_periodicity_data_essential": "20m",
                    "sync_periodicity_data": "30m"
                },
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

        // then
        assertNotNull(appSettings)
        assertEquals(
            InputSettings(dateSettings = InputDateSettings.DEFAULT),
            appSettings.inputSettings
        )
    }

    @Test
    fun `should read app settings from JSON with input settings and no date settings`() {
        assertEquals(
            InputSettings(dateSettings = InputDateSettings.DEFAULT),
            AppSettingsJsonReader().read(
                """{
                "sync": {
                    "geonature_url": "https://demo.geonature.fr/geonature",
                    "taxhub_url": "https://demo.geonature.fr/taxhub",
                    "gn_application_id": 3,
                    "observers_list_id": 1,
                    "taxa_list_id": 100,
                    "code_area_type": "M10",
                    "page_size": 1000,
                    "sync_periodicity_data_essential": "20m",
                    "sync_periodicity_data": "30m"
                },
                "map": {
                    "layers": [
                        {
                          "label": "OSM",
                          "source": "https://a.tile.openstreetmap.org"
                        }
                    ]
                },
                "input": {
                    "date": null
                }
            }""".trimIndent()
            ).inputSettings
        )
        assertEquals(
            InputSettings(dateSettings = InputDateSettings.DEFAULT),
            AppSettingsJsonReader().read(
                """{
                "sync": {
                    "geonature_url": "https://demo.geonature.fr/geonature",
                    "taxhub_url": "https://demo.geonature.fr/taxhub",
                    "gn_application_id": 3,
                    "observers_list_id": 1,
                    "taxa_list_id": 100,
                    "code_area_type": "M10",
                    "page_size": 1000,
                    "sync_periodicity_data_essential": "20m",
                    "sync_periodicity_data": "30m"
                },
                "map": {
                    "layers": [
                        {
                          "label": "OSM",
                          "source": "https://a.tile.openstreetmap.org"
                        }
                    ]
                },
                "input": {
                    "date": {
                        "enable_end_date": null,
                        "enable_hours": null
                    }
                }
            }""".trimIndent()
            ).inputSettings
        )
    }

    @Test
    fun `should read app settings from JSON with input settings and invalid date settings`() {
        assertEquals(
            InputSettings(dateSettings = InputDateSettings.DEFAULT),
            AppSettingsJsonReader().read(
                """{
                "sync": {
                    "geonature_url": "https://demo.geonature.fr/geonature",
                    "taxhub_url": "https://demo.geonature.fr/taxhub",
                    "gn_application_id": 3,
                    "observers_list_id": 1,
                    "taxa_list_id": 100,
                    "code_area_type": "M10",
                    "page_size": 1000,
                    "sync_periodicity_data_essential": "20m",
                    "sync_periodicity_data": "30m"
                },
                "map": {
                    "layers": [
                        {
                          "label": "OSM",
                          "source": "https://a.tile.openstreetmap.org"
                        }
                    ]
                },
                "input": {
                    "date": {
                        "enable_end_date": "no_such_settings",
                        "enable_hours": "no_such_settings"
                    }
                }
            }""".trimIndent()
            ).inputSettings
        )
        assertEquals(
            InputSettings(
                dateSettings = InputDateSettings(
                    startDateSettings = InputDateSettings.DateSettings.DATE,
                    endDateSettings = InputDateSettings.DateSettings.DATE
                )
            ),
            AppSettingsJsonReader().read(
                """{
                "sync": {
                    "geonature_url": "https://demo.geonature.fr/geonature",
                    "taxhub_url": "https://demo.geonature.fr/taxhub",
                    "gn_application_id": 3,
                    "observers_list_id": 1,
                    "taxa_list_id": 100,
                    "code_area_type": "M10",
                    "page_size": 1000,
                    "sync_periodicity_data_essential": "20m",
                    "sync_periodicity_data": "30m"
                },
                "map": {
                    "layers": [
                        {
                          "label": "OSM",
                          "source": "https://a.tile.openstreetmap.org"
                        }
                    ]
                },
                "input": {
                    "date": {
                        "enable_end_date": true,
                        "enable_hours": "no_such_settings"
                    }
                }
            }""".trimIndent()
            ).inputSettings
        )
        assertEquals(
            InputSettings(
                dateSettings = InputDateSettings(
                    startDateSettings = InputDateSettings.DateSettings.DATETIME
                )
            ),
            AppSettingsJsonReader().read(
                """{
                "sync": {
                    "geonature_url": "https://demo.geonature.fr/geonature",
                    "taxhub_url": "https://demo.geonature.fr/taxhub",
                    "gn_application_id": 3,
                    "observers_list_id": 1,
                    "taxa_list_id": 100,
                    "code_area_type": "M10",
                    "page_size": 1000,
                    "sync_periodicity_data_essential": "20m",
                    "sync_periodicity_data": "30m"
                },
                "map": {
                    "layers": [
                        {
                          "label": "OSM",
                          "source": "https://a.tile.openstreetmap.org"
                        }
                    ]
                },
                "input": {
                    "date": {
                        "enable_end_date": "no_such_settings",
                        "enable_hours": true
                    }
                }
            }""".trimIndent()
            ).inputSettings
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
            AppSettingsJsonReader().read(
                """{
                "sync": {
                    "geonature_url": "https://demo.geonature.fr/geonature",
                    "taxhub_url": "https://demo.geonature.fr/taxhub",
                    "gn_application_id": 3,
                    "observers_list_id": 1,
                    "taxa_list_id": 100,
                    "code_area_type": "M10",
                    "page_size": 1000,
                    "sync_periodicity_data_essential": "20m",
                    "sync_periodicity_data": "30m"
                },
                "map": {
                    "layers": [
                        {
                          "label": "OSM",
                          "source": "https://a.tile.openstreetmap.org"
                        }
                    ]
                },
                "input": {
                    "date": {
                        "enable_end_date": false,
                        "enable_hours": false
                    }
                }
            }""".trimIndent()
            ).inputSettings
        )
        assertEquals(
            InputSettings(
                dateSettings = InputDateSettings(
                    startDateSettings = InputDateSettings.DateSettings.DATETIME
                )
            ),
            AppSettingsJsonReader().read(
                """{
                "sync": {
                    "geonature_url": "https://demo.geonature.fr/geonature",
                    "taxhub_url": "https://demo.geonature.fr/taxhub",
                    "gn_application_id": 3,
                    "observers_list_id": 1,
                    "taxa_list_id": 100,
                    "code_area_type": "M10",
                    "page_size": 1000,
                    "sync_periodicity_data_essential": "20m",
                    "sync_periodicity_data": "30m"
                },
                "map": {
                    "layers": [
                        {
                          "label": "OSM",
                          "source": "https://a.tile.openstreetmap.org"
                        }
                    ]
                },
                "input": {
                    "date": {
                        "enable_end_date": false,
                        "enable_hours": true
                    }
                }
            }""".trimIndent()
            ).inputSettings
        )
        assertEquals(
            InputSettings(
                dateSettings = InputDateSettings(
                    startDateSettings = InputDateSettings.DateSettings.DATE,
                    endDateSettings = InputDateSettings.DateSettings.DATE
                )
            ),
            AppSettingsJsonReader().read(
                """{
                "sync": {
                    "geonature_url": "https://demo.geonature.fr/geonature",
                    "taxhub_url": "https://demo.geonature.fr/taxhub",
                    "gn_application_id": 3,
                    "observers_list_id": 1,
                    "taxa_list_id": 100,
                    "code_area_type": "M10",
                    "page_size": 1000,
                    "sync_periodicity_data_essential": "20m",
                    "sync_periodicity_data": "30m"
                },
                "map": {
                    "layers": [
                        {
                          "label": "OSM",
                          "source": "https://a.tile.openstreetmap.org"
                        }
                    ]
                },
                "input": {
                    "date": {
                        "enable_end_date": true,
                        "enable_hours": false
                    }
                }
            }""".trimIndent()
            ).inputSettings
        )
        assertEquals(
            InputSettings(
                dateSettings = InputDateSettings(
                    startDateSettings = InputDateSettings.DateSettings.DATETIME,
                    endDateSettings = InputDateSettings.DateSettings.DATETIME
                )
            ),
            AppSettingsJsonReader().read(
                """{
                "sync": {
                    "geonature_url": "https://demo.geonature.fr/geonature",
                    "taxhub_url": "https://demo.geonature.fr/taxhub",
                    "gn_application_id": 3,
                    "observers_list_id": 1,
                    "taxa_list_id": 100,
                    "code_area_type": "M10",
                    "page_size": 1000,
                    "sync_periodicity_data_essential": "20m",
                    "sync_periodicity_data": "30m"
                },
                "map": {
                    "layers": [
                        {
                          "label": "OSM",
                          "source": "https://a.tile.openstreetmap.org"
                        }
                    ]
                },
                "input": {
                    "date": {
                        "enable_end_date": true,
                        "enable_hours": true
                    }
                }
            }""".trimIndent()
            ).inputSettings
        )
    }

    @Test
    fun `should read app settings from JSON with no nomenclature settings`() {
        // when read a JSON as AppSettings
        val appSettings = AppSettingsJsonReader().read(
            """{
                "sync": {
                    "geonature_url": "https://demo.geonature.fr/geonature",
                    "taxhub_url": "https://demo.geonature.fr/taxhub",
                    "gn_application_id": 3,
                    "observers_list_id": 1,
                    "taxa_list_id": 100,
                    "code_area_type": "M10",
                    "page_size": 1000,
                    "sync_periodicity_data_essential": "20m",
                    "sync_periodicity_data": "30m"
                },
                "map": {
                    "layers": [
                        {
                          "label": "OSM",
                          "source": "https://a.tile.openstreetmap.org"
                        }
                    ]
                },
                "nomenclature": null
            }""".trimIndent()
        )

        // then
        assertNotNull(appSettings)
        assertNull(appSettings.nomenclatureSettings)
    }

    @Test
    fun `should override existing app settings from partial JSON file`() {
        // given an existing app settings
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
                .minZoomLevel(5.0)
                .maxZoomLevel(13.0)
                .addLayer(
                    LayerSettings.Builder.newInstance()
                        .label("OSM")
                        .addSource("https://a.tile.openstreetmap.org")
                        .build()
                )
                .build()
        )

        // when read a partial JSON settings
        val appSettings = AppSettingsJsonReader(existingAppSettings).read(
            """{
                "sync": {
                    "code_area_type": "M20",
                    "page_size": 10000
                },
                "map": {
                    "min_zoom": 7.0,
                    "max_zoom": 12.0,
                    "layers": [
                        {
                          "label": "Wikimedia",
                          "source": "https://maps.wikimedia.org/osm-intl"
                        }
                    ]
                },
                "nomenclature": null
            }""".trimIndent()
        )

        // then
        assertEquals(
            AppSettings(
                dataSyncSettings = DataSyncSettings(
                    geoNatureServerUrl = "https://demo.geonature.fr/geonature",
                    taxHubServerUrl = "https://demo.geonature.fr/taxhub",
                    applicationId = 3,
                    usersListId = 1,
                    taxrefListId = 100,
                    codeAreaType = "M20",
                    pageSize = 10000,
                    dataSyncPeriodicity = 30.toDuration(DurationUnit.MINUTES),
                    essentialDataSyncPeriodicity = 20.toDuration(DurationUnit.MINUTES)
                ),
                mapSettings = MapSettings.Builder()
                    .minZoomLevel(7.0)
                    .maxZoomLevel(12.0)
                    .addLayer(
                        LayerSettings.Builder.newInstance()
                            .label("Wikimedia")
                            .addSource("https://maps.wikimedia.org/osm-intl")
                            .build()
                    )
                    .build()
            ),
            appSettings
        )
    }
}
