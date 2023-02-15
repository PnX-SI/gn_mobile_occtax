package fr.geonature.occtax.features.nomenclature.data

import fr.geonature.occtax.features.nomenclature.domain.EditableNomenclatureType
import fr.geonature.occtax.features.record.domain.AllMediaRecord
import fr.geonature.occtax.features.record.domain.CountingRecord
import fr.geonature.occtax.features.record.domain.PropertyValue
import fr.geonature.occtax.settings.PropertySettings

/**
 * Default implementation of [INomenclatureSettingsLocalDataSource].
 *
 * @author S. Grimault
 */
class NomenclatureSettingsLocalDataSourceImpl :
    INomenclatureSettingsLocalDataSource {

    private val defaultNomenclatureTypes = listOf(
        EditableNomenclatureType(
            EditableNomenclatureType.Type.DEFAULT,
            "TYP_GRP",
            EditableNomenclatureType.ViewType.NOMENCLATURE_TYPE
        ),
        EditableNomenclatureType(
            EditableNomenclatureType.Type.INFORMATION,
            "METH_OBS",
            EditableNomenclatureType.ViewType.NOMENCLATURE_TYPE
        ),
        EditableNomenclatureType(
            EditableNomenclatureType.Type.INFORMATION,
            "ETA_BIO",
            EditableNomenclatureType.ViewType.NOMENCLATURE_TYPE
        ),
        EditableNomenclatureType(
            EditableNomenclatureType.Type.INFORMATION,
            "METH_DETERMIN",
            EditableNomenclatureType.ViewType.NOMENCLATURE_TYPE,
            default = false
        ),
        EditableNomenclatureType(
            EditableNomenclatureType.Type.INFORMATION,
            "determiner",
            EditableNomenclatureType.ViewType.TEXT_SIMPLE,
            default = false
        ),
        EditableNomenclatureType(
            EditableNomenclatureType.Type.INFORMATION,
            "STATUT_BIO",
            EditableNomenclatureType.ViewType.NOMENCLATURE_TYPE,
            default = false
        ),
        EditableNomenclatureType(
            EditableNomenclatureType.Type.INFORMATION,
            "OCC_COMPORTEMENT",
            EditableNomenclatureType.ViewType.NOMENCLATURE_TYPE,
            default = false
        ),
        EditableNomenclatureType(
            EditableNomenclatureType.Type.INFORMATION,
            "NATURALITE",
            EditableNomenclatureType.ViewType.NOMENCLATURE_TYPE,
            default = false
        ),
        EditableNomenclatureType(
            EditableNomenclatureType.Type.INFORMATION,
            "PREUVE_EXIST",
            EditableNomenclatureType.ViewType.NOMENCLATURE_TYPE,
            default = false
        ),
        EditableNomenclatureType(
            EditableNomenclatureType.Type.INFORMATION,
            "comment",
            EditableNomenclatureType.ViewType.TEXT_MULTIPLE,
            default = false
        ),
        EditableNomenclatureType(
            EditableNomenclatureType.Type.COUNTING,
            "STADE_VIE",
            EditableNomenclatureType.ViewType.NOMENCLATURE_TYPE
        ),
        EditableNomenclatureType(
            EditableNomenclatureType.Type.COUNTING,
            "SEXE",
            EditableNomenclatureType.ViewType.NOMENCLATURE_TYPE
        ),
        EditableNomenclatureType(
            EditableNomenclatureType.Type.COUNTING,
            "OBJ_DENBR",
            EditableNomenclatureType.ViewType.NOMENCLATURE_TYPE
        ),
        EditableNomenclatureType(
            EditableNomenclatureType.Type.COUNTING,
            "TYP_DENBR",
            EditableNomenclatureType.ViewType.NOMENCLATURE_TYPE
        ),
        EditableNomenclatureType(
            EditableNomenclatureType.Type.COUNTING,
            CountingRecord.MIN_KEY,
            EditableNomenclatureType.ViewType.MIN_MAX
        ).apply {
            value = PropertyValue.Number(
                code,
                1
            )
        },
        EditableNomenclatureType(
            EditableNomenclatureType.Type.COUNTING,
            CountingRecord.MAX_KEY,
            EditableNomenclatureType.ViewType.MIN_MAX
        ).apply {
            value = PropertyValue.Number(
                code,
                1
            )
        },
        EditableNomenclatureType(
            EditableNomenclatureType.Type.COUNTING,
            AllMediaRecord.MEDIAS_KEY,
            EditableNomenclatureType.ViewType.MEDIA
        ).apply {
            value = PropertyValue.Media(code)
        }
    )

    override suspend fun getNomenclatureTypeSettings(
        type: EditableNomenclatureType.Type,
        vararg defaultPropertySettings: PropertySettings
    ): List<EditableNomenclatureType> {
        if (defaultPropertySettings.isEmpty() || type == EditableNomenclatureType.Type.DEFAULT) {
            return defaultNomenclatureTypes.filter { it.type == type }
        }

        return defaultPropertySettings
            .mapNotNull { property ->
                defaultNomenclatureTypes.find { it.code == property.key }
                    ?.let {
                        EditableNomenclatureType(
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