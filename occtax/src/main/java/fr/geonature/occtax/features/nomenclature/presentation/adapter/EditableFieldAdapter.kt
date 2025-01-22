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
class EditableFieldAdapter(private val listener: OnEditableFieldAdapter) :
    RecyclerView.Adapter<EditableFieldAdapter.AbstractViewHolder>() {

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

            ViewType.CHECKBOX.ordinal -> CheckboxViewHolder(
                parent,
                listener
            )

            ViewType.DATE.ordinal -> FormFieldDateViewHolder(
                parent,
                listener
            )

            ViewType.MEDIA.ordinal -> MediaViewHolder(
                parent,
                listener
            )

            ViewType.MIN_MAX.ordinal -> MinMaxViewHolder(
                parent,
                listener
            )

            ViewType.NOMENCLATURE_TYPE.ordinal -> NomenclatureTypeViewHolder(
                parent,
                listener
            )

            ViewType.NUMBER.ordinal -> NumberViewHolder(
                parent,
                listener
            )

            ViewType.RADIO.ordinal -> RadioViewHolder(
                parent,
                listener
            )

            ViewType.SELECT.ordinal -> SelectSimpleViewHolder(
                parent,
                listener
            )

            ViewType.SELECT_MULTIPLE.ordinal -> SelectMultipleViewHolder(
                parent,
                listener
            )

            ViewType.TEXT.ordinal -> TextSimpleViewHolder(
                parent,
                listener
            )

            ViewType.TEXT_MULTIPLE.ordinal -> TextMultipleViewHolder(
                parent,
                listener
            )

            ViewType.TIME.ordinal -> FormFieldTimeViewHolder(
                parent,
                listener
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
            is FormField.NomenclatureType -> ViewType.NOMENCLATURE_TYPE.ordinal
            is FormField.Number -> ViewType.NUMBER.ordinal
            is FormField.Radio -> ViewType.RADIO.ordinal
            is FormField.Select -> ViewType.SELECT.ordinal
            is FormField.SelectMultiple -> ViewType.SELECT_MULTIPLE.ordinal
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

                        else -> it
                    }
                }
                .sorted()
        )

        if (showAll) showAllFormFields(notify = true) else showDefaultFormFields(notify = true)
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

                else -> it
            }
        }
            .also {
                availableEditableFields.clear()
                availableEditableFields.addAll(it)
            }

        if (showAll) showAllFormFields(notify = true) else showDefaultFormFields(notify = true)
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
     * the [FormField].
     */
    abstract class AbstractLockableViewHolder<FF : FormField>(itemView: View) :
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
     * Callback used by [EditableFieldAdapter].
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
         * Called when we want to add media.
         */
        fun onAddMedia(nomenclatureTypeMnemonic: String)

        /**
         * Called when we want to show given media.
         */
        fun onMediaSelected(mediaRecord: MediaRecord.File)
    }
}