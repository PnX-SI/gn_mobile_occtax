package fr.geonature.occtax.features.record.usecase

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import fr.geonature.commons.data.entity.Dataset
import fr.geonature.commons.features.dataset.repository.IDatasetRepository
import fr.geonature.commons.interactor.BaseResultUseCase
import fr.geonature.occtax.features.record.domain.ObservationRecord
import fr.geonature.occtax.util.SettingsUtils
import org.tinylog.kotlin.Logger
import javax.inject.Inject

/**
 * Loads and sets [Dataset] to a given [ObservationRecord]. If no dataset was set, use the default
 * one.
 *
 * @author S. Grimault
 */
class SetDefaultDatasetUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val datasetRepository: IDatasetRepository
) : BaseResultUseCase<ObservationRecord, SetDefaultDatasetUseCase.Params>() {

    override suspend fun run(params: Params): Result<ObservationRecord> {
        val observationRecord = params.observationRecord

        val dataset = (observationRecord.dataset.dataset?.value?.id
            ?: SettingsUtils.getDefaultDatasetId(context))?.let {
            datasetRepository.getDatasetById(it)
                .onFailure {
                    Logger.error { "failed to load selected dataset from record '${observationRecord.internalId}" }
                }
                .getOrNull()
        }

        if (dataset == null) {
            Logger.info { "no default dataset found" }
        } else {
            Logger.info { "current dataset: ${dataset.id}, taxa list ID: ${dataset.taxaListId}" }
        }

        observationRecord.dataset.setDataset(dataset)

        return Result.success(observationRecord)
    }

    data class Params(
        val observationRecord: ObservationRecord
    )
}
