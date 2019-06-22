package fr.geonature.occtax.ui.home

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
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import fr.geonature.commons.data.AppSync
import fr.geonature.commons.data.Provider.buildUri
import fr.geonature.commons.input.InputManager
import fr.geonature.commons.settings.AppSettingsManager
import fr.geonature.occtax.R
import fr.geonature.occtax.input.Input
import fr.geonature.occtax.settings.AppSettings
import fr.geonature.occtax.ui.settings.PreferencesFragment
import fr.geonature.occtax.ui.shared.view.ListItemActionView
import kotlinx.android.synthetic.main.fragment_home.appSyncView
import kotlinx.android.synthetic.main.fragment_home.fab
import kotlinx.android.synthetic.main.fragment_home.homeContent
import kotlinx.android.synthetic.main.fragment_home.inputEmptyTextView
import kotlinx.android.synthetic.main.fragment_home.inputRecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * Home screen [Fragment].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class HomeFragment : Fragment() {

    private var listener: OnHomeFragmentFragmentListener? = null
    private lateinit var adapter: InputRecyclerViewAdapter
    private var appSettings: AppSettings? = null
    private var selectedInputToDelete: Pair<Int, Input>? = null

    private val loaderCallbacks = object : LoaderManager.LoaderCallbacks<Cursor> {
        override fun onCreateLoader(
                id: Int,
                args: Bundle?): Loader<Cursor> {
            when (id) {
                LOADER_APP_SYNC -> return CursorLoader(requireContext(),
                                                       buildUri(AppSync.TABLE_NAME,
                                                                args?.getString(AppSync.COLUMN_ID)
                                                                        ?: ""),
                                                       arrayOf(AppSync.COLUMN_ID,
                                                               AppSync.COLUMN_LAST_SYNC,
                                                               AppSync.COLUMN_INPUTS_TO_SYNCHRONIZE),
                                                       null,
                                                       null,
                                                       null)
                else -> throw IllegalArgumentException()
            }
        }

        override fun onLoadFinished(
                loader: Loader<Cursor>,
                data: Cursor?) {

            if (data == null) return

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

        val selectedInputPositionToDelete = savedInstanceState?.getInt(STATE_SELECTED_INPUT_POSITION_TO_DELETE)
        val selectedInputToDelete = savedInstanceState?.getParcelable<Input>(STATE_SELECTED_INPUT_TO_DELETE)

        if (selectedInputPositionToDelete != null && selectedInputToDelete != null) {
            this.selectedInputToDelete = Pair.create(selectedInputPositionToDelete,
                                                     selectedInputToDelete)
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_home,
                                container,
                                false)
    }

    override fun onViewCreated(
            view: View,
            savedInstanceState: Bundle?) {
        super.onViewCreated(view,
                            savedInstanceState)

        setHasOptionsMenu(true)

        appSyncView.setListener(object : ListItemActionView.OnListItemActionViewListener {
            override fun onAction() {
                listener?.onStartSync()
            }
        })

        fab.setOnClickListener { listener?.onStartInput() }

        adapter = InputRecyclerViewAdapter(object : InputRecyclerViewAdapter.OnInputRecyclerViewAdapterListener {
            override fun onInputClicked(input: Input) {
                Log.i(TAG,
                      "input selected: ${input.id}")

                listener?.onStartInput(input)
            }

            override fun onInputLongClicked(position: Int,
                                            input: Input) {
                selectedInputToDelete = Pair.create(position,
                                                    input)

                GlobalScope.launch(Dispatchers.Main) {
                    listener?.getInputManager()
                        ?.deleteInput(input.id)
                    (inputRecyclerView.adapter as InputRecyclerViewAdapter).remove(input)
                }

                Snackbar.make(homeContent,
                              R.string.home_snackbar_input_deleted,
                              Snackbar.LENGTH_SHORT)
                    .setAction(R.string.home_snackbar_input_undo
                    ) {
                        GlobalScope.launch(Dispatchers.Main) {
                            val inputToRestore = selectedInputToDelete?.second

                            if (inputToRestore != null) {
                                listener?.getInputManager()
                                    ?.saveInput(inputToRestore)
                                (inputRecyclerView.adapter as InputRecyclerViewAdapter).addInput(inputToRestore,
                                                                                                 selectedInputToDelete?.first)
                            }

                            selectedInputToDelete = null
                        }
                    }
                    .show()
            }
        })
        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()

                showEmptyTextView(adapter.itemCount == 0)
            }

            override fun onItemRangeInserted(positionStart: Int,
                                             itemCount: Int) {
                super.onItemRangeInserted(positionStart,
                                          itemCount)

                showEmptyTextView(false)
            }
        })

        with(inputRecyclerView) {
            layoutManager = LinearLayoutManager(context)
            adapter = this@HomeFragment.adapter
        }

        val dividerItemDecoration = DividerItemDecoration(inputRecyclerView.context,
                                                          (inputRecyclerView.layoutManager as LinearLayoutManager).orientation)
        inputRecyclerView.addItemDecoration(dividerItemDecoration)
    }

    override fun onResume() {
        super.onResume()

        LoaderManager.getInstance(this)
            .initLoader(LOADER_APP_SYNC,
                        bundleOf(AppSync.COLUMN_ID to requireContext().packageName),
                        loaderCallbacks)

        GlobalScope.launch(Dispatchers.Main) {
            appSettings = listener?.getAppSettingsManager()
                ?.loadAppSettings()

            if (appSettings == null) {
                showToastMessage(getString(R.string.message_settings_not_found,
                                           listener?.getAppSettingsManager()?.getAppSettingsFilename()))
            }
            else {
                fab.show()
                activity?.invalidateOptionsMenu()

                val inputs: List<Input> = listener?.getInputManager()?.readInputs()
                        ?: emptyList()
                (inputRecyclerView.adapter as InputRecyclerViewAdapter).setInputs(inputs)
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is OnHomeFragmentFragmentListener) {
            listener = context
        }
        else {
            throw RuntimeException("$context must implement OnHomeFragmentFragmentListener")
        }
    }

    override fun onDetach() {
        super.onDetach()

        listener = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        val selectedInputPositionToDelete = this.selectedInputToDelete?.first
        val selectedInputToDelete = this.selectedInputToDelete?.second

        if (selectedInputPositionToDelete != null && selectedInputToDelete != null) {
            outState.putInt(STATE_SELECTED_INPUT_POSITION_TO_DELETE,
                            selectedInputPositionToDelete)
            outState.putParcelable(STATE_SELECTED_INPUT_TO_DELETE,
                                   selectedInputToDelete)
        }

        super.onSaveInstanceState(outState)
    }

    override fun onCreateOptionsMenu(
            menu: Menu,
            inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu,
                                  inflater)

        inflater.inflate(R.menu.settings,
                         menu)
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

    private fun showEmptyTextView(show: Boolean) {
        if (inputEmptyTextView.visibility == View.VISIBLE == show) {
            return
        }

        if (show) {
            inputEmptyTextView.startAnimation(AnimationUtils.loadAnimation(context,
                                                                           android.R.anim.fade_in))
            inputEmptyTextView.visibility = View.VISIBLE

        }
        else {
            inputEmptyTextView.startAnimation(AnimationUtils.loadAnimation(context,
                                                                           android.R.anim.fade_out))
            inputEmptyTextView.visibility = View.GONE
        }
    }

    private fun showToastMessage(message: CharSequence) {
        val context = context ?: return

        Toast.makeText(context,
                       message,
                       Toast.LENGTH_LONG)
            .show()
    }

    /**
     * Callback used by [PreferencesFragment].
     */
    interface OnHomeFragmentFragmentListener {
        fun getInputManager(): InputManager<Input>
        fun getAppSettingsManager(): AppSettingsManager<AppSettings>
        fun onShowSettings()
        fun onStartSync()
        fun onStartInput(input: Input? = null)
    }

    companion object {
        private val TAG = HomeFragment::class.java.name
        private const val LOADER_APP_SYNC = 1
        private const val STATE_SELECTED_INPUT_POSITION_TO_DELETE = "state_selected_input_position_to_delete"
        private const val STATE_SELECTED_INPUT_TO_DELETE = "state_selected_input_to_delete"

        /**
         * Use this factory method to create a new instance of [HomeFragment].
         *
         * @return A new instance of [HomeFragment]
         */
        fun newInstance() = HomeFragment()
    }
}
