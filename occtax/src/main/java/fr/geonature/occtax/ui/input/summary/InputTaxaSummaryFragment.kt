package fr.geonature.occtax.ui.input.summary

import android.app.Activity
import android.content.Intent
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
import android.widget.ProgressBar
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.view.get
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import fr.geonature.commons.data.entity.Taxonomy
import fr.geonature.commons.ui.adapter.AbstractListItemRecyclerViewAdapter
import fr.geonature.compat.content.getParcelableArrayExtraCompat
import fr.geonature.compat.os.getParcelableArrayCompat
import fr.geonature.compat.os.getParcelableCompat
import fr.geonature.occtax.R
import fr.geonature.occtax.features.record.domain.TaxonRecord
import fr.geonature.occtax.features.settings.domain.InputDateSettings
import fr.geonature.occtax.ui.input.AbstractInputFragment
import fr.geonature.occtax.ui.input.taxa.Filter
import fr.geonature.occtax.ui.input.taxa.FilterTaxonomy
import fr.geonature.occtax.ui.input.taxa.TaxaFilterActivity
import fr.geonature.occtax.ui.shared.dialog.InputDateDialogFragment
import java.util.Date

/**
 * Summary of all edited taxa.
 *
 * @author S. Grimault
 */
class InputTaxaSummaryFragment : AbstractInputFragment() {

    private lateinit var savedState: Bundle
    private lateinit var dateSettings: InputDateSettings
    private lateinit var taxaFilterResultLauncher: ActivityResultLauncher<Intent>

    private var adapter: InputTaxaSummaryRecyclerViewAdapter? = null
    private var progressBar: ProgressBar? = null
    private var emptyTextView: View? = null
    private var filterChipGroup: ChipGroup? = null
    private var startEditTaxon = false

