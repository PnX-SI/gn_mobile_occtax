package fr.geonature.occtax.features.nomenclature.repository

import fr.geonature.commons.data.entity.AdditionalField
import fr.geonature.commons.features.nomenclature.data.IAdditionalFieldLocalDataSource
import fr.geonature.occtax.features.nomenclature.domain.AdditionalFieldType
import fr.geonature.occtax.features.nomenclature.domain.EditableField
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
        type: EditableField.Type
    ): Result<List<EditableField>> {
        return runCatching {
            additionalFieldLocalDataSource.getAdditionalFields(
                datasetId,
                *when (type) {
                    EditableField.Type.DEFAULT -> arrayOf(AdditionalFieldType.DEFAULT.type)
                    EditableField.Type.INFORMATION -> arrayOf(AdditionalFieldType.INFORMATION.type)
                    EditableField.Type.COUNTING -> arrayOf(AdditionalFieldType.COUNTING.type)
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

                            EditableField(
                                type = type,
                                code = it.additionalField.name,
                                viewType = EditableField.ViewType.CHECKBOX,
                                visible = true,
                                default = true,
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

                            EditableField(
                                type = type,
                                code = it.additionalField.name,
                                viewType = EditableField.ViewType.SELECT_MULTIPLE,
                                visible = true,
                                default = true,
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

                        AdditionalField.FieldType.NOMENCLATURE -> {
                            it.nomenclatureTypeMnemonic?.let { mnemonic ->
                                EditableField(
                                    type = type,
                                    code = it.additionalField.name,
                                    viewType = EditableField.ViewType.NOMENCLATURE_TYPE,
                                    nomenclatureType = mnemonic,
                                    visible = true,
                                    default = true,
                                    additionalField = true,
                                    label = it.additionalField.label
                                )
                            } ?: run {
                                Logger.warn {
                                    "invalid additional field of type '${it.additionalField.fieldType}': missing nomenclature type mnemonic code"
                                }

                                null
                            }
                        }

                        AdditionalField.FieldType.NUMBER -> EditableField(
                            type = type,
                            code = it.additionalField.name,
                            viewType = EditableField.ViewType.NUMBER,
                            visible = true,
                            default = true,
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

                            EditableField(
                                type = type,
                                code = it.additionalField.name,
                                viewType = EditableField.ViewType.RADIO,
                                visible = true,
                                default = true,
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

                            EditableField(
                                type = type,
                                code = it.additionalField.name,
                                viewType = EditableField.ViewType.SELECT_SIMPLE,
                                visible = true,
                                default = true,
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

                        AdditionalField.FieldType.TEXT -> EditableField(
                            type = type,
                            code = it.additionalField.name,
                            viewType = EditableField.ViewType.TEXT_SIMPLE,
                            visible = true,
                            default = true,
                            additionalField = true,
                            label = it.additionalField.label
                        )

                        AdditionalField.FieldType.TEXTAREA -> EditableField(
                            type = type,
                            code = it.additionalField.name,
                            viewType = EditableField.ViewType.TEXT_MULTIPLE,
                            visible = true,
                            default = true,
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