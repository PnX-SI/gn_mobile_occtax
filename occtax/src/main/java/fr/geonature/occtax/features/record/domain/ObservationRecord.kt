package fr.geonature.occtax.features.record.domain

import android.os.Parcelable
import fr.geonature.commons.data.entity.AbstractTaxon
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.locationtech.jts.geom.Geometry
import java.util.Calendar
import java.util.Date
import java.util.SortedMap

/**
 * Describes an observation record.
 *
 * An observation record includes a geographical position and a list of properties that contains all
 * taxa added with their counting.
 *
 * @author S. Grimault
 */
@Parcelize
data class ObservationRecord(

    /**
     * The internal ID of this observation record
     */
    val internalId: Long = generateId(),

    /**
     * The public ID of an existing observation record from _GeoNature_.
     */
    val id: Long? = null,

    /**
     * The geometry of this observation record.
     */
    val geometry: Geometry? = null,

    /**
     * The main properties of this observation record
     */
    val properties: SortedMap<String, PropertyValue> = sortedMapOf(),

    /**
     * The current status of this observation record.
     */
    val status: Status = Status.DRAFT
) : Parcelable {

    init {
        PropertyValue.Text(
            "meta_device_entry",
            "mobile"
        )
            .toPair()
            .also {
                properties[it.first] = it.second
            }
    }

    @IgnoredOnParcel
    val comment = CommentRecord(properties)

    @IgnoredOnParcel
    val dataset = DatasetRecord(properties)

    @IgnoredOnParcel
    val dates = DatesRecord(properties)

    @IgnoredOnParcel
    val feature = FeatureRecord(properties)

    @IgnoredOnParcel
    val module = ModuleRecord(properties)

    @IgnoredOnParcel
    val observers = ObserversRecord(properties)

    @IgnoredOnParcel
    val taxa = TaxaRecord(
        internalId,
        properties
    )

    enum class Status {
        DRAFT,
        TO_SYNC,
        SYNC_IN_PROGRESS,
        SYNC_ERROR,
        SYNC_SUCCESSFUL
    }
}

/**
 * Convenient class to manage the main comment of this observation record.
 *
 * @author S. Grimault
 */
class CommentRecord(private val properties: SortedMap<String, PropertyValue>) {

    /**
     * The main comment to this observation record.
     */
    var comment: String?
        get() = properties[COMMENT_KEY]?.takeIf { it is PropertyValue.Text }
            ?.let { it as PropertyValue.Text }?.value
        set(value) {
            PropertyValue.Text(
                COMMENT_KEY,
                value
            )
                .also {
                    properties[it.code] = it
                }
        }

    companion object {
        const val COMMENT_KEY = "comment"
    }
}

/**
 * Convenient class to manage the current selected dataset of this observation record.
 *
 * @author S. Grimault
 */
class DatasetRecord(private val properties: SortedMap<String, PropertyValue>) {

    /**
     * The current selected dataset of this observation record.
     */
    var datasetId: Long?
        get() = properties[DATASET_ID_KEY].takeIf { it is PropertyValue.Number }
            ?.let { it as PropertyValue.Number }?.value?.toLong()
        set(value) {
            PropertyValue.Number(
                DATASET_ID_KEY,
                value
            )
                .also {
                    properties[it.code] = it
                }
        }

    companion object {
        const val DATASET_ID_KEY = "id_dataset"
    }
}

/**
 * Convenient class to manage the start and end date of this observation record.
 *
 * @author S. Grimault
 */
class DatesRecord(private val properties: SortedMap<String, PropertyValue>) {

    /**
     * The start date of this observation record.
     */
    var start: Date
        get() = properties[DATE_MIN_KEY].takeIf { it is PropertyValue.Date }
            ?.let { it as PropertyValue.Date }?.value ?: Date()
        set(value) {
            PropertyValue.Date(
                DATE_MIN_KEY,
                value
            )
                .also {
                    properties[it.code] = it
                }

            if (end.before(value) || properties[DATE_MAX_KEY] == null) {
                end = value
            }
        }

