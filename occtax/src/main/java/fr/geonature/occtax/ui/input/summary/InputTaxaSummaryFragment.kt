package fr.geonature.occtax.ui.input.summary

import android.os.Bundle
import android.os.Vibrator
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import fr.geonature.commons.input.AbstractInput
import fr.geonature.commons.input.AbstractInputTaxon
import fr.geonature.commons.ui.adapter.AbstractListItemRecyclerViewAdapter
import fr.geonature.occtax.R
import fr.geonature.occtax.input.Input
import fr.geonature.occtax.ui.input.IInputFragment
import fr.geonature.occtax.ui.shared.dialog.CommentDialogFragment
import fr.geonature.viewpager.ui.AbstractPagerFragmentActivity
import fr.geonature.viewpager.ui.IValidateFragment

/**
 * Summary of all edited taxa.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class InputTaxaSummaryFragment : Fragment(),
    IValidateFragment,
    IInputFragment {

    private var input: Input? = null
    private var adapter: InputTaxaSummaryRecyclerViewAdapter? = null
    private var contentView: CoordinatorLayout? = null
    private var recyclerView: RecyclerView? = null
    private var emptyTextView: TextView? = null
    private var fab: FloatingActionButton? = null

    private val onCommentDialogFragmentListener =
        object : CommentDialogFragment.OnCommentDialogFragmentListener {
            override fun onChanged(comment: String?) {
                input?.comment = comment
                activity?.invalidateOptionsMenu()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val supportFragmentManager = activity?.supportFragmentManager ?: return

        (supportFragmentManager.findFragmentByTag(COMMENT_DIALOG_FRAGMENT) as CommentDialogFragment?)?.also {
            it.setOnCommentDialogFragmentListener(onCommentDialogFragmentListener)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(
            R.layout.fragment_recycler_view_fab,
            container,
            false
        )
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(
            view,
            savedInstanceState
        )

        // we have a menu item to show in action bar
        setHasOptionsMenu(true)

        contentView = view.findViewById(android.R.id.content)
        recyclerView = view.findViewById(android.R.id.list)

        emptyTextView = view.findViewById(android.R.id.empty)
        emptyTextView?.text = getString(R.string.summary_no_data)

        fab?.setOnClickListener {
            ((activity as AbstractPagerFragmentActivity?))?.also {
                input?.clearCurrentSelectedInputTaxon()
                it.goToPreviousPage()
                it.goToNextPage()
            }
        }

        adapter = InputTaxaSummaryRecyclerViewAdapter(object :
            AbstractListItemRecyclerViewAdapter.OnListItemRecyclerViewAdapterListener<AbstractInputTaxon> {
            override fun onClick(item: AbstractInputTaxon) {
                input?.setCurrentSelectedInputTaxonId(item.taxon.id)
                (activity as AbstractPagerFragmentActivity?)?.goToPageByKey(R.string.pager_fragment_information_title)
            }

            override fun onLongClicked(
                position: Int,
                item: AbstractInputTaxon
            ) {
                adapter?.remove(item)
                input?.removeInputTaxon(item.taxon.id)
                (activity as AbstractPagerFragmentActivity?)?.validateCurrentPage()

                context?.run {
                    @Suppress("DEPRECATION")
                    getSystemService(
                        this,
                        Vibrator::class.java
                    )?.vibrate(100)
                }

                contentView?.also {
                    Snackbar.make(
                        it,
                        R.string.summary_snackbar_input_taxon_deleted,
                        Snackbar.LENGTH_LONG
                    )
                        .setAction(
                            R.string.summary_snackbar_input_taxon_undo
                        ) {
                            adapter?.add(
                                item,
                                position
                            )
                            input?.addInputTaxon(item)
                        }
                        .addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                            override fun onDismissed(
                                transientBottomBar: Snackbar?,
                                event: Int
                            ) {
                                super.onDismissed(
                                    transientBottomBar,
                                    event
                                )

                                (activity as AbstractPagerFragmentActivity?)?.validateCurrentPage()

                                // check if this step is still valid and apply automatic redirection if any
                                if (!this@InputTaxaSummaryFragment.validate()) {
                                    val context = context ?: return

                                    Toast.makeText(
                                        context,
                                        R.string.summary_toast_no_input_taxon,
                                        Toast.LENGTH_LONG
                                    )
                                        .show()

                                    ((activity as AbstractPagerFragmentActivity?))?.also { activity ->
                                        activity.goToPreviousPage()
                                        activity.goToNextPage()
                                    }
                                }
                            }
                        })
                        .show()
                }
            }

            override fun showEmptyTextView(show: Boolean) {
                if (emptyTextView?.visibility == View.VISIBLE == show) {
                    return
                }

                if (show) {
                    emptyTextView?.startAnimation(
                        AnimationUtils.loadAnimation(
                            context,
                            android.R.anim.fade_in
                        )
                    )
                    emptyTextView?.visibility = View.VISIBLE
                } else {
                    emptyTextView?.startAnimation(
                        AnimationUtils.loadAnimation(
                            context,
                            android.R.anim.fade_out
                        )
                    )
                    emptyTextView?.visibility = View.GONE
                }
            }
        })

        with(recyclerView as RecyclerView) {
            layoutManager = LinearLayoutManager(context)
            adapter = this@InputTaxaSummaryFragment.adapter

            val dividerItemDecoration = DividerItemDecoration(
                context,
                (layoutManager as LinearLayoutManager).orientation
            )
            addItemDecoration(dividerItemDecoration)
        }
    }

    override fun onCreateOptionsMenu(
        menu: Menu,
        inflater: MenuInflater
    ) {

        super.onCreateOptionsMenu(
            menu,
            inflater
        )

        inflater.inflate(
            R.menu.comment,
            menu
        )
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)

        val commentItem = menu.findItem(R.id.menu_comment)
        commentItem.title =
            if (TextUtils.isEmpty(input?.comment)) getString(R.string.action_comment_add) else getString(
                R.string.action_comment_edit
            )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_comment -> {
                val supportFragmentManager = activity?.supportFragmentManager ?: return true

                CommentDialogFragment.newInstance(input?.comment)
                    .apply {
                        setOnCommentDialogFragmentListener(onCommentDialogFragmentListener)
                        show(
                            supportFragmentManager,
                            COMMENT_DIALOG_FRAGMENT
                        )
                    }

                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun getResourceTitle(): Int {
        return R.string.pager_fragment_summary_title
    }

    override fun getSubtitle(): CharSequence? {
        return input?.getCurrentSelectedInputTaxon()?.taxon?.name
    }

    override fun pagingEnabled(): Boolean {
        return true
    }

    override fun validate(): Boolean {
        return this.input?.getCurrentSelectedInputTaxon() != null
    }

    override fun refreshView() {
        adapter?.setItems(input?.getInputTaxa() ?: emptyList())
    }

    override fun setInput(input: AbstractInput) {
        this.input = input as Input
    }

    companion object {

        private const val COMMENT_DIALOG_FRAGMENT = "comment_dialog_fragment"

        /**
         * Use this factory method to create a new instance of [InputTaxaSummaryFragment].
         *
         * @return A new instance of [InputTaxaSummaryFragment]
         */
        @JvmStatic
        fun newInstance() = InputTaxaSummaryFragment()
    }
}
