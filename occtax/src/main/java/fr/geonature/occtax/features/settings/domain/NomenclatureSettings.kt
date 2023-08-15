package fr.geonature.occtax.features.settings.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Nomenclature settings.
 *
 * @author S. Grimault
 */
@Parcelize
data class NomenclatureSettings(
    /**
     * Whether we want to save locally and only during a session of use selected nomenclature values
     * as default values.
     */
    val saveDefaultValues: Boolean = false,

    /**
     * Whether we want to show additional fields
     */
    val withAdditionalFields: Boolean = false,

    val information: List<PropertySettings>,
    val counting: List<PropertySettings>
) : Parcelable