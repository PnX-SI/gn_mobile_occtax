package fr.geonature.occtax.features.record.presentation

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import androidx.work.WorkManager
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.geonature.commons.interactor.BaseResultUseCase
import fr.geonature.commons.lifecycle.BaseAndroidViewModel
import fr.geonature.occtax.features.record.domain.ObservationRecord
import fr.geonature.occtax.features.record.domain.SynchronizationStatus
import fr.geonature.occtax.features.record.usecase.DeleteObservationRecordUseCase
import fr.geonature.occtax.features.record.usecase.EditObservationRecordUseCase
import fr.geonature.occtax.features.record.usecase.ExportObservationRecordUseCase
import fr.geonature.occtax.features.record.usecase.GetAllObservationRecordsUseCase
import fr.geonature.occtax.features.record.usecase.SaveObservationRecordUseCase
import fr.geonature.occtax.features.record.worker.SynchronizeObservationRecordsWorker
import fr.geonature.occtax.features.settings.domain.AppSettings
import kotlinx.coroutines.delay
import org.tinylog.kotlin.Logger
import java.util.UUID
import javax.inject.Inject

/**
 * [ObservationRecord] view model.
 *
 * @author S. Grimault
 */
@HiltViewModel
class ObservationRecordViewModel @Inject constructor(
    application: Application,
    private val getAllObservationRecordsUseCase: GetAllObservationRecordsUseCase,
    private val saveObservationRecordUseCase: SaveObservationRecordUseCase,
    private val editObservationRecordUseCase: EditObservationRecordUseCase,
    private val deleteObservationRecordUseCase: DeleteObservationRecordUseCase,
    private val exportObservationRecordUseCase: ExportObservationRecordUseCase
) : BaseAndroidViewModel(application) {

    private val workManager: WorkManager = WorkManager.getInstance(getApplication())

    private val _observationRecords = MutableLiveData<List<ObservationRecord>>()
    private val _observationRecord = MutableLiveData<ObservationRecord>()

    /**
     * The current [ObservationRecord] being edited.
     */
    val observationRecord: LiveData<ObservationRecord> = _observationRecord

    private var currentSyncWorkerId: UUID? = null
        set(value) {
            field = value
            _isSyncRunning.postValue(field != null)
        }

    private val _isSyncRunning: MutableLiveData<Boolean> = MutableLiveData(false)
    val isSyncRunning: LiveData<Boolean> = _isSyncRunning

    private val _observeSynchronizationStatus: LiveData<SynchronizationStatus?> =
        workManager.getWorkInfosByTagLiveData(
            SynchronizeObservationRecordsWorker.OBSERVATION_RECORDS_SYNC_WORKER_TAG
        )
            .map { workInfoList ->
                if (workInfoList == null || workInfoList.isEmpty()) {
                    currentSyncWorkerId = null
                    return@map null
                }

                val workInfo = workInfoList.firstOrNull { it.id == currentSyncWorkerId }
                    ?: workInfoList.firstOrNull { it.state == WorkInfo.State.RUNNING }

                // no work info is running: abort
                if (workInfo == null) {
                    currentSyncWorkerId = null
                    return@map null
                }

                // this is a new work info: set the current worker
                if (workInfo.id != currentSyncWorkerId) {
                    currentSyncWorkerId = workInfo.id
                }

                workInfoList.firstOrNull()
                    ?.let {
                        SynchronizeObservationRecordsWorker.toSynchronizationStatus(it)
                    }
                    ?.also {
                        if (it.state.isFinished) currentSyncWorkerId = null
                    }
                    ?: return@map null
            }

    /**
     * All [ObservationRecord]s loaded.
     */
    val observationRecords: LiveData<List<ObservationRecord>> =
        MediatorLiveData<List<ObservationRecord>>().apply {
            addSource(_observationRecords) {
                value = it.map { observationRecord ->
                    if (observationRecord.status == ObservationRecord.Status.DRAFT) return@map observationRecord

                    (value
                        ?: emptyList()).firstOrNull { existingObservationRecord ->
                        existingObservationRecord.internalId == observationRecord.internalId
                    }
                        ?.let { existingObservationRecord ->
                            observationRecord.copy(status = existingObservationRecord.status.takeIf { status ->
                                status.ordinal > observationRecord.status.ordinal
                            }
                                ?: observationRecord.status)
                        }
                        ?: observationRecord
                }
            }
            addSource(_observeSynchronizationStatus.switchMap { synchronizationStatus ->
                liveData {
                    if (synchronizationStatus == null) return@liveData
                    if (synchronizationStatus !is SynchronizationStatus.ObservationRecordStatus) return@liveData

                    emit(synchronizationStatus)

                    if (synchronizationStatus.status == ObservationRecord.Status.SYNC_SUCCESSFUL) {
                        delay(500)
                        value =
                            (value ?: emptyList()).filter {
                                it.internalId != synchronizationStatus.internalId
                            }
                    }
                }
            }) { synchronizationStatus ->
                value = (value ?: emptyList()).map { observationRecord ->
                    if (synchronizationStatus.internalId == observationRecord.internalId) {
                        observationRecord.copy(status = synchronizationStatus.status)
                    } else observationRecord
                }
            }
        }

    /**
     * Whether some [ObservationRecord]s are ready to synchronize according to their current status.
     */
    val hasObservationRecordsReadyToSynchronize =
        observationRecords.map { observationRecords ->
            observationRecords.any {
                it.status == ObservationRecord.Status.TO_SYNC
            }
        }

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
     * @param withAdditionalFields whether we want to manage additional fields
     */
    fun startEdit(
        observationRecord: ObservationRecord,
        withAdditionalFields: Boolean = false
    ) {
        Logger.info { "loading default nomenclature values from record '${observationRecord.internalId}'..." }

        editObservationRecordUseCase(
            EditObservationRecordUseCase.Params(
                observationRecord,
                withAdditionalFields
            ),
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

    /**
     * Synchronizes all eligible [ObservationRecord]s (i.e. with a valid status [ObservationRecord.Status.TO_SYNC]).
     */
    fun synchronizeObservationRecords() {
        currentSyncWorkerId = SynchronizeObservationRecordsWorker.enqueueUniqueWork(
            getApplication()
        )
    }
}