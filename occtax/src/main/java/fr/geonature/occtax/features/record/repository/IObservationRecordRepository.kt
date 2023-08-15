package fr.geonature.occtax.features.record.repository

import fr.geonature.occtax.features.record.domain.ObservationRecord
import fr.geonature.occtax.features.settings.domain.AppSettings

/**
 * [ObservationRecord] repository.
 *
 * @author S. Grimault
 */
interface IObservationRecordRepository {

    /**
     * Reads all [ObservationRecord]s, both those in progress and those ready to be synchronized.
     *
     * @return A list of [ObservationRecord]s
     */
    suspend fun readAll(): Result<List<ObservationRecord>>

    /**
     * Reads [ObservationRecord] from a given ID.
     *
     * @param id The [ObservationRecord] ID to read.
     *
     * @return [ObservationRecord] or [Result.Failure] if not found or something goes wrong
     */
    suspend fun read(id: Long): Result<ObservationRecord>

    /**
     * Saves the given [ObservationRecord].
     *
     * @param observationRecord the [ObservationRecord] to save
     *
     * @return [ObservationRecord] saved or [Result.Failure] if not found or something goes wrong
     */
    suspend fun save(
        observationRecord: ObservationRecord,
        status: ObservationRecord.Status = ObservationRecord.Status.DRAFT
    ): Result<ObservationRecord>

    /**
     * Deletes [ObservationRecord] from a given ID.
     *
     * @param id the [ObservationRecord] ID to delete
     */
    suspend fun delete(id: Long): Result<ObservationRecord>

    /**
     * Exports [ObservationRecord] from a given ID as `JSON` file.
     *
     * @param id the [ObservationRecord] ID to export
     * @param settings additional settings
     */
    suspend fun export(
        id: Long,
        settings: AppSettings? = null
    ): Result<ObservationRecord>

    /**
     * Exports [ObservationRecord] as `JSON` file.
     *
     * @param observationRecord the [ObservationRecord] to save
     * @param settings additional settings
     */
    suspend fun export(
        observationRecord: ObservationRecord,
        settings: AppSettings? = null
    ): Result<ObservationRecord>
}