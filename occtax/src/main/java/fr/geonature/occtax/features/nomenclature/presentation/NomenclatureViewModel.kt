package fr.geonature.occtax.features.nomenclature.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.geonature.commons.data.entity.Nomenclature
import fr.geonature.commons.data.entity.Taxonomy
import fr.geonature.commons.lifecycle.BaseViewModel
import fr.geonature.occtax.features.nomenclature.domain.EditableField
import fr.geonature.occtax.features.nomenclature.usecase.GetEditableFieldsUseCase
import fr.geonature.occtax.features.nomenclature.usecase.GetNomenclatureValuesByTypeAndTaxonomyUseCase
import fr.geonature.occtax.settings.PropertySettings
import org.tinylog.Logger
import javax.inject.Inject

/**
 * Nomenclature view model.
 *
 * @author S. Grimault
 *
 * @see GetEditableFieldsUseCase
 * @see GetNomenclatureValuesByTypeAndTaxonomyUseCase
 */
@HiltViewModel
class NomenclatureViewModel @Inject constructor(
    private val getEditableFieldsUseCase: GetEditableFieldsUseCase,
    private val getNomenclatureValuesByTypeAndTaxonomyUseCase: GetNomenclatureValuesByTypeAndTaxonomyUseCase
) : BaseViewModel() {

    private val _editableNomenclatures = MutableLiveData<List<EditableField>>()
    val editableNomenclatures: LiveData<List<EditableField>> = _editableNomenclatures

    /**
     * Gets all editable fields from given type with default values.
     *
     * @param type the main editable nomenclature type
     * @param defaultPropertySettings the default nomenclature settings
     */
    fun getEditableFields(
        datasetId: Long? = null,
        withAdditionalFields: Boolean = false,
        type: EditableField.Type,
        defaultPropertySettings: List<PropertySettings> = listOf(),
        taxonomy: Taxonomy? = null
    ) {
        getEditableFieldsUseCase(
            GetEditableFieldsUseCase.Params(
                datasetId,
                withAdditionalFields,
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