package fr.geonature.occtax.api

import fr.geonature.occtax.features.record.domain.ObservationRecord
import fr.geonature.occtax.features.record.domain.TaxonRecord
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call

/**
 * _GeoNature_ "Occtax" module API client.
 *
 * @author S. Grimault
 */
interface IOcctaxAPIClient {

    /**
     * Sets the GeoNature base URL to use.
     *
     * @param url base URL
     */
    fun setBaseUrl(url: String)

    /**
     * Sends a newly created [ObservationRecord] without any [TaxonRecord]s.
     */
    fun sendObservationRecord(json: JSONObject): Call<ResponseBody>

    /**
     * Sends a [TaxonRecord] with all counting. The counting order matters.
     */
    fun sendTaxonRecord(recordId: Long, json: JSONObject): Call<ResponseBody>

    /**
     * Deletes an existing [ObservationRecord].
     */
    fun deleteObservationRecord(recordId: Long): Call<ResponseBody>
}