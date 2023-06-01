package fr.geonature.occtax.features.nomenclature.domain

/**
 * _Occtax_ additional field types.
 *
 * @author S. Grimault
 */
enum class AdditionalFieldType(val type: String) {

    /**
     * Default type.
     */
    DEFAULT("OCCTAX_RELEVE"),

    /**
     * Additional field type used for main information.
     */
    INFORMATION("OCCTAX_OCCURENCE"),

    /**
     * Additional field type used for describing counting.
     */
    COUNTING("OCCTAX_DENOMBREMENT")
}