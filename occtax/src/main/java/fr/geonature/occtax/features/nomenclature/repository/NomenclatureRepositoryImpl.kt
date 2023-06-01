package fr.geonature.occtax.features.nomenclature.repository

import fr.geonature.commons.data.entity.NomenclatureWithType
import fr.geonature.commons.features.nomenclature.data.INomenclatureLocalDataSource
import fr.geonature.commons.features.nomenclature.error.NomenclatureException
import fr.geonature.occtax.features.nomenclature.data.INomenclatureSettingsLocalDataSource
import fr.geonature.occtax.features.nomenclature.domain.EditableNomenclatureType
import fr.geonature.occtax.features.record.domain.PropertyValue
import fr.geonature.occtax.settings.PropertySettings
import org.tinylog.Logger
import fr.geonature.commons.features.nomenclature.repository.NomenclatureRepositoryImpl as BaseNomenclatureRepositoryImpl

/**
 * Implementation of [INomenclatureRepository] based from [BaseNomenclatureRepositoryImpl] with
 * support of [EditableNomenclatureType].
 *
 * @author S. Grimault
 * @see BaseNomenclatureRepositoryImpl
 */
class NomenclatureRepositoryImpl(
    private val nomenclatureLocalDataSource: INomenclatureLocalDataSource,
    private val nomenclatureSettingsLocalDataSource: INomenclatureSettingsLocalDataSource,
) : BaseNomenclatureRepositoryImpl(nomenclatureLocalDataSource), INomenclatureRepository {

    override suspend fun getEditableNomenclatures(
        type: EditableNomenclatureType.Type,
        vararg defaultPropertySettings: PropertySettings
    ): Result<List<EditableNomenclatureType>> {
        return runCatching {
            val nomenclatureTypes =
                nomenclatureLocalDataSource.getAllNomenclatureTypes()
                    .associateBy { it.mnemonic }

            val defaultNomenclatureValues =
                nomenclatureLocalDataSource.getAllDefaultNomenclatureValues()
                    .map { nomenclature ->
                        NomenclatureWithType(nomenclature).apply {
                            this.type =
                                nomenclatureTypes.entries.firstOrNull { it.value.id == typeId }?.value
                        }
                    }
                    .filter { it.type != null }

            nomenclatureSettingsLocalDataSource.getNomenclatureTypeSettings(
                type,
                *defaultPropertySettings
            )
                .mapNotNull {
                    if (it.viewType == EditableNomenclatureType.ViewType.NOMENCLATURE_TYPE) nomenclatureTypes[it.code]?.let { nomenclatureType ->
                        EditableNomenclatureType(
                            type = it.type,
                            code = it.code,
                            viewType = it.viewType,
                            visible = it.visible,
                            default = it.default,
                            additionalField = false,
                            label = nomenclatureType.defaultLabel.takeIf { label -> label.isNotEmpty() }
                                ?: run {
                                    Logger.warn { "no label found for nomenclature type '${nomenclatureType.mnemonic}', use default..." }
                                    null
                                }
                        )
                    } else it
                }
                .map { editableNomenclature ->
                    editableNomenclature.copy(value = defaultNomenclatureValues.firstOrNull { it.type?.mnemonic == editableNomenclature.code }
                        ?.let {
                            PropertyValue.Nomenclature(
                                editableNomenclature.code,
                                it.defaultLabel,
                                it.id
                            )
                        } ?: editableNomenclature.value)
                }
                .also {
                    if (it.isEmpty()) throw NomenclatureException.NoNomenclatureTypeFoundException
                }
        }
    }
}