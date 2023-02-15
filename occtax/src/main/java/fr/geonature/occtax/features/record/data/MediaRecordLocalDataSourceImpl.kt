package fr.geonature.occtax.features.record.data

import android.content.Context
import fr.geonature.occtax.features.record.domain.CountingRecord
import fr.geonature.occtax.features.record.domain.TaxonRecord
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.toList
import java.io.File

/**
 * Default implementation of [IMediaRecordLocalDataSource].
 *
 * @author S. Grimault
 */
class MediaRecordLocalDataSourceImpl(private val context: Context) : IMediaRecordLocalDataSource {

    override suspend fun loadAll(
        taxonRecord: TaxonRecord,
        countingRecord: CountingRecord
    ): List<File> {
        return taxonRecord.counting.mediaBasePath(
            context,
            countingRecord
        )
            .walkTopDown()
            .asFlow()
            .filter { file ->
                file.isFile && file.canWrite() && file.toURI()
                    .toURL()
                    .run { openConnection().contentType }
                    ?.startsWith("image/") == true
            }
            .toList()
    }
}