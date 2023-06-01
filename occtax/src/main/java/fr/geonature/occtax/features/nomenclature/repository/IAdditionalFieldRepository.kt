package fr.geonature.occtax.features.nomenclature.repository

import fr.geonature.occtax.features.nomenclature.domain.EditableNomenclatureType

/**
 * Additional fields repository
 *
 *  @author S. Grimault
 */
interface IAdditionalFieldRepository {

    /**
     * Gets all additional fields as list of [EditableNomenclatureType].
     *
     * @param type the main editable nomenclature type
     *
     * @return a list of [EditableNomenclatureType]
     */
    suspend fun getAllAdditionalFields(
        datasetId: Long? = null,
        type: EditableNomenclatureType.Type
    ): Result<List<EditableNomenclatureType>>
}