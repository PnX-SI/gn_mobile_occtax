package fr.geonature.occtax.features.nomenclature.usecase

import fr.geonature.commons.data.entity.Nomenclature
import fr.geonature.commons.data.entity.Taxonomy
import fr.geonature.commons.error.Failure
import fr.geonature.commons.fp.Either
import fr.geonature.commons.interactor.BaseUseCase
import fr.geonature.occtax.features.nomenclature.repository.INomenclatureRepository
import javax.inject.Inject

/**
 * Gets all nomenclature values matching given nomenclature type and an optional taxonomy rank.
 *
 * @author S. Grimault
 */
class GetNomenclatureValuesByTypeAndTaxonomyUseCase @Inject constructor(private val nomenclatureRepository: INomenclatureRepository) :
    BaseUseCase<List<Nomenclature>, GetNomenclatureValuesByTypeAndTaxonomyUseCase.Params>() {
    override suspend fun run(params: Params): Either<Failure, List<Nomenclature>> {
        return nomenclatureRepository.getNomenclatureValuesByTypeAndTaxonomy(
            params.mnemonic,
            params.taxonomy
        )
    }

    data class Params(
        val mnemonic: String,
        val taxonomy: Taxonomy? = null
    )
}