package fr.geonature.occtax.features.nomenclature.error

import fr.geonature.commons.data.entity.Nomenclature
import fr.geonature.commons.data.entity.NomenclatureType
import fr.geonature.commons.error.Failure

/**
 * Failure about no [NomenclatureType] found locally.
 *
 * @author S. Grimault
 */
object NoNomenclatureTypeFoundLocallyFailure : Failure.FeatureFailure()

/**
 * Failure about no [Nomenclature] found from given mnemonic.
 *
 * @author S. Grimault
 */
data class NoNomenclatureValuesFoundFailure(val mnemonic: String) : Failure.FeatureFailure()