package fr.geonature.occtax.features.nomenclature.usecase

import fr.geonature.commons.data.entity.Taxonomy
import fr.geonature.commons.features.nomenclature.error.NomenclatureException
import fr.geonature.commons.fp.getOrElse
import fr.geonature.commons.interactor.BaseResultUseCase
import fr.geonature.occtax.features.nomenclature.domain.FormField
import fr.geonature.occtax.features.nomenclature.repository.IAdditionalFieldRepository
import fr.geonature.occtax.features.nomenclature.repository.IDefaultPropertyValueRepository
import fr.geonature.occtax.features.nomenclature.repository.INomenclatureRepository
import fr.geonature.occtax.features.settings.domain.PropertySettings
import org.tinylog.Logger
import javax.inject.Inject

/**
 * Gets all editable nomenclatures from given type with default values.
 *
 * @author S. Grimault
 */
class GetEditableFieldsUseCase @Inject constructor(
    private val nomenclatureRepository: INomenclatureRepository,
    private val additionalFieldRepository: IAdditionalFieldRepository,
    private val defaultPropertyValueRepository: IDefaultPropertyValueRepository
) :
    BaseResultUseCase<List<FormField>, GetEditableFieldsUseCase.Params>() {
    override suspend fun run(params: Params): Result<List<FormField>> {
        Logger.info { "loading editable fields of type '${params.type.name}'${if (params.withAdditionalFields) " with additional fields" else ""}${params.datasetId?.let { " matching dataset ID $it" } ?: ""}..." }

        val editableNomenclatures = (nomenclatureRepository.getEditableFields(
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
            if (params.withAdditionalFields) additionalFieldRepository.getAllAdditionalFields(
                params.datasetId,
                params.type
            )
                .getOrDefault(emptyList()) else emptyList())
            .sorted()

        val defaultPropertyValues =
            defaultPropertyValueRepository.getPropertyValues(params.taxonomy)
                .getOrElse { emptyList() }

        return Result.success(
            editableNomenclatures.map
            { editableNomenclature ->
                when (editableNomenclature) {
                    is FormField.Editable -> editableNomenclature.apply {
                        locked = defaultPropertyValues.any {
                            it.code == editableNomenclature.getValue().code
                        }
                        setValue(defaultPropertyValues.firstOrNull { it.code == editableNomenclature.getValue().code }
                            ?: editableNomenclature.getValue())
                    }

                    else -> editableNomenclature
                }
            }
        )
    }

    data class Params(
        val datasetId: Long? = null,
        val withAdditionalFields: Boolean = false,
        val type: FormField.Type,
        val settings: List<PropertySettings> = listOf(),
        val taxonomy: Taxonomy? = null
    )
}