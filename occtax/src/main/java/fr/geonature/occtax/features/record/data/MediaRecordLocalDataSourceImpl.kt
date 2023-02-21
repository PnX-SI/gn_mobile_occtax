package fr.geonature.occtax.features.record.data

import android.content.Context
import android.webkit.MimeTypeMap
import fr.geonature.occtax.features.record.domain.CountingRecord
import fr.geonature.occtax.features.record.domain.TaxonRecord
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.toList
import org.tinylog.Logger
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
            .also {
                Logger.debug { "load all medias from '$it'..." }
            }
            .walkTopDown()
            .asFlow()
            .filter { file -> file.isFile && file.canWrite() }
            .filter { file ->
                val mimetype = file.toURI()
                    .toURL()
                    .run { openConnection().contentType }
                    ?: file.extension.takeIf { it.isNotEmpty() }
                        ?.let {
                            MimeTypeMap.getSingleton()
                                .getMimeTypeFromExtension(it)
                        }

                Logger.debug { "loading file '${file.name}' (mime-type: $mimetype)" }

                file.isFile && file.canWrite() && mimetype?.startsWith("image/") == true
            }
            .toList()
    }
}