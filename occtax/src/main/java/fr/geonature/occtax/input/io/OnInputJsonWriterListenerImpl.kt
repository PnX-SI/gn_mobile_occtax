package fr.geonature.occtax.input.io

import android.text.TextUtils
import android.util.JsonWriter
import fr.geonature.commons.input.AbstractInputTaxon
import fr.geonature.commons.input.io.InputJsonWriter
import fr.geonature.commons.util.IsoDateUtils
import fr.geonature.maps.jts.geojson.io.GeoJsonWriter
import fr.geonature.occtax.input.Input
import fr.geonature.occtax.input.InputTaxon
import fr.geonature.occtax.input.SelectedProperty
import java.util.Locale

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
            .value(inputTaxon.taxon.id)
        writer.name("nom_cite")
            .value(inputTaxon.taxon.name)
        writer.name("regne")
            .value(inputTaxon.taxon.taxonomy.kingdom)
        writer.name("group2_inpn")
            .value(inputTaxon.taxon.taxonomy.group)

        val defaultMnemonicOrder = arrayOf("METH_OBS",
                                           "ETA_BIO",
                                           "METH_DETERMIN",
                                           "DETERMINER",
                                           "STATUT_BIO",
                                           "NATURALITE",
                                           "PREUVE_EXIST",
                                           "COMMENT")

        writeInputTaxonProperties(writer,
                                  (inputTaxon as InputTaxon).properties.toSortedMap(Comparator { o1, o2 ->
                                      val i1 = defaultMnemonicOrder.indexOfFirst { it == o1 }
                                      val i2 = defaultMnemonicOrder.indexOfFirst { it == o2 }

                                      when {
                                          i1 == -1 -> 1
                                          i2 == -1 -> -1
                                          else -> i1 - i2
                                      }
                                  }))

        writer.endObject()
    }

    private fun writeInputTaxonProperties(writer: JsonWriter,
                                          properties: Map<String, SelectedProperty>) {
        writer.name("properties")

        if (properties.isEmpty()) {
            writer.nullValue()
            return
        }

        writer.beginObject()

        properties.forEach {
            writeInputTaxonProperty(writer,
                                    it.key,
                                    it.value)
        }

        writer.endObject()

        // GeoNature mapping
        properties.forEach {
            if (it.value.isEmpty()) return@forEach

            when (it.key) {
                "METH_OBS" -> writer.name("id_nomenclature_obs_meth").value(it.value.id)
                "ETA_BIO" -> writer.name("id_nomenclature_bio_condition").value(it.value.id)
                "METH_DETERMIN" -> writer.name("id_nomenclature_determination_method").value(it.value.id)
                "DETERMINER" -> writer.name("determiner").value(it.value.label)
                "STATUT_BIO" -> writer.name("id_nomenclature_bio_status").value(it.value.id)
                "NATURALITE" -> writer.name("id_nomenclature_naturalness").value(it.value.id)
                "PREUVE_EXIST" -> writer.name("id_nomenclature_exist_proof").value(it.value.id)
                "COMMENT" -> writer.name("comment").value(it.value.label)
            }
        }
    }

    private fun writeInputTaxonProperty(writer: JsonWriter,
                                        name: String,
                                        selectedProperty: SelectedProperty) {
        if (selectedProperty.isEmpty()) return

        writer.name(name.toLowerCase(Locale.ROOT))
            .beginObject()

        writer.name("type")
            .value(selectedProperty.type.name.toLowerCase(Locale.ROOT))

        if (selectedProperty.id != null) {
            writer.name("id")
                .value(selectedProperty.id)
        }

        if (!TextUtils.isEmpty(selectedProperty.label)) {
            writer.name("label")
                .value(selectedProperty.label)
        }

        writer.endObject()
    }
}