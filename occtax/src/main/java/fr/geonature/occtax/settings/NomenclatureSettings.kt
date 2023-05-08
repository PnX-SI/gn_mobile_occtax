package fr.geonature.occtax.settings

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Nomenclature settings.
 *
 * @author S. Grimault
 */
@Parcelize
data class NomenclatureSettings(
    val saveDefaultValues: Boolean = false,
    val information: List<PropertySettings>,
    val counting: List<PropertySettings>
) : Parcelable