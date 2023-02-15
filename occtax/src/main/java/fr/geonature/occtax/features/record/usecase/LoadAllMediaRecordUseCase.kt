package fr.geonature.occtax.features.record.usecase

import fr.geonature.commons.interactor.BaseResultUseCase
import fr.geonature.occtax.features.record.domain.ObservationRecord
import fr.geonature.occtax.features.record.repository.IMediaRecordRepository
import org.tinylog.kotlin.Logger
import javax.inject.Inject

/**
 * Loads all local medias to a given [ObservationRecord].
 *
 * @author S. Grimault
 */
class LoadAllMediaRecordUseCase @Inject constructor(
    private val mediaRecordRepository: IMediaRecordRepository
) :
    BaseResultUseCase<ObservationRecord, LoadAllMediaRecordUseCase.Params>() {

    override suspend fun run(params: Params): Result<ObservationRecord> {
        val observationRecord = params.observationRecord

        observationRecord.taxa.taxa.forEach { taxonRecord ->
            taxonRecord.counting.counting.forEach { countingRecord ->
                val files = mediaRecordRepository.loadAll(
                    taxonRecord,
                    countingRecord
                )
                    .onFailure { Logger.warn { "failed to load local medias for counting #${countingRecord.index} of taxon '${taxonRecord.taxon.id}'" } }
                    .getOrElse { emptyList() }
                    .map { it.absolutePath }

                countingRecord.medias.files = files
            }
        }

        return Result.success(observationRecord)
    }

    data class Params(val observationRecord: ObservationRecord)
}