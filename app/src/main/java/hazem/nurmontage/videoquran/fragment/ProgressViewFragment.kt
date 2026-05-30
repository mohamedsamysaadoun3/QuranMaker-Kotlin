package hazem.nurmontage.videoquran.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.databinding.FragmentProgressViewBinding
import hazem.nurmontage.videoquran.views.text.TextCustumFont

/**
 * A progress overlay fragment that displays a "current/total" text counter
 * (e.g., "3/10") during export or rendering operations.
 *
 * The [update] method is called from the host to reflect progress changes.
 * The singleton pattern ensures only one instance exists at a time.
 *
 * Converted from ProgressViewFragment.java (47 lines).
 */
class ProgressViewFragment : Fragment() {

    companion object {
        @Volatile
        private var _instance: ProgressViewFragment? = null

        @JvmStatic
        fun getInstance(): ProgressViewFragment {
            if (_instance == null) {
                _instance = ProgressViewFragment()
            }
            return _instance!!
        }
    }

    private var binding: FragmentProgressViewBinding? = null
    private var tvProgress: TextCustumFont? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val bind = FragmentProgressViewBinding.inflate(inflater, container, false)
        binding = bind
        val root: FrameLayout = bind.root
        tvProgress = root.findViewById(R.id.tv_progress)
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _instance = null
    }

    /**
     * Updates the progress text to show "current/total".
     *
     * @param current The current progress step
     * @param total   The total number of steps
     */
    fun update(current: Int, total: Int) {
        tvProgress?.text = "$current/$total"
    }
}
