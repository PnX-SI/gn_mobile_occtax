package fr.geonature.occtax.input.io

import android.text.TextUtils
import android.util.JsonReader
import android.util.JsonToken
import fr.geonature.commons.data.Taxon
import fr.geonature.commons.data.Taxonomy
import fr.geonature.commons.input.io.InputJsonReader
import fr.geonature.commons.util.IsoDateUtils
import fr.geonature.maps.jts.geojson.io.GeoJsonReader
import fr.geonature.occtax.input.CountingMetadata
import fr.geonature.occtax.input.Input
import fr.geonature.occtax.input.InputTaxon
import fr.geonature.occtax.input.PropertyValue
import java.io.Serializable
import java.util.Date
import java.util.Locale

/**
 * Default implementation of [InputJsonReader.OnInputJsonReaderListener].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class OnInputJsonReaderListenerImpl : InputJsonReader.OnInputJsonReaderListener<Input> {

    private val geoJsonReader = GeoJsonReader()

    override fun createInput(): Input {
        return Input()
    }

    override fun readAdditionalInputData(reader: JsonReader,
                                         keyName: String,
                                         input: Input) {
        when (keyName) {
            "geometry" -> readGeometry(reader,
                                       input)
            "properties" -> readProperties(reader,
                                           input)
            else -> reader.skipValue()
        }
    }

    private fun readGeometry(reader: JsonReader,
                             input: Input) {
        when (reader.peek()) {
            JsonToken.NULL -> reader.nextNull()
            JsonToken.BEGIN_OBJECT -> {
                input.geometry = geoJsonReader.readGeometry(reader)
            }
            else -> reader.skipValue()
        }
    }

    private fun readProperties(reader: JsonReader,
                               input: Input) {
        reader.beginObject()

        while (reader.hasNext()) {
            when (reader.nextName()) {
                "date_min" -> input.date = IsoDateUtils.toDate(reader.nextString()) ?: Date()
                "id_dataset" -> {
                    if (reader.peek() != JsonToken.NULL) {
                        input.datasetId = reader.nextLong()
                    }
                    else {
                        reader.nextNull()
                    }
                }
                "id_digitiser" -> {
                    if (reader.peek() != JsonToken.NULL) {
                        input.setPrimaryInputObserverId(reader.nextLong())
                    }
                    else {
                        reader.nextNull()
                    }
                }
                "observers" -> {
                    if (reader.peek() != JsonToken.NULL) {
                        readInputObservers(reader,
                                           input)
                    }
                    else {
                        reader.nextNull()
                    }
                }
                "comment" -> {
                    if (reader.peek() != JsonToken.NULL) {
                        input.comment = reader.nextString()
                    }
                    else {
                        reader.nextNull()
                    }
                }
                "t_occurrences_occtax" -> {
                    if (reader.peek() != JsonToken.NULL) {
                        readInputTaxa(reader,
                                      input)
                    }
                    else {
                        reader.nextNull()
                    }
                }
                else -> reader.skipValue()
            }
        }

        reader.endObject()
    }

    private fun readInputObservers(reader: JsonReader,
                                   input: Input) {
        reader.beginArray()

        while (reader.hasNext()) {
            when (reader.peek()) {
                JsonToken.NUMBER -> input.addInputObserverId(reader.nextLong())
                else -> reader.skipValue()
            }
        }

        reader.endArray()
    }

    private fun readInputTaxa(reader: JsonReader,
                              input: Input) {
        reader.beginArray()

        while (reader.hasNext()) {
            when (reader.peek()) {
                JsonToken.BEGIN_OBJECT -> readInputTaxon(reader,
                                                         input)
                else -> reader.skipValue()
            }
        }

        reader.endArray()
    }

    /**
     * Reads input taxon as object:
     *
     * ```
     * {
     *  "cd_nom": "String",
     *  "nom_cite": "String",
     *  "regne": "String",
     *  "group2_inpn": "String",
     *  "properties": {
     *      "property_code_x": {
     *          "type": "PropertyType",
     *          "id": "Long",
     *          "label: "String"
     *      },
     *      ...
     *      "counting": [
     *          {
     *              "property_code_x": {
     *                  "type": "PropertyType",
     *                  "id": "Long",
     *                  "label: "String"
     *              },
     *              ...
     *              "min": "Int",
     *              "max": "Int"
     *          },
     *          ...
     *      ]
     *  }
     * }
     * ```
     */
    private fun readInputTaxon(reader: JsonReader,
                               input: Input) {
        reader.beginObject()

        var id: Long? = null
        var name: String? = null
        var kingdom: String? = null
        var group: String? = null
        val properties = Pair(mutableMapOf<String, PropertyValue>(),
                              mutableListOf<CountingMetadata>())

        while (reader.hasNext()) {
            when (reader.nextName()) {
                "cd_nom" -> id = reader.nextLong()
                "nom_cite" -> name = reader.nextString()
                "regne" -> kingdom = reader.nextString()
                "group2_inpn" -> group = reader.nextString()
                "properties" -> {
                    if (reader.peek() != JsonToken.NULL) {
                        readInputTaxonProperties(reader).also {
                            properties.first.putAll(it.first)
                            properties.second.addAll(it.second)
                        }
                    }
                    else {
                        reader.nextNull()
                    }
                }
                else -> reader.skipValue()
            }
        }

        reader.endObject()

        if (id == null || TextUtils.isEmpty(name) || TextUtils.isEmpty(kingdom)) return

        input.addInputTaxon(InputTaxon(Taxon(id,
                                             name!!,
                                             Taxonomy(kingdom!!,
                                                      group))).apply {
            this.properties.putAll(properties.first)
            properties.second.forEach { this.addCountingMetadata(it) }
        })
    }

    /**
     * Reads input taxon properties as object:
     *
     * ```
     * {
     *  "property_code_x": {
     *      "type": "PropertyType",
     *      "id": "Long",
     *      "label: "String"
     *  },
     *  ...
     *  "counting": [
     *      {
     *          "property_code_x": {
     *              "id": "Long",
     *              "label: "String"
     *          },
     *          ...
     *          "min": "Int",
     *          "max": "Int"
     *      },
     *      ...
     *  ]
     * }
     * ```
     */
    private fun readInputTaxonProperties(reader: JsonReader): Pair<Map<String, PropertyValue>, List<CountingMetadata>> {
        val properties = Pair(mutableMapOf<String, PropertyValue>(),
                              mutableListOf<CountingMetadata>())

        reader.beginObject()

        while (reader.hasNext()) {
            when (reader.peek()) {
                JsonToken.NAME -> {
                    when (val propertyName = reader.nextName()) {
                        "counting" -> properties.second.addAll(readInputTaxonCounting(reader))
                        else -> readInputTaxonPropertyValue(reader,
                                                            propertyName)?.also {
                            properties.first[it.code] = it
                        }
                    }
                }
                else -> reader.skipValue()
            }
        }

        reader.endObject()

        return properties
    }

    /**
     * Reads input taxon counting as array:
     *
     * ```
     * [
     *  {
     *      "property_code_x": {
     *          "id": "Long",
     *          "label: "String"
     *      },
     *      ...
     *      "min": "Int",
     *      "max": "Int"
     *  },
     *  ...
     * ]
     * ```
     */
    private fun readInputTaxonCounting(reader: JsonReader): List<CountingMetadata> {
        val counting = mutableListOf<CountingMetadata>()

        reader.beginArray()

        while (reader.hasNext()) {
            readInputTaxonCountingMetadata(reader)?.also {
                counting.add(it)
            }
        }

        reader.endArray()

        return counting
    }

    /**
     * Reads input taxon counting metadata as object:
     *
     * ```
     * {
     *  "index": "Int",
     *  "property_code_x": {
     *      "id": "Long",
     *      "label: "String"
     *  },
     *  ...
     *  "min": "Int",
     *  "max": "Int"
     * }
     * ```
     */
    private fun readInputTaxonCountingMetadata(reader: JsonReader): CountingMetadata? {
        reader.beginObject()

        val countingMetadata = CountingMetadata()

        while (reader.hasNext()) {
            when (reader.peek()) {
                JsonToken.NAME -> {
                    when (val propertyName = reader.nextName()) {
                        "index" -> countingMetadata.index = reader.nextInt()
                        "min" -> countingMetadata.min = reader.nextInt()
                        "max" -> countingMetadata.max = reader.nextInt()
                        else -> readInputTaxonPropertyValue(reader,
                                                            propertyName)?.also {
                            countingMetadata.properties[it.code] = it
                        }
                    }
                }
                else -> reader.skipValue()
            }
        }

        reader.endObject()

        return if (countingMetadata.isEmpty()) null else countingMetadata
    }

    /**
     * Reads input taxon property as object:
     *
     * ```
     * {
     *  "label: "String"
     *  "value": "String"|Long|Int
     * }
     * ```
     */
    private fun readInputTaxonPropertyValue(reader: JsonReader,
                                            code: String): PropertyValue? {
        reader.beginObject()

        var label: String? = null
        var value: Serializable? = null

        while (reader.hasNext()) {
            when (reader.nextName()) {
                "label" -> label = reader.nextString()
                "value" -> {
                    when (reader.peek()) {
                        JsonToken.STRING -> value = reader.nextString()
                        JsonToken.NUMBER -> value = reader.nextLong()
                        else -> reader.nextNull()
                    }
                }
                else -> reader.skipValue()
            }
        }

        reader.endObject()

        val propertyValue = PropertyValue(
                code.toUpperCase(Locale.ROOT),
                label,
                value)

        return if (propertyValue.isEmpty()) null else propertyValue
    }
}