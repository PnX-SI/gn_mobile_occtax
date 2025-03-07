package fr.geonature.occtax.features.nomenclature.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import fr.geonature.occtax.R
import fr.geonature.occtax.features.nomenclature.domain.FormField
import fr.geonature.occtax.ui.shared.view.ListItemActionView
import fr.geonature.occtax.ui.shared.view.ValidationCardView

/**
 * [FormFieldAdapter] view holder representing a list of selectable items from modal.
 *
 * @author S. Grimault
 */
class FormFieldModalMultipleViewHolder(
    parent: ViewGroup,
    private val listener: OnFormFieldModalMultipleViewHolder
) :
    FormFieldAdapter.AbstractFormFieldViewHolder<FormField.ModalMultiple>(
        LayoutInflater.from(parent.context)
            .inflate(
                R.layout.list_item_formfield_modal_multiple,
                parent,
                false
            )
    ) {

    private var listItemActionView: ListItemActionView =
        itemView.findViewById(R.id.list_item_action_view)

    init {
        with(listItemActionView) {
            setListener(object : ListItemActionView.OnListItemActionViewListener {
                override fun onAction() {
                    formField?.also {
                        listener.onAction(it)
                    }
                }
            })
        }
    }

    override fun onBind(formField: FormField.ModalMultiple) {
        with(listItemActionView) {
            setTitle(formField.label)
            setEmptyText(formField.emptyText)
            setActionText(formField.actionText)
            setActionEmptyText(formField.actionEmptyText)
            setVisibleItems(formField.visibleItems)
            setItems(formField.items)
        }

        if (formField.mandatory && formField.getValue().isEmpty()) {
            (itemView as ValidationCardView).hasErrors = true
            formField.error = itemView.context.getString(R.string.form_field_error_mandatory)
        }
    }

    /**
     * Callback used by [FormFieldModalMultipleViewHolder].
     */
    interface OnFormFieldModalMultipleViewHolder {

        /**
         * Called when the action button has been clicked.
         * Should show the modal of selectable items.
         */
        fun onAction(formField: FormField.ModalMultiple)
    }
}