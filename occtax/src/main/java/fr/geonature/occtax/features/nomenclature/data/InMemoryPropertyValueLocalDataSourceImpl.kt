package fr.geonature.occtax.features.nomenclature.data

import fr.geonature.commons.data.entity.Taxonomy
import fr.geonature.occtax.features.input.domain.PropertyValue

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
            getPropertyValues(taxonomy).filter { existingPropertyValue -> propertyValue.none { it.code == existingPropertyValue.code } }
                .toSet() + propertyValue.toSet()
    }

    override suspend fun clearPropertyValue(taxonomy: Taxonomy, vararg code: String) {
        propertyValues[taxonomy] =
            getPropertyValues(taxonomy).filter { propertyValue -> code.none { it == propertyValue.code } }
                .toSet()
    }

    override suspend fun clearAllPropertyValues() {
        propertyValues.clear()
    }
}