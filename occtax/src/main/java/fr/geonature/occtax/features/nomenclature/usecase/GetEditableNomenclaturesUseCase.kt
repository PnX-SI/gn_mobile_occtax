package fr.geonature.occtax.features.nomenclature.usecase

import fr.geonature.commons.data.entity.Taxonomy
import fr.geonature.commons.features.nomenclature.error.NomenclatureException
import fr.geonature.commons.fp.getOrElse
import fr.geonature.commons.interactor.BaseResultUseCase
import fr.geonature.occtax.features.nomenclature.domain.EditableNomenclatureType
import fr.geonature.occtax.features.nomenclature.repository.IAdditionalFieldRepository
import fr.geonature.occtax.features.nomenclature.repository.IDefaultPropertyValueRepository
import fr.geonature.occtax.features.nomenclature.repository.INomenclatureRepository
import fr.geonature.occtax.settings.PropertySettings
import javax.inject.Inject

/**
 * Gets all editable nomenclatures from given type with default values.
 *
 * @author S. Grimault
 */
class GetEditableNomenclaturesUseCase @Inject constructor(
    private val nomenclatureRepository: INomenclatureRepository,
    private val additionalFieldRepository: IAdditionalFieldRepository,
    private val defaultPropertyValueRepository: IDefaultPropertyValueRepository
) :
    BaseResultUseCase<List<EditableNomenclatureType>, GetEditableNomenclaturesUseCase.Params>() {
    override suspend fun run(params: Params): Result<List<EditableNomenclatureType>> {
        val editableNomenclatures = (nomenclatureRepository.getEditableNomenclatures(
            params.type,
            *params.settings.toTypedArray()
        )
            .let {
                it.getOrNull() ?: return run {
                    Result.failure(
                        it.exceptionOrNull()
                            ?: NomenclatureException.NoNomenclatureTypeFoundException
                    )
                }
            } +
            additionalFieldRepository.getAllAdditionalFields(
                params.datasetId,
                params.type
            )
                .getOrDefault(emptyList()))
            // set media type at last position
            .sortedWith { a, b ->
                if (a.viewType == EditableNomenclatureType.ViewType.MEDIA) 1
                else if (b.viewType == EditableNomenclatureType.ViewType.MEDIA) -1
                else 0
            }

        val defaultPropertyValues =
            defaultPropertyValueRepository.getPropertyValues(params.taxonomy)
                .getOrElse { emptyList() }

        return Result.success(
            editableNomenclatures.map
            { editableNomenclature ->
                editableNomenclature.copy(
                    value = defaultPropertyValues.firstOrNull {
                        it.toPair().first == editableNomenclature.code
                    }
                        ?: editableNomenclature.value,
                    locked = defaultPropertyValues.any {
                        it.toPair().first == editableNomenclature.code
                    }
                )
            }
        )
    }

    data class Params(
        val datasetId: Long? = null,
        val type: EditableNomenclatureType.Type,
        val settings: List<PropertySettings> = listOf(),
        val taxonomy: Taxonomy? = null
    )
}