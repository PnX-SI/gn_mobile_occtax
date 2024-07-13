package fr.geonature.occtax.features.record.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import fr.geonature.commons.data.entity.Taxon
import fr.geonature.commons.data.entity.Taxonomy
import fr.geonature.commons.util.add
import fr.geonature.datasync.auth.ICookieManager
import fr.geonature.datasync.settings.DataSyncSettings
import fr.geonature.maps.settings.LayerSettings
import fr.geonature.maps.settings.MapSettings
import fr.geonature.occtax.CoroutineTestRule
import fr.geonature.occtax.FixtureHelper.getFixture
import fr.geonature.occtax.api.IOcctaxAPIClient
import fr.geonature.occtax.api.OcctaxAPIClientImpl
import fr.geonature.occtax.features.record.domain.ObservationRecord
import fr.geonature.occtax.features.record.domain.PropertyValue
import fr.geonature.occtax.features.record.error.ObservationRecordException
import fr.geonature.occtax.features.settings.domain.AppSettings
import fr.geonature.occtax.features.settings.domain.InputDateSettings
import fr.geonature.occtax.features.settings.domain.InputSettings
import io.mockk.MockKAnnotations.init
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.Cookie
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.*
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
 * Unit tests about [IObservationRecordRemoteDataSource].
 *
 * @author S. Grimault
 */
