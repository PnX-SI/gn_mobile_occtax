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
    private val setDefaultDatasetUseCase: SetDefaultDatasetUseCase,
    private val setDefaultInputObserversUseCase: SetDefaultInputObserversUseCase,
    private val setDefaultNomenclatureValuesUseCase: SetDefaultNomenclatureValuesUseCase,
    private val loadAllMediaRecordUseCase: LoadAllMediaRecordUseCase
) : BaseResultUseCase<ObservationRecord, EditObservationRecordUseCase.Params>() {

    override suspend fun run(params: Params): Result<ObservationRecord> {
        val observationRecord = params.observationRecord

        loadSelectedDataset(observationRecord)
        loadSelectedObservers(observationRecord)
        loadDefaultNomenclatureValues(
            observationRecord,
            params.withAdditionalFields
        )
        loadAllMedia(observationRecord)

        return Result.success(observationRecord)
    }

    data class Params(
        val observationRecord: ObservationRecord,
        val withAdditionalFields: Boolean = false
    )

    private suspend fun loadSelectedDataset(observationRecord: ObservationRecord): Result<ObservationRecord> {
        Logger.info { "loading selected dataset from record '${observationRecord.internalId}'..." }

        return setDefaultDatasetUseCase.run(SetDefaultDatasetUseCase.Params(observationRecord))
            .onFailure {
                Logger.warn { "failed to load selected dataset from record '${observationRecord.internalId}" }
            }
    }

    private suspend fun loadSelectedObservers(observationRecord: ObservationRecord): Result<ObservationRecord> {
        Logger.info { "loading selected observers from record '${observationRecord.internalId}'..." }

        return setDefaultInputObserversUseCase.run(SetDefaultInputObserversUseCase.Params(observationRecord))
            .onFailure {
                Logger.warn { "failed to load selected observers from record '${observationRecord.internalId}" }
            }
    }

    private suspend fun loadDefaultNomenclatureValues(
        observationRecord: ObservationRecord,
        withAdditionalFields: Boolean = false
    ): Result<ObservationRecord> {
        Logger.info { "loading default nomenclature values from record '${observationRecord.internalId}'..." }

        return setDefaultNomenclatureValuesUseCase.run(
            SetDefaultNomenclatureValuesUseCase.Params(
                observationRecord,
                withAdditionalFields
            )
        )
            .onFailure {
                Logger.warn { "failed to load default nomenclature values from record '${observationRecord.internalId}" }
            }
            .onSuccess {
                Logger.info { "default nomenclature values successfully loaded for record '${observationRecord.internalId}'" }
            }
    }

    private suspend fun loadAllMedia(observationRecord: ObservationRecord): Result<ObservationRecord> {
        Logger.info { "loading all local medias from record '${observationRecord.internalId}'..." }

        return loadAllMediaRecordUseCase.run(LoadAllMediaRecordUseCase.Params(observationRecord))
            .onFailure {
                Logger.warn { "failed to load all local medias from record '${observationRecord.internalId}'" }
            }
            .onSuccess {
                Logger.info { "all medias successfully loaded for record '${observationRecord.internalId}'" }
            }
    }
}