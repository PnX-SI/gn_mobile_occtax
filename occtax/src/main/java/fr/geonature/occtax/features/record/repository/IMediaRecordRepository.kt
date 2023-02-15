package fr.geonature.occtax.features.record.repository

import fr.geonature.occtax.features.record.domain.CountingRecord
import fr.geonature.occtax.features.record.domain.MediaRecord
import fr.geonature.occtax.features.record.domain.TaxonRecord
import java.io.File

/**
 * [MediaRecord] repository.
 *
 * @author S. Grimault
 */
interface IMediaRecordRepository {

    /**
     * Loads all medias as local files from given [CountingRecord].
     */
    suspend fun loadAll(
        taxonRecord: TaxonRecord,
        countingRecord: CountingRecord
    ): Result<List<File>>
}