@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class ObservationRecordRemoteDataSourceTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    @MockK
    private lateinit var cookieManager: ICookieManager

    private lateinit var mockWebServer: MockWebServer
    private lateinit var occtaxAPIClient: IOcctaxAPIClient
    private lateinit var observationRecordRemoteDataSource: IObservationRecordRemoteDataSource

    @Before
    fun setUp() {
        init(this)

        mockWebServer = MockWebServer()
        occtaxAPIClient = OcctaxAPIClientImpl(cookieManager)
        observationRecordRemoteDataSource = ObservationRecordRemoteDataSourceImpl(occtaxAPIClient)

        every { cookieManager.cookie } returns Cookie
            .Builder()
            .name("token")
            .value("some_value")
            .domain("demo.geonature.fr")
            .path("/")
            .expiresAt(
                Date().add(
                    Calendar.HOUR,
                    1
                ).time
            )
            .build()

        occtaxAPIClient.setBaseUrl(
            mockWebServer.url("/")
                .toString()
        )
    }

    @Test
    fun `should send successfully an observation record`() = runTest {
        // given an observation record with some taxa
        val observationRecord = ObservationRecord(
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

        val expectedJSONResponse = getFixture("observation_record_no_taxa_sent.json")
        mockWebServer.enqueue(MockResponse().setBody(expectedJSONResponse))

        // when sending this observation record
        val observationRecordUpdated = observationRecordRemoteDataSource.sendObservationRecord(
            observationRecord,
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
                    .build(),
                inputSettings = InputSettings(
                    dateSettings = InputDateSettings(
                        startDateSettings = InputDateSettings.DateSettings.DATETIME,
                        endDateSettings = InputDateSettings.DateSettings.DATETIME
                    )
                )
            )
        )

        // then
        assertEquals(
            ObservationRecord(
                internalId = 1240L,
                id = 492L,
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
            },
            observationRecordUpdated
        )
    }

    @Test
    fun `should throw InvalidStatusException when sending observation record with wrong status`() =
        runTest {
            // given an observation record with some taxa and wrong status
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

            // when sending this observation record
            val exception = runCatching {
                observationRecordRemoteDataSource.sendObservationRecord(
                    observationRecord,
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
                            .build(),
                        inputSettings = InputSettings(
                            dateSettings = InputDateSettings(
                                startDateSettings = InputDateSettings.DateSettings.DATETIME,
                                endDateSettings = InputDateSettings.DateSettings.DATETIME
                            )
                        )
                    )
                )
            }.exceptionOrNull()

            assertTrue(exception is ObservationRecordException.InvalidStatusException)
            assertEquals(
                (exception as ObservationRecordException.InvalidStatusException).message,
                ObservationRecordException.InvalidStatusException(1240L).message
            )
        }

    @Test
    fun `should send successfully a taxon record`() = runTest {
        // given an observation record with some taxa
        val observationRecord = ObservationRecord(
            internalId = 1240L,
            id = 1234L,
            status = ObservationRecord.Status.TO_SYNC
        ).apply {
            comment.comment = "some comment"
            taxa.add(
                Taxon(
                    10L,
                    "taxon_01",
                    Taxonomy(
                        "Animalia",
                        "Ascidies"
                    )
                )
            )
                .apply {
                    listOf(
                        PropertyValue.Text(
                            "comment",
                            "Some comment"
                        ),
                        PropertyValue.Text(
                            "determiner",
                            "Determiner value"
                        ),
                        PropertyValue.Nomenclature(
                            "ETA_BIO",
                            null,
                            29
                        ),
                        PropertyValue.Nomenclature(
                            "METH_DETERMIN",
                            null,
                            445
                        ),
                        PropertyValue.Nomenclature(
                            "METH_OBS",
                            null,
                            41
                        ),
                        PropertyValue.Nomenclature(
                            "NATURALITE",
                            null,
                            160
                        ),
                        PropertyValue.Nomenclature(
                            "OCC_COMPORTEMENT",
                            null,
                            580
                        ),
                        PropertyValue.Nomenclature(
                            "PREUVE_EXIST",
                            null,
                            81
                        ),
                        PropertyValue.Nomenclature(
                            "STATUT_BIO",
                            null,
                            29
                        )
                    ).map { it.toPair() }
                        .forEach { properties[it.first] = it.second }

                    counting.addOrUpdate(counting.create()
                        .apply {
                            listOf(
                                PropertyValue.Number(
                                    "count_min",
                                    1L
                                ),
                                PropertyValue.Number(
                                    "count_max",
                                    2L
                                ),
                                PropertyValue.Nomenclature(
                                    "OBJ_DENBR",
                                    null,
                                    146
                                ),
                                PropertyValue.Nomenclature(
                                    "SEXE",
                                    null,
                                    168
                                ),
                                PropertyValue.Nomenclature(
                                    "STADE_VIE",
                                    null,
                                    2
                                ),
                                PropertyValue.Nomenclature(
                                    "TYP_DENBR",
                                    null,
                                    93
                                )
                            ).map { it.toPair() }
                                .forEach { properties[it.first] = it.second }
                        }
                    )
                }
        }

        val expectedJSONResponse = getFixture("taxon_record_with_counting.json")
        mockWebServer.enqueue(MockResponse().setBody(expectedJSONResponse))

        // when sending all taxa records
        val observationRecordUpdated =
            observationRecordRemoteDataSource.sendTaxaRecords(
                observationRecord,
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
                        .build(),
                    inputSettings = InputSettings(
                        dateSettings = InputDateSettings(
                            startDateSettings = InputDateSettings.DateSettings.DATETIME,
                            endDateSettings = InputDateSettings.DateSettings.DATETIME
                        )
                    )
                )
            )

        // then
        assertEquals(
            observationRecord,
            observationRecordUpdated
        )
    }

    @Test
    fun `should throw InvalidStatusException when sending taxa record with wrong status`() =
        runTest {
            // given an observation record with some taxa and wrong status
            val observationRecord = ObservationRecord(
                internalId = 1240L,
                id = 1234L
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

            // when sending this observation record
            val exception = runCatching {
                observationRecordRemoteDataSource.sendTaxaRecords(
                    observationRecord,
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
                            .build(),
                        inputSettings = InputSettings(
                            dateSettings = InputDateSettings(
                                startDateSettings = InputDateSettings.DateSettings.DATETIME,
                                endDateSettings = InputDateSettings.DateSettings.DATETIME
                            )
                        )
                    )
                )
            }.exceptionOrNull()

            assertTrue(exception is ObservationRecordException.InvalidStatusException)
            assertEquals(
                (exception as ObservationRecordException.InvalidStatusException).message,
                ObservationRecordException.InvalidStatusException(1240L).message
            )
        }

    @Test
    fun `should throw SynchronizeException when sending taxa record with missing observation record ID`() =
        runTest {
            // given an observation record with some taxa and wrong status
            val observationRecord = ObservationRecord(
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

            // when sending this observation record
            val exception = runCatching {
                observationRecordRemoteDataSource.sendTaxaRecords(
                    observationRecord,
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
                            .build(),
                        inputSettings = InputSettings(
                            dateSettings = InputDateSettings(
                                startDateSettings = InputDateSettings.DateSettings.DATETIME,
                                endDateSettings = InputDateSettings.DateSettings.DATETIME
                            )
                        )
                    )
                )
            }.exceptionOrNull()

            assertTrue(exception is ObservationRecordException.SynchronizeException)
            assertEquals(
                (exception as ObservationRecordException.SynchronizeException).message,
                ObservationRecordException.SynchronizeException(1240L).message
            )
        }
}