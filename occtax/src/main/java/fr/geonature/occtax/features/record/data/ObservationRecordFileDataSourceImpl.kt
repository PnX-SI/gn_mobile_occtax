package fr.geonature.occtax.features.record.data

import android.content.Context
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
 * File based implementation of [IObservationRecordLocalDataSource].
 *
 * @author S. Grimault
 */
class ObservationRecordFileDataSourceImpl(
    private val context: Context,
    private val geoNatureModuleName: String,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : IObservationRecordLocalDataSource {

    private val observationRecordJsonReader: ObservationRecordJsonReader =
        ObservationRecordJsonReader()
    private val observationRecordJsonWriter: ObservationRecordJsonWriter =
        ObservationRecordJsonWriter()

    override suspend fun readAll(): List<ObservationRecord> {
        return FileUtils
            .getInputsFolder(context)
            .walkTopDown()
            .asFlow()
            .filter { it.isFile && it.extension == "json" }
            .filter { it.nameWithoutExtension.startsWith("input") }
            .filter { it.canRead() }
            .map {
                val observationRecord =
                    runCatching { observationRecordJsonReader.read(it.readText()) }.getOrNull()

                if (observationRecord == null) {
                    Logger.warn { "invalid observation record file found '${it.name}'" }

                    it.delete()

                    return@map null
                }

                observationRecord
            }
            .filterNotNull()
            .toList()
            .sortedBy { it.internalId }
    }

    override suspend fun read(id: Long): ObservationRecord = withContext(dispatcher) {
        val inputAsJson = File(
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
    ): ObservationRecord = withContext(dispatcher) {
        val savedObservationRecord = observationRecord.copy(status = status)
        val asJson = runCatching { observationRecordJsonWriter.write(savedObservationRecord) }
            .getOrNull()

        if (asJson.isNullOrBlank()) throw ObservationRecordException.WriteException(savedObservationRecord.internalId)

        File(
            FileUtils
                .getInputsFolder(context)
                .also { it.mkdirs() },
            "input_${savedObservationRecord.internalId}.json"
        )
            .bufferedWriter()
            .use { out ->
                out.write(asJson)
                out.flush()
                out.close()
            }

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

            observationRecordToDelete
        }
    }

    override suspend fun export(id: Long, settings: AppSettings?): ObservationRecord {
        val observationRecordToExport = read(id)

        return export(
            observationRecordToExport,
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

        return withContext(dispatcher) {
            val asJson =
                runCatching { observationRecordJsonWriter.write(observationRecordToSync) }.getOrNull()
            if (asJson.isNullOrBlank()) throw ObservationRecordException.WriteException(observationRecordToSync.internalId)

            File(
                FileUtils
                    .getInputsFolder(context)
                    .also { it.mkdirs() },
                "input_${observationRecordToSync.internalId}.json"
            )
                .bufferedWriter()
                .use { out ->
                    out.write(asJson)
                    out.flush()
                    out.close()
                }

            observationRecordToSync
        }
    }
}