package fr.geonature.occtax.features.record.repository

import android.content.Context
import fr.geonature.datasync.auth.IAuthManager
import fr.geonature.datasync.auth.error.AuthException
import fr.geonature.occtax.R
import fr.geonature.occtax.features.record.data.IMediaRecordLocalDataSource
import fr.geonature.occtax.features.record.data.IMediaRecordRemoteDataSource
import fr.geonature.occtax.features.record.domain.CountingRecord
import fr.geonature.occtax.features.record.domain.TaxonRecord
import java.io.File
import java.text.DateFormat
import java.util.Date

/**
 * Default implementation of [IMediaRecordRepository].
 *
 * @author S. Grimault
 */
class MediaRecordRepositoryImpl(
    private val context: Context,
    private val authManager: IAuthManager,
    private val mediaRecordLocalDataSource: IMediaRecordLocalDataSource,
    private val mediaRecordRemoteDataSource: IMediaRecordRemoteDataSource
) :
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

    override suspend fun synchronizeMediaFiles(taxonRecord: TaxonRecord): Result<TaxonRecord> {
        val authUser = authManager.getAuthLogin()?.user
            ?: return Result.failure(AuthException.NotConnectedException)

        taxonRecord.counting.counting.forEach { countingRecord ->
            countingRecord.medias.medias = mediaRecordLocalDataSource.loadAll(
                taxonRecord,
                countingRecord
            )
                .mapNotNull { file ->
                    val modifiedAt = DateFormat.getDateInstance(DateFormat.MEDIUM)
                        .format(Date(file.lastModified()))
                    val mediaTitle = context.getString(
                        R.string.counting_media_title,
                        taxonRecord.taxon.name,
                        modifiedAt
                    )
                    val mediaDescription = context.getString(
                        R.string.counting_media_description,
                        taxonRecord.taxon.name,
                        modifiedAt
                    )

                    runCatching {
                        mediaRecordRemoteDataSource.sendMediaFile(
                            file,
                            author = "${authUser.lastname.uppercase()} ${authUser.firstname.replaceFirstChar { c -> c.uppercase() }}",
                            title = mediaTitle,
                            description = mediaDescription
                        )
                    }.getOrNull()
                }
        }

        return Result.success(taxonRecord)
    }

    override suspend fun deleteAllMediaFiles(taxonRecord: TaxonRecord): Result<Unit> {
        taxonRecord.counting.counting.forEach { countingRecord ->
            countingRecord.medias.medias.forEach { media ->
                runCatching { mediaRecordRemoteDataSource.deleteMediaFile(media) }
            }
        }

        return Result.success(Unit)
    }
}