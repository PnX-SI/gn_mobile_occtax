package fr.geonature.occtax.features.nomenclature.usecase

import fr.geonature.commons.data.entity.Taxonomy
import fr.geonature.commons.error.Failure
import fr.geonature.commons.fp.Either
import fr.geonature.commons.fp.Either.Left
import fr.geonature.commons.fp.Either.Right
import fr.geonature.commons.fp.getOrElse
import fr.geonature.commons.interactor.BaseUseCase
import fr.geonature.occtax.features.nomenclature.domain.EditableNomenclatureType
import fr.geonature.occtax.features.nomenclature.error.NoNomenclatureTypeFoundLocallyFailure
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
    private val defaultPropertyValueRepository: IDefaultPropertyValueRepository
) :
    BaseUseCase<List<EditableNomenclatureType>, GetEditableNomenclaturesUseCase.Params>() {
    override suspend fun run(params: Params): Either<Failure, List<EditableNomenclatureType>> {
        val editableNomenclaturesResult = nomenclatureRepository.getEditableNomenclatures(
            params.type,
            *params.settings.toTypedArray()
        )

        if (editableNomenclaturesResult.isLeft) {
            return editableNomenclaturesResult
        }

        val editableNomenclatures = editableNomenclaturesResult.getOrElse { emptyList() }

        if (editableNomenclatures.isEmpty()) {
            return Left(NoNomenclatureTypeFoundLocallyFailure)
        }

        val defaultPropertyValues =
            defaultPropertyValueRepository.getPropertyValues(params.taxonomy)
                .getOrElse { emptyList() }

        return Right(editableNomenclatures.map { editableNomenclature ->
            editableNomenclature.copy(
                value = defaultPropertyValues.firstOrNull { it.toPair().first == editableNomenclature.code }
                    ?: editableNomenclature.value,
                locked = defaultPropertyValues.any { it.toPair().first == editableNomenclature.code }
            )
        })
    }

    data class Params(
        val type: EditableNomenclatureType.Type,
        val settings: List<PropertySettings> = listOf(),
        val taxonomy: Taxonomy? = null
    )
}