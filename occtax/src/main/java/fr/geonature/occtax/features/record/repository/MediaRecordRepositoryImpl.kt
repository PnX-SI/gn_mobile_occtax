package fr.geonature.occtax.features.record.repository

import fr.geonature.occtax.features.record.data.IMediaRecordLocalDataSource
import fr.geonature.occtax.features.record.domain.CountingRecord
import fr.geonature.occtax.features.record.domain.TaxonRecord
import java.io.File

/**
 * Default implementation of [IMediaRecordRepository].
 *
 * @author S. Grimault
 */
class MediaRecordRepositoryImpl(private val mediaRecordLocalDataSource: IMediaRecordLocalDataSource) :
    IMediaRecordRepository {

    override suspend fun loadAll(
        taxonRecord: TaxonRecord,
        countingRecord: CountingRecord
    ): Result<List<File>> {
        return runCatching {
            mediaRecordLocalDataSource.loadAll(
                taxonRecord,
                countingRecord
            )
        }
    }
}