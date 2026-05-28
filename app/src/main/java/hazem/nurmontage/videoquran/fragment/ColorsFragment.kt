package hazem.nurmontage.videoquran.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.adapter.ColorAdapter
import hazem.nurmontage.videoquran.core.common.Common
import hazem.nurmontage.videoquran.databinding.FragmentColorsBinding

/**
 * Child fragment displaying a horizontal color palette for
 * iPad frame color selection inside [EditIpadFragment].
 *
 * Uses [ColorAdapter] with [Common.MUSLIM_COLORS] as the palette source.
 * Selection events are forwarded to the host via [EditIpadFragment.IIpadEditCallback].
 *
 * Converted from ColorsFragment.java.
 */
class ColorsFragment : Fragment {

    companion object {
        @Volatile
        private var instance: ColorsFragment? = null

        fun getInstance(
            callback: EditIpadFragment.IIpadEditCallback?,
            index: Int
        ): ColorsFragment {
            if (instance == null) {
                instance = ColorsFragment(callback, index)
            }
            return instance!!
        }
    }

    // ── State ────────────────────────────────────────────────────────
    private var adapter: ColorAdapter? = null
    private var binding: FragmentColorsBinding? = null
    private var iIpadEditCallback: EditIpadFragment.IIpadEditCallback? = null
    private var index: Int = 0
    private var recyclerView: RecyclerView? = null

    private val iColorCallback = object : ColorAdapter.IColor {
        override fun onColor(color: Int, position: Int) {
            scrollToSelectedPosition()
            iIpadEditCallback?.onClick(color, position)
        }
    }

    // ── Constructors ─────────────────────────────────────────────────
    constructor()
    constructor(callback: EditIpadFragment.IIpadEditCallback?, index: Int) {
        this.iIpadEditCallback = callback
        this.index = index
    }

    // ── Lifecycle ────────────────────────────────────────────────────
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val bind = FragmentColorsBinding.inflate(inflater, container, false)
        binding = bind
        val root: LinearLayout = bind.root

        recyclerView = root.findViewById(R.id.rv_color)
        adapter = ColorAdapter(iColorCallback, Common.MUSLIM_COLORS, index)

        recyclerView?.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            itemAnimator = null
            setHasFixedSize(true)
            this@ColorsFragment.adapter?.let { adapter = it }
        }

        try {
            if (index > 3) {
                recyclerView?.scrollToPosition(index - 3)
            }
        } catch (_: Exception) {
        }

        return root
    }

    // ── Public helpers ───────────────────────────────────────────────
    fun scrollToSelectedPosition() {
        val lm = recyclerView?.layoutManager as? LinearLayoutManager ?: return
        adapter?.let { adp ->
            lm.scrollToPositionWithOffset(
                adp.getPosSelect(),
                (recyclerView?.width ?: 0) / 2 - 50
            )
        }
    }

    // ── Cleanup ──────────────────────────────────────────────────────
    override fun onDestroyView() {
        super.onDestroyView()
        instance = null
        binding = null
    }
}
