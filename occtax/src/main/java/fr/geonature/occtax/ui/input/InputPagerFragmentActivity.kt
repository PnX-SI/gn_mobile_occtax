package fr.geonature.occtax.ui.input

import android.content.Context
import android.content.Intent
import androidx.viewpager.widget.ViewPager
import fr.geonature.occtax.ui.input.observers.ObserversAndDateInputFragment
import fr.geonature.viewpager.ui.AbstractNavigationHistoryPagerFragmentActivity
import fr.geonature.viewpager.ui.AbstractPagerFragmentActivity
import fr.geonature.viewpager.ui.IValidateFragment

/**
 * [ViewPager] implementation as [AbstractPagerFragmentActivity] with navigation history support.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class InputPagerFragmentActivity : AbstractNavigationHistoryPagerFragmentActivity() {

    override val pagerFragments: Map<Int, IValidateFragment>
        get() = LinkedHashMap<Int, IValidateFragment>().apply {
            put(fr.geonature.occtax.R.string.fragment_observers_and_date_input_title,
                ObserversAndDateInputFragment.newInstance())
        }

    override fun performFinishAction() {
        // TODO: save current input
        finish()
    }

    companion object {

        fun newIntent(context: Context): Intent {
            return Intent(context,
                          InputPagerFragmentActivity::class.java)
        }
    }
}
