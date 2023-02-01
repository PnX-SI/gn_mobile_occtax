package fr.geonature.occtax.features.record.io

import android.util.JsonReader
import android.util.JsonToken
import fr.geonature.commons.util.nextStringOrNull
import fr.geonature.commons.util.toDate
import fr.geonature.maps.jts.geojson.io.GeoJsonReader
import fr.geonature.occtax.features.record.domain.ObservationRecord
import fr.geonature.occtax.features.record.domain.ObserversRecord
import fr.geonature.occtax.features.record.domain.PropertyValue
import fr.geonature.occtax.features.record.domain.TaxaRecord
import java.io.IOException
import java.io.Reader
import java.io.StringReader

/**
 * Default `JsonReader` about reading a `JSON` stream and build the corresponding [ObservationRecord].
 *
 * @author S. Grimault
 *
 * @see ObservationRecordJsonWriter
 * @see TaxonRecordJsonReader
 */
class ObservationRecordJsonReader {

    private val taxonRecordJsonReader: TaxonRecordJsonReader = TaxonRecordJsonReader()

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
                "dataset" -> reader.skipValue()
                // ignore legacy "default" property
                "default" -> reader.skipValue()
                "digitiser" -> reader.skipValue()
                ObserversRecord.OBSERVERS_KEY -> readObservers(
                    reader,
                    observationRecord
                )
                TaxaRecord.TAXA_KEY -> readTaxa(
                    reader,
                    observationRecord
                )
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

    /**
     * Reads observers as array of numbers (observer ID) or as array of full `JSON` objects.
     */
    private fun readObservers(
        reader: JsonReader,
        observationRecord: ObservationRecord
    ) {
        when (reader.peek()) {
            JsonToken.BEGIN_ARRAY -> {
                reader.beginArray()
                while (reader.hasNext()) {
                    when (reader.peek()) {
                        JsonToken.NUMBER -> {
                            observationRecord.observers.addObserverId(reader.nextLong())
                        }
                        JsonToken.BEGIN_OBJECT -> {
                            reader.beginObject()
                            while (reader.hasNext()) {
                                when (reader.nextName()) {
                                    "id_role" -> observationRecord.observers.addObserverId(reader.nextLong())
                                    else -> reader.skipValue()
                                }
                            }
                            reader.endObject()
                        }
                        else -> reader.skipValue()
                    }
                }
                reader.endArray()
            }
            else -> reader.skipValue()
        }
    }

    /**
     * Reads taxa as array of `JSON` objects.
     */
    private fun readTaxa(
        reader: JsonReader,
        observationRecord: ObservationRecord
    ) {
        when (reader.peek()) {
            JsonToken.BEGIN_ARRAY -> {
                reader.beginArray()
                while (reader.hasNext()) {
                    when (reader.peek()) {
                        JsonToken.BEGIN_OBJECT -> {
                            taxonRecordJsonReader.readTaxonRecord(
                                reader,
                                observationRecord
                            )
                        }
                        else -> reader.skipValue()
                    }
                }
                reader.endArray()
            }
            else -> reader.skipValue()
        }
    }
}