package fr.geonature.occtax.features.nomenclature.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.geonature.commons.data.entity.Nomenclature
import fr.geonature.commons.data.entity.Taxonomy
import fr.geonature.commons.lifecycle.BaseViewModel
import fr.geonature.occtax.features.nomenclature.domain.EditableNomenclatureType
import fr.geonature.occtax.features.nomenclature.usecase.GetEditableNomenclaturesUseCase
import fr.geonature.occtax.features.nomenclature.usecase.GetNomenclatureValuesByTypeAndTaxonomyUseCase
import fr.geonature.occtax.settings.PropertySettings
import org.tinylog.Logger
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
) : BaseViewModel() {

    private val _editableNomenclatures = MutableLiveData<List<EditableNomenclatureType>>()
    val editableNomenclatures: LiveData<List<EditableNomenclatureType>> = _editableNomenclatures

    /**
     * Gets all editable nomenclatures from given type with default values.
     *
     * @param type the main editable nomenclature type
     * @param defaultPropertySettings the default nomenclature settings
     */
    fun getEditableNomenclatures(
        datasetId: Long? = null,
        type: EditableNomenclatureType.Type,
        defaultPropertySettings: List<PropertySettings> = listOf(),
        taxonomy: Taxonomy? = null
    ) {
        getEditableNomenclaturesUseCase(
            GetEditableNomenclaturesUseCase.Params(
                datasetId,
                type,
                defaultPropertySettings,
                taxonomy
            ),
            viewModelScope
        ) {
            it.fold(
                onSuccess = { editableNomenclatures ->
                    _editableNomenclatures.value = editableNomenclatures
                },
                ::handleError
            )
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
    ): LiveData<List<Nomenclature>> {
        Logger.info {
            "fetching nomenclature values from mnemonic '$mnemonic'${taxonomy?.let { " matching taxonomy ${it.kingdom}:${it.group}" }}..."
        }

        val nomenclatureValuesByTypeAndTaxonomy = MutableLiveData<List<Nomenclature>>()

        getNomenclatureValuesByTypeAndTaxonomyUseCase(
            GetNomenclatureValuesByTypeAndTaxonomyUseCase.Params(
                mnemonic,
                taxonomy
            ),
            viewModelScope
        ) {
            it.fold(
                onSuccess = { nomenclatureValues ->
                    nomenclatureValuesByTypeAndTaxonomy.value = nomenclatureValues
                },
                ::handleError
            )
        }

        return nomenclatureValuesByTypeAndTaxonomy
    }
}