package fr.geonature.occtax.input.io

import android.text.TextUtils
import android.util.JsonWriter
import fr.geonature.commons.input.AbstractInputTaxon
import fr.geonature.commons.input.io.InputJsonWriter
import fr.geonature.commons.util.IsoDateUtils
import fr.geonature.maps.jts.geojson.io.GeoJsonWriter
import fr.geonature.occtax.input.CountingMetadata
import fr.geonature.occtax.input.Input
import fr.geonature.occtax.input.InputTaxon
import fr.geonature.occtax.input.PropertyValue
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

        writer.name("id_dataset")
            .value(input.datasetId)
        writer.name("id_nomenclature_obs_meth")
            .value(input.technicalObservationId)

        writer.name("id_digitiser")
            .value(input.getPrimaryObserverId())

        writeInputObserverIds(writer,
                              input)

        writer.name("comment")
            .value(input.comment)

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

        input.getAllInputObserverIds()
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

        writeInputTaxonProperties(writer,
                                  (inputTaxon as InputTaxon).properties,
                                  inputTaxon.getCounting())

        writer.endObject()
    }

    private fun writeInputTaxonProperties(writer: JsonWriter,
                                          properties: Map<String, PropertyValue>,
                                          counting: List<CountingMetadata>) {
        writer.name("properties")

        if (properties.isEmpty() && counting.isEmpty()) {
            writer.nullValue()
            return
        }

        writer.beginObject()

        properties.forEach {
            writeInputTaxonPropertyValue(writer,
                                         it.key,
                                         it.value)
        }

        writeInputTaxonCounting(writer,
                                counting)

        writer.endObject()

        // GeoNature mapping
        properties.forEach {
            if (it.value.isEmpty()) return@forEach

            when (it.key) {
                "METH_OBS" -> writer.name("id_nomenclature_obs_meth").value(it.value.value as Long)
                "ETA_BIO" -> writer.name("id_nomenclature_bio_condition").value(it.value.value as Long)
                "METH_DETERMIN" -> writer.name("id_nomenclature_determination_method").value(it.value.value as Long)
                "DETERMINER" -> writer.name("determiner").value(it.value.value as String?)
                "STATUT_BIO" -> writer.name("id_nomenclature_bio_status").value(it.value.value as Long)
                "NATURALITE" -> writer.name("id_nomenclature_naturalness").value(it.value.value as Long)
                "PREUVE_EXIST" -> writer.name("id_nomenclature_exist_proof").value(it.value.value as Long)
                "COMMENT" -> writer.name("comment").value(it.value.value as String?)
            }
        }

        // GeoNature mapping: counting
        writer.name("cor_counting_occtax")
            .beginArray()
        counting.forEach { c ->
            if (c.isEmpty()) return@forEach

            writer.beginObject()

            c.properties.forEach { p ->
                when (p.key) {
                    "STADE_VIE" -> writer.name("id_nomenclature_life_stage").value(p.value.value as Long)
                    "SEXE" -> writer.name("id_nomenclature_sex").value(p.value.value as Long)
                    "OBJ_DENBR" -> writer.name("id_nomenclature_obj_count").value(p.value.value as Long)
                    "TYP_DENBR" -> writer.name("id_nomenclature_type_count").value(p.value.value as Long)
                }
            }

            writer.name("count_min")
                .value(c.min)
            writer.name("count_max")
                .value(c.max)

            writer.endObject()
        }
        writer.endArray()
    }

    private fun writeInputTaxonCounting(writer: JsonWriter,
                                        counting: List<CountingMetadata>) {
        writer.name("counting")
            .beginArray()

        counting.forEach {
            writeInputTaxonCountingMetadata(writer,
                                            it)
        }

        writer.endArray()
    }

    private fun writeInputTaxonCountingMetadata(writer: JsonWriter,
                                                countingMetadata: CountingMetadata) {
        if (countingMetadata.isEmpty()) return

        writer.beginObject()

        writer.name("index")
            .value(countingMetadata.index)
        countingMetadata.properties.forEach {
            writeInputTaxonPropertyValue(writer,
                                         it.key,
                                         it.value)
        }

        writer.name("min")
            .value(countingMetadata.min)
        writer.name("max")
            .value(countingMetadata.max)

        writer.endObject()
    }

    /**
     * Writes property value as object:
     *
     * ```
     * "property_code": {
     *      "label": "String",
     *      "value": "String"|Long|Int
     * }
     * ```
     */
    private fun writeInputTaxonPropertyValue(writer: JsonWriter,
                                             name: String,
                                             propertyValue: PropertyValue) {
        if (propertyValue.isEmpty()) return

        writer.name(name.toLowerCase(Locale.ROOT))
            .beginObject()

        if (!TextUtils.isEmpty(propertyValue.label)) {
            writer.name("label")
                .value(propertyValue.label)
        }

        if (propertyValue.value != null) {
            when (propertyValue.value) {
                is String -> writer.name("value")
                    .value(propertyValue.value)
                is Long -> writer.name("value")
                    .value(propertyValue.value)
                is Int -> writer.name("value")
                    .value(propertyValue.value)
            }
        }

        writer.endObject()
    }
}