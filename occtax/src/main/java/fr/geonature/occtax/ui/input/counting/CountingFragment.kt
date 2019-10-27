package fr.geonature.occtax.ui.input.counting

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import fr.geonature.commons.data.Taxonomy
import fr.geonature.commons.input.AbstractInput
import fr.geonature.occtax.R
import fr.geonature.occtax.input.CountingMetadata
import fr.geonature.occtax.input.Input
import fr.geonature.occtax.input.InputTaxon
import fr.geonature.occtax.ui.input.IInputFragment
import fr.geonature.viewpager.ui.IValidateFragment
import kotlinx.android.synthetic.main.fragment_counting.countingContent
import kotlinx.android.synthetic.main.fragment_counting.countingEmptyTextView
import kotlinx.android.synthetic.main.fragment_counting.countingRecyclerView
import kotlinx.android.synthetic.main.fragment_counting.fab

/**
 * [Fragment] to let the user to add additional counting information for the given [Input].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class CountingFragment : Fragment(),
                         IValidateFragment,
                         IInputFragment {

    private var input: Input? = null
    private var adapter: CountingRecyclerViewAdapter? = null

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_counting,
                                container,
                                false)
    }

    override fun onViewCreated(
            view: View,
            savedInstanceState: Bundle?) {
        super.onViewCreated(view,
                            savedInstanceState)

        fab.setOnClickListener {
            val context = context ?: return@setOnClickListener

            startActivityForResult(EditCountingMetadataActivity.newIntent(context,
                                                                          input?.getCurrentSelectedInputTaxon()?.taxon?.taxonomy
                                                                                  ?: Taxonomy(Taxonomy.ANY,
                                                                                              Taxonomy.ANY)),
                                   0)
        }

        adapter = CountingRecyclerViewAdapter(object : CountingRecyclerViewAdapter.OnCountingRecyclerViewAdapterListener {
            override fun onClick(countingMetadata: CountingMetadata) {
                val context = context ?: return
                startActivityForResult(EditCountingMetadataActivity.newIntent(context,
                                                                              input?.getCurrentSelectedInputTaxon()?.taxon?.taxonomy
                                                                                      ?: Taxonomy(Taxonomy.ANY,
                                                                                                  Taxonomy.ANY),
                                                                              countingMetadata),
                                       0)
            }

            override fun onLongClicked(position: Int,
                                       countingMetadata: CountingMetadata) {
                if ((input?.getCurrentSelectedInputTaxon() as InputTaxon?)?.deleteCountingMetadata(countingMetadata.index) != null) {
                    refreshView()

                    Snackbar.make(countingContent,
                                  R.string.counting_snackbar_counting_deleted,
                                  Snackbar.LENGTH_LONG)
                        .setAction(R.string.counting_snackbar_counting_undo
                        ) {
                            (input?.getCurrentSelectedInputTaxon() as InputTaxon?)?.addCountingMetadata(countingMetadata)
                            refreshView()
                        }
                        .show()
                }
            }

            override fun showEmptyTextView(show: Boolean) {
                if (countingEmptyTextView.visibility == View.VISIBLE == show) {
                    return
                }

                if (show) {
                    countingEmptyTextView.startAnimation(AnimationUtils.loadAnimation(context,
                                                                                   android.R.anim.fade_in))
                    countingEmptyTextView.visibility = View.VISIBLE

                }
                else {
                    countingEmptyTextView.startAnimation(AnimationUtils.loadAnimation(context,
                                                                                   android.R.anim.fade_out))
                    countingEmptyTextView.visibility = View.GONE
                }
            }
        })

        with(countingRecyclerView as RecyclerView) {
            layoutManager = LinearLayoutManager(context)
            adapter = this@CountingFragment.adapter

            val dividerItemDecoration = DividerItemDecoration(context,
                                                              (layoutManager as LinearLayoutManager).orientation)
            addItemDecoration(dividerItemDecoration)
        }
    }

    override fun onActivityResult(
            requestCode: Int,
            resultCode: Int,
            data: Intent?) {
        if ((resultCode == Activity.RESULT_OK) && (data != null)) {
            val countingMetadata = data.getParcelableExtra<CountingMetadata>(EditCountingMetadataActivity.EXTRA_COUNTING_METADATA)

            if (countingMetadata.isEmpty()) {
                (input?.getCurrentSelectedInputTaxon() as InputTaxon?)?.deleteCountingMetadata(countingMetadata.index)
                Toast.makeText(context,
                               R.string.counting_toast_empty,
                               Toast.LENGTH_LONG)
                    .show()

                return
            }

            (input?.getCurrentSelectedInputTaxon() as InputTaxon?)?.addCountingMetadata(countingMetadata)
        }
    }

    override fun getResourceTitle(): Int {
        return R.string.pager_fragment_counting_title
    }

    override fun pagingEnabled(): Boolean {
        return true
    }

    override fun validate(): Boolean {
        return true
    }

    override fun refreshView() {
        (input?.getCurrentSelectedInputTaxon() as InputTaxon?)?.getCounting()
            ?.also {
                adapter?.setCounting(it)
            }
    }

    override fun setInput(input: AbstractInput) {
        this.input = input as Input
    }

    companion object {

        /**
         * Use this factory method to create a new instance of [CountingFragment].
         *
         * @return A new instance of [CountingFragment]
         */
        @JvmStatic
        fun newInstance() = CountingFragment()
    }
}