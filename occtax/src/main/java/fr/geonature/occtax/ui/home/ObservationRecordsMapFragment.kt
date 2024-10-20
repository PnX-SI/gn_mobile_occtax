package fr.geonature.occtax.ui.home

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.setMargins
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.chip.Chip
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import dagger.hilt.android.AndroidEntryPoint
import fr.geonature.commons.lifecycle.observe
import fr.geonature.commons.util.ThemeUtils.getAccentColor
import fr.geonature.commons.util.ThemeUtils.getColor
import fr.geonature.maps.jts.geojson.GeometryUtils
import fr.geonature.maps.settings.MapSettings
import fr.geonature.maps.ui.MapFragment
import fr.geonature.maps.util.DrawableUtils.createScaledDrawable
import fr.geonature.occtax.R
import fr.geonature.occtax.features.record.domain.ObservationRecord
import fr.geonature.occtax.features.record.presentation.ObservationRecordViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.FolderOverlay
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

/**
 * [Fragment] to show all current [ObservationRecord] on the map.
 *
 * @author S. Grimault
 */
@AndroidEntryPoint
class ObservationRecordsMapFragment : MapFragment() {

    private val observationRecordViewModel: ObservationRecordViewModel by viewModels()

    private var listener: OnObservationRecordListener? = null

    private var bottomSheetBehavior: BottomSheetBehavior<ViewGroup>? = null
    private var bottomSheetContent: ViewGroup? = null

    private var statusesFilter: MutableList<ObservationRecord.Status> = mutableListOf(
        ObservationRecord.Status.DRAFT,
        ObservationRecord.Status.TO_SYNC
    )
    private var isSyncRunning = false
    private var hasObservationRecordsReadyToSynchronize = false
    private var observationRecordsOverlay: FolderOverlay? = null
    private var selectedObservationRecord: ObservationRecord? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        onConfigureBottomSheetListener = { parent, bottomSheetBehavior ->
            bottomSheetBehavior.isDraggable = false
            bottomSheetBehavior.isFitToContents = true
            parent.layoutParams = parent.layoutParams.apply {
                height = CoordinatorLayout.LayoutParams.WRAP_CONTENT
            }

            parent.addView(
                LayoutInflater.from(parent.context)
                    .inflate(
                        R.layout.list_item_input_edit,
                        parent,
                        false
                    )
                    .apply {
                        this@ObservationRecordsMapFragment.bottomSheetContent = (this as ViewGroup)
                    })

            this.bottomSheetBehavior = bottomSheetBehavior
        }
        onConfigureBottomFabsListener = { parent ->
            parent.addView(ExtendedFloatingActionButton(parent.context).apply {
                backgroundTintList = ColorStateList.valueOf(getAccentColor(parent.context))
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                    .apply {
                        setMargins(parent.context.resources.getDimensionPixelSize(R.dimen.padding_default))
                    }
                text = getString(R.string.action_new_input)
                setIconResource(R.drawable.ic_add)
                elevation = parent.context.resources.getDimension(R.dimen.fab_elevation)
                extend()
                setOnClickListener { listener?.onStartEditObservationRecord() }
            })
        }
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(
            view,
            savedInstanceState
        )

        activity?.actionBar?.subtitle = getString(R.string.home_last_inputs)

        // we have a menu item to show in action bar
        setHasOptionsMenu(true)

        configureObservationRecordViewModel()

