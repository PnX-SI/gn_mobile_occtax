package fr.geonature.occtax.features.record.io

import android.util.JsonWriter
import fr.geonature.commons.util.format
import fr.geonature.commons.util.toIsoDateString
import fr.geonature.maps.jts.geojson.io.GeoJsonWriter
import fr.geonature.occtax.features.record.domain.DatesRecord
import fr.geonature.occtax.features.record.domain.ObservationRecord
import fr.geonature.occtax.features.record.domain.PropertyValue
import fr.geonature.occtax.features.settings.domain.AppSettings
import fr.geonature.occtax.features.settings.domain.InputDateSettings
import java.io.IOException
import java.io.StringWriter
import java.io.Writer
import java.util.TimeZone

/**
 * Default `JsonWriter` about writing an [ObservationRecord] as `JSON`.
 *
 * @author S. Grimault
 *
 * @see ObservationRecordDefaultJsonReader
 * @see TaxonRecordDefaultJsonWriter
 */
class ObservationRecordDefaultJsonWriter {

    private val taxonRecordDefaultJsonWriter: TaxonRecordDefaultJsonWriter =
        TaxonRecordDefaultJsonWriter()
    private var indent: String = ""

    /**
     * Sets the indentation string to be repeated for each level of indentation in the encoded
     * document. If `indent.isEmpty()` the encoded document will be compact. Otherwise the encoded
     * document will be more human-readable.
     *
     * @param indent a string containing only whitespace.
     *
     * @return [ObservationRecordDefaultJsonWriter] fluent interface
     */
    fun setIndent(indent: String): ObservationRecordDefaultJsonWriter {
        this.indent = indent

        return this
    }

    /**
     * Convert the given [ObservationRecord] as `JSON` string.
     *
     * @param observationRecord the [ObservationRecord] to convert
     *
     * @return a `JSON` string representation of the given [ObservationRecord]
     * @throws IOException if something goes wrong
     * @see [write][fr.geonature.occtax.features.record.io.ObservationRecordJsonWriter.write(java.io.Writer, fr.geonature.occtax.features.record.domain.ObservationRecord, fr.geonature.occtax.settings.AppSettings)]
     */
    fun write(observationRecord: ObservationRecord): String {
        val writer = StringWriter()

        write(
            writer,
            observationRecord
        )

        return writer.toString()
    }

    /**
     * Convert the given [ObservationRecord] as `JSON` and write it to the given `Writer`.
     *
     * @param out the `Writer` to use
     * @param observationRecord the [ObservationRecord] to convert
     *
     * @throws IOException if something goes wrong
     */
    fun write(
        out: Writer,
        observationRecord: ObservationRecord
    ) {
        val writer = JsonWriter(out)
        writer.setIndent(this.indent)
        writeObservationRecord(
            writer,
            observationRecord
        )
        writer.flush()
        writer.close()
    }

    private fun writeObservationRecord(
        writer: JsonWriter,
        observationRecord: ObservationRecord
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
            observationRecord
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
        observationRecord: ObservationRecord
    ) {
        writer.name("properties")
            .beginObject()

        writer.name("internal_id")
            .value(observationRecord.internalId)

        writeDates(
            writer,
            observationRecord
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

                is PropertyValue.Dataset -> writer.name(propertyValue.code)
                    .value(propertyValue.value?.id)

                is PropertyValue.Nomenclature -> {
                    // GeoNature default properties mapping
                    when (propertyValue.code) {
                        "TYP_GRP" -> writer.name("id_nomenclature_grp_typ")
                            .value(propertyValue.value)
                    }
                }

                is PropertyValue.Observers -> {
                    writer.name(propertyValue.code)
                        .beginArray()
                    propertyValue.value.forEach { value -> writer.value(value.id) }
                    writer.endArray()
                }

                is PropertyValue.AdditionalFields -> {
                    writer.name(propertyValue.code)
                    writeAdditionalFields(
                        writer,
                        propertyValue
                    )
                }

                is PropertyValue.Taxa -> {
                    writer.name(propertyValue.code)
                        .beginArray()
                    propertyValue.value.forEach { taxonRecord ->
                        taxonRecordDefaultJsonWriter.writeTaxonRecord(
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
        observationRecord: ObservationRecord
    ) {
        writer.name(DatesRecord.DATE_MIN_KEY)
            .value(observationRecord.dates.start.toIsoDateString())
        writer.name(DatesRecord.DATE_MAX_KEY)
            .value(observationRecord.dates.end.toIsoDateString())
        writer.name(DatesRecord.DATE_LAST_MODIFIED)
            .value(observationRecord.dates.lastModified.toIsoDateString())
    }

    private fun writeAdditionalFields(
        writer: JsonWriter,
        additionalFields: PropertyValue.AdditionalFields
    ) {
        writer.beginObject()

        additionalFields.value.values.forEach {
            when (it) {
                is PropertyValue.Date -> writer.name(it.code)
                    .value(it.value?.format("yyyy-MM-dd"))

                is PropertyValue.Nomenclature -> writer.name(it.code)
                    .value(it.value)

                is PropertyValue.Number -> writer.name(it.code)
                    .value(it.value)

                is PropertyValue.NumberArray -> {
                    writer.name(it.code)
                        .beginArray()
                    it.value.forEach { value -> writer.value(value) }
                    writer.endArray()
                }

                is PropertyValue.StringArray -> {
                    writer.name(it.code)
                        .beginArray()
                    it.value.forEach { value -> writer.value(value) }
                    writer.endArray()
                }

                is PropertyValue.Text -> writer.name(it.code)
                    .value(it.value)

                is PropertyValue.Time -> writer.name(it.code)
                    .value(it.toTimeString())

                else -> {}
            }
        }

        writer.endObject()
    }
}