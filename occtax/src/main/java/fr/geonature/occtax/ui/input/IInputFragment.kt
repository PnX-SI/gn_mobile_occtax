package fr.geonature.occtax.ui.input

import fr.geonature.commons.input.AbstractInput

/**
 * `Fragment` using [AbstractInput].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
interface IInputFragment {

    /**
     * Sets the current [AbstractInput] to update.
     *
     * @param input the current [AbstractInput] to update
     */
    fun setInput(input: AbstractInput)
}