    /**
     * The end date of this observation record.
     */
    var end: Date
        get() = properties[DATE_MAX_KEY].takeIf { it is PropertyValue.Date }
            ?.let { it as PropertyValue.Date }?.value ?: start
        set(value) {
            PropertyValue.Date(
                DATE_MAX_KEY,
                value
            )
                .also {
                    properties[it.code] = it
                }

            if (start.after(value) || properties[DATE_MIN_KEY] == null) {
                start = value
            }
        }

    companion object {
        const val DATE_MIN_KEY = "date_min"
        const val DATE_MAX_KEY = "date_max"
    }
}

/**
 * Convenient class to set the selected feature of this observation record.
 *
 * @author S. Grimault
 */
class FeatureRecord(private val properties: SortedMap<String, PropertyValue>) {

    /**
     * The current selected feature ID of this observation record.
     */
    var id: String?
        get() = properties[FEATURE_ID_KEY].takeIf { it is PropertyValue.Text }
            ?.let { it as PropertyValue.Text }?.value
        set(value) {
            PropertyValue.Text(
                FEATURE_ID_KEY,
                value
            )
                .also {
                    properties[it.code] = it
                }
        }

    companion object {
        const val FEATURE_ID_KEY = "feature_id"
    }
}

/**
 * Convenient class to set the module name of this observation record.
 *
 * @author S. Grimault
 */
class ModuleRecord(private val properties: SortedMap<String, PropertyValue>) {

    /**
     * The module name of this observation record.
     */
    var module: String?
        get() = properties[MODULE_KEY].takeIf { it is PropertyValue.Text }
            ?.let { it as PropertyValue.Text }?.value
        set(value) {
            PropertyValue.Text(
                MODULE_KEY,
                value
            )
                .also {
                    properties[it.code] = it
                }
        }

    companion object {
        const val MODULE_KEY = "module"
    }
}

/**
 * Convenient class to manage observers of this observation record.
 *
 * @author S. Grimault
 */
class ObserversRecord(private val properties: SortedMap<String, PropertyValue>) {

    /**
     * Gets the primary observer of this observation record.
     */
    fun getPrimaryObserverId(): Long? {
        return getAllObserverIds().firstOrNull()
    }

    /**
     * Gets all observers (i.e. the primary input observer at first position, then others) of this
     * observation record.
     */
    fun getAllObserverIds(): Set<Long> {
        return properties[OBSERVERS_KEY]
            ?.takeIf { it is PropertyValue.NumberArray }
            ?.let { it as PropertyValue.NumberArray }
            ?.value?.map { it.toLong() }
            ?.toSet() ?: emptySet()
    }

    /**
     * Sets the primary observer ID of this observation record.
     */
    fun setPrimaryObserverId(id: Long) {
        PropertyValue.Number(
            DIGITISER_KEY,
            id
        )
            .also {
                properties[it.code] = it
            }
        PropertyValue.NumberArray(
            OBSERVERS_KEY,
            getAllObserverIds().toMutableList()
                .also {
                    it.add(
                        0,
                        id
                    )
                }
                .toTypedArray()
        )
            .also {
                properties[it.code] = it
            }
    }

    /**
     * Adds observer ID to this observation record.
     */
    fun addObserverId(id: Long) {
        if (getAllObserverIds().isEmpty()) {
            PropertyValue.Number(
                DIGITISER_KEY,
                id
            )
                .also {
                    properties[it.code] = it
                }
        }

        PropertyValue.NumberArray(
            OBSERVERS_KEY,
            getAllObserverIds().toMutableList()
                .also {
                    it.add(id)
                }
                .toTypedArray()
        )
            .also {
                properties[it.code] = it
            }
    }

