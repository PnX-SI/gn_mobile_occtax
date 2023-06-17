package fr.geonature.occtax.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.FileProvider
import androidx.drawerlayout.widget.DrawerLayout
import androidx.work.WorkInfo
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import fr.geonature.commons.error.Failure
import fr.geonature.commons.lifecycle.observe
import fr.geonature.commons.lifecycle.observeOnce
import fr.geonature.commons.lifecycle.observeUntil
import fr.geonature.commons.lifecycle.onFailure
import fr.geonature.commons.util.ThemeUtils.getErrorColor
import fr.geonature.commons.util.add
import fr.geonature.datasync.api.IGeoNatureAPIClient
import fr.geonature.datasync.auth.AuthLoginViewModel
import fr.geonature.datasync.features.settings.presentation.ConfigureServerSettingsActivity
import fr.geonature.datasync.features.settings.presentation.ConfigureServerSettingsViewModel
import fr.geonature.datasync.features.settings.presentation.UpdateSettingsViewModel
import fr.geonature.datasync.packageinfo.PackageInfo
import fr.geonature.datasync.packageinfo.PackageInfoViewModel
import fr.geonature.datasync.packageinfo.error.PackageInfoNotFoundFailure
import fr.geonature.datasync.packageinfo.error.PackageInfoNotFoundFromRemoteFailure
import fr.geonature.datasync.settings.error.DataSyncSettingsJsonParseFailure
import fr.geonature.datasync.settings.error.DataSyncSettingsNotFoundFailure
import fr.geonature.datasync.sync.DataSyncViewModel
import fr.geonature.datasync.sync.ServerStatus
import fr.geonature.datasync.ui.login.LoginActivity
import fr.geonature.occtax.BuildConfig
import fr.geonature.occtax.MainApplication
import fr.geonature.occtax.R
import fr.geonature.occtax.features.record.domain.ObservationRecord
import fr.geonature.occtax.settings.AppSettings
import fr.geonature.occtax.settings.AppSettingsViewModel
import fr.geonature.occtax.ui.input.InputPagerFragmentActivity
import fr.geonature.occtax.ui.settings.PreferencesActivity
import org.tinylog.Logger
import java.io.File
import java.text.DateFormat
import java.time.Instant
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

/**
 * Home screen Activity.
 *
 * @author S. Grimault
 */
