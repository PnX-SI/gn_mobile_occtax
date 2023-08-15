package fr.geonature.occtax.features.nomenclature.data

import fr.geonature.occtax.features.nomenclature.domain.EditableField
import fr.geonature.occtax.features.settings.domain.PropertySettings

/**
 * Local data source about nomenclature types settings.
 *
 * @author S. Grimault
 */
interface INomenclatureSettingsLocalDataSource {

    /**
     * Gets all [EditableField] matching given nomenclature main type.
     * If the default main type is requested, returns all default [EditableField]
     * whatever the given [PropertySettings].
     *
     * @return a list of [EditableField]
     */
    suspend fun getNomenclatureTypeSettings(
        type: EditableField.Type,
        vararg defaultPropertySettings: PropertySettings
    ): List<EditableField>
}