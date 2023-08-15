package fr.geonature.occtax.features.nomenclature.data

import fr.geonature.occtax.features.nomenclature.domain.EditableField
import fr.geonature.occtax.features.record.domain.AllMediaRecord
import fr.geonature.occtax.features.record.domain.CountingRecord
import fr.geonature.occtax.features.record.domain.PropertyValue
import fr.geonature.occtax.features.settings.domain.PropertySettings

/**
 * Default implementation of [INomenclatureSettingsLocalDataSource].
 *
 * @author S. Grimault
 */
class NomenclatureSettingsLocalDataSourceImpl :
    INomenclatureSettingsLocalDataSource {

    private val defaultNomenclatureTypes = listOf(
        EditableField(
            type = EditableField.Type.DEFAULT,
            code = "TYP_GRP",
            viewType = EditableField.ViewType.NOMENCLATURE_TYPE,
            nomenclatureType = "TYP_GRP"
        ),
        EditableField(
            type = EditableField.Type.INFORMATION,
            code = "METH_OBS",
            viewType = EditableField.ViewType.NOMENCLATURE_TYPE,
            nomenclatureType = "METH_OBS"
        ),
        EditableField(
            type = EditableField.Type.INFORMATION,
            code = "ETA_BIO",
            viewType = EditableField.ViewType.NOMENCLATURE_TYPE,
            nomenclatureType = "ETA_BIO"
        ),
        EditableField(
            type = EditableField.Type.INFORMATION,
            code = "METH_DETERMIN",
            viewType = EditableField.ViewType.NOMENCLATURE_TYPE,
            nomenclatureType = "METH_DETERMIN",
            default = false
        ),
        EditableField(
            type = EditableField.Type.INFORMATION,
            code = "determiner",
            viewType = EditableField.ViewType.TEXT_SIMPLE,
            default = false
        ),
        EditableField(
            type = EditableField.Type.INFORMATION,
            code = "STATUT_BIO",
            viewType = EditableField.ViewType.NOMENCLATURE_TYPE,
            nomenclatureType = "STATUT_BIO",
            default = false
        ),
        EditableField(
            type = EditableField.Type.INFORMATION,
            code = "OCC_COMPORTEMENT",
            viewType = EditableField.ViewType.NOMENCLATURE_TYPE,
            nomenclatureType = "OCC_COMPORTEMENT",
            default = false
        ),
        EditableField(
            type = EditableField.Type.INFORMATION,
            code = "NATURALITE",
            viewType = EditableField.ViewType.NOMENCLATURE_TYPE,
            nomenclatureType = "NATURALITE",
            default = false
        ),
        EditableField(
            type = EditableField.Type.INFORMATION,
            code = "PREUVE_EXIST",
            viewType = EditableField.ViewType.NOMENCLATURE_TYPE,
            nomenclatureType = "PREUVE_EXIST",
            default = false
        ),
        EditableField(
            type = EditableField.Type.INFORMATION,
            code = "comment",
            viewType = EditableField.ViewType.TEXT_MULTIPLE,
            default = false
        ),
        EditableField(
            type = EditableField.Type.COUNTING,
            code = "STADE_VIE",
            viewType = EditableField.ViewType.NOMENCLATURE_TYPE,
            nomenclatureType = "STADE_VIE"
        ),
        EditableField(
            type = EditableField.Type.COUNTING,
            code = "SEXE",
            viewType = EditableField.ViewType.NOMENCLATURE_TYPE,
            nomenclatureType = "SEXE"
        ),
        EditableField(
            type = EditableField.Type.COUNTING,
            code = "OBJ_DENBR",
            viewType = EditableField.ViewType.NOMENCLATURE_TYPE,
            nomenclatureType = "OBJ_DENBR"
        ),
        EditableField(
            type = EditableField.Type.COUNTING,
            code = "TYP_DENBR",
            viewType = EditableField.ViewType.NOMENCLATURE_TYPE,
            nomenclatureType = "TYP_DENBR"
        ),
        EditableField(
            type = EditableField.Type.COUNTING,
            code = CountingRecord.MIN_KEY,
            viewType = EditableField.ViewType.MIN_MAX
        ).apply {
            value = PropertyValue.Number(
                code,
                1
            )
        },
        EditableField(
            type = EditableField.Type.COUNTING,
            code = CountingRecord.MAX_KEY,
            viewType = EditableField.ViewType.MIN_MAX
        ).apply {
            value = PropertyValue.Number(
                code,
                1
            )
        },
        EditableField(
            type = EditableField.Type.COUNTING,
            code = AllMediaRecord.MEDIAS_KEY,
            viewType = EditableField.ViewType.MEDIA
        ).apply {
            value = PropertyValue.Media(code)
        }
    )

    override suspend fun getNomenclatureTypeSettings(
        type: EditableField.Type,
        vararg defaultPropertySettings: PropertySettings
    ): List<EditableField> {
        if (defaultPropertySettings.isEmpty() || type == EditableField.Type.DEFAULT) {
            return defaultNomenclatureTypes.filter { it.type == type }
        }

        return defaultPropertySettings
            .mapNotNull { property ->
                defaultNomenclatureTypes.find { it.code == property.key }
                    ?.let {
                        EditableField(
                            type = it.type,
                            code = it.code,
                            viewType = it.viewType,
                            nomenclatureType = it.nomenclatureType,
                            visible = property.visible,
                            default = property.default,
                            value = it.value
                        )
                    }
            }
    }
}