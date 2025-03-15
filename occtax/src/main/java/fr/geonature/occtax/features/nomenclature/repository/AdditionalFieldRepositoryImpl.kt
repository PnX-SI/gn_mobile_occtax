package fr.geonature.occtax.features.nomenclature.repository

import fr.geonature.commons.data.entity.AdditionalField
import fr.geonature.commons.features.nomenclature.data.IAdditionalFieldLocalDataSource
import fr.geonature.commons.util.toDate
import fr.geonature.occtax.features.nomenclature.domain.AdditionalFieldType
import fr.geonature.occtax.features.nomenclature.domain.FormField
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
        type: FormField.Type
    ): Result<List<FormField>> {
        return runCatching {
            additionalFieldLocalDataSource.getAdditionalFields(
                datasetId,
                *when (type) {
                    FormField.Type.DEFAULT -> arrayOf(AdditionalFieldType.DEFAULT.type)
                    FormField.Type.INFORMATION -> arrayOf(AdditionalFieldType.INFORMATION.type)
                    FormField.Type.COUNTING -> arrayOf(AdditionalFieldType.COUNTING.type)
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

                            FormField.Checkbox(
                                type = type,
                                label = it.additionalField.label,
                                visible = true,
                                default = true,
                                order = it.additionalField.order,
                                additionalField = true,
                                mandatory = it.additionalField.mandatory,
                                values = it.values.map { fieldValue ->
                                    PropertyValue.Text(
                                        fieldValue.value,
                                        fieldValue.label
                                    )
                                },
                                value = PropertyValue.StringArray(code = it.additionalField.name)
                            )
                                .also { ff ->
                                    val defaultValue =
                                        it.additionalField.defaultValue ?: return@also

                                    if (ff.values.map { pv -> pv.code }
                                            .any { code -> code == defaultValue }) {
                                        ff.value = PropertyValue.StringArray(
                                            code = it.additionalField.name,
                                            value = arrayOf(defaultValue)
                                        )
                                    }
                                }
                        }

                        AdditionalField.FieldType.DATE -> {
                            FormField.Date(
                                type = type,
                                label = it.additionalField.label,
                                visible = true,
                                default = true,
                                order = it.additionalField.order,
                                additionalField = true,
                                mandatory = it.additionalField.mandatory,
                                value = PropertyValue.Date(
                                    it.additionalField.name,
                                    value = it.additionalField.defaultValue?.let { defaultValue -> toDate(defaultValue) }
                                )
                            )
                        }

                        AdditionalField.FieldType.MULTISELECT -> {
                            if (it.values.isEmpty()) {
                                Logger.warn {
                                    "missing values for additional field of type '${it.additionalField.fieldType}'"
                                }

                                return@mapNotNull null
                            }

                            FormField.SelectMultiple(
                                type = type,
                                label = it.additionalField.label,
                                visible = true,
                                default = true,
                                order = it.additionalField.order,
                                additionalField = true,
                                mandatory = it.additionalField.mandatory,
                                values = it.values.map { fieldValue ->
                                    PropertyValue.Text(
                                        fieldValue.value,
                                        fieldValue.label
                                    )
                                },
                                value = PropertyValue.StringArray(it.additionalField.name)
                            )
                                .also { ff ->
                                    val defaultValue =
                                        it.additionalField.defaultValue ?: return@also

                                    if (ff.values.map { pv -> pv.code }
                                            .any { code -> code == defaultValue }) {
                                        ff.value = PropertyValue.StringArray(
                                            code = it.additionalField.name,
                                            value = arrayOf(defaultValue)
                                        )
                                    }
                                }
                        }

                        AdditionalField.FieldType.NOMENCLATURE -> {
                            it.nomenclatureTypeMnemonic?.let { mnemonic ->
                                FormField.NomenclatureType(
                                    type = type,
                                    label = it.additionalField.label,
                                    nomenclatureType = mnemonic,
                                    visible = true,
                                    default = true,
                                    order = it.additionalField.order,
                                    additionalField = true,
                                    mandatory = it.additionalField.mandatory,
                                    value = PropertyValue.Nomenclature(
                                        code = it.additionalField.name,
                                        label = null,
                                        value = it.additionalField.defaultValue?.toLongOrNull()
                                    )
                                )
                            } ?: run {
                                Logger.warn {
                                    "invalid additional field of type '${it.additionalField.fieldType}': missing nomenclature type mnemonic code"
                                }

                                null
                            }
                        }

                        AdditionalField.FieldType.NUMBER -> FormField.Number(
                            type = type,
                            label = it.additionalField.label,
                            visible = true,
                            default = true,
                            order = it.additionalField.order,
                            additionalField = true,
                            mandatory = it.additionalField.mandatory,
                            value = PropertyValue.Number(
                                code = it.additionalField.name,
                                value = it.additionalField.defaultValue?.toLongOrNull()
                            )
                        )

                        AdditionalField.FieldType.RADIO -> {
                            if (it.values.isEmpty()) {
                                Logger.warn {
                                    "missing values for additional field of type '${it.additionalField.fieldType}'"
                                }

                                return@mapNotNull null
                            }

                            FormField.Radio(
                                type = type,
                                label = it.additionalField.label,
                                visible = true,
                                default = true,
                                order = it.additionalField.order,
                                additionalField = true,
                                mandatory = it.additionalField.mandatory,
                                values = it.values.map { fieldValue ->
                                    PropertyValue.Text(
                                        fieldValue.value,
                                        fieldValue.label
                                    )
                                },
                                value = PropertyValue.Text(
                                    code = it.additionalField.name,
                                    value = null
                                )
                            )
                                .also { ff ->
                                    val defaultValue =
                                        it.additionalField.defaultValue ?: return@also

                                    if (ff.values.map { pv -> pv.code }
                                            .any { code -> code == defaultValue }) {
                                        ff.value = PropertyValue.Text(
                                            code = it.additionalField.name,
                                            value = defaultValue
                                        )
                                    }
                                }
                        }

                        AdditionalField.FieldType.SELECT -> {
                            if (it.values.isEmpty()) {
                                Logger.warn {
                                    "missing values for additional field of type '${it.additionalField.fieldType}'"
                                }

                                return@mapNotNull null
                            }

                            FormField.Select(
                                type = type,
                                label = it.additionalField.label,
                                visible = true,
                                default = true,
                                order = it.additionalField.order,
                                additionalField = true,
                                mandatory = it.additionalField.mandatory,
                                values = it.values.map { fieldValue ->
                                    PropertyValue.Text(
                                        fieldValue.value,
                                        fieldValue.label
                                    )
                                },
                                value = PropertyValue.Text(
                                    code = it.additionalField.name,
                                    value = null
                                )
                            )
                                .also { ff ->
                                    val defaultValue =
                                        it.additionalField.defaultValue ?: return@also

                                    if (ff.values.map { pv -> pv.code }
                                            .any { code -> code == defaultValue }) {
                                        ff.value = PropertyValue.Text(
                                            code = it.additionalField.name,
                                            value = defaultValue
                                        )
                                    }
                                }
                        }

                        AdditionalField.FieldType.TEXT -> FormField.Text(
                            type = type,
                            label = it.additionalField.label,
                            visible = true,
                            default = true,
                            order = it.additionalField.order,
                            additionalField = true,
                            mandatory = it.additionalField.mandatory,
                            value = PropertyValue.Text(
                                code = it.additionalField.name,
                                value = it.additionalField.defaultValue
                            )
                        )

                        AdditionalField.FieldType.TEXTAREA -> FormField.TextMultiple(
                            type = type,
                            label = it.additionalField.label,
                            visible = true,
                            default = true,
                            order = it.additionalField.order,
                            additionalField = true,
                            mandatory = it.additionalField.mandatory,
                            value = PropertyValue.Text(
                                code = it.additionalField.name,
                                value = it.additionalField.defaultValue
                            )
                        )

                        AdditionalField.FieldType.TIME -> {
                            FormField.Time(
                                type = type,
                                label = it.additionalField.label,
                                visible = true,
                                default = true,
                                order = it.additionalField.order,
                                additionalField = true,
                                mandatory = it.additionalField.mandatory,
                                value = it.additionalField.defaultValue?.let { defaultValue ->
                                    PropertyValue.Time.parse(
                                        it.additionalField.name,
                                        defaultValue
                                    )
                                } ?: PropertyValue.Time(it.additionalField.name)
                            )
                        }

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