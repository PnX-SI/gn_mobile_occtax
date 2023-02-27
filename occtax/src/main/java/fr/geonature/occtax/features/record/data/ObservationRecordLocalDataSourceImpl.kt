package fr.geonature.occtax.features.record.data

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import fr.geonature.occtax.features.record.domain.ObservationRecord
import fr.geonature.occtax.features.record.error.ObservationRecordException
import fr.geonature.occtax.features.record.io.ObservationRecordJsonReader
import fr.geonature.occtax.features.record.io.ObservationRecordJsonWriter
import fr.geonature.occtax.settings.AppSettings
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Default implementation of [IObservationRecordLocalDataSource] using [SharedPreferences].
 *
 * @author S. Grimault
 */
class ObservationRecordLocalDataSourceImpl(
    context: Context,
    private val geoNatureModuleName: String,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : IObservationRecordLocalDataSource {

    private val preferenceManager: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)
    private val observationRecordJsonReader: ObservationRecordJsonReader =
        ObservationRecordJsonReader()
    private val observationRecordJsonWriter: ObservationRecordJsonWriter =
        ObservationRecordJsonWriter()

    override suspend fun readAll(): List<ObservationRecord> = withContext(dispatcher) {
        preferenceManager.all
            .filterKeys { it.startsWith("${KEY_PREFERENCE_INPUT}_") }
            .values
            .mapNotNull { if (it is String && it.isNotBlank()) runCatching { observationRecordJsonReader.read(it) }.getOrNull() else null }
            .sortedBy { it.internalId }
    }

    override suspend fun read(id: Long): ObservationRecord = withContext(dispatcher) {
        val asJson = preferenceManager.getString(
            buildInputPreferenceKey(id),
            null
        )

        if (asJson.isNullOrBlank()) {
            throw ObservationRecordException.NotFoundException(id)
        }

        runCatching { observationRecordJsonReader.read(asJson) }
            .onFailure { throw ObservationRecordException.ReadException(id) }
            .getOrThrow()
    }

    override suspend fun save(
        observationRecord: ObservationRecord,
        status: ObservationRecord.Status
    ): ObservationRecord = withContext(dispatcher) {
        val savedObservationRecord = observationRecord.copy(status = status)
        val asJson = runCatching { observationRecordJsonWriter.write(savedObservationRecord) }
            .getOrNull()

        if (asJson.isNullOrBlank()) throw ObservationRecordException.WriteException(savedObservationRecord.internalId)

        val saved = preferenceManager
            .edit()
            .putString(
                buildInputPreferenceKey(savedObservationRecord.internalId),
                asJson
            )
            .commit()

        if (!saved) throw ObservationRecordException.WriteException(savedObservationRecord.internalId)

        savedObservationRecord
    }

    override suspend fun delete(id: Long): ObservationRecord {
        val observationRecordToDelete = read(id)

        return withContext(dispatcher) {
            if (preferenceManager.contains(buildInputPreferenceKey(id))) {
                val deleted = preferenceManager
                    .edit()
                    .remove(buildInputPreferenceKey(id))
                    .commit()

                if (!deleted) throw ObservationRecordException.WriteException(id)
            }

            observationRecordToDelete
        }
    }

    override suspend fun export(id: Long, settings: AppSettings?): ObservationRecord {
        val inputToExport = read(id)

        return export(
            inputToExport,
            settings
        )
    }

    override suspend fun export(
        observationRecord: ObservationRecord,
        settings: AppSettings?
    ): ObservationRecord {
        return save(
            observationRecord.copy()
                .apply { module.module = geoNatureModuleName },
            ObservationRecord.Status.TO_SYNC
        )
    }

    private fun buildInputPreferenceKey(id: Long): String {
        return "${KEY_PREFERENCE_INPUT}_$id"
    }

    companion object {
        private const val KEY_PREFERENCE_INPUT = "key_preference_input"
    }
}