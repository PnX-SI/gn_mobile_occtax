package fr.geonature.occtax.features.nomenclature.repository

import fr.geonature.commons.data.entity.Nomenclature
import fr.geonature.commons.data.entity.Taxonomy
import fr.geonature.commons.error.Failure
import fr.geonature.commons.fp.Either
import fr.geonature.occtax.features.nomenclature.domain.BaseEditableNomenclatureType
import fr.geonature.occtax.features.nomenclature.domain.EditableNomenclatureType
import fr.geonature.occtax.settings.PropertySettings

/**
 * Editable nomenclature types repository.
 *
 * @author S. Grimault
 */
interface INomenclatureRepository {

    /**
     * Gets all editable nomenclatures from given type with default values from nomenclature.
     *
     * @param type the main editable nomenclature type
     * @param defaultPropertySettings the default nomenclature settings
     *
     * @return a list of [EditableNomenclatureType] or [Failure] if none was configured
     */
    suspend fun getEditableNomenclatures(
        type: BaseEditableNomenclatureType.Type,
        vararg defaultPropertySettings: PropertySettings
    ): Either<Failure, List<EditableNomenclatureType>>

    /**
     * Gets all nomenclature values matching given nomenclature type and an optional taxonomy rank.
     *
     * @param mnemonic the nomenclature type as main filter
     * @param taxonomy the taxonomy rank
     *
     * @return a list of [Nomenclature] matching given criteria or [Failure] if something goes wrong
     */
    suspend fun getNomenclatureValuesByTypeAndTaxonomy(
        mnemonic: String,
        taxonomy: Taxonomy? = null
    ): Either<Failure, List<Nomenclature>>
}