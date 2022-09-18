package fr.geonature.occtax.features.nomenclature.presentation

import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import fr.geonature.commons.data.entity.Nomenclature
import fr.geonature.commons.util.KeyboardUtils
import fr.geonature.occtax.R
import fr.geonature.occtax.features.nomenclature.domain.BaseEditableNomenclatureType
import fr.geonature.occtax.features.nomenclature.domain.EditableNomenclatureType
import fr.geonature.occtax.input.PropertyValue
import java.util.Locale

/**
 * Default RecyclerView Adapter about [EditableNomenclatureType].
 *
 * @author S. Grimault
 */
class EditableNomenclatureTypeAdapter(private val listener: OnEditableNomenclatureTypeAdapter) :
    RecyclerView.Adapter<EditableNomenclatureTypeAdapter.AbstractViewHolder>() {

    private val availableNomenclatureTypes = mutableListOf<EditableNomenclatureType>()
    private val selectedNomenclatureTypes = mutableListOf<EditableNomenclatureType>()
    private var showAllNomenclatureTypes = false

    init {
        this.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()

                listener.showEmptyTextView(itemCount == 0)
            }

            override fun onItemRangeChanged(
                positionStart: Int,
                itemCount: Int
            ) {
                super.onItemRangeChanged(
                    positionStart,
                    itemCount
                )

                listener.showEmptyTextView(getItemCount() == 0)
            }

            override fun onItemRangeInserted(
                positionStart: Int,
                itemCount: Int
            ) {
                super.onItemRangeInserted(
                    positionStart,
                    itemCount
                )

                listener.showEmptyTextView(false)
            }

            override fun onItemRangeRemoved(
                positionStart: Int,
                itemCount: Int
            ) {
                super.onItemRangeRemoved(
                    positionStart,
                    itemCount
                )

                listener.showEmptyTextView(getItemCount() == 0)
            }
        })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AbstractViewHolder {
        return when (viewType) {
            BaseEditableNomenclatureType.ViewType.NONE.ordinal -> MoreViewHolder(parent)
            BaseEditableNomenclatureType.ViewType.TEXT_SIMPLE.ordinal -> TextSimpleViewHolder(parent)
            BaseEditableNomenclatureType.ViewType.TEXT_MULTIPLE.ordinal -> TextMultipleViewHolder(parent)
            else -> NomenclatureTypeViewHolder(parent)
        }
    }

    override fun onBindViewHolder(holder: AbstractViewHolder, position: Int) {
        selectedNomenclatureTypes[position].also {
            holder.bind(it)
        }
    }

    override fun getItemCount(): Int {
        return selectedNomenclatureTypes.size
    }

    override fun getItemViewType(position: Int): Int {
        return selectedNomenclatureTypes[position].viewType.ordinal
    }

    fun bind(
        nomenclatureTypes: List<EditableNomenclatureType>,
        vararg propertyValue: PropertyValue
    ) {
        availableNomenclatureTypes.clear()
        availableNomenclatureTypes.addAll(nomenclatureTypes.map {
            it.copy(value = propertyValue.firstOrNull { propertyValue -> propertyValue.code == it.code }
                ?: it.value)
        })

        if (showAllNomenclatureTypes) showAllNomenclatureTypes() else showDefaultNomenclatureTypes()
    }

    fun setPropertyValue(vararg propertyValue: PropertyValue) {
        setSelectedNomenclatureTypes(selectedNomenclatureTypes.map {
            it.copy(value = propertyValue.firstOrNull { propertyValue -> propertyValue.code == it.code }
                ?: it.value)
        })
    }

    fun showDefaultNomenclatureTypes() {
        showAllNomenclatureTypes = false
        setSelectedNomenclatureTypes(
            availableNomenclatureTypes.filter { it.visible } + listOf(
                EditableNomenclatureType(
                    BaseEditableNomenclatureType.Type.INFORMATION,
                    "MORE",
                    BaseEditableNomenclatureType.ViewType.NONE,
                    true
                )
            )
        )
    }

    fun showAllNomenclatureTypes() {
        showAllNomenclatureTypes = true
        setSelectedNomenclatureTypes(availableNomenclatureTypes)
    }

    private fun setSelectedNomenclatureTypes(nomenclatureTypes: List<EditableNomenclatureType>) {
        val oldKeys = selectedNomenclatureTypes.map { it.code }
        val newKeys = nomenclatureTypes.map { it.code }

        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = oldKeys.size

            override fun getNewListSize(): Int = newKeys.size

            override fun areItemsTheSame(
                oldItemPosition: Int,
                newItemPosition: Int
            ) = oldKeys.elementAtOrNull(oldItemPosition) == newKeys.elementAtOrNull(newItemPosition)

            override fun areContentsTheSame(
                oldItemPosition: Int,
                newItemPosition: Int
            ) =
                oldKeys.elementAtOrNull(oldItemPosition)
                    ?.let { code -> selectedNomenclatureTypes.firstOrNull { it.code == code } }
                    ?.value?.value == newKeys.elementAtOrNull(newItemPosition)
                    ?.let { code -> nomenclatureTypes.firstOrNull { it.code == code } }
                    ?.value?.value
        })

        selectedNomenclatureTypes.clear()
        selectedNomenclatureTypes.addAll(nomenclatureTypes)

        diffResult.dispatchUpdatesTo(this)
    }

    abstract inner class AbstractViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var nomenclatureType: EditableNomenclatureType? = null

        fun bind(nomenclatureType: EditableNomenclatureType) {
            this.nomenclatureType = nomenclatureType

            onBind(nomenclatureType)
        }

        abstract fun onBind(nomenclatureType: EditableNomenclatureType)

        fun getNomenclatureTypeLabel(mnemonic: String): String {
            return itemView.resources.getIdentifier(
                "nomenclature_${mnemonic.lowercase()}",
                "string",
                itemView.context.packageName
            ).takeIf { it > 0 }?.let { itemView.context.getString(it) } ?: mnemonic
        }
    }

    inner class NomenclatureTypeViewHolder(parent: ViewGroup) : AbstractViewHolder(
        LayoutInflater.from(parent.context).inflate(
            R.layout.view_action_nomenclature_type_select,
            parent,
            false
        )
    ) {
        private var edit: TextInputLayout = itemView.findViewById(android.R.id.edit)

        init {
            (edit.editText as? AutoCompleteTextView)?.setAdapter(
                ArrayAdapter<Nomenclature>(
                    parent.context,
                    R.layout.list_item_2
                )
            )
        }

        override fun onBind(nomenclatureType: EditableNomenclatureType) {
            with(edit) {
                hint = nomenclatureType.label
                setEndIconOnClickListener {
                    listener.onAction(nomenclatureType.code)
                }

                editText?.apply {
                    setOnClickListener {
                        listener.onAction(nomenclatureType.code)
                    }
                    text = nomenclatureType.value?.let {
                        Editable.Factory
                            .getInstance()
                            .newEditable(it.label ?: it.code)
                    }
                }

            }
        }
    }

    inner class MoreViewHolder(parent: ViewGroup) : AbstractViewHolder(
        LayoutInflater.from(parent.context).inflate(
            R.layout.view_action_more,
            parent,
            false
        )
    ) {
        private var button1: Button = itemView.findViewById(android.R.id.button1)

        override fun onBind(nomenclatureType: EditableNomenclatureType) {
            with(button1) {
                text = getNomenclatureTypeLabel(nomenclatureType.code)
                setOnClickListener {
                    showAllNomenclatureTypes()
                    listener.showMore()
                }
            }
        }
    }

    open inner class TextSimpleViewHolder(parent: ViewGroup) : AbstractViewHolder(
        LayoutInflater.from(parent.context).inflate(
            R.layout.view_action_edit_text,
            parent,
            false
        )
    ) {
        internal var edit: TextInputLayout = itemView.findViewById(android.R.id.edit)
        private val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
            }

            override fun afterTextChanged(s: Editable?) {
                nomenclatureType?.also {
                    listener.onEdit(it.code,
                        s?.toString()?.ifEmpty { null }?.ifBlank { null })
                }
            }
        }

        init {
            with(edit) {
                editText?.addTextChangedListener(textWatcher)
                setOnFocusChangeListener { v, hasFocus ->
                    if (!hasFocus) {
                        // workaround to force hide the soft keyboard
                        KeyboardUtils.hideSoftKeyboard(v)
                    }
                }
            }
        }

        override fun onBind(nomenclatureType: EditableNomenclatureType) {
            edit.hint = getEditTextHint(nomenclatureType.code)

            if (nomenclatureType.value?.value is String? && !(nomenclatureType.value?.value as String?).isNullOrEmpty()) {
                edit.editText?.removeTextChangedListener(textWatcher)
                edit.editText?.text = (nomenclatureType.value?.value as String?)?.let {
                    Editable.Factory.getInstance().newEditable(it)
                }
                edit.editText?.addTextChangedListener(textWatcher)
            }
        }

        private fun getEditTextHint(mnemonic: String): String? {
            return itemView.resources.getIdentifier(
                "information_${mnemonic.lowercase(Locale.getDefault())}_hint",
                "string",
                itemView.context.packageName
            ).takeIf { it > 0 }?.let { itemView.context.getString(it) }
        }
    }

    inner class TextMultipleViewHolder(parent: ViewGroup) : TextSimpleViewHolder(parent) {
        init {
            edit.isCounterEnabled = true
            edit.editText?.apply {
                isSingleLine = false
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
                minLines = 2
                maxLines = 4
            }
        }
    }

    /**
     * Callback used by [EditableNomenclatureTypeAdapter].
     */
    interface OnEditableNomenclatureTypeAdapter {

        /**
         * Whether to show an empty text view when data changed.
         */
        fun showEmptyTextView(show: Boolean)

        /**
         * Called when the 'more' action button has been clicked.
         */
        fun showMore()

        /**
         * Called when the action button has been clicked for a given nomenclature type.
         *
         * @param nomenclatureTypeMnemonic the selected nomenclature type
         */
        fun onAction(nomenclatureTypeMnemonic: String)

        /**
         * Called when a value has been directly edited for a given nomenclature type.
         *
         * @param nomenclatureTypeMnemonic the selected nomenclature type
         * @param value the corresponding value (may be `null`)
         */
        fun onEdit(
            nomenclatureTypeMnemonic: String,
            value: String?
        )
    }
}