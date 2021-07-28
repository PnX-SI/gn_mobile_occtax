package fr.geonature.occtax.ui.home

import android.annotation.SuppressLint
import android.database.Cursor
import android.os.Bundle
import android.os.Vibrator
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import fr.geonature.commons.data.AppSync
import fr.geonature.commons.data.helper.Provider
import fr.geonature.commons.ui.adapter.AbstractListItemRecyclerViewAdapter
import fr.geonature.commons.util.observeOnce
import fr.geonature.occtax.MainApplication
import fr.geonature.occtax.R
import fr.geonature.occtax.input.Input
import fr.geonature.occtax.input.InputViewModel
import fr.geonature.occtax.settings.AppSettings
import fr.geonature.occtax.settings.AppSettingsViewModel
import fr.geonature.occtax.ui.input.InputPagerFragmentActivity
import fr.geonature.occtax.ui.settings.PreferencesActivity
import fr.geonature.occtax.ui.shared.view.ListItemActionView
import fr.geonature.occtax.util.IntentUtils.syncActivity

/**
 * Home screen Activity.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class HomeActivity : AppCompatActivity() {

    private lateinit var appSettingsViewModel: AppSettingsViewModel
    private lateinit var inputViewModel: InputViewModel

    private var homeContent: CoordinatorLayout? = null
    private var appSyncView: AppSyncView? = null
    private var inputRecyclerView: RecyclerView? = null
    private var inputEmptyTextView: TextView? = null
    private var fab: FloatingActionButton? = null

    private lateinit var adapter: InputRecyclerViewAdapter

    private var appSettings: AppSettings? = null

    private val loaderCallbacks = object : LoaderManager.LoaderCallbacks<Cursor> {
        override fun onCreateLoader(
            id: Int,
            args: Bundle?
        ): Loader<Cursor> {
            return when (id) {
                LOADER_APP_SYNC -> CursorLoader(
                    this@HomeActivity,
                    Provider.buildUri(
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
                Log.w(
                    TAG,
                    "Failed to load data from '${(loader as CursorLoader).uri}'"
                )

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

        appSettingsViewModel = configureAppSettingsViewModel()
        inputViewModel = configureInputViewModel()

        appSyncView?.setListener(object : ListItemActionView.OnListItemActionViewListener {
            override fun onAction() {
                syncActivity(this@HomeActivity)?.also {
                    startActivity(it)
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

                Log.i(
                    TAG,
                    "input selected: ${item.id}"
                )

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

        if (checkAppSync()) {
            loadAppSettings()
        }
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

        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.run {
            findItem(R.id.menu_settings)?.also {
                it.isEnabled = appSettings != null
            }
        }

        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_settings -> {
                startActivity(
                    PreferencesActivity.newIntent(
                        this,
                        appSettings
                    )
                )
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

    private fun configureAppSettingsViewModel(): AppSettingsViewModel {
        return ViewModelProvider(
            this,
            fr.geonature.commons.settings.AppSettingsViewModel.Factory {
                AppSettingsViewModel(application)
            }
        ).get(AppSettingsViewModel::class.java)
    }

    private fun configureInputViewModel(): InputViewModel {
        return ViewModelProvider(
            this,
            fr.geonature.commons.input.InputViewModel.Factory { InputViewModel((application as MainApplication).sl.inputManager) }).get(
            InputViewModel::class.java
        )
    }

    private fun loadAppSettings() {
        appSettingsViewModel.loadAppSettings()
            .observeOnce(this) {
                if (it?.mapSettings == null) {
                    fab?.hide()
                    adapter.clear()
                    invalidateOptionsMenu()

                    makeSnackbar(
                        getString(
                            if (it == null) R.string.snackbar_settings_not_found else R.string.snackbar_settings_map_invalid,
                            appSettingsViewModel.getAppSettingsFilename()
                        )
                    )?.show()
                } else {
                    appSettings = it
                    fab?.show()
                    invalidateOptionsMenu()

                    loadInputs()
                }
            }
    }

    private fun loadInputs() {
        inputViewModel.readInputs().observe(
            this,
            {
                adapter.setItems(it)
            }
        )
    }

    private fun checkAppSync(): Boolean {
        if (syncActivity(this) == null) {
            fab?.hide()
            appSyncView?.enableActionButton(false)
            makeSnackbar(getString(R.string.snackbar_app_sync_not_found))?.show()

            return false
        }

        appSyncView?.enableActionButton()

        return true
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

    companion object {
        private val TAG = HomeActivity::class.java.name

        private const val LOADER_APP_SYNC = 1
    }
}
