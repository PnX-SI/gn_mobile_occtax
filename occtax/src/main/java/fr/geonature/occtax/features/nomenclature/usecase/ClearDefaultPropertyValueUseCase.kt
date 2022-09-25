package fr.geonature.occtax.features.nomenclature.usecase

import fr.geonature.commons.data.entity.Taxonomy
import fr.geonature.commons.error.Failure
import fr.geonature.commons.fp.Either
import fr.geonature.commons.interactor.BaseUseCase
import fr.geonature.occtax.features.nomenclature.repository.IDefaultPropertyValueRepository
import javax.inject.Inject

/**
 * Remove given property value by its code for the given given taxonomy rank.
 *
 * @author S. Grimault
 */
class ClearDefaultPropertyValueUseCase @Inject constructor(
    private val defaultPropertyValueRepository: IDefaultPropertyValueRepository
) :
    BaseUseCase<Unit, ClearDefaultPropertyValueUseCase.Params>() {
    override suspend fun run(params: Params): Either<Failure, Unit> {
        return defaultPropertyValueRepository.clearPropertyValue(
            params.taxonomy,
            params.code
        )
    }

    data class Params(
        val taxonomy: Taxonomy = Taxonomy(
            kingdom = Taxonomy.ANY,
            group = Taxonomy.ANY
        ),
        val code: String
    )
}