@AndroidEntryPoint
class HomeActivity : AppCompatActivity(),
    LastObservationRecordsFragment.OnLastObservationRecordsFragmentListener {

    private val authLoginViewModel: AuthLoginViewModel by viewModels()
    private val appSettingsViewModel: AppSettingsViewModel by viewModels()
    private val packageInfoViewModel: PackageInfoViewModel by viewModels()
    private val dataSyncViewModel: DataSyncViewModel by viewModels()
    private val configureServerSettingsViewModel: ConfigureServerSettingsViewModel by viewModels()
    private val updateSettingsViewModel: UpdateSettingsViewModel by viewModels()

    @Inject
    lateinit var geoNatureAPIClient: IGeoNatureAPIClient

    private var loginLastnameTextView: TextView? = null
    private var loginFirstnameTextView: TextView? = null
    private var loginButton: Button? = null
    private var navMenuDataSync: DrawerMenuEntryView? = null
    private var navMenuLogout: DrawerMenuEntryView? = null
    private var homeContent: CoordinatorLayout? = null
    private var progressSnackbar: Pair<Snackbar, CircularProgressIndicator>? = null

    private var appSettings: AppSettings? = null

    private lateinit var startSyncResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_home)

        // setting a custom ActionBar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.apply {
            // showing the burger button on the ActionBar
            setDisplayHomeAsUpEnabled(true)
            subtitle = getString(R.string.home_last_inputs)
        }

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val toggle =
            ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.home_drawer_open,
                R.string.home_drawer_close
            )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        findViewById<ViewGroup>(R.id.nav_header)?.also {
            loginLastnameTextView = it.findViewById(android.R.id.text1)
            loginFirstnameTextView = it.findViewById(android.R.id.text2)
            loginButton = it.findViewById<Button?>(android.R.id.button1)
                ?.apply {
                    setOnClickListener {
                        startSyncResultLauncher.launch(LoginActivity.newIntent(this@HomeActivity))
                        drawerLayout.close()
                    }
                }
        }
        findViewById<DrawerMenuEntryView>(R.id.nav_menu_settings)?.also {
            it.setOnClickListener {
                startSyncResultLauncher.launch(
                    PreferencesActivity.newIntent(
                        this,
                        appSettings
                    )
                )
                drawerLayout.close()
            }
        }
        navMenuDataSync = findViewById<DrawerMenuEntryView>(R.id.nav_menu_sync)?.also {
            it.setOnClickListener {
                appSettings?.dataSyncSettings?.also { dataSyncSettings ->
                    dataSyncViewModel.startSync(
                        dataSyncSettings,
                        appSettings?.nomenclatureSettings?.withAdditionalFields ?: false,
                        HomeActivity::class.java,
                        MainApplication.CHANNEL_DATA_SYNCHRONIZATION
                    )
                }
            }
        }
        navMenuLogout = findViewById<DrawerMenuEntryView>(R.id.nav_menu_logout)?.also {
            it.setOnClickListener {
                authLoginViewModel
                    .logout()
                    .observe(
                        this
                    ) {
                        Toast
                            .makeText(
                                this,
                                R.string.toast_logout_success,
                                Toast.LENGTH_SHORT
                            )
                            .show()
                    }
                drawerLayout.close()
            }
        }
        findViewById<DrawerMenuEntryView>(R.id.nav_menu_about)?.also {
            it.setText2(
                getString(
                    R.string.app_version,
                    BuildConfig.VERSION_NAME,
                    BuildConfig.VERSION_CODE,
                    DateFormat.getDateTimeInstance()
                        .format(Date(BuildConfig.BUILD_DATE.toLong()))
                )
            )
        }

        homeContent = findViewById(R.id.homeContent)

        configureAuthLoginViewModel()
        configureDataSyncViewModel()
        configureConfigureServerSettingsViewModel()
        configureUpdateSettingsViewModel()

        startSyncResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                when (result.resultCode) {
                    RESULT_OK -> {
                        val dataSyncSettings = appSettings?.dataSyncSettings

                        if (dataSyncSettings == null || geoNatureAPIClient.getBaseUrls().geoNatureBaseUrl != dataSyncSettings.geoNatureServerUrl) {
                            configureServerSettingsViewModel.loadAppSettings(geoNatureAPIClient.getBaseUrls().geoNatureBaseUrl)
                        } else {
                            dataSyncViewModel.startSync(
                                dataSyncSettings,
                                appSettings?.nomenclatureSettings?.withAdditionalFields ?: false,
                                HomeActivity::class.java,
                                MainApplication.CHANNEL_DATA_SYNCHRONIZATION
                            )
                        }
                    }
                }
            }

        updateSettingsViewModel.updateAppSettings()
    }

    override fun onStartEditObservationRecord(selectedObservationRecord: ObservationRecord?) {
        val appSettings = appSettings ?: return

        startActivity(
            InputPagerFragmentActivity.newIntent(
                this,
                appSettings,
                selectedObservationRecord
            )
        )
    }

    private fun configureAuthLoginViewModel() {
        authLoginViewModel.also { vm ->
            vm
                .checkAuthLogin()
                .observeOnce(this@HomeActivity) {
                    if (checkGeoNatureSettings() && it == null) {
                        Logger.info { "not connected, redirect to ${LoginActivity::class.java.name}" }

                        startSyncResultLauncher.launch(LoginActivity.newIntent(this@HomeActivity))
                    }
                }
            vm.isLoggedIn.observe(
                this@HomeActivity
            ) {
                loginLastnameTextView?.apply {
                    text = it?.user?.lastname
                    visibility = if (it == null) View.GONE else View.VISIBLE
                }
                loginFirstnameTextView?.apply {
                    text = it?.user?.firstname
                    visibility = if (it == null) View.GONE else View.VISIBLE
                }
                loginButton?.visibility = if (it == null) View.VISIBLE else View.GONE
                navMenuLogout?.visibility = if (it == null) View.GONE else View.VISIBLE
            }
            vm.loginResult.observe(this@HomeActivity) { result ->
                result.success?.also {
                    loginLastnameTextView?.text = it.user.lastname
                    loginFirstnameTextView?.text = it.user.firstname
                }

                loginLastnameTextView?.visibility =
                    if (result.success == null) View.GONE else View.VISIBLE
                loginFirstnameTextView?.visibility =
                    if (result.success == null) View.GONE else View.VISIBLE
                loginButton?.visibility = if (result.success == null) View.VISIBLE else View.GONE
                navMenuLogout?.visibility = if (result.success == null) View.GONE else View.VISIBLE
            }
        }
    }

    private fun configureDataSyncViewModel() {
        dataSyncViewModel.also { vm ->
            vm.isSyncRunning.observe(
                this@HomeActivity
            ) {
                navMenuDataSync?.apply {
                    isClickable = !it
                    setText1(R.string.action_data_sync)
                }
            }
            vm.lastSynchronizedDate.observe(this@HomeActivity) { syncState ->
                navMenuDataSync?.setText2(getString(
                    R.string.sync_last_synchronization,
                    syncState?.second?.let {
                        android.text.format.DateFormat.format(
                            getString(R.string.sync_last_synchronization_date),
                            it
                        )
                    } ?: getString(R.string.sync_last_synchronization_never)
                ))
            }
            vm
                .observeDataSyncStatus()
                .observe(
                    this@HomeActivity
                ) {
                    if (it == null) {
                        navMenuDataSync?.apply {
                            icon.clearAnimation()
                        }
                    }

                    it?.run {
                        when (state) {
                            WorkInfo.State.RUNNING -> {
                                navMenuDataSync?.apply {
                                    setText1(R.string.action_data_sync_in_progress)
                                    setText2(syncMessage)

                                    if (icon.animation == null) {
                                        setIcon(R.drawable.ic_sync)
                                        icon.startAnimation(
                                            RotateAnimation(
                                                0F,
                                                -360F,
                                                Animation.RELATIVE_TO_SELF,
                                                0.5F,
                                                Animation.RELATIVE_TO_SELF,
                                                0.5F
                                            ).apply {
                                                interpolator = LinearInterpolator()
                                                duration = 900
                                                repeatCount = Animation.INFINITE
                                            }
                                        )
                                    }
                                }
                            }

                            WorkInfo.State.FAILED -> {
                                navMenuDataSync?.apply {
                                    icon.clearAnimation()
                                    setIcon(R.drawable.ic_sync_problem)
                                }
                            }

                            else -> {
                                navMenuDataSync?.apply {
                                    icon.clearAnimation()
                                    setIcon(R.drawable.ic_sync)
                                }
                            }
                        }

                        if (it.serverStatus == ServerStatus.UNAUTHORIZED) {
                            Logger.info { "not connected (HTTP error code: 401), redirect to ${LoginActivity::class.java.name}" }

                            Toast
                                .makeText(
                                    this@HomeActivity,
                                    R.string.toast_not_connected,
                                    Toast.LENGTH_SHORT
                                )
                                .show()

                            startSyncResultLauncher.launch(LoginActivity.newIntent(this@HomeActivity))
                        }
                    }
                }
        }
    }

    private fun configureConfigureServerSettingsViewModel() {
        with(configureServerSettingsViewModel) {
            observe(dataSyncSettingLoaded) {
                loadAppSettings()
            }
            onFailure(
                failure,
                ::handleFailure
            )
        }
    }

    private fun configureUpdateSettingsViewModel() {
        with(updateSettingsViewModel) {
            observe(
                packageInfoUpdated,
                ::packageInfoUpdated
            )
            onFailure(
                failure,
                ::handleFailure
            )
        }
    }

    private fun loadAppSettings() {
        appSettingsViewModel.loadAppSettings()
            .observeOnce(this) {
                if (it?.mapSettings == null) {
                    Logger.info { "failed to load settings" }

                    makeSnackbar(
                        getString(
                            if (it == null) R.string.snackbar_settings_not_found else R.string.snackbar_settings_map_invalid,
                            appSettingsViewModel.getAppSettingsFilename()
                        )
                    )?.show()
                } else {
                    Logger.info { "app settings successfully loaded" }

                    appSettings = it

                    it.dataSyncSettings?.also { dataSyncSettings ->
                        dataSyncViewModel.configurePeriodicSync(
                            dataSyncSettings,
                            appSettings?.nomenclatureSettings?.withAdditionalFields ?: false,
                            HomeActivity::class.java,
                            MainApplication.CHANNEL_DATA_SYNCHRONIZATION
                        )

                        if (dataSyncViewModel.lastSynchronizedDate.value?.second == null) {
                            dataSyncViewModel.startSync(
                                dataSyncSettings,
                                appSettings?.nomenclatureSettings?.withAdditionalFields ?: false,
                                HomeActivity::class.java,
                                MainApplication.CHANNEL_DATA_SYNCHRONIZATION
                            )

                            return@also
                        }

                        dataSyncViewModel.hasLocalData()
                            .observeOnce(this) { hasLocalData ->
                                if (hasLocalData == true) {
                                    dataSyncViewModel.lastSynchronizedDate.value?.second?.also { lastDataSynchronization ->
                                        if (
                                            lastDataSynchronization.add(
                                                Calendar.SECOND,
                                                dataSyncSettings.dataSyncPeriodicity?.inWholeSeconds?.toInt()
                                                    ?: 0
                                            )
                                                .before(Date.from(Instant.now()))
                                        ) {
                                            Logger.info {
                                                "the last data synchronization seems to be old (done on $lastDataSynchronization), restarting data synchronization..."
                                            }

                                            dataSyncViewModel.startSync(
                                                dataSyncSettings,
                                                appSettings?.nomenclatureSettings?.withAdditionalFields
                                                    ?: false,
                                                HomeActivity::class.java,
                                                MainApplication.CHANNEL_DATA_SYNCHRONIZATION
                                            )
                                        }
                                    }
                                } else {
                                    Logger.warn {
                                        "no local data found locally, starting a new data synchronization..."
                                    }

                                    dataSyncViewModel.startSync(
                                        dataSyncSettings,
                                        appSettings?.nomenclatureSettings?.withAdditionalFields
                                            ?: false,
                                        HomeActivity::class.java,
                                        MainApplication.CHANNEL_DATA_SYNCHRONIZATION
                                    )
                                }
                            }
                    }
                }
            }
    }

    private fun packageInfoUpdated(packageInfo: PackageInfo) {
        if (packageInfo.hasNewVersionAvailable()) {
            confirmBeforeUpgrade(this.packageName)
        }

        if (packageInfo.apkUrl != null && packageInfo.apkUrl?.isNotEmpty() == true && packageInfo.settings != null) {
            Logger.info { "reloading settings after update..." }
        }

        loadAppSettings()
    }

    private fun handleFailure(failure: Failure) {
        when (failure) {
            is Failure.NetworkFailure -> {
                makeSnackbar(failure.reason)
                    ?.setBackgroundTint(getErrorColor(this))
                    ?.show()
            }

            is Failure.ServerFailure -> {
                makeSnackbar(getString(fr.geonature.datasync.R.string.settings_server_error))
                    ?.setBackgroundTint(getErrorColor(this))
                    ?.show()
            }

            is PackageInfoNotFoundFromRemoteFailure -> {
                makeSnackbar(getString(fr.geonature.datasync.R.string.settings_server_settings_not_found))
                    ?.setBackgroundTint(getErrorColor(this))
                    ?.show()
            }

            is DataSyncSettingsNotFoundFailure -> {
                Logger.warn { "failed to load settings${if (failure.source.isNullOrBlank()) "" else " from source '${failure.source}'"}" }

                val geoNatureBaseUrl = failure.geoNatureBaseUrl

                if (geoNatureBaseUrl.isNullOrBlank()) {
                    // no configuration found: redirect user to server settings configuration activity
                    startSyncResultLauncher.launch(ConfigureServerSettingsActivity.newIntent(this))
                } else {
                    Logger.info { "try to reload settings from '${geoNatureBaseUrl}'..." }

                    configureServerSettingsViewModel.loadAppSettings(geoNatureBaseUrl)
                }
            }

            is DataSyncSettingsJsonParseFailure -> {
                makeSnackbar(
                    getString(
                        R.string.snackbar_settings_invalid
                    )
                )
                    ?.setBackgroundTint(getErrorColor(this))
                    ?.show()
            }

            is PackageInfoNotFoundFailure -> {
                // should never occur...
                Logger.error { "this app '${packageName}' seems to be incompatible..." }
            }

            else -> {
                makeSnackbar(getString(fr.geonature.datasync.R.string.error_settings_undefined))
                    ?.setBackgroundTint(getErrorColor(this))
                    ?.show()
            }
        }
    }

    private fun makeSnackbar(
        text: CharSequence,
        @BaseTransientBottomBar.Duration duration: Int = Snackbar.LENGTH_LONG
    ): Snackbar? {
        val view = homeContent ?: return null

        return Snackbar.make(
            view,
            text,
            duration
        )
    }

    private fun makeProgressSnackbar(text: CharSequence): Pair<Snackbar, CircularProgressIndicator>? {
        val view = homeContent
            ?: return null

        return Snackbar
            .make(
                view,
                text,
                Snackbar.LENGTH_INDEFINITE
            )
            .let { snackbar ->
                val circularProgressIndicator = CircularProgressIndicator(this).also {
                    it.isIndeterminate = true
                    it.layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                }

                (snackbar.view.findViewById<View>(com.google.android.material.R.id.snackbar_text).parent as ViewGroup).addView(
                    circularProgressIndicator,
                    0
                )

                Pair(
                    snackbar,
                    circularProgressIndicator
                )
            }
    }

    private fun checkGeoNatureSettings(): Boolean {
        return geoNatureAPIClient.checkSettings()
    }

    private fun confirmBeforeUpgrade(packageName: String) {
        AlertDialog
            .Builder(this)
            .setIcon(R.drawable.ic_upgrade)
            .setTitle(R.string.alert_new_app_version_available_title)
            .setMessage(R.string.alert_new_app_version_available_description)
            .setPositiveButton(
                R.string.alert_new_app_version_action_ok
            ) { dialog, _ ->
                dataSyncViewModel.cancelTasks()
                packageInfoViewModel.cancelTasks()
                downloadApk(packageName)
                dialog.dismiss()
            }
            .setNegativeButton(
                R.string.alert_new_app_version_action_later
            ) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun downloadApk(packageName: String) {
        if (packageName == BuildConfig.APPLICATION_ID) {
            progressSnackbar =
                makeProgressSnackbar(getString(R.string.snackbar_upgrading_app))?.also { it.first.show() }
        }

        packageInfoViewModel
            .downloadAppPackage(packageName)
            .observeUntil(this@HomeActivity,
                { appPackageDownloadStatus ->
                    appPackageDownloadStatus?.state in arrayListOf(
                        WorkInfo.State.SUCCEEDED,
                        WorkInfo.State.FAILED,
                        WorkInfo.State.CANCELLED
                    )
                }) {
                it?.run {
                    when (state) {
                        WorkInfo.State.FAILED -> {
                            if (packageName == BuildConfig.APPLICATION_ID) progressSnackbar?.first?.dismiss()
                        }

                        WorkInfo.State.SUCCEEDED -> {
                            if (packageName == BuildConfig.APPLICATION_ID) progressSnackbar?.first?.dismiss()
                            apkFilePath?.run {
                                installApk(this)
                            }
                        }

                        else -> {
                            if (packageName == BuildConfig.APPLICATION_ID) {
                                progressSnackbar?.second?.also { circularProgressIndicator ->
                                    circularProgressIndicator.isIndeterminate = false
                                    circularProgressIndicator.progress = progress
                                }
                            }
                        }
                    }
                }
            }
    }

    private fun installApk(apkFilePath: String) {
        val contentUri = FileProvider.getUriForFile(
            this,
            "${BuildConfig.APPLICATION_ID}.file.provider",
            File(apkFilePath)
        )
        val install = Intent(Intent.ACTION_VIEW).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(
                Intent.EXTRA_NOT_UNKNOWN_SOURCE,
                true
            )
            data = contentUri
        }

        startActivity(install)
    }
}
