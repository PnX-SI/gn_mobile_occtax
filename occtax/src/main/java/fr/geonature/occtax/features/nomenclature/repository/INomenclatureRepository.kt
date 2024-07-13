package fr.geonature.occtax.features.nomenclature.repository

import fr.geonature.occtax.features.nomenclature.domain.EditableField
import fr.geonature.occtax.features.settings.domain.PropertySettings
import fr.geonature.commons.features.nomenclature.repository.INomenclatureRepository as IBaseNomenclatureRepository

/**
 * Editable nomenclature types repository based from [IBaseNomenclatureRepository].
 *
 * @author S. Grimault
 * @see IBaseNomenclatureRepository
 */
interface INomenclatureRepository : IBaseNomenclatureRepository {

    /**
     * Gets all editable fields from given type with default values from nomenclature.
     *
     * @param type the main editable field type
     * @param defaultPropertySettings the default nomenclature settings
     *
     * @return a list of [EditableField] or [Result.Failure] if none was configured
     */
    suspend fun getEditableFields(
        type: EditableField.Type,
        vararg defaultPropertySettings: PropertySettings
    ): Result<List<EditableField>>
}