package fr.geonature.occtax.features.record.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.geonature.commons.interactor.BaseResultUseCase
import fr.geonature.commons.lifecycle.BaseViewModel
import fr.geonature.occtax.features.record.domain.ObservationRecord
import fr.geonature.occtax.features.record.usecase.DeleteObservationRecordUseCase
import fr.geonature.occtax.features.record.usecase.EditObservationRecordUseCase
import fr.geonature.occtax.features.record.usecase.ExportObservationRecordUseCase
import fr.geonature.occtax.features.record.usecase.GetAllObservationRecordsUseCase
import fr.geonature.occtax.features.record.usecase.SaveObservationRecordUseCase
import fr.geonature.occtax.settings.AppSettings
import org.tinylog.kotlin.Logger
import javax.inject.Inject

/**
 * [ObservationRecord] view model.
 *
 * @author S. Grimault
 */
@HiltViewModel
class ObservationRecordViewModel @Inject constructor(
    private val getAllObservationRecordsUseCase: GetAllObservationRecordsUseCase,
    private val saveObservationRecordUseCase: SaveObservationRecordUseCase,
    private val editObservationRecordUseCase: EditObservationRecordUseCase,
    private val deleteObservationRecordUseCase: DeleteObservationRecordUseCase,
    private val exportObservationRecordUseCase: ExportObservationRecordUseCase
) : BaseViewModel() {

    private val _observationRecords = MutableLiveData<List<ObservationRecord>>()
    val observationRecords: LiveData<List<ObservationRecord>> = _observationRecords

    private val _observationRecord = MutableLiveData<ObservationRecord>()
    val observationRecord: LiveData<ObservationRecord> = _observationRecord

    /**
     * Gets all [ObservationRecord]s.
     *
     * @param filter additional filter to apply
     */
    fun getAll(filter: (input: ObservationRecord) -> Boolean = { true }) {
        getAllObservationRecordsUseCase(
            BaseResultUseCase.None(),
            viewModelScope
        ) {
            it.fold(
                onSuccess = { observationRecords ->
                    _observationRecords.value =
                        observationRecords.filter { observationRecord -> filter(observationRecord) }
                },
                ::handleError
            )
        }
    }

    /**
     * Start edit the given [ObservationRecord].
     *
     * @param observationRecord the [ObservationRecord] to edit
     */
    fun startEdit(observationRecord: ObservationRecord) {
        Logger.info { "loading default nomenclature values from record '${observationRecord.internalId}'..." }

        editObservationRecordUseCase(
            EditObservationRecordUseCase.Params(observationRecord),
            viewModelScope
        ) {
            it.fold(
                onSuccess = { observationRecordUpdated ->
                    _observationRecord.value = observationRecordUpdated
                },
                ::handleError
            )
        }
    }

    /**
     * Edit the current [ObservationRecord].
     *
     * @param observationRecord the [ObservationRecord] to edit
     */
    fun edit(observationRecord: ObservationRecord) {
        _observationRecord.value = observationRecord
    }

    /**
     * Saves the given [ObservationRecord].
     *
     * @param observationRecord the [ObservationRecord] to save
     */
    fun save(observationRecord: ObservationRecord) {
        saveObservationRecordUseCase(
            SaveObservationRecordUseCase.Params(observationRecord),
            viewModelScope
        ) {
            it.fold(
                onSuccess = { observationRecordSaved ->
                    _observationRecord.value = observationRecordSaved
                    _observationRecords.value =
                        (_observationRecords.value?.filter { existingObservationRecord -> existingObservationRecord.internalId != observationRecordSaved.internalId }
                            ?: emptyList()) + listOf(observationRecordSaved)
                },
                ::handleError
            )
        }
    }

    /**
     * Deletes [ObservationRecord] from given ID.
     *
     * @param observationRecord the [ObservationRecord] to delete
     */
    fun delete(observationRecord: ObservationRecord) {
        deleteObservationRecordUseCase(
            DeleteObservationRecordUseCase.Params(observationRecord),
            viewModelScope
        ) {
            it.fold(
                onSuccess = {
                    _observationRecords.value =
                        _observationRecords.value?.filter { existingObservationRecord -> existingObservationRecord.internalId != observationRecord.internalId }
                },
                ::handleError
            )
        }
    }

    /**
     * Exports the given [ObservationRecord] as `JSON` file.
     *
     * @param observationRecord the [ObservationRecord] to save
     * @param settings additional settings
     */
    fun export(
        observationRecord: ObservationRecord,
        settings: AppSettings? = null,
        exported: () -> Unit = {}
    ) {
        exportObservationRecordUseCase(
            ExportObservationRecordUseCase.Params(
                observationRecord,
                settings
            ),
            viewModelScope
        ) {
            it.fold(
                onSuccess = { inputExported ->
                    _observationRecord.value = inputExported
                    _observationRecords.value =
                        (_observationRecords.value?.filter { existingObservationRecord -> existingObservationRecord.internalId != inputExported.internalId }
                            ?: emptyList()) + listOf(inputExported)

                    exported()
                },
                ::handleError
            )
        }
    }
}