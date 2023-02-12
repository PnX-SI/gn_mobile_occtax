package fr.geonature.occtax.features.nomenclature.presentation

import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.NumberPicker
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
import fr.geonature.commons.util.KeyboardUtils.hideSoftKeyboard
import fr.geonature.occtax.R
import fr.geonature.occtax.features.nomenclature.domain.EditableNomenclatureType
import fr.geonature.occtax.features.record.domain.CountingRecord
import fr.geonature.occtax.features.record.domain.MediaRecord
import fr.geonature.occtax.features.record.domain.PropertyValue
import fr.geonature.occtax.ui.shared.view.setOnValueChangedListener
import java.io.File
import kotlin.math.ceil

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
            EditableNomenclatureType.ViewType.NONE.ordinal -> MoreViewHolder(parent)
            EditableNomenclatureType.ViewType.TEXT_SIMPLE.ordinal -> TextSimpleViewHolder(parent)
            EditableNomenclatureType.ViewType.TEXT_MULTIPLE.ordinal -> TextMultipleViewHolder(parent)
            EditableNomenclatureType.ViewType.MIN_MAX.ordinal -> MinMaxViewHolder(parent)
            EditableNomenclatureType.ViewType.MEDIA.ordinal -> MediaViewHolder(parent)
            else -> NomenclatureTypeViewHolder(parent)
        }
    }

    override fun onBindViewHolder(holder: AbstractViewHolder, position: Int) {
        selectedNomenclatureTypes.fold(listOf<EditableNomenclatureType>()) { acc, editableNomenclatureType ->
            acc + if (editableNomenclatureType.viewType == EditableNomenclatureType.ViewType.MIN_MAX && acc.any { it.viewType == EditableNomenclatureType.ViewType.MIN_MAX }) listOf() else listOf(editableNomenclatureType)
        }
            .sortedWith { o1, o2 ->
                val i1 = selectedNomenclatureTypes.indexOfFirst { it == o1 }
                val i2 = selectedNomenclatureTypes.indexOfFirst { it == o2 }

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
        return selectedNomenclatureTypes.filter { it.viewType != EditableNomenclatureType.ViewType.MIN_MAX }.size +
            selectedNomenclatureTypes.filter { it.viewType == EditableNomenclatureType.ViewType.MIN_MAX }.size.coerceAtMost(1)
    }

    override fun getItemViewType(position: Int): Int {
        return selectedNomenclatureTypes.fold(listOf<EditableNomenclatureType>()) { acc, editableNomenclatureType ->
            acc + if (editableNomenclatureType.viewType == EditableNomenclatureType.ViewType.MIN_MAX && acc.any { it.viewType == EditableNomenclatureType.ViewType.MIN_MAX }) listOf() else listOf(editableNomenclatureType)
        }
            .sortedWith { o1, o2 ->
                val i1 = selectedNomenclatureTypes.indexOfFirst { it == o1 }
                val i2 = selectedNomenclatureTypes.indexOfFirst { it == o2 }

                when {
                    i1 == -1 -> 1
                    i2 == -1 -> -1
                    else -> i1 - i2
                }
            }[position].viewType.ordinal
    }

    fun bind(
        nomenclatureTypes: List<EditableNomenclatureType>,
        vararg propertyValue: PropertyValue
    ) {
        availableNomenclatureTypes.clear()
        availableNomenclatureTypes.addAll(
            nomenclatureTypes.filter { it.visible }
                .map {
                    it.copy(value = propertyValue.firstOrNull { propertyValue -> propertyValue.toPair().first == it.code }
                        ?: it.value)
                }
        )

        if (showAllNomenclatureTypes) showAllNomenclatureTypes(notify = true) else showDefaultNomenclatureTypes(notify = true)
    }

    fun showDefaultNomenclatureTypes(notify: Boolean = false) {
        showAllNomenclatureTypes = false

        if (availableNomenclatureTypes.isEmpty()) return

        availableNomenclatureTypes.filter { it.default }
            .run {
                if (isEmpty()) {
                    // nothing to show by default: show everything
                    showAllNomenclatureTypes(notify)
                } else {
                    setSelectedNomenclatureTypes(
                        // show 'MORE' button only if we have some other editable nomenclatures to show
                        this + if (this.size < availableNomenclatureTypes.size) listOf(
                            EditableNomenclatureType(
                                EditableNomenclatureType.Type.INFORMATION,
                                "MORE",
                                EditableNomenclatureType.ViewType.NONE,
                                true
                            )
                        ) else emptyList()
                    )
                }
            }
    }

    fun showAllNomenclatureTypes(notify: Boolean = false) {
        showAllNomenclatureTypes = true
        setSelectedNomenclatureTypes(
            availableNomenclatureTypes,
            notify
        )
    }

    fun lockDefaultValues(lock: Boolean = false) {
        lockDefaultValues = lock
    }

    fun setPropertyValues(vararg propertyValue: PropertyValue) {
        availableNomenclatureTypes.map {
            it.copy(value = propertyValue.firstOrNull { propertyValue -> propertyValue.toPair().first == it.code }
                ?: it.value)
        }
            .also {
                availableNomenclatureTypes.clear()
                availableNomenclatureTypes.addAll(it)
            }

        if (showAllNomenclatureTypes) showAllNomenclatureTypes(notify = true) else showDefaultNomenclatureTypes(notify = true)
    }

    private fun setSelectedNomenclatureTypes(
        nomenclatureTypes: List<EditableNomenclatureType>,
        notify: Boolean = false
    ) {
        val oldKeys = selectedNomenclatureTypes.map { it.code }
        val newKeys = nomenclatureTypes.map { it.code }

        if (notify && oldKeys.isEmpty() && newKeys.isEmpty()) {
            listener.showEmptyTextView(true)

            return
        }

        val checkMedia =
            selectedNomenclatureTypes.firstOrNull { it.viewType == EditableNomenclatureType.ViewType.MEDIA }?.value == nomenclatureTypes.firstOrNull { it.viewType == EditableNomenclatureType.ViewType.MEDIA }?.value

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
                        ?.let { code -> selectedNomenclatureTypes.firstOrNull { it.code == code } }
                        ?.value?.toPair()?.second == newKeys.elementAtOrNull(newItemPosition)
                        ?.let { code -> nomenclatureTypes.firstOrNull { it.code == code } }
                        ?.value?.toPair()?.second)
        })

        selectedNomenclatureTypes.clear()
        selectedNomenclatureTypes.addAll(nomenclatureTypes)

        diffResult.dispatchUpdatesTo(this)
    }

    /**
     * Base [ViewHolder][RecyclerView.ViewHolder] used by this adapter.
     */
    abstract inner class AbstractViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var nomenclatureType: EditableNomenclatureType? = null

        fun bind(nomenclatureType: EditableNomenclatureType) {
            this.nomenclatureType = nomenclatureType

            onBind(nomenclatureType)
        }

        abstract fun onBind(nomenclatureType: EditableNomenclatureType)

        /**
         * Build the default label for given editable nomenclature type as fallback.
         */
        fun getNomenclatureTypeLabel(mnemonic: String): String {
            return itemView.resources.getIdentifier(
                "nomenclature_${mnemonic.lowercase()}",
                "string",
                itemView.context.packageName
            )
                .takeIf { it > 0 }
                ?.let { itemView.context.getString(it) } ?: mnemonic
        }
    }

    inner class NomenclatureTypeViewHolder(parent: ViewGroup) : AbstractViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(
                R.layout.view_action_nomenclature_type_select,
                parent,
                false
            )
    ) {
        private var edit: TextInputLayout = itemView.findViewById(android.R.id.edit)
        private var nomenclatureAdapter = NomenclatureValueAdapter(parent.context)
        private var showDropdown = false

        init {
            (edit.editText as? AutoCompleteTextView)?.also {
                it.setAdapter(nomenclatureAdapter)
                it.setOnItemClickListener { _, _, position, _ ->
                    showDropdown = false
                    nomenclatureType?.run {
                        value = nomenclatureAdapter.getNomenclatureValue(position)
                            .let { nomenclature ->
                                PropertyValue.Nomenclature(
                                    nomenclature.code,
                                    nomenclature.defaultLabel,
                                    nomenclature.id
                                )
                            }
                        listener.onUpdate(this)
                    }
                }
            }
        }

        override fun onBind(nomenclatureType: EditableNomenclatureType) {
            if (!lockDefaultValues) {
                nomenclatureType.locked = false
            }

            with(edit) {
                startIconDrawable = if (lockDefaultValues) ResourcesCompat.getDrawable(
                    itemView.resources,
                    if (nomenclatureType.locked) R.drawable.ic_lock else R.drawable.ic_lock_open,
                    itemView.context.theme
                ) else null
                setStartIconOnClickListener {
                    if (!lockDefaultValues) return@setStartIconOnClickListener

                    nomenclatureType.locked = !nomenclatureType.locked
                    startIconDrawable = ResourcesCompat.getDrawable(
                        itemView.resources,
                        if (nomenclatureType.locked) R.drawable.ic_lock else R.drawable.ic_lock_open,
                        itemView.context.theme
                    )
                    listener.onUpdate(nomenclatureType)
                }
                hint = nomenclatureType.label ?: getNomenclatureTypeLabel(nomenclatureType.code)
                setEndIconOnClickListener { setNomenclatureValues(nomenclatureType) }
                (editText as? AutoCompleteTextView)?.apply {
                    setOnClickListener { setNomenclatureValues(nomenclatureType) }
                    text = nomenclatureType.value
                        ?.takeIf { it is PropertyValue.Nomenclature }
                        ?.let { it as PropertyValue.Nomenclature }
                        ?.let {
                            Editable.Factory
                                .getInstance()
                                .newEditable(it.label ?: it.code)
                        }
                }
            }
        }

        private fun setNomenclatureValues(nomenclatureType: EditableNomenclatureType) {
            if (showDropdown) {
                showDropdown = false
                (edit.editText as? AutoCompleteTextView)?.dismissDropDown()
                return
            }

            listener.getNomenclatureValues(nomenclatureType.code)
                .observeOnce(listener.getLifecycleOwner()) {
                    showDropdown = true
                    nomenclatureAdapter.setNomenclatureValues(it ?: listOf())
                    (edit.editText as? AutoCompleteTextView)?.showDropDown()
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
                nomenclatureType?.run {
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

        override fun onBind(nomenclatureType: EditableNomenclatureType) {
            if (!lockDefaultValues) {
                nomenclatureType.locked = false
            }

            with(edit) {
                startIconDrawable = if (lockDefaultValues) ResourcesCompat.getDrawable(
                    itemView.resources,
                    if (nomenclatureType.locked) R.drawable.ic_lock else R.drawable.ic_lock_open,
                    itemView.context.theme
                ) else null
                setStartIconOnClickListener {
                    if (!lockDefaultValues) return@setStartIconOnClickListener

                    nomenclatureType.locked = !nomenclatureType.locked
                    startIconDrawable = ResourcesCompat.getDrawable(
                        itemView.resources,
                        if (nomenclatureType.locked) R.drawable.ic_lock else R.drawable.ic_lock_open,
                        itemView.context.theme
                    )
                    listener.onUpdate(nomenclatureType)
                }
                hint = getNomenclatureTypeLabel(nomenclatureType.code)
            }

            nomenclatureType.value
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

        private var minNomenclatureType: EditableNomenclatureType? = null
        private var maxNomenclatureType: EditableNomenclatureType? = null

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

                    minNomenclatureType?.also { nomenclatureType ->
                        nomenclatureType.value = PropertyValue.Number(
                            nomenclatureType.code,
                            newValue
                        )
                        listener.onUpdate(nomenclatureType)
                    }
                    maxNomenclatureType?.also { nomenclatureType ->
                        nomenclatureType.value = PropertyValue.Number(
                            nomenclatureType.code,
                            editMaxPicker.value
                        )
                        listener.onUpdate(nomenclatureType)
                    }
                }
            }

            with(editMaxPicker) {
                minValue = 0
                maxValue = defaultMaxValueOffset
                setOnValueChangedListener(defaultMaxValueOffset) { _, newValue ->
                    editMinPicker.maxValue = editMaxPicker.maxValue

                    if (editMinPicker.value > newValue) editMinPicker.value = newValue

                    minNomenclatureType?.also { nomenclatureType ->
                        nomenclatureType.value = PropertyValue.Number(
                            nomenclatureType.code,
                            editMinPicker.value
                        )
                        listener.onUpdate(nomenclatureType)
                    }
                    maxNomenclatureType?.also { nomenclatureType ->
                        nomenclatureType.value = PropertyValue.Number(
                            nomenclatureType.code,
                            newValue
                        )
                        listener.onUpdate(nomenclatureType)
                    }
                }
            }
        }

        override fun onBind(nomenclatureType: EditableNomenclatureType) {
            minNomenclatureType =
                selectedNomenclatureTypes.firstOrNull { it.viewType == EditableNomenclatureType.ViewType.MIN_MAX && it.code == CountingRecord.MIN_KEY }
            maxNomenclatureType =
                selectedNomenclatureTypes.firstOrNull { it.viewType == EditableNomenclatureType.ViewType.MIN_MAX && it.code == CountingRecord.MAX_KEY }

            with(if (minNomenclatureType != null) View.VISIBLE else View.GONE) {
                editMinLabel.visibility = this
                editMinPicker.visibility = this
            }

            with(if (maxNomenclatureType != null) View.VISIBLE else View.GONE) {
                editMaxLabel.visibility = this
                editMaxPicker.visibility = this
            }

            minNomenclatureType?.value?.takeIf { it is PropertyValue.Number }
                ?.let { it as PropertyValue.Number }?.value?.toInt()
                ?.also {
                    if (it > editMinPicker.maxValue) {
                        editMinPicker.maxValue =
                            (ceil((it.toDouble() / defaultMaxValueOffset)) * defaultMaxValueOffset).toInt()
                        editMaxPicker.maxValue = editMinPicker.maxValue
                    }

                    editMinPicker.value = it
                }

            maxNomenclatureType?.value?.takeIf { it is PropertyValue.Number }
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

        override fun onBind(nomenclatureType: EditableNomenclatureType) {
            title.text = nomenclatureType.label ?: getNomenclatureTypeLabel(nomenclatureType.code)
            adapter.setItems(nomenclatureType.value
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
                else R.layout.list_item_media
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
                nomenclatureType?.also {
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
                            nomenclatureType?.code?.also {
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
     * Callback used by [EditableNomenclatureTypeAdapter].
     */
    interface OnEditableNomenclatureTypeAdapter {

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
         * Called when an [EditableNomenclatureType] has been updated.
         *
         * @param editableNomenclatureType the [EditableNomenclatureType] updated
         */
        fun onUpdate(editableNomenclatureType: EditableNomenclatureType)

        /**
         * Called when we want to add media.
         */
        fun onAddMedia(nomenclatureTypeMnemonic: String)
    }
}