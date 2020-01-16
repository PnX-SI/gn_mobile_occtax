package fr.geonature.occtax.ui.input.map

import android.os.Bundle
import androidx.fragment.app.Fragment
import fr.geonature.commons.input.AbstractInput
import fr.geonature.commons.util.ThemeUtils
import fr.geonature.maps.jts.geojson.GeometryUtils.fromPoint
import fr.geonature.maps.jts.geojson.GeometryUtils.toPoint
import fr.geonature.maps.settings.LayerStyleSettings
import fr.geonature.maps.settings.MapSettings
import fr.geonature.maps.ui.MapFragment
import fr.geonature.maps.ui.overlay.feature.FeatureCollectionOverlay
import fr.geonature.maps.ui.overlay.feature.filter.ContainsFeaturesFilter
import fr.geonature.maps.ui.widget.EditFeatureButton
import fr.geonature.occtax.R
import fr.geonature.occtax.input.Input
import fr.geonature.occtax.ui.input.IInputFragment
import fr.geonature.viewpager.ui.AbstractPagerFragmentActivity
import fr.geonature.viewpager.ui.IValidateFragment
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.locationtech.jts.geom.Point
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView

/**
 * Simple [Fragment] embedding a [MapView] instance to edit a single POI on the map.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class InputMapFragment : MapFragment(),
    IValidateFragment,
    IInputFragment {

    private var input: Input? = null

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
                val geometry = input?.geometry

                if (geometry != null && geometry is Point) {
                    selectPOI(fromPoint(geometry))
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()

        clearActiveSelection()
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
        return this.input?.geometry != null
    }

    override fun refreshView() {
        val geometry = input?.geometry ?: return

        if (geometry is Point) {
            setSelectedPOIs(listOf(fromPoint(geometry)))
        }
    }

    override fun setInput(input: AbstractInput) {
        this.input = input as Input
    }

    private fun clearInputSelection() {
        input?.geometry = null

        (activity as AbstractPagerFragmentActivity?)?.validateCurrentPage()

        GlobalScope.launch(Main) {
            getOverlays { overlay -> overlay is FeatureCollectionOverlay }
                .asSequence()
                .map { it as FeatureCollectionOverlay }
                .forEach { it.setStyle(it.layerStyle) }
        }
    }

    private fun selectPOI(poi: GeoPoint) {
        GlobalScope.launch(Main) {
            val context = context ?: return@launch

            input?.geometry = toPoint(poi)
            val accentColor = ThemeUtils.getAccentColor(context)

            // select matching Feature from Overlays
            input?.selectedFeatureId =
                getOverlays { overlay -> overlay is FeatureCollectionOverlay }
                    .asSequence()
                    .map { it as FeatureCollectionOverlay }
                    .map { it.also { it.setStyle(it.layerStyle) } }
                    .map {
                        val filter = ContainsFeaturesFilter(
                            poi,
                            it.layerStyle,
                            LayerStyleSettings.Builder.newInstance().from(it.layerStyle).color(
                                accentColor
                            ).build()
                        )
                        it.apply(filter)
                        filter.getSelectedFeatures()
                    }
                    .flatMap { it.asSequence() }
                    .map { it.id }
                    .firstOrNull()

            (activity as AbstractPagerFragmentActivity?)?.validateCurrentPage()
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
                putSerializable(
                    ARG_EDIT_MODE,
                    EditFeatureButton.EditMode.SINGLE
                )
            }
        }
    }
}
