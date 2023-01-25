package fr.geonature.occtax.features.nomenclature.data

import fr.geonature.commons.data.entity.Taxonomy
import fr.geonature.occtax.features.input.domain.PropertyValue

/**
 * [PropertyValue] local data source.
 *
 * @author S. Grimault
 */
interface IPropertyValueLocalDataSource {

    /**
     * Gets all property values matching given taxonomy rank.
     *
     * @param taxonomy the taxonomy rank as filter
     */
    suspend fun getPropertyValues(
        taxonomy: Taxonomy = Taxonomy(
            kingdom = Taxonomy.ANY,
            group = Taxonomy.ANY
        )
    ): List<PropertyValue>

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
        vararg propertyValue: PropertyValue
    )

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
        vararg code: String
    )

    /**
     * Clears all saved property values.
     */
    suspend fun clearAllPropertyValues()
}