package fr.geonature.occtax.features.record.data

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import fr.geonature.commons.util.getInputsFolder
import fr.geonature.mountpoint.util.FileUtils
import fr.geonature.occtax.features.record.domain.ObservationRecord
import fr.geonature.occtax.features.record.error.ObservationRecordException
import fr.geonature.occtax.features.record.io.ObservationRecordJsonReader
import fr.geonature.occtax.features.record.io.ObservationRecordJsonWriter
import fr.geonature.occtax.settings.AppSettings
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext
import org.tinylog.Logger
import java.io.File

/**
 * Default implementation of [IObservationRecordLocalDataSource] using [SharedPreferences].
 *
 * @author S. Grimault
 */
class ObservationRecordLocalDataSourceImpl(
    private val context: Context,
    private val geoNatureModuleName: String,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : IObservationRecordLocalDataSource {

    private val preferenceManager: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)
    private val observationRecordJsonReader: ObservationRecordJsonReader =
        ObservationRecordJsonReader()
    private val observationRecordJsonWriter: ObservationRecordJsonWriter =
        ObservationRecordJsonWriter()

    override suspend fun readAll(): List<ObservationRecord> {
        val exportedInput = FileUtils
            .getInputsFolder(context)
            .walkTopDown()
            .asFlow()
            .filter { it.isFile && it.extension == "json" }
            .filter { it.nameWithoutExtension.startsWith("input") }
            .filter { it.canRead() }
            .map {
                val input =
                    runCatching { observationRecordJsonReader.read(it.readText()) }.getOrNull()

                if (input == null) {
                    Logger.warn { "invalid exported observation record file found '${it.name}'" }

                    it.delete()

                    return@map null
                }

                input
            }
            .filterNotNull()
            .toList()

        return withContext(dispatcher) {
            (
                exportedInput + preferenceManager.all
                    .filterKeys { it.startsWith("${KEY_PREFERENCE_INPUT}_") }
                    .values
                    .mapNotNull { if (it is String && it.isNotBlank()) runCatching { observationRecordJsonReader.read(it) }.getOrNull() else null }
                ).sortedBy { it.internalId }
        }
    }

    override suspend fun read(id: Long): ObservationRecord = withContext(dispatcher) {
        val inputAsJson = preferenceManager.getString(
            buildInputPreferenceKey(id),
            null
        )
            ?: File(
                FileUtils
                    .getInputsFolder(context)
                    .also { it.mkdirs() },
                "input_${id}.json"
            )
                .takeIf { it.exists() }
                ?.readText()

        if (inputAsJson.isNullOrBlank()) {
            throw ObservationRecordException.NotFoundException(id)
        }

        runCatching { observationRecordJsonReader.read(inputAsJson) }
            .onFailure { throw ObservationRecordException.ReadException(id) }
            .getOrThrow()
    }

    override suspend fun save(
        observationRecord: ObservationRecord,
        status: ObservationRecord.Status
    ): ObservationRecord =
        withContext(dispatcher) {
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

            File(
                FileUtils
                    .getInputsFolder(context)
                    .also { it.mkdirs() },
                "input_${savedObservationRecord.internalId}.json"
            )
                .takeIf { it.exists() }
                ?.delete()

            savedObservationRecord
        }

    override suspend fun delete(id: Long): ObservationRecord {
        val observationRecordToDelete = read(id)

        return withContext(dispatcher) {
            File(
                FileUtils
                    .getInputsFolder(context)
                    .also { it.mkdirs() },
                "input_${id}.json"
            )
                .takeIf { it.exists() }
                ?.delete()
            File(
                FileUtils.getInputsFolder(context),
                "$id"
            ).takeIf { it.exists() }
                ?.deleteRecursively()

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
        val observationRecordToSync =
            observationRecord.copy(status = ObservationRecord.Status.TO_SYNC)
                .apply { module.module = geoNatureModuleName }

        if (preferenceManager.contains(buildInputPreferenceKey(observationRecord.internalId))) {
            preferenceManager
                .edit()
                .remove(buildInputPreferenceKey(observationRecord.internalId))
                .apply()
        }

        return withContext(dispatcher) {
            val inputAsJson =
                runCatching { observationRecordJsonWriter.write(observationRecordToSync) }.getOrNull()
            if (inputAsJson.isNullOrBlank()) throw ObservationRecordException.WriteException(observationRecordToSync.internalId)

            File(
                FileUtils
                    .getInputsFolder(context)
                    .also { it.mkdirs() },
                "input_${observationRecordToSync.internalId}.json"
            )
                .bufferedWriter()
                .use { out ->
                    out.write(inputAsJson)
                    out.flush()
                    out.close()
                }

            observationRecordToSync
        }
    }

    private fun buildInputPreferenceKey(id: Long): String {
        return "${KEY_PREFERENCE_INPUT}_$id"
    }

    companion object {
        private const val KEY_PREFERENCE_INPUT = "key_preference_input"
    }
}