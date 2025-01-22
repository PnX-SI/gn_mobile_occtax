package fr.geonature.occtax.features.nomenclature.data

import android.content.Context
import fr.geonature.occtax.R
import fr.geonature.occtax.features.nomenclature.domain.FormField
import fr.geonature.occtax.features.record.domain.AllMediaRecord
import fr.geonature.occtax.features.record.domain.CountingRecord
import fr.geonature.occtax.features.record.domain.PropertyValue
import fr.geonature.occtax.features.settings.domain.PropertySettings

/**
 * Default implementation of [INomenclatureSettingsLocalDataSource].
 *
 * @author S. Grimault
 */
class NomenclatureSettingsLocalDataSourceImpl(context: Context) :
    INomenclatureSettingsLocalDataSource {

    private val defaultNomenclatureTypes = listOf(
        FormField.NomenclatureType(
            type = FormField.Type.DEFAULT,
            label = context.getString(R.string.nomenclature_typ_grp),
            nomenclatureType = "TYP_GRP",
            value = PropertyValue.Nomenclature(code = "TYP_GRP")
        ),
        FormField.NomenclatureType(
            type = FormField.Type.INFORMATION,
            label = context.getString(R.string.nomenclature_meth_obs),
            nomenclatureType = "METH_OBS",
            value = PropertyValue.Nomenclature(code = "METH_OBS")
        ),
        FormField.NomenclatureType(
            type = FormField.Type.INFORMATION,
            label = context.getString(R.string.nomenclature_eta_bio),
            nomenclatureType = "ETA_BIO",
            value = PropertyValue.Nomenclature(code = "ETA_BIO")
        ),
        FormField.NomenclatureType(
            type = FormField.Type.INFORMATION,
            label = context.getString(R.string.nomenclature_meth_determin),
            default = false,
            nomenclatureType = "METH_DETERMIN",
            value = PropertyValue.Nomenclature(code = "METH_DETERMIN")
        ),
        FormField.Text(
            type = FormField.Type.INFORMATION,
            label = context.getString(R.string.nomenclature_determiner),
            default = false,
            value = PropertyValue.Text(code = "determiner")
        ),
        FormField.NomenclatureType(
            type = FormField.Type.INFORMATION,
            label = context.getString(R.string.nomenclature_statut_bio),
            default = false,
            nomenclatureType = "STATUT_BIO",
            value = PropertyValue.Nomenclature(code = "STATUT_BIO")
        ),
        FormField.NomenclatureType(
            type = FormField.Type.INFORMATION,
            label = context.getString(R.string.nomenclature_occ_comportement),
            default = false,
            nomenclatureType = "OCC_COMPORTEMENT",
            value = PropertyValue.Nomenclature(code = "OCC_COMPORTEMENT")
        ),
        FormField.NomenclatureType(
            type = FormField.Type.INFORMATION,
            label = context.getString(R.string.nomenclature_naturalite),
            default = false,
            nomenclatureType = "NATURALITE",
            value = PropertyValue.Nomenclature(code = "NATURALITE")
        ),
        FormField.NomenclatureType(
            type = FormField.Type.INFORMATION,
            label = context.getString(R.string.nomenclature_preuve_exist),
            default = false,
            nomenclatureType = "PREUVE_EXIST",
            value = PropertyValue.Nomenclature(code = "PREUVE_EXIST")
        ),
        FormField.TextMultiple(
            type = FormField.Type.INFORMATION,
            label = context.getString(R.string.nomenclature_comment),
            default = false,
            value = PropertyValue.Text(code = "comment")
        ),
        FormField.NomenclatureType(
            type = FormField.Type.COUNTING,
            label = context.getString(R.string.nomenclature_stade_vie),
            nomenclatureType = "STADE_VIE",
            value = PropertyValue.Nomenclature(code = "STADE_VIE")
        ),
        FormField.NomenclatureType(
            type = FormField.Type.COUNTING,
            label = context.getString(R.string.nomenclature_sexe),
            nomenclatureType = "SEXE",
            value = PropertyValue.Nomenclature(code = "SEXE")
        ),
        FormField.NomenclatureType(
            type = FormField.Type.COUNTING,
            label = context.getString(R.string.nomenclature_obj_denbr),
            nomenclatureType = "OBJ_DENBR",
            value = PropertyValue.Nomenclature(code = "OBJ_DENBR")
        ),
        FormField.NomenclatureType(
            type = FormField.Type.COUNTING,
            label = context.getString(R.string.nomenclature_typ_denbr),
            nomenclatureType = "TYP_DENBR",
            value = PropertyValue.Nomenclature(code = "TYP_DENBR")
        ),
        FormField.MinMax(
            type = FormField.Type.COUNTING,
            label = "",
            min = FormField.Number(
                type = FormField.Type.COUNTING,
                label = context.getString(R.string.nomenclature_count_min),
                value = PropertyValue.Number(
                    CountingRecord.MIN_KEY,
                    1
                )
            ),
            max = FormField.Number(
                type = FormField.Type.COUNTING,
                label = context.getString(R.string.nomenclature_count_max),
                value = PropertyValue.Number(
                    CountingRecord.MAX_KEY,
                    1
                )
            ),
        ),
        FormField.Media(
            type = FormField.Type.COUNTING,
            label = context.getString(R.string.nomenclature_media),
            value = PropertyValue.Media(AllMediaRecord.MEDIAS_KEY)
        )
    )

    override suspend fun getNomenclatureTypeSettings(
        type: FormField.Type,
        vararg defaultPropertySettings: PropertySettings
    ): List<FormField> {
        if (defaultPropertySettings.isEmpty() || type == FormField.Type.DEFAULT) {
            return defaultNomenclatureTypes.filter { it.type == type }
                .mapIndexed { index, formField ->
                    formField.update(order = index)
                }
        }

        return defaultNomenclatureTypes.mapNotNull { ff ->
            when (ff) {
                is FormField.Editable -> defaultPropertySettings.indexOfFirst { property -> ff.getValue().code == property.key }
                    .takeIf { it >= 0 }
                    ?.let {
                        ff.update(
                            visible = defaultPropertySettings[it].visible,
                            default = defaultPropertySettings[it].default,
                            order = it
                        )
                    }

                is FormField.MinMax -> defaultPropertySettings.indexOfFirst { propertySettings ->
                    arrayOf(
                        ff.min,
                        ff.max
                    ).any { it.value.code == propertySettings.key }
                }
                    .takeIf { it >= 0 }
                    ?.let {
                        ff.copy(
                            order = it,
                            min = defaultPropertySettings.find { property -> ff.min.value.code == property.key }
                                ?.let { property ->
                                    ff.min.copy(
                                        visible = property.visible,
                                        default = property.default
                                    )
                                } ?: ff.min.copy(
                                visible = false,
                                default = false
                            ),
                            max = defaultPropertySettings.find { property -> ff.max.value.code == property.key }
                                ?.let { property ->
                                    ff.max.copy(
                                        visible = property.visible,
                                        default = property.default
                                    )
                                } ?: ff.max.copy(
                                visible = false,
                                default = false
                            )
                        )
                    }

                else -> null
            }
        }
    }
}