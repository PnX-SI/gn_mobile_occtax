package fr.geonature.occtax.features.record.usecase

import fr.geonature.commons.data.entity.Taxonomy
import fr.geonature.commons.fp.getOrElse
import fr.geonature.commons.interactor.BaseResultUseCase
import fr.geonature.occtax.features.nomenclature.domain.EditableNomenclatureType
import fr.geonature.occtax.features.nomenclature.repository.INomenclatureRepository
import fr.geonature.occtax.features.record.domain.ObservationRecord
import fr.geonature.occtax.features.record.domain.PropertyValue
import fr.geonature.occtax.features.record.error.ObservationRecordException
import javax.inject.Inject

/**
 * Loads and sets default nomenclature values to a given [ObservationRecord].
 *
 * @author S. Grimault
 */
class SetDefaultNomenclatureValuesUseCase @Inject constructor(
    private val nomenclatureRepository: INomenclatureRepository
) : BaseResultUseCase<ObservationRecord, SetDefaultNomenclatureValuesUseCase.Params>() {

    override suspend fun run(params: Params): Result<ObservationRecord> {
        val observationRecord = params.observationRecord

        // load default property values from default nomenclature values
        val defaultNomenclatureValues =
            nomenclatureRepository.getEditableNomenclatures(EditableNomenclatureType.Type.DEFAULT)
                .getOrElse { emptyList() }
                .mapNotNull { editableNomenclatureType ->
                    editableNomenclatureType.value?.let {
                        if (it.label.isNullOrEmpty() || it.value == null) return@let null

                        PropertyValue.Nomenclature(
                            code = editableNomenclatureType.code,
                            label = it.label,
                            value = it.value as Long
                        )
                    }
                }

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

        observationRecord.taxa.taxa.forEach { taxonRecord ->
            // load property values from nomenclature values for each taxon added
            loadPropertyValuesFromNomenclature(
                taxonRecord.taxon.taxonomy,
                taxonRecord.properties.values.toList()
            )
                .map { it.toPair() }
                .forEach {
                    taxonRecord.properties[it.first] = it.second
                }

            // load property values from nomenclature values for each counting added
            taxonRecord.counting.counting.forEach { countingRecord ->
                loadPropertyValuesFromNomenclature(
                    taxonRecord.taxon.taxonomy,
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

    private suspend fun loadPropertyValuesFromNomenclature(
        taxonomy: Taxonomy,
        propertyValues: List<PropertyValue>
    ): List<PropertyValue.Nomenclature> {
        return propertyValues.filterIsInstance<PropertyValue.Nomenclature>()
            .filterNot { it.value == null }
            .mapNotNull { propertyValue ->
                nomenclatureRepository.getNomenclatureValuesByTypeAndTaxonomy(
                    propertyValue.code,
                    taxonomy
                )
                    .getOrDefault(emptyList())
                    .firstOrNull { it.id == propertyValue.value }
                    ?.let {
                        PropertyValue.Nomenclature(
                            code = propertyValue.code,
                            label = it.defaultLabel,
                            value = it.id
                        )
                    }
            }
    }

    data class Params(val observationRecord: ObservationRecord)
}