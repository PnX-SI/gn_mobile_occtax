package fr.geonature.occtax.features.nomenclature.presentation.adapter

import android.text.Editable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import com.google.android.material.textfield.TextInputLayout
import fr.geonature.occtax.R
import fr.geonature.occtax.features.nomenclature.domain.FormField

/**
 * [FormFieldAdapter] view holder representing a selectable item from modal.
 *
 * @author S. Grimault
 */
class FormFieldModalViewHolder(
    parent: ViewGroup,
    private val listener: OnFormFieldModalViewHolder
) :
    FormFieldAdapter.AbstractFormFieldViewHolder<FormField.Modal>(
        LayoutInflater.from(parent.context)
            .inflate(
                R.layout.list_item_formfield_modal,
                parent,
                false
            )
    ) {

    private var edit: TextInputLayout = itemView.findViewById(android.R.id.edit)

    init {
        edit.editText?.apply {
            setOnClickListener {
                formField?.also {
                    listener.onAction(it)
                }
            }
        }
    }

    override fun onBind(formField: FormField.Modal) {
        edit.hint = formField.label

        formField.item?.also {
            edit.post {
                edit.editText?.text = Editable.Factory.getInstance()
                    .newEditable(
                        HtmlCompat.fromHtml(
                            "<b>${it.first}</b>${it.second?.let { second -> "<br/><small><i>$second</i></small>" } ?: ""}",
                            HtmlCompat.FROM_HTML_MODE_LEGACY
                        )
                    )
            }
        }

        if (formField.mandatory && formField.getValue()
                .isEmpty()
        ) {
            edit.error = itemView.context.getString(R.string.form_field_error_mandatory)
            formField.error = edit.error
        }
    }

    /**
     * Callback used by [FormFieldModalViewHolder].
     */
    interface OnFormFieldModalViewHolder {

        /**
         * Called when the action button has been clicked.
         * Should show the modal of selectable item.
         */
        fun onAction(formField: FormField.Modal)
    }
}