package fr.geonature.occtax.ui.shared.view

import android.content.Context
import android.util.AttributeSet
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils.loadAnimation
import android.widget.Button
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import fr.geonature.commons.util.StringUtils
import fr.geonature.occtax.R
import java.util.ArrayList

/**
 * Generic [View] about selected list items.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class ListItemActionView : ConstraintLayout {

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

    constructor(context: Context) : super(context,
                                          null) {
        init(null,
             0)
    }

    constructor(context: Context,
                attrs: AttributeSet) : super(context,
                                             attrs) {
        init(attrs,
             0)
    }

    constructor(context: Context,
                attrs: AttributeSet,
                defStyleAttr: Int) : super(context,
                                           attrs,
                                           defStyleAttr) {
        init(attrs,
             defStyleAttr)
    }

    fun setListener(listener: OnListItemActionViewListener) {
        this.listener = listener
    }

    fun setTitle(@StringRes titleResourceId: Int) {
        if (titleResourceId == 0) {
            return
        }

        titleTextView.setText(titleResourceId)
    }

    fun setTitle(title: String) {
        titleTextView.setText(title)
    }

    fun setEmptyText(@StringRes emptyTextResourceId: Int) {
        emptyTextView.setText(if (emptyTextResourceId == 0) R.string.no_data else emptyTextResourceId)
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
        val typedArray = context.theme.obtainStyledAttributes(intArrayOf(R.attr.listPreferredItemHeight))
        val listPreferredItemHeight = typedArray.getDimension(0,
                                                              0f)

        if (listPreferredItemHeight > 0) {
            recyclerView.layoutParams.height = visibleItems * listPreferredItemHeight.toInt()
        }

        typedArray.recycle()
    }

    fun setItems(collection: Collection<Pair<String, String?>>) {
        adapter.setItems(collection)
    }

    private fun init(attrs: AttributeSet?,
                     defStyle: Int) {
        View.inflate(context,
                     R.layout.view_action_list_item,
                     this)

        titleTextView = findViewById(android.R.id.title)
        recyclerView = findViewById(android.R.id.list)
        recyclerView.setHasFixedSize(true)
        actionButton = findViewById(android.R.id.button1)
        actionButton.setOnClickListener { listener?.onAction() }
        emptyTextView = findViewById(android.R.id.empty)

        adapter = EditListItemViewRecyclerViewAdapter()
        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()

                actionButton.setText(if (adapter.itemCount == 0) actionEmptyText else actionText)
                showEmptyTextView(adapter.itemCount == 0)
            }

            override fun onItemRangeInserted(positionStart: Int,
                                             itemCount: Int) {
                super.onItemRangeInserted(positionStart,
                                          itemCount)

                actionButton.setText(actionText)
                showEmptyTextView(false)
            }
        })

        with(recyclerView) {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
            adapter = this@ListItemActionView.adapter
        }

        // load attributes
        val ta = context.obtainStyledAttributes(
                attrs,
                R.styleable.ListItemActionView,
                defStyle,
                0)

        setTitle(ta.getResourceId(R.styleable.ListItemActionView_title,
                                  0))
        setEmptyText(ta.getResourceId(R.styleable.ListItemActionView_no_data,
                                      R.string.no_data))
        setActionText(ta.getResourceId(R.styleable.ListItemActionView_action,
                                       0))
        setActionText(ta.getResourceId(R.styleable.ListItemActionView_action,
                                       0))
        setActionEmptyText(ta.getResourceId(R.styleable.ListItemActionView_action_empty,
                                            0))
        setVisibleItems(ta.getInteger(R.styleable.ListItemActionView_visible_items,
                                      1))

        ta.recycle()
    }

    private fun showEmptyTextView(show: Boolean) {
        if (emptyTextView.visibility == View.VISIBLE == show) {
            return
        }

        if (show) {
            emptyTextView.startAnimation(loadAnimation(context,
                                                       android.R.anim.fade_in))
            emptyTextView.visibility = View.VISIBLE

        }
        else {
            emptyTextView.startAnimation(loadAnimation(context,
                                                       android.R.anim.fade_out))
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

    private inner class EditListItemViewRecyclerViewAdapter : RecyclerView.Adapter<EditListItemViewRecyclerViewAdapter.ViewHolder>() {

        internal val items: MutableList<Pair<String, String?>> = ArrayList()

        override fun onCreateViewHolder(parent: ViewGroup,
                                        viewType: Int): ViewHolder {
            return ViewHolder(parent)
        }

        override fun onBindViewHolder(holder: ViewHolder,
                                      position: Int) {
            holder.bind(items[position])
        }

        override fun getItemCount(): Int {
            return items.size
        }

        fun setItems(collection: Collection<Pair<String, String?>>) {
            items.clear()
            items.addAll(collection)

            notifyDataSetChanged()
        }

        internal inner class ViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(LayoutInflater.from(parent.context)
                                                                                             .inflate(R.layout.list_item_2,
                                                                                                      parent,
                                                                                                      false)) {

            private val textView1: TextView = itemView.findViewById(android.R.id.text1)
            private val textView2: TextView = itemView.findViewById(android.R.id.text2)

            fun bind(pair: Pair<String, String?>) {
                bind(pair.first,
                     pair.second)
            }

            fun bind(label: String,
                     description: String?) {
                textView1.text = label
                textView2.text = if (StringUtils.isEmpty(description)) "" else description
            }
        }
    }
}
