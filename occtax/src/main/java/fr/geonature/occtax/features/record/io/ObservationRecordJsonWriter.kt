package fr.geonature.occtax.features.record.io

import android.util.JsonWriter
import fr.geonature.commons.util.format
import fr.geonature.commons.util.toIsoDateString
import fr.geonature.maps.jts.geojson.io.GeoJsonWriter
import fr.geonature.occtax.features.record.domain.CountingRecord
import fr.geonature.occtax.features.record.domain.DatesRecord
import fr.geonature.occtax.features.record.domain.ObservationRecord
import fr.geonature.occtax.features.record.domain.PropertyValue
import fr.geonature.occtax.features.record.domain.TaxonRecord
import fr.geonature.occtax.settings.AppSettings
import fr.geonature.occtax.settings.InputDateSettings
import java.io.IOException
import java.io.StringWriter
import java.io.Writer
import java.util.TimeZone

/**
 * Default `JsonWriter` about writing an [ObservationRecord] as `JSON`.
 *
 * @author S. Grimault
 *
 * @see ObservationRecordJsonReader
 */
class ObservationRecordJsonWriter {

    private var indent: String = ""

    /**
     * Sets the indentation string to be repeated for each level of indentation in the encoded document.
     * If `indent.isEmpty()` the encoded document will be compact.
     * Otherwise the encoded document will be more human-readable.
     *
     * @param indent a string containing only whitespace.
     *
     * @return [ObservationRecordJsonWriter] fluent interface
     */
    fun setIndent(indent: String): ObservationRecordJsonWriter {
        this.indent = indent

        return this
    }

    /**
     * Convert the given [ObservationRecord] as `JSON` string.
     *
     * @param observationRecord the [ObservationRecord] to convert
     * @param settings additional settings
     *
     * @return a `JSON` string representation of the given [ObservationRecord]
     * @throws IOException if something goes wrong
     * @see [write][fr.geonature.occtax.features.record.io.ObservationRecordJsonWriter.write(java.io.Writer, fr.geonature.occtax.features.record.domain.ObservationRecord, fr.geonature.occtax.settings.AppSettings)]
     */
    fun write(
        observationRecord: ObservationRecord,
        settings: AppSettings? = null
    ): String {
        val writer = StringWriter()

        write(
            writer,
            observationRecord,
            settings
        )

        return writer.toString()
    }

    /**
     * Convert the given [ObservationRecord] as `JSON` and write it to the given `Writer`.
     *
     * @param out the `Writer` to use
     * @param observationRecord the [ObservationRecord] to convert
     * @throws IOException if something goes wrong
     */
    fun write(
        out: Writer,
        observationRecord: ObservationRecord,
        settings: AppSettings? = null
    ) {
        val writer = JsonWriter(out)
        writer.setIndent(this.indent)
        writeObservationRecord(
            writer,
            observationRecord,
            settings
        )
        writer.flush()
        writer.close()
    }

    private fun writeObservationRecord(
        writer: JsonWriter,
        observationRecord: ObservationRecord,
        settings: AppSettings? = null
    ) {
        writer.beginObject()

        writer.name("id")
            .value(observationRecord.id ?: observationRecord.internalId)
        writer.name("status")
            .value(observationRecord.status.name.lowercase())

        writeGeometry(
            writer,
            observationRecord
        )
        writeProperties(
            writer,
            observationRecord,
            settings
        )

        writer.endObject()
    }

    private fun writeGeometry(
        writer: JsonWriter,
        observationRecord: ObservationRecord,
    ) {
        writer.name("geometry")

        val geometry = observationRecord.geometry

        if (geometry == null) {
            writer.nullValue()
        } else {
            GeoJsonWriter().writeGeometry(
                writer,
                geometry
            )
        }
    }

    private fun writeProperties(
        writer: JsonWriter,
        observationRecord: ObservationRecord,
        settings: AppSettings? = null
    ) {
        writer.name("properties")
            .beginObject()
        writer.name("internal_id")
            .value(observationRecord.internalId)

        writeDates(
            writer,
            observationRecord,
            settings?.inputSettings?.dateSettings
        )

        observationRecord.properties.forEach {
            when (val propertyValue = it.value) {
                is PropertyValue.Text -> writer.name(propertyValue.code)
                    .value(propertyValue.value)
                is PropertyValue.Number -> writer.name(propertyValue.code)
                    .value(propertyValue.value)
                is PropertyValue.NumberArray -> {
                    writer.name(propertyValue.code)
                        .beginArray()
                    propertyValue.value.forEach { value -> writer.value(value) }
                    writer.endArray()
                }
                is PropertyValue.Nomenclature -> {
                    // GeoNature default properties mapping
                    when (propertyValue.code) {
                        "TYP_GRP" -> writer.name("id_nomenclature_grp_typ")
                            .value(propertyValue.value)
                    }
                }
                is PropertyValue.Taxa -> {
                    writer.name(propertyValue.code)
                        .beginArray()
                    propertyValue.value.forEach { taxonRecord ->
                        writeTaxon(
                            writer,
                            taxonRecord
                        )
                    }
                    writer.endArray()
                }
                else -> {}
            }
        }

        writer.endObject()
    }

