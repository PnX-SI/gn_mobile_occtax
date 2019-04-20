package fr.geonature.occtax.ui.home

import android.content.Context
import android.text.format.DateFormat
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import fr.geonature.commons.data.AppSync
import fr.geonature.occtax.R
import java.text.NumberFormat

/**
 * Custom [View] about [AppSync].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class AppSyncView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null) : ConstraintLayout(context,
        attrs) {

    private val textViewLastSyncValue: TextView
    private val textViewInputsNotSyncValue: TextView
    private var listener: OnAppSyncViewListener? = null

    init {

        View.inflate(context, R.layout.view_app_sync, this)

        textViewLastSyncValue = findViewById(R.id.textViewLastSyncValue)
        textViewInputsNotSyncValue = findViewById(R.id.textViewInputsNotSyncValue)
        findViewById<View>(R.id.buttonSynchronize).setOnClickListener { listener?.onStartSync() }
    }

    fun setAppSync(appSync: AppSync?) {
        if (appSync == null) {
            return
        }

        textViewLastSyncValue.text = if (appSync.lastSync == null) context.getString(R.string.sync_last_synchronization_never)
        else DateFormat.format(context.getString(R.string.sync_last_synchronization_date),
                appSync.lastSync!!)
        textViewInputsNotSyncValue.text = NumberFormat.getInstance()
                .format(appSync.inputsToSynchronize)
    }

    fun setListener(listener: OnAppSyncViewListener) {
        this.listener = listener
    }

    /**
     * Callback used by [AppSyncView].
     */
    interface OnAppSyncViewListener {
        fun onStartSync()
    }
}
