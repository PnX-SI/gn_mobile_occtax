package fr.geonature.occtax.ui.home

import android.content.Context
import android.text.format.DateFormat
import android.util.AttributeSet
import android.util.Pair
import android.view.View
import fr.geonature.commons.data.entity.AppSync
import fr.geonature.occtax.R
import fr.geonature.occtax.ui.shared.view.ListItemActionView
import java.text.NumberFormat

/**
 * Custom [View] about [AppSync].
 *
 * @author S. Grimault
 */
class AppSyncView : ListItemActionView {

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

    fun setAppSync(appSync: AppSync?) {
        if (appSync == null) {
            return
        }

        setItems(
            listOf(
                Pair.create(
                    context.getString(R.string.sync_last_synchronization),
                    if (appSync.lastSync == null) context.getString(R.string.sync_last_synchronization_never)
                    else DateFormat.format(
                        context.getString(R.string.sync_last_synchronization_date),
                        appSync.lastSync!!
                    ).toString()
                ),
                Pair.create(
                    context.getString(R.string.sync_inputs_not_synchronized),
                    NumberFormat.getInstance().format(appSync.inputsToSynchronize)
                )
            )
        )
    }

    private fun init() {
        setItems(
            listOf(
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
