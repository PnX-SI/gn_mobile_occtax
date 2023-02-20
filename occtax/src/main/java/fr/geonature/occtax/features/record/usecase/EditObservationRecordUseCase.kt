package fr.geonature.occtax.features.record.usecase

import fr.geonature.commons.interactor.BaseResultUseCase
import fr.geonature.occtax.features.record.domain.ObservationRecord
import org.tinylog.kotlin.Logger
import javax.inject.Inject

/**
 * Start editing the given [ObservationRecord].
 *
 * @author S. Grimault
 *
 * @see SetDefaultNomenclatureValuesUseCase
 * @see LoadAllMediaRecordUseCase
 */
class EditObservationRecordUseCase @Inject constructor(
    private val setDefaultNomenclatureValuesUseCase: SetDefaultNomenclatureValuesUseCase,
    private val loadAllMediaRecordUseCase: LoadAllMediaRecordUseCase
) :
    BaseResultUseCase<ObservationRecord, EditObservationRecordUseCase.Params>() {

    override suspend fun run(params: Params): Result<ObservationRecord> {
        Logger.info { "loading default nomenclature values from record '${params.observationRecord.internalId}'..." }

        var result =
            setDefaultNomenclatureValuesUseCase.run(SetDefaultNomenclatureValuesUseCase.Params(params.observationRecord))

        if (result.isFailure) {
            Logger.error { "failed to load default nomenclature values from record '${params.observationRecord.internalId}" }
            return result
        }

        val observationRecordUpdated = result.getOrNull() ?: params.observationRecord

        Logger.info { "default nomenclature values successfully loaded for record '${observationRecordUpdated.internalId}'" }
        Logger.info { "loading all local medias from record '${observationRecordUpdated.internalId}'..." }

        result =
            loadAllMediaRecordUseCase.run(LoadAllMediaRecordUseCase.Params(observationRecordUpdated))

        if (result.isFailure) {
            Logger.warn { "failed to load all local medias from record '${observationRecordUpdated.internalId}'" }
            return result
        }

        Logger.info { "all medias successfully loaded for record '${observationRecordUpdated.internalId}'" }

        return result
    }

    data class Params(val observationRecord: ObservationRecord)
}