package fr.geonature.occtax.features.nomenclature.error

import fr.geonature.commons.error.Failure
import fr.geonature.occtax.input.PropertyValue

/**
 * Failure about [PropertyValue].
 *
 * @author S. Grimault
 */
data class PropertyValueFailure(val code: String) : Failure.FeatureFailure()
