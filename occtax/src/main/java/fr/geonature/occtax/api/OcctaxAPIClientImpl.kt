package fr.geonature.occtax.api

import fr.geonature.datasync.api.createServiceClient
import fr.geonature.datasync.api.error.MissingConfigurationException
import fr.geonature.datasync.auth.ICookieManager
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call

/**
 * Default implementation of _GeoNature_ "Occtax" module API client.
 *
 * @author S. Grimault
 */
class OcctaxAPIClientImpl(private val cookieManager: ICookieManager) : IOcctaxAPIClient {

    private var occtaxAPIService: IOcctaxAPIService? = null
    private var geoNatureBaseUrl: String? = null

    override fun setBaseUrl(url: String) {
        if (url.isNotBlank()) {
            this.geoNatureBaseUrl = url
            occtaxAPIService = createServiceClient(
                url,
                cookieManager,
                IOcctaxAPIService::class.java
            )
        }
    }

    override fun sendObservationRecord(json: JSONObject): Call<ResponseBody> {
        val occtaxAPIService = occtaxAPIService
            ?: throw MissingConfigurationException.MissingGeoNatureBaseURLException

        return occtaxAPIService.sendObservationRecord(
            json.toString()
                .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        )
    }

    override fun sendTaxonRecord(recordId: Long, json: JSONObject): Call<ResponseBody> {
        val occtaxAPIService = occtaxAPIService
            ?: throw MissingConfigurationException.MissingGeoNatureBaseURLException

        return occtaxAPIService.sendTaxonRecord(
            recordId,
            json.toString()
                .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        )
    }

    override fun deleteObservationRecord(recordId: Long): Call<ResponseBody> {
        val occtaxAPIService = occtaxAPIService
            ?: throw MissingConfigurationException.MissingGeoNatureBaseURLException

        return occtaxAPIService.deleteObservationRecord(recordId)
    }
}