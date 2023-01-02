package fr.geonature.occtax.features.record.usecase

import fr.geonature.commons.interactor.BaseResultUseCase
import fr.geonature.occtax.features.record.domain.ObservationRecord
import fr.geonature.occtax.features.record.repository.IObservationRecordRepository
import javax.inject.Inject

/**
 * Saves an [ObservationRecord].
 *
 * @author S. Grimault
 */
class SaveObservationRecordUseCase @Inject constructor(private val observationRecordRepository: IObservationRecordRepository) :
    BaseResultUseCase<ObservationRecord, SaveObservationRecordUseCase.Params>() {

    override suspend fun run(params: Params): Result<ObservationRecord> {
        return observationRecordRepository.save(params.observationRecord.copy(status = ObservationRecord.Status.DRAFT))
    }

    data class Params(val observationRecord: ObservationRecord)
}

