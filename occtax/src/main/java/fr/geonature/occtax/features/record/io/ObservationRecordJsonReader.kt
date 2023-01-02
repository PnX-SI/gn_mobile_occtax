package fr.geonature.occtax.features.record.io

import android.util.JsonReader
import android.util.JsonToken
import fr.geonature.commons.data.entity.Taxon
import fr.geonature.commons.data.entity.Taxonomy
import fr.geonature.commons.util.nextStringOrNull
import fr.geonature.commons.util.toDate
import fr.geonature.maps.jts.geojson.io.GeoJsonReader
import fr.geonature.occtax.features.record.domain.ObservationRecord
import fr.geonature.occtax.features.record.domain.PropertyValue
import fr.geonature.occtax.features.record.domain.TaxonRecord
import java.io.IOException
import java.io.Reader
import java.io.StringReader

/**
 * Default `JsonReader` about reading a `JSON` stream and build the corresponding [ObservationRecord].
 *
 * @author S. Grimault
 *
 * @see ObservationRecordJsonWriter
 */
class ObservationRecordJsonReader {

    /**
     * parse a `JSON` string to convert as [ObservationRecord].
     *
     * @param json the `JSON` string to parse
     * @return a [ObservationRecord] instance from the `JSON` string
     * @throws IOException if something goes wrong
     * @see [read][fr.geonature.occtax.features.record.io.ObservationRecordJsonReader.read(java.io.Reader)]
     */
    fun read(json: String): ObservationRecord {
        return read(StringReader(json))
    }

    /**
     * parse a `JSON` reader to convert as [ObservationRecord].
     *
     * @param reader the `Reader` to parse
     * @return a [ObservationRecord] instance from the `JSON` reader
     * @throws IOException if something goes wrong
     */
    fun read(reader: Reader): ObservationRecord {
        val jsonReader = JsonReader(reader)
        val observationRecord = readObservationRecord(jsonReader)
        jsonReader.close()

        return observationRecord
    }

    private fun readObservationRecord(reader: JsonReader): ObservationRecord {
        reader.beginObject()

        var observationRecord = ObservationRecord()

        while (reader.hasNext()) {
            when (reader.nextName()) {
                "id" -> reader.nextLong()
                    .also {
                        observationRecord = observationRecord.copy(
                            id = it,
                            internalId = it
                        )
                    }
                "status" -> observationRecord = observationRecord.copy(
                    status = runCatching {
                        reader
                            .nextStringOrNull()
                            ?.let { ObservationRecord.Status.valueOf(it.uppercase()) }
                    }.getOrNull()
                        ?: ObservationRecord.Status.DRAFT
                )
                "geometry" -> observationRecord = readGeometry(
                    reader,
                    observationRecord
                )
                "properties" -> observationRecord = readProperties(
                    reader,
                    observationRecord
                )
                else -> reader.skipValue()
            }
        }

        reader.endObject()

        return observationRecord
    }

    /**
     * Reads geometry as object:
     *
     * ```json
     * {
     *   "type": "Point",
     *   "coordinates": ["Double"]
     * }
     * ```
     */
    private fun readGeometry(
        reader: JsonReader,
        observationRecord: ObservationRecord
    ): ObservationRecord {
        return when (reader.peek()) {
            JsonToken.NULL -> {
                reader.nextNull()
                observationRecord
            }
            JsonToken.BEGIN_OBJECT -> {
                observationRecord.copy(geometry = GeoJsonReader().readGeometry(reader))
            }
            else -> {
                reader.skipValue()
                observationRecord
            }
        }
    }

    private fun readProperties(
        reader: JsonReader,
        observationRecord: ObservationRecord
    ): ObservationRecord {
        var updatedObservationRecord = observationRecord

        reader.beginObject()

        while (reader.hasNext()) {
            when (val keyName = reader.nextName()) {
                "internal_id" -> updatedObservationRecord =
                    updatedObservationRecord.copy(internalId = reader.nextLong())
                // ignore legacy "default" property
                "default" -> reader.skipValue()
                else -> {
                    if (keyName.startsWith("id_nomenclature")) {
                        readNomenclatureValue(
                            reader,
                            keyName
                        )?.toPair()
                            ?.also {
                                updatedObservationRecord.properties[it.first] = it.second
                            }

                        continue
                    }

                    when (reader.peek()) {
                        JsonToken.STRING -> readPropertyValueFromString(
                            reader,
                            keyName
                        ).toPair()
                            .also {
                                updatedObservationRecord.properties[it.first] = it.second
                            }
                        JsonToken.NUMBER -> updatedObservationRecord.properties[keyName] =
                            PropertyValue.Number(
                                keyName,
                                reader.nextLong()
                            )
                        JsonToken.BEGIN_ARRAY -> {
                            readPropertyValueAsArray(
                                reader,
                                keyName,
                                updatedObservationRecord
                            )
                        }
                        else -> reader.skipValue()
                    }
                }
            }
        }

        reader.endObject()

        return updatedObservationRecord
    }

    private fun readPropertyValueFromString(
        reader: JsonReader,
        code: String
    ): PropertyValue {
        val value = reader.nextStringOrNull()

        if (value.isNullOrEmpty()) {
            return PropertyValue.Text(
                code,
                value
            )
        }

        return toDate(value)?.let {
            PropertyValue.Date(
                code,
                it
            )
        } ?: PropertyValue.Text(
            code,
            value
        )
    }

    /**
     * GeoNature nomenclature property values mapping
     */
    private fun readNomenclatureValue(
        reader: JsonReader,
        code: String
    ): PropertyValue? {
        val propertyValue = when (code) {
            "id_nomenclature_grp_typ" -> "TYP_GRP"
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

    private fun readPropertyValueAsArray(
        reader: JsonReader,
        code: String,
        observationRecord: ObservationRecord
    ) {
        reader.beginArray()

        while (reader.hasNext()) {
            when (reader.peek()) {
                JsonToken.NUMBER -> {
                    ((observationRecord.properties[code] as PropertyValue.NumberArray?)
                        ?: PropertyValue.NumberArray(
                            code,
                            emptyArray()
                        )).let {
                        it.copy(
                            code = it.code,
                            value = it.value + arrayOf(reader.nextLong())
                        )
                    }
                        .also {
                            observationRecord.properties[code] = it
                        }
                }
                JsonToken.BEGIN_OBJECT -> {
                    readTaxon(
                        reader,
                        observationRecord
                    )
                }
                else -> reader.skipValue()
            }
        }

        reader.endArray()
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
    private fun readTaxon(
        reader: JsonReader,
        observationRecord: ObservationRecord
    ) {
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

                    when (reader.peek()) {
                        JsonToken.STRING -> readPropertyValueFromString(
                            reader,
                            keyName
                        ).toPair()
                            .also {
                                taxonRecord?.properties?.set(
                                    it.first,
                                    it.second
                                )
                            }
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

                    when (reader.peek()) {
                        JsonToken.STRING -> readPropertyValueFromString(
                            reader,
                            keyName
                        ).toPair()
                            .also {
                                countingRecord.properties[it.first] = it.second
                            }
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
}