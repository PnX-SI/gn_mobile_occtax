package fr.geonature.occtax.features.nomenclature.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import fr.geonature.occtax.R
import fr.geonature.occtax.features.nomenclature.domain.FormField

/**
 * [FormFieldAdapter] [FormField.Button] view holder representing a simple button.
 *
 * @author S. Grimault
 */
class FormFieldButtonViewHolder(
    parent: ViewGroup,
    private val listener: OnFormFieldButtonViewHolderListener
) : FormFieldAdapter.AbstractFormFieldViewHolder<FormField.Button>(
    LayoutInflater.from(parent.context)
        .inflate(
            R.layout.list_item_formfield_button,
            parent,
            false
        )
) {
    private var button1: Button = itemView.findViewById(android.R.id.button1)

    override fun onBind(formField: FormField.Button) {
        with(button1) {
            text = formField.label
            setOnClickListener {
                listener.onClick(formField)
            }
        }
    }

    /**
     * Callback used by [FormFieldButtonViewHolder].
     */
    interface OnFormFieldButtonViewHolderListener {

        /**
         * Called when the button has been clicked.
         */
        fun onClick(button: FormField.Button)
    }
}