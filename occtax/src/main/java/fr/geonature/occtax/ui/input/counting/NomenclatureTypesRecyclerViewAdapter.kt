package fr.geonature.occtax.ui.input.counting

import android.database.Cursor
import android.text.Editable
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
import fr.geonature.occtax.input.CountingMetadata
import fr.geonature.occtax.input.NomenclatureTypeViewType
import fr.geonature.occtax.input.PropertyValue
import fr.geonature.occtax.settings.PropertySettings
import java.util.Locale

/**
 * Default RecyclerView Adapter used by [EditCountingMetadataFragment].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class NomenclatureTypesRecyclerViewAdapter(private val listener: OnNomenclatureTypesRecyclerViewAdapterListener) :
    RecyclerView.Adapter<NomenclatureTypesRecyclerViewAdapter.AbstractCardViewHolder>() {

    private val mnemonicFilter = CountingMetadata.defaultMnemonic
    private val availableNomenclatureTypes = mutableListOf<Pair<String, NomenclatureTypeViewType>>()
    private val properties = mutableListOf<PropertyValue>()

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
    ): AbstractCardViewHolder {
        return when (NomenclatureTypeViewType.values()[viewType]) {
            NomenclatureTypeViewType.MIN_MAX -> MinMaxViewHolder(parent)
            else -> NomenclatureTypeViewHolder(parent)
        }
    }

    override fun getItemCount(): Int {
        return properties.filter { p -> mnemonicFilter.find { it.first == p.code }?.second != NomenclatureTypeViewType.MIN_MAX }.size +
            properties.filter { p -> mnemonicFilter.find { it.first == p.code }?.second == NomenclatureTypeViewType.MIN_MAX }.size.coerceAtMost(1)
    }

    override fun onBindViewHolder(
        holder: AbstractCardViewHolder,
        position: Int
    ) {
        holder.bind(properties[position])
    }

    override fun getItemViewType(position: Int): Int {
        return mnemonicFilter.first { it.first == properties[position].code }
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

        setNomenclatureTypes(availableNomenclatureTypes)
    }

    fun setCountingMetata(countingMetadata: CountingMetadata) {
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
                val viewType = mnemonicFilter.firstOrNull { it.first == oldProperty.code }
                    ?.second ?: return false

                return when (viewType) {
                    NomenclatureTypeViewType.MIN_MAX -> {
                        val minProperty =
                            this@NomenclatureTypesRecyclerViewAdapter.properties.firstOrNull { it.code == "MIN" }
                        val maxProperty =
                            this@NomenclatureTypesRecyclerViewAdapter.properties.firstOrNull { it.code == "MAX" }

                        if (minProperty == null || maxProperty == null) return false

                        return minProperty.value as Int == countingMetadata.min && maxProperty.value as Int == countingMetadata.max
                    }
                    else -> countingMetadata.properties[oldProperty.code] == oldProperty
                }
            }
        })

        val newProperties = this.properties.map { p ->
            when (mnemonicFilter.firstOrNull { it.first == p.code }?.second ?: return@map p) {
                NomenclatureTypeViewType.MIN_MAX -> when (p.code) {
                    "MIN" -> PropertyValue.fromValue(
                        p.code,
                        countingMetadata.min
                    )
                    "MAX" -> PropertyValue.fromValue(
                        p.code,
                        countingMetadata.max
                    )
                    else -> p
                }
                else -> countingMetadata.properties[p.code]
                    ?: p
            }
        }
        this.properties.clear()
        this.properties.addAll(newProperties)

        diffResult.dispatchUpdatesTo(this)
    }

    private fun setNomenclatureTypes(nomenclatureTypes: List<Pair<String, NomenclatureTypeViewType>>) {
        if (this.properties.isEmpty()) {
            this.properties.addAll(nomenclatureTypes.map {
                when (it.second) {
                    NomenclatureTypeViewType.NOMENCLATURE_TYPE -> PropertyValue.fromNomenclature(
                        it.first,
                        null
                    )
                    else -> PropertyValue.fromValue(
                        it.first,
                        0
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

        this.properties.clear()
        this.properties.addAll(nomenclatureTypes.map {
            when (it.second) {
                NomenclatureTypeViewType.NOMENCLATURE_TYPE -> PropertyValue.fromNomenclature(
                    it.first,
                    null
                )
                else -> PropertyValue.fromValue(
                    it.first,
                    0
                )
            }
        })

        diffResult.dispatchUpdatesTo(this)
    }

    abstract inner class AbstractCardViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(
            R.layout.card_view,
            parent,
            false
        )
    ) {
        internal val contentView: View
        private var property: PropertyValue? = null

        init {
            contentView = LayoutInflater.from(itemView.context)
                .inflate(
                    this.getLayoutResourceId(),
                    itemView as FrameLayout,
                    true
                )

            // workaround to force hide the soft keyboard
            contentView.setOnClickListener {
                it.clearFocus()
            }
        }

        fun bind(property: PropertyValue) {
            this.property = property

            onBind(property)
        }

        @LayoutRes
        abstract fun getLayoutResourceId(): Int

        abstract fun onBind(property: PropertyValue)

        fun getNomenclatureTypeLabel(mnemonic: String): String {
            val resourceId = contentView.resources.getIdentifier(
                "nomenclature_${mnemonic.toLowerCase(Locale.getDefault())}",
                "string",
                contentView.context.packageName
            )

            return if (resourceId == 0) mnemonic else contentView.context.getString(resourceId)
        }
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

    inner class MinMaxViewHolder(parent: ViewGroup) : AbstractCardViewHolder(parent) {
        private var title: TextView = contentView.findViewById(android.R.id.title)
        private var editMin: EditText = contentView.findViewById(R.id.editMin)
        private var editMax: EditText = contentView.findViewById(R.id.editMax)

        private val minTextWatcher = object : TextWatcher {
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
                val minValue = s?.toString()?.toIntOrNull() ?: 0
                val maxValue = editMax.text?.toString()?.toIntOrNull() ?: 0

                if (minValue > maxValue) setMaxValue(minValue)

                setMinValue(minValue)
                editMin.setSelection(minValue.toString().length)

                listener.onMinMaxValues(
                    minValue,
                    if (minValue > maxValue) minValue else maxValue
                )
            }
        }

        private val maxTextWatcher = object : TextWatcher {
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
                val minValue = editMin.text?.toString()?.toIntOrNull() ?: 0
                val maxValue = s?.toString()?.toIntOrNull() ?: 0

                setMaxValue(maxValue)
                editMax.setSelection(maxValue.toString().length)

                listener.onMinMaxValues(
                    minValue,
                    if (minValue > maxValue) minValue else maxValue
                )
            }
        }

        init {
            with(editMin) {
                visibility = View.GONE
                addTextChangedListener(minTextWatcher)
                setOnFocusChangeListener { v, hasFocus ->
                    if (!hasFocus) {
                        // workaround to force hide the soft keyboard
                        hideSoftKeyboard(v)
                    }
                }
            }

            with(editMax) {
                visibility = View.GONE
                addTextChangedListener(maxTextWatcher)
                setOnFocusChangeListener { v, hasFocus ->
                    if (!hasFocus) {
                        // workaround to force hide the soft keyboard
                        hideSoftKeyboard(v)

                        val minValue = editMin.text?.toString()?.toIntOrNull() ?: 0
                        val maxValue = editMax.text?.toString()?.toIntOrNull() ?: 0

                        if (minValue > maxValue) setMaxValue(minValue)
                    }
                }
            }
        }

        override fun getLayoutResourceId(): Int {
            return R.layout.view_action_min_max
        }

        override fun onBind(property: PropertyValue) {
            setTitle(property)

            editMin.visibility =
                if (hasMinAndMaxPropertyValues() || property.code == "MIN") View.VISIBLE else View.GONE
            editMax.visibility =
                if (hasMinAndMaxPropertyValues() || property.code == "MAX") View.VISIBLE else View.GONE

            (properties.firstOrNull { it.code == "MIN" }?.value as Int?)?.also {
                setMinValue(it)
            }

            (properties.firstOrNull { it.code == "MAX" }?.value as Int?)?.also {
                setMaxValue(it)
            }
        }

        private fun setTitle(property: PropertyValue) {
            title.setText(if (hasMinAndMaxPropertyValues()) {
                R.string.counting_min_max_title
            } else {
                contentView.resources.getIdentifier(
                    "counting_${property.code.toLowerCase(Locale.ROOT)}_title",
                    "string",
                    contentView.context.packageName
                ).takeIf { it > 0 } ?: R.string.counting_min_max_title
            })
        }

        private fun setMinValue(min: Int = 0) {
            editMin.also {
                it.removeTextChangedListener(minTextWatcher)
                it.text = Editable.Factory.getInstance()
                    .newEditable(min.toString())
                it.addTextChangedListener(minTextWatcher)
            }

            if (editMax.text?.toString()?.toIntOrNull() ?: 0 < min) setMaxValue(min)
        }

        private fun setMaxValue(max: Int = 0) {
            editMax.also {
                it.removeTextChangedListener(maxTextWatcher)
                it.text = Editable.Factory.getInstance()
                    .newEditable(max.toString())
                it.addTextChangedListener(maxTextWatcher)
            }
        }

        private fun hasMinAndMaxPropertyValues(): Boolean {
            return properties.filter { p -> mnemonicFilter.find { it.first == p.code }?.second == NomenclatureTypeViewType.MIN_MAX }.size == 2
        }
    }

    /**
     * Callback used by [NomenclatureTypesRecyclerViewAdapter].
     */
    interface OnNomenclatureTypesRecyclerViewAdapterListener {

        /**
         * Called when the action button has been clicked for a given nomenclature type.
         *
         * @param nomenclatureTypeMnemonic the selected nomenclature type
         */
        fun onAction(nomenclatureTypeMnemonic: String)

        /**
         * Called when min/max values have been set.
         *
         * @param min the min value (default: 0)
         * @param max the max value (default: the min value)
         */
        fun onMinMaxValues(
            min: Int = 0,
            max: Int = 0
        )
    }
}