    private val onInputDateDialogFragmentListener =
        object : InputDateDialogFragment.OnInputDateDialogFragmentListener {
            override fun onDatesChanged(startDate: Date, endDate: Date) {
                observationRecord?.dates?.also {
                    it.start = startDate
                    it.end = endDate
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        savedState = savedInstanceState ?: Bundle()
        dateSettings =
            arguments?.getParcelableCompat(ARG_DATE_SETTINGS) ?: InputDateSettings.DEFAULT

        val supportFragmentManager = activity?.supportFragmentManager ?: return

        (supportFragmentManager.findFragmentByTag(INPUT_DATE_DIALOG_FRAGMENT) as InputDateDialogFragment?)?.also {
            it.setOnInputDateDialogFragmentListenerListener(onInputDateDialogFragmentListener)
        }

        taxaFilterResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
                if ((activityResult.resultCode != Activity.RESULT_OK) || (activityResult.data == null)) {
                    return@registerForActivityResult
                }

                val selectedFilters =
                    activityResult.data?.getParcelableArrayExtraCompat<Filter<*>>(TaxaFilterActivity.EXTRA_SELECTED_FILTERS)
                        ?: emptyArray()
                applyFilters(*selectedFilters)
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(
            R.layout.fragment_input_summary,
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

        val recyclerView = view.findViewById<RecyclerView>(android.R.id.list)
        progressBar = view.findViewById(android.R.id.progress)
        emptyTextView = view.findViewById(android.R.id.empty)
        filterChipGroup = view.findViewById(R.id.chip_group_filter)

        view.findViewById<ExtendedFloatingActionButton>(R.id.fab)
            .apply {
                setOnClickListener {
                    startEditTaxon = true
                    observationRecord?.taxa?.selectedTaxonRecord = null
                    listener.startEditTaxon()
                }
            }

        adapter = InputTaxaSummaryRecyclerViewAdapter(object :
            AbstractListItemRecyclerViewAdapter.OnListItemRecyclerViewAdapterListener<TaxonRecord> {
            override fun onClick(item: TaxonRecord) {
                startEditTaxon = true
                observationRecord?.taxa?.selectedTaxonRecord = item
                listener.startEditTaxon()
            }

            override fun onLongClicked(
                position: Int,
                item: TaxonRecord
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
                            observationRecord?.taxa?.delete(item.taxon.id)
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(savedState.apply { putAll(outState) })
    }

    override fun onResume() {
        super.onResume()

        Handler(Looper.getMainLooper()).post {
            // bypass this page and redirect to the previous one if we have started editing the first taxon
            if (startEditTaxon && observationRecord?.taxa?.taxa?.isEmpty() == true) {
                startEditTaxon = false
                listener.goToPreviousPage()
                return@post
            }

            // no taxon added yet: redirect to the edit taxon pages
            if (observationRecord?.taxa?.taxa?.isEmpty() == true) {
                startEditTaxon = true
                listener.startEditTaxon()
                return@post
            }

            // finish taxon editing workflow
            startEditTaxon = false
            listener.finishEditTaxon()
        }
    }

    @Deprecated("Deprecated in Java")
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
                R.menu.date,
                menu
            )
            inflate(
                R.menu.filter,
                menu
            )
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)

        val dateMenuItem = menu.findItem(R.id.menu_date)
        dateMenuItem.isVisible = dateSettings.endDateSettings != null
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_date -> {
                val supportFragmentManager = activity?.supportFragmentManager ?: return true

                InputDateDialogFragment.newInstance(
                    InputDateSettings(endDateSettings = dateSettings.endDateSettings),
                    observationRecord?.dates?.start ?: Date(),
                    observationRecord?.dates?.end
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
            R.id.menu_filter -> {
                val context = context ?: return true

                taxaFilterResultLauncher.launch(
                    TaxaFilterActivity.newIntent(
                        context,
                        filter = getSelectedFilters().toTypedArray()
                    )
                )

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun getResourceTitle(): Int {
        return R.string.pager_fragment_summary_title
    }

    override fun getSubtitle(): CharSequence? {
        val context = context ?: return null

        return observationRecord?.taxa?.taxa?.size?.let {
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
        return startEditTaxon || (this.observationRecord?.taxa?.taxa ?: emptyList()).all {
            it.properties.isNotEmpty() && it.counting.counting.isNotEmpty()
        }
    }

    override fun refreshView() {
        // FIXME: this is a workaround to refresh adapter's list as getInputTaxa() items are not immutable...
        if ((adapter?.itemCount ?: 0) > 0) adapter?.clear()

        val selectedFilters =
            savedState.getParcelableArrayCompat<Filter<*>>(KEY_SELECTED_FILTERS)
                ?.toList() ?: emptyList()
        val filterByTaxonomy =
            selectedFilters.find { filter -> filter.type == Filter.FilterType.TAXONOMY }?.value as Taxonomy?

        adapter?.setItems((observationRecord?.taxa?.taxa ?: emptyList()).filter {
            val taxonomy = filterByTaxonomy ?: return@filter true

            // filter by kingdom only
            if (taxonomy.group == Taxonomy.ANY) {
                return@filter it.taxon.taxonomy.kingdom == taxonomy.kingdom
            }

            it.taxon.taxonomy == taxonomy
        })
    }

    private fun applyFilters(vararg filter: Filter<*>) {
        savedState.putParcelableArray(
            KEY_SELECTED_FILTERS,
            filter
        )

        val selectedTaxonomy =
            filter.find { it.type == Filter.FilterType.TAXONOMY }?.value as Taxonomy?

        filterByTaxonomy(selectedTaxonomy)
        refreshView()
    }

    private fun filterByTaxonomy(selectedTaxonomy: Taxonomy?) {
        val filterChipGroup = filterChipGroup ?: return
        val context = context ?: return

        val taxonomyChipsToDelete = arrayListOf<Chip>()

        for (i in 0 until filterChipGroup.childCount) {
            with(filterChipGroup[i]) {
                if (this is Chip && tag is Taxonomy) {
                    taxonomyChipsToDelete.add(this)
                }
            }
        }

        taxonomyChipsToDelete.forEach {
            filterChipGroup.removeView(it)
        }

        filterChipGroup.visibility = if (filterChipGroup.childCount > 0) View.VISIBLE else View.GONE

        if (selectedTaxonomy != null) {
            filterChipGroup.visibility = View.VISIBLE

            // build kingdom taxonomy filter chip
            with(
                LayoutInflater.from(context)
                    .inflate(
                        R.layout.chip,
                        filterChipGroup,
                        false
                    ) as Chip
            ) {
                tag = Taxonomy(selectedTaxonomy.kingdom)
                text = selectedTaxonomy.kingdom
                setOnClickListener {
                    applyFilters(*getSelectedFilters().filter { it.type != Filter.FilterType.TAXONOMY }
                        .toTypedArray())
                }
                setOnCloseIconClickListener {
                    applyFilters(*getSelectedFilters().filter { it.type != Filter.FilterType.TAXONOMY }
                        .toTypedArray())
                }

                filterChipGroup.addView(this)
            }

            // build group taxonomy filter chip
            if (selectedTaxonomy.group != Taxonomy.ANY) {
                with(
                    LayoutInflater.from(context)
                        .inflate(
                            R.layout.chip,
                            filterChipGroup,
                            false
                        ) as Chip
                ) {
                    tag = selectedTaxonomy
                    text = selectedTaxonomy.group
                    setOnClickListener {
                        applyFilters(*(getSelectedFilters().filter { filter -> filter.type != Filter.FilterType.TAXONOMY }
                            .toTypedArray() + mutableListOf(FilterTaxonomy(Taxonomy((it.tag as Taxonomy).kingdom)))))
                    }
                    setOnCloseIconClickListener {
                        applyFilters(*(getSelectedFilters().filter { filter -> filter.type != Filter.FilterType.TAXONOMY }
                            .toTypedArray() + mutableListOf(FilterTaxonomy(Taxonomy((it.tag as Taxonomy).kingdom)))))
                    }

                    filterChipGroup.addView(this)
                }
            }
        }
    }

    private fun getSelectedFilters(): List<Filter<*>> {
        return savedState.getParcelableArrayCompat<Filter<*>>(KEY_SELECTED_FILTERS)
            ?.toList() ?: emptyList()
    }

    companion object {

        private const val INPUT_DATE_DIALOG_FRAGMENT = "input_date_dialog_fragment"
        private const val ARG_DATE_SETTINGS = "arg_date_settings"
        private const val KEY_SELECTED_FILTERS = "key_selected_filters"

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
