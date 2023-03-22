package fr.geonature.occtax.ui.home

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.TextSwitcher
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.ConstraintLayout
import fr.geonature.occtax.R

/**
 * Generic [View] about drawer menu entry.
 *
 * @author S. Grimault
 */
class DrawerMenuEntryView : ConstraintLayout {

    lateinit var icon: ImageView
        private set

    private lateinit var textView1: TextView
    private lateinit var textView2: TextSwitcher

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

    fun setIcon(@DrawableRes drawableResourceId: Int) {
        icon.setImageResource(drawableResourceId)
    }

    fun setText1(@StringRes textResourceId: Int) {
        setText1(if (textResourceId == 0) null else context.getString(textResourceId))
    }

    fun setText1(title: String?) {
        textView1.text = title
    }

    fun setText2(@StringRes textResourceId: Int) {
        setText2(if (textResourceId == 0) null else context.getString(textResourceId))
    }

    fun setText2(text: String?) {
        textView2.setText(text)
        textView2.visibility = if (text.isNullOrBlank()) GONE else VISIBLE
    }

    private fun init(
        attrs: AttributeSet?,
        defStyle: Int
    ) {
        View.inflate(
            context,
            R.layout.view_drawer_menu_entry,
            this
        )

        icon = findViewById(android.R.id.icon)
        textView1 = findViewById(android.R.id.text1)
        textView2 = findViewById(android.R.id.text2)

        // load attributes
        val ta = context.obtainStyledAttributes(
            attrs,
            R.styleable.DrawerMenuEntryView,
            defStyle,
            0
        )

        setIcon(
            ta.getResourceId(
                R.styleable.DrawerMenuEntryView_icon,
                0
            )
        )

        ta.getString(R.styleable.DrawerMenuEntryView_text1)
            ?.also {
                setText1(it)
            }
        setText1(
            ta.getResourceId(
                R.styleable.DrawerMenuEntryView_text1,
                0
            )
        )

        ta.getString(R.styleable.DrawerMenuEntryView_text2)
            ?.also {
                setText2(it)
            }
        setText2(
            ta.getResourceId(
                R.styleable.DrawerMenuEntryView_text2,
                0
            )
        )

        ta.recycle()
    }
}