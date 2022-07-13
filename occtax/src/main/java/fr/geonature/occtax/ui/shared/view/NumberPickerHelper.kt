package fr.geonature.occtax.ui.shared.view

import android.widget.EditText
import android.widget.NumberPicker
import androidx.core.view.children
import kotlin.math.ceil

/**
 * Utilities function about NumberPicker.
 *
 * @author S. Grimault
 */

/**
 * Extension function to increment automatically the `maxValue` attribute when the current value
 * reach the max value.
 *
 * @param offset the offset used to increment the max value
 * @param callback the listener to be notified on change of the current value
 */
fun NumberPicker.setOnValueChangedListener(
    offset: Int,
    callback: (oldValue: Int, newValue: Int) -> Unit
) {
    setOnValueChangedListener { _, oldVal, newVal ->
        if (newVal == maxValue) {
            maxValue += offset
        }

        callback.invoke(
            oldVal,
            newVal
        )
    }

    children.find { it is EditText }?.let { it as EditText }?.also {
        it.filters = emptyArray()
        it.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                it.selectAll()
            } else {
                it.setSelection(
                    0,
                    0
                )

                val intValue = it.text?.toString()?.toIntOrNull() ?: 0

                if (intValue > maxValue) maxValue =
                    (ceil((intValue.toDouble() / offset)) * offset).toInt()
                value = intValue
                callback.invoke(
                    value,
                    value
                )
            }
        }
    }
}