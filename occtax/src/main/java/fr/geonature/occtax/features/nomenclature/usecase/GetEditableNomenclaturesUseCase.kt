package fr.geonature.occtax.features.nomenclature.usecase

import fr.geonature.commons.error.Failure
import fr.geonature.commons.fp.Either
import fr.geonature.commons.fp.Either.Left
import fr.geonature.commons.fp.Either.Right
import fr.geonature.commons.fp.getOrElse
import fr.geonature.commons.interactor.BaseUseCase
import fr.geonature.occtax.features.nomenclature.domain.BaseEditableNomenclatureType
import fr.geonature.occtax.features.nomenclature.domain.EditableNomenclatureType
import fr.geonature.occtax.features.nomenclature.error.NoNomenclatureTypeFoundLocallyFailure
import fr.geonature.occtax.features.nomenclature.repository.INomenclatureRepository
import fr.geonature.occtax.input.PropertyValue
import fr.geonature.occtax.settings.PropertySettings
import javax.inject.Inject

/**
 * Gets all editable nomenclatures from given type with default values.
 *
 * @author S. Grimault
 */
class GetEditableNomenclaturesUseCase @Inject constructor(private val nomenclatureRepository: INomenclatureRepository) :
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

        val defaultNomenclatureValues =
            nomenclatureRepository.getAllDefaultNomenclatureValues().getOrElse { emptyList() }

        return Right(editableNomenclatures.map { editableNomenclature ->
            editableNomenclature.copy(value = defaultNomenclatureValues.firstOrNull { it.type?.mnemonic == editableNomenclature.code }
                ?.let {
                    PropertyValue.fromNomenclature(
                        editableNomenclature.code,
                        it
                    )
                })
        })
    }

    data class Params(
        val type: BaseEditableNomenclatureType.Type,
        val settings: List<PropertySettings> = listOf()
    )
}