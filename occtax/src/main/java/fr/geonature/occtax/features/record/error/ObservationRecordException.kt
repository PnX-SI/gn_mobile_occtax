package fr.geonature.occtax.features.record.error

import fr.geonature.occtax.features.record.domain.ObservationRecord

/**
 * Base exception about [ObservationRecord].
 *
 * @author S. Grimault
 */
sealed class ObservationRecordException(
    message: String? = null,
    cause: Throwable? = null
) : RuntimeException(
    message,
    cause
) {

    /**
     * Thrown if no [ObservationRecord] was found locally from a given ID.
     */
    data class NotFoundException(val id: Long) :
        ObservationRecordException("no observation record found with ID '$id'")

    /**
     * Thrown if something goes wrong while reading [ObservationRecord] from its given ID.
     */
    data class ReadException(val id: Long) :
        ObservationRecordException("I/O Exception while reading observation record with ID '$id'")

    /**
     * Thrown if something goes wrong while writing [ObservationRecord] from its given ID.
     */
    data class WriteException(val id: Long) :
        ObservationRecordException("I/O Exception while writing observation record with ID '$id'")

    data class NoDefaultNomenclatureValuesFoundException(val id: Long) :
        ObservationRecordException("no default nomenclature values found for observation record with ID '$id'")
}
