package fr.geonature.occtax.features.nomenclature.repository

import fr.geonature.commons.data.entity.AdditionalField
import fr.geonature.commons.features.nomenclature.data.IAdditionalFieldLocalDataSource
import fr.geonature.occtax.features.nomenclature.domain.AdditionalFieldType
import fr.geonature.occtax.features.nomenclature.domain.EditableNomenclatureType
import fr.geonature.occtax.features.record.domain.PropertyValue
import org.tinylog.Logger

/**
 * Default implementation of [IAdditionalFieldRepository].
 *
 * @author S. Grimault
 */
class AdditionalFieldRepositoryImpl(
    private val additionalFieldLocalDataSource: IAdditionalFieldLocalDataSource
) :
    IAdditionalFieldRepository {

    override suspend fun getAllAdditionalFields(
        datasetId: Long?,
        type: EditableNomenclatureType.Type
    ): Result<List<EditableNomenclatureType>> {
        return runCatching {
            additionalFieldLocalDataSource.getAdditionalFields(
                datasetId,
                *when (type) {
                    EditableNomenclatureType.Type.DEFAULT -> arrayOf(AdditionalFieldType.DEFAULT.type)
                    EditableNomenclatureType.Type.INFORMATION -> arrayOf(AdditionalFieldType.INFORMATION.type)
                    EditableNomenclatureType.Type.COUNTING -> arrayOf(AdditionalFieldType.COUNTING.type)
                }
            )
                .mapNotNull {
                    when (it.additionalField.fieldType) {
                        AdditionalField.FieldType.CHECKBOX -> {
                            if (it.values.isEmpty()) {
                                Logger.warn {
                                    "missing values for additional field of type '${it.additionalField.fieldType}'"
                                }

                                return@mapNotNull null
                            }

                            EditableNomenclatureType(
                                type = type,
                                code = it.additionalField.name,
                                viewType = EditableNomenclatureType.ViewType.CHECKBOX,
                                visible = true,
                                default = type != EditableNomenclatureType.Type.INFORMATION,
                                additionalField = true,
                                label = it.additionalField.label,
                                values = it.values.map { fieldValue ->
                                    PropertyValue.Text(
                                        fieldValue.value,
                                        fieldValue.label
                                    )
                                }
                            )
                        }

                        AdditionalField.FieldType.MULTISELECT -> {
                            if (it.values.isEmpty()) {
                                Logger.warn {
                                    "missing values for additional field of type '${it.additionalField.fieldType}'"
                                }

                                return@mapNotNull null
                            }

                            EditableNomenclatureType(
                                type = type,
                                code = it.additionalField.name,
                                viewType = EditableNomenclatureType.ViewType.SELECT_MULTIPLE,
                                visible = true,
                                default = type != EditableNomenclatureType.Type.INFORMATION,
                                additionalField = true,
                                label = it.additionalField.label,
                                values = it.values.map { fieldValue ->
                                    PropertyValue.Text(
                                        fieldValue.value,
                                        fieldValue.label
                                    )
                                }
                            )
                        }

                        // TODO: additional field code should use mnemonic from nomenclature type...
                        // AdditionalField.FieldType.NOMENCLATURE -> EditableNomenclatureType(
                        //     type = type,
                        //     code = it.additionalField.name,
                        //     viewType = EditableNomenclatureType.ViewType.NOMENCLATURE_TYPE,
                        //     visible = true,
                        //     default = type != EditableNomenclatureType.Type.INFORMATION,
                        //     additionalField = true,
                        //     label = it.additionalField.label
                        // )

                        AdditionalField.FieldType.NUMBER -> EditableNomenclatureType(
                            type = type,
                            code = it.additionalField.name,
                            viewType = EditableNomenclatureType.ViewType.NUMBER,
                            visible = true,
                            default = type != EditableNomenclatureType.Type.INFORMATION,
                            additionalField = true,
                            label = it.additionalField.label
                        )

                        AdditionalField.FieldType.RADIO -> {
                            if (it.values.isEmpty()) {
                                Logger.warn {
                                    "missing values for additional field of type '${it.additionalField.fieldType}'"
                                }

                                return@mapNotNull null
                            }

                            EditableNomenclatureType(
                                type = type,
                                code = it.additionalField.name,
                                viewType = EditableNomenclatureType.ViewType.RADIO,
                                visible = true,
                                default = type != EditableNomenclatureType.Type.INFORMATION,
                                additionalField = true,
                                label = it.additionalField.label,
                                values = it.values.map { fieldValue ->
                                    PropertyValue.Text(
                                        fieldValue.value,
                                        fieldValue.label
                                    )
                                }
                            )
                        }

                        AdditionalField.FieldType.SELECT -> {
                            if (it.values.isEmpty()) {
                                Logger.warn {
                                    "missing values for additional field of type '${it.additionalField.fieldType}'"
                                }

                                return@mapNotNull null
                            }

                            EditableNomenclatureType(
                                type = type,
                                code = it.additionalField.name,
                                viewType = EditableNomenclatureType.ViewType.SELECT_SIMPLE,
                                visible = true,
                                default = type != EditableNomenclatureType.Type.INFORMATION,
                                additionalField = true,
                                label = it.additionalField.label,
                                values = it.values.map { fieldValue ->
                                    PropertyValue.Text(
                                        fieldValue.value,
                                        fieldValue.label
                                    )
                                }
                            )
                        }

                        AdditionalField.FieldType.TEXT -> EditableNomenclatureType(
                            type = type,
                            code = it.additionalField.name,
                            viewType = EditableNomenclatureType.ViewType.TEXT_SIMPLE,
                            visible = true,
                            default = type != EditableNomenclatureType.Type.INFORMATION,
                            additionalField = true,
                            label = it.additionalField.label
                        )

                        AdditionalField.FieldType.TEXTAREA -> EditableNomenclatureType(
                            type = type,
                            code = it.additionalField.name,
                            viewType = EditableNomenclatureType.ViewType.TEXT_MULTIPLE,
                            visible = true,
                            default = type != EditableNomenclatureType.Type.INFORMATION,
                            additionalField = true,
                            label = it.additionalField.label
                        )

                        else -> {
                            Logger.warn {
                                "additional field of type '${it.additionalField.fieldType}' is not supported"
                            }

                            null
                        }
                    }
                }
        }
    }
}