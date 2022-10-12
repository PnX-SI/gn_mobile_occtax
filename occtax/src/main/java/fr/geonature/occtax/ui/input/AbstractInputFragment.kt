package fr.geonature.occtax.ui.input

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import fr.geonature.commons.features.input.domain.AbstractInput
import fr.geonature.commons.lifecycle.observeUntil
import fr.geonature.occtax.features.input.domain.Input
import fr.geonature.occtax.features.input.presentation.InputViewModel
import fr.geonature.viewpager.model.IPageWithValidationFragment

/**
 * `Fragment` using [AbstractInput] as page.
 *
 * @author S. Grimault
 */
abstract class AbstractInputFragment : Fragment(), IPageWithValidationFragment, IInputFragment {

    lateinit var listener: OnInputPageFragmentListener

    private val inputViewModel: InputViewModel by activityViewModels()
    var input: Input? = null
        private set

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is OnInputPageFragmentListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement ${OnInputPageFragmentListener::class.simpleName}")
        }
    }

    override fun onResume() {
        super.onResume()

        // give a chance to this page to refresh correctly both its title and subtitle
        Handler(Looper.getMainLooper()).post {
            (activity as AppCompatActivity?)?.apply {
                setTitle(getResourceTitle())
                supportActionBar?.subtitle = getSubtitle()
            }
        }

        inputViewModel.input.observeUntil(
            viewLifecycleOwner,
            { it != null }) {
            if (it == null) return@observeUntil

            input = it
            listener.validateCurrentPage()
            refreshView()
        }
    }
}