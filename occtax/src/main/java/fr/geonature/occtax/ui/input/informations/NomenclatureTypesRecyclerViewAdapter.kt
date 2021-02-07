package fr.geonature.occtax.ui.input.informations

import android.database.Cursor
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import fr.geonature.commons.data.NomenclatureType
import fr.geonature.commons.util.KeyboardUtils.hideSoftKeyboard
import fr.geonature.occtax.R
import fr.geonature.occtax.input.InputTaxon
import fr.geonature.occtax.input.NomenclatureTypeViewType
import fr.geonature.occtax.input.PropertyValue
import fr.geonature.occtax.settings.PropertySettings
import java.util.Locale

/**
 * Default RecyclerView Adapter used by [InformationFragment].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class NomenclatureTypesRecyclerViewAdapter(private val listener: OnNomenclatureTypesRecyclerViewAdapterListener) :
    RecyclerView.Adapter<NomenclatureTypesRecyclerViewAdapter.AbstractViewHolder>() {

    private val mnemonicFilter = InputTaxon.defaultPropertiesMnemonic
    private val moreViewType = Triple(
        "MORE",
        NomenclatureTypeViewType.MORE,
        true
    )

    private val availableNomenclatureTypes =
        mutableListOf<Triple<String, NomenclatureTypeViewType, Boolean>>()
    private val properties = mutableListOf<PropertyValue>()
    private var showAllNomenclatureTypes = false

    private val onClickListener: View.OnClickListener

    init {
        onClickListener = View.OnClickListener { v ->
            val selectedProperty = v.tag as PropertyValue
            listener.onAction(selectedProperty.code)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AbstractViewHolder {
        return when (NomenclatureTypeViewType.values()[viewType]) {
            NomenclatureTypeViewType.MORE -> MoreViewHolder(parent)
            NomenclatureTypeViewType.TEXT_SIMPLE -> TextSimpleViewHolder(parent)
            NomenclatureTypeViewType.TEXT_MULTIPLE -> TextMultipleViewHolder(parent)
            else -> NomenclatureTypeViewHolder(parent)
        }
    }

    override fun getItemCount(): Int {
        return properties.size
    }

    override fun onBindViewHolder(
        holder: AbstractViewHolder,
        position: Int
    ) {
        holder.bind(properties[position])
    }

    override fun getItemViewType(position: Int): Int {
        val property = properties[position]

        return if (property.code == moreViewType.first) moreViewType.second.ordinal
        else mnemonicFilter.first { it.first == properties[position].code }
            .second.ordinal
    }

    fun defaultMnemonicFilter(): List<String> {
        return mnemonicFilter.asSequence()
            .filter { it.second == NomenclatureTypeViewType.NOMENCLATURE_TYPE }
            .map { it.first }
            .toList()
    }

    fun bind(cursor: Cursor?, vararg defaultPropertySettings: PropertySettings) {
        availableNomenclatureTypes.clear()

        cursor?.run {
            if (this.isClosed) return@run

            this.moveToFirst()

            while (!this.isAfterLast) {
                NomenclatureType.fromCursor(this)
                    ?.run {
                        (if (defaultPropertySettings.isEmpty()) {
                            mnemonicFilter.find { it.first == mnemonic }
                        } else {
                            defaultPropertySettings.find { it.key == mnemonic && it.visible }
                                ?.let { property -> mnemonicFilter.find { it.first == property.key } }
                        })?.also {
                            availableNomenclatureTypes.add(it)
                        }
                    }
                cursor.moveToNext()
            }

            // add default mnemonic filters
            availableNomenclatureTypes.addAll(mnemonicFilter.filter {
                it.second != NomenclatureTypeViewType.NOMENCLATURE_TYPE &&
                    (defaultPropertySettings.isEmpty() || defaultPropertySettings.any { p -> p.key == it.first })
            })
        }

        availableNomenclatureTypes.sortWith { o1, o2 ->
            val i1 = mnemonicFilter.indexOfFirst { it.first == o1.first }
            val i2 = mnemonicFilter.indexOfFirst { it.first == o2.first }

            when {
                i1 == -1 -> 1
                i2 == -1 -> -1
                else -> i1 - i2
            }
        }

        val nomenclatureTypes = if (showAllNomenclatureTypes) {
            availableNomenclatureTypes
        } else {
            val defaultNomenclatureTypes =
                availableNomenclatureTypes.filter { availableNomenclatureType ->
                    if (defaultPropertySettings.isEmpty()) availableNomenclatureType.third
                    else defaultPropertySettings.any { it.key == availableNomenclatureType.first && it.default }
                }

            // add MORE ViewType if default nomenclature types are presents
            if (defaultNomenclatureTypes.size < availableNomenclatureTypes.size) {
                listOf(
                    *defaultNomenclatureTypes.toTypedArray(),
                    moreViewType
                )
            } else {
                availableNomenclatureTypes
            }
        }

        setNomenclatureTypes(nomenclatureTypes)
    }

    fun setPropertyValues(selectedProperties: List<PropertyValue>) {
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int {
                return this@NomenclatureTypesRecyclerViewAdapter.properties.size
            }

            override fun getNewListSize(): Int {
                return this@NomenclatureTypesRecyclerViewAdapter.properties.size
            }

            override fun areItemsTheSame(
                oldItemPosition: Int,
                newItemPosition: Int
            ): Boolean {
                return true
            }

            override fun areContentsTheSame(
                oldItemPosition: Int,
                newItemPosition: Int
            ): Boolean {
                val oldProperty =
                    this@NomenclatureTypesRecyclerViewAdapter.properties[oldItemPosition]
                val newProperty = selectedProperties.firstOrNull { it.code == oldProperty.code }

                return oldProperty == newProperty
            }
        })

        val newProperties = this.properties.map { p ->
            selectedProperties.firstOrNull { it.code == p.code } ?: p
        }
        this.properties.clear()
        this.properties.addAll(newProperties)

        diffResult.dispatchUpdatesTo(this)
    }

    fun showAllNomenclatureTypes(showAllNomenclatureTypes: Boolean = false) {
        this.showAllNomenclatureTypes = showAllNomenclatureTypes
        setNomenclatureTypes(availableNomenclatureTypes)
    }

    private fun setNomenclatureTypes(nomenclatureTypes: List<Triple<String, NomenclatureTypeViewType, Boolean>>) {
        if (this.properties.isEmpty()) {
            this.properties.addAll(nomenclatureTypes.map {
                when (it.second) {
                    NomenclatureTypeViewType.NOMENCLATURE_TYPE -> PropertyValue.fromNomenclature(
                        it.first,
                        null
                    )
                    else -> PropertyValue.fromValue(
                        it.first,
                        null
                    )
                }
            })

            if (this.properties.isNotEmpty()) {
                notifyItemRangeInserted(
                    0,
                    this.properties.size
                )
            }

            return
        }

        if (nomenclatureTypes.isEmpty()) {
            this.properties.clear()
            notifyDataSetChanged()

            return
        }

        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {

            override fun getOldListSize(): Int {
                return this@NomenclatureTypesRecyclerViewAdapter.properties.size
            }

            override fun getNewListSize(): Int {
                return nomenclatureTypes.size
            }

            override fun areItemsTheSame(
                oldItemPosition: Int,
                newItemPosition: Int
            ): Boolean {
                return this@NomenclatureTypesRecyclerViewAdapter.properties[oldItemPosition].code == nomenclatureTypes[newItemPosition].first
            }

            override fun areContentsTheSame(
                oldItemPosition: Int,
                newItemPosition: Int
            ): Boolean {
                return this@NomenclatureTypesRecyclerViewAdapter.properties[oldItemPosition].code == nomenclatureTypes[newItemPosition].first
            }
        })

        val newProperties = nomenclatureTypes.map { pair ->
            properties.firstOrNull {
                it.code == pair.first
            }
                ?: when (pair.second) {
                    NomenclatureTypeViewType.NOMENCLATURE_TYPE -> PropertyValue.fromNomenclature(
                        pair.first,
                        null
                    )
                    else -> PropertyValue.fromValue(
                        pair.first,
                        null
                    )
                }
        }

        this.properties.clear()
        this.properties.addAll(newProperties)

        diffResult.dispatchUpdatesTo(this)
    }

    abstract inner class AbstractViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var property: PropertyValue? = null

        fun bind(property: PropertyValue) {
            this.property = property

            onBind(property)
        }

        abstract fun onBind(property: PropertyValue)

        fun getNomenclatureTypeLabel(mnemonic: String): String {
            val resourceId = itemView.resources.getIdentifier(
                "nomenclature_${mnemonic.toLowerCase(Locale.getDefault())}",
                "string",
                itemView.context.packageName
            )

            return if (resourceId == 0) mnemonic else itemView.context.getString(resourceId)
        }
    }

    abstract inner class AbstractCardViewHolder(parent: ViewGroup) : AbstractViewHolder(
        LayoutInflater.from(parent.context).inflate(
            R.layout.card_view,
            parent,
            false
        )
    ) {
        internal val contentView: View

        init {
            contentView = LayoutInflater.from(itemView.context)
                .inflate(
                    this.getLayoutResourceId(),
                    itemView as FrameLayout,
                    true
                )
        }

        @LayoutRes
        abstract fun getLayoutResourceId(): Int
    }

    inner class NomenclatureTypeViewHolder(parent: ViewGroup) : AbstractCardViewHolder(parent) {
        private var title: TextView = contentView.findViewById(android.R.id.title)
        private var text1: TextView = contentView.findViewById(android.R.id.text1)
        private var button1: Button = contentView.findViewById(android.R.id.button1)

        override fun getLayoutResourceId(): Int {
            return R.layout.view_action_nomenclature_type
        }

        override fun onBind(property: PropertyValue) {
            title.text = getNomenclatureTypeLabel(property.code)
            text1.text = property.label

            with(button1) {
                tag = property
                setOnClickListener(onClickListener)
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

        override fun onBind(property: PropertyValue) {
            with(button1) {
                text = getNomenclatureTypeLabel(property.code)
                setOnClickListener {
                    showAllNomenclatureTypes(true)
                    listener.showMore()
                }
            }
        }
    }

    open inner class TextSimpleViewHolder(parent: ViewGroup) : AbstractCardViewHolder(parent) {
        private var title: TextView = contentView.findViewById(android.R.id.title)
        internal var edit: EditText = contentView.findViewById(android.R.id.edit)
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
                val property = property ?: return

                listener.onEdit(property.code,
                    s?.toString()?.ifEmpty { null }?.ifBlank { null })
            }
        }

        init {
            with(edit) {
                addTextChangedListener(textWatcher)
                setOnFocusChangeListener { v, hasFocus ->
                    if (!hasFocus) {
                        // workaround to force hide the soft keyboard
                        hideSoftKeyboard(v)
                    }
                }
            }
        }

        override fun getLayoutResourceId(): Int {
            return R.layout.view_action_edit_text
        }

        override fun onBind(property: PropertyValue) {
            title.text = getNomenclatureTypeLabel(property.code)
            edit.hint = getEditTextHint(property.code)

            if (property.value is String? && !property.value.isNullOrEmpty()) {
                edit.removeTextChangedListener(textWatcher)
                edit.text = Editable.Factory.getInstance()
                    .newEditable(property.value)
                edit.addTextChangedListener(textWatcher)
            }
        }

        private fun getEditTextHint(mnemonic: String): String {
            val resourceId = contentView.resources.getIdentifier(
                "information_${mnemonic.toLowerCase(Locale.getDefault())}_hint",
                "string",
                contentView.context.packageName
            )
            return if (resourceId == 0) "" else contentView.context.getString(resourceId)
        }
    }

    inner class TextMultipleViewHolder(parent: ViewGroup) : TextSimpleViewHolder(parent) {
        init {
            edit.apply {
                isSingleLine = false
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
                minLines = 2
                maxLines = 4
            }
        }
    }

    /**
     * Callback used by [NomenclatureTypesRecyclerViewAdapter].
     */
    interface OnNomenclatureTypesRecyclerViewAdapterListener {

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