    /**
     * Clears all added observers.
     */
    fun clearAll() {
        properties.remove(DIGITISER_KEY)
        properties.remove(OBSERVERS_KEY)
    }

    companion object {
        const val DIGITISER_KEY = "id_digitiser"
        const val OBSERVERS_KEY = "observers"
    }
}

/**
 * Convenient class to manage all taxa of this observation record.
 *
 * @author S. Grimault
 */
class TaxaRecord(
    private val recordId: Long,
    private val properties: SortedMap<String, PropertyValue>
) {

    /**
     * All taxa of this observation record.
     */
    var taxa: List<TaxonRecord>
        get() = properties[TAXA_KEY]?.takeIf { it is PropertyValue.Taxa }
            ?.let { it as PropertyValue.Taxa }?.value
            ?.toList() ?: emptyList()
        set(value) {
            PropertyValue.Taxa(
                TAXA_KEY,
                value.distinctBy { it.taxon.id }
                    .toTypedArray()
            )
                .also {
                    properties[it.code] = it
                }
            selectedTaxonRecord = null
        }

    /**
     * The current selected taxon record.
     */
    var selectedTaxonRecord: TaxonRecord?
        get() = properties[CURRENT_TAXON_ID]?.takeIf { it is PropertyValue.Number }
            ?.let { it as PropertyValue.Number }?.value?.let {
                properties[TAXA_KEY]?.takeIf { it is PropertyValue.Taxa }
                    ?.let { it as PropertyValue.Taxa }?.value?.firstOrNull { taxonRecord ->
                        taxonRecord.taxon.id == it
                    }
            }
        set(value) {
            PropertyValue.Number(
                CURRENT_TAXON_ID,
                value?.taxon?.id
            )
                .also {
                    if (it.isEmpty()) properties.remove(CURRENT_TAXON_ID)
                    else properties[it.code] = it
                }
        }

    /**
     * Adds a taxon record from given [AbstractTaxon].
     */
    fun add(taxon: AbstractTaxon): TaxonRecord {
        return TaxonRecord(
            recordId = recordId,
            taxon = taxon
        ).also {
            taxa = taxa.filterNot { t -> it.taxon.id == t.taxon.id } + listOf(it)
            selectedTaxonRecord = it
        }
    }

    /**
     * Adds or updates a given [TaxonRecord].
     */
    fun addOrUpdate(taxonRecord: TaxonRecord): TaxonRecord {
        return taxonRecord.copy(recordId = recordId)
            .also {
                taxa = taxa.filterNot { t -> it.taxon.id == t.taxon.id } + listOf(it)
                selectedTaxonRecord = it
            }
    }

    /**
     * Deletes an exiting [TaxonRecord].
     *
     * @return the deleted [TaxonRecord], `null` otherwise
     */
    fun delete(taxonId: Long): TaxonRecord? {
        val taxonRecordToDelete = taxa.find { it.taxon.id == taxonId }
        taxa = taxa.filterNot { it.taxon.id == taxonId }

        if (selectedTaxonRecord?.taxon?.id == taxonId) {
            selectedTaxonRecord = null
        }

        return taxonRecordToDelete
    }

    companion object {
        const val TAXA_KEY = "t_occurrences_occtax"
        const val CURRENT_TAXON_ID = "current_taxon_id"
    }
}

/**
 * Generates a pseudo unique ID. The value is the number of seconds since Jan. 1, 2016, midnight.
 *
 * @return an unique ID
 */
private fun generateId(): Long {
    val now = Calendar.getInstance()
        .apply {
            set(
                Calendar.MILLISECOND,
                0
            )
        }

    val start = Calendar.getInstance()
        .apply {
            set(
                2016,
                Calendar.JANUARY,
                1,
                0,
                0,
                0
            )
            set(
                Calendar.MILLISECOND,
                0
            )
        }

    return (now.timeInMillis - start.timeInMillis) / 1000
}