package fr.geonature.occtax.features.nomenclature.presentation.adapter

import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams
import fr.geonature.occtax.features.nomenclature.domain.FormField

/**
 * [EditableFieldAdapter] view holder representing nothing and show only an empty view
 * (i.e. a non supported [FormField]).
 *
 * @author S. Grimault
 */
class NoneViewHolder(parent: ViewGroup) :
    EditableFieldAdapter.AbstractViewHolder(LinearLayout(parent.context).apply {
        layoutParams = ViewGroup.LayoutParams(
            LayoutParams.MATCH_PARENT,
            0
        )
    }) {

    override fun bind(formField: FormField) {
        // nothing to do...
    }
}