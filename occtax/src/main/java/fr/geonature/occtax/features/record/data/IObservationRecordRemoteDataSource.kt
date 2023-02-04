package fr.geonature.occtax.features.record.data

import fr.geonature.occtax.features.record.domain.ObservationRecord
import fr.geonature.occtax.features.record.domain.TaxonRecord
import fr.geonature.occtax.settings.AppSettings

/**
 * Remote data source about [ObservationRecord].
 *
 * @author S. Grimault
 */
interface IObservationRecordRemoteDataSource {

    /**
     * Sets the GeoNature base URL to use.
     *
     * @param url base URL
     */
    fun setBaseUrl(url: String)

    /**
     * Sends a newly created [ObservationRecord] without any [TaxonRecord]s.
     *
     * @return [ObservationRecord] successfully created with ID coming from _GeoNature_.
     */
    suspend fun sendObservationRecord(
        observationRecord: ObservationRecord,
        appSettings: AppSettings
    ): ObservationRecord

    /**
     * Sends all added [TaxonRecord] from given [ObservationRecord] with all counting for each of them.
     *
     * @return [ObservationRecord] successfully updated with all [TaxonRecord].
     */
    suspend fun sendTaxaRecords(
        observationRecord: ObservationRecord,
        appSettings: AppSettings
    ): ObservationRecord

    /**
     * Deletes an existing [ObservationRecord].
     */
    suspend fun deleteObservationRecord(observationRecord: ObservationRecord)
}