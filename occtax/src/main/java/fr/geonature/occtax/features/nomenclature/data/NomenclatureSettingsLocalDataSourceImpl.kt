package fr.geonature.occtax.features.nomenclature.data

import fr.geonature.occtax.features.nomenclature.domain.BaseEditableNomenclatureType
import fr.geonature.occtax.settings.PropertySettings

/**
 * Default implementation of [INomenclatureSettingsLocalDataSource].
 *
 * @author S. Grimault
 */
class NomenclatureSettingsLocalDataSourceImpl :
    INomenclatureSettingsLocalDataSource {

    private val defaultNomenclatureTypes = listOf(
        BaseEditableNomenclatureType.from(
            BaseEditableNomenclatureType.Type.INFORMATION,
            "METH_OBS",
            BaseEditableNomenclatureType.ViewType.NOMENCLATURE_TYPE
        ),
        BaseEditableNomenclatureType.from(
            BaseEditableNomenclatureType.Type.INFORMATION,
            "ETA_BIO",
            BaseEditableNomenclatureType.ViewType.NOMENCLATURE_TYPE
        ),
        BaseEditableNomenclatureType.from(
            BaseEditableNomenclatureType.Type.INFORMATION,
            "METH_DETERMIN",
            BaseEditableNomenclatureType.ViewType.NOMENCLATURE_TYPE,
            default = false
        ),
        BaseEditableNomenclatureType.from(
            BaseEditableNomenclatureType.Type.INFORMATION,
            "DETERMINER",
            BaseEditableNomenclatureType.ViewType.TEXT_SIMPLE,
            default = false
        ),
        BaseEditableNomenclatureType.from(
            BaseEditableNomenclatureType.Type.INFORMATION,
            "STATUT_BIO",
            BaseEditableNomenclatureType.ViewType.NOMENCLATURE_TYPE,
            default = false
        ),
        BaseEditableNomenclatureType.from(
            BaseEditableNomenclatureType.Type.INFORMATION,
            "OCC_COMPORTEMENT",
            BaseEditableNomenclatureType.ViewType.NOMENCLATURE_TYPE,
            default = false
        ),
        BaseEditableNomenclatureType.from(
            BaseEditableNomenclatureType.Type.INFORMATION,
            "NATURALITE",
            BaseEditableNomenclatureType.ViewType.NOMENCLATURE_TYPE,
            default = false
        ),
        BaseEditableNomenclatureType.from(
            BaseEditableNomenclatureType.Type.INFORMATION,
            "PREUVE_EXIST",
            BaseEditableNomenclatureType.ViewType.NOMENCLATURE_TYPE,
            default = false
        ),
        BaseEditableNomenclatureType.from(
            BaseEditableNomenclatureType.Type.INFORMATION,
            "COMMENT",
            BaseEditableNomenclatureType.ViewType.TEXT_MULTIPLE,
            default = false
        ),
        BaseEditableNomenclatureType.from(
            BaseEditableNomenclatureType.Type.COUNTING,
            "STADE_VIE",
            BaseEditableNomenclatureType.ViewType.NOMENCLATURE_TYPE
        ),
        BaseEditableNomenclatureType.from(
            BaseEditableNomenclatureType.Type.COUNTING,
            "SEXE",
            BaseEditableNomenclatureType.ViewType.NOMENCLATURE_TYPE
        ),
        BaseEditableNomenclatureType.from(
            BaseEditableNomenclatureType.Type.COUNTING,
            "OBJ_DENBR",
            BaseEditableNomenclatureType.ViewType.NOMENCLATURE_TYPE
        ),
        BaseEditableNomenclatureType.from(
            BaseEditableNomenclatureType.Type.COUNTING,
            "TYP_DENBR",
            BaseEditableNomenclatureType.ViewType.NOMENCLATURE_TYPE
        ),
        BaseEditableNomenclatureType.from(
            BaseEditableNomenclatureType.Type.COUNTING,
            "MIN",
            BaseEditableNomenclatureType.ViewType.MIN_MAX
        ),
        BaseEditableNomenclatureType.from(
            BaseEditableNomenclatureType.Type.COUNTING,
            "MAX",
            BaseEditableNomenclatureType.ViewType.MIN_MAX
        )
    )

    override suspend fun getNomenclatureTypeSettings(
        type: BaseEditableNomenclatureType.Type,
        vararg defaultPropertySettings: PropertySettings
    ): List<BaseEditableNomenclatureType> {
        if (defaultPropertySettings.isEmpty()) {
            return defaultNomenclatureTypes.filter { it.type == type }
        }

        return defaultPropertySettings
            .mapNotNull { property ->
                defaultNomenclatureTypes.find { it.code == property.key }?.let {
                    BaseEditableNomenclatureType.from(
                        it.type,
                        it.code,
                        it.viewType,
                        property.visible,
                        property.default
                    )
                }
            }
    }
}