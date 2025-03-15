package fr.geonature.occtax.features.record.usecase

import fr.geonature.commons.data.entity.Taxonomy
import fr.geonature.commons.interactor.BaseResultUseCase
import fr.geonature.occtax.features.nomenclature.domain.FormField
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
        val editableFieldsDefault =
            (nomenclatureRepository.getEditableFields(FormField.Type.DEFAULT)
                .getOrElse { emptyList() } + if (params.withAdditionalFields) additionalFieldRepository.getAllAdditionalFields(
                observationRecord.dataset.dataset?.value?.id,
                FormField.Type.DEFAULT
            )
                .getOrElse { emptyList() } else emptyList()).filterIsInstance<FormField.Editable>()

        // no default nomenclature values found: abort
        if (editableFieldsDefault.isEmpty()) {
            Logger.warn {
                "no editable fields of type '${FormField.Type.DEFAULT.name}' found"
            }

            return Result.failure(
                ObservationRecordException.NoDefaultNomenclatureValuesFoundException(
                    observationRecord.internalId
                )
            )
        }

        // load default property values from editable fields of type INFORMATION
        val editableFieldsInformation =
            (nomenclatureRepository.getEditableFields(FormField.Type.INFORMATION)
                .getOrElse { emptyList() } + if (params.withAdditionalFields) additionalFieldRepository.getAllAdditionalFields(
                observationRecord.dataset.dataset?.value?.id,
                FormField.Type.INFORMATION
            )
                .getOrElse { emptyList() } else emptyList()).filterIsInstance<FormField.Editable>()

        // no nomenclature values found: abort
        if (editableFieldsInformation.isEmpty()) {
            Logger.warn {
                "no editable fields of type '${FormField.Type.INFORMATION.name}' found"
            }

            return Result.failure(
                ObservationRecordException.NoDefaultNomenclatureValuesFoundException(
                    observationRecord.internalId
                )
            )
        }

        // load default property values from editable fields of type COUNTING
        val editableFieldsCounting =
            (nomenclatureRepository.getEditableFields(FormField.Type.COUNTING)
                .getOrElse { emptyList() } + if (params.withAdditionalFields) additionalFieldRepository.getAllAdditionalFields(
                observationRecord.dataset.dataset?.value?.id,
                FormField.Type.COUNTING
            )
                .getOrElse { emptyList() } else emptyList()).filterIsInstance<FormField.Editable>()

        // no nomenclature values found: abort
        if (editableFieldsCounting.isEmpty()) {
            Logger.warn {
                "no editable fields of type '${FormField.Type.COUNTING.name}' found"
            }

            return Result.failure(
                ObservationRecordException.NoDefaultNomenclatureValuesFoundException(
                    observationRecord.internalId
                )
            )
        }

        mapPropertyValuesFromNomenclature(
            editableFieldsDefault,
            observationRecord.properties.values.toList()
        )
            .map { it.toPair() }
            .forEach {
                observationRecord.properties[it.first] = it.second
            }

        observationRecord.taxa.taxa.forEach { taxonRecord ->
            // load property values from nomenclature values for each taxon added
            mapPropertyValuesFromNomenclature(
                editableFieldsInformation,
                taxonRecord.properties.values.toList(),
                taxonRecord.taxon.taxonomy
            )
                .map { it.toPair() }
                .forEach {
                    taxonRecord.properties[it.first] = it.second
                }

            // load property values from nomenclature values for each counting added
            taxonRecord.counting.counting.forEach { countingRecord ->
                mapPropertyValuesFromNomenclature(
                    editableFieldsCounting,
                    countingRecord.properties.values.toList(),
                    taxonRecord.taxon.taxonomy
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
        editableFields: List<FormField.Editable>,
        propertyValues: List<PropertyValue>,
        taxonomy: Taxonomy? = null
    ): List<PropertyValue> {
        val editableFieldsWithValue = editableFields
            .filterIsInstance<FormField.NomenclatureType>()
            .map { ff ->
                propertyValues
                    .flatMap { pv ->
                        when (pv) {
                            is PropertyValue.AdditionalFields -> pv.value.values
                            else -> listOf(pv)
                        }
                    }
                    .firstOrNull { pv -> pv.code == ff.value.code }
                    ?.let { pv ->
                        when (pv) {
                            is PropertyValue.Nomenclature -> pv.value
                            is PropertyValue.Number -> pv.value
                            else -> null
                        }
                    }
                    ?.let { currentValue ->
                        nomenclatureRepository.getNomenclatureValuesByTypeAndTaxonomy(
                            ff.nomenclatureType,
                            taxonomy
                        )
                            .getOrDefault(emptyList())
                            .firstOrNull { nomenclatureValue -> nomenclatureValue.id == currentValue }
                            ?.let { nomenclatureValue ->
                                PropertyValue.Nomenclature(
                                    code = ff.value.code,
                                    label = nomenclatureValue.defaultLabel,
                                    value = nomenclatureValue.id
                                )
                            }
                    }
                    ?.let {
                        ff.value = it
                        ff
                    } ?: ff
            }

        return editableFieldsWithValue
            .filter { !it.additionalField }
            .map { it.value } + listOfNotNull(
            propertyValues
                .firstOrNull { pv -> pv is PropertyValue.AdditionalFields }
                ?.let { pv -> pv as PropertyValue.AdditionalFields }
                ?.let { pv ->
                    pv.copy(
                        value = pv.value + editableFieldsWithValue
                            .filter { it.additionalField }
                            .map { it.value }
                            .associate { it.toPair() })
                })
    }

    data class Params(
        val observationRecord: ObservationRecord,
        val withAdditionalFields: Boolean = false
    )
}