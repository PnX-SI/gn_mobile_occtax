package fr.geonature.occtax.input.io

import android.util.JsonReader
import android.util.JsonToken
import fr.geonature.commons.input.AbstractInput
import fr.geonature.commons.input.io.InputJsonReader
import fr.geonature.commons.util.IsoDateUtils
import fr.geonature.maps.jts.geojson.io.GeoJsonReader
import fr.geonature.occtax.input.Input
import fr.geonature.occtax.input.InputTaxon
import java.util.Date

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

        val inputTaxon = InputTaxon()

        while (reader.hasNext()) {
            when (reader.nextName()) {
                "cd_nom" -> inputTaxon.id = reader.nextLong()
                else -> reader.skipValue()
            }
        }

        reader.endObject()

        input.addInputTaxon(inputTaxon)
    }
}