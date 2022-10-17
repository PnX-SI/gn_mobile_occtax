package fr.geonature.occtax.features.nomenclature.data

import fr.geonature.occtax.features.nomenclature.domain.EditableNomenclatureType
import fr.geonature.occtax.settings.PropertySettings

/**
 * Local data source about nomenclature types settings.
 *
 * @author S. Grimault
 */
interface INomenclatureSettingsLocalDataSource {

    /**
     * Gets all [EditableNomenclatureType] matching given nomenclature main type.
     *
     * @return a list of [EditableNomenclatureType]
     */
    suspend fun getNomenclatureTypeSettings(
        type: EditableNomenclatureType.Type,
        vararg defaultPropertySettings: PropertySettings
    ): List<EditableNomenclatureType>
}