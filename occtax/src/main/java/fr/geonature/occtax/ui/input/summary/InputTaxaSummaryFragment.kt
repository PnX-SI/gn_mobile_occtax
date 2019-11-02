package fr.geonature.occtax.ui.input.summary

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import fr.geonature.commons.input.AbstractInput
import fr.geonature.commons.input.AbstractInputTaxon
import fr.geonature.occtax.R
import fr.geonature.occtax.input.Input
import fr.geonature.occtax.ui.input.IInputFragment
import fr.geonature.occtax.ui.shared.adapter.ListItemRecyclerViewAdapter
import fr.geonature.occtax.ui.shared.dialog.CommentDialogFragment
import fr.geonature.viewpager.ui.AbstractPagerFragmentActivity
import fr.geonature.viewpager.ui.IValidateFragment
import kotlinx.android.synthetic.main.fragment_recycler_view_fab.content
import kotlinx.android.synthetic.main.fragment_recycler_view_fab.empty
import kotlinx.android.synthetic.main.fragment_recycler_view_fab.fab
import kotlinx.android.synthetic.main.fragment_recycler_view_fab.list

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

    private val onCommentDialogFragmentListener = object : CommentDialogFragment.OnCommentDialogFragmentListener {
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

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_recycler_view_fab,
                                container,
                                false)
    }

    override fun onViewCreated(
            view: View,
            savedInstanceState: Bundle?) {
        super.onViewCreated(view,
                            savedInstanceState)

        // we have a menu item to show in action bar
        setHasOptionsMenu(true)

        empty.text = getString(R.string.summary_no_data)

        fab.setOnClickListener {
            input?.clearCurrentSelectedInputTaxon()

            ((activity as AbstractPagerFragmentActivity?))?.also {
                if (this@InputTaxaSummaryFragment.validate()) {
                    it.goToPageByKey(R.string.pager_fragment_taxa_title)

                    return@also
                }

                it.goToPreviousPage()
                it.goToNextPage()
            }
        }

        adapter = InputTaxaSummaryRecyclerViewAdapter(object : ListItemRecyclerViewAdapter.OnListItemRecyclerViewAdapterListener<AbstractInputTaxon> {
            override fun onClick(item: AbstractInputTaxon) {
                input?.setCurrentSelectedInputTaxonId(item.taxon.id)
                (activity as AbstractPagerFragmentActivity?)?.goToPageByKey(R.string.pager_fragment_information_title)
            }

            override fun onLongClicked(position: Int,
                                       item: AbstractInputTaxon) {
                adapter?.remove(item)
                input?.removeInputTaxon(item.taxon.id)
                (activity as AbstractPagerFragmentActivity?)?.validateCurrentPage()

                Snackbar.make(content,
                              R.string.summary_snackbar_input_taxon_deleted,
                              Snackbar.LENGTH_LONG)
                    .setAction(R.string.summary_snackbar_input_taxon_undo
                    ) {
                        adapter?.add(item,
                                     position)
                        input?.addInputTaxon(item)
                    }
                    .addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                        override fun onDismissed(transientBottomBar: Snackbar?,
                                                 event: Int) {
                            super.onDismissed(transientBottomBar,
                                              event)

                            (activity as AbstractPagerFragmentActivity?)?.validateCurrentPage()

                            // check if this step is still valid and apply automatic redirection if any
                            if (!this@InputTaxaSummaryFragment.validate()) {
                                val context = context ?: return

                                Toast.makeText(context,
                                               R.string.summary_toast_no_input_taxon,
                                               Toast.LENGTH_LONG)
                                    .show()

                                ((activity as AbstractPagerFragmentActivity?))?.also {
                                    it.goToPreviousPage()
                                    it.goToNextPage()
                                }
                            }
                        }
                    })
                    .show()
            }

            override fun showEmptyTextView(show: Boolean) {
                if (empty.visibility == View.VISIBLE == show) {
                    return
                }

                if (show) {
                    empty.startAnimation(AnimationUtils.loadAnimation(context,
                                                                      android.R.anim.fade_in))
                    empty.visibility = View.VISIBLE

                }
                else {
                    empty.startAnimation(AnimationUtils.loadAnimation(context,
                                                                      android.R.anim.fade_out))
                    empty.visibility = View.GONE
                }
            }
        })

        with(list as RecyclerView) {
            layoutManager = LinearLayoutManager(context)
            adapter = this@InputTaxaSummaryFragment.adapter

            val dividerItemDecoration = DividerItemDecoration(context,
                                                              (layoutManager as LinearLayoutManager).orientation)
            addItemDecoration(dividerItemDecoration)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu,
                                     inflater: MenuInflater) {

        super.onCreateOptionsMenu(menu,
                                  inflater)

        inflater.inflate(R.menu.comment,
                         menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)

        val commentItem = menu.findItem(R.id.menu_comment)
        commentItem.title = if (TextUtils.isEmpty(input?.comment)) getString(R.string.action_comment_add) else getString(R.string.action_comment_edit)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_comment -> {
                val supportFragmentManager = activity?.supportFragmentManager ?: return true

                CommentDialogFragment.newInstance(input?.comment)
                    .apply {
                        setOnCommentDialogFragmentListener(onCommentDialogFragmentListener)
                        show(supportFragmentManager,
                             COMMENT_DIALOG_FRAGMENT)
                    }

                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun getResourceTitle(): Int {
        return R.string.pager_fragment_summary_title
    }

    override fun pagingEnabled(): Boolean {
        return true
    }

    override fun validate(): Boolean {
        return input?.getInputTaxa()?.isNotEmpty() ?: false
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