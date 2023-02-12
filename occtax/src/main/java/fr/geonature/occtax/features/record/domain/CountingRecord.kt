package fr.geonature.occtax.features.record.domain

import android.os.Parcelable
import fr.geonature.datasync.api.model.Media
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.util.SortedMap

/**
 * Describes a counting of a taxon record.
 *
 * @author S. Grimault
 */
@Parcelize
data class CountingRecord(

    /**
     * The current index of this taxon counting record.
     */
    val index: Int = 0,

    /**
     * The main properties of this taxon counting record.
     */
    val properties: SortedMap<String, PropertyValue> = sortedMapOf()
) : Parcelable {

    /**
     * The min value of this taxon counting record.
     */
    @IgnoredOnParcel
    var min: Int
        get() = properties[MIN_KEY].takeIf { it is PropertyValue.Number }
            ?.let { it as PropertyValue.Number }?.value?.toInt() ?: 0
        set(value) {
            PropertyValue.Number(
                MIN_KEY,
                value
            )
                .also {
                    properties[it.code] = it
                }

            if (max < value || properties[MAX_KEY] == null) {
                max = value
            }
        }

    /**
     * The max value of this taxon counting record.
     */
    @IgnoredOnParcel
    var max: Int
        get() = properties[MAX_KEY].takeIf { it is PropertyValue.Number }
            ?.let { it as PropertyValue.Number }?.value?.toInt() ?: 0
        set(value) {
            PropertyValue.Number(
                MAX_KEY,
                value
            )
                .also {
                    properties[it.code] = it
                }

            if (min > value || properties[MIN_KEY] == null) {
                min = value
            }
        }

    /**
     * All [MediaRecord] added to this taxon counting record.
     */
    @IgnoredOnParcel
    val medias = AllMediaRecord(properties)

    /**
     * Whether this counting is considered empty or not.
     */
    @IgnoredOnParcel
    val isEmpty: () -> Boolean = {
        properties.all { it.value.isEmpty() }
    }

    companion object {
        const val MIN_KEY = "count_min"
        const val MAX_KEY = "count_max"
    }
}

/**
 * Convenient class to manage all medias of a [CountingRecord].
 *
 * @author S. Grimault
 */
class AllMediaRecord(private val properties: SortedMap<String, PropertyValue>) {

    /**
     * All [MediaRecord.Media] added to this taxon counting record.
     */
    var medias: List<Media>
        get() = properties[MEDIAS_KEY]?.takeIf { it is PropertyValue.Media }
            ?.let { it as PropertyValue.Media }?.value?.filterIsInstance<MediaRecord.Media>()
            ?.map { it.media }
            ?.toList() ?: emptyList()
        set(value) {
            PropertyValue.Media(
                MEDIAS_KEY,
                value.distinctBy { it.id }
                    .map { MediaRecord.Media(it) }
                    .toTypedArray()
            )
                .also {
                    properties[it.code] = it
                }
        }

    /**
     * All [MediaRecord.File] added to this taxon counting record.
     */
    var files: List<String>
        get() = properties[MEDIAS_KEY]?.takeIf { it is PropertyValue.Media }
            ?.let { it as PropertyValue.Media }?.value?.filterIsInstance<MediaRecord.File>()
            ?.map { it.path }
            ?.toList() ?: emptyList()
        set(value) {
            PropertyValue.Media(
                MEDIAS_KEY,
                value.distinct()
                    .map { MediaRecord.File(it) }
                    .toTypedArray()
            )
                .also {
                    properties[it.code] = it
                }
        }

    /**
     * Adds given local file as media.
     */
    fun addFile(path: String) {
        files = files + listOf(path)
    }

    companion object {
        const val MEDIAS_KEY = "medias"
    }
}

/**
 * Describes a media record, either as [Media] or as local file.
 *
 * @author S. Grimault
 */
sealed class MediaRecord : Parcelable {

    /**
     * As [Media] (i.e. already synchronized media from _GeoNature_).
     */
    @Parcelize
    data class Media(val media: fr.geonature.datasync.api.model.Media) : MediaRecord()

    /**
     * As local file.
     */
    @Parcelize
    data class File(val path: String) : MediaRecord()
}
