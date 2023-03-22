package fr.geonature.occtax.ui.home

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import fr.geonature.commons.lifecycle.observe
import fr.geonature.commons.ui.adapter.AbstractListItemRecyclerViewAdapter
import fr.geonature.commons.util.ThemeUtils.getColor
import fr.geonature.occtax.R
import fr.geonature.occtax.features.record.domain.ObservationRecord
import fr.geonature.occtax.features.record.presentation.ObservationRecordViewModel
import fr.geonature.occtax.settings.AppSettings
import fr.geonature.occtax.settings.AppSettingsViewModel
import org.tinylog.Logger

/**
 * [Fragment] to show all current [ObservationRecord].
 *
 * @author S. Grimault
 */
@AndroidEntryPoint
class LastObservationRecordsFragment : Fragment(R.layout.fragment_recycler_view_fab) {

    private val appSettingsViewModel: AppSettingsViewModel by activityViewModels()
    private val observationRecordViewModel: ObservationRecordViewModel by viewModels()

    private var fab: ExtendedFloatingActionButton? = null
    private var listener: OnLastObservationRecordsFragmentListener? = null
    private var adapter: ObservationRecordRecyclerViewAdapter? = null
    private var statusesFilter: MutableList<ObservationRecord.Status> = mutableListOf(
        ObservationRecord.Status.DRAFT,
        ObservationRecord.Status.TO_SYNC
    )
    private var appSettings: AppSettings? = null
    private var isSyncRunning = false
    private var hasObservationRecordsReadyToSynchronize = false

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        activity?.actionBar?.subtitle = getString(R.string.home_last_inputs)

        // we have a menu item to show in action bar
        setHasOptionsMenu(true)

        val recyclerView = view.findViewById<RecyclerView>(android.R.id.list)
        val emptyTextView = view.findViewById<TextView>(android.R.id.empty)
            .apply {
                text = getString(R.string.home_no_input)
            }
        val progressBar = view.findViewById<ProgressBar>(android.R.id.progress)
            .apply { visibility = View.VISIBLE }

