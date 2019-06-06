package fr.geonature.occtax.ui.input

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.viewpager.widget.ViewPager
import fr.geonature.commons.input.InputManager
import fr.geonature.occtax.R
import fr.geonature.occtax.input.Input
import fr.geonature.occtax.input.io.OnInputJsonReaderListenerImpl
import fr.geonature.occtax.input.io.OnInputJsonWriterListenerImpl
import fr.geonature.occtax.ui.input.observers.ObserversAndDateInputFragment
import fr.geonature.occtax.ui.input.taxa.TaxaFragment
import fr.geonature.viewpager.ui.AbstractNavigationHistoryPagerFragmentActivity
import fr.geonature.viewpager.ui.AbstractPagerFragmentActivity
import fr.geonature.viewpager.ui.IValidateFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * [ViewPager] implementation as [AbstractPagerFragmentActivity] with navigation history support.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class InputPagerFragmentActivity : AbstractNavigationHistoryPagerFragmentActivity() {

    private lateinit var inputManager: InputManager
    private lateinit var input: Input

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        inputManager = InputManager(application,
                                    OnInputJsonReaderListenerImpl(),
                                    OnInputJsonWriterListenerImpl())

        readCurrentInput()
    }

    override fun onPause() {
        super.onPause()

        GlobalScope.launch(Dispatchers.Main) {
            inputManager.saveInput(input)
        }
    }

    override val pagerFragments: Map<Int, IValidateFragment>
        get() = LinkedHashMap<Int, IValidateFragment>().apply {
            put(R.string.pager_fragment_observers_and_date_input_title,
                ObserversAndDateInputFragment.newInstance())
            put(R.string.pager_fragment_taxa_title,
                TaxaFragment.newInstance())
        }

    override fun performFinishAction() {
        GlobalScope.launch(Dispatchers.Main) {
            inputManager.exportInput(input.id)
        }

        finish()
    }

    override fun onPageSelected(position: Int) {
        super.onPageSelected(position)

        setInputToCurrentPage()
    }

    private fun readCurrentInput() {
        GlobalScope.launch(Dispatchers.Main) {
            val input = inputManager.readCurrentInput() ?: Input()

            Log.i(TAG, "loading input: ${input.id}")

            pagerHelper.load(input.id)

            this@InputPagerFragmentActivity.input = input as Input
            this@InputPagerFragmentActivity.setInputToCurrentPage()
        }
    }

    private fun setInputToCurrentPage() {
        val pageFragment = getCurrentPageFragment()

        if (pageFragment is IInputFragment && ::input.isInitialized) {
            pageFragment.setInput(input)
            pageFragment.refreshView()
            validateCurrentPage()
        }
    }

    companion object {

        private val TAG = InputPagerFragmentActivity::class.java.name

        fun newIntent(context: Context): Intent {
            return Intent(context,
                          InputPagerFragmentActivity::class.java)
        }
    }
}
