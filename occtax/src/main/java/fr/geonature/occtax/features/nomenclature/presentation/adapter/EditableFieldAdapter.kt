package fr.geonature.occtax.features.nomenclature.presentation.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import fr.geonature.commons.data.entity.Nomenclature
import fr.geonature.occtax.R
import fr.geonature.occtax.features.nomenclature.domain.EditableField
import fr.geonature.occtax.features.record.domain.CountingRecord
import fr.geonature.occtax.features.record.domain.MediaRecord
import fr.geonature.occtax.features.record.domain.PropertyValue
import fr.geonature.occtax.ui.shared.view.setOnValueChangedListener
import kotlin.math.ceil

/**
 * Default RecyclerView Adapter about [EditableField].
 *
 * @author S. Grimault
 */
class EditableFieldAdapter(private val listener: OnEditableFieldAdapter) :
    RecyclerView.Adapter<EditableFieldAdapter.AbstractViewHolder>() {

    private val availableEditableFields = mutableListOf<EditableField>()
    private val selectedEditableFields = mutableListOf<EditableField>()
    private var showAll = false
    private var lockDefaultValues = false

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
            EditableField.ViewType.NONE.ordinal -> ShowMoreViewHolder(
                parent,
                object : ShowMoreViewHolder.OnMoreViewHolderListener {
                    override fun showMore() {
                        showAllEditableFields()
                        listener.showMore()
                    }
                })

            EditableField.ViewType.CHECKBOX.ordinal -> CheckboxViewHolder(
                parent,
                listener
            )

            EditableField.ViewType.MIN_MAX.ordinal -> MinMaxViewHolder(parent)
            EditableField.ViewType.MEDIA.ordinal -> MediaViewHolder(
                parent,
                listener
            )

            EditableField.ViewType.NOMENCLATURE_TYPE.ordinal -> NomenclatureTypeViewHolder(
                parent,
                listener
            )

            EditableField.ViewType.RADIO.ordinal -> RadioViewHolder(
                parent,
                listener
            )

            EditableField.ViewType.SELECT_SIMPLE.ordinal -> SelectSimpleViewHolder(
                parent,
                listener
            )

            EditableField.ViewType.SELECT_MULTIPLE.ordinal -> SelectMultipleViewHolder(
                parent,
                listener
            )

            EditableField.ViewType.TEXT_MULTIPLE.ordinal -> TextMultipleViewHolder(
                parent,
                listener
            )

            EditableField.ViewType.NUMBER.ordinal -> NumberViewHolder(
                parent,
                listener
            )

            else -> TextSimpleViewHolder(
                parent,
                listener
            )
        }
    }

    override fun onBindViewHolder(holder: AbstractViewHolder, position: Int) {
        selectedEditableFields.fold(listOf<EditableField>()) { acc, editableField ->
            acc + if (editableField.viewType == EditableField.ViewType.MIN_MAX && acc.any { it.viewType == EditableField.ViewType.MIN_MAX }) listOf() else listOf(editableField)
        }
            .sortedWith { o1, o2 ->
                val i1 = selectedEditableFields.indexOfFirst { it == o1 }
                val i2 = selectedEditableFields.indexOfFirst { it == o2 }

                when {
                    i1 == -1 -> 1
                    i2 == -1 -> -1
                    else -> i1 - i2
                }
            }[position].also {
            when (holder) {
                is AbstractLockableViewHolder -> holder.bind(
                    it,
                    lockDefaultValues
                )

                else -> holder.bind(it)
            }
        }
    }

    override fun getItemCount(): Int {
        return selectedEditableFields.filter { it.viewType != EditableField.ViewType.MIN_MAX }.size +
            selectedEditableFields.filter { it.viewType == EditableField.ViewType.MIN_MAX }.size.coerceAtMost(1)
    }

    override fun getItemViewType(position: Int): Int {
        return selectedEditableFields.fold(listOf<EditableField>()) { acc, editableField ->
            acc + if (editableField.viewType == EditableField.ViewType.MIN_MAX && acc.any { it.viewType == EditableField.ViewType.MIN_MAX }) listOf() else listOf(editableField)
        }
            .sortedWith { o1, o2 ->
                val i1 = selectedEditableFields.indexOfFirst { it == o1 }
                val i2 = selectedEditableFields.indexOfFirst { it == o2 }

                when {
                    i1 == -1 -> 1
                    i2 == -1 -> -1
                    else -> i1 - i2
                }
            }[position].viewType.ordinal
    }

    fun bind(
        editableFields: List<EditableField>,
        vararg propertyValue: PropertyValue
    ) {
        availableEditableFields.clear()
        availableEditableFields.addAll(
            editableFields.filter { it.visible }
                .map {
                    it.copy(value = propertyValue.firstOrNull { propertyValue -> propertyValue.toPair().first == it.code }
                        ?: it.value)
                }
        )

        if (showAll) showAllEditableFields(notify = true) else showDefaultEditableFields(notify = true)
    }

    fun showDefaultEditableFields(notify: Boolean = false) {
        showAll = false

        if (availableEditableFields.isEmpty()) return

        availableEditableFields.filter { it.default }
            .run {
                if (isEmpty()) {
                    // nothing to show by default: show everything
                    showAllEditableFields(notify)
                } else {
                    setEditableFields(
                        // show 'MORE' button only if we have some other editable field to show
                        this + if (this.size < availableEditableFields.size) listOf(
                            EditableField(
                                type = EditableField.Type.INFORMATION,
                                code = "MORE",
                                viewType = EditableField.ViewType.NONE,
                                visible = true
                            )
                        ) else emptyList()
                    )
                }
            }
    }

    fun showAllEditableFields(notify: Boolean = false) {
        showAll = true
        setEditableFields(
            availableEditableFields,
            notify
        )
    }

    fun lockDefaultValues(lock: Boolean = false) {
        lockDefaultValues = lock
    }

    fun setPropertyValues(vararg propertyValue: PropertyValue) {
        availableEditableFields.map {
            it.copy(value = propertyValue.firstOrNull { propertyValue -> propertyValue.toPair().first == it.code }
                ?: it.value)
        }
            .also {
                availableEditableFields.clear()
                availableEditableFields.addAll(it)
            }

        if (showAll) showAllEditableFields(notify = true) else showDefaultEditableFields(notify = true)
    }

    private fun setEditableFields(
        editableFields: List<EditableField>,
        notify: Boolean = false
    ) {
        val oldKeys = selectedEditableFields.map { it.code }
        val newKeys = editableFields.map { it.code }

        if (notify && oldKeys.isEmpty() && newKeys.isEmpty()) {
            listener.showEmptyTextView(true)

            return
        }

        val checkMedia =
            selectedEditableFields.firstOrNull { it.viewType == EditableField.ViewType.MEDIA }?.value == editableFields.firstOrNull { it.viewType == EditableField.ViewType.MEDIA }?.value

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
                checkMedia &&
                    (oldKeys.elementAtOrNull(oldItemPosition)
                        ?.let { code -> selectedEditableFields.firstOrNull { it.code == code } }
                        ?.value?.toPair()?.second == newKeys.elementAtOrNull(newItemPosition)
                        ?.let { code -> editableFields.firstOrNull { it.code == code } }
                        ?.value?.toPair()?.second)
        })

        selectedEditableFields.clear()
        selectedEditableFields.addAll(editableFields)

        diffResult.dispatchUpdatesTo(this)
    }

    /**
     * Base [ViewHolder][RecyclerView.ViewHolder] used by this adapter.
     */
    abstract class AbstractViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var editableField: EditableField? = null

        fun bind(editableField: EditableField) {
            this.editableField = editableField

            onBind(editableField)
        }

        abstract fun onBind(editableField: EditableField)

        /**
         * Build the default label for given editable field as fallback.
         */
        @SuppressLint("DiscouragedApi")
        fun getDefaultLabel(editableField: EditableField): String {
            return editableField.label ?: itemView.resources.getIdentifier(
                "nomenclature_${editableField.code.lowercase()}",
                "string",
                itemView.context.packageName
            )
                .takeIf { it > 0 }
                ?.let { itemView.context.getString(it) } ?: editableField.code
        }
    }

    /**
     * Base [ViewHolder][RecyclerView.ViewHolder] used by this adapter with the option of locking
     * the [EditableField].
     */
    abstract class AbstractLockableViewHolder(itemView: View) : AbstractViewHolder(itemView) {

        fun bind(editableField: EditableField, lockDefaultValues: Boolean = false) {
            this.editableField = editableField

            onBind(
                editableField,
                lockDefaultValues
            )
        }

        override fun onBind(editableField: EditableField) {
            onBind(
                editableField,
                false
            )
        }

        abstract fun onBind(editableField: EditableField, lockDefaultValues: Boolean = false)
    }

    /**
     * [EditableFieldAdapter] view holder representing a bounded numerical value.
     */
    inner class MinMaxViewHolder(parent: ViewGroup) : AbstractViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(
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

        private var minEditableField: EditableField? = null
        private var maxEditableField: EditableField? = null

        init {
            with(editMinPicker) {
                minValue = 0
                maxValue = defaultMaxValueOffset
                setOnValueChangedListener(defaultMaxValueOffset) { oldValue, newValue ->
                    if (editMaxPicker.value < newValue) {
                        editMaxPicker.maxValue = editMinPicker.maxValue
                        editMaxPicker.value = newValue
                    }

                    if (editMaxPicker.value == oldValue) {
                        editMaxPicker.value = newValue
                    }

                    minEditableField?.also { editableField ->
                        editableField.value = PropertyValue.Number(
                            editableField.code,
                            newValue
                        )
                        listener.onUpdate(editableField)
                    }
                    maxEditableField?.also { editableField ->
                        editableField.value = PropertyValue.Number(
                            editableField.code,
                            editMaxPicker.value
                        )
                        listener.onUpdate(editableField)
                    }
                }
            }

            with(editMaxPicker) {
                minValue = 0
                maxValue = defaultMaxValueOffset
                setOnValueChangedListener(defaultMaxValueOffset) { _, newValue ->
                    editMinPicker.maxValue = editMaxPicker.maxValue

                    if (editMinPicker.value > newValue) editMinPicker.value = newValue

                    minEditableField?.also { editableField ->
                        editableField.value = PropertyValue.Number(
                            editableField.code,
                            editMinPicker.value
                        )
                        listener.onUpdate(editableField)
                    }
                    maxEditableField?.also { editableField ->
                        editableField.value = PropertyValue.Number(
                            editableField.code,
                            newValue
                        )
                        listener.onUpdate(editableField)
                    }
                }
            }
        }

        override fun onBind(editableField: EditableField) {
            minEditableField =
                selectedEditableFields.firstOrNull { it.viewType == EditableField.ViewType.MIN_MAX && it.code == CountingRecord.MIN_KEY }
            maxEditableField =
                selectedEditableFields.firstOrNull { it.viewType == EditableField.ViewType.MIN_MAX && it.code == CountingRecord.MAX_KEY }

            with(if (minEditableField != null) View.VISIBLE else View.GONE) {
                editMinLabel.visibility = this
                editMinPicker.visibility = this
            }

            with(if (maxEditableField != null) View.VISIBLE else View.GONE) {
                editMaxLabel.visibility = this
                editMaxPicker.visibility = this
            }

            minEditableField?.value?.takeIf { it is PropertyValue.Number }
                ?.let { it as PropertyValue.Number }?.value?.toInt()
                ?.also {
                    if (it > editMinPicker.maxValue) {
                        editMinPicker.maxValue =
                            (ceil((it.toDouble() / defaultMaxValueOffset)) * defaultMaxValueOffset).toInt()
                        editMaxPicker.maxValue = editMinPicker.maxValue
                    }

                    editMinPicker.value = it
                }

            maxEditableField?.value?.takeIf { it is PropertyValue.Number }
                ?.let { it as PropertyValue.Number }?.value?.toInt()
                ?.also {
                    if (it > editMaxPicker.maxValue) {
                        editMaxPicker.maxValue =
                            (ceil((it.toDouble() / defaultMaxValueOffset)) * defaultMaxValueOffset).toInt()
                        editMinPicker.maxValue = editMaxPicker.maxValue
                    }

                    editMaxPicker.value = it
                }
        }
    }

    /**
     * Callback used by [EditableFieldAdapter].
     */
    interface OnEditableFieldAdapter {

        fun getLifecycleOwner(): LifecycleOwner

        fun getCoordinatorLayout(): CoordinatorLayout?

        /**
         * Whether to show an empty text view when data changed.
         */
        fun showEmptyTextView(show: Boolean)

        /**
         * Called when the 'more' action button has been clicked.
         */
        fun showMore()

        /**
         * Requests showing all available nomenclature values from given nomenclature type.
         */
        fun getNomenclatureValues(nomenclatureTypeMnemonic: String): LiveData<List<Nomenclature>>

        /**
         * Called when an [EditableField] has been updated.
         *
         * @param editableField the [EditableField] updated
         */
        fun onUpdate(editableField: EditableField)

        /**
         * Called when we want to add media.
         */
        fun onAddMedia(nomenclatureTypeMnemonic: String)

        /**
         * Called when we want to show given media.
         */
        fun onMediaSelected(mediaRecord: MediaRecord.File)
    }
}