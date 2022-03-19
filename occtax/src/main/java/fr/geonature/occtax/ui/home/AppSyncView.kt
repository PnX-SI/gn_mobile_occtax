package fr.geonature.occtax.ui.home

import android.content.Context
import android.text.format.DateFormat
import android.util.AttributeSet
import android.util.Pair
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.work.WorkInfo
import fr.geonature.commons.data.entity.AppSync
import fr.geonature.datasync.packageinfo.PackageInfo
import fr.geonature.datasync.sync.DataSyncStatus
import fr.geonature.occtax.R
import fr.geonature.occtax.ui.shared.view.ListItemActionView
import java.text.NumberFormat

/**
 * Custom [View] about [AppSync].
 *
 * @author S. Grimault
 */
class AppSyncView : ConstraintLayout {

    private lateinit var iconStatus: TextView
    private lateinit var listItemActionView: ListItemActionView
    private var listener: OnAppSyncViewListener? = null

    private val stateAnimation = AlphaAnimation(
        0.0f,
        1.0f
    ).apply {
        duration = 250
        startOffset = 10
        repeatMode = Animation.REVERSE
        repeatCount = Animation.INFINITE
    }

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(
        context: Context,
        attrs: AttributeSet
    ) : super(
        context,
        attrs
    ) {
        init()
    }

    constructor(
        context: Context,
        attrs: AttributeSet,
        defStyleAttr: Int
    ) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    fun setListener(listener: OnAppSyncViewListener) {
        this.listener = listener
    }

    fun enableActionButton(enabled: Boolean = true) {
        listItemActionView.enableActionButton(enabled)
    }

    fun setDataSyncStatus(dataSyncStatus: DataSyncStatus?) {
        when (dataSyncStatus?.state ?: WorkInfo.State.ENQUEUED) {
            WorkInfo.State.RUNNING -> {
                iconStatus.setTextColor(
                    ResourcesCompat.getColor(
                        resources,
                        R.color.datasync_status_pending,
                        context?.theme
                    )
                )

                if (iconStatus.animation?.hasStarted() != true) {
                    iconStatus.startAnimation(stateAnimation)
                }
            }
            WorkInfo.State.FAILED -> {
                iconStatus.setTextColor(
                    ResourcesCompat.getColor(
                        resources,
                        R.color.datasync_status_ko,
                        context?.theme
                    )
                )
                iconStatus.clearAnimation()
            }
            WorkInfo.State.SUCCEEDED -> {
                iconStatus.setTextColor(
                    ResourcesCompat.getColor(
                        resources,
                        R.color.datasync_status_ok,
                        context?.theme
                    )
                )
                iconStatus.clearAnimation()
            }
            else -> {
                iconStatus.setTextColor(
                    ResourcesCompat.getColor(
                        resources,
                        R.color.datasync_status_unknown,
                        context?.theme
                    )
                )
                iconStatus.clearAnimation()
            }
        }

        listItemActionView.set(
            Pair.create(
                context.getString(R.string.sync_data),
                dataSyncStatus?.syncMessage
            ),
            0
        )
    }

    fun setPackageInfo(packageInfo: PackageInfo) {
        val numberOfInputsToSynchronize = packageInfo.inputsStatus?.inputs ?: return

        listItemActionView.set(
            Pair.create(
                context.getString(R.string.sync_inputs_not_synchronized),
                NumberFormat.getInstance().format(numberOfInputsToSynchronize)
            ),
            2
        )
    }

    fun setAppSync(appSync: AppSync?) {
        if (appSync == null) {
            return
        }

        listItemActionView.set(
            Pair.create(
                context.getString(R.string.sync_last_synchronization),
                if (appSync.lastSync == null) context.getString(R.string.sync_last_synchronization_never)
                else DateFormat.format(
                    context.getString(R.string.sync_last_synchronization_date),
                    appSync.lastSync!!
                ).toString()
            ),
            1
        )
        listItemActionView.set(
            Pair.create(
                context.getString(R.string.sync_inputs_not_synchronized),
                NumberFormat.getInstance().format(appSync.inputsToSynchronize)
            ),
            2
        )
    }

    private fun init() {
        View.inflate(
            context,
            R.layout.view_app_sync,
            this
        )

        iconStatus = findViewById(android.R.id.icon)
        listItemActionView = findViewById<ListItemActionView?>(R.id.list_item).also {
            it.setListener(object : ListItemActionView.OnListItemActionViewListener {
                override fun onAction() {
                    listener?.onAction()
                }
            })

            it.setItems(
                listOf(
                    Pair.create(
                        context.getString(R.string.sync_data),
                        null
                    ),
                    Pair.create(
                        context.getString(R.string.sync_last_synchronization),
                        context.getString(R.string.sync_last_synchronization_never)
                    ),
                    Pair.create(
                        context.getString(R.string.sync_inputs_not_synchronized),
                        NumberFormat.getInstance().format(0)
                    )
                )
            )
        }
    }

    /**
     * Callback used by [AppSyncView].
     */
    interface OnAppSyncViewListener {

        /**
         * Called when the 'synchronize' button has been clicked.
         */
        fun onAction()
    }
}
