package fr.geonature.occtax.ui.input.map

import android.os.Bundle
import androidx.fragment.app.Fragment
import fr.geonature.commons.input.AbstractInput
import fr.geonature.maps.jts.geojson.GeometryUtils.fromPoint
import fr.geonature.maps.jts.geojson.GeometryUtils.toPoint
import fr.geonature.maps.settings.MapSettings
import fr.geonature.maps.ui.MapFragment
import fr.geonature.maps.ui.widget.EditFeatureButton
import fr.geonature.occtax.R
import fr.geonature.occtax.input.Input
import fr.geonature.occtax.ui.input.IInputFragment
import fr.geonature.viewpager.ui.AbstractPagerFragmentActivity
import fr.geonature.viewpager.ui.IValidateFragment
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

        onSelectedPOIsListener = object : OnSelectedPOIsListener {
            override fun onSelectedPOIs(pois: List<GeoPoint>) {
                if (pois.isNotEmpty()) {
                    input?.geometry = toPoint(pois[0])
                }

                (activity as AbstractPagerFragmentActivity?)?.validateCurrentPage()
            }
        }
    }

    override fun getResourceTitle(): Int {
        return R.string.pager_fragment_map_title
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
