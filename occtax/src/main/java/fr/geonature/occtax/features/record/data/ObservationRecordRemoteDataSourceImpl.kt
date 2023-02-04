package fr.geonature.occtax.features.record.data

import fr.geonature.occtax.api.IOcctaxAPIClient
import fr.geonature.occtax.features.record.domain.ObservationRecord
import fr.geonature.occtax.features.record.error.ObservationRecordException
import fr.geonature.occtax.features.record.io.ObservationRecordJsonWriter
import fr.geonature.occtax.features.record.io.TaxonRecordJsonWriter
import fr.geonature.occtax.settings.AppSettings
import org.json.JSONObject
import retrofit2.await
import retrofit2.awaitResponse

/**
 * Default implementation of [IObservationRecordRemoteDataSource].
 *
 * @author S. Grimault
 */
class ObservationRecordRemoteDataSourceImpl(private val occtaxAPIClient: IOcctaxAPIClient) :
    IObservationRecordRemoteDataSource {

    private val observationRecordJsonWriter: ObservationRecordJsonWriter =
        ObservationRecordJsonWriter()
    private val taxonRecordJsonWriter: TaxonRecordJsonWriter = TaxonRecordJsonWriter()

    override fun setBaseUrl(url: String) {
        occtaxAPIClient.setBaseUrl(url)
    }

    override suspend fun sendObservationRecord(
        observationRecord: ObservationRecord,
        appSettings: AppSettings
    ): ObservationRecord {
        if (observationRecord.status != ObservationRecord.Status.TO_SYNC) {
            throw ObservationRecordException.InvalidStatusException(observationRecord.internalId)
        }

        val observationRecordToSend = ObservationRecord(
            internalId = observationRecord.internalId,
            geometry = observationRecord.geometry
        ).apply {
            comment.comment = observationRecord.comment.comment
            dataset.datasetId = observationRecord.dataset.datasetId
            dates.start = observationRecord.dates.start
            dates.end = observationRecord.dates.end
            observationRecord.observers.getAllObserverIds()
                .forEach {
                    observers.addObserverId(it)
                }
        }

        val observationRecordToSendAsJson = runCatching {
            JSONObject(
                observationRecordJsonWriter.write(
                    observationRecordToSend,
                    appSettings
                )
            )
        }.onFailure { throw ObservationRecordException.WriteException(observationRecord.internalId) }
            .getOrThrow()

        val json = occtaxAPIClient.sendObservationRecord(observationRecordToSendAsJson)
            .await()
            .string()
        val id = runCatching {
            JSONObject(json).optLong("id")
                .takeIf { it > 0L }
        }.onFailure { throw ObservationRecordException.ReadException(observationRecord.internalId) }
            .getOrThrow()
            ?: throw ObservationRecordException.SynchronizeException(observationRecord.internalId)

        return observationRecord.copy(id = id)
    }

    override suspend fun sendTaxaRecords(
        observationRecord: ObservationRecord,
        appSettings: AppSettings
    ): ObservationRecord {
        if (observationRecord.status != ObservationRecord.Status.TO_SYNC) {
            throw ObservationRecordException.InvalidStatusException(observationRecord.internalId)
        }

        val recordId = observationRecord.id
            ?: throw ObservationRecordException.SynchronizeException(observationRecord.internalId)

        observationRecord.taxa.taxa.forEach {
            val asJson = runCatching {
                JSONObject(
                    taxonRecordJsonWriter.write(
                        it,
                        appSettings
                    )
                )
            }.onFailure { throw ObservationRecordException.WriteException(observationRecord.internalId) }
                .getOrThrow()

            occtaxAPIClient.sendTaxonRecord(
                recordId,
                asJson
            )
                .awaitResponse()
        }

        return observationRecord
    }

    override suspend fun deleteObservationRecord(observationRecord: ObservationRecord) {
        val recordId = observationRecord.id
            ?: throw ObservationRecordException.SynchronizeException(observationRecord.internalId)

        occtaxAPIClient.deleteObservationRecord(recordId)
            .awaitResponse()
    }
}