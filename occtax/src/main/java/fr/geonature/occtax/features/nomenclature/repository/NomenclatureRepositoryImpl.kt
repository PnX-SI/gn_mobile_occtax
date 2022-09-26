package fr.geonature.occtax.features.nomenclature.repository

import fr.geonature.commons.data.entity.Nomenclature
import fr.geonature.commons.data.entity.NomenclatureWithType
import fr.geonature.commons.data.entity.Taxonomy
import fr.geonature.commons.error.Failure
import fr.geonature.commons.fp.Either
import fr.geonature.commons.fp.Either.Left
import fr.geonature.commons.fp.Either.Right
import fr.geonature.occtax.features.nomenclature.data.INomenclatureLocalDataSource
import fr.geonature.occtax.features.nomenclature.data.INomenclatureSettingsLocalDataSource
import fr.geonature.occtax.features.nomenclature.domain.BaseEditableNomenclatureType
import fr.geonature.occtax.features.nomenclature.domain.EditableNomenclatureType
import fr.geonature.occtax.features.nomenclature.error.NoNomenclatureTypeFoundLocallyFailure
import fr.geonature.occtax.features.nomenclature.error.NoNomenclatureValuesFoundFailure
import fr.geonature.occtax.input.PropertyValue
import fr.geonature.occtax.settings.PropertySettings
import org.tinylog.Logger

/**
 * Default implementation of [INomenclatureRepository].
 *
 * @author S. Grimault
 */
class NomenclatureRepositoryImpl(
    private val nomenclatureLocalDataSource: INomenclatureLocalDataSource,
    private val nomenclatureSettingsLocalDataSource: INomenclatureSettingsLocalDataSource
) : INomenclatureRepository {

    override suspend fun getEditableNomenclatures(
        type: BaseEditableNomenclatureType.Type,
        vararg defaultPropertySettings: PropertySettings
    ): Either<Failure, List<EditableNomenclatureType>> {
        return runCatching {
            val nomenclatureTypes =
                nomenclatureLocalDataSource.getAllNomenclatureTypes().associateBy { it.mnemonic }

            val defaultNomenclatureValues =
                nomenclatureLocalDataSource.getAllDefaultNomenclatureValues().map { nomenclature ->
                    NomenclatureWithType(nomenclature).apply {
                        this.type =
                            nomenclatureTypes.entries.firstOrNull { it.value.id == typeId }?.value
                    }
                }.filter { it.type != null }

            nomenclatureSettingsLocalDataSource.getNomenclatureTypeSettings(
                type,
                *defaultPropertySettings
            ).mapNotNull {
                if (it.viewType == BaseEditableNomenclatureType.ViewType.NOMENCLATURE_TYPE) nomenclatureTypes[it.code]?.let { nomenclatureType ->
                    EditableNomenclatureType(
                        it.type,
                        it.code,
                        it.viewType,
                        it.visible,
                        it.default,
                        nomenclatureType.defaultLabel.takeIf { label -> label.isNotEmpty() }
                            ?: run {
                                Logger.warn { "no label found for nomenclature type '${nomenclatureType.mnemonic}', use default…" }
                                null
                            }
                    )
                } else EditableNomenclatureType(
                    it.type,
                    it.code,
                    it.viewType,
                    it.visible,
                    it.default
                )
            }.map { editableNomenclature ->
                editableNomenclature.copy(value = defaultNomenclatureValues.firstOrNull { it.type?.mnemonic == editableNomenclature.code }
                    ?.let {
                        PropertyValue.fromNomenclature(
                            editableNomenclature.code,
                            it
                        )
                    })
            }
        }.fold(
            onSuccess = {
                if (it.isEmpty()) Left(NoNomenclatureTypeFoundLocallyFailure) else Right(it)
            },
            onFailure = {
                Left(Failure.DbFailure(it))
            }
        )
    }

    override suspend fun getNomenclatureValuesByTypeAndTaxonomy(
        mnemonic: String,
        taxonomy: Taxonomy?
    ): Either<Failure, List<Nomenclature>> {
        return runCatching {
            nomenclatureLocalDataSource.getNomenclatureValuesByTypeAndTaxonomy(
                mnemonic,
                taxonomy
            )
        }.fold(
            onSuccess = {
                if (it.isEmpty()) Left(NoNomenclatureValuesFoundFailure(mnemonic)) else Right(it)
            },
            onFailure = {
                Left(Failure.DbFailure(it))
            }
        )
    }
}