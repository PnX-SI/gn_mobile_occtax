package fr.geonature.occtax.features.record.usecase

import fr.geonature.commons.data.entity.Taxonomy
import fr.geonature.commons.interactor.BaseResultUseCase
import fr.geonature.occtax.features.nomenclature.domain.EditableField
import fr.geonature.occtax.features.nomenclature.repository.IAdditionalFieldRepository
import fr.geonature.occtax.features.nomenclature.repository.INomenclatureRepository
import fr.geonature.occtax.features.record.domain.ObservationRecord
import fr.geonature.occtax.features.record.domain.PropertyValue
import fr.geonature.occtax.features.record.error.ObservationRecordException
import org.tinylog.Logger
import javax.inject.Inject

/**
 * Loads and sets default nomenclature values to a given [ObservationRecord].
 *
 * @author S. Grimault
 */
class SetDefaultNomenclatureValuesUseCase @Inject constructor(
    private val nomenclatureRepository: INomenclatureRepository,
    private val additionalFieldRepository: IAdditionalFieldRepository
) : BaseResultUseCase<ObservationRecord, SetDefaultNomenclatureValuesUseCase.Params>() {

    override suspend fun run(params: Params): Result<ObservationRecord> {
        val observationRecord = params.observationRecord

        // load default property values from editable fields of type DEFAULT
        val defaultNomenclatureValues =
            nomenclatureRepository.getEditableFields(EditableField.Type.DEFAULT)
                .getOrElse { emptyList() }
                .mapNotNull { it.value }

        defaultNomenclatureValues.map { it.toPair() }
            .forEach {
                observationRecord.properties[it.first] = it.second
            }

        // no default nomenclature values found: abort
        if (observationRecord.properties.filterValues { it is PropertyValue.Nomenclature }
                .isEmpty()) {
            return Result.failure(
                ObservationRecordException.NoDefaultNomenclatureValuesFoundException(
                    observationRecord.internalId
                )
            )
        }

        val editableFieldsInformation =
            nomenclatureRepository.getEditableFields(EditableField.Type.INFORMATION)
                .getOrElse { emptyList() } + additionalFieldRepository.getAllAdditionalFields(
                observationRecord.dataset.datasetId,
                EditableField.Type.INFORMATION
            )
                .getOrElse { emptyList() }

        if (editableFieldsInformation.isEmpty()) {
            Logger.warn {
                "no editable fields of type '${EditableField.Type.INFORMATION.name}' found"
            }

            return Result.failure(
                ObservationRecordException.NoDefaultNomenclatureValuesFoundException(
                    observationRecord.internalId
                )
            )
        }

        val editableFieldsCounting =
            nomenclatureRepository.getEditableFields(EditableField.Type.COUNTING)
                .getOrElse { emptyList() } + additionalFieldRepository.getAllAdditionalFields(
                observationRecord.dataset.datasetId,
                EditableField.Type.COUNTING
            )
                .getOrElse { emptyList() }

        if (editableFieldsCounting.isEmpty()) {
            Logger.warn {
                "no editable fields of type '${EditableField.Type.COUNTING.name}' found"
            }

            return Result.failure(
                ObservationRecordException.NoDefaultNomenclatureValuesFoundException(
                    observationRecord.internalId
                )
            )
        }

        observationRecord.taxa.taxa.forEach { taxonRecord ->
            // load property values from nomenclature values for each taxon added
            mapPropertyValuesFromNomenclature(
                taxonRecord.taxon.taxonomy,
                editableFieldsInformation,
                taxonRecord.properties.values.toList()
            )
                .map { it.toPair() }
                .forEach {
                    taxonRecord.properties[it.first] = it.second
                }

            // load property values from nomenclature values for each counting added
            taxonRecord.counting.counting.forEach { countingRecord ->
                mapPropertyValuesFromNomenclature(
                    taxonRecord.taxon.taxonomy,
                    editableFieldsCounting,
                    countingRecord.properties.values.toList()
                )
                    .map { it.toPair() }
                    .forEach {
                        countingRecord.properties[it.first] = it.second
                    }
            }
        }

        return Result.success(observationRecord)
    }

    private suspend fun mapPropertyValuesFromNomenclature(
        taxonomy: Taxonomy,
        editableFields: List<EditableField>,
        propertyValues: List<PropertyValue>
    ): List<PropertyValue> {
        return propertyValues.map {
            when (it) {
                is PropertyValue.AdditionalField -> {
                    PropertyValue.AdditionalField(
                        it.code,
                        mapPropertyValuesFromNomenclature(
                            taxonomy,
                            editableFields,
                            it.value.values.toList()
                        ).associate { pv -> pv.toPair() })
                }

                else -> {
                    editableFields.firstOrNull { editableField -> editableField.code == it.toPair().first }
                        ?.takeIf { editableField -> editableField.viewType == EditableField.ViewType.NOMENCLATURE_TYPE }
                        ?.let { editableField ->
                            when (it) {
                                is PropertyValue.Number -> it.value
                                is PropertyValue.Nomenclature -> it.value
                                else -> null
                            }?.let { currentValue ->
                                editableField.nomenclatureType?.let { nomenclatureType ->
                                    nomenclatureRepository.getNomenclatureValuesByTypeAndTaxonomy(
                                        nomenclatureType,
                                        taxonomy
                                    )
                                        .getOrDefault(emptyList())
                                        .firstOrNull { nomenclatureValue -> nomenclatureValue.id == currentValue }
                                        ?.let { nomenclatureValue ->
                                            PropertyValue.Nomenclature(
                                                code = editableField.code,
                                                label = nomenclatureValue.defaultLabel,
                                                value = nomenclatureValue.id
                                            )
                                        }
                                }
                            }
                        } ?: it
                }
            }
        }
    }

    data class Params(val observationRecord: ObservationRecord)
}