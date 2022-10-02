package fr.geonature.occtax.ui.input

import fr.geonature.viewpager.ui.OnPageFragmentListener

/**
 * Callback used within pages to control [InputPagerFragmentActivity] view pager.
 *
 * @author S. Grimault
 */
interface OnInputPageFragmentListener : OnPageFragmentListener {

    /**
     * Start taxon editing workflow.
     */
    fun startEditTaxon()

    /**
     * Finish taxon editing workflow.
     */
    fun finishEditTaxon()
}