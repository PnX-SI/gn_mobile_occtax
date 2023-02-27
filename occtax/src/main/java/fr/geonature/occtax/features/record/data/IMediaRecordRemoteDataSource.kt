package fr.geonature.occtax.features.record.data

import fr.geonature.datasync.api.model.Media
import fr.geonature.occtax.features.record.domain.MediaRecord
import fr.geonature.occtax.features.record.error.MediaRecordException
import java.io.File

/**
 * Remote data source about [MediaRecord].
 *
 * @author S. Grimault
 */
interface IMediaRecordRemoteDataSource {

    /**
     * Sends given file as [Media].
     *
     * @throws [MediaRecordException.SynchronizeException] if something goes wrong
     */
    suspend fun sendMediaFile(file: File, author: String, title: String, description: String): Media

    /**
     * Deletes already uploaded [Media] file.
     *
     * @throws [MediaRecordException.DeleteException] if the given [Media] cannot be deleted
     */
    suspend fun deleteMediaFile(media: Media)
}