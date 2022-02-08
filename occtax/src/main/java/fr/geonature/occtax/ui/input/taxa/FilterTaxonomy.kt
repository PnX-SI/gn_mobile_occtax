package fr.geonature.occtax.ui.input.taxa

import fr.geonature.commons.data.entity.Taxonomy

/**
 * [Taxonomy] filter.
 *
 * @author S. Grimault
 */
class FilterTaxonomy(value: Taxonomy) : Filter<Taxonomy>(
    FilterType.TAXONOMY,
    value
)