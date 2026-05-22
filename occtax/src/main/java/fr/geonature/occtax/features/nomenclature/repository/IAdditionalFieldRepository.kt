package fr.geonature.occtax.features.nomenclature.repository

import fr.geonature.occtax.features.nomenclature.domain.FormField

/**
 * Additional fields repository
 *
 *  @author S. Grimault
 */
interface IAdditionalFieldRepository {

    /**
     * Gets all additional fields as list of [FormField].
     *
     * @param type the main editable nomenclature type
     *
     * @return a list of [FormField]
     */
    suspend fun getAllAdditionalFields(
        datasetId: Long? = null,
        type: FormField.Type
    ): Result<List<FormField>>
}