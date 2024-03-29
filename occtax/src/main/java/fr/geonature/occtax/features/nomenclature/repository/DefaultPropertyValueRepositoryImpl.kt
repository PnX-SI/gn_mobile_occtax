package fr.geonature.occtax.features.nomenclature.repository

import fr.geonature.commons.data.entity.Taxonomy
import fr.geonature.commons.error.Failure
import fr.geonature.commons.features.nomenclature.data.INomenclatureLocalDataSource
import fr.geonature.commons.fp.Either
import fr.geonature.commons.fp.Either.Left
import fr.geonature.commons.fp.Either.Right
import fr.geonature.occtax.features.nomenclature.data.IPropertyValueLocalDataSource
import fr.geonature.occtax.features.nomenclature.error.PropertyValueFailure
import fr.geonature.occtax.features.record.domain.PropertyValue
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.toList

/**
 * Default implementation of [IDefaultPropertyValueRepository].
 *
 * @author S. Grimault
 */
class DefaultPropertyValueRepositoryImpl(
    private val propertyValueLocalDataSource: IPropertyValueLocalDataSource,
    private val nomenclatureLocalDataSource: INomenclatureLocalDataSource
) :
    IDefaultPropertyValueRepository {

    override suspend fun getPropertyValues(taxonomy: Taxonomy?): Either<Failure, List<PropertyValue>> {
        return Right(runCatching {
            propertyValueLocalDataSource.getPropertyValues(
                taxonomy ?: Taxonomy(
                    kingdom = Taxonomy.ANY,
                    group = Taxonomy.ANY
                )
            )
        }.getOrElse { emptyList() }
            .asFlow()
            .filter { propertyValue ->
                runCatching {
                    nomenclatureLocalDataSource.getNomenclatureValuesByTypeAndTaxonomy(
                        propertyValue.toPair().first,
                        taxonomy
                    )
                }.getOrElse { emptyList() }
                    .takeIf { it.isNotEmpty() }
                    ?.any {
                        propertyValue.takeIf { propertyValue is PropertyValue.Nomenclature }
                            ?.let { propertyValue as PropertyValue.Nomenclature }?.value == it.id
                    } ?: true
            }
            .toList())
    }

    override suspend fun setPropertyValue(
        taxonomy: Taxonomy,
        propertyValue: PropertyValue
    ): Either<Failure, Unit> {
        return runCatching {
            propertyValueLocalDataSource.setPropertyValue(
                taxonomy,
                propertyValue
            )
        }.fold(
            onSuccess = { Right(Unit) },
            onFailure = { Left(PropertyValueFailure(propertyValue.toPair().first)) }
        )
    }

    override suspend fun clearPropertyValue(
        taxonomy: Taxonomy,
        code: String
    ): Either<Failure, Unit> {
        return runCatching {
            propertyValueLocalDataSource.clearPropertyValue(
                taxonomy,
                code
            )
        }.fold(
            onSuccess = { Right(Unit) },
            onFailure = { Left(PropertyValueFailure(code)) }
        )
    }

    override suspend fun clearAllPropertyValues(): Either<Failure, Unit> {
        propertyValueLocalDataSource.clearAllPropertyValues()

        return Right(Unit)
    }
}