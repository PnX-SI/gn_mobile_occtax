package fr.geonature.occtax.features.settings.domain

import android.os.Parcelable
import fr.geonature.occtax.features.record.domain.ObservationRecord
import kotlinx.parcelize.Parcelize

/**
 * [ObservationRecord] settings.
 *
 * @author S. Grimault
 */
@Parcelize
data class InputSettings(val dateSettings: InputDateSettings) : Parcelable