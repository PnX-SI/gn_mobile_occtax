package fr.geonature.occtax.features.nomenclature.repository

import fr.geonature.commons.data.entity.Taxonomy
import fr.geonature.commons.error.Failure
import fr.geonature.commons.fp.Either
import fr.geonature.occtax.input.PropertyValue

/**
 * Default [PropertyValue] repository.
 *
 * @author S. Grimault
 */
interface IDefaultPropertyValueRepository {

    /**
     * Gets all defined property values.
     *
     * @param taxonomy the taxonomy rank as filter
     */
    suspend fun getPropertyValues(taxonomy: Taxonomy? = null): Either<Failure, List<PropertyValue>>

    /**
     * Adds or updates given property value for the given given taxonomy rank.
     *
     * @param taxonomy the taxonomy rank
     * @param propertyValue the property value to add or update
     */
    suspend fun setPropertyValue(
        taxonomy: Taxonomy = Taxonomy(
            kingdom = Taxonomy.ANY,
            group = Taxonomy.ANY
        ),
        propertyValue: PropertyValue
    ): Either<Failure, Unit>

    /**
     * Remove given property value by its code for the given given taxonomy rank.
     *
     * @param taxonomy the taxonomy rank
     * @param code the property value code to remove
     */
    suspend fun clearPropertyValue(
        taxonomy: Taxonomy = Taxonomy(
            kingdom = Taxonomy.ANY,
            group = Taxonomy.ANY
        ),
        code: String
    ): Either<Failure, Unit>

    /**
     * Clears all saved property values.
     */
    suspend fun clearAllPropertyValues(): Either<Failure, Unit>
}