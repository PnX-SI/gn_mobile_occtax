package fr.geonature.occtax.features.nomenclature.presentation.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import fr.geonature.commons.data.entity.Nomenclature
import fr.geonature.occtax.R
import fr.geonature.occtax.features.nomenclature.domain.FormField
import fr.geonature.occtax.features.record.domain.MediaRecord
import fr.geonature.occtax.features.record.domain.PropertyValue

/**
 * Default RecyclerView Adapter about [FormField].
 *
 * @author S. Grimault
 */
class FormFieldAdapter(private val listener: OnEditableFieldAdapter) :
    RecyclerView.Adapter<FormFieldAdapter.AbstractViewHolder>() {

    private val availableEditableFields = mutableListOf<FormField>()
    private val selectedEditableFields = mutableListOf<FormField>()
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
            ViewType.BUTTON.ordinal -> FormFieldButtonViewHolder(
                parent,
                object : FormFieldButtonViewHolder.OnFormFieldButtonViewHolderListener {
                    override fun onClick(button: FormField.Button) {
                        if (button.label == listener.getContext()
                                .getString(R.string.nomenclature_more)
                        ) {
                            showAllFormFields()
                            listener.showMore()
                        }
                    }
                })

            ViewType.CHECKBOX.ordinal -> FormFieldCheckboxViewHolder(
                parent,
                object :
                    FormFieldCheckboxViewHolder.OnFormFieldCheckboxViewHolderListener {
                    override fun onUpdate(editableField: FormField.Checkbox) {
                        availableEditableFields[availableEditableFields.indexOfFirst { ff -> ff is FormField.Checkbox && ff.value.code == editableField.value.code }] =
                            editableField
                        listener.onUpdate(editableField)
                    }
                }
            )

            ViewType.DATE.ordinal -> FormFieldDateViewHolder(
                parent,
                object : FormFieldDateViewHolder.OnFormFieldDateViewHolderListener {
                    override fun onUpdate(editableField: FormField.Date) {
                        availableEditableFields[availableEditableFields.indexOfFirst { ff -> ff is FormField.Date && ff.value.code == editableField.value.code }] =
                            editableField
                        listener.onUpdate(editableField)
                    }

                    override fun fragmentManager(): FragmentManager? {
                        return listener.fragmentManager()
                    }
                }
            )

            ViewType.MEDIA.ordinal -> FormFieldMediaViewHolder(
                parent,
                listener
            )

            ViewType.MIN_MAX.ordinal -> FormFieldMinMaxViewHolder(
                parent,
                listener
            )

            ViewType.MODAL.ordinal -> FormFieldModalViewHolder(
                parent,
                object : FormFieldModalViewHolder.OnFormFieldModalViewHolder {
                    override fun onAction(formField: FormField.Modal) {
                        listener.onAction(formField)
                    }
                }
            )

            ViewType.MODAL_MULTIPLE.ordinal -> FormFieldModalMultipleViewHolder(
                parent,
                object : FormFieldModalMultipleViewHolder.OnFormFieldModalMultipleViewHolder {
                    override fun onAction(formField: FormField.ModalMultiple) {
                        listener.onAction(formField)
                    }
                }
            )

            ViewType.NOMENCLATURE_TYPE.ordinal -> FormFieldNomenclatureTypeViewHolder(
                parent,
                object :
                    FormFieldNomenclatureTypeViewHolder.OnFormFieldNomenclatureTypeViewHolderListener {
                    override fun getLifecycleOwner(): LifecycleOwner {
                        return listener.getLifecycleOwner()
                    }

                    override fun getNomenclatureValues(nomenclatureTypeMnemonic: String): LiveData<List<Nomenclature>> {
                        return listener.getNomenclatureValues(nomenclatureTypeMnemonic)
                    }

                    override fun onUpdate(editableField: FormField.NomenclatureType) {
                        availableEditableFields[availableEditableFields.indexOfFirst { ff -> ff is FormField.NomenclatureType && ff.value.code == editableField.value.code }] =
                            editableField
                        listener.onUpdate(editableField)
                    }
                }
            )

            ViewType.NUMBER.ordinal -> FormFieldNumberViewHolder(
                parent,
                object :
                    AbstractFormFieldTextViewHolder.OnAbstractFormFieldTextViewHolderViewHolderListener<FormField.Number> {
                    override fun onUpdate(editableField: FormField.Number) {
                        availableEditableFields[availableEditableFields.indexOfFirst { ff -> ff is FormField.Number && ff.value.code == editableField.value.code }] =
                            editableField
                        listener.onUpdate(editableField)
                    }
                }
            )

            ViewType.RADIO.ordinal -> FormFieldRadioViewHolder(
                parent,
                object : FormFieldRadioViewHolder.OnFormFieldRadioViewHolderListener {
                    override fun onUpdate(editableField: FormField.Radio) {
                        availableEditableFields[availableEditableFields.indexOfFirst { ff -> ff is FormField.Radio && ff.value.code == editableField.value.code }] =
                            editableField
                        listener.onUpdate(editableField)
                    }
                }
            )

            ViewType.SELECT.ordinal -> FormFieldSelectSimpleViewHolder(
                parent,
                object : FormFieldSelectSimpleViewHolder.OnFormFieldSelectSimpleViewHolderListener {
                    override fun onUpdate(editableField: FormField.Select) {
                        availableEditableFields[availableEditableFields.indexOfFirst { ff -> ff is FormField.Select && ff.value.code == editableField.value.code }] =
                            editableField
                        listener.onUpdate(editableField)
                    }
                }
            )

            ViewType.SELECT_MULTIPLE.ordinal -> FormFieldSelectMultipleViewHolder(
                parent,
                object :
                    FormFieldSelectMultipleViewHolder.OnFormFieldSelectMultipleViewHolderListener {
                    override fun onUpdate(editableField: FormField.SelectMultiple) {
                        availableEditableFields[availableEditableFields.indexOfFirst { ff -> ff is FormField.SelectMultiple && ff.value.code == editableField.value.code }] =
                            editableField
                        listener.onUpdate(editableField)
                    }
                }
            )

            ViewType.START_END.ordinal -> FormFieldStartEndViewHolder(
                parent,
                object : FormFieldStartEndViewHolder.OnFormFieldStartEndViewHolderListener {
                    override fun fragmentManager(): FragmentManager? {
                        return listener.fragmentManager()
                    }

                    override fun onUpdate(formField: FormField.StartEnd) {
                        availableEditableFields[availableEditableFields.indexOfFirst { ff -> ff is FormField.StartEnd }] =
                            formField
                        listener.onUpdate(formField.start)
                        listener.onUpdate(formField.end)
                    }
                }
            )

            ViewType.TEXT.ordinal -> FormFieldTextSimpleViewHolder(
                parent,
                object :
                    AbstractFormFieldTextViewHolder.OnAbstractFormFieldTextViewHolderViewHolderListener<FormField.Text> {
                    override fun onUpdate(editableField: FormField.Text) {
                        availableEditableFields[availableEditableFields.indexOfFirst { ff -> ff is FormField.Text && ff.value.code == editableField.value.code }] =
                            editableField
                        listener.onUpdate(editableField)
                    }
                }
            )

            ViewType.TEXT_MULTIPLE.ordinal -> FormFieldTextMultipleViewHolder(
                parent,
                object :
                    AbstractFormFieldTextViewHolder.OnAbstractFormFieldTextViewHolderViewHolderListener<FormField.TextMultiple> {
                    override fun onUpdate(editableField: FormField.TextMultiple) {
                        availableEditableFields[availableEditableFields.indexOfFirst { ff -> ff is FormField.TextMultiple && ff.value.code == editableField.value.code }] =
                            editableField
                        listener.onUpdate(editableField)
                    }
                }
            )

            ViewType.TIME.ordinal -> FormFieldTimeViewHolder(
                parent,
                object : FormFieldTimeViewHolder.OnFormFieldTimeViewHolderListener {
                    override fun onUpdate(editableField: FormField.Time) {
                        availableEditableFields[availableEditableFields.indexOfFirst { ff -> ff is FormField.Time && ff.value.code == editableField.value.code }] =
                            editableField
                        listener.onUpdate(editableField)
                    }

                    override fun fragmentManager(): FragmentManager? {
                        return listener.fragmentManager()
                    }
                }
            )

            // not supported
            else -> NoneViewHolder(parent)
        }
    }

    override fun onBindViewHolder(holder: AbstractViewHolder, position: Int) {
        selectedEditableFields[position].also {
            when (holder) {
                is AbstractLockableViewHolder<*> -> holder.bind(
                    it,
                    lockDefaultValues
                )

                else -> holder.bind(it)
            }
        }
    }

    override fun getItemCount(): Int {
        return selectedEditableFields.size
    }

    override fun getItemViewType(position: Int): Int {
        return when (selectedEditableFields[position]) {
            is FormField.Button -> ViewType.BUTTON.ordinal
            is FormField.Checkbox -> ViewType.CHECKBOX.ordinal
            is FormField.Date -> ViewType.DATE.ordinal
            is FormField.Media -> ViewType.MEDIA.ordinal
            is FormField.MinMax -> ViewType.MIN_MAX.ordinal
            is FormField.Modal -> ViewType.MODAL.ordinal
            is FormField.ModalMultiple -> ViewType.MODAL_MULTIPLE.ordinal
            is FormField.NomenclatureType -> ViewType.NOMENCLATURE_TYPE.ordinal
            is FormField.Number -> ViewType.NUMBER.ordinal
            is FormField.Radio -> ViewType.RADIO.ordinal
            is FormField.Select -> ViewType.SELECT.ordinal
            is FormField.SelectMultiple -> ViewType.SELECT_MULTIPLE.ordinal
            is FormField.StartEnd -> ViewType.START_END.ordinal
            is FormField.Text -> ViewType.TEXT.ordinal
            is FormField.TextMultiple -> ViewType.TEXT_MULTIPLE.ordinal
            is FormField.Time -> ViewType.TIME.ordinal
            // not supported
            else -> -1
        }
    }

    fun bind(
        formFields: List<FormField>,
        vararg propertyValue: PropertyValue
    ) {
        availableEditableFields.clear()
        availableEditableFields.addAll(
            formFields.filter { it.visible }
                .map {
                    when (it) {
                        is FormField.Editable -> it.apply {
                            setValue(propertyValue.firstOrNull { pv -> pv.code == it.getValue().code }
                                ?: it.getValue())
                        }

                        is FormField.MinMax -> it.copy(
                            min = it.min.copy(
                                value = propertyValue.filterIsInstance<PropertyValue.Number>()
                                    .firstOrNull { pv -> pv.code == it.min.value.code }
                                    ?: it.min.value),
                            max = it.max.copy(
                                value = propertyValue.filterIsInstance<PropertyValue.Number>()
                                    .firstOrNull { pv -> pv.code == it.max.value.code }
                                    ?: it.max.value)
                        )

                        is FormField.StartEnd -> it.copy(
                            start = it.start.copy(value = propertyValue.filterIsInstance<PropertyValue.Date>()
                                .firstOrNull { pv -> pv.code == it.start.value.code }
                                ?: it.start.value),
                            end = it.end.copy(
                                value = propertyValue.filterIsInstance<PropertyValue.Date>()
                                    .firstOrNull { pv -> pv.code == it.end.value.code }
                                    ?: it.end.value)
                        )

                        else -> it
                    }
                }
                .sorted()
        )

        if (showAll) showAllFormFields(notify = true) else showDefaultFormFields(notify = true)
    }

    fun updateEditableField(vararg formField: FormField.Editable) {
        availableEditableFields.map { ff ->
            when (ff) {
                is FormField.Editable -> formField.firstOrNull { it.getValue().code == ff.getValue().code }
                    ?: ff

                else -> ff
            }
        }
            .also {
                availableEditableFields.clear()
                availableEditableFields.addAll(it)
            }

        setFormFields(
            selectedEditableFields.map { ff ->
                when (ff) {
                    is FormField.Editable -> formField.firstOrNull { it.getValue().code == ff.getValue().code }
                        ?: ff

                    else -> ff
                }
            },
            notify = true
        )
    }

    fun showDefaultFormFields(notify: Boolean = false) {
        showAll = false

        if (availableEditableFields.isEmpty()) return

        availableEditableFields.filter { it.default }
            .run {
                if (isEmpty()) {
                    // nothing to show by default: show everything
                    showAllFormFields(notify)
                } else {
                    setFormFields(
                        // show 'MORE' button only if we have some other editable field to show
                        this + if (this.size < availableEditableFields.size) listOf(
                            FormField.Button(
                                type = FormField.Type.INFORMATION,
                                label = listener.getContext()
                                    .getString(R.string.nomenclature_more)
                            )
                        ) else emptyList()
                    )
                }
            }
    }

    fun showAllFormFields(notify: Boolean = false) {
        showAll = true
        setFormFields(
            availableEditableFields,
            notify
        )
    }

    fun lockDefaultValues(lock: Boolean = false) {
        lockDefaultValues = lock
    }

    fun setPropertyValues(vararg propertyValue: PropertyValue) {
        availableEditableFields.map {
            when (it) {
                is FormField.Editable -> it.clone().apply {
                    setValue(propertyValue.firstOrNull { pv -> pv.code == it.getValue().code }
                        ?: it.getValue())
                }

                is FormField.MinMax -> it.copy(
                    min = it.min.copy(
                        value = propertyValue.filterIsInstance<PropertyValue.Number>()
                            .firstOrNull { pv -> pv.code == it.min.value.code }
                            ?: it.min.value),
                    max = it.max.copy(
                        value = propertyValue.filterIsInstance<PropertyValue.Number>()
                            .firstOrNull { pv -> pv.code == it.max.value.code }
                            ?: it.max.value)
                )

                else -> it
            }
        }
            .also {
                availableEditableFields.clear()
                availableEditableFields.addAll(it)
            }

        if (showAll) showAllFormFields(notify = true) else showDefaultFormFields(notify = true)
    }

    /**
     * Whether at least one [FormField.Editable] contains error.
     */
    fun hasErrors(): Boolean {
        return availableEditableFields.filterIsInstance<FormField.Editable>()
            .any {
                it.error?.isNotBlank() == true
            } || availableEditableFields.filterIsInstance<FormField.StartEnd>()
            .flatMap {
                listOf(
                    it.start,
                    it.end
                )
            }
            .any { it.error?.isNotBlank() == true }
    }

    private fun setFormFields(
        formFields: List<FormField>,
        notify: Boolean = false
    ) {
        if (notify && selectedEditableFields.isEmpty() && formFields.isEmpty()) {
            listener.showEmptyTextView(true)

            return
        }

        val checkMedia =
            selectedEditableFields
                .firstOrNull { it is FormField.Media }
                ?.let { it as FormField.Media }
                ?.value == formFields
                .firstOrNull { it is FormField.Media }
                ?.let { it as FormField.Media }
                ?.value

        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = selectedEditableFields.size

            override fun getNewListSize(): Int = formFields.size

            override fun areItemsTheSame(
                oldItemPosition: Int,
                newItemPosition: Int
            ) =
                selectedEditableFields.elementAtOrNull(oldItemPosition) == formFields.elementAtOrNull(newItemPosition)

            override fun areContentsTheSame(
                oldItemPosition: Int,
                newItemPosition: Int
            ) =
                checkMedia && selectedEditableFields.elementAtOrNull(oldItemPosition) == formFields.elementAtOrNull(newItemPosition)
        })

        selectedEditableFields.clear()
        selectedEditableFields.addAll(formFields)

        diffResult.dispatchUpdatesTo(this)
    }

    /**
     * Base [ViewHolder][RecyclerView.ViewHolder] used by this adapter.
     */
    abstract class AbstractViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind(formField: FormField)
    }

    /**
     * Base [ViewHolder][RecyclerView.ViewHolder] used by this adapter embedding a [FormField].
     */
    abstract class AbstractFormFieldViewHolder<FF : FormField>(itemView: View) :
        AbstractViewHolder(itemView) {
        internal var formField: FF? = null

        override fun bind(formField: FormField) {
            @Suppress("UNCHECKED_CAST")
            this.formField = formField as FF

            onBind(formField)
        }

        abstract fun onBind(formField: FF)
    }

    /**
     * Base [ViewHolder][RecyclerView.ViewHolder] used by this adapter with the option of locking
     * the [FormField.Editable].
     */
    abstract class AbstractLockableViewHolder<FF : FormField.Editable>(itemView: View) :
        AbstractFormFieldViewHolder<FF>(itemView) {

        fun bind(editableField: FormField, lockDefaultValues: Boolean = false) {
            @Suppress("UNCHECKED_CAST")
            this.formField = editableField as FF

            onBind(
                editableField,
                lockDefaultValues
            )
        }

        override fun onBind(formField: FF) {
            onBind(
                formField,
                false
            )
        }

        abstract fun onBind(formField: FF, lockDefaultValues: Boolean = false)
    }

    /**
     * Describes a form field's view type.
     */
    enum class ViewType {

        /**
         * As button.
         */
        BUTTON,

        /**
         * As list of checkboxes.
         */
        CHECKBOX,

        /**
         * As a date.
         */
        DATE,

        /**
         * As media file.
         */
        MEDIA,

        /**
         * As a bounded numerical value.
         */
        MIN_MAX,

        /**
         * As selectable item from modal.
         */
        MODAL,

        /**
         * As list of selectable items from modal.
         */
        MODAL_MULTIPLE,

        /**
         * As dropdown nomenclature items.
         */
        NOMENCLATURE_TYPE,

        /**
         * As number text field.
         */
        NUMBER,

        /**
         * As radio group.
         */
        RADIO,

        /**
         * As a single select.
         */
        SELECT,

        /**
         * As multiselect.
         */
        SELECT_MULTIPLE,

        /**
         * As a group of start and end dates.
         */
        START_END,

        /**
         * As a single text field.
         */
        TEXT,

        /**
         * As multi-lines text field.
         */
        TEXT_MULTIPLE,

        /**
         * As a time field.
         */
        TIME
    }

    /**
     * Callback used by [FormFieldAdapter].
     */
    interface OnEditableFieldAdapter {

        fun getContext(): Context

        fun getLifecycleOwner(): LifecycleOwner

        fun getCoordinatorLayout(): CoordinatorLayout?

        /**
         * Return the FragmentManager for interacting with fragments associated with this adapter views.
         */
        fun fragmentManager(): FragmentManager?

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
         * Called when an [FormField.Editable] has been updated.
         *
         * @param editableField the [FormField.Editable] updated
         */
        fun onUpdate(editableField: FormField.Editable)

        /**
         * Called when the action button has been clicked from [FormFieldModalMultipleViewHolder].
         * Should show the modal of selectable items.
         */
        fun onAction(formField: FormField)

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