package fr.geonature.occtax.features.record.usecase

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import fr.geonature.commons.data.entity.InputObserver
import fr.geonature.commons.features.inputObservers.repository.IInputObserverRepository
import fr.geonature.commons.interactor.BaseResultUseCase
import fr.geonature.occtax.features.record.domain.ObservationRecord
import fr.geonature.occtax.util.SettingsUtils.getDefaultObserversId
import org.tinylog.kotlin.Logger
import javax.inject.Inject

/**
 * Loads and sets [InputObserver]s to a given [ObservationRecord]. If no [InputObserver]s was set,
 * use the default ones.
 *
 * @author S. Grimault
 */
class SetDefaultInputObserversUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val inputObserverRepository: IInputObserverRepository
) : BaseResultUseCase<ObservationRecord, SetDefaultInputObserversUseCase.Params>() {

    override suspend fun run(params: Params): Result<ObservationRecord> {
        val observationRecord = params.observationRecord

        val inputObservers =
            inputObserverRepository.findInputObserversByIds(*(observationRecord.observers.getAllObserverIds()
                .takeIf { it.isNotEmpty() }
            // not defined, use default
                ?: getDefaultObserversId(context)).toLongArray())
                .onFailure {
                    Logger.error { "failed to load input observers from record '${observationRecord.internalId}" }
                }
                .getOrDefault(emptyList())

        if (inputObservers.isEmpty()) {
            Logger.info { "no default input observers found" }
        }

        observationRecord.observers.setObservers(inputObservers)

        return Result.success(observationRecord)
    }

    data class Params(
        val observationRecord: ObservationRecord
    )
}