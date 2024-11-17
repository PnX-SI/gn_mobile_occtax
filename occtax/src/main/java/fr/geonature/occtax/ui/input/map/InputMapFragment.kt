package fr.geonature.occtax.ui.input.map

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.view.menu.MenuBuilder
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import fr.geonature.commons.lifecycle.observeUntil
import fr.geonature.commons.util.ThemeUtils.getAccentColor
import fr.geonature.commons.util.ThemeUtils.getColor
import fr.geonature.compat.os.getParcelableCompat
import fr.geonature.maps.jts.geojson.GeometryUtils.fromPoint
import fr.geonature.maps.jts.geojson.GeometryUtils.toPoint
import fr.geonature.maps.settings.LayerStyleSettings
import fr.geonature.maps.settings.MapSettings
import fr.geonature.maps.ui.MapFragment
import fr.geonature.maps.ui.overlay.feature.FeatureCollectionOverlay
import fr.geonature.maps.ui.overlay.feature.filter.ContainsFeaturesFilter
import fr.geonature.occtax.R
import fr.geonature.occtax.features.record.domain.ObservationRecord
import fr.geonature.occtax.features.record.presentation.ObservationRecordViewModel
import fr.geonature.occtax.ui.input.IInputFragment
import fr.geonature.viewpager.model.IPageWithValidationFragment
import fr.geonature.viewpager.ui.OnPageFragmentListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import org.locationtech.jts.geom.Point
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView

/**
 * Simple [Fragment] embedding a [MapView] instance to edit a single POI on the map.
 *
 * @author S. Grimault
 */
class InputMapFragment : MapFragment(),
    IPageWithValidationFragment,
    IInputFragment {

    private val observationRecordViewModel: ObservationRecordViewModel by activityViewModels()

    private lateinit var listener: OnPageFragmentListener

    private var observationRecord: ObservationRecord? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        onSelectedPOIsListener = { pois ->
            if (pois.isEmpty()) {
                clearInputSelection()
            } else {
                selectPOI(pois[0])
            }
        }
        onVectorLayersChangedListener = {
            if (it.isNotEmpty()) {
                val geometry = observationRecord?.geometry

                if (geometry != null && geometry is Point) {
                    selectPOI(fromPoint(geometry))
                }
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is OnPageFragmentListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement ${OnPageFragmentListener::class.simpleName}")
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

        // we have a menu item to show in action bar
        setHasOptionsMenu(true)
    }

    override fun onResume() {
        super.onResume()

        observationRecordViewModel.observationRecord.observeUntil(
            viewLifecycleOwner,
            { it != null }) {
            if (it == null) return@observeUntil

            observationRecord = it
            refreshView()
        }
    }

    override fun onPause() {
        super.onPause()

        clearActiveSelection()
    }

    @Deprecated("Deprecated in Java")
    @SuppressLint("RestrictedApi")
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
                R.menu.map_settings,
                menu
            )
        }

        // workaround to show menu item icons
        if (menu is MenuBuilder) {
            menu.setOptionalIconsVisible(true)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)

        val context = context ?: return

        menu.findItem(R.id.menu_map_settings)
            .apply {
                isEnabled = arguments?.getParcelableCompat<MapSettings>(ARG_MAP_SETTINGS) != null
                // workaround to fix icon color states
                icon?.setTintList(
                    ColorStateList(
                        arrayOf(
                            intArrayOf(-android.R.attr.state_enabled),
                            IntArray(0)
                        ),
                        intArrayOf(
                            getColor(
                                context,
                                android.R.attr.textColorHint
                            ),
                            getColor(
                                context,
                                android.R.attr.textColorPrimary
                            )
                        )
                    )
                )
            }
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_map_settings -> {
                val context = context ?: return true
                val mapSettings =
                    arguments?.getParcelableCompat<MapSettings>(ARG_MAP_SETTINGS) ?: return true

                startActivity(
                    MapPreferencesActivity.newIntent(
                        context,
                        mapSettings
                    )
                )

                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun getResourceTitle(): Int {
        return R.string.pager_fragment_map_title
    }

    override fun getSubtitle(): CharSequence? {
        return null
    }

    override fun pagingEnabled(): Boolean {
        return false
    }

    override fun validate(): Boolean {
        return this.observationRecord?.geometry != null
    }

    override fun refreshView() {
        val geometry = observationRecord?.geometry ?: return

        if (geometry is Point) {
            setSelectedPOIs(listOf(fromPoint(geometry)))
        }
    }

    private fun clearInputSelection() {
        observationRecord = observationRecord?.copy(geometry = null)

        listener.validateCurrentPage()

        CoroutineScope(Main).launch {
            getOverlays { overlay -> overlay is FeatureCollectionOverlay }
                .asSequence()
                .map { it as FeatureCollectionOverlay }
                .forEach { it.setStyle(it.layerStyle) }
        }
    }

    private fun selectPOI(poi: GeoPoint) {
        CoroutineScope(Main).launch {
            val context = context ?: return@launch

            observationRecord = observationRecord?.copy(geometry = toPoint(poi))
                ?.also {
                    observationRecordViewModel.edit(it)
                }

            val accentColor = getAccentColor(context)

            // select matching Feature from Overlays
            observationRecord?.feature?.id =
                getOverlays { overlay -> overlay is FeatureCollectionOverlay }
                    .asSequence()
                    .map { it as FeatureCollectionOverlay }
                    .map { it.also { it.setStyle(it.layerStyle) } }
                    .map {
                        val filter = ContainsFeaturesFilter(
                            poi,
                            it.layerStyle,
                            LayerStyleSettings.Builder.newInstance()
                                .from(it.layerStyle)
                                .color(
                                    accentColor
                                )
                                .build()
                        )
                        it.apply(filter)
                        filter.getSelectedFeatures()
                    }
                    .flatMap { it.asSequence() }
                    // keep only valid Feature with ID as number
                    .map { it.id?.toLongOrNull() }
                    .firstOrNull()
                    ?.toString()

            listener.validateCurrentPage()
        }
    }

    companion object {

        /**
         * Use this factory method to create a new instance of [InputMapFragment].
         *
         * @return A new instance of [InputMapFragment]
         */
        @JvmStatic
        fun newInstance(mapSettings: MapSettings) = InputMapFragment().apply {
            arguments = Bundle().apply {
                putParcelable(
                    ARG_MAP_SETTINGS,
                    mapSettings
                )
            }
        }
    }
}
