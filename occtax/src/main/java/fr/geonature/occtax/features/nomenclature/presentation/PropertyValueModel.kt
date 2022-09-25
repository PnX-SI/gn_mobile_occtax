package fr.geonature.occtax.features.nomenclature.presentation

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.geonature.commons.data.entity.Taxonomy
import fr.geonature.commons.lifecycle.BaseViewModel
import fr.geonature.occtax.features.nomenclature.usecase.ClearDefaultPropertyValueUseCase
import fr.geonature.occtax.features.nomenclature.usecase.SetDefaultPropertyValueUseCase
import fr.geonature.occtax.input.PropertyValue
import javax.inject.Inject

/**
 * [PropertyValue] view model.
 *
 * @author S. Grimault
 *
 * @see SetDefaultPropertyValueUseCase
 */
@HiltViewModel
class PropertyValueModel @Inject constructor(
    private val setDefaultPropertyValueUseCase: SetDefaultPropertyValueUseCase,
    private val clearDefaultPropertyValueUseCase: ClearDefaultPropertyValueUseCase
) :
    BaseViewModel() {

    /**
     * Adds or updates given property value for the given given taxonomy rank.
     *
     * @param taxonomy the taxonomy rank
     * @param propertyValue the property value to add or update
     */
    fun setPropertyValue(
        taxonomy: Taxonomy = Taxonomy(
            kingdom = Taxonomy.ANY,
            group = Taxonomy.ANY
        ),
        propertyValue: PropertyValue
    ) {
        setDefaultPropertyValueUseCase(
            SetDefaultPropertyValueUseCase.Params(
                taxonomy,
                propertyValue
            ),
            viewModelScope
        ) {
            it.fold(::handleFailure) {}
        }
    }

    /**
     * Remove given property value by its code for the given given taxonomy rank.
     *
     * @param taxonomy the taxonomy rank
     * @param code the property value code to remove
     */
    fun clearPropertyValue(
        taxonomy: Taxonomy = Taxonomy(
            kingdom = Taxonomy.ANY,
            group = Taxonomy.ANY
        ),
        code: String
    ) {
        clearDefaultPropertyValueUseCase(
            ClearDefaultPropertyValueUseCase.Params.Params(
                taxonomy,
                code
            ),
            viewModelScope
        ) {
            it.fold(::handleFailure) {}
        }
    }

    /**
     * Clears all saved property values.
     */
    fun clearAllPropertyValues() {
        clearDefaultPropertyValueUseCase(
            ClearDefaultPropertyValueUseCase.Params.None,
            viewModelScope
        ) {
            it.fold(::handleFailure) {}
        }
    }
}