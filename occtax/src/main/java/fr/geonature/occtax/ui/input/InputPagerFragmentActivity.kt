package fr.geonature.occtax.ui.input

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import fr.geonature.commons.input.AbstractInput
import fr.geonature.maps.settings.MapSettings
import fr.geonature.maps.ui.MapFragment
import fr.geonature.maps.util.CheckPermissionLifecycleObserver
import fr.geonature.maps.util.ManageExternalStoragePermissionLifecycleObserver
import fr.geonature.maps.util.MapSettingsPreferencesUtils.showCompass
import fr.geonature.maps.util.MapSettingsPreferencesUtils.showScale
import fr.geonature.maps.util.MapSettingsPreferencesUtils.showZoom
import fr.geonature.occtax.MainApplication
import fr.geonature.occtax.R
import fr.geonature.occtax.input.Input
import fr.geonature.occtax.input.InputViewModel
import fr.geonature.occtax.settings.AppSettings
import fr.geonature.occtax.ui.input.counting.CountingFragment
import fr.geonature.occtax.ui.input.informations.InformationFragment
import fr.geonature.occtax.ui.input.map.InputMapFragment
import fr.geonature.occtax.ui.input.observers.ObserversAndDateInputFragment
import fr.geonature.occtax.ui.input.summary.InputTaxaSummaryFragment
import fr.geonature.occtax.ui.input.taxa.TaxaFragment
import fr.geonature.viewpager.ui.AbstractNavigationHistoryPagerFragmentActivity
import fr.geonature.viewpager.ui.AbstractPagerFragmentActivity
import fr.geonature.viewpager.ui.IValidateFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * [ViewPager] implementation as [AbstractPagerFragmentActivity] with navigation history support.
 *
 * @author S. Grimault
 */
class InputPagerFragmentActivity : AbstractNavigationHistoryPagerFragmentActivity(),
    MapFragment.OnMapFragmentPermissionsListener {

    private lateinit var inputViewModel: InputViewModel
    private lateinit var appSettings: AppSettings
    private lateinit var input: Input

    private var manageExternalStoragePermissionLifecycleObserver: ManageExternalStoragePermissionLifecycleObserver? =
        null
    private var readExternalStoragePermissionLifecycleObserver: CheckPermissionLifecycleObserver? =
        null
    private var locationPermissionLifecycleObserver: CheckPermissionLifecycleObserver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            manageExternalStoragePermissionLifecycleObserver =
                ManageExternalStoragePermissionLifecycleObserver(this)
        } else {
            readExternalStoragePermissionLifecycleObserver = CheckPermissionLifecycleObserver(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }

        locationPermissionLifecycleObserver = CheckPermissionLifecycleObserver(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        inputViewModel = configureInputViewModel()

        appSettings = intent.getParcelableExtra(EXTRA_APP_SETTINGS)!!
        input = intent.getParcelableExtra(EXTRA_INPUT) ?: Input()
        val lastAddedInputTaxon = input.getLastAddedInputTaxon()

        if (lastAddedInputTaxon != null) {
            input.setCurrentSelectedInputTaxonId(lastAddedInputTaxon.taxon.id)
        }

        Log.i(
            TAG,
            "loading input: ${input.id}"
        )

        CoroutineScope(Dispatchers.Main).launch {
            pagerManager.load(input.id)
        }
    }

    override fun onPause() {
        super.onPause()

        if (input.status == AbstractInput.Status.DRAFT) {
            inputViewModel.saveInput(input)
        }
    }

    override val pagerFragments: Map<Int, IValidateFragment>
        get() = LinkedHashMap<Int, IValidateFragment>().apply {
            put(
                R.string.pager_fragment_observers_and_date_input_title,
                ObserversAndDateInputFragment.newInstance()
            )
            put(
                R.string.pager_fragment_map_title,
                InputMapFragment.newInstance(getMapSettings())
            )
            put(
                R.string.pager_fragment_taxa_title,
                TaxaFragment.newInstance(appSettings.areaObservationDuration)
            )
            put(
                R.string.pager_fragment_information_title,
                InformationFragment.newInstance(
                    *appSettings.nomenclatureSettings?.information?.toTypedArray() ?: emptyArray()
                )
            )
            put(
                R.string.pager_fragment_counting_title,
                CountingFragment.newInstance(
                    *appSettings.nomenclatureSettings?.counting?.toTypedArray() ?: emptyArray()
                )
            )
            put(
                R.string.pager_fragment_summary_title,
                InputTaxaSummaryFragment.newInstance()
            )
        }

    override fun performFinishAction() {
        inputViewModel.exportInput(input) {
            finish()
        }
    }

    override fun onPageSelected(position: Int) {
        super.onPageSelected(position)

        val pageFragment = getCurrentPageFragment()

        if (pageFragment is IInputFragment && ::input.isInitialized) {
            pageFragment.setInput(input)
            pageFragment.refreshView()
            validateCurrentPage()
            inputViewModel.saveInput(input)
        }
    }

    override suspend fun onStoragePermissionsGranted() =
        suspendCancellableCoroutine<Boolean> { continuation ->
            lifecycleScope.launch {
                continuation.resume(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        manageExternalStoragePermissionLifecycleObserver?.invoke()
                    } else {
                        readExternalStoragePermissionLifecycleObserver?.invoke(this@InputPagerFragmentActivity)
                    } ?: false
                )
            }
        }

    override suspend fun onLocationPermissionGranted() =
        suspendCancellableCoroutine<Boolean> { continuation ->
            lifecycleScope.launch {
                continuation.resume(
                    locationPermissionLifecycleObserver?.invoke(this@InputPagerFragmentActivity)
                        ?: false
                )
            }
        }

    private fun configureInputViewModel(): InputViewModel {
        return ViewModelProvider(
            this,
            fr.geonature.commons.input.InputViewModel.Factory { InputViewModel((application as MainApplication).sl.inputManager) }).get(
            InputViewModel::class.java
        )
    }

    private fun getMapSettings(): MapSettings {
        return MapSettings.Builder.newInstance()
            .from(appSettings.mapSettings!!)
            .showCompass(showCompass(this))
            .showScale(showScale(this))
            .showZoom(showZoom(this))
            .build()
    }

    companion object {

        private val TAG = InputPagerFragmentActivity::class.java.name

        private const val EXTRA_APP_SETTINGS = "extra_app_settings"
        private const val EXTRA_INPUT = "extra_input"

        fun newIntent(
            context: Context,
            appSettings: AppSettings,
            input: Input? = null
        ): Intent {
            return Intent(
                context,
                InputPagerFragmentActivity::class.java
            ).apply {
                putExtra(
                    EXTRA_APP_SETTINGS,
                    appSettings
                )
                putExtra(
                    EXTRA_INPUT,
                    input
                )
            }
        }
    }
}
