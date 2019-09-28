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
import java.util.Locale


/**
 * Default RecyclerView Adapter used by [InformationFragment].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class NomenclatureTypesRecyclerViewAdapter(private val listener: OnNomenclatureTypesRecyclerViewAdapterListener) : RecyclerView.Adapter<NomenclatureTypesRecyclerViewAdapter.AbstractCardViewHolder>() {

    private val mnemonicFilter = arrayOf(Pair("METH_OBS",
                                              ViewType.NOMENCLATURE_TYPE),
                                         Pair("ETA_BIO",
                                              ViewType.NOMENCLATURE_TYPE),
                                         Pair("METH_DETERMIN",
                                              ViewType.NOMENCLATURE_TYPE),
                                         Pair("DETERMINER",
                                              ViewType.TEXT_SIMPLE),
                                         Pair("STATUT_BIO",
                                              ViewType.NOMENCLATURE_TYPE),
                                         Pair("NATURALITE",
                                              ViewType.NOMENCLATURE_TYPE),
                                         Pair("PREUVE_EXIST",
                                              ViewType.NOMENCLATURE_TYPE),
                                         Pair("COMMENT",
                                              ViewType.TEXT_MULTIPLE))
    private val defaultMnemonicFilter = mnemonicFilter.slice(IntRange(0,
                                                                      1))

    private val availableNomenclatureTypes = mutableListOf<Pair<String, ViewType>>()
    private val nomenclatureTypes = mutableListOf<Pair<String, ViewType>>()
    private var showAllNomenclatureTypes = false

    private val onClickListener: View.OnClickListener

    init {
        onClickListener = View.OnClickListener { v ->
            val mnemonic = v.tag as String
            listener.onAction(mnemonic)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): AbstractCardViewHolder {
        return when (ViewType.values()[viewType]) {
            ViewType.MORE -> MoreViewHolder(parent)
            ViewType.TEXT_SIMPLE -> TextSimpleViewHolder(parent)
            ViewType.TEXT_MULTIPLE -> TextMultipleViewHolder(parent)
            else -> NomenclatureTypeViewHolder(parent)
        }
    }

    override fun getItemCount(): Int {
        return nomenclatureTypes.size
    }

    override fun onBindViewHolder(holder: AbstractCardViewHolder,
                                  position: Int) {
        holder.bind(nomenclatureTypes[position].first)
    }

    override fun getItemViewType(position: Int): Int {
        return nomenclatureTypes[position].second.ordinal
    }

    fun bind(cursor: Cursor?) {
        availableNomenclatureTypes.clear()

        cursor?.run {
            if (this.isClosed) return@run

            this.moveToFirst()

            while (!this.isAfterLast) {
                NomenclatureType.fromCursor(this)
                    ?.run {
                        val validNomenclatureType = mnemonicFilter.find { it.first == this.mnemonic }
                        if (validNomenclatureType != null) {
                            availableNomenclatureTypes.add(validNomenclatureType)
                        }
                    }
                cursor.moveToNext()
            }
        }
        availableNomenclatureTypes.addAll(mnemonicFilter.filter { it.second != ViewType.NOMENCLATURE_TYPE })

        availableNomenclatureTypes.sortWith(Comparator { o1, o2 ->
            val i1 = mnemonicFilter.indexOfFirst { it.first == o1.first }
            val i2 = mnemonicFilter.indexOfFirst { it.first == o2.first }

            when {
                i1 == -1 -> 1
                i2 == -1 -> -1
                else -> i1 - i2
            }
        })

        if (showAllNomenclatureTypes) {
            setNomenclatureTypes(availableNomenclatureTypes)
        }
        else {
            val defaultNomenclatureTypes = availableNomenclatureTypes.filter { availableNomenclatureType -> defaultMnemonicFilter.any { it.first == availableNomenclatureType.first } }

            // add MORE ViewType if default nomenclature types are presents
            setNomenclatureTypes(if (defaultNomenclatureTypes.size == defaultMnemonicFilter.size) {
                listOf(*defaultNomenclatureTypes.toTypedArray(),
                       Pair("MORE",
                            ViewType.MORE))
            }
                                 else {
                availableNomenclatureTypes
            })
        }
    }

    private fun setNomenclatureTypes(nomenclatureTypes: List<Pair<String, ViewType>>) {
        if (this.nomenclatureTypes.isEmpty()) {
            this.nomenclatureTypes.addAll(nomenclatureTypes)

            if (this.nomenclatureTypes.isNotEmpty()) {
                notifyItemRangeInserted(0,
                                        this.nomenclatureTypes.size)
            }

            return
        }

        if (nomenclatureTypes.isEmpty()) {
            clear()

            return
        }

        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {

            override fun getOldListSize(): Int {
                return this@NomenclatureTypesRecyclerViewAdapter.nomenclatureTypes.size
            }

            override fun getNewListSize(): Int {
                return nomenclatureTypes.size
            }

            override fun areItemsTheSame(oldItemPosition: Int,
                                         newItemPosition: Int): Boolean {
                return this@NomenclatureTypesRecyclerViewAdapter.nomenclatureTypes[oldItemPosition].first == nomenclatureTypes[newItemPosition].first
            }

            override fun areContentsTheSame(oldItemPosition: Int,
                                            newItemPosition: Int): Boolean {
                return this@NomenclatureTypesRecyclerViewAdapter.nomenclatureTypes[oldItemPosition] == nomenclatureTypes[newItemPosition]
            }
        })
        this.nomenclatureTypes.clear()
        this.nomenclatureTypes.addAll(nomenclatureTypes)
        diffResult.dispatchUpdatesTo(this)
    }

    private fun clear() {
        this.nomenclatureTypes.clear()
        notifyDataSetChanged()
    }

    abstract inner class AbstractCardViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(fr.geonature.occtax.R.layout.card_view,
                                                                                                                                         parent,
                                                                                                                                         false)) {
        internal val contentView: View
        internal var mnemonic: String? = null

        init {
            contentView = LayoutInflater.from(itemView.context)
                .inflate(this.getLayoutResourceId(),
                         itemView as FrameLayout,
                         true)
        }

        fun bind(mnemonic: String) {
            this.mnemonic = mnemonic

            onBind(mnemonic)
        }

        @LayoutRes
        abstract fun getLayoutResourceId(): Int

        abstract fun onBind(mnemonic: String)

        fun getNomenclatureTypeLabel(mnemonic: String): String {
            val resourceId = contentView.resources.getIdentifier("nomenclature_${mnemonic.toLowerCase(Locale.getDefault())}",
                                                                 "string",
                                                                 contentView.context.packageName)

            return if (resourceId == 0) mnemonic else contentView.context.getString(resourceId)
        }
    }

    inner class NomenclatureTypeViewHolder(parent: ViewGroup) : AbstractCardViewHolder(parent) {
        private var title: TextView = contentView.findViewById(android.R.id.title)
        private var text1: TextView = contentView.findViewById(android.R.id.text1)
        private var button1: Button = contentView.findViewById(android.R.id.button1)

        override fun getLayoutResourceId(): Int {
            return fr.geonature.occtax.R.layout.view_action_nomenclature_type
        }

        override fun onBind(mnemonic: String) {
            title.text = getNomenclatureTypeLabel(mnemonic)
            button1.setOnClickListener(onClickListener)
        }
    }

    inner class MoreViewHolder(parent: ViewGroup) : AbstractCardViewHolder(parent) {
        private var title: TextView = contentView.findViewById(android.R.id.title)
        private var button1: Button = contentView.findViewById(android.R.id.button1)

        override fun getLayoutResourceId(): Int {
            return fr.geonature.occtax.R.layout.view_action_more
        }

        override fun onBind(mnemonic: String) {
            title.text = getNomenclatureTypeLabel(mnemonic)
            button1.setOnClickListener {
                showAllNomenclatureTypes = true
                setNomenclatureTypes(availableNomenclatureTypes)
            }
        }
    }

    open inner class TextSimpleViewHolder(parent: ViewGroup) : AbstractCardViewHolder(parent) {
        private var title: TextView = contentView.findViewById(android.R.id.title)
        internal var edit: EditText = contentView.findViewById(android.R.id.edit)

        init {
            edit.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?,
                                               start: Int,
                                               count: Int,
                                               after: Int) {
                }

                override fun onTextChanged(s: CharSequence?,
                                           start: Int,
                                           before: Int,
                                           count: Int) {
                }

                override fun afterTextChanged(s: Editable?) {
                    val mnemonic = mnemonic ?: return

                    listener.onEdit(mnemonic,
                                    s?.toString()?.ifEmpty { null }?.ifBlank { null })
                }
            })
        }

        override fun getLayoutResourceId(): Int {
            return fr.geonature.occtax.R.layout.view_action_edit_text
        }

        override fun onBind(mnemonic: String) {
            title.text = getNomenclatureTypeLabel(mnemonic)
            edit.hint = getEditTextHint(mnemonic)
        }

        private fun getEditTextHint(mnemonic: String): String {
            val resourceId = contentView.resources.getIdentifier("information_${mnemonic.toLowerCase(Locale.getDefault())}_hint",
                                                                 "string",
                                                                 contentView.context.packageName)
            return if (resourceId == 0) "" else contentView.context.getString(resourceId)
        }
    }

    inner class TextMultipleViewHolder(parent: ViewGroup) : TextSimpleViewHolder(parent) {
        init {
            edit.apply {
                setSingleLine(false)
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
                minLines = 2
                maxLines = 4
            }
        }
    }

    enum class ViewType {
        NOMENCLATURE_TYPE,
        MORE,
        TEXT_SIMPLE,
        TEXT_MULTIPLE
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
         * Called when a value has been directly edited for a given nomenclature type.
         *
         * @param nomenclatureTypeMnemonic the selected nomenclature type
         * @param value the corresponding value (may be `null`)
         */
        fun onEdit(nomenclatureTypeMnemonic: String,
                   value: String?)
    }
}