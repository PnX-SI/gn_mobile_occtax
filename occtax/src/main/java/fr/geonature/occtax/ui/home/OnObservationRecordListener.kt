package fr.geonature.occtax.ui.home

import fr.geonature.occtax.features.record.domain.ObservationRecord

/**
 * Callback used by any fragment added to the view pager in [HomeActivity].
 *
 * @see ObservationRecordsListFragment
 * @see ObservationRecordsMapFragment
 *
 * @author S. Grimault
 */
interface OnObservationRecordListener {

    /**
     * Called when we want to start editing a given [ObservationRecord].
     * If no [ObservationRecord] was given, creates a new one.
     */
    fun onStartEditObservationRecord(selectedObservationRecord: ObservationRecord? = null)
}