package fr.geonature.occtax.features.record.usecase

import fr.geonature.commons.interactor.BaseResultUseCase
import fr.geonature.occtax.features.record.domain.ObservationRecord
import fr.geonature.occtax.features.record.repository.IObservationRecordRepository
import javax.inject.Inject

/**
 * Gets all [ObservationRecord]s.
 *
 * @author S. Grimault
 */
class GetAllObservationRecordsUseCase @Inject constructor(
    private val observationRecordRepository: IObservationRecordRepository
) :
    BaseResultUseCase<List<ObservationRecord>, BaseResultUseCase.None>() {

    override suspend fun run(params: None): Result<List<ObservationRecord>> {
        return observationRecordRepository.readAll()
    }
}