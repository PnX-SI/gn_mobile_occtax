package fr.geonature.occtax.features.nomenclature.data

import fr.geonature.commons.data.entity.Taxonomy
import fr.geonature.occtax.features.record.domain.PropertyValue

/**
 * In memory implementation of [IPropertyValueLocalDataSource].
 *
 * @author S. Grimault
 */
class InMemoryPropertyValueLocalDataSourceImpl : IPropertyValueLocalDataSource {
    private val propertyValues = mutableMapOf<Taxonomy, Set<PropertyValue>>()

    override suspend fun getPropertyValues(taxonomy: Taxonomy): List<PropertyValue> {
        return propertyValues[taxonomy]?.toList() ?: emptyList()
    }

    override suspend fun setPropertyValue(taxonomy: Taxonomy, vararg propertyValue: PropertyValue) {
        propertyValues[taxonomy] =
            getPropertyValues(taxonomy)
                .map { it.toPair() }
                .filter { existingPropertyValue -> propertyValue.map { it.toPair() }.none { it.first == existingPropertyValue.first } }
                .map { it.second }
                .toSet() + propertyValue.toSet()
    }

    override suspend fun clearPropertyValue(taxonomy: Taxonomy, vararg code: String) {
        propertyValues[taxonomy] =
            getPropertyValues(taxonomy)
                .map { it.toPair() }
                .filter { propertyValue -> code.none { it == propertyValue.first } }
                .map { it.second }
                .toSet()
    }

    override suspend fun clearAllPropertyValues() {
        propertyValues.clear()
    }
}