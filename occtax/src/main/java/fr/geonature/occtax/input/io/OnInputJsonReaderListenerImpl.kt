package fr.geonature.occtax.input.io

import android.text.TextUtils
import android.util.JsonReader
import android.util.JsonToken
import fr.geonature.commons.data.Taxon
import fr.geonature.commons.data.Taxonomy
import fr.geonature.commons.input.AbstractInput
import fr.geonature.commons.input.io.InputJsonReader
import fr.geonature.commons.util.IsoDateUtils
import fr.geonature.maps.jts.geojson.io.GeoJsonReader
import fr.geonature.occtax.input.Input
import fr.geonature.occtax.input.InputTaxon
import fr.geonature.occtax.input.SelectedProperty
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
                               input: AbstractInput) {
        reader.beginObject()

        while (reader.hasNext()) {
            when (reader.nextName()) {
                "date_min" -> input.date = IsoDateUtils.toDate(reader.nextString()) ?: Date()
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
                                   input: AbstractInput) {
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
                              input: AbstractInput) {
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

    private fun readInputTaxon(reader: JsonReader,
                               input: AbstractInput) {
        reader.beginObject()

        var id: Long? = null
        var name: String? = null
        var kingdom: String? = null
        var group: String? = null
        val properties = mutableMapOf<String, SelectedProperty>()

        while (reader.hasNext()) {
            when (reader.nextName()) {
                "cd_nom" -> id = reader.nextLong()
                "nom_cite" -> name = reader.nextString()
                "regne" -> kingdom = reader.nextString()
                "group2_inpn" -> group = reader.nextString()
                "properties" -> {
                    if (reader.peek() != JsonToken.NULL) {
                        properties.putAll(readInputTaxonProperties(reader))
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
            this.properties.putAll(properties)
        })
    }

    private fun readInputTaxonProperties(reader: JsonReader): Map<String, SelectedProperty> {
        val properties = mutableMapOf<String, SelectedProperty>()

        reader.beginObject()

        while (reader.hasNext()) {
            when (reader.peek()) {
                JsonToken.NAME -> {
                    val selectedProperty = readInputTaxonProperty(reader,
                                                                  reader.nextName())
                    if (selectedProperty != null) properties[selectedProperty.code] = selectedProperty
                }
                else -> reader.skipValue()
            }
        }

        reader.endObject()

        return properties
    }

    private fun readInputTaxonProperty(reader: JsonReader,
                                       code: String): SelectedProperty? {
        reader.beginObject()

        var type: SelectedProperty.PropertyType? = null
        var id: Long? = null
        var label: String? = null

        while (reader.hasNext()) {
            when (reader.nextName()) {
                "type" -> type = SelectedProperty.PropertyType.fromString(reader.nextString())
                "id" -> id = reader.nextLong()
                "label" -> label = reader.nextString()
                else -> reader.skipValue()
            }
        }

        reader.endObject()

        if (type == null) return null

        val selectedProperty = SelectedProperty(type,
                                                code.toUpperCase(Locale.ROOT),
                                                id,
                                                label)

        return if (selectedProperty.isEmpty()) null else selectedProperty
    }
}