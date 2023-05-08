package fr.geonature.occtax.ui.input.counting

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils.loadAnimation
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import fr.geonature.compat.content.getParcelableExtraCompat
import fr.geonature.occtax.R
import fr.geonature.occtax.features.record.domain.CountingRecord
import fr.geonature.occtax.features.record.domain.TaxonRecord
import fr.geonature.viewpager.ui.UnderlinePagerIndicator
import kotlinx.coroutines.launch
import org.tinylog.Logger
import java.io.File

/**
 * Manage media from given [CountingRecord] activity.
 *
 * @author S. Grimault
 */
@AndroidEntryPoint
class MediaListActivity : AppCompatActivity() {

    private lateinit var taxonRecord: TaxonRecord
    private lateinit var countingRecord: CountingRecord

    private var content: CoordinatorLayout? = null
    private var viewPager: ViewPager2? = null
    private var fabMenu: FloatingActionButton? = null
    private var takePhotoLifecycleObserver: TakePhotoLifecycleObserver? = null
    private var adapter: MediaRecyclerViewAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_media_pager)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        content = findViewById(R.id.content)
        val progressBar: ProgressBar = findViewById(android.R.id.progress)
        val emptyTextView: TextView = findViewById(android.R.id.empty)

        takePhotoLifecycleObserver = TakePhotoLifecycleObserver(
            this,
            activityResultRegistry
        ).apply {
            lifecycle.addObserver(this)
        }

        val fabFromPhoto = findViewById<FloatingActionButton>(R.id.fab_photo)?.apply {
            setOnClickListener { onAddMedia(TakePhotoLifecycleObserver.ImagePicker.CAMERA) }
        }

        val fabFromGallery = findViewById<FloatingActionButton>(R.id.fab_gallery)?.apply {
            setOnClickListener { onAddMedia(TakePhotoLifecycleObserver.ImagePicker.GALLERY) }
        }

        val fabOpenAnimation = loadAnimation(
            this,
            R.anim.fab_open
        )
        val fabCloseAnimation = loadAnimation(
            this,
            R.anim.fab_close
        )

        fabMenu = findViewById<FloatingActionButton>(R.id.fab_main)?.apply {
            setOnClickListener {
                val opened = it.tag == true
                it.tag = !opened

                animate().apply {
                    duration = 300L
                    rotation(if (opened) 0.0F else 135.0F)
                }

                fabFromPhoto?.startAnimation(if (opened) fabCloseAnimation else fabOpenAnimation)
                fabFromGallery?.startAnimation(if (opened) fabCloseAnimation else fabOpenAnimation)
            }
        }

        adapter = MediaRecyclerViewAdapter(object :
            MediaRecyclerViewAdapter.OnMediaRecyclerViewAdapterListener {
            override fun onClick(item: File) {
                if (fabMenu?.tag == true) {
                    fabMenu?.performClick()
                }
            }

            override fun onLongClicked(position: Int, item: File) {
                // nothing to do…
            }

            override fun showEmptyTextView(show: Boolean) {
                progressBar.visibility = View.GONE

                if (emptyTextView.visibility == View.VISIBLE == show) {
                    return
                }

                if (show) {
                    emptyTextView.startAnimation(
                        loadAnimation(
                            this@MediaListActivity,
                            android.R.anim.fade_in
                        )
                    )
                    emptyTextView.visibility = View.VISIBLE
                } else {
                    emptyTextView.startAnimation(
                        loadAnimation(
                            this@MediaListActivity,
                            android.R.anim.fade_out
                        )
                    )
                    emptyTextView.visibility = View.GONE
                }
            }

            override fun onUpdate(medias: List<File>) {
                // nothing to do…
            }
        })

        taxonRecord = intent.getParcelableExtraCompat(EXTRA_TAXON_RECORD) ?: run {
            // TODO: show a toast message about missing taxon record
            finish()
            return
        }

        countingRecord = intent.getParcelableExtraCompat(EXTRA_COUNTING_RECORD) ?: run {
            // TODO: show a toast message about missing counting record
            finish()
            return
        }

        val medias = countingRecord.medias.files.map { File(it) }
        val selectedMedia = intent.getStringExtra(EXTRA_SELECTED_MEDIA)
            ?.let { File(it) }

        adapter?.setItems(medias)

        viewPager = findViewById<ViewPager2>(R.id.pager)?.apply {
            this.adapter = this@MediaListActivity.adapter
            currentItem = selectedMedia?.let { medias.indexOf(it) }
                ?.takeIf { it >= 0 } ?: 0
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    if (fabMenu?.tag == true) {
                        fabMenu?.performClick()
                    }
                }
            })
        }

        findViewById<UnderlinePagerIndicator>(R.id.indicator)?.also { pagerIndicator ->
            viewPager?.also { pagerIndicator.setViewPager(it) }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(
            R.menu.delete,
            menu
        )

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                sendResult()
                finish()
                true
            }
            R.id.menu_delete -> {
                if (fabMenu?.tag == true) {
                    fabMenu?.performClick()
                }

                val currentIndex = viewPager?.currentItem
                val currentMedia = currentIndex?.let { adapter?.items?.get(it) }
                    ?.also {
                        adapter?.remove(it)
                    }

                makeSnackbar(getString(R.string.counting_media_deleted))?.setAction(R.string.counting_media_action_undo) {
                    currentMedia?.also {
                        adapter?.add(
                            it,
                            currentIndex
                        )
                        adapter?.items?.size?.also { size -> viewPager?.currentItem = size }
                    }
                }
                    ?.show()

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        sendResult()
        finish()
    }

    private fun sendResult() {
        setResult(
            Activity.RESULT_OK,
            Intent().apply {
                putExtra(
                    EXTRA_COUNTING_RECORD,
                    countingRecord.apply {
                        adapter?.items?.map { it.absolutePath }
                            ?.also {
                                medias.files = it
                            }
                    }
                )
            })
    }

    private fun onAddMedia(picker: TakePhotoLifecycleObserver.ImagePicker) {
        lifecycleScope.launch {
            val imageFile =
                takePhotoLifecycleObserver?.invoke(
                    picker,
                    taxonRecord.counting.mediaBasePath(
                        this@MediaListActivity,
                        countingRecord
                    ).absolutePath
                )

            if (imageFile != null) {
                Logger.info { "add image from file '${imageFile.absolutePath}'" }

                adapter?.add(imageFile)
                adapter?.items?.size?.also { viewPager?.currentItem = it }
            }
        }
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

        const val EXTRA_COUNTING_RECORD = "extra_counting_record"
        private const val EXTRA_TAXON_RECORD = "extra_taxon_record"
        private const val EXTRA_SELECTED_MEDIA = "extra_selected_media"

        fun newIntent(
            context: Context,
            taxonRecord: TaxonRecord,
            countingRecord: CountingRecord? = null,
            selectedMedia: String? = null
        ): Intent {
            return Intent(
                context,
                MediaListActivity::class.java
            ).apply {
                putExtra(
                    EXTRA_TAXON_RECORD,
                    taxonRecord
                )
                countingRecord?.also {
                    putExtra(
                        EXTRA_COUNTING_RECORD,
                        countingRecord
                    )
                }
                putExtra(
                    EXTRA_SELECTED_MEDIA,
                    selectedMedia
                )
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
        }
    }
}