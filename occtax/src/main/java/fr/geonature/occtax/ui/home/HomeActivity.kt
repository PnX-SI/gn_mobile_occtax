package fr.geonature.occtax.ui.home

import android.annotation.SuppressLint
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.os.Vibrator
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.WorkInfo
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import fr.geonature.commons.data.ContentProviderAuthority
import fr.geonature.commons.data.entity.AppSync
import fr.geonature.commons.data.helper.ProviderHelper.buildUri
import fr.geonature.commons.error.Failure
import fr.geonature.commons.lifecycle.observe
import fr.geonature.commons.lifecycle.observeOnce
import fr.geonature.commons.lifecycle.observeUntil
import fr.geonature.commons.lifecycle.onFailure
import fr.geonature.commons.ui.adapter.AbstractListItemRecyclerViewAdapter
import fr.geonature.commons.util.ThemeUtils.getErrorColor
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
import fr.geonature.occtax.input.Input
import fr.geonature.occtax.input.InputViewModel
import fr.geonature.occtax.settings.AppSettings
import fr.geonature.occtax.settings.AppSettingsViewModel
import fr.geonature.occtax.ui.input.InputPagerFragmentActivity
import fr.geonature.occtax.ui.settings.PreferencesActivity
import org.tinylog.Logger
import java.io.File
import javax.inject.Inject

/**
 * Home screen Activity.
 *
 * @author S. Grimault
 */
