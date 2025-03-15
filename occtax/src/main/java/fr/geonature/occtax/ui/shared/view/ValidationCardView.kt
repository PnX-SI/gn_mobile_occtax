package fr.geonature.occtax.ui.shared.view

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.card.MaterialCardView
import fr.geonature.occtax.R

/**
 * custom [MaterialCardView] with validation.
 *
 * @author S. Grimault
 */
class ValidationCardView : MaterialCardView {

    /**
     * Whether this [MaterialCardView] is considered as not valid (i.e. contains some errors).
     */
    var hasErrors: Boolean = false
        set(value) {
            if (field == value) return
            field = value
            refreshDrawableState()
        }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(
        context,
        attrs
    )

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    override fun onCreateDrawableState(extraSpace: Int): IntArray {
        if (hasErrors) {
            val drawableState = super.onCreateDrawableState(extraSpace + 1)
            mergeDrawableStates(
                drawableState,
                intArrayOf(R.attr.state_error)
            )
            return drawableState
        }

        return super.onCreateDrawableState(extraSpace)
    }
}