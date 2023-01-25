package fr.geonature.occtax.features.record.domain

import android.os.Parcelable
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
