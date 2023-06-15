package fr.geonature.occtax.features.nomenclature.presentation

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.NumberPicker
import android.widget.RadioButton
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.net.toUri
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import fr.geonature.commons.data.entity.Nomenclature
import fr.geonature.commons.lifecycle.observeOnce
import fr.geonature.commons.ui.adapter.AbstractListItemRecyclerViewAdapter
import fr.geonature.commons.util.KeyboardUtils.hideSoftKeyboard
import fr.geonature.occtax.R
import fr.geonature.occtax.features.nomenclature.domain.EditableField
import fr.geonature.occtax.features.record.domain.CountingRecord
import fr.geonature.occtax.features.record.domain.MediaRecord
import fr.geonature.occtax.features.record.domain.PropertyValue
import fr.geonature.occtax.ui.shared.view.setOnValueChangedListener
import java.io.File
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
            EditableField.ViewType.NONE.ordinal -> MoreViewHolder(parent)
            EditableField.ViewType.CHECKBOX.ordinal -> CheckboxViewHolder(parent)
            EditableField.ViewType.MIN_MAX.ordinal -> MinMaxViewHolder(parent)
            EditableField.ViewType.MEDIA.ordinal -> MediaViewHolder(parent)
            EditableField.ViewType.NOMENCLATURE_TYPE.ordinal -> NomenclatureTypeViewHolder(parent)
            EditableField.ViewType.RADIO.ordinal -> RadioViewHolder(parent)
            EditableField.ViewType.SELECT_SIMPLE.ordinal -> SelectSimpleViewHolder(parent)
            EditableField.ViewType.SELECT_MULTIPLE.ordinal -> SelectMultipleViewHolder(parent)
            EditableField.ViewType.TEXT_MULTIPLE.ordinal -> TextMultipleViewHolder(parent)
            EditableField.ViewType.NUMBER.ordinal -> NumberViewHolder(parent)
            else -> TextSimpleViewHolder(parent)
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
            holder.bind(it)
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
    abstract inner class AbstractViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
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

    inner class CheckboxViewHolder(parent: ViewGroup) : AbstractViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(
                R.layout.view_action_list,
                parent,
                false
            )
    ) {
        private var title: TextView = itemView.findViewById(android.R.id.title)
        private var recyclerView: RecyclerView = itemView.findViewById(android.R.id.list)
        private var adapter =
            object : AbstractListItemRecyclerViewAdapter<Pair<PropertyValue.Text, Boolean>>() {
                override fun getViewHolder(view: View, viewType: Int): AbstractViewHolder {
                    return ViewHolder(view)
                }

                override fun getLayoutResourceId(
                    position: Int,
                    item: Pair<PropertyValue.Text, Boolean>
                ): Int {
                    return R.layout.list_item_checkbox
                }

                override fun areItemsTheSame(
                    oldItems: List<Pair<PropertyValue.Text, Boolean>>,
                    newItems: List<Pair<PropertyValue.Text, Boolean>>,
                    oldItemPosition: Int,
                    newItemPosition: Int
                ): Boolean {
                    return oldItems[oldItemPosition] == newItems[newItemPosition]
                }

                override fun areContentsTheSame(
                    oldItems: List<Pair<PropertyValue.Text, Boolean>>,
                    newItems: List<Pair<PropertyValue.Text, Boolean>>,
                    oldItemPosition: Int,
                    newItemPosition: Int
                ): Boolean {
                    return oldItems[oldItemPosition] == newItems[newItemPosition]
                }

                inner class ViewHolder(itemView: View) :
                    AbstractListItemRecyclerViewAdapter<Pair<PropertyValue.Text, Boolean>>.AbstractViewHolder(itemView) {

                    private val checkbox: CheckBox =
                        itemView.findViewById<CheckBox?>(android.R.id.checkbox)
                            .apply {
                                setOnClickListener { view ->
                                    editableField?.run {
                                        value = value?.takeIf { it is PropertyValue.StringArray }
                                            ?.let { it as PropertyValue.StringArray }
                                            ?.let {
                                                it.copy(
                                                    value = it.value.filter { value -> value != view.tag.toString() }
                                                        .toTypedArray() + (
                                                        if (isChecked) arrayOf(view.tag.toString())
                                                        else arrayOf()),
                                                )
                                            } ?: PropertyValue.StringArray(
                                            code,
                                            if (isChecked) arrayOf(view.tag.toString())
                                            else arrayOf()
                                        )
                                        listener.onUpdate(this)
                                    }
                                }
                            }

                    override fun onBind(item: Pair<PropertyValue.Text, Boolean>) {
                        with(checkbox) {
                            tag = item.first.code
                            text = item.first.value ?: item.first.code
                            isChecked = item.second
                        }
                    }
                }
            }

        init {
            with(recyclerView) {
                layoutManager = GridLayoutManager(
                    context,
                    2
                )
                adapter = this@CheckboxViewHolder.adapter
            }
        }

        override fun onBind(editableField: EditableField) {
            title.text = editableField.label

            val currentValues = editableField.value
                .takeIf { it is PropertyValue.StringArray }
                ?.let { it as PropertyValue.StringArray }?.value

            adapter.setItems(editableField.values.mapNotNull { pv ->
                pv.takeIf { it is PropertyValue.Text }
                    ?.let { it as PropertyValue.Text }
                    ?.let {
                        Pair(
                            it,
                            currentValues?.contains(it.code) == true
                        )
                    }
            })
        }
    }

    inner class NomenclatureTypeViewHolder(parent: ViewGroup) : AbstractViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(
                R.layout.view_action_select_simple,
                parent,
                false
            )
    ) {
        private var edit: TextInputLayout = itemView.findViewById(android.R.id.edit)
        private val nomenclatureAdapter = NomenclatureValueAdapter(parent.context)

        init {
            (edit.editText as? AutoCompleteTextView)?.also {
                it.setAdapter(nomenclatureAdapter)
                it.setOnItemClickListener { _, _, position, _ ->
                    editableField?.run {
                        value = nomenclatureAdapter.getNomenclatureValue(position)
                            .let { nomenclature ->
                                PropertyValue.Nomenclature(
                                    code,
                                    nomenclature.defaultLabel,
                                    nomenclature.id
                                )
                            }
                        listener.onUpdate(this)
                    }
                }
            }
        }

        override fun onBind(editableField: EditableField) {
            if (!lockDefaultValues) {
                editableField.locked = false
            }

            listener.getNomenclatureValues(editableField.nomenclatureType ?: editableField.code)
                .observeOnce(listener.getLifecycleOwner()) { nomenclatureValues ->
                    nomenclatureAdapter.setNomenclatureValues(nomenclatureValues ?: listOf())
                    (edit.editText as AutoCompleteTextView?)?.text = editableField.value
                        ?.takeIf { it is PropertyValue.Nomenclature }
                        ?.let { it as PropertyValue.Nomenclature }
                        ?.let { pv -> nomenclatureValues?.firstOrNull { it.id == pv.value } }
                        ?.let { nomenclatureValue ->
                            Editable.Factory
                                .getInstance()
                                .newEditable(nomenclatureValue.defaultLabel)
                        }
                }

            with(edit) {
                startIconDrawable = if (lockDefaultValues) ResourcesCompat.getDrawable(
                    itemView.resources,
                    if (editableField.locked) R.drawable.ic_lock else R.drawable.ic_lock_open,
                    itemView.context.theme
                ) else null
                setStartIconOnClickListener {
                    if (!lockDefaultValues) return@setStartIconOnClickListener

                    editableField.locked = !editableField.locked
                    startIconDrawable = ResourcesCompat.getDrawable(
                        itemView.resources,
                        if (editableField.locked) R.drawable.ic_lock else R.drawable.ic_lock_open,
                        itemView.context.theme
                    )
                    listener.onUpdate(editableField)
                }
                hint = editableField.label ?: getDefaultLabel(editableField)
            }
        }
    }

    inner class MoreViewHolder(parent: ViewGroup) : AbstractViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(
                R.layout.view_action_more,
                parent,
                false
            )
    ) {
        private var button1: Button = itemView.findViewById(android.R.id.button1)

        override fun onBind(editableField: EditableField) {
            with(button1) {
                text = getDefaultLabel(editableField)
                setOnClickListener {
                    showAllEditableFields()
                    listener.showMore()
                }
            }
        }
    }

    inner class RadioViewHolder(parent: ViewGroup) : AbstractViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(
                R.layout.view_action_list,
                parent,
                false
            )
    ) {
        private var title: TextView = itemView.findViewById(android.R.id.title)
        private var recyclerView: RecyclerView = itemView.findViewById(android.R.id.list)
        private var adapter =
            object : AbstractListItemRecyclerViewAdapter<Pair<PropertyValue.Text, Boolean>>() {
                override fun getViewHolder(view: View, viewType: Int): AbstractViewHolder {
                    return ViewHolder(view)
                }

                override fun getLayoutResourceId(
                    position: Int,
                    item: Pair<PropertyValue.Text, Boolean>
                ): Int {
                    return R.layout.list_item_radio
                }

                override fun areItemsTheSame(
                    oldItems: List<Pair<PropertyValue.Text, Boolean>>,
                    newItems: List<Pair<PropertyValue.Text, Boolean>>,
                    oldItemPosition: Int,
                    newItemPosition: Int
                ): Boolean {
                    return oldItems[oldItemPosition] == newItems[newItemPosition]
                }

                override fun areContentsTheSame(
                    oldItems: List<Pair<PropertyValue.Text, Boolean>>,
                    newItems: List<Pair<PropertyValue.Text, Boolean>>,
                    oldItemPosition: Int,
                    newItemPosition: Int
                ): Boolean {
                    return oldItems[oldItemPosition] == newItems[newItemPosition]
                }

                inner class ViewHolder(itemView: View) :
                    AbstractListItemRecyclerViewAdapter<Pair<PropertyValue.Text, Boolean>>.AbstractViewHolder(itemView) {

                    private val radioButton: RadioButton =
                        itemView.findViewById<RadioButton?>(android.R.id.checkbox)
                            .apply {
                                setOnClickListener { view ->
                                    editableField?.run {
                                        value = (view as CompoundButton).takeIf { it.isChecked }
                                            ?.let {
                                                PropertyValue.Text(
                                                    code,
                                                    it.tag.toString()
                                                )
                                            }
                                        listener.onUpdate(this)
                                    }

                                    if (isChecked) {
                                        setItems(items.map {
                                            Pair(
                                                it.first,
                                                it.first.code == view.tag.toString()
                                            )
                                        })
                                    }
                                }
                            }

                    override fun onBind(item: Pair<PropertyValue.Text, Boolean>) {
                        with(radioButton) {
                            tag = item.first.code
                            text = item.first.value ?: item.first.code
                            isChecked = item.second
                        }
                    }
                }
            }

        init {
            with(recyclerView) {
                layoutManager = GridLayoutManager(
                    context,
                    2
                )
                adapter = this@RadioViewHolder.adapter
            }
        }

        override fun onBind(editableField: EditableField) {
            title.text = editableField.label

            adapter.setItems(editableField.values.mapNotNull { pv ->
                pv.takeIf { it is PropertyValue.Text }
                    ?.let { it as PropertyValue.Text }
                    ?.let {
                        Pair(
                            it,
                            editableField.value?.let { value -> value is PropertyValue.Text && value.value == it.code }
                                ?: false
                        )
                    }
            })
        }
    }

    inner class SelectSimpleViewHolder(parent: ViewGroup) : AbstractViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(
                R.layout.view_action_select_simple,
                parent,
                false
            )
    ) {
        private var edit: TextInputLayout = itemView.findViewById(android.R.id.edit)
        private var propertyValueTextAdapter = PropertyValueTextAdapter(parent.context)

        init {
            (edit.editText as? AutoCompleteTextView)?.also {
                it.setAdapter(propertyValueTextAdapter)
                it.setOnItemClickListener { _, _, position, _ ->
                    editableField?.run {
                        value = PropertyValue.Text(
                            code = code,
                            propertyValueTextAdapter.getPropertyValue(position)
                                .takeIf { pv -> pv is PropertyValue.Text }
                                ?.let { pv -> pv as PropertyValue.Text }?.code
                        )
                        listener.onUpdate(this)
                    }
                }
            }
        }

        override fun onBind(editableField: EditableField) {
            if (!lockDefaultValues) {
                editableField.locked = false
            }

            propertyValueTextAdapter.setPropertyValues(editableField.values)

            with(edit) {
                startIconDrawable = if (lockDefaultValues) ResourcesCompat.getDrawable(
                    itemView.resources,
                    if (editableField.locked) R.drawable.ic_lock else R.drawable.ic_lock_open,
                    itemView.context.theme
                ) else null
                setStartIconOnClickListener {
                    if (!lockDefaultValues) return@setStartIconOnClickListener

                    editableField.locked = !editableField.locked
                    startIconDrawable = ResourcesCompat.getDrawable(
                        itemView.resources,
                        if (editableField.locked) R.drawable.ic_lock else R.drawable.ic_lock_open,
                        itemView.context.theme
                    )
                    listener.onUpdate(editableField)
                }
                hint = editableField.label ?: getDefaultLabel(editableField)
                (editText as? AutoCompleteTextView)?.apply {
                    text = editableField.value
                        ?.takeIf { it is PropertyValue.Text }
                        ?.let { it as PropertyValue.Text }
                        ?.let { pv -> editableField.values.firstOrNull { it is PropertyValue.Text && it.code == pv.value } }
                        ?.let { it as PropertyValue.Text }
                        ?.let {
                            Editable.Factory
                                .getInstance()
                                .newEditable(it.value ?: it.code)
                        }
                }
            }
        }
    }

    inner class SelectMultipleViewHolder(parent: ViewGroup) : AbstractViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(
                R.layout.view_action_select_simple,
                parent,
                false
            )
    ) {
        private var edit: TextInputLayout = itemView.findViewById(android.R.id.edit)

        init {
            (edit.editText as? AutoCompleteTextView)?.also { editText ->
                editText.setOnClickListener {
                    editableField?.also {
                        showSelectionDialog(
                            itemView.context,
                            it,
                            editText
                        )
                    }
                }
                edit.setEndIconOnClickListener {
                    editableField?.also {
                        showSelectionDialog(
                            itemView.context,
                            it,
                            editText
                        )
                    }
                }
            }
        }

        override fun onBind(editableField: EditableField) {
            if (!lockDefaultValues) {
                editableField.locked = false
            }

            with(edit) {
                startIconDrawable = if (lockDefaultValues) ResourcesCompat.getDrawable(
                    itemView.resources,
                    if (editableField.locked) R.drawable.ic_lock else R.drawable.ic_lock_open,
                    itemView.context.theme
                ) else null
                setStartIconOnClickListener {
                    if (!lockDefaultValues) return@setStartIconOnClickListener

                    editableField.locked = !editableField.locked
                    startIconDrawable = ResourcesCompat.getDrawable(
                        itemView.resources,
                        if (editableField.locked) R.drawable.ic_lock else R.drawable.ic_lock_open,
                        itemView.context.theme
                    )
                    listener.onUpdate(editableField)
                }
                hint = editableField.label ?: getDefaultLabel(editableField)
                (editText as? AutoCompleteTextView)?.apply {
                    text = editableField.value
                        ?.takeIf { it is PropertyValue.StringArray }
                        ?.let { it as PropertyValue.StringArray }
                        ?.let { stringArray ->
                            editableField.values.mapNotNull { pv ->
                                pv.takeIf { it is PropertyValue.Text }
                                    ?.let { it as PropertyValue.Text }
                                    ?.let { text ->
                                        stringArray.value.firstOrNull { it == text.code }
                                            ?.let { text }
                                    }
                            }
                        }
                        ?.joinToString(", ") { it.value ?: it.code }
                        ?.let {
                            Editable.Factory
                                .getInstance()
                                .newEditable(it)
                        }
                }
            }
        }

        private fun showSelectionDialog(
            context: Context,
            editableField: EditableField,
            editText: EditText
        ) {
            val items = editableField.values.filterIsInstance<PropertyValue.Text>()
                .map { it.value ?: it.code }
                .toTypedArray()
            val selectedItems = editableField.values.filterIsInstance<PropertyValue.Text>()
                .associateWith { false }
                .let {
                    val selectedItems =
                        editableField.value?.takeIf { v -> v is PropertyValue.StringArray }
                            ?.let { v -> v as PropertyValue.StringArray }?.value ?: emptyArray()
                    it.map { item -> item.key to selectedItems.any { selectedItem -> selectedItem == item.key.code } }
                }
                .map { it.second }
                .toBooleanArray()

            AlertDialog.Builder(context)
                .setTitle(
                    editableField.label ?: getDefaultLabel(editableField)
                )
                .setNegativeButton(context.getString(R.string.alert_dialog_cancel)) { _, _ ->
                    // nothing to do...
                }
                .setPositiveButton(context.getString(R.string.alert_dialog_ok)) { _, _ ->
                    PropertyValue.StringArray(
                        code = editableField.code,
                        value = editableField.values.filterIsInstance<PropertyValue.Text>()
                            .mapIndexed { index, v ->
                                v to selectedItems[index]
                            }
                            .filter {
                                it.second
                            }
                            .map { it.first.code }
                            .toTypedArray()
                    )
                        .also { propertyValue ->
                            editableField.value = propertyValue
                            editText.text = propertyValue
                                .let { stringArray ->
                                    editableField.values.mapNotNull { pv ->
                                        pv.takeIf { it is PropertyValue.Text }
                                            ?.let { it as PropertyValue.Text }
                                            ?.let { text ->
                                                stringArray.value.firstOrNull { it == text.code }
                                                    ?.let { text }
                                            }
                                    }
                                }
                                .joinToString(", ") { it.value ?: it.code }
                                .let {
                                    Editable.Factory
                                        .getInstance()
                                        .newEditable(it)
                                }
                        }
                    listener.onUpdate(editableField)
                }
                .setMultiChoiceItems(
                    items,
                    selectedItems
                ) { _, which, checked ->
                    selectedItems[which] = checked
                }
                .show()
        }
    }

    open inner class TextSimpleViewHolder(parent: ViewGroup) : AbstractViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(
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
                editableField?.run {
                    value = PropertyValue.Text(
                        code,
                        s?.toString()
                            ?.ifEmpty { null }
                            ?.ifBlank { null }
                    )
                    listener.onUpdate(this)
                }
            }
        }

        init {
            with(edit) {
                editText?.addTextChangedListener(textWatcher)
                setOnFocusChangeListener { v, hasFocus ->
                    if (!hasFocus) {
                        // workaround to force hide the soft keyboard
                        hideSoftKeyboard(v)
                    }
                }
            }
        }

        override fun onBind(editableField: EditableField) {
            if (!lockDefaultValues) {
                editableField.locked = false
            }

            with(edit) {
                startIconDrawable = if (lockDefaultValues) ResourcesCompat.getDrawable(
                    itemView.resources,
                    if (editableField.locked) R.drawable.ic_lock else R.drawable.ic_lock_open,
                    itemView.context.theme
                ) else null
                setStartIconOnClickListener {
                    if (!lockDefaultValues) return@setStartIconOnClickListener

                    editableField.locked = !editableField.locked
                    startIconDrawable = ResourcesCompat.getDrawable(
                        itemView.resources,
                        if (editableField.locked) R.drawable.ic_lock else R.drawable.ic_lock_open,
                        itemView.context.theme
                    )
                    listener.onUpdate(editableField)
                }
                hint = getDefaultLabel(editableField)
            }

            editableField.value
                .takeIf { it is PropertyValue.Text && !it.isEmpty() }
                ?.let { it as PropertyValue.Text }
                ?.also {
                    edit.editText?.removeTextChangedListener(textWatcher)
                    edit.editText?.text = Editable.Factory.getInstance()
                        .newEditable(it.value)
                    edit.editText?.addTextChangedListener(textWatcher)
                }
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

    inner class NumberViewHolder(parent: ViewGroup) : TextSimpleViewHolder(parent) {
        init {
            edit.editText?.apply {
                inputType = InputType.TYPE_CLASS_NUMBER
            }
        }
    }

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

    inner class MediaViewHolder(parent: ViewGroup) : AbstractViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(
                R.layout.view_action_media,
                parent,
                false
            )
    ) {
        private var title: TextView = itemView.findViewById(android.R.id.title)
        private var recyclerView: RecyclerView = itemView.findViewById(android.R.id.list)
        private var adapter: MediaAdapter = MediaAdapter()

        init {
            with(recyclerView) {
                layoutManager = GridLayoutManager(
                    context,
                    2
                )
                adapter = this@MediaViewHolder.adapter
            }
        }

        override fun onBind(editableField: EditableField) {
            title.text = editableField.label ?: getDefaultLabel(editableField)
            adapter.setItems(editableField.value
                .takeIf { it is PropertyValue.Media }
                ?.let { it as PropertyValue.Media }?.value
                ?.filterIsInstance<MediaRecord.File>()
                ?.map { it.path }
                ?.mapNotNull { runCatching { File(it) }.getOrNull() }
                ?: emptyList())
        }

        private inner class MediaAdapter :
            RecyclerView.Adapter<MediaAdapter.AbstractViewHolder>() {

            private val items = mutableListOf<File>()

            init {
                this.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                    override fun onChanged() {
                        super.onChanged()

                        onUpdate()
                    }

                    override fun onItemRangeChanged(
                        positionStart: Int,
                        itemCount: Int
                    ) {
                        super.onItemRangeChanged(
                            positionStart,
                            itemCount
                        )

                        onUpdate()
                    }

                    override fun onItemRangeInserted(
                        positionStart: Int,
                        itemCount: Int
                    ) {
                        super.onItemRangeInserted(
                            positionStart,
                            itemCount
                        )

                        onUpdate()
                    }

                    override fun onItemRangeRemoved(
                        positionStart: Int,
                        itemCount: Int
                    ) {
                        super.onItemRangeRemoved(
                            positionStart,
                            itemCount
                        )

                        onUpdate()
                    }
                })
            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AbstractViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(
                        viewType,
                        parent,
                        false
                    )

                return when (viewType) {
                    R.layout.list_item_media_add -> AddImageViewHolder(view)
                    else -> ImageViewHolder(view)
                }
            }

            override fun onBindViewHolder(holder: AbstractViewHolder, position: Int) {
                holder.onBind(if ((itemCount - 1) == position) null else items[position])
            }

            override fun getItemCount(): Int {
                return items.size + 1
            }

            override fun getItemViewType(position: Int): Int {
                return if ((itemCount - 1) == position) R.layout.list_item_media_add
                else R.layout.list_item_media_thumbnail
            }

            fun setItems(items: List<File>) {
                val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                    override fun getOldListSize(): Int {
                        return this@MediaAdapter.items.size
                    }

                    override fun getNewListSize(): Int {
                        return items.size
                    }

                    override fun areItemsTheSame(
                        oldItemPosition: Int,
                        newItemPosition: Int
                    ): Boolean {
                        return this@MediaAdapter.items[oldItemPosition] == items[newItemPosition]
                    }

                    override fun areContentsTheSame(
                        oldItemPosition: Int,
                        newItemPosition: Int
                    ): Boolean {
                        return this@MediaAdapter.items[oldItemPosition] == items[newItemPosition]
                    }
                })

                with(this.items) {
                    clear()
                    addAll(items)
                }

                diffResult.dispatchUpdatesTo(this)
            }

            fun onUpdate() {
                editableField?.also {
                    listener.onUpdate(it.apply {
                        value = PropertyValue.Media(
                            it.code,
                            items.map { file -> MediaRecord.File(file.absolutePath) }
                                .toTypedArray()
                        )
                    })
                }
            }

            abstract inner class AbstractViewHolder(itemView: View) :
                RecyclerView.ViewHolder(itemView) {

                abstract fun onBind(file: File?)
            }

            inner class ImageViewHolder(itemView: View) : AbstractViewHolder(itemView) {
                override fun onBind(file: File?) {
                    itemView.findViewById<ShapeableImageView>(R.id.image)
                        .apply {
                            setImageURI(file?.toUri())
                            setOnClickListener {
                                file?.absolutePath?.also {
                                    listener.onMediaSelected(MediaRecord.File(it))
                                }
                            }
                            setOnLongClickListener {
                                val currentPosition = items.indexOf(file)

                                this@MediaAdapter.setItems(items.filter { it.absolutePath != file?.absolutePath })

                                makeSnackbar(itemView.context.getString(R.string.counting_media_deleted))?.setAction(R.string.counting_media_action_undo) {
                                    file?.also {
                                        this@MediaAdapter.setItems(
                                            items.toMutableList()
                                                .apply {
                                                    add(
                                                        currentPosition,
                                                        it
                                                    )
                                                })
                                    }
                                }
                                    ?.show()

                                true
                            }
                        }
                }
            }

            inner class AddImageViewHolder(itemView: View) : AbstractViewHolder(itemView) {
                override fun onBind(file: File?) {
                    itemView.findViewById<MaterialButton>(android.R.id.button1)
                        .setOnClickListener {
                            editableField?.code?.also {
                                listener.onAddMedia(it)
                            }
                        }
                }
            }
        }

        private fun makeSnackbar(
            text: CharSequence,
        ): Snackbar? {
            val view = listener.getCoordinatorLayout() ?: return null

            return Snackbar.make(
                view,
                text,
                BaseTransientBottomBar.LENGTH_LONG
            )
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