package fr.geonature.occtax.features.record.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.geonature.commons.interactor.BaseResultUseCase
import fr.geonature.commons.lifecycle.BaseViewModel
import fr.geonature.occtax.features.record.domain.ObservationRecord
import fr.geonature.occtax.features.record.usecase.DeleteObservationRecordUseCase
import fr.geonature.occtax.features.record.usecase.ExportObservationRecordUseCase
import fr.geonature.occtax.features.record.usecase.GetAllObservationRecordsUseCase
import fr.geonature.occtax.features.record.usecase.LoadAllMediaRecordUseCase
import fr.geonature.occtax.features.record.usecase.SaveObservationRecordUseCase
import fr.geonature.occtax.features.record.usecase.SetDefaultNomenclatureValuesUseCase
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
    private val setDefaultNomenclatureValuesUseCase: SetDefaultNomenclatureValuesUseCase,
    private val loadAllMediaRecordUseCase: LoadAllMediaRecordUseCase,
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
     * Edit current [ObservationRecord].
     *
     * @param observationRecord the [ObservationRecord] to edit
     */
    fun edit(observationRecord: ObservationRecord) {
        _observationRecord.value = observationRecord
    }

    /**
     * Loads and set all default nomenclature values to the given [ObservationRecord].
     *
     * @param observationRecord the [ObservationRecord] to update
     */
    fun loadDefaultNomenclatureValues(observationRecord: ObservationRecord) {
        Logger.info { "loading default nomenclature values from record '${observationRecord.internalId}'..." }

        setDefaultNomenclatureValuesUseCase(
            SetDefaultNomenclatureValuesUseCase.Params(observationRecord),
            viewModelScope
        ) {
            it.fold(
                onSuccess = { observationRecordUpdated ->
                    Logger.info { "default nomenclature values successfully loaded for record '${observationRecordUpdated.internalId}'" }

                    _observationRecord.value = observationRecordUpdated
                },
                ::handleError
            )
        }
    }

    fun loadAllMedias(observationRecord: ObservationRecord) {
        Logger.info { "loading all local medias from record '${observationRecord.internalId}'..." }

        loadAllMediaRecordUseCase(
            LoadAllMediaRecordUseCase.Params(observationRecord),
            viewModelScope
        ) {
            it.fold(
                onSuccess = { observationRecordUpdated ->
                    Logger.info { "all medias successfully loaded for record '${observationRecordUpdated.internalId}'" }

                    _observationRecord.value = observationRecordUpdated
                },
                ::handleError
            )
        }
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