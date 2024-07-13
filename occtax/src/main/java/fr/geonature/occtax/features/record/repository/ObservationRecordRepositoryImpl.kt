package fr.geonature.occtax.features.record.repository

import fr.geonature.commons.features.taxon.data.ITaxonLocalDataSource
import fr.geonature.occtax.features.record.data.IObservationRecordLocalDataSource
import fr.geonature.occtax.features.record.domain.ObservationRecord
import fr.geonature.occtax.features.settings.domain.AppSettings

/**
 * Default implementation of [IObservationRecordRepository].
 *
 * @author S. Grimault
 */
class ObservationRecordRepositoryImpl(
    private val observationRecordLocalDataSource: IObservationRecordLocalDataSource,
    private val taxonLocalDataSource: ITaxonLocalDataSource
) : IObservationRecordRepository {

    override suspend fun readAll(): Result<List<ObservationRecord>> {
        return runCatching {
            observationRecordLocalDataSource.readAll()
                .map { loadTaxa(it) }
        }
    }

    override suspend fun read(id: Long): Result<ObservationRecord> {
        return runCatching {
            loadTaxa(observationRecordLocalDataSource.read(id))
        }
    }

    override suspend fun save(
        observationRecord: ObservationRecord,
        status: ObservationRecord.Status
    ): Result<ObservationRecord> {
        return runCatching {
            observationRecordLocalDataSource.save(
                observationRecord,
                status
            )
        }
    }

    override suspend fun delete(id: Long): Result<ObservationRecord> {
        return runCatching { observationRecordLocalDataSource.delete(id) }
    }

    override suspend fun export(id: Long, settings: AppSettings?): Result<ObservationRecord> {
        return runCatching {
            observationRecordLocalDataSource.export(
                id,
                settings
            )
        }
    }

    override suspend fun export(
        observationRecord: ObservationRecord,
        settings: AppSettings?
    ): Result<ObservationRecord> {
        return runCatching {
            observationRecordLocalDataSource.export(
                observationRecord,
                settings
            )
        }
    }

    /**
     * Loads all taxa added to the given [ObservationRecord] from local datasource.
     */
    private suspend fun loadTaxa(observationRecord: ObservationRecord): ObservationRecord {
        val taxaFoundFromLocalDataSource =
            taxonLocalDataSource.findTaxaByIds(*observationRecord.taxa.taxa.map { it.taxon.id }
                .toLongArray())

        return observationRecord.apply {
            taxa.taxa = taxa.taxa.map {
                it.copy(taxon = taxaFoundFromLocalDataSource.firstOrNull { taxon -> taxon.id == it.taxon.id }
                    ?: it.taxon)
            }
        }
    }
}