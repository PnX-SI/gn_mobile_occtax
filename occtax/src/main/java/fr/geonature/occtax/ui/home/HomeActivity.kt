package fr.geonature.occtax.ui.home

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
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
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import androidx.work.WorkInfo
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import fr.geonature.commons.error.Failure
import fr.geonature.commons.lifecycle.observe
import fr.geonature.commons.lifecycle.observeOnce
import fr.geonature.commons.lifecycle.observeUntil
import fr.geonature.commons.lifecycle.onError
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
import fr.geonature.datasync.settings.AppSettingsFilename
import fr.geonature.datasync.settings.error.DataSyncSettingsJsonParseFailure
import fr.geonature.datasync.settings.error.DataSyncSettingsNotFoundFailure
import fr.geonature.datasync.sync.DataSyncViewModel
import fr.geonature.datasync.sync.ServerStatus
import fr.geonature.datasync.ui.login.LoginActivity
import fr.geonature.maps.ui.MapFragment
import fr.geonature.maps.util.CheckPermissionLifecycleObserver
import fr.geonature.maps.util.ManageExternalStoragePermissionLifecycleObserver
import fr.geonature.occtax.BuildConfig
import fr.geonature.occtax.MainApplication
import fr.geonature.occtax.R
import fr.geonature.occtax.features.record.domain.ObservationRecord
import fr.geonature.occtax.features.record.presentation.ObservationRecordViewModel
import fr.geonature.occtax.features.settings.domain.AppSettings
import fr.geonature.occtax.features.settings.error.AppSettingsException
import fr.geonature.occtax.features.settings.presentation.AppSettingsViewModel
import fr.geonature.occtax.ui.input.InputPagerFragmentActivity
import fr.geonature.occtax.ui.settings.PreferencesActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.tinylog.Logger
import java.io.File
import java.text.DateFormat
import java.time.Instant
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.time.Duration.Companion.milliseconds

/**
 * Home screen Activity.
 *
 * @author S. Grimault
 */
