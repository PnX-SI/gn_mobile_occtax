package fr.geonature.occtax.ui.input.counting

import android.database.Cursor
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.NumberPicker
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import fr.geonature.commons.data.entity.Nomenclature
import fr.geonature.commons.data.entity.NomenclatureType
import fr.geonature.occtax.R
import fr.geonature.occtax.input.CountingMetadata
import fr.geonature.occtax.input.NomenclatureTypeViewType
import fr.geonature.occtax.input.PropertyValue
import fr.geonature.occtax.settings.PropertySettings
import fr.geonature.occtax.ui.shared.view.setOnValueChangedListener
import java.util.Locale
import kotlin.math.ceil

/**
 * Default RecyclerView Adapter used by [EditCountingMetadataFragment].
 *
 * @author S. Grimault
 */
class NomenclatureTypesRecyclerViewAdapter(private val listener: OnNomenclatureTypesRecyclerViewAdapterListener) :
    RecyclerView.Adapter<NomenclatureTypesRecyclerViewAdapter.AbstractViewHolder>() {

    private val mnemonicFilter = CountingMetadata.defaultMnemonic
    private val availableNomenclatureTypes = mutableListOf<Pair<String, NomenclatureTypeViewType>>()
    private val properties = mutableListOf<PropertyValue>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AbstractViewHolder {
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
        holder: AbstractViewHolder,
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
            val numberOfProperties = this.properties.size
            this.properties.clear()
            notifyItemRangeRemoved(
                0,
                numberOfProperties
            )

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

    abstract inner class AbstractViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var property: PropertyValue? = null

        fun bind(property: PropertyValue) {
            this.property = property

            onBind(property)
        }

        abstract fun onBind(property: PropertyValue)

        fun getNomenclatureTypeLabel(mnemonic: String): String {
            val resourceId = itemView.resources.getIdentifier(
                "nomenclature_${mnemonic.lowercase(Locale.getDefault())}",
                "string",
                itemView.context.packageName
            )

            return if (resourceId == 0) mnemonic else itemView.context.getString(resourceId)
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

        override fun onBind(property: PropertyValue) {
            with(edit) {
                hint = getNomenclatureTypeLabel(property.code)
                setEndIconOnClickListener {
                    listener.onAction(property.code)
                }

                editText?.apply {
                    setOnClickListener {
                        listener.onAction(property.code)
                    }
                    text = property.label?.let {
                        Editable.Factory
                            .getInstance()
                            .newEditable(it)
                    }
                }
            }
        }
    }

    inner class MinMaxViewHolder(parent: ViewGroup) : AbstractViewHolder(
        LayoutInflater.from(parent.context).inflate(
            R.layout.view_action_min_max,
            parent,
            false
        )
    ) {
        private val defaultMaxValueOffset = 50
        private var editMinLabel: TextView = itemView.findViewById(R.id.editMinLabel)
        private var editMaxLabel: TextView = itemView.findViewById(R.id.editMaxLabel)
        private var editMinPicker: NumberPicker = itemView.findViewById(R.id.editMinPicker)
        private var editMaxPicker: NumberPicker = itemView.findViewById(R.id.editMaxPicker)

        init {
            with(editMinPicker) {
                minValue = 0
                maxValue = defaultMaxValueOffset
                setOnValueChangedListener(defaultMaxValueOffset) {
                    editMaxPicker.maxValue = editMinPicker.maxValue

                    if (editMaxPicker.value < it) editMaxPicker.value = it

                    listener.onMinMaxValues(
                        it,
                        editMaxPicker.value
                    )
                }
            }

            with(editMaxPicker) {
                minValue = 0
                maxValue = defaultMaxValueOffset
                setOnValueChangedListener(defaultMaxValueOffset) {
                    editMinPicker.maxValue = editMaxPicker.maxValue

                    if (editMinPicker.value > it) editMinPicker.value = it

                    listener.onMinMaxValues(
                        editMinPicker.value,
                        it
                    )
                }
            }
        }

        override fun onBind(property: PropertyValue) {
            with(if (hasMinAndMaxPropertyValues() || property.code == "MIN") View.VISIBLE else View.GONE) {
                editMinLabel.visibility = this
                editMinPicker.visibility = this
            }

            with(if (hasMinAndMaxPropertyValues() || property.code == "MAX") View.VISIBLE else View.GONE) {
                editMaxLabel.visibility = this
                editMaxPicker.visibility = this
            }

            (properties.firstOrNull { it.code == "MIN" }?.value as Int?)?.also {
                if (it > editMinPicker.maxValue) {
                    editMinPicker.maxValue =
                        (ceil((it.toDouble() / defaultMaxValueOffset)) * defaultMaxValueOffset).toInt()
                    editMaxPicker.maxValue = editMinPicker.maxValue
                }

                editMinPicker.value = it
            }

            (properties.firstOrNull { it.code == "MAX" }?.value as Int?)?.also {
                if (it > editMaxPicker.maxValue) {
                    editMaxPicker.maxValue =
                        (ceil((it.toDouble() / defaultMaxValueOffset)) * defaultMaxValueOffset).toInt()
                    editMinPicker.maxValue = editMaxPicker.maxValue
                }

                editMaxPicker.value = it
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
