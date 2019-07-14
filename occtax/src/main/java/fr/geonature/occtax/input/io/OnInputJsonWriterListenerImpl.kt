package fr.geonature.occtax.input.io

import android.util.JsonWriter
import fr.geonature.commons.input.AbstractInputTaxon
import fr.geonature.commons.input.io.InputJsonWriter
import fr.geonature.commons.util.IsoDateUtils
import fr.geonature.maps.jts.geojson.io.GeoJsonWriter
import fr.geonature.occtax.input.Input

/**
 * Default implementation of [InputJsonWriter.OnInputJsonWriterListener].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class OnInputJsonWriterListenerImpl : InputJsonWriter.OnInputJsonWriterListener<Input> {

    private val geoJsonWriter = GeoJsonWriter()

    override fun writeAdditionalInputData(writer: JsonWriter,
                                          input: Input) {
        writeGeometry(writer,
                      input)
        writeProperties(writer,
                        input)
    }

    private fun writeGeometry(writer: JsonWriter,
                              input: Input) {
        writer.name("geometry")

        val geometry = input.geometry

        if (geometry == null) {
            writer.nullValue()
        }
        else {
            geoJsonWriter.writeGeometry(writer,
                                        geometry)
        }
    }

    private fun writeProperties(writer: JsonWriter,
                                input: Input) {
        writer.name("properties")
            .beginObject()

        writer.name("meta_device_entry")
            .value("mobile")

        writeDate(writer,
                  input)

        writer.name("id_digitiser")
            .value(input.getPrimaryObserverId())
        writeInputObserverIds(writer,
                              input)

        writeInputTaxa(writer,
                       input)

        writer.endObject()
    }

    private fun writeDate(writer: JsonWriter,
                          input: Input) {
        val dateToIsoString = IsoDateUtils.toIsoDateString(input.date)
        writer.name("date_min")
            .value(dateToIsoString)
        writer.name("date_max")
            .value(dateToIsoString)
    }

    private fun writeInputObserverIds(writer: JsonWriter,
                                      input: Input) {
        writer.name("observers")
            .beginArray()

        input.getInputObserverIds()
            .forEach { writer.value(it) }

        writer.endArray()
    }

    private fun writeInputTaxa(writer: JsonWriter,
                               input: Input) {
        writer.name("t_occurrences_occtax")
            .beginArray()

        input.getInputTaxa()
            .forEach {
                writeInputTaxon(writer,
                                it)
            }

        writer.endArray()
    }

    private fun writeInputTaxon(writer: JsonWriter,
                                inputTaxon: AbstractInputTaxon) {
        writer.beginObject()

        writer.name("cd_nom")
            .value(inputTaxon.id)

        writer.endObject()
    }
}