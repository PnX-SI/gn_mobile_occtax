package fr.geonature.occtax.ui.input.counting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import fr.geonature.occtax.R
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import org.tinylog.Logger
import java.io.File

/**
 * [Fragment] to manage media.
 *
 * @author S. Grimault
 */
class MediaFragment : Fragment() {

    private var content: CoordinatorLayout? = null
    private var fab: ExtendedFloatingActionButton? = null
    private var adapter: MediaRecyclerViewAdapter? = null
    private var takePhotoLifecycleObserver: TakePhotoLifecycleObserver? = null
    private var deletedFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.also {
            takePhotoLifecycleObserver = TakePhotoLifecycleObserver(
                it.applicationContext,
                it.activityResultRegistry
            ).apply {
                lifecycle.addObserver(this)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(
            R.layout.fragment_recycler_view_fab,
            container,
            false
        )
    }

    override fun onViewCreated(
        view: View, savedInstanceState: Bundle?
    ) {
        content = view.findViewById(android.R.id.content)

        val recyclerView = view.findViewById<RecyclerView>(android.R.id.list).apply {
            setPadding(resources.getDimensionPixelSize(R.dimen.padding_default))
        }
        val emptyTextView = view.findViewById<TextView>(android.R.id.empty).apply {
            setText(R.string.counting_media_no_data)
        }

        fab = view.findViewById(R.id.fab)
        fab?.apply {
            setText(R.string.action_add)
            extend()
            setOnClickListener {
                onAddMedia()
            }
        }

        adapter = MediaRecyclerViewAdapter(object :
            MediaRecyclerViewAdapter.OnMediaRecyclerViewAdapterListener {
            override fun onClick(item: File) {
                // TODO:
            }

            override fun onLongClicked(position: Int, item: File) {
                deletedFile = item
                adapter?.remove(item)
                makeSnackbar(getString(R.string.counting_media_deleted))?.setAction(R.string.counting_media_action_undo) {
                    deletedFile?.also {
                        adapter?.add(
                            it,
                            position
                        )
                        deletedFile = null
                    }
                }?.show()
            }

            override fun showEmptyTextView(show: Boolean) {
                if (emptyTextView?.visibility == View.VISIBLE == show) {
                    return
                }

                if (show) {
                    emptyTextView?.startAnimation(
                        AnimationUtils.loadAnimation(
                            context,
                            android.R.anim.fade_in
                        )
                    )
                    emptyTextView?.visibility = View.VISIBLE
                } else {
                    emptyTextView?.startAnimation(
                        AnimationUtils.loadAnimation(
                            context,
                            android.R.anim.fade_out
                        )
                    )
                    emptyTextView?.visibility = View.GONE
                }
            }

            override fun onUpdate(medias: List<File>) {
                // nothing to do…
            }
        })

        with(recyclerView) {
            layoutManager = GridLayoutManager(
                context,
                2
            )
            adapter = this@MediaFragment.adapter
        }

        lifecycleScope.launch {
            adapter?.setItems(loadMediaFiles())
        }
    }

    private suspend fun loadMediaFiles(): List<File> {
        val basePath = arguments?.getString(ARG_BASE_PATH) ?: return emptyList()

        Logger.info { "getting all media from '$basePath'…" }

        return File(basePath).walkTopDown().asFlow().filter { file ->
            file.isFile && file.canWrite() && file.toURI().toURL()
                .run { openConnection().contentType }
                ?.startsWith("image/") == true
        }.toList()
    }

    private fun onAddMedia() {
        val basePath = arguments?.getString(ARG_BASE_PATH) ?: return

        AddPhotoBottomSheetDialogFragment().apply {
            setOnAddPhotoBottomSheetDialogFragmentListener(object :
                AddPhotoBottomSheetDialogFragment.OnAddPhotoBottomSheetDialogFragmentListener {
                override fun onSelectMenuItem(menuItem: AddPhotoBottomSheetDialogFragment.MenuItem) {
                    lifecycleScope.launch {
                        val imageFile =
                            takePhotoLifecycleObserver?.invoke(
                                if (menuItem.iconResourceId == R.drawable.ic_add_photo) TakePhotoLifecycleObserver.ImagePicker.CAMERA else TakePhotoLifecycleObserver.ImagePicker.GALLERY,
                                basePath
                            )

                        if (imageFile != null) {
                            Logger.info { "add image from file '${imageFile.absolutePath}'" }

                            adapter?.add(imageFile)
                        }

                        dismiss()
                    }
                }
            })
        }.show(
            childFragmentManager,
            ADD_PHOTO_DIALOG_FRAGMENT
        )
    }

    private fun makeSnackbar(
        text: CharSequence,
    ): Snackbar? {
        val view = content ?: return null

        return Snackbar.make(
            view,
            text,
            BaseTransientBottomBar.LENGTH_LONG
        )
    }

    companion object {

        private const val ARG_BASE_PATH = "arg_base_path"
        private const val ADD_PHOTO_DIALOG_FRAGMENT = "add_photo_dialog_fragment"

        /**
         * Use this factory method to create a new instance of [MediaFragment].
         *
         * @return A new instance of [MediaFragment]
         */
        @JvmStatic
        fun newInstance(basePath: String) = MediaFragment().apply {
            arguments = Bundle().apply {
                putString(
                    ARG_BASE_PATH,
                    basePath
                )
            }
        }
    }
}