@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {

    private val authLoginViewModel: AuthLoginViewModel by viewModels()
    private val appSettingsViewModel: AppSettingsViewModel by viewModels()
    private val packageInfoViewModel: PackageInfoViewModel by viewModels()
    private val dataSyncViewModel: DataSyncViewModel by viewModels()
    private val configureServerSettingsViewModel: ConfigureServerSettingsViewModel by viewModels()
    private val updateSettingsViewModel: UpdateSettingsViewModel by viewModels()
    private val inputViewModel: InputViewModel by viewModels()

    @Inject
    lateinit var geoNatureAPIClient: IGeoNatureAPIClient

    @ContentProviderAuthority
    @Inject
    lateinit var authority: String

    private var homeContent: CoordinatorLayout? = null
    private var appSyncView: AppSyncView? = null
    private var inputRecyclerView: RecyclerView? = null
    private var inputEmptyTextView: TextView? = null
    private var fab: ExtendedFloatingActionButton? = null

    private lateinit var adapter: InputRecyclerViewAdapter
    private var progressSnackbar: Pair<Snackbar, CircularProgressIndicator>? = null

    private var appSettings: AppSettings? = null
    private var isLoggedIn: Boolean = false

    private lateinit var startSyncResultLauncher: ActivityResultLauncher<Intent>

    private val loaderCallbacks = object : LoaderManager.LoaderCallbacks<Cursor> {
        override fun onCreateLoader(
            id: Int,
            args: Bundle?
        ): Loader<Cursor> {
            return when (id) {
                LOADER_APP_SYNC -> CursorLoader(
                    this@HomeActivity,
                    buildUri(
                        authority,
                        AppSync.TABLE_NAME,
                        args?.getString(AppSync.COLUMN_ID)
                            ?: ""
                    ),
                    null,
                    null,
                    null,
                    null
                )
                else -> throw IllegalArgumentException()
            }
        }

        override fun onLoadFinished(
            loader: Loader<Cursor>,
            data: Cursor?
        ) {

            if (data == null) {
                Logger.warn { "failed to load data from '${(loader as CursorLoader).uri}'" }

                return
            }

            when (loader.id) {
                LOADER_APP_SYNC -> {
                    if (data.moveToFirst()) {
                        appSyncView?.setAppSync(AppSync.fromCursor(data))
                    }
                }
            }
        }

        override fun onLoaderReset(loader: Loader<Cursor>) {
            // nothing to do...
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_home)

        homeContent = findViewById(R.id.homeContent)
        appSyncView = findViewById(R.id.appSyncView)
        inputRecyclerView = findViewById(R.id.inputRecyclerView)
        inputEmptyTextView = findViewById(R.id.inputEmptyTextView)
        fab = findViewById(R.id.fab)

        configureAuthLoginViewModel()
        configurePackageInfoViewModel()
        configureDataSyncViewModel()
        configureConfigureServerSettingsViewModel()
        configureUpdateSettingsViewModel()

        appSyncView?.setListener(object : AppSyncView.OnAppSyncViewListener {
            override fun onAction() {
                appSettings?.dataSyncSettings?.run {
                    dataSyncViewModel.startSync(
                        this,
                        HomeActivity::class.java,
                        MainApplication.CHANNEL_DATA_SYNCHRONIZATION
                    )
                    packageInfoViewModel.getAllApplications()
                    packageInfoViewModel.synchronizeInstalledApplications()
                }
            }
        })

        fab?.setOnClickListener {
            val appSettings = appSettings ?: return@setOnClickListener

            startInput(appSettings)
        }

        adapter = InputRecyclerViewAdapter(object :
            AbstractListItemRecyclerViewAdapter.OnListItemRecyclerViewAdapterListener<Input> {
            override fun onClick(item: Input) {
                val appSettings = appSettings ?: return

                Logger.info { "input selected: ${item.id}" }

                startInput(
                    appSettings,
                    item
                )
            }

            override fun onLongClicked(
                position: Int,
                item: Input
            ) {
                inputViewModel.deleteInput(item)

                ContextCompat.getSystemService(
                    this@HomeActivity,
                    Vibrator::class.java
                )?.vibrate(100)

                makeSnackbar(getString(R.string.home_snackbar_input_deleted))
                    ?.setAction(R.string.home_snackbar_input_undo) {
                        inputViewModel.restoreDeletedInput()
                    }
                    ?.show()
            }

            override fun showEmptyTextView(show: Boolean) {
                if (show) fab?.extend() else fab?.shrink()

                if (inputEmptyTextView?.visibility == View.VISIBLE == show) {
                    return
                }

                if (show) {
                    inputEmptyTextView?.startAnimation(
                        AnimationUtils.loadAnimation(
                            this@HomeActivity,
                            android.R.anim.fade_in
                        )
                    )
                    inputEmptyTextView?.visibility = View.VISIBLE
                } else {
                    inputEmptyTextView?.startAnimation(
                        AnimationUtils.loadAnimation(
                            this@HomeActivity,
                            android.R.anim.fade_out
                        )
                    )
                    inputEmptyTextView?.visibility = View.GONE
                }
            }
        })

        inputRecyclerView?.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@HomeActivity.adapter

            val dividerItemDecoration = DividerItemDecoration(
                context,
                (layoutManager as LinearLayoutManager).orientation
            )
            addItemDecoration(dividerItemDecoration)
        }

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
                                HomeActivity::class.java,
                                MainApplication.CHANNEL_DATA_SYNCHRONIZATION
                            )
                        }
                    }
                }
            }

        updateSettingsViewModel.updateAppSettings()
    }

    override fun onResume() {
        super.onResume()

        LoaderManager.getInstance(this)
            .restartLoader(
                LOADER_APP_SYNC,
                bundleOf(AppSync.COLUMN_ID to packageName),
                loaderCallbacks
            )
    }

    @SuppressLint("RestrictedApi")
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(
            R.menu.settings,
            menu
        )
        menuInflater.inflate(
            R.menu.login,
            menu
        )

        if (menu is MenuBuilder) {
            menu.setOptionalIconsVisible(true)
        }

        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.run {
            findItem(R.id.menu_login)?.also {
                it.isEnabled = appSettings != null
                it.isVisible = !isLoggedIn
            }
            findItem(R.id.menu_logout)?.also {
                it.isEnabled = appSettings != null
                it.isVisible = isLoggedIn
            }
        }

        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_settings -> {
                startSyncResultLauncher.launch(
                    PreferencesActivity.newIntent(
                        this,
                        appSettings
                    )
                )
                true
            }
            R.id.menu_login -> {
                startSyncResultLauncher.launch(LoginActivity.newIntent(this))
                true
            }
            R.id.menu_logout -> {
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
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun startInput(
        appSettings: AppSettings,
        input: Input? = null
    ) {
        startActivity(
            InputPagerFragmentActivity.newIntent(
                this,
                appSettings,
                input
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
                this@HomeActivity.isLoggedIn = it
                invalidateOptionsMenu()
            }
        }
    }

    private fun configurePackageInfoViewModel() {
        with(packageInfoViewModel) {
            observe(packageInfos) {
                it.find { packageInfo -> packageInfo.packageName == BuildConfig.APPLICATION_ID }
                    ?.also { packageInfo ->
                        appSyncView?.setPackageInfo(packageInfo)
                    }
            }
        }
    }

    private fun configureDataSyncViewModel() {
        dataSyncViewModel.also { vm ->
            vm.isSyncRunning.observe(
                this@HomeActivity
            ) {
                invalidateOptionsMenu()
            }
            vm
                .observeDataSyncStatus()
                .observe(
                    this@HomeActivity
                ) {
                    if (it == null) {
                        appSyncView?.setDataSyncStatus(it)
                    }

                    it?.run {
                        appSyncView?.setDataSyncStatus(this)

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
                dataSyncViewModel.startSync(
                    it,
                    HomeActivity::class.java,
                    MainApplication.CHANNEL_DATA_SYNCHRONIZATION
                )
                
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

                    fab?.hide()
                    adapter.clear()
                    appSyncView?.enableActionButton(false)
                    invalidateOptionsMenu()

                    makeSnackbar(
                        getString(
                            if (it == null) R.string.snackbar_settings_not_found else R.string.snackbar_settings_map_invalid,
                            appSettingsViewModel.getAppSettingsFilename()
                        )
                    )?.show()
                } else {
                    Logger.info { "app settings successfully loaded" }

                    appSettings = it
                    fab?.show()
                    appSyncView?.enableActionButton(it.dataSyncSettings != null)
                    invalidateOptionsMenu()

                    it.dataSyncSettings?.also { dataSyncSettings ->
                        dataSyncViewModel.configurePeriodicSync(
                            dataSyncSettings,
                            HomeActivity::class.java,
                            MainApplication.CHANNEL_DATA_SYNCHRONIZATION
                        )
                    }

                    loadInputs()
                }
            }
    }

    private fun loadInputs() {
        inputViewModel.readInputs().observe(
            this
        ) {
            adapter.setItems(it)
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
        fab?.hide()
        adapter.clear()
        appSyncView?.enableActionButton(false)
        invalidateOptionsMenu()

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

    companion object {
        private const val LOADER_APP_SYNC = 1
    }
}
