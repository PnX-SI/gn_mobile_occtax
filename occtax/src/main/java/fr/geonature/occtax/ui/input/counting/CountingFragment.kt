package fr.geonature.occtax.ui.input.counting

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.snackbar.Snackbar
import fr.geonature.commons.data.entity.Taxonomy
import fr.geonature.commons.input.AbstractInput
import fr.geonature.commons.ui.adapter.AbstractListItemRecyclerViewAdapter
import fr.geonature.occtax.R
import fr.geonature.occtax.input.CountingMetadata
import fr.geonature.occtax.input.Input
import fr.geonature.occtax.input.InputTaxon
import fr.geonature.occtax.settings.PropertySettings
import fr.geonature.occtax.ui.input.IInputFragment
import fr.geonature.viewpager.ui.AbstractPagerFragmentActivity
import fr.geonature.viewpager.ui.IValidateFragment

/**
 * [Fragment] to let the user to add additional counting information for the given [Input].
 *
 * @author S. Grimault
 */
class CountingFragment : Fragment(),
    IValidateFragment,
    IInputFragment {

    private lateinit var editCountingResultLauncher: ActivityResultLauncher<Intent>

    private var input: Input? = null
    private var adapter: CountingRecyclerViewAdapter? = null
    private var contentView: CoordinatorLayout? = null
    private var recyclerView: RecyclerView? = null
    private var emptyTextView: TextView? = null
    private var fab: ExtendedFloatingActionButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        editCountingResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if ((it.resultCode != Activity.RESULT_OK) || (it.data == null)) {
                    return@registerForActivityResult
                }

                updateCountingMetadata(it.data?.getParcelableExtra(EditCountingMetadataActivity.EXTRA_COUNTING_METADATA))
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(
            R.layout.fragment_recycler_view_fab,
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

        contentView = view.findViewById(android.R.id.content)
        recyclerView = view.findViewById(android.R.id.list)

        emptyTextView = view.findViewById(android.R.id.empty)
        emptyTextView?.text = getString(R.string.counting_no_data)

        fab = view.findViewById(R.id.fab)
        fab?.apply {
            setText(R.string.action_new_counting)
            extend()
            setOnClickListener {
                launchEditCountingMetadataActivity()
            }
        }

        adapter = CountingRecyclerViewAdapter(object :
            AbstractListItemRecyclerViewAdapter.OnListItemRecyclerViewAdapterListener<CountingMetadata> {
            override fun onClick(item: CountingMetadata) {
                launchEditCountingMetadataActivity(item)
            }

            override fun onLongClicked(
                position: Int,
                item: CountingMetadata
            ) {
                adapter?.remove(item)
                (input?.getCurrentSelectedInputTaxon() as InputTaxon?)?.deleteCountingMetadata(item.index)
                (activity as AbstractPagerFragmentActivity?)?.validateCurrentPage()

                context?.run {
                    @Suppress("DEPRECATION")
                    getSystemService(
                        this,
                        Vibrator::class.java
                    )?.vibrate(100)
                }

                contentView?.also {
                    Snackbar.make(
                        it,
                        R.string.counting_snackbar_counting_deleted,
                        Snackbar.LENGTH_LONG
                    )
                        .setAction(
                            R.string.counting_snackbar_counting_undo
                        ) {
                            adapter?.add(
                                item,
                                position
                            )
                            (input?.getCurrentSelectedInputTaxon() as InputTaxon?)?.addCountingMetadata(
                                item
                            )
                        }
                        .show()
                }
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
        })

        with(recyclerView as RecyclerView) {
            layoutManager = LinearLayoutManager(context)
            adapter = this@CountingFragment.adapter

            val dividerItemDecoration = DividerItemDecoration(
                context,
                (layoutManager as LinearLayoutManager).orientation
            )
            addItemDecoration(dividerItemDecoration)
        }
    }

    override fun getResourceTitle(): Int {
        return R.string.pager_fragment_counting_title
    }

    override fun getSubtitle(): CharSequence? {
        return input?.getCurrentSelectedInputTaxon()?.taxon?.name
    }

    override fun pagingEnabled(): Boolean {
        return true
    }

    override fun validate(): Boolean {
        return (input?.getCurrentSelectedInputTaxon() as InputTaxon?)?.getCounting()?.isNotEmpty()
            ?: false
    }

    override fun refreshView() {
        if (input == null) {
            return
        }

        val counting = (input?.getCurrentSelectedInputTaxon() as InputTaxon?)?.getCounting()
            ?: emptyList()

        adapter?.setItems(counting)

        if (counting.isEmpty()) {
            launchEditCountingMetadataActivity()
        }
    }

    override fun setInput(input: AbstractInput) {
        this.input = input as Input
    }

    private fun launchEditCountingMetadataActivity(countingMetadata: CountingMetadata? = null) {
        val context = context ?: return

        editCountingResultLauncher.launch(EditCountingMetadataActivity.newIntent(
            context,
            input?.getCurrentSelectedInputTaxon()?.taxon?.taxonomy
                ?: Taxonomy(
                    Taxonomy.ANY,
                    Taxonomy.ANY
                ),
            countingMetadata,
            *(arguments?.getParcelableArray(ARG_PROPERTIES)
                ?.map { it as PropertySettings }
                ?.toTypedArray() ?: emptyArray())
        ))
    }

    private fun updateCountingMetadata(countingMetadata: CountingMetadata?) {
        if (countingMetadata == null) {
            Toast.makeText(
                context,
                R.string.counting_toast_empty,
                Toast.LENGTH_LONG
            )
                .show()

            return
        }

        if (countingMetadata.isEmpty()) {
            (input?.getCurrentSelectedInputTaxon() as InputTaxon?)?.deleteCountingMetadata(
                countingMetadata.index
            )
            Toast.makeText(
                context,
                R.string.counting_toast_empty,
                Toast.LENGTH_LONG
            )
                .show()

            return
        }

        (input?.getCurrentSelectedInputTaxon() as InputTaxon?)?.addCountingMetadata(
            countingMetadata
        )

        val counting = (input?.getCurrentSelectedInputTaxon() as InputTaxon?)?.getCounting()
            ?: emptyList()

        adapter?.setItems(counting)
    }

    companion object {

        private const val ARG_PROPERTIES = "arg_properties"

        /**
         * Use this factory method to create a new instance of [CountingFragment].
         *
         * @return A new instance of [CountingFragment]
         */
        @JvmStatic
        fun newInstance(vararg propertySettings: PropertySettings) = CountingFragment().apply {
            arguments = Bundle().apply {
                putParcelableArray(
                    ARG_PROPERTIES,
                    propertySettings
                )
            }
        }
    }
}