    private fun writeDates(
        writer: JsonWriter,
        observationRecord: ObservationRecord,
        dateSettings: InputDateSettings? = null
    ) {
        observationRecord.dates.start.run {
            writer.name(DatesRecord.DATE_MIN_KEY)
                .value(
                    if (dateSettings == null) toIsoDateString() else format(
                        "yyyy-MM-dd",
                        TimeZone.getDefault()
                    )
                )
            writer.name("hour_min")
                .value(
                    if (dateSettings?.startDateSettings == InputDateSettings.DateSettings.DATETIME) format(
                        "HH:mm",
                        TimeZone.getDefault()
                    )
                    else null
                )
        }

        observationRecord.dates.end.run {
            writer.name(DatesRecord.DATE_MAX_KEY)
                .value(
                    if (dateSettings == null) toIsoDateString()
                    else if (dateSettings.endDateSettings != null) format(
                        "yyyy-MM-dd",
                        TimeZone.getDefault()
                    ) else observationRecord.dates.start.format(
                        "yyyy-MM-dd",
                        TimeZone.getDefault()
                    )
                )
            writer.name("hour_max")
                .value(
                    if (dateSettings?.endDateSettings == InputDateSettings.DateSettings.DATETIME) format(
                        "HH:mm",
                        TimeZone.getDefault()
                    )
                    else if (dateSettings?.startDateSettings == InputDateSettings.DateSettings.DATETIME) observationRecord.dates.start.format(
                        "HH:mm",
                        TimeZone.getDefault()
                    )
                    else null
                )
        }
    }

    private fun writeTaxon(writer: JsonWriter, taxonRecord: TaxonRecord) {
        writer.beginObject()

        writer.name("cd_nom")
            .value(taxonRecord.taxon.id)
        writer.name("nom_cite")
            .value(taxonRecord.taxon.name)
        writer.name("regne")
            .value(taxonRecord.taxon.taxonomy.kingdom)
        writer.name("group2_inpn")
            .value(taxonRecord.taxon.taxonomy.group)

        taxonRecord.properties.forEach {
            when (val propertyValue = it.value) {
                is PropertyValue.Text -> writer.name(propertyValue.code)
                    .value(propertyValue.value)
                is PropertyValue.Number -> writer.name(propertyValue.code)
                    .value(propertyValue.value)
                is PropertyValue.NumberArray -> {
                    writer.name(propertyValue.code)
                        .beginArray()
                    propertyValue.value.forEach { value -> writer.value(value) }
                    writer.endArray()
                }
                is PropertyValue.Nomenclature -> {
                    // GeoNature mapping: taxon
                    when (propertyValue.code) {
                        "ETA_BIO" -> writer.name("id_nomenclature_bio_condition")
                            .value(propertyValue.value)
                        "METH_DETERMIN" -> writer.name("id_nomenclature_determination_method")
                            .value(propertyValue.value)
                        "METH_OBS" -> writer.name("id_nomenclature_obs_technique")
                            .value(propertyValue.value)
                        "NATURALITE" -> writer.name("id_nomenclature_naturalness")
                            .value(propertyValue.value)
                        "OCC_COMPORTEMENT" -> writer.name("id_nomenclature_behaviour")
                            .value(propertyValue.value)
                        "PREUVE_EXIST" -> writer.name("id_nomenclature_exist_proof")
                            .value(propertyValue.value)
                        "STATUT_BIO" -> writer.name("id_nomenclature_bio_status")
                            .value(propertyValue.value)
                    }
                }
                is PropertyValue.Counting -> {
                    writer.name(propertyValue.code)
                        .beginArray()
                    propertyValue.value.forEach { counting ->
                        writeCounting(
                            writer,
                            counting
                        )
                    }
                    writer.endArray()
                }
                else -> {}
            }
        }

        writer.endObject()
    }

    private fun writeCounting(writer: JsonWriter, countingRecord: CountingRecord) {
        writer.beginObject()

        countingRecord.properties.forEach {
            when (val propertyValue = it.value) {
                is PropertyValue.Text -> writer.name(propertyValue.code)
                    .value(propertyValue.value)
                is PropertyValue.Number -> writer.name(propertyValue.code)
                    .value(propertyValue.value)
                is PropertyValue.NumberArray -> {
                    writer.name(propertyValue.code)
                        .beginArray()
                    propertyValue.value.forEach { value -> writer.value(value) }
                    writer.endArray()
                }
                is PropertyValue.Nomenclature -> {
                    // GeoNature mapping: counting
                    when (propertyValue.code) {
                        "OBJ_DENBR" -> writer.name("id_nomenclature_obj_count")
                            .value(propertyValue.value)
                        "SEXE" -> writer.name("id_nomenclature_sex")
                            .value(propertyValue.value)
                        "STADE_VIE" -> writer.name("id_nomenclature_life_stage")
                            .value(propertyValue.value)
                        "TYP_DENBR" -> writer.name("id_nomenclature_type_count")
                            .value(propertyValue.value)
                    }
                }
                else -> {}
            }
        }

        writer.endObject()
    }
}