package fr.geonature.occtax.api

import fr.geonature.occtax.features.record.domain.ObservationRecord
import fr.geonature.occtax.features.record.domain.TaxonRecord
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * _GeoNature_ "Occtax" module API interface definition.
 *
 * @author S. Grimault
 */
interface IOcctaxAPIService {

    /**
     * Sends a newly created [ObservationRecord] without any [TaxonRecord]s.
     */
    @Headers(
        "Accept: application/json",
        "Content-Type: application/json;charset=UTF-8"
    )
    @POST("api/occtax/OCCTAX/only/releve")
    fun sendObservationRecord(
        @Body observationRecord: RequestBody
    ): Call<ResponseBody>

    /**
     * Sends a [TaxonRecord] with all counting. The counting order matters.
     */
    @Headers(
        "Accept: application/json",
        "Content-Type: application/json;charset=UTF-8"
    )
    @POST("api/occtax/OCCTAX/releve/{id}/occurrence")
    fun sendTaxonRecord(
        @Path("id") recordId: Long,
        @Body taxonRecord: RequestBody
    ): Call<ResponseBody>

    /**
     * Deletes an existing [ObservationRecord].
     */
    @Headers(
        "Accept: application/json"
    )
    @DELETE("api/occtax/OCCTAX/releve/{id}")
    fun deleteObservationRecord(@Path("id") recordId: Long): Call<ResponseBody>
}