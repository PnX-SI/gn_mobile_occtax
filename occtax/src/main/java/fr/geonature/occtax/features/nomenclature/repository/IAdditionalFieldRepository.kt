package fr.geonature.occtax.features.nomenclature.repository

import fr.geonature.occtax.features.nomenclature.domain.EditableField

/**
 * Additional fields repository
 *
 *  @author S. Grimault
 */
interface IAdditionalFieldRepository {

    /**
     * Gets all additional fields as list of [EditableField].
     *
     * @param type the main editable nomenclature type
     *
     * @return a list of [EditableField]
     */
    suspend fun getAllAdditionalFields(
        datasetId: Long? = null,
        type: EditableField.Type
    ): Result<List<EditableField>>
}