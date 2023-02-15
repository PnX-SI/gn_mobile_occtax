package fr.geonature.occtax.features.record.data

import fr.geonature.occtax.features.record.domain.CountingRecord
import fr.geonature.occtax.features.record.domain.MediaRecord
import fr.geonature.occtax.features.record.domain.TaxonRecord
import java.io.File

/**
 * Local data source about [MediaRecord].
 *
 * @author S. Grimault
 */
interface IMediaRecordLocalDataSource {

    /**
     * Loads all medias as local files from given [CountingRecord].
     */
    suspend fun loadAll(taxonRecord: TaxonRecord, countingRecord: CountingRecord): List<File>
}