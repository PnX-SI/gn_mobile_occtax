package fr.geonature.occtax.ui.shared.view

import android.content.Context
import android.util.AttributeSet
import android.util.Pair
import android.view.View
import android.view.animation.AnimationUtils.loadAnimation
import android.widget.Button
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import fr.geonature.commons.ui.adapter.AbstractListItemRecyclerViewAdapter
import fr.geonature.occtax.R

/**
 * Generic [View] about selected list items.
 *
 * @author S. Grimault
 */
open class ListItemActionView : ConstraintLayout {

    private lateinit var titleTextView: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var actionButton: Button
    private lateinit var emptyTextView: TextView
    private lateinit var adapter: EditListItemViewRecyclerViewAdapter
    private var listener: OnListItemActionViewListener? = null

    @StringRes
    private var actionText: Int = 0

    @StringRes
    private var actionEmptyText: Int = 0

    constructor(context: Context) : super(context) {
        init(
            null,
            0
        )
    }

    constructor(
        context: Context,
        attrs: AttributeSet
    ) : super(
        context,
        attrs
    ) {
        init(
            attrs,
            0
        )
    }

    constructor(
        context: Context,
        attrs: AttributeSet,
        defStyleAttr: Int
    ) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(
            attrs,
            defStyleAttr
        )
    }

    fun setListener(listener: OnListItemActionViewListener) {
        this.listener = listener
    }

    fun setTitle(@StringRes titleResourceId: Int) {
        setTitle(if (titleResourceId == 0) null else context.getString(titleResourceId))
    }

    fun setTitle(title: String?) {
        titleTextView.text = title
        titleTextView.visibility = if (title.isNullOrBlank()) GONE else VISIBLE
    }

    fun setEmptyText(@StringRes emptyTextResourceId: Int) {
        emptyTextView.setText(if (emptyTextResourceId == 0) R.string.no_data else emptyTextResourceId)
    }

    fun enableActionButton(enabled: Boolean = true) {
        actionButton.isEnabled = enabled
    }

    fun setActionText(@StringRes actionResourceId: Int) {
        if (actionResourceId == 0) {
            return
        }

        actionText = actionResourceId
        actionEmptyText = actionResourceId
    }

    fun setActionEmptyText(@StringRes actionResourceId: Int) {
        if (actionResourceId == 0) {
            return
        }

        actionEmptyText = actionResourceId
    }

    fun setVisibleItems(visibleItems: Int = 1) {
        val typedArray =
            context.theme.obtainStyledAttributes(intArrayOf(R.attr.listPreferredItemHeight))
        val listPreferredItemHeight = typedArray.getDimension(
            0,
            0f
        )

        if (listPreferredItemHeight > 0) {
            recyclerView.layoutParams.height = visibleItems * listPreferredItemHeight.toInt()
        }

        typedArray.recycle()
    }

    fun setItems(collection: List<Pair<String, String?>>) {
        adapter.setItems(collection)
    }

    fun set(item: Pair<String, String?>, index: Int) {
        adapter.set(
            item,
            index
        )
    }

    private fun init(
        attrs: AttributeSet?,
        defStyle: Int
    ) {
        View.inflate(
            context,
            R.layout.view_action_list_item,
            this
        )

        titleTextView = findViewById(android.R.id.title)
        recyclerView = findViewById(android.R.id.list)
        recyclerView.setHasFixedSize(true)
        actionButton = findViewById(android.R.id.button1)
        actionButton.setOnClickListener { listener?.onAction() }
        emptyTextView = findViewById(android.R.id.empty)

        adapter = EditListItemViewRecyclerViewAdapter(object :
            AbstractListItemRecyclerViewAdapter.OnListItemRecyclerViewAdapterListener<Pair<String, String?>> {
            override fun onClick(item: Pair<String, String?>) {
                // nothing to do...
            }

            override fun onLongClicked(position: Int, item: Pair<String, String?>) {
                // nothing to do...
            }

            override fun showEmptyTextView(show: Boolean) {
                actionButton.setText(if (show) actionEmptyText else actionText)
                this@ListItemActionView.showEmptyTextView(show)
            }
        })

        with(recyclerView) {
            layoutManager = LinearLayoutManager(context)
            adapter = this@ListItemActionView.adapter
        }

        val dividerItemDecoration = DividerItemDecoration(
            recyclerView.context,
            (recyclerView.layoutManager as LinearLayoutManager).orientation
        )
        recyclerView.addItemDecoration(dividerItemDecoration)

        // load attributes
        val ta = context.obtainStyledAttributes(
            attrs,
            R.styleable.ListItemActionView,
            defStyle,
            0
        )

        ta.getString(R.styleable.ListItemActionView_title)?.also {
            setTitle(it)
        }
        setTitle(
            ta.getResourceId(
                R.styleable.ListItemActionView_title,
                0
            )
        )

        setEmptyText(
            ta.getResourceId(
                R.styleable.ListItemActionView_no_data,
                R.string.no_data
            )
        )

        enableActionButton(
            ta.getBoolean(
                R.styleable.ListItemActionView_action_enabled,
                true
            )
        )

        setActionText(
            ta.getResourceId(
                R.styleable.ListItemActionView_action,
                0
            )
        )
        setActionEmptyText(
            ta.getResourceId(
                R.styleable.ListItemActionView_action_empty,
                0
            )
        )
        actionButton.setText(if (adapter.itemCount == 0) actionEmptyText else actionText)

        setVisibleItems(
            ta.getInteger(
                R.styleable.ListItemActionView_visible_items,
                1
            )
        )

        ta.recycle()
    }

    private fun showEmptyTextView(show: Boolean) {
        if (emptyTextView.visibility == View.VISIBLE == show) {
            return
        }

        if (show) {
            emptyTextView.startAnimation(
                loadAnimation(
                    context,
                    android.R.anim.fade_in
                )
            )
            emptyTextView.visibility = View.VISIBLE
        } else {
            emptyTextView.startAnimation(
                loadAnimation(
                    context,
                    android.R.anim.fade_out
                )
            )
            emptyTextView.visibility = View.GONE
        }
    }

    /**
     * Callback used by [ListItemActionView].
     */
    interface OnListItemActionViewListener {

        /**
         * Called when the action button has been clicked.
         */
        fun onAction()
    }

    /**
     * Default RecyclerView Adapter used by [ListItemActionView].
     *
     * @see ListItemActionView
     */
    private inner class EditListItemViewRecyclerViewAdapter(listener: OnListItemRecyclerViewAdapterListener<Pair<String, String?>>) :
        AbstractListItemRecyclerViewAdapter<Pair<String, String?>>(listener) {

        override fun getViewHolder(view: View, viewType: Int): AbstractViewHolder {
            return ViewHolder(view)
        }

        override fun getLayoutResourceId(position: Int, item: Pair<String, String?>): Int {
            return R.layout.list_item_2
        }

        override fun areItemsTheSame(
            oldItems: List<Pair<String, String?>>,
            newItems: List<Pair<String, String?>>,
            oldItemPosition: Int,
            newItemPosition: Int
        ): Boolean {
            return oldItems[oldItemPosition].first == newItems[newItemPosition].first &&
                oldItems[oldItemPosition].second == newItems[newItemPosition].second
        }

        override fun areContentsTheSame(
            oldItems: List<Pair<String, String?>>,
            newItems: List<Pair<String, String?>>,
            oldItemPosition: Int,
            newItemPosition: Int
        ): Boolean {
            return oldItems[oldItemPosition].first == newItems[newItemPosition].first &&
                oldItems[oldItemPosition].second == newItems[newItemPosition].second
        }

        inner class ViewHolder(itemView: View) :
            AbstractListItemRecyclerViewAdapter<Pair<String, String?>>.AbstractViewHolder(itemView) {

            private val textView1: TextView = itemView.findViewById(android.R.id.text1)
            private val textView2: TextView = itemView.findViewById(android.R.id.text2)

            override fun onBind(item: Pair<String, String?>) {
                textView1.text = item.first
                textView2.text = item.second.orEmpty()
            }
        }
    }
}