@AndroidEntryPoint
class HomeActivity : AppCompatActivity(),
    OnObservationRecordListener,
    MapFragment.OnMapFragmentPermissionsListener {

    private val authLoginViewModel: AuthLoginViewModel by viewModels()
    private val appSettingsViewModel: AppSettingsViewModel by viewModels()
    private val packageInfoViewModel: PackageInfoViewModel by viewModels()
    private val dataSyncViewModel: DataSyncViewModel by viewModels()
    private val configureServerSettingsViewModel: ConfigureServerSettingsViewModel by viewModels()
    private val updateSettingsViewModel: UpdateSettingsViewModel by viewModels()
    private val observationRecordViewModel: ObservationRecordViewModel by viewModels()

    @Inject
    lateinit var geoNatureAPIClient: IGeoNatureAPIClient

    @AppSettingsFilename
    @Inject
    lateinit var appSettingsFilename: String

    private var manageExternalStoragePermissionLifecycleObserver: ManageExternalStoragePermissionLifecycleObserver? =
        null
    private var readExternalStoragePermissionLifecycleObserver: CheckPermissionLifecycleObserver? =
        null
    private var locationPermissionLifecycleObserver: CheckPermissionLifecycleObserver? = null
    private var loginLastnameTextView: TextView? = null
    private var loginFirstnameTextView: TextView? = null
    private var loginButton: Button? = null
    private var navMenuDataSync: DrawerMenuEntryView? = null
    private var navMenuLogout: DrawerMenuEntryView? = null
    private var homeContent: CoordinatorLayout? = null
    private var emptyTextView: TextView? = null
    private var progressSnackbar: Pair<Snackbar, CircularProgressIndicator>? = null
    private var appSettings: AppSettings? = null
    private var isMapViewSelected: Boolean = false

    private lateinit var startSyncResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().also {
            var keepSplashScreenOn = true
            lifecycleScope.launch {
                delay(resources.getInteger(R.integer.splash_screen_duration).toLong().milliseconds)
                keepSplashScreenOn = false
            }
            it.setKeepOnScreenCondition { keepSplashScreenOn }
        }

        super.onCreate(savedInstanceState)

        Logger.info { "onCreate: ${intent.action}" }

        setContentView(R.layout.activity_home)

        // setting a custom ActionBar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.apply {
            // showing the burger button on the ActionBar
            setDisplayHomeAsUpEnabled(true)
            subtitle = getString(R.string.home_last_inputs)
        }

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
                        appSettings?.nomenclatureSettings?.withAdditionalFields ?: true,
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
                        .format(
                            Date(
                                File(
                                    packageManager.getApplicationInfo(
                                        packageName,
                                        0
                                    ).sourceDir
                                ).lastModified()
                            )
                        )
                )
            )
        }

        homeContent = findViewById(R.id.homeContent)
        emptyTextView = findViewById(android.R.id.empty)
        initializeHomeFragments(savedInstanceState)

        configureAuthLoginViewModel()
        configureDataSyncViewModel()
        configureConfigureServerSettingsViewModel()
        configureUpdateSettingsViewModel()
        configureAppSettingsViewModel()
        configureObservationRecordsViewModel()

        startSyncResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                when (result.resultCode) {
                    RESULT_OK -> {
                        val dataSyncSettings = appSettings?.dataSyncSettings

                        if (dataSyncSettings == null) {
                            configureServerSettingsViewModel.loadAppSettings(geoNatureAPIClient.getBaseUrls().geoNatureBaseUrl)
                        }
                        else if (geoNatureAPIClient.getBaseUrls().geoNatureBaseUrl != dataSyncSettings.geoNatureServerUrl) {
                            dataSyncViewModel.purgeLocalData().observeOnce(this) {
                                configureServerSettingsViewModel.loadAppSettings(geoNatureAPIClient.getBaseUrls().geoNatureBaseUrl)
                            }
                            Toast
                                .makeText(
                                    this@HomeActivity,
                                    R.string.toast_purge_local_data,
                                    Toast.LENGTH_LONG
                                )
                                .show()
                        } else {
                            dataSyncViewModel.startSync(
                                dataSyncSettings,
                                appSettings?.nomenclatureSettings?.withAdditionalFields ?: true,
                                HomeActivity::class.java,
                                MainApplication.CHANNEL_DATA_SYNCHRONIZATION
                            )
                        }
                    }
                }
            }

        updateSettingsViewModel.updateAppSettings()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        // update the current intent from this new one
        setIntent(intent)

        handleShortcuts()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(
            R.menu.list_map,
            menu
        )

        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem(android.R.id.toggle)
            ?.apply {
                setIcon(if (isMapViewSelected) R.drawable.ic_list else R.drawable.ic_map)
                setTitle(if (isMapViewSelected) R.string.action_as_list else R.string.action_as_map)
                isEnabled = dataSyncViewModel.getLastSynchronizedDate().second != null
            }

        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.toggle -> {
                toggleHomeFragment()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
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

    override suspend fun onStoragePermissionsGranted() =
        suspendCancellableCoroutine { continuation ->
            lifecycleScope.launch {
                continuation.resume(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        manageExternalStoragePermissionLifecycleObserver?.invoke()
                    } else {
                        readExternalStoragePermissionLifecycleObserver?.invoke(this@HomeActivity)
                    } ?: false
                )
            }
        }

    override suspend fun onLocationPermissionGranted() =
        suspendCancellableCoroutine { continuation ->
            lifecycleScope.launch {
                continuation.resume(
                    locationPermissionLifecycleObserver?.invoke(this@HomeActivity)
                        ?: false
                )
            }
        }

    private fun configureAuthLoginViewModel() {
        authLoginViewModel.also { vm ->
            vm
                .checkAuthLogin()
                .observeOnce(this@HomeActivity) {
                    if (checkGeoNatureSettings() && it == null) {
                        Logger.info { "not connected, redirect to ${LoginActivity::class.java.name}..." }
                        lifecycleScope.launch {
                            delay(100.milliseconds)
                            startSyncResultLauncher.launch(LoginActivity.newIntent(this@HomeActivity))
                        }
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
                emptyTextView?.isVisible = it

                if (it && dataSyncViewModel.getLastSynchronizedDate().second == null) {
                    emptyTextView?.text = getString(R.string.home_first_sync)
                }

                navMenuDataSync?.apply {
                    isClickable = !it
                    setText1(R.string.action_data_sync)
                }
            }
            vm.dataSyncStatus.observe(
                    this@HomeActivity
                ) {
                    if (it == null) {
                        navMenuDataSync?.apply {
                            icon.clearAnimation()
                        }
                        emptyTextView?.isVisible = false
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

                            WorkInfo.State.SUCCEEDED -> {
                                navMenuDataSync?.apply {
                                    icon.clearAnimation()
                                    setIcon(R.drawable.ic_sync)
                                    setText2(
                                        getString(
                                            R.string.sync_last_synchronization,
                                            vm.getLastSynchronizedDate().second?.let {
                                                android.text.format.DateFormat.format(
                                                    getString(R.string.sync_last_synchronization_date),
                                                    it
                                                )
                                            } ?: getString(R.string.sync_last_synchronization_never)
                                        ))
                                }
                                emptyTextView?.isVisible = false
                                invalidateOptionsMenu()
                            }

                            else -> {
                                navMenuDataSync?.apply {
                                    icon.clearAnimation()
                                    setIcon(R.drawable.ic_sync)
                                }
                                emptyTextView?.isVisible = false
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

    private fun configureAppSettingsViewModel() {
        with(appSettingsViewModel) {
            observe(appSettings) {
                this@HomeActivity.appSettings = it

                if (isMapViewSelected) {
                    showMapFragment(forceReplace = true)
                }

                it.dataSyncSettings.also { dataSyncSettings ->
                    dataSyncViewModel.configurePeriodicSync(
                        dataSyncSettings,
                        this@HomeActivity.appSettings?.nomenclatureSettings?.withAdditionalFields
                            ?: true,
                        HomeActivity::class.java,
                        MainApplication.CHANNEL_DATA_SYNCHRONIZATION
                    )

                    dataSyncViewModel.hasLocalData()
                        .observeOnce(this@HomeActivity) { hasLocalData ->
                            if (hasLocalData == true) {
                                handleShortcuts()

                                dataSyncViewModel.getLastSynchronizedDate().second?.also { lastDataSynchronization ->
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
                                            this@HomeActivity.appSettings?.nomenclatureSettings?.withAdditionalFields
                                                ?: true,
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
                                    this@HomeActivity.appSettings?.nomenclatureSettings?.withAdditionalFields
                                        ?: true,
                                    HomeActivity::class.java,
                                    MainApplication.CHANNEL_DATA_SYNCHRONIZATION
                                )
                            }
                        }
                }
            }
            onError(
                error,
                ::handleError
            )
        }
    }

    private fun configureConfigureServerSettingsViewModel() {
        with(configureServerSettingsViewModel) {
            observe(dataSyncSettingLoaded) {
                appSettingsViewModel.loadAppSettings()
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

    private fun configureObservationRecordsViewModel() {
        // check if we have at least one observation record, and if so, adds a shortcut to resume the last edited one
        observationRecordViewModel.observationRecords.observe(this@HomeActivity) {
            if (it.isEmpty()) {
                ShortcutManagerCompat.removeAllDynamicShortcuts(this)
            }

            if (it.isNotEmpty()) {
                val shortcut = ShortcutInfoCompat.Builder(
                    this,
                    "input_resume_last"
                )
                    .setShortLabel(getString(R.string.shortcut_input_resume_last_short))
                    .setLongLabel(getString(R.string.shortcut_input_resume_last_long))
                    .setIcon(
                        IconCompat.createWithResource(
                            this,
                            R.drawable.ic_input_edit
                        )
                    )
                    .setIntent(
                        Intent(
                            this,
                            HomeActivity::class.java
                        ).apply { action = getString(R.string.intent_action_input_resume_last) })
                    .build()

                ShortcutManagerCompat.removeAllDynamicShortcuts(this)
                ShortcutManagerCompat.pushDynamicShortcut(
                    this,
                    shortcut
                )
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

        appSettingsViewModel.loadAppSettings()
    }

    private fun handleShortcuts() {
        val appSettings = appSettings ?: return

        when (intent.action) {
            // start a new input
            getString(R.string.intent_action_input_new) -> {
                Logger.info { "shortcut intent action: ${intent.action}" }
                startActivity(
                    InputPagerFragmentActivity.newIntent(
                        this@HomeActivity,
                        appSettings
                    )
                )
            }
            // resume last edited input
            getString(R.string.intent_action_input_resume_last) -> {
                Logger.info { "shortcut intent action: ${intent.action}" }
                observationRecordViewModel.observationRecords.observeOnce(this@HomeActivity) { observationRecords ->
                    (observationRecords ?: emptyList())
                        .sortedByDescending { it.dates.lastModified }
                        .firstOrNull { it.status == ObservationRecord.Status.DRAFT }
                        ?.also { observationRecord ->
                            startActivity(
                                InputPagerFragmentActivity.newIntent(
                                    this@HomeActivity,
                                    appSettings,
                                    observationRecord
                                )
                            )
                        }
                }
            }
        }
    }

    private fun handleError(error: Throwable) {
        when (error) {
            is AppSettingsException.NotFoundException, is AppSettingsException.NoAppSettingsFoundLocallyException -> {
                emptyTextView?.setText(R.string.home_no_settings)
                makeSnackbar(
                    getString(
                        R.string.snackbar_settings_not_found,
                        appSettingsFilename
                    )
                )?.show()
            }

            is AppSettingsException.JsonParseException, is AppSettingsException.MissingAttributeException -> {
                makeSnackbar(
                    getString(
                        R.string.snackbar_settings_invalid
                    )
                )
                    ?.setBackgroundTint(getErrorColor(this))
                    ?.show()
            }
        }
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
            .observeUntil(
                this@HomeActivity,
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

    private fun initializeHomeFragments(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            showListFragment()
            return
        }

        isMapViewSelected = supportFragmentManager.findFragmentByTag(TAG_FRAGMENT_OBSERVATION_RECORDS_MAP)?.isVisible == true

        if (!isMapViewSelected && supportFragmentManager.findFragmentByTag(TAG_FRAGMENT_OBSERVATION_RECORDS_LIST) == null) {
            showListFragment()
            return
        }

        invalidateOptionsMenu()
    }

    private fun toggleHomeFragment() {
        if (isMapViewSelected) {
            showListFragment()
        } else {
            showMapFragment()
        }
    }

    private fun showListFragment() {
        val listFragment =
            supportFragmentManager.findFragmentByTag(TAG_FRAGMENT_OBSERVATION_RECORDS_LIST) ?: ObservationRecordsListFragment.newInstance()
        val mapFragment = supportFragmentManager.findFragmentByTag(TAG_FRAGMENT_OBSERVATION_RECORDS_MAP)

        supportFragmentManager.commit {
            setReorderingAllowed(true)
            mapFragment?.let { hide(it) }

            if (listFragment.isAdded) {
                show(listFragment)
            } else {
                add(R.id.home_fragment_container, listFragment, TAG_FRAGMENT_OBSERVATION_RECORDS_LIST)
            }
        }

        isMapViewSelected = false
        emptyTextView?.isGone = true
        invalidateOptionsMenu()
    }

    private fun showMapFragment(forceReplace: Boolean = false) {
        val appSettings = appSettings ?: return

        var mapFragment: Fragment? = supportFragmentManager.findFragmentByTag(TAG_FRAGMENT_OBSERVATION_RECORDS_MAP)
        val listFragment = supportFragmentManager.findFragmentByTag(TAG_FRAGMENT_OBSERVATION_RECORDS_LIST)

        supportFragmentManager.commit {
            setReorderingAllowed(true)
            listFragment?.let { hide(it) }

            if (forceReplace && mapFragment != null) {
                remove(mapFragment)
                mapFragment = null
            }

            val targetMapFragment = mapFragment
                ?: ObservationRecordsMapFragment.newInstance(appSettings.mapSettings)

            if (targetMapFragment.isAdded) {
                show(targetMapFragment)
            } else {
                add(R.id.home_fragment_container, targetMapFragment, TAG_FRAGMENT_OBSERVATION_RECORDS_MAP)
            }
        }

        isMapViewSelected = true
        emptyTextView?.isGone = true
        invalidateOptionsMenu()
    }

    companion object {
        private const val TAG_FRAGMENT_OBSERVATION_RECORDS_LIST = "observation_records_list"
        private const val TAG_FRAGMENT_OBSERVATION_RECORDS_MAP = "observation_records_map"
    }
}
