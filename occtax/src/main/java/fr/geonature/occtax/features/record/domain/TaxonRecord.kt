package fr.geonature.occtax.features.record.domain

import android.content.Context
import android.os.Parcelable
import fr.geonature.commons.data.entity.AbstractTaxon
import fr.geonature.commons.util.getInputsFolder
import fr.geonature.mountpoint.util.FileUtils
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.io.File
import java.util.SortedMap

/**
 * Describes a [taxon][fr.geonature.commons.data.entity.AbstractTaxon] record.
 *
 * @author S. Grimault
 */
@Parcelize
data class TaxonRecord(

    /**
     * The observation record ID linked to this taxon record.
     */
    val recordId: Long,

    /**
     * The public ID of an existing taxon record from _GeoNature_.
     */
    val id: Long? = null,

    /**
     * The selected taxon of this record.
     */
    val taxon: AbstractTaxon,

    /**
     * The main properties of this taxon record
     */
    val properties: SortedMap<String, PropertyValue> = sortedMapOf()
) : Parcelable {

    @IgnoredOnParcel
    val counting = AllCountingRecord(
        recordId,
        taxon.id,
        properties
    )
}

/**
 * Convenient class to manage all counting of this taxon record.
 *
 * @author S. Grimault
 */
class AllCountingRecord(
    private val recordId: Long,
    private val taxonId: Long,
    private val properties: SortedMap<String, PropertyValue>
) {

    /**
     * All counting of this taxon record.
     */
    var counting: List<CountingRecord>
        get() = properties[COUNTING_KEY]?.takeIf { it is PropertyValue.Counting }
            ?.let { it as PropertyValue.Counting }?.value
            ?.toList()
            ?.distinctBy { it.index }
            ?.sortedBy { it.index } ?: emptyList()
        set(value) {
            PropertyValue.Counting(
                COUNTING_KEY,
                value.distinctBy { it.index }
                    .sortedBy { it.index }
                    .toTypedArray()
            )
                .also {
                    properties[it.code] = it
                }
        }

    /**
     * The media base path.
     */
    val mediaBasePath: (context: Context, countingRecord: CountingRecord) -> File =
        { context, countingRecord ->
            FileUtils.getFile(
                FileUtils.getInputsFolder(context),
                "$recordId",
                "taxon",
                "$taxonId",
                "counting",
                "${countingRecord.index}"
            )
        }

    fun create(): CountingRecord {
        val index = counting
            .maxOfOrNull { it.index }
            ?.plus(1) ?: 1

        return CountingRecord(index = index)
    }

    fun addOrUpdate(counting: CountingRecord): CountingRecord? {
        if (counting.isEmpty()) return null

        val existingCounting = this.counting
        val index = if (counting.index > 0) counting.index
        else existingCounting
            .maxOfOrNull { it.index }
            ?.plus(1) ?: 1

        return counting.copy(index = index)
            .also { countingToUpdate ->
                this.counting =
                    (existingCounting.filterNot { counting -> counting.index == countingToUpdate.index } + listOf(countingToUpdate)).sortedBy { it.index }
            }
    }

    fun delete(context: Context, index: Int): CountingRecord? {
        val existingCounting = counting

        this.counting = existingCounting.filterNot { it.index == index }
            .sortedBy { it.index }

        return existingCounting.firstOrNull { it.index == index }
            ?.also {
                mediaBasePath(
                    context,
                    it
                ).deleteRecursively()
            }
    }

    companion object {
        const val COUNTING_KEY = "cor_counting_occtax"
    }
}
