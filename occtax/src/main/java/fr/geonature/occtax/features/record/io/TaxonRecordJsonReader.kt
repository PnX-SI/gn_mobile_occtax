package fr.geonature.occtax.features.record.io

import android.util.JsonReader
import android.util.JsonToken
import fr.geonature.commons.data.entity.Taxon
import fr.geonature.commons.data.entity.Taxonomy
import fr.geonature.commons.util.nextStringOrNull
import fr.geonature.occtax.features.record.domain.CountingRecord
import fr.geonature.occtax.features.record.domain.ObservationRecord
import fr.geonature.occtax.features.record.domain.PropertyValue
import fr.geonature.occtax.features.record.domain.TaxonRecord
import fr.geonature.occtax.features.record.error.ObservationRecordException
import java.io.IOException
import java.io.Reader
import java.io.Serializable
import java.io.StringReader

/**
 * Default `JsonReader` about reading a `JSON` stream and build the corresponding [TaxonRecord].
 *
 * @author S. Grimault
 *
 * @see TaxonRecordJsonWriter
 */
class TaxonRecordJsonReader {

    /**
     * parse a `JSON` string to convert as [TaxonRecord].
     *
     * @param json the `JSON` string to parse
     * @param observationRecord the [ObservationRecord] on which to add the [TaxonRecord] read
     * @return a [TaxonRecord] instance from the `JSON` string
     * @throws IOException if something goes wrong
     * @see [read][fr.geonature.occtax.features.record.io.TaxonRecordJsonReader.read(java.io.Reader)]
     */
    fun read(json: String, observationRecord: ObservationRecord): TaxonRecord {
        return read(
            StringReader(json),
            observationRecord
        )
    }

    /**
     * parse a `JSON` reader to convert as [TaxonRecord].
     *
     * @param reader the `Reader` to parse
     * @param observationRecord the [ObservationRecord] on which to add the [TaxonRecord] read
     * @return a [TaxonRecord] instance from the `JSON` reader
     * @throws IOException if something goes wrong
     */
    fun read(reader: Reader, observationRecord: ObservationRecord): TaxonRecord {
        val jsonReader = JsonReader(reader)
        val taxonRecord = readTaxonRecord(
            jsonReader,
            observationRecord
        )
        jsonReader.close()

        return taxonRecord
    }

    /**
     * Reads taxon as object:
     *
     * ```json
     * {
     *  "id_occurrence_occtax": "Long",
     *  "cd_nom": "String",
     *  "nom_cite": "String",
     *  "regne": "String",
     *  "group2_inpn": "String",
     *  "property_code_x": "String|Long|Int"
     *  "cor_counting_occtax": [
     *      {
     *          "index": "Int",
     *          "property_code_x": "String|Long|Int"
     *          "count_min": "Int",
     *          "count_max": "Int"
     *      }
     *  ]
     * }
     * ```
     */
    internal fun readTaxonRecord(
        reader: JsonReader,
        observationRecord: ObservationRecord
    ): TaxonRecord {
        reader.beginObject()

        var id: Long? = null
        var taxonId: Long? = null
        var name: String? = null
        var kingdom: String? = null
        var group: String? = null
        var taxonRecord: TaxonRecord? = null

        while (reader.hasNext()) {
            when (val keyName = reader.nextName()) {
                "id_occurrence_occtax" -> id = reader.nextLong()
                "cd_nom" -> taxonId = reader.nextLong()
                "nom_cite" -> name = reader.nextString()
                "regne" -> kingdom = reader.nextString()
                "group2_inpn" -> group = reader.nextString()
                // ignore legacy "properties" property
                "properties" -> reader.skipValue()
                else -> {
                    if (taxonId == null || name.isNullOrEmpty() || kingdom.isNullOrEmpty()) {
                        reader.skipValue()
                        continue
                    }

                    taxonRecord = taxonRecord ?: observationRecord.taxa.add(
                        Taxon(
                            taxonId,
                            name,
                            Taxonomy(
                                kingdom,
                                group
                            )
                        )
                    )
                        .copy(id = id)

                    if (keyName.startsWith("id_nomenclature")) {
                        readNomenclatureValue(
                            reader,
                            keyName
                        )?.toPair()
                            ?.also {
                                taxonRecord?.properties?.set(
                                    it.first,
                                    it.second
                                )
                            }

                        continue
                    }

                    if (keyName == TaxonRecord.ADDITIONAL_FIELDS_KEY) {
                        readAdditionalFields(reader).takeIf { it.isNotEmpty() }
                            ?.also {
                                taxonRecord?.additionalFields = it
                            }

                        continue
                    }

                    when (reader.peek()) {
                        JsonToken.STRING -> taxonRecord.properties[keyName] = PropertyValue.Text(
                            keyName,
                            reader.nextStringOrNull()
                        )

                        JsonToken.NUMBER -> taxonRecord.properties[keyName] = PropertyValue.Number(
                            keyName,
                            reader.nextLong()
                        )

                        JsonToken.BEGIN_ARRAY -> {
                            reader.beginArray()

                            while (reader.hasNext()) {
                                readCounting(
                                    reader,
                                    taxonRecord
                                )
                            }

                            reader.endArray()
                        }

                        else -> reader.skipValue()
                    }
                }
            }
        }

        if (taxonRecord == null && taxonId != null && !name.isNullOrEmpty() && !kingdom.isNullOrEmpty()) {
            taxonRecord = observationRecord.taxa.add(
                Taxon(
                    taxonId,
                    name,
                    Taxonomy(
                        kingdom,
                        group
                    )
                )
            )
        }

        taxonRecord?.also {
            observationRecord.taxa.addOrUpdate(taxonRecord.copy(id = id))
        }

        reader.endObject()

        return taxonRecord
            ?: throw ObservationRecordException.ReadException(observationRecord.internalId)
    }

