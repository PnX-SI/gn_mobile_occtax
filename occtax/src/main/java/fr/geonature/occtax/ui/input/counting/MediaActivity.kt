package fr.geonature.occtax.ui.input.counting

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import org.tinylog.kotlin.Logger

/**
 * Manage media activity.
 *
 * @see MediaFragment
 *
 * @author S. Grimault
 */
class MediaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.run {
            setDisplayHomeAsUpEnabled(true)
        }

        val basePath = intent.getStringExtra(EXTRA_BASE_PATH)

        if (basePath.isNullOrEmpty()) {
            Logger.warn { "missing base path: abort" }

            setResult(Activity.RESULT_CANCELED)
            finish()

            return
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(
                    android.R.id.content,
                    MediaFragment.newInstance(basePath)
                )
                .commit()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                sendResult()
                finish()
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
        setResult(Activity.RESULT_OK)
    }

    companion object {

        private const val EXTRA_BASE_PATH = "extra_base_path"

        fun newIntent(
            context: Context,
            basePath: String
        ): Intent {
            return Intent(
                context,
                MediaActivity::class.java
            ).apply {
                putExtra(
                    EXTRA_BASE_PATH,
                    basePath
                )
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
        }
    }
}