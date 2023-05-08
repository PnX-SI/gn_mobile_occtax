package fr.geonature.occtax.ui.shared.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import fr.geonature.occtax.R

/**
 * Generic [View] about adding custom view with an action button.
 *
 * @author S. Grimault
 */
class ActionView : ConstraintLayout {

    private var contentView: FrameLayout? = null
    private lateinit var titleTextView: TextView
    private lateinit var actionButton: Button
    private lateinit var emptyTextView: TextView
    private var listener: OnActionViewListener? = null

    private var contentViewVisibility: Int = View.GONE

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

    override fun addView(child: View?, index: Int, params: ViewGroup.LayoutParams?) {
        if (contentView == null) {
            super.addView(
                child,
                index,
                params
            )
        } else {
            contentView?.children?.filter { it.id != emptyTextView.id }
                ?.forEach {
                    contentView?.removeView(it)
                }
            contentView?.addView(
                child,
                index,
                params
            )
            setContentViewVisibility(contentViewVisibility)
        }
    }

    fun getContentView(): View? {
        return contentView?.children?.firstOrNull { it.id != emptyTextView.id }
    }

    fun setListener(listener: OnActionViewListener) {
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

    fun setContentViewVisibility(visibility: Int) {
        contentViewVisibility = visibility
        actionButton.setText(if (visibility == View.VISIBLE) actionText else actionEmptyText.takeIf { it > 0 }
            ?: actionText)
        contentView?.children?.forEach {
            if (it.id == emptyTextView.id) it.visibility =
                if (visibility == View.VISIBLE) View.GONE else View.VISIBLE
            else it.visibility = if (visibility == View.VISIBLE) View.VISIBLE else View.GONE
        }
    }

    private fun init(
        attrs: AttributeSet?,
        defStyle: Int
    ) {
        View.inflate(
            context,
            R.layout.view_action,
            this
        )

        titleTextView = findViewById(android.R.id.title)
        actionButton = findViewById(android.R.id.button1)
        actionButton.setOnClickListener { listener?.onAction() }
        contentView = findViewById(android.R.id.content)
        emptyTextView = findViewById(android.R.id.empty)

        // load attributes
        val ta = context.obtainStyledAttributes(
            attrs,
            R.styleable.ActionView,
            defStyle,
            0
        )

        ta.getString(R.styleable.ActionView_title)
            ?.also {
                setTitle(it)
            }
        setTitle(
            ta.getResourceId(
                R.styleable.ActionView_title,
                0
            )
        )

        setEmptyText(
            ta.getResourceId(
                R.styleable.ActionView_no_data,
                R.string.no_data
            )
        )

        enableActionButton(
            ta.getBoolean(
                R.styleable.ActionView_action_enabled,
                true
            )
        )

        setActionText(
            ta.getResourceId(
                R.styleable.ActionView_action,
                0
            )
        )
        setActionEmptyText(
            ta.getResourceId(
                R.styleable.ActionView_action_empty,
                0
            )
        )

        setContentViewVisibility(
            ta.getInt(
                R.styleable.ActionView_content_visibility,
                View.GONE
            )
        )

        ta.recycle()
    }

    /**
     * Callback used by [ActionView].
     */
    interface OnActionViewListener {

        /**
         * Called when the action button has been clicked.
         */
        fun onAction()
    }
}