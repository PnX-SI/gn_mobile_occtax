package fr.geonature.occtax.features.record.usecase

import fr.geonature.commons.interactor.BaseResultUseCase
import fr.geonature.occtax.features.record.domain.ObservationRecord
import fr.geonature.occtax.features.record.repository.IObservationRecordRepository
import fr.geonature.occtax.settings.AppSettings
import javax.inject.Inject

/**
 * Exports a given [ObservationRecord].
 *
 * @author S. Grimault
 */
class ExportObservationRecordUseCase @Inject constructor(private val observationRecordRepository: IObservationRecordRepository) :
    BaseResultUseCase<ObservationRecord, ExportObservationRecordUseCase.Params>() {

    override suspend fun run(params: Params): Result<ObservationRecord> {
        return observationRecordRepository.export(
            params.observationRecord,
            params.settings
        )
    }

    data class Params(val observationRecord: ObservationRecord, val settings: AppSettings? = null)
}