package fr.geonature.occtax.features.nomenclature.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import fr.geonature.occtax.R
import fr.geonature.occtax.features.nomenclature.domain.EditableField

/**
 * [EditableFieldAdapter] special view holder representing a simple 'Show more' button.
 *
 * @author S. Grimault
 */
class ShowMoreViewHolder(
    parent: ViewGroup,
    private val listener: OnMoreViewHolderListener
) : EditableFieldAdapter.AbstractViewHolder(
    LayoutInflater.from(parent.context)
        .inflate(
            R.layout.view_action_more,
            parent,
            false
        )
) {
    private var button1: Button = itemView.findViewById(android.R.id.button1)

    override fun onBind(editableField: EditableField) {
        with(button1) {
            text = getDefaultLabel(editableField)
            setOnClickListener {
                listener.showMore()
            }
        }
    }

    /**
     * Callback used by [ShowMoreViewHolder].
     */
    interface OnMoreViewHolderListener {

        /**
         * Called when the 'more' action button has been clicked.
         */
        fun showMore()
    }
}