    /**
     * Reads taxon counting as object:
     *
     * ```json
     * {
     *  "index": "Int",
     *  "property_code_x": "String|Long|Int"
     *  "count_min": "Int",
     *  "count_max": "Int"
     * }
     * ```
     */
    private fun readCounting(
        reader: JsonReader,
        taxonRecord: TaxonRecord
    ) {
        reader.beginObject()

        val countingRecord = taxonRecord.counting.create()

        while (reader.hasNext()) {
            when (val keyName = reader.nextName()) {
                // ignore "index" legacy property
                "index" -> reader.skipValue()
                else -> {
                    if (keyName.startsWith("id_nomenclature")) {
                        readNomenclatureValue(
                            reader,
                            keyName
                        )?.toPair()
                            ?.also { countingRecord.properties[it.first] = it.second }

                        continue
                    }

                    if (keyName == CountingRecord.ADDITIONAL_FIELDS_KEY) {
                        readAdditionalFields(reader).takeIf { it.isNotEmpty() }
                            ?.also {
                                countingRecord.additionalFields = it
                            }

                        continue
                    }

                    when (reader.peek()) {
                        JsonToken.STRING -> taxonRecord.properties[keyName] = PropertyValue.Text(
                            keyName,
                            reader.nextStringOrNull()
                        )

                        JsonToken.NUMBER -> countingRecord.properties[keyName] =
                            PropertyValue.Number(
                                keyName,
                                reader.nextLong()
                            )

                        else -> reader.skipValue()
                    }
                }
            }
        }

        reader.endObject()

        taxonRecord.counting.addOrUpdate(countingRecord)
    }

    private fun readAdditionalFields(reader: JsonReader): List<PropertyValue> {
        val additionalFields = mutableListOf<PropertyValue>()

        reader.beginObject()

        while (reader.hasNext()) {
            when (val keyName = reader.nextName()) {
                else -> {
                    when (reader.peek()) {
                        JsonToken.STRING -> additionalFields.add(
                            PropertyValue.Text(
                                keyName,
                                reader.nextStringOrNull()
                            )
                        )

                        JsonToken.NUMBER -> additionalFields.add(
                            PropertyValue.Number(
                                keyName,
                                reader.nextLong()
                            )
                        )

                        JsonToken.BEGIN_ARRAY -> {
                            val values = mutableListOf<Serializable>()

                            reader.beginArray()

                            while (reader.hasNext()) {
                                when (reader.peek()) {
                                    JsonToken.STRING -> values.add(reader.nextString())
                                    JsonToken.NUMBER -> values.add(reader.nextLong())
                                    else -> reader.skipValue()
                                }
                            }

                            reader.endArray()

                            (values.filterIsInstance<String>()
                                .takeIf { it.size == values.size }
                                ?.let {
                                    PropertyValue.StringArray(
                                        keyName,
                                        it.toTypedArray()
                                    )
                                } ?: values.filterIsInstance<Long>()
                                .takeIf { it.size == values.size }
                                ?.let {
                                    PropertyValue.NumberArray(
                                        keyName,
                                        it.toTypedArray()
                                    )
                                })?.also {
                                additionalFields.add(it)
                            }
                        }

                        else -> reader.skipValue()
                    }
                }
            }
        }

        reader.endObject()

        return additionalFields
    }

    /**
     * GeoNature nomenclature property values mapping
     */
    private fun readNomenclatureValue(
        reader: JsonReader,
        code: String
    ): PropertyValue? {
        val propertyValue = when (code) {
            "id_nomenclature_bio_condition" -> "ETA_BIO"
            "id_nomenclature_determination_method" -> "METH_DETERMIN"
            "id_nomenclature_obs_technique" -> "METH_OBS"
            "id_nomenclature_naturalness" -> "NATURALITE"
            "id_nomenclature_behaviour" -> "OCC_COMPORTEMENT"
            "id_nomenclature_exist_proof" -> "PREUVE_EXIST"
            "id_nomenclature_bio_status" -> "STATUT_BIO"
            "id_nomenclature_obj_count" -> "OBJ_DENBR"
            "id_nomenclature_sex" -> "SEXE"
            "id_nomenclature_life_stage" -> "STADE_VIE"
            "id_nomenclature_type_count" -> "TYP_DENBR"
            // unknown nomenclature value property
            else -> null
        }?.let {
            PropertyValue.Nomenclature(
                code = it,
                label = null,
                value = reader.nextLong()
            )
        }

        if (propertyValue == null) {
            reader.skipValue()
        }

        return propertyValue
    }
}