package fr.geonature.occtax.features.nomenclature.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.geonature.commons.data.entity.Nomenclature
import fr.geonature.commons.data.entity.Taxonomy
import fr.geonature.commons.lifecycle.BaseViewModel
import fr.geonature.occtax.features.nomenclature.domain.BaseEditableNomenclatureType
import fr.geonature.occtax.features.nomenclature.domain.EditableNomenclatureType
import fr.geonature.occtax.features.nomenclature.usecase.GetEditableNomenclaturesUseCase
import fr.geonature.occtax.features.nomenclature.usecase.GetNomenclatureValuesByTypeAndTaxonomyUseCase
import fr.geonature.occtax.settings.PropertySettings
import javax.inject.Inject

/**
 * Nomenclature view model.
 *
 * @author S. Grimault
 *
 * @see GetEditableNomenclaturesUseCase
 * @see GetNomenclatureValuesByTypeAndTaxonomyUseCase
 */
@HiltViewModel
class NomenclatureViewModel @Inject constructor(
    private val getEditableNomenclaturesUseCase: GetEditableNomenclaturesUseCase,
    private val getNomenclatureValuesByTypeAndTaxonomyUseCase: GetNomenclatureValuesByTypeAndTaxonomyUseCase
) :
    BaseViewModel() {

    private val _editableNomenclatures = MutableLiveData<List<EditableNomenclatureType>>(listOf())
    val editableNomenclatures: LiveData<List<EditableNomenclatureType>> = _editableNomenclatures

    private val _nomenclatureValuesByTypeAndTaxonomy =
        MutableLiveData<Map<String, List<Nomenclature>>>(mapOf())
    val nomenclatureValuesByTypeAndTaxonomy: LiveData<Map<String, List<Nomenclature>>> =
        _nomenclatureValuesByTypeAndTaxonomy

    /**
     * Gets all editable nomenclatures from given type with default values.
     *
     * @param type the main editable nomenclature type
     * @param defaultPropertySettings the default nomenclature settings
     */
    fun getEditableNomenclatures(
        type: BaseEditableNomenclatureType.Type,
        vararg defaultPropertySettings: PropertySettings
    ) {
        getEditableNomenclaturesUseCase(
            GetEditableNomenclaturesUseCase.Params(
                type,
                defaultPropertySettings.asList()
            ),
            viewModelScope
        ) {
            it.fold(::handleFailure) { editableNomenclatures ->
                _editableNomenclatures.value = editableNomenclatures
            }
        }
    }

    /**
     * Gets all nomenclature values matching given nomenclature type and an optional taxonomy rank.
     *
     * @param mnemonic the nomenclature type as main filter
     * @param taxonomy the taxonomy rank
     */
    fun getNomenclatureValuesByTypeAndTaxonomy(
        mnemonic: String,
        taxonomy: Taxonomy? = null
    ) {
        getNomenclatureValuesByTypeAndTaxonomyUseCase(
            GetNomenclatureValuesByTypeAndTaxonomyUseCase.Params(
                mnemonic,
                taxonomy
            ),
            viewModelScope
        ) {
            it.fold(::handleFailure) { nomenclatureValues ->
                _nomenclatureValuesByTypeAndTaxonomy.value =
                    mapOf(
                        *(_nomenclatureValuesByTypeAndTaxonomy.value?.entries?.map { entry -> entry.key to entry.value }
                            ?.toTypedArray() ?: emptyArray()),
                        mnemonic to nomenclatureValues
                    )
            }
        }
    }
}