package fr.geonature.occtax.features.nomenclature.repository

import fr.geonature.commons.error.Failure
import fr.geonature.commons.fp.Either
import fr.geonature.occtax.features.nomenclature.domain.EditableNomenclatureType
import fr.geonature.occtax.settings.PropertySettings
import fr.geonature.commons.features.nomenclature.repository.INomenclatureRepository as IBaseNomenclatureRepository

/**
 * Editable nomenclature types repository based from [IBaseNomenclatureRepository].
 *
 * @author S. Grimault
 * @see IBaseNomenclatureRepository
 */
interface INomenclatureRepository : IBaseNomenclatureRepository {

    /**
     * Gets all editable nomenclatures from given type with default values from nomenclature.
     *
     * @param type the main editable nomenclature type
     * @param defaultPropertySettings the default nomenclature settings
     *
     * @return a list of [EditableNomenclatureType] or [Failure] if none was configured
     */
    suspend fun getEditableNomenclatures(
        type: EditableNomenclatureType.Type,
        vararg defaultPropertySettings: PropertySettings
    ): Either<Failure, List<EditableNomenclatureType>>
}