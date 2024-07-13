package fr.geonature.occtax.features.record.io

import android.util.JsonWriter
import fr.geonature.commons.util.format
import fr.geonature.datasync.api.model.Media
import fr.geonature.occtax.features.record.domain.CountingRecord
import fr.geonature.occtax.features.record.domain.MediaRecord
import fr.geonature.occtax.features.record.domain.PropertyValue
import fr.geonature.occtax.features.record.domain.TaxonRecord
import fr.geonature.occtax.features.settings.domain.AppSettings
import java.io.IOException
import java.io.StringWriter
import java.io.Writer
import java.util.TimeZone

/**
 * Default `JsonWriter` about writing an [TaxonRecord] as `JSON`.
 *
 * @author S. Grimault
 *
 * @see TaxonRecordJsonReader
 */
class TaxonRecordJsonWriter {

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
    fun setIndent(indent: String): TaxonRecordJsonWriter {
        this.indent = indent

        return this
    }

    /**
     * Convert the given [TaxonRecord] as `JSON` string.
     *
     * @param taxonRecord the [TaxonRecord] to convert
     * @param settings additional settings
     *
     * @return a `JSON` string representation of the given [TaxonRecord]
     * @throws IOException if something goes wrong
     * @see [write][fr.geonature.occtax.features.record.io.TaxonRecordJsonWriter.write(java.io.Writer, fr.geonature.occtax.features.record.domain.TaxonRecord)]
     */
    fun write(taxonRecord: TaxonRecord, settings: AppSettings? = null): String {
        val writer = StringWriter()

        write(
            writer,
            taxonRecord,
            settings
        )

        return writer.toString()
    }

    /**
     * Convert the given [TaxonRecord] as `JSON` and write it to the given `Writer`.
     *
     * @param out the `Writer` to use
     * @param taxonRecord the [TaxonRecord] to convert
     * @param settings additional settings
     *
     * @throws IOException if something goes wrong
     */
    fun write(
        out: Writer,
        taxonRecord: TaxonRecord,
        settings: AppSettings? = null
    ) {
        val writer = JsonWriter(out)
        writer.setIndent(this.indent)
        writeTaxonRecord(
            writer,
            taxonRecord,
            settings
        )
        writer.flush()
        writer.close()
    }

    internal fun writeTaxonRecord(
        writer: JsonWriter,
        taxonRecord: TaxonRecord,
        settings: AppSettings? = null
    ) {
        writer.beginObject()

        writer.name("cd_nom")
            .value(taxonRecord.taxon.id)
        writer.name("nom_cite")
            .value(taxonRecord.taxon.name)

        if (settings == null) {
            writer.name("regne")
                .value(taxonRecord.taxon.taxonomy.kingdom)
            writer.name("group2_inpn")
                .value(taxonRecord.taxon.taxonomy.group)
        }

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

                is PropertyValue.AdditionalFields -> {
                    writer.name(propertyValue.code)
                    writeAdditionalFields(
                        writer,
                        propertyValue
                    )
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

                is PropertyValue.AdditionalFields -> {
                    writer.name(propertyValue.code)
                    writeAdditionalFields(
                        writer,
                        propertyValue
                    )
                }

                is PropertyValue.Media -> {
                    writer.name(propertyValue.code)
                        .beginArray()
                    propertyValue.value.filterIsInstance<MediaRecord.Media>()
                        .map { mediaRecord -> mediaRecord.media }
                        .forEach { media ->
                            writeMedia(
                                writer,
                                media
                            )
                        }
                    writer.endArray()
                }

                else -> {}
            }
        }

        writer.endObject()
    }

    private fun writeMedia(writer: JsonWriter, media: Media) {
        writer.beginObject()

        writer.name("id_media")
            .value(media.id)
        writer.name("unique_id_media")
            .value(media.uuid.toString())
        writer.name("id_nomenclature_media_type")
            .value(media.mediaType)
        writer.name("id_table_location")
            .value(media.idTableLocation)
        writer.name("media_path")
            .value(media.path)
        writer.name("author")
            .value(media.author)
        writer.name("title_en")
            .value(media.titleEn)
        writer.name("title_fr")
            .value(media.titleFr)
        writer.name("description_en")
            .value(media.descriptionEn)
        writer.name("meta_create_date")
            .value(
                media.createdAt.format(
                    "yyyy-MM-dd HH:mm:ss",
                    TimeZone.getDefault()
                )
            )
        writer.name("meta_update_date")
            .value(
                media.updatedAt.format(
                    "yyyy-MM-dd HH:mm:ss",
                    TimeZone.getDefault()
                )
            )

        writer.endObject()
    }

    private fun writeAdditionalFields(
        writer: JsonWriter,
        additionalFields: PropertyValue.AdditionalFields
    ) {
        writer.beginObject()

        additionalFields.value.values.forEach {
            when (it) {
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

                else -> {}
            }
        }

        writer.endObject()
    }
}