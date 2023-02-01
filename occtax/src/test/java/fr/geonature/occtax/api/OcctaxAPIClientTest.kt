package fr.geonature.occtax.api

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import fr.geonature.commons.util.add
import fr.geonature.datasync.api.error.BaseApiException
import fr.geonature.datasync.api.error.MissingConfigurationException
import fr.geonature.datasync.auth.ICookieManager
import fr.geonature.occtax.CoroutineTestRule
import fr.geonature.occtax.FixtureHelper.getFixture
import io.mockk.MockKAnnotations.init
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.Cookie
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import retrofit2.awaitResponse
import java.util.Calendar
import java.util.Date

/**
 * Unit tests about [IOcctaxAPIClient].
 *
 * @author S. Grimault
 */
@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class OcctaxAPIClientTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    @MockK
    private lateinit var cookieManager: ICookieManager

    private lateinit var mockWebServer: MockWebServer
    private lateinit var occtaxAPIClient: IOcctaxAPIClient

    @Before
    fun setUp() {
        init(this)

        mockWebServer = MockWebServer()
        occtaxAPIClient = OcctaxAPIClientImpl(cookieManager)

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
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `should throw MissingGeoNatureBaseURLException if no base URL was set before calling API endpoint`() =
        runTest {
            assertThrows(MissingConfigurationException.MissingGeoNatureBaseURLException::class.java) {
                occtaxAPIClient.sendObservationRecord(JSONObject(getFixture("observation_record_no_taxa.json")))
                    .execute()
            }
        }

    @Test
    fun `should send successfully an observation record`() = runTest {
        val expectedJSONResponse = getFixture("observation_record_no_taxa.json")
        mockWebServer.enqueue(MockResponse().setBody(expectedJSONResponse))

        occtaxAPIClient.setBaseUrl(
            mockWebServer.url("/")
                .toString()
        )

        val response = occtaxAPIClient.sendObservationRecord(JSONObject(expectedJSONResponse))
            .awaitResponse()

        val requestUrl = mockWebServer.takeRequest().requestUrl

        assertTrue(response.isSuccessful)
        assertEquals(
            expectedJSONResponse,
            response.body()
                ?.string()
        )
        assertEquals(
            "/api/occtax/OCCTAX/only/releve",
            requestUrl?.toUrl()?.path
        )
    }

    @Test
    fun `should throw BadRequestException when sending an invalid observation record`() =
        runTest {
            val expectedJSONResponse = getFixture("observation_record_empty.json")
            mockWebServer.enqueue(MockResponse().setResponseCode(400))

            occtaxAPIClient.setBaseUrl(
                mockWebServer.url("/")
                    .toString()
            )

            assertThrows(BaseApiException.BadRequestException::class.java) {
                occtaxAPIClient.sendObservationRecord(JSONObject(expectedJSONResponse))
                    .execute()
            }
        }

    @Test
    fun `should send successfully a taxon record`() = runTest {
        val expectedJSONResponse = getFixture("taxon_record_with_counting.json")
        mockWebServer.enqueue(MockResponse().setBody(expectedJSONResponse))

        occtaxAPIClient.setBaseUrl(
            mockWebServer.url("/")
                .toString()
        )

        val response = occtaxAPIClient.sendTaxonRecord(
            1234L,
            JSONObject(expectedJSONResponse)
        )
            .awaitResponse()

        val requestUrl = mockWebServer.takeRequest().requestUrl

        assertTrue(response.isSuccessful)
        assertEquals(
            expectedJSONResponse,
            response.body()
                ?.string()
        )
        assertEquals(
            "/api/occtax/OCCTAX/releve/1234/occurrence",
            requestUrl?.toUrl()?.path
        )
    }

    @Test
    fun `should throw BadRequestException when sending an invalid taxon record`() =
        runTest {
            mockWebServer.enqueue(MockResponse().setResponseCode(400))

            occtaxAPIClient.setBaseUrl(
                mockWebServer.url("/")
                    .toString()
            )

            assertThrows(BaseApiException.BadRequestException::class.java) {
                occtaxAPIClient.sendTaxonRecord(
                    1234L,
                    JSONObject("{}")
                )
                    .execute()
            }
        }
}