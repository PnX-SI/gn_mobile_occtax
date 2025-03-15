package fr.geonature.occtax.features.nomenclature.repository

import fr.geonature.commons.data.entity.NomenclatureWithType
import fr.geonature.commons.features.nomenclature.data.INomenclatureLocalDataSource
import fr.geonature.commons.features.nomenclature.error.NomenclatureException
import fr.geonature.occtax.features.nomenclature.data.INomenclatureSettingsLocalDataSource
import fr.geonature.occtax.features.nomenclature.domain.FormField
import fr.geonature.occtax.features.record.domain.PropertyValue
import fr.geonature.occtax.features.settings.domain.PropertySettings
import org.tinylog.Logger
import fr.geonature.commons.features.nomenclature.repository.NomenclatureRepositoryImpl as BaseNomenclatureRepositoryImpl

/**
 * Implementation of [INomenclatureRepository] based from [BaseNomenclatureRepositoryImpl] with
 * support of [FormField].
 *
 * @author S. Grimault
 * @see BaseNomenclatureRepositoryImpl
 */
class NomenclatureRepositoryImpl(
    private val nomenclatureLocalDataSource: INomenclatureLocalDataSource,
    private val nomenclatureSettingsLocalDataSource: INomenclatureSettingsLocalDataSource,
) : BaseNomenclatureRepositoryImpl(nomenclatureLocalDataSource), INomenclatureRepository {

    override suspend fun getEditableFields(
        type: FormField.Type,
        vararg defaultPropertySettings: PropertySettings
    ): Result<List<FormField>> {
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
                    if (it is FormField.NomenclatureType) nomenclatureTypes[it.nomenclatureType]?.let { nomenclatureType ->
                        it.copy(label = nomenclatureType.defaultLabel.takeIf { label -> label.isNotEmpty() }
                            ?: run {
                                Logger.warn { "no label found for nomenclature type '${nomenclatureType.mnemonic}', use default..." }
                                it.label
                            })
                    } else it
                }
                .map { formField ->
                    if (formField is FormField.NomenclatureType) {
                        formField.also { ff ->
                            ff.setValue(defaultNomenclatureValues.firstOrNull { it.type?.mnemonic == formField.nomenclatureType }
                                ?.let {
                                    PropertyValue.Nomenclature(
                                        formField.nomenclatureType,
                                        it.defaultLabel,
                                        it.id
                                    )
                                } ?: formField.value)
                        }
                    } else formField
                }
                .also {
                    if (it.isEmpty()) throw NomenclatureException.NoNomenclatureTypeFoundException
                }
        }
    }
}