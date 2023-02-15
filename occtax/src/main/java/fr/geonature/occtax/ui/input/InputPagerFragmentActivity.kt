package fr.geonature.occtax.ui.input

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import fr.geonature.commons.lifecycle.onError
import fr.geonature.commons.util.KeyboardUtils.hideKeyboard
import fr.geonature.commons.util.ThemeUtils
import fr.geonature.maps.settings.MapSettings
import fr.geonature.maps.ui.MapFragment
import fr.geonature.maps.util.CheckPermissionLifecycleObserver
import fr.geonature.maps.util.ManageExternalStoragePermissionLifecycleObserver
import fr.geonature.maps.util.MapSettingsPreferencesUtils.showCompass
import fr.geonature.maps.util.MapSettingsPreferencesUtils.showScale
import fr.geonature.maps.util.MapSettingsPreferencesUtils.showZoom
import fr.geonature.occtax.R
import fr.geonature.occtax.features.record.domain.ObservationRecord
import fr.geonature.occtax.features.record.error.ObservationRecordException
import fr.geonature.occtax.features.record.presentation.ObservationRecordViewModel
import fr.geonature.occtax.settings.AppSettings
import fr.geonature.occtax.ui.input.counting.CountingFragment
import fr.geonature.occtax.ui.input.informations.InformationFragment
import fr.geonature.occtax.ui.input.map.InputMapFragment
import fr.geonature.occtax.ui.input.observers.ObserversAndDateInputFragment
import fr.geonature.occtax.ui.input.summary.InputTaxaSummaryFragment
import fr.geonature.occtax.ui.input.taxa.TaxaFragment
import fr.geonature.viewpager.model.IPageWithValidationFragment
import fr.geonature.viewpager.ui.AbstractPagerFragmentActivity
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.tinylog.kotlin.Logger
import kotlin.coroutines.resume

/**
 * `ViewPager2` implementation through [AbstractPagerFragmentActivity].
 *
 * @author S. Grimault
 */
@AndroidEntryPoint
class InputPagerFragmentActivity : AbstractPagerFragmentActivity(),
    OnInputPageFragmentListener,
    MapFragment.OnMapFragmentPermissionsListener {

    private val observationRecordViewModel: ObservationRecordViewModel by viewModels()

    private lateinit var appSettings: AppSettings
    private lateinit var observationRecord: ObservationRecord

    private var observationRecordExported = false

    private var manageExternalStoragePermissionLifecycleObserver: ManageExternalStoragePermissionLifecycleObserver? =
        null
    private var readExternalStoragePermissionLifecycleObserver: CheckPermissionLifecycleObserver? =
        null
    private var locationPermissionLifecycleObserver: CheckPermissionLifecycleObserver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        with(observationRecordViewModel) {
            onError(
                error,
                ::handleError
            )
        }

        // FIXME: this is a workaround to keep MapView alive from InputMapFragment…
        // see: https://github.com/osmdroid/osmdroid/issues/1581
        viewPager.offscreenPageLimit = 6

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

        appSettings = intent.getParcelableExtra(EXTRA_APP_SETTINGS)!!
        observationRecord =
            intent.getParcelableExtra(EXTRA_OBSERVATION_RECORD) ?: ObservationRecord()

        Logger.info { "loading observation record: ${observationRecord.id}" }

        observationRecordViewModel.loadDefaultNomenclatureValues(observationRecord)
        observationRecordViewModel.loadAllMedias(observationRecord)

        pageFragmentViewModel.set(
            R.string.pager_fragment_observers_and_date_input_title to ObserversAndDateInputFragment.newInstance(
                dateSettings = appSettings.inputSettings.dateSettings,
                saveDefaultValues = appSettings.nomenclatureSettings?.saveDefaultValues ?: false
            ),
            R.string.pager_fragment_map_title to InputMapFragment.newInstance(
                MapSettings.Builder.newInstance()
                    .from(appSettings.mapSettings!!)
                    .showCompass(showCompass(this))
                    .showScale(showScale(this))
                    .showZoom(showZoom(this))
                    .build()
            ),
            R.string.pager_fragment_summary_title to InputTaxaSummaryFragment.newInstance(appSettings.inputSettings.dateSettings)
        )
    }

    override fun onPause() {
        super.onPause()

        if (!observationRecordExported) {
            observationRecordViewModel.observationRecord.value?.also {
                observationRecordViewModel.save(it)
            }
        }
    }

    override fun getDefaultTitle(): CharSequence {
        return getString(R.string.activity_input_title)
    }

    override fun onNextAction(): Boolean {
        return false
    }

    override fun performFinishAction() {
        observationRecordViewModel.observationRecord.value?.also {
            observationRecordViewModel.export(
                it,
                appSettings
            ) {
                observationRecordExported = true
                finish()
            }
        }
    }

    override fun onPageSelected(position: Int) {
        super.onPageSelected(position)

        getCurrentPageFragment()?.also { page ->
            if (page is IPageWithValidationFragment) {
                // override the default next button color for the last page
                nextButton.backgroundTintList = ColorStateList(
                    arrayOf(
                        intArrayOf(-android.R.attr.state_enabled),
                        IntArray(0)
                    ),
                    intArrayOf(
                        ColorUtils.setAlphaComponent(
                            ThemeUtils.getColor(
                                this,
                                R.attr.colorOnSurface
                            ),
                            32
                        ),
                        if (position < ((viewPager.adapter?.itemCount
                                ?: 0) - 1)
                        ) ThemeUtils.getAccentColor(this)
                        else ThemeUtils.getPrimaryColor(this)
                    )
                )

                hideKeyboard(page as Fragment)
            }
        }
    }

    override fun startEditTaxon() {
        pageFragmentViewModel.add(
            R.string.pager_fragment_taxa_title to TaxaFragment.newInstance(appSettings.areaObservationDuration),
            R.string.pager_fragment_information_title to InformationFragment.newInstance(
                saveDefaultValues = appSettings.nomenclatureSettings?.saveDefaultValues ?: false,
                *appSettings.nomenclatureSettings?.information?.toTypedArray()
                    ?: emptyArray()
            ),
            R.string.pager_fragment_counting_title to CountingFragment.newInstance(
                *appSettings.nomenclatureSettings?.counting?.toTypedArray()
                    ?: emptyArray()
            ),
            R.string.pager_fragment_taxa_added_title to InputTaxaSummaryFragment.newInstance(appSettings.inputSettings.dateSettings)
        )
        goToNextPage()
    }

    override fun finishEditTaxon() {
        removePage(
            R.string.pager_fragment_taxa_title,
            R.string.pager_fragment_information_title,
            R.string.pager_fragment_counting_title,
            R.string.pager_fragment_taxa_added_title
        )
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

    private fun handleError(throwable: Throwable) {
        throwable.message?.also {
            Logger.error(throwable) { it }
        }

        when (throwable) {
            is ObservationRecordException.NoDefaultNomenclatureValuesFoundException -> {
                Toast.makeText(
                    this,
                    R.string.toast_input_default_properties_loading_failed,
                    Toast.LENGTH_LONG
                )
                    .show()
            }
        }
    }

    companion object {

        private const val EXTRA_APP_SETTINGS = "extra_app_settings"
        private const val EXTRA_OBSERVATION_RECORD = "extra_observation_record"

        fun newIntent(
            context: Context,
            appSettings: AppSettings,
            input: ObservationRecord? = null
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
                    EXTRA_OBSERVATION_RECORD,
                    input
                )
            }
        }
    }
}
