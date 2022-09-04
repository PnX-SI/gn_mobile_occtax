package fr.geonature.occtax.ui.input.summary

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.getSystemService
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import fr.geonature.commons.input.AbstractInputTaxon
import fr.geonature.commons.ui.adapter.AbstractListItemRecyclerViewAdapter
import fr.geonature.occtax.R
import fr.geonature.occtax.settings.InputDateSettings
import fr.geonature.occtax.ui.input.AbstractInputFragment
import fr.geonature.occtax.ui.shared.dialog.InputDateDialogFragment
import java.util.Date

/**
 * Summary of all edited taxa.
 *
 * @author S. Grimault
 */
class InputTaxaSummaryFragment : AbstractInputFragment() {

    private lateinit var dateSettings: InputDateSettings

    private var adapter: InputTaxaSummaryRecyclerViewAdapter? = null
    private var recyclerView: RecyclerView? = null
    private var emptyTextView: TextView? = null
    private var fab: ExtendedFloatingActionButton? = null
    private var startEditTaxon = false

    private val onInputDateDialogFragmentListener =
        object : InputDateDialogFragment.OnInputDateDialogFragmentListener {
            override fun onDatesChanged(startDate: Date, endDate: Date) {
                input?.apply {
                    this.startDate = startDate
                    this.endDate = endDate
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dateSettings = arguments?.getParcelable(ARG_DATE_SETTINGS) ?: InputDateSettings.DEFAULT

        val supportFragmentManager = activity?.supportFragmentManager ?: return

        (supportFragmentManager.findFragmentByTag(INPUT_DATE_DIALOG_FRAGMENT) as InputDateDialogFragment?)?.also {
            it.setOnInputDateDialogFragmentListenerListener(onInputDateDialogFragmentListener)
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

        recyclerView = view.findViewById(android.R.id.list)
        fab = view.findViewById(R.id.fab)

        emptyTextView = view.findViewById(android.R.id.empty)
        emptyTextView?.text = getString(R.string.summary_no_data)

        fab?.apply {
            setText(R.string.action_add_taxon)
            extend()
            setOnClickListener {
                startEditTaxon = true
                input?.clearCurrentSelectedInputTaxon()
                listener.startEditTaxon()
            }
        }

        adapter = InputTaxaSummaryRecyclerViewAdapter(object :
            AbstractListItemRecyclerViewAdapter.OnListItemRecyclerViewAdapterListener<AbstractInputTaxon> {
            override fun onClick(item: AbstractInputTaxon) {
                startEditTaxon = true
                input?.setCurrentSelectedInputTaxonId(item.taxon.id)
                listener.startEditTaxon()
            }

            override fun onLongClicked(
                position: Int,
                item: AbstractInputTaxon
            ) {
                context?.run {
                    getSystemService(
                        this,
                        Vibrator::class.java
                    )?.vibrate(
                        VibrationEffect.createOneShot(
                            100,
                            VibrationEffect.DEFAULT_AMPLITUDE
                        )
                    )

                    AlertDialog.Builder(this)
                        .setTitle(R.string.alert_dialog_taxon_delete_title)
                        .setPositiveButton(
                            R.string.alert_dialog_ok
                        ) { dialog, _ ->
                            adapter?.remove(item)
                            input?.removeInputTaxon(item.taxon.id)
                            listener.validateCurrentPage()

                            dialog.dismiss()
                        }
                        .setNegativeButton(
                            R.string.alert_dialog_cancel
                        ) { dialog, _ -> dialog.dismiss() }
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

    override fun onResume() {
        super.onResume()

        Handler(Looper.getMainLooper()).post {
            // bypass this page and redirect to the previous one if we have started editing the first taxon
            if (startEditTaxon && input?.getInputTaxa()?.isEmpty() == true) {
                startEditTaxon = false
                listener.goToPreviousPage()
                return@post
            }

            // no taxon added yet: redirect to the edit taxon pages
            if (input?.getInputTaxa()?.isEmpty() == true) {
                startEditTaxon = true
                listener.startEditTaxon()
                return@post
            }

            // finish taxon editing workflow
            startEditTaxon = false
            listener.finishEditTaxon()
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
            R.menu.date,
            menu
        )
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)

        val dateMenuItem = menu.findItem(R.id.menu_date)
        dateMenuItem.isVisible = dateSettings.endDateSettings != null
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_date -> {
                val supportFragmentManager = activity?.supportFragmentManager ?: return true

                InputDateDialogFragment.newInstance(
                    InputDateSettings(endDateSettings = dateSettings.endDateSettings),
                    input?.startDate ?: Date(),
                    input?.endDate
                )
                    .apply {
                        setOnInputDateDialogFragmentListenerListener(onInputDateDialogFragmentListener)
                        show(
                            supportFragmentManager,
                            INPUT_DATE_DIALOG_FRAGMENT
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
        val context = context ?: return null

        return input?.getInputTaxa()?.size?.let {
            context.resources.getQuantityString(
                R.plurals.summary_taxa_subtitle,
                it,
                it
            )
        }
    }

    override fun pagingEnabled(): Boolean {
        return true
    }

    override fun validate(): Boolean {
        return startEditTaxon || this.input?.getInputTaxa()?.isNotEmpty() ?: false
    }

    override fun refreshView() {
        // FIXME: this is a workaround to refresh adapter's list as getInputTaxa() items are not immutable...
        if ((adapter?.itemCount ?: 0) > 0) adapter?.clear()
        adapter?.setItems(input?.getInputTaxa() ?: emptyList())
    }

    companion object {

        private const val INPUT_DATE_DIALOG_FRAGMENT = "input_date_dialog_fragment"
        private const val ARG_DATE_SETTINGS = "arg_date_settings"

        /**
         * Use this factory method to create a new instance of [InputTaxaSummaryFragment].
         *
         * @return A new instance of [InputTaxaSummaryFragment]
         */
        @JvmStatic
        fun newInstance(dateSettings: InputDateSettings) = InputTaxaSummaryFragment().apply {
            arguments = Bundle().apply {
                putParcelable(
                    ARG_DATE_SETTINGS,
                    dateSettings
                )
            }
        }
    }
}
