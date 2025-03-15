package fr.geonature.occtax.features.nomenclature.data

import fr.geonature.occtax.features.nomenclature.domain.FormField
import fr.geonature.occtax.features.settings.domain.PropertySettings

/**
 * Local data source about nomenclature types settings.
 *
 * @author S. Grimault
 */
interface INomenclatureSettingsLocalDataSource {

    /**
     * Gets all [FormField] matching given nomenclature main type.
     * If the default main type is requested, returns all default [FormField]
     * whatever the given [PropertySettings].
     *
     * @return a list of [FormField]
     */
    suspend fun getNomenclatureTypeSettings(
        type: FormField.Type,
        vararg defaultPropertySettings: PropertySettings
    ): List<FormField>
}