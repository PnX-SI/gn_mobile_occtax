package fr.geonature.occtax.ui.input.taxa

import fr.geonature.commons.data.Taxonomy

/**
 * [Taxonomy] filter.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class FilterTaxonomy(value: Taxonomy) : Filter<Taxonomy>(
    FilterType.TAXONOMY,
    value
)