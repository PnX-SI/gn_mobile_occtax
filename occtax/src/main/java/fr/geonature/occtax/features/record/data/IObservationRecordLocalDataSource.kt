package fr.geonature.occtax.features.record.data

import fr.geonature.occtax.features.record.domain.ObservationRecord
import fr.geonature.occtax.features.record.error.ObservationRecordException
import fr.geonature.occtax.settings.AppSettings

/**
 * Local data source about [ObservationRecord].
 *
 * @author S. Grimault
 */
interface IObservationRecordLocalDataSource {

    /**
     * Reads all [ObservationRecord]s, both those in progress and those ready to be synchronized.
     *
     * @return A list of [ObservationRecord]s
     */
    suspend fun readAll(): List<ObservationRecord>

    /**
     * Reads [ObservationRecord] from a given ID.
     *
     * @param id The [ObservationRecord] ID to read.
     *
     * @return [ObservationRecord] or throws [ObservationRecordException.NotFoundException] if not found
     *
     * @throws [ObservationRecordException.NotFoundException] it not found
     * @throws [ObservationRecordException.ReadException] if something goes wrong
     */
    suspend fun read(id: Long): ObservationRecord

    /**
     * Saves the given [ObservationRecord].
     *
     * @param observationRecord the [ObservationRecord] to save
     *
     * @return [ObservationRecord] saved
     *
     * @throws [ObservationRecordException.WriteException] if something goes wrong
     */
    suspend fun save(
        observationRecord: ObservationRecord,
        status: ObservationRecord.Status = ObservationRecord.Status.DRAFT
    ): ObservationRecord

    /**
     * Deletes [ObservationRecord] from a given ID.
     *
     * @param id the [ObservationRecord] ID to delete
     *
     * @throws [ObservationRecordException.NotFoundException] it not found
     * @throws [ObservationRecordException.WriteException] if something goes wrong
     */
    suspend fun delete(id: Long): ObservationRecord

    /**
     * Exports [ObservationRecord] from a given ID as `JSON` file.
     *
     * @param id the [ObservationRecord] ID to export
     * @param settings additional settings
     *
     * @throws [ObservationRecordException.NotFoundException] it not found
     * @throws [ObservationRecordException.WriteException] if something goes wrong
     */
    suspend fun export(
        id: Long,
        settings: AppSettings? = null
    ): ObservationRecord

    /**
     * Exports [ObservationRecord] as `JSON` file.
     *
     * @param observationRecord the [ObservationRecord] to save
     * @param settings additional settings
     *
     * @throws [ObservationRecordException.WriteException] if something goes wrong
     */
    suspend fun export(
        observationRecord: ObservationRecord,
        settings: AppSettings? = null
    ): ObservationRecord
}