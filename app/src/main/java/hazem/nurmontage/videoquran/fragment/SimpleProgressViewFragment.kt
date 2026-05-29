package hazem.nurmontage.videoquran.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.databinding.FragmentProgressViewBinding

/**
 * A lightweight progress overlay fragment that hides both progress views
 * (view_1 and view_2) and sets the background to transparent.
 *
 * Used as a placeholder / idle-state fragment in the bottom sheet area
 * where other progress fragments normally appear. The singleton pattern
 * ensures only one instance exists at a time.
 *
 * Converted from SimpleProgressViewFragment.java (41 lines).
 */
class SimpleProgressViewFragment : Fragment() {

    companion object {
        @Volatile
        @JvmStatic var instance: SimpleProgressViewFragment? = null

        fun getInstance(): SimpleProgressViewFragment {
            if (instance == null) {
                instance = SimpleProgressViewFragment()
            }
            return instance!!
        }
    }

    private var fragmentBinding: FragmentProgressViewBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentProgressViewBinding.inflate(inflater, container, false)
        fragmentBinding = binding
        val root: FrameLayout = binding.root
        root.setBackgroundColor(0)
        root.findViewById<View>(R.id.view_1).visibility = View.GONE
        root.findViewById<View>(R.id.view_2).visibility = View.GONE
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        fragmentBinding = null
        instance = null
    }
}