        mapView.overlays.add(MapEventsOverlay(object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                clearSelection()
                return true
            }

            override fun longPressHelper(p: GeoPoint?): Boolean {
                return false
            }
        }))
        mapView.addMapListener(object : MapListener {
            override fun onScroll(event: ScrollEvent?): Boolean {
                return true
            }

            override fun onZoom(event: ZoomEvent?): Boolean {
                clearSelection()
                return true
            }
        })
    }

    override fun onResume() {
        super.onResume()

        loadObservationRecords()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        context.takeIf { it is OnObservationRecordListener }
            ?.let { it as OnObservationRecordListener }
            ?.also {
                listener = it
            } ?: throw RuntimeException(
            "$context must implement ${OnObservationRecordListener::class.java.simpleName}"
        )
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
                R.menu.sync,
                menu
            )
            inflate(
                R.menu.status_filter,
                menu
            )
        }
    }

    @Deprecated("Deprecated in Java")
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
        menu.findItem(R.id.menu_status_to_sync)
            ?.apply {
                isChecked = statusesFilter.contains(ObservationRecord.Status.TO_SYNC)
            }
    }

    @Deprecated("Deprecated in Java")
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

    private fun configureObservationRecordViewModel() {
        with(observationRecordViewModel) {
            observe(
                observationRecords,
                ::handleObservationRecords
            )
            observe(isSyncRunning) {
                this@ObservationRecordsMapFragment.isSyncRunning = it
                activity?.invalidateOptionsMenu()
            }
            observe(hasObservationRecordsReadyToSynchronize) {
                this@ObservationRecordsMapFragment.hasObservationRecordsReadyToSynchronize = it
                activity?.invalidateOptionsMenu()
            }
        }
    }

    private fun loadObservationRecords() {
        if (statusesFilter.isEmpty()) {
            statusesFilter.addAll(
                listOf(
                    ObservationRecord.Status.DRAFT,
                    ObservationRecord.Status.TO_SYNC
                )
            )
        }

        observationRecordViewModel.getAll { input -> statusesFilter.any { input.status == it } }
    }

    private fun handleObservationRecords(observationRecords: List<ObservationRecord>) {
        val context = context ?: return

        getOverlays { it is FolderOverlay && it.name == LAYER_RECORDS }.forEach {
            removeOverlay(it)
        }

        observationRecordsOverlay = FolderOverlay().apply {
            name = LAYER_RECORDS
            observationRecords.forEach { observationRecord ->
                observationRecord.geometry?.centroid
                    ?.also {
                        add(
                            Marker(mapView).apply {
                                id = observationRecord.internalId.toString()
                                position = GeometryUtils.fromPoint(it)
                                setAnchor(
                                    Marker.ANCHOR_CENTER,
                                    Marker.ANCHOR_BOTTOM
                                )
                                setMarkerIcon(
                                    this,
                                    context.resources.getColor(
                                        getObservationRecordStatusColor(observationRecord),
                                        context.theme
                                    ),
                                    2.0f
                                )
                                isDraggable = false
                                infoWindow = null
                                setOnMarkerClickListener { marker, _ ->
                                    selectObservationRecord(
                                        observationRecord,
                                        marker
                                    )
                                    centerMapToSelection(marker)

                                    true
                                }
                            },
                        )
                    }
            }
        }
            .apply {
                mapView.zoomToBoundingBox(
                    bounds,
                    false,
                    64
                )
                addOverlay(this)
                mapView.invalidate()
            }
    }

    private fun clearSelection(): Marker? {
        return observationRecordsOverlay?.items
            ?.filterIsInstance<Marker>()
            ?.firstOrNull { it.id == selectedObservationRecord?.internalId?.toString() }
            ?.also { marker ->
                selectedObservationRecord?.also { observationRecord ->
                    deselectObservationRecord(
                        observationRecord,
                        marker
                    )
                }
            }
    }

    private fun setMarkerIcon(
        marker: Marker,
        @ColorInt tintColor: Int,
        scale: Float = 1.0f
    ) {
        val context = context ?: return

        marker.icon = createScaledDrawable(
            context,
            fr.geonature.maps.R.drawable.ic_poi,
            tintColor,
            scale
        )
    }

    private fun selectObservationRecord(observationRecord: ObservationRecord, marker: Marker) {
        val context = context ?: return

        selectedObservationRecord = observationRecord

        bottomSheetContent?.apply {
            findViewById<TextView>(android.R.id.text1).text = context.getString(
                R.string.home_input_created_at,
                DateFormat.format(
                    context.getString(R.string.home_input_date),
                    observationRecord.dates.start
                )
            )
            findViewById<TextView>(android.R.id.text2).text =
                if (observationRecord.taxa.taxa.isNotEmpty())
                    context.resources.getQuantityString(
                        R.plurals.home_input_taxa_count,
                        observationRecord.taxa.taxa.size,
                        observationRecord.taxa.taxa.size
                    ) else context.getString(R.string.home_input_taxa_count_empty)
            with(findViewById<Chip>(R.id.chip_status)) {
                isDuplicateParentStateEnabled = true
                text = context.resources.getIdentifier(
                    "home_input_status_${observationRecord.status.name.lowercase()}",
                    "string",
                    context.packageName
                )
                    .takeIf { it > 0 }
                    ?.let { context.getString(it) }
                    ?: context.getString(R.string.home_input_status_draft)
                isCloseIconVisible = false
                isEnabled = false
                setChipBackgroundColorResource(getObservationRecordStatusColor(observationRecord))
                chipIcon =
                    if (observationRecord.status == ObservationRecord.Status.SYNC_IN_PROGRESS) CircularProgressIndicator(context).apply {
                        isIndeterminate = true
                        indicatorSize = 44
                        trackThickness = 4
                        indicatorInset = 4
                        setIndicatorColor(
                            getColor(
                                context,
                                com.google.android.material.R.attr.colorOnPrimary
                            )
                        )
                    }.indeterminateDrawable?.apply {
                        start()
                        invalidate()
                    } else null

                setTextColor(
                    getColor(
                        context,
                        com.google.android.material.R.attr.colorOnPrimary
                    )
                )
            }
            findViewById<Button>(android.R.id.edit).setOnClickListener {
                deselectObservationRecord(
                    observationRecord,
                    marker
                )
                listener?.onStartEditObservationRecord(observationRecord)
            }

            bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
        }

        setMarkerIcon(
            marker,
            getAccentColor(context),
            2.5f
        )
        mapView.invalidate()
    }

    private fun deselectObservationRecord(observationRecord: ObservationRecord, marker: Marker) {
        selectedObservationRecord = null
        bottomSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN

        val context = context ?: return

        setMarkerIcon(
            marker,
            context.resources.getColor(
                getObservationRecordStatusColor(observationRecord),
                context.theme
            ),
            2.0f
        )
        mapView.invalidate()
    }

    private fun centerMapToSelection(marker: Marker) {
        mapView.controller.animateTo(
            marker.position,
            mapView.zoomLevelDouble,
            Configuration.getInstance().animationSpeedDefault.toLong()
        )
    }

    @ColorRes
    private fun getObservationRecordStatusColor(observationRecord: ObservationRecord): Int {
        val context = context ?: return R.color.input_status_draft

        return context.resources.getIdentifier(
            "input_status_${observationRecord.status.name.lowercase()}",
            "color",
            context.packageName
        )
            .takeIf { it > 0 } ?: R.color.input_status_draft
    }

    companion object {

        private const val LAYER_RECORDS = "records"

        /**
         * Use this factory method to create a new instance of [ObservationRecordsMapFragment].
         *
         * @return A new instance of [ObservationRecordsMapFragment]
         */
        @JvmStatic
        fun newInstance(mapSettings: MapSettings) = ObservationRecordsMapFragment().apply {
            arguments = Bundle().apply {
                putParcelable(
                    ARG_MAP_SETTINGS,
                    mapSettings
                )
            }
        }
    }
}