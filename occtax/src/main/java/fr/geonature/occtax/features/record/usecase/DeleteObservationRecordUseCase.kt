package fr.geonature.occtax.features.record.usecase

import fr.geonature.commons.interactor.BaseResultUseCase
import fr.geonature.occtax.features.record.domain.ObservationRecord
import fr.geonature.occtax.features.record.repository.IObservationRecordRepository
import javax.inject.Inject

/**
 * Deletes a given [ObservationRecord].
 *
 * @author S. Grimault
 */
class DeleteObservationRecordUseCase @Inject constructor(private val observationRecordRepository: IObservationRecordRepository) :
    BaseResultUseCase<ObservationRecord, DeleteObservationRecordUseCase.Params>() {

    override suspend fun run(params: Params): Result<ObservationRecord> {
        return observationRecordRepository.delete(params.observationRecord.internalId)
    }

    data class Params(val observationRecord: ObservationRecord)
}