        adapter = ObservationRecordRecyclerViewAdapter(object :
            AbstractListItemRecyclerViewAdapter.OnListItemRecyclerViewAdapterListener<ObservationRecord> {
            override fun onClick(item: ObservationRecord) {
                Logger.info { "observation record selected: ${item.id}" }

                listener?.onStartEditObservationRecord(item)
            }

            override fun onLongClicked(
                position: Int,
                item: ObservationRecord
            ) {
                val context = context ?: return

                ContextCompat.getSystemService(
                    context,
                    Vibrator::class.java
                )
                    ?.vibrate(
                        VibrationEffect.createOneShot(
                            100,
                            VibrationEffect.DEFAULT_AMPLITUDE
                        )
                    )

                AlertDialog.Builder(context)
                    .setTitle(R.string.alert_dialog_input_delete_title)
                    .setPositiveButton(
                        R.string.alert_dialog_ok
                    ) { dialog, _ ->
                        observationRecordViewModel.delete(item)
                        dialog.dismiss()
                    }
                    .setNegativeButton(
                        R.string.alert_dialog_cancel
                    ) { dialog, _ -> dialog.dismiss() }
                    .show()
            }

            override fun showEmptyTextView(show: Boolean) {
                progressBar?.visibility = View.GONE

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

        fab = view.findViewById<ExtendedFloatingActionButton?>(R.id.fab)
            ?.apply {
                hide()
                text = getString(R.string.action_new_input)
                extend()
                setOnClickListener { listener?.onStartEditObservationRecord() }
            }

        with(recyclerView) {
            layoutManager = LinearLayoutManager(context)
            adapter = this@LastObservationRecordsFragment.adapter

            val dividerItemDecoration = DividerItemDecoration(
                context,
                (layoutManager as LinearLayoutManager).orientation
            )
            addItemDecoration(dividerItemDecoration)
        }

        configureAppSettingsViewModel()
        configureObservationRecordViewModel()
    }

    override fun onResume() {
        super.onResume()

        appSettings?.run {
            loadObservationRecords()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        context.takeIf { it is OnLastObservationRecordsFragmentListener }
            ?.let { it as OnLastObservationRecordsFragmentListener }
            ?.also {
                listener = it
            } ?: throw RuntimeException(
            "$context must implement ${OnLastObservationRecordsFragmentListener::class.java.simpleName}"
        )
    }

    override fun onCreateOptionsMenu(
        menu: Menu,
        inflater: MenuInflater
    ) {
        super.onCreateOptionsMenu(
            menu,
            inflater
        )

        with(inflater) {
            inflate(
                R.menu.sync,
                menu
            )
            inflate(
                R.menu.status,
                menu
            )
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)

        menu.findItem(R.id.menu_sync)
            ?.apply {
                isVisible = !isSyncRunning
                isEnabled = hasObservationRecordsReadyToSynchronize
            }
        menu.findItem(R.id.menu_sync_in_progress)
            ?.apply {
                isVisible = isSyncRunning
                (actionView as ProgressBar).apply {
                    indeterminateTintList = ColorStateList.valueOf(
                        getColor(
                            context,
                            android.R.attr.textColorPrimary
                        )
                    )
                    setPadding(
                        0,
                        resources.getDimensionPixelSize(R.dimen.text_margin),
                        0,
                        resources.getDimensionPixelSize(R.dimen.text_margin)
                    )
                }
            }
        menu.findItem(R.id.menu_status_draft)
            ?.apply {
                isChecked = statusesFilter.contains(ObservationRecord.Status.DRAFT)
            }
        menu.findItem(R.id.menu_status_to_sync)?.apply {
            isChecked = statusesFilter.contains(ObservationRecord.Status.TO_SYNC)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_sync -> {
                observationRecordViewModel.synchronizeObservationRecords()
                true
            }
            R.id.menu_status_draft -> {
                item.isChecked = !item.isChecked

                if (item.isChecked) statusesFilter.add(ObservationRecord.Status.DRAFT)
                else statusesFilter.remove(ObservationRecord.Status.DRAFT)

                loadObservationRecords()
                true
            }
            R.id.menu_status_to_sync -> {
                item.isChecked = !item.isChecked

                if (item.isChecked) statusesFilter.add(ObservationRecord.Status.TO_SYNC)
                else statusesFilter.remove(ObservationRecord.Status.TO_SYNC)

                loadObservationRecords()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun configureAppSettingsViewModel() {
        appSettingsViewModel.appSettings.observe(viewLifecycleOwner) {
            appSettings = it

            if (it == null) {
                fab?.hide()
                adapter?.clear()

                return@observe
            }

            fab?.show()
            loadObservationRecords()
        }
    }

    private fun configureObservationRecordViewModel() {
        with(observationRecordViewModel) {
            observe(
                observationRecords,
                ::handleObservationRecords
            )
            observe(isSyncRunning) {
                this@LastObservationRecordsFragment.isSyncRunning = it
                activity?.invalidateOptionsMenu()
            }
            observe(hasObservationRecordsReadyToSynchronize) {
                this@LastObservationRecordsFragment.hasObservationRecordsReadyToSynchronize = it
                activity?.invalidateOptionsMenu()
            }
        }
    }

    private fun loadObservationRecords() {
        if (statusesFilter.isEmpty()) {
            statusesFilter.addAll(listOf(ObservationRecord.Status.DRAFT, ObservationRecord.Status.TO_SYNC))
        }

        observationRecordViewModel.getAll { input -> statusesFilter.any { input.status == it } }
    }

    private fun handleObservationRecords(observationRecords: List<ObservationRecord>) {
        adapter?.setItems(observationRecords)
    }

    /**
     * Callback used by [LastObservationRecordsFragment].
     */
    interface OnLastObservationRecordsFragmentListener {

        /**
         * Called when we want to start editing a given [ObservationRecord].
         * If no [ObservationRecord] was given, creates a new one.
         */
        fun onStartEditObservationRecord(selectedObservationRecord: ObservationRecord? = null)
    }

    companion object {

        /**
         * Use this factory method to create a new instance of [LastObservationRecordsFragment].
         *
         * @return A new instance of [LastObservationRecordsFragment]
         */
        @JvmStatic
        fun newInstance() = LastObservationRecordsFragment()
    }
}