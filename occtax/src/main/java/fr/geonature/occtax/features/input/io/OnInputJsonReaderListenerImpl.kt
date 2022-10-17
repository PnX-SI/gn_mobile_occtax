package fr.geonature.occtax.features.input.io

import android.text.TextUtils
import android.util.JsonReader
import android.util.JsonToken
import fr.geonature.commons.data.entity.Taxon
import fr.geonature.commons.data.entity.Taxonomy
import fr.geonature.commons.features.input.io.InputJsonReader
import fr.geonature.commons.util.toDate
import fr.geonature.maps.jts.geojson.io.GeoJsonReader
import fr.geonature.occtax.features.input.domain.CountingMetadata
import fr.geonature.occtax.features.input.domain.Input
import fr.geonature.occtax.features.input.domain.InputTaxon
import fr.geonature.occtax.features.input.domain.PropertyValue
import java.io.Serializable
import java.util.Date
import kotlin.collections.set

/**
 * Default implementation of [InputJsonReader.OnInputJsonReaderListener].
 *
 * @author S. Grimault
 */
class OnInputJsonReaderListenerImpl : InputJsonReader.OnInputJsonReaderListener<Input> {

    private val geoJsonReader = GeoJsonReader()

    override fun createInput(): Input {
        return Input()
    }

    override fun readAdditionalInputData(
        reader: JsonReader,
        keyName: String,
        input: Input
    ) {
        when (keyName) {
            "geometry" -> readGeometry(
                reader,
                input
            )
            "properties" -> readProperties(
                reader,
                input
            )
            else -> reader.skipValue()
        }
    }

    private fun readGeometry(
        reader: JsonReader,
        input: Input
    ) {
        when (reader.peek()) {
            JsonToken.NULL -> reader.nextNull()
            JsonToken.BEGIN_OBJECT -> {
                input.geometry = geoJsonReader.readGeometry(reader)
            }
            else -> reader.skipValue()
        }
    }

    private fun readProperties(
        reader: JsonReader,
        input: Input
    ) {
        reader.beginObject()

        val now = Date()

        while (reader.hasNext()) {
            when (reader.nextName()) {
                "date_min" -> input.startDate = toDate(reader.nextString()) ?: now
                "date_max" -> input.endDate = toDate(reader.nextString()) ?: now
                "id_dataset" -> {
                    if (reader.peek() != JsonToken.NULL) {
                        input.datasetId = reader.nextLong()
                    } else {
                        reader.nextNull()
                    }
                }
                "id_digitiser" -> {
                    if (reader.peek() != JsonToken.NULL) {
                        input.setPrimaryInputObserverId(reader.nextLong())
                    } else {
                        reader.nextNull()
                    }
                }
                "observers" -> {
                    if (reader.peek() != JsonToken.NULL) {
                        readInputObservers(
                            reader,
                            input
                        )
                    } else {
                        reader.nextNull()
                    }
                }
                "comment" -> {
                    if (reader.peek() != JsonToken.NULL) {
                        input.comment = reader.nextString()
                    } else {
                        reader.nextNull()
                    }
                }
                "default" -> {
                    if (reader.peek() != JsonToken.NULL) {
                        readInputDefaultProperties(
                            reader,
                            input
                        )
                    } else {
                        reader.nextNull()
                    }
                }
                "t_occurrences_occtax" -> {
                    if (reader.peek() != JsonToken.NULL) {
                        readInputTaxa(
                            reader,
                            input
                        )
                    } else {
                        reader.nextNull()
                    }
                }
                else -> reader.skipValue()
            }
        }

        reader.endObject()
    }

    private fun readInputDefaultProperties(
        reader: JsonReader,
        input: Input
    ) {
        reader.beginObject()

        while (reader.hasNext()) {
            when (reader.peek()) {
                JsonToken.NAME -> {
                    when (val propertyName = reader.nextName()) {
                        else -> readPropertyValue(
                            reader,
                            propertyName
                        )?.also {
                            input.properties[it.code] = it
                        }
                    }
                }
                else -> reader.skipValue()
            }
        }

        reader.endObject()
    }

    private fun readInputObservers(
        reader: JsonReader,
        input: Input
    ) {
        reader.beginArray()

        while (reader.hasNext()) {
            when (reader.peek()) {
                JsonToken.NUMBER -> input.addInputObserverId(reader.nextLong())
                else -> reader.skipValue()
            }
        }

        reader.endArray()
    }

    private fun readInputTaxa(
        reader: JsonReader,
        input: Input
    ) {
        reader.beginArray()

        while (reader.hasNext()) {
            when (reader.peek()) {
                JsonToken.BEGIN_OBJECT -> readInputTaxon(
                    reader,
                    input
                )
                else -> reader.skipValue()
            }
        }

        reader.endArray()
    }

    /**
     * Reads input taxon as object:
     *
     * ```json
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
     *      // ...
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
     *          }
     *          // ...
     *      ]
     *  }
     * }
     * ```
     */
    private fun readInputTaxon(
        reader: JsonReader,
        input: Input
    ) {
        reader.beginObject()

        var id: Long? = null
        var name: String? = null
        var kingdom: String? = null
        var group: String? = null
        val properties = Pair(
            mutableMapOf<String, PropertyValue>(),
            mutableListOf<CountingMetadata>()
        )

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
                    } else {
                        reader.nextNull()
                    }
                }
                else -> reader.skipValue()
            }
        }

        reader.endObject()

        if (id == null || TextUtils.isEmpty(name) || TextUtils.isEmpty(kingdom)) return

        input.addInputTaxon(
            InputTaxon(
                Taxon(
                    id,
                    name!!,
                    Taxonomy(
                        kingdom!!,
                        group
                    )
                )
            ).apply {
                this.properties.putAll(properties.first)
                properties.second.forEach { this.addCountingMetadata(it) }
            })
    }

    /**
     * Reads input taxon properties as object:
     *
     * ```json
     * {
     *  "property_code_x": {
     *      "type": "PropertyType",
     *      "id": "Long",
     *      "label: "String"
     *  },
     *  // ...
     *  "counting": [
     *      {
     *          "property_code_x": {
     *              "id": "Long",
     *              "label: "String"
     *          },
     *          ...
     *          "min": "Int",
     *          "max": "Int"
     *      }
     *      // ...
     *  ]
     * }
     * ```
     */
    private fun readInputTaxonProperties(reader: JsonReader): Pair<Map<String, PropertyValue>, List<CountingMetadata>> {
        val properties = Pair(
            mutableMapOf<String, PropertyValue>(),
            mutableListOf<CountingMetadata>()
        )

        reader.beginObject()

        while (reader.hasNext()) {
            when (reader.peek()) {
                JsonToken.NAME -> {
                    when (val propertyName = reader.nextName()) {
                        "counting" -> properties.second.addAll(readInputTaxonCounting(reader))
                        else -> readPropertyValue(
                            reader,
                            propertyName
                        )?.also {
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
     * ```json
     * [
     *  {
     *      "property_code_x": {
     *          "id": "Long",
     *          "label: "String"
     *      },
     *      // ...
     *      "min": "Int",
     *      "max": "Int"
     *  }
     *  // ...
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
     * ```json
     * {
     *  "index": "Int",
     *  "property_code_x": {
     *      "id": "Long",
     *      "label: "String"
     *  },
     *  // ...
     *  "min": "Int",
     *  "max": "Int"
     * }
     * ```
     */
    private fun readInputTaxonCountingMetadata(reader: JsonReader): CountingMetadata? {
        reader.beginObject()

        var countingMetadata = CountingMetadata()

        while (reader.hasNext()) {
            when (reader.peek()) {
                JsonToken.NAME -> {
                    when (val propertyName = reader.nextName()) {
                        "index" -> countingMetadata =
                            countingMetadata.copy(index = reader.nextInt())
                        "min", "max" -> {
                            when (reader.peek()) {
                                JsonToken.NUMBER -> PropertyValue.fromValue(
                                    propertyName.uppercase(),
                                    reader.nextInt()
                                )
                                else -> readPropertyValue(
                                    reader,
                                    propertyName
                                )
                            }?.also {
                                countingMetadata.properties[it.code] = it
                            }
                        }
                        else -> readPropertyValue(
                            reader,
                            propertyName
                        )?.also {
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
     * Reads property value as object:
     *
     * ```json
     * {
     *  "label: "String"
     *  "value": "String|Long|Int"
     * }
     * ```
     */
    private fun readPropertyValue(
        reader: JsonReader,
        code: String
    ): PropertyValue? {
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
            code.uppercase(),
            label,
            value
        )

        return if (propertyValue.isEmpty()) null else propertyValue
    }
}
