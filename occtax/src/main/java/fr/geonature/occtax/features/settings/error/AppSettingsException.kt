package fr.geonature.occtax.features.settings.error

import fr.geonature.occtax.features.settings.domain.AppSettings

/**
 * Base exception about [AppSettings].
 *
 * @author S. Grimault
 */
sealed class AppSettingsException(
    message: String? = null,
    cause: Throwable? = null
) : RuntimeException(
    message,
    cause
) {

    /**
     * Handles no [AppSettings] found locally.
     */
    object NotFoundException : AppSettingsException("no app settings found locally")

    /**
     * Handles no [AppSettings] found locally.
     */
    data class NoAppSettingsFoundLocallyException(val path: String) :
        AppSettingsException("no app settings found locally at '$path'")

    /**
     * Handles missing attribute from `JSON`.
     */
    data class MissingAttributeException(val attributeName: String) :
        AppSettingsException("missing '$attributeName' attribute configuration")

    /**
     * Thrown if [AppSettings] cannot be parsed from `JSON`.
     */
    class JsonParseException(message: String?) : AppSettingsException(message)
}
