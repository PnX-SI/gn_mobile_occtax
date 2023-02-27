package fr.geonature.occtax.features.record.error

import fr.geonature.datasync.api.model.Media
import fr.geonature.occtax.features.record.domain.MediaRecord
import java.io.File

/**
 * Base exception about [MediaRecord].
 *
 * @author S. Grimault
 */
sealed class MediaRecordException(
    message: String? = null,
    cause: Throwable? = null
) : RuntimeException(
    message,
    cause
) {
    /**
     * Thrown if the [MediaRecord] synchronization finished with errors.
     */
    data class SynchronizeException(val file: File) :
        ObservationRecordException("failed to synchronize media record from file '${file.absolutePath}'")

    /**
     * Thrown if an existing [MediaRecord] cannot be deleted.
     */
    data class DeleteException(val media: Media) :
        ObservationRecordException("failed to delete media file '${media.id}'")
}
