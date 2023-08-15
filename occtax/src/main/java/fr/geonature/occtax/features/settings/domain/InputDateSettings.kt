package fr.geonature.occtax.features.settings.domain

import android.os.Parcelable
import fr.geonature.occtax.features.record.domain.ObservationRecord
import kotlinx.parcelize.Parcelize

/**
 * [ObservationRecord] date settings.
 *
 * @author S. Grimault
 */
@Parcelize
data class InputDateSettings(
    val startDateSettings: DateSettings? = null,
    val endDateSettings: DateSettings? = null
) : Parcelable {

    companion object {
        val DEFAULT = InputDateSettings(startDateSettings = DateSettings.DATE)
    }

    enum class DateSettings {
        DATE,
        DATETIME
    }
}
