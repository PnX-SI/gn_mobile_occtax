package fr.geonature.occtax.features.nomenclature.data

import fr.geonature.commons.data.entity.Nomenclature
import fr.geonature.commons.data.entity.NomenclatureType
import fr.geonature.commons.data.entity.Taxonomy

/**
 * [NomenclatureType], [Nomenclature] local data source.
 *
 * @author S. Grimault
 */
interface INomenclatureLocalDataSource {

    /**
     * Gets all [NomenclatureType].
     *
     * @return a list of [NomenclatureType]
     */
    suspend fun getAllNomenclatureTypes(): List<NomenclatureType>

    /**
     * Gets all [Nomenclature] as default nomenclature values.
     *
     * @return a list of default [Nomenclature]
     */
    suspend fun getAllDefaultNomenclatureValues(): List<Nomenclature>

    /**
     * Gets all nomenclature values matching given nomenclature type and an optional taxonomy rank.
     *
     * @param mnemonic the nomenclature type as main filter
     * @param taxonomy the taxonomy rank
     *
     * @return a list of [Nomenclature] matching given criteria
     */
    suspend fun getNomenclatureValuesByTypeAndTaxonomy(
        mnemonic: String,
        taxonomy: Taxonomy? = null
    ): List<Nomenclature>
}