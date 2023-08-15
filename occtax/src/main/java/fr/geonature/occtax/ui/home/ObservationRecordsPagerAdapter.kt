package fr.geonature.occtax.ui.home

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import fr.geonature.occtax.features.settings.domain.AppSettings

/**
 * Default pager adapter to show current observation records as list or on the map.
 *
 * @author S. Grimault
 */
class ObservationRecordsPagerAdapter(
    fragmentActivity: FragmentActivity,
    private val appSettings: AppSettings
) :
    FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            1 -> ObservationRecordsMapFragment.newInstance(appSettings.mapSettings)
            else -> ObservationRecordsListFragment.newInstance()
        }
    }
}