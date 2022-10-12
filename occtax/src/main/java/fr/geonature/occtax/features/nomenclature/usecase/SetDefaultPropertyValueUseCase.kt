package fr.geonature.occtax.features.nomenclature.usecase

import fr.geonature.commons.data.entity.Taxonomy
import fr.geonature.commons.error.Failure
import fr.geonature.commons.fp.Either
import fr.geonature.commons.interactor.BaseUseCase
import fr.geonature.occtax.features.nomenclature.repository.IDefaultPropertyValueRepository
import fr.geonature.occtax.features.input.domain.PropertyValue
import javax.inject.Inject

/**
 * Adds or updates given property value for the given given taxonomy rank.
 *
 * @author S. Grimault
 */
class SetDefaultPropertyValueUseCase @Inject constructor(
    private val defaultPropertyValueRepository: IDefaultPropertyValueRepository
) :
    BaseUseCase<Unit, SetDefaultPropertyValueUseCase.Params>() {
    override suspend fun run(params: Params): Either<Failure, Unit> {
        return defaultPropertyValueRepository.setPropertyValue(
            params.taxonomy,
            params.propertyValue
        )
    }

    data class Params(
        val taxonomy: Taxonomy = Taxonomy(
            kingdom = Taxonomy.ANY,
            group = Taxonomy.ANY
        ),
        val propertyValue: PropertyValue
    )
}