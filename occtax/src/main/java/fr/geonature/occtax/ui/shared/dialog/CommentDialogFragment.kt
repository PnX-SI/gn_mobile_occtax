package fr.geonature.occtax.ui.shared.dialog

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import fr.geonature.commons.util.KeyboardUtils.hideSoftKeyboard
import fr.geonature.commons.util.KeyboardUtils.showSoftKeyboard
import fr.geonature.occtax.R

/**
 * Custom [Dialog] used to add comment.
 *
 * @author S. Grimault
 */
class CommentDialogFragment : DialogFragment() {

    private var comment: String? = null
    private var onCommentDialogFragmentListener: OnCommentDialogFragmentListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = requireContext()

        val view = View.inflate(
            context,
            R.layout.dialog_comment,
            null
        )
        val editText = view.findViewById<EditText>(android.R.id.edit)
            .also {
                it.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {
                    }

                    override fun afterTextChanged(s: Editable?) {
                    }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                        comment = s?.toString()
                    }
                })
            }

        arguments?.getString(KEY_COMMENT)
            ?.also {
                comment = it
                editText.text = Editable.Factory.getInstance()
                    .newEditable(it)
            }

        // restore the previous state if any
        savedInstanceState?.getString(KEY_COMMENT)
            ?.also {
                comment = it
                editText.text = Editable.Factory.getInstance()
                    .newEditable(it)
            }

        // show automatically the soft keyboard for the EditText
        editText.post {
            showSoftKeyboard(editText)
        }

        return AlertDialog.Builder(context)
            .setTitle(if (TextUtils.isEmpty(comment)) R.string.alert_dialog_add_comment_title else R.string.alert_dialog_edit_comment_title)
            .setView(view)
            .setPositiveButton(R.string.alert_dialog_ok) { _, _ ->
                hideSoftKeyboard(editText)
                onCommentDialogFragmentListener?.onChanged(comment)
            }
            .setNegativeButton(
                R.string.alert_dialog_cancel,
                null
            )
            .create()
    }

    override fun onStart() {
        super.onStart()

        // resize the dialog width to match parent
        dialog?.also {
            it.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putSerializable(
            KEY_COMMENT,
            comment
        )

        super.onSaveInstanceState(outState)
    }

    fun setOnCommentDialogFragmentListener(onCommentDialogFragmentListener: OnCommentDialogFragmentListener) {

        this.onCommentDialogFragmentListener = onCommentDialogFragmentListener
    }

    companion object {

        const val KEY_COMMENT = "comment"

        /**
         * Use this factory method to create a new instance of [CommentDialogFragment].
         *
         * @return A new instance of [CommentDialogFragment]
         */
        @JvmStatic
        fun newInstance(comment: String?) = CommentDialogFragment().apply {
            arguments = Bundle().apply {
                putString(
                    KEY_COMMENT,
                    comment
                )
            }
        }
    }

    /**
     * The callback used by [CommentDialogFragment].
     *
     * @author S. Grimault
     */
    interface OnCommentDialogFragmentListener {

        /**
         * Invoked when the positive button of the dialog is pressed.
         *
         * @param comment the string comment edited from this dialog
         */
        fun onChanged(comment: String?)
    }
}
