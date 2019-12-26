package fr.geonature.occtax.ui.home

import android.Manifest
import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import fr.geonature.commons.data.AppSync
import fr.geonature.commons.data.helper.Provider.buildUri
import fr.geonature.commons.ui.adapter.AbstractListItemRecyclerViewAdapter
import fr.geonature.commons.util.PermissionUtils
import fr.geonature.commons.util.PermissionUtils.checkPermissions
import fr.geonature.commons.util.PermissionUtils.checkSelfPermissions
import fr.geonature.commons.util.PermissionUtils.requestPermissions
import fr.geonature.occtax.R
import fr.geonature.occtax.input.Input
import fr.geonature.occtax.input.InputViewModel
import fr.geonature.occtax.settings.AppSettings
import fr.geonature.occtax.settings.AppSettingsViewModel
import fr.geonature.occtax.ui.shared.view.ListItemActionView
import kotlinx.android.synthetic.main.fragment_home.*

/**
 * Home screen [Fragment].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class HomeFragment : Fragment() {

    private var listener: OnHomeFragmentListener? = null
    private lateinit var adapter: InputRecyclerViewAdapter
    private var appSettings: AppSettings? = null
    private var appSettingsViewModel: AppSettingsViewModel? = null
    private var inputViewModel: InputViewModel? = null

    private val loaderCallbacks = object : LoaderManager.LoaderCallbacks<Cursor> {
        override fun onCreateLoader(
            id: Int,
            args: Bundle?
        ): Loader<Cursor> {
            return when (id) {
                LOADER_APP_SYNC -> CursorLoader(
                    requireContext(),
                    buildUri(
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
                        appSyncView.setAppSync(AppSync.fromCursor(data))
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

        appSettingsViewModel = activity?.run {
            ViewModelProvider(this,
                fr.geonature.commons.settings.AppSettingsViewModel.Factory {
                    AppSettingsViewModel(
                        this.application
                    )
                }).get(AppSettingsViewModel::class.java)
        }

        inputViewModel = activity?.run {
            ViewModelProvider(this,
                fr.geonature.commons.input.InputViewModel.Factory { InputViewModel(this.application) }).get(
                InputViewModel::class.java
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(
            R.layout.fragment_home,
            container,
            false
        )
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(
            view,
            savedInstanceState
        )

        setHasOptionsMenu(true)

        appSyncView.setListener(object : ListItemActionView.OnListItemActionViewListener {
            override fun onAction() {
                listener?.onStartSync()
            }
        })

        fab.setOnClickListener {
            val appSettings = appSettings ?: return@setOnClickListener
            listener?.onStartInput(appSettings)
        }

        adapter = InputRecyclerViewAdapter(object :
            AbstractListItemRecyclerViewAdapter.OnListItemRecyclerViewAdapterListener<Input> {
            override fun onClick(item: Input) {
                val appSettings = appSettings ?: return

                Log.i(
                    TAG,
                    "input selected: ${item.id}"
                )

                listener?.onStartInput(
                    appSettings,
                    item
                )
            }

            override fun onLongClicked(
                position: Int,
                item: Input
            ) {
                inputViewModel?.deleteInput(item)

                Snackbar.make(
                    homeContent,
                    R.string.home_snackbar_input_deleted,
                    Snackbar.LENGTH_SHORT
                )
                    .setAction(
                        R.string.home_snackbar_input_undo
                    ) {
                        inputViewModel?.restoreDeletedInput()
                    }
                    .show()
            }

            override fun showEmptyTextView(show: Boolean) {
                if (inputEmptyTextView.visibility == View.VISIBLE == show) {
                    return
                }

                if (show) {
                    inputEmptyTextView.startAnimation(
                        AnimationUtils.loadAnimation(
                            context,
                            android.R.anim.fade_in
                        )
                    )
                    inputEmptyTextView.visibility = View.VISIBLE
                } else {
                    inputEmptyTextView.startAnimation(
                        AnimationUtils.loadAnimation(
                            context,
                            android.R.anim.fade_out
                        )
                    )
                    inputEmptyTextView.visibility = View.GONE
                }
            }
        })

        with(inputRecyclerView) {
            layoutManager = LinearLayoutManager(context)
            adapter = this@HomeFragment.adapter
        }

        val dividerItemDecoration = DividerItemDecoration(
            inputRecyclerView.context,
            (inputRecyclerView.layoutManager as LinearLayoutManager).orientation
        )
        inputRecyclerView.addItemDecoration(dividerItemDecoration)

        checkSelfPermissions()
    }

    override fun onResume() {
        super.onResume()

        LoaderManager.getInstance(this)
            .restartLoader(
                LOADER_APP_SYNC,
                bundleOf(AppSync.COLUMN_ID to requireContext().packageName),
                loaderCallbacks
            )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is OnHomeFragmentListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnHomeFragmentListener")
        }
    }

    override fun onDetach() {
        super.onDetach()

        listener = null
    }

    override fun onCreateOptionsMenu(
        menu: Menu,
        inflater: MenuInflater
    ) {
        super.onCreateOptionsMenu(
            menu,
            inflater
        )

        inflater.inflate(
            R.menu.settings,
            menu
        )
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)

        val menuItemSettings = menu.findItem(R.id.menu_settings)
        menuItemSettings.isEnabled = appSettings != null
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_settings -> {
                listener?.onShowSettings()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_STORAGE_PERMISSIONS -> {
                val requestPermissionsResult = checkPermissions(grantResults)

                if (requestPermissionsResult) {
                    Snackbar.make(
                        homeContent,
                        R.string.snackbar_permission_external_storage_available,
                        Snackbar.LENGTH_LONG
                    )
                        .show()
                    loadAppSettings()
                } else {
                    Snackbar.make(
                        homeContent,
                        R.string.snackbar_permissions_not_granted,
                        Snackbar.LENGTH_LONG
                    )
                        .show()
                }
            }
            else -> super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
            )
        }
    }

    private fun checkSelfPermissions() {
        val context = context ?: return

        checkSelfPermissions(
            context,
            object : PermissionUtils.OnCheckSelfPermissionListener {
                override fun onPermissionsGranted() {
                    loadAppSettings()
                }

                override fun onRequestPermissions(vararg permissions: String) {
                    requestPermissions(
                        this@HomeFragment,
                        homeContent,
                        R.string.snackbar_permission_external_storage_rationale,
                        REQUEST_STORAGE_PERMISSIONS,
                        *permissions
                    )
                }
            },
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    private fun loadAppSettings() {
        appSettingsViewModel?.getAppSettings<AppSettings>()
            ?.observe(this,
                Observer {
                    if (it == null) {
                        fab.hide()
                        adapter.clear()
                        activity?.invalidateOptionsMenu()

                        Snackbar.make(
                            homeContent,
                            getString(
                                R.string.snackbar_settings_not_found,
                                appSettingsViewModel?.getAppSettingsFilename()
                            ),
                            Snackbar.LENGTH_LONG
                        )
                            .show()
                    } else {
                        appSettings = it
                        fab.show()
                        activity?.invalidateOptionsMenu()

                        loadInputs()
                    }
                })
    }

    private fun loadInputs() {
        inputViewModel?.readInputs()
            ?.observe(this,
                Observer {
                    adapter.setItems(it)
                })
    }

    /**
     * Callback used by [HomeFragment].
     */
    interface OnHomeFragmentListener {
        fun onShowSettings()
        fun onStartSync()
        fun onStartInput(
            appSettings: AppSettings,
            input: Input? = null
        )
    }

    companion object {
        private val TAG = HomeFragment::class.java.name
        private const val LOADER_APP_SYNC = 1
        private const val REQUEST_STORAGE_PERMISSIONS = 0

        /**
         * Use this factory method to create a new instance of [HomeFragment].
         *
         * @return A new instance of [HomeFragment]
         */
        @JvmStatic
        fun newInstance() = HomeFragment()
    }
}
