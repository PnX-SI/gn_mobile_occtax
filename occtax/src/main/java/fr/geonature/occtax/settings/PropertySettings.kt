package fr.geonature.occtax.settings

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Property settings.
 *
 * @author S. Grimault
 */
@Parcelize
data class PropertySettings(
    val key: String,
    val visible: Boolean,
    val default: Boolean
) : Parcelable