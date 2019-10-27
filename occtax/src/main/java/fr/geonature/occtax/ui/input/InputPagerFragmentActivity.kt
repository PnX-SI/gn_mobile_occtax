package fr.geonature.occtax.ui.input

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import fr.geonature.maps.settings.MapSettings
import fr.geonature.occtax.R
import fr.geonature.occtax.input.Input
import fr.geonature.occtax.input.InputViewModel
import fr.geonature.occtax.settings.AppSettings
import fr.geonature.occtax.ui.input.counting.CountingFragment
import fr.geonature.occtax.ui.input.informations.InformationFragment
import fr.geonature.occtax.ui.input.map.InputMapFragment
import fr.geonature.occtax.ui.input.observers.ObserversAndDateInputFragment
import fr.geonature.occtax.ui.input.taxa.TaxaFragment
import fr.geonature.occtax.util.SettingsUtils.getMapShowCompass
import fr.geonature.occtax.util.SettingsUtils.getMapShowScale
import fr.geonature.viewpager.ui.AbstractNavigationHistoryPagerFragmentActivity
import fr.geonature.viewpager.ui.AbstractPagerFragmentActivity
import fr.geonature.viewpager.ui.IValidateFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * [ViewPager] implementation as [AbstractPagerFragmentActivity] with navigation history support.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class InputPagerFragmentActivity : AbstractNavigationHistoryPagerFragmentActivity() {

    private lateinit var inputViewModel: InputViewModel
    private lateinit var appSettings: AppSettings
    private lateinit var input: Input

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        inputViewModel = ViewModelProvider(this,
                                           fr.geonature.commons.input.InputViewModel.Factory { InputViewModel(this.application) }).get(InputViewModel::class.java)

        appSettings = intent.getParcelableExtra(EXTRA_APP_SETTINGS)
        input = intent.getParcelableExtra(EXTRA_INPUT) ?: Input()
        val lastAddedInputTaxon = input.getLastAddedInputTaxon()

        if (lastAddedInputTaxon != null) {
            input.setCurrentSelectedInputTaxonId(lastAddedInputTaxon.taxon.id)
        }

        Log.i(TAG,
              "loading input: ${input.id}")

        GlobalScope.launch(Dispatchers.Main) {
            pagerManager.load(input.id)
        }
    }

    override fun onPause() {
        super.onPause()

        inputViewModel.saveInput(input)
    }

    override val pagerFragments: Map<Int, IValidateFragment>
        get() = LinkedHashMap<Int, IValidateFragment>().apply {
            put(R.string.pager_fragment_observers_and_date_input_title,
                ObserversAndDateInputFragment.newInstance())
            put(R.string.pager_fragment_map_title,
                InputMapFragment.newInstance(getMapSettings()))
            put(R.string.pager_fragment_taxa_title,
                TaxaFragment.newInstance())
            put(R.string.pager_fragment_information_title,
                InformationFragment.newInstance())
            put(R.string.pager_fragment_counting_title,
                CountingFragment.newInstance())
        }

    override fun performFinishAction() {
        inputViewModel.exportInput(input.id)

        finish()
    }

    override fun onPageSelected(position: Int) {
        super.onPageSelected(position)

        setInputToCurrentPage()
    }

    private fun setInputToCurrentPage() {
        val pageFragment = getCurrentPageFragment()

        if (pageFragment is IInputFragment && ::input.isInitialized) {
            pageFragment.setInput(input)
            pageFragment.refreshView()
            validateCurrentPage()
        }
    }

    private fun getMapSettings(): MapSettings {
        return MapSettings.Builder.newInstance()
            .from(appSettings.mapSettings!!)
            .showScale(getMapShowScale(this))
            .showCompass(getMapShowCompass(this))
            .build()
    }

    companion object {

        private val TAG = InputPagerFragmentActivity::class.java.name

        private const val EXTRA_APP_SETTINGS = "extra_app_settings"
        private const val EXTRA_INPUT = "extra_input"

        fun newIntent(context: Context,
                      appSettings: AppSettings,
                      input: Input? = null): Intent {
            return Intent(context,
                          InputPagerFragmentActivity::class.java).apply {
                putExtra(EXTRA_APP_SETTINGS,
                         appSettings)
                putExtra(EXTRA_INPUT,
                         input)
            }
        }
    }
}
