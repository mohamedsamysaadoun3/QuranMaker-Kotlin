package hazem.nurmontage.videoquran.fragment

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.adapter.FontAdapter
import hazem.nurmontage.videoquran.utils.FontProvider
import hazem.nurmontage.videoquran.databinding.FragmentFontBinding

/**
 * Fragment for selecting Quran font typefaces in a horizontal carousel.
 *
 * Uses a [LinearSnapHelper] to snap to the nearest font item,
 * and communicates font selection back to the host via [IFontCallback].
 *
 * Architecture decisions:
 * - Singleton pattern preserved from original for Fragment transaction reuse.
 * - [FontProvider] is instantiated per [onCreateView] to respect config changes.
 * - Initial scroll position is computed from the font name minus its extension.
 * - [isInit] flag prevents the first snap from firing a selection change.
 *
 * Converted from FontFragment.java.
 */
class FontFragment : Fragment {

    companion object {
        @Volatile
        @JvmStatic var instance: FontFragment? = null

        fun getInstance(callback: IFontCallback?, fontName: String?, typeface: Typeface?): FontFragment {
            if (instance == null) {
                instance = FontFragment(callback, fontName, typeface)
            }
            return instance!!
        }
    }

    /** Callback interface for font selection events. */
    interface IFontCallback {
        fun onAdd(fontName: String?, typeface: Typeface?)
        fun onCancel(fontName: String?, typeface: Typeface?)
        fun onDone(fontName: String?, typeface: Typeface?)
    }

    // ── State ────────────────────────────────────────────────────────
    private var fontSelect: String? = null
    private var fragmentBinding: FragmentFontBinding? = null
    private var iFontCallback: IFontCallback? = null
    private var isInit: Boolean = true
    private var lastTypeface: Typeface? = null
    private var lastFont: String? = null
    private var linearLayoutManager: LinearLayoutManager? = null
    private var recyclerView: RecyclerView? = null
    private var typeface: Typeface? = null

    // ── Constructors ─────────────────────────────────────────────────
    constructor()
    constructor(callback: IFontCallback?, fontName: String?, typeface: Typeface?) {
        this.iFontCallback = callback
        this.lastFont = fontName
        this.lastTypeface = typeface
    }

    // ── Public API ───────────────────────────────────────────────────
    fun add(typeface: Typeface?, fontName: String?) {
        this.typeface = typeface
        this.fontSelect = fontName
    }

    // ── Lifecycle ────────────────────────────────────────────────────
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentFontBinding.inflate(inflater, container, false)
        fragmentBinding = binding
        val root: LinearLayout = binding.root

        try {
            val fontProvider = FontProvider(resources)
            recyclerView = root.findViewById(R.id.rv)

            // Find the index of the current font (strip the .ttf extension for lookup)
            val fontIndex = fontProvider.getFontNamesQuran()
                .indexOf(lastFont?.substring(0, lastFont!!.length - 4))

            val fontAdapter = FontAdapter(
                fontProvider,
                iFontCallback,
                fontProvider.getFontNamesQuran(),
                fontIndex
            )

            linearLayoutManager = LinearLayoutManager(requireContext())
            recyclerView?.apply {
                layoutManager = linearLayoutManager
                setHasFixedSize(true)
                adapter = fontAdapter
            }

            // Snap helper for carousel-style scrolling
            val snapHelper = LinearSnapHelper()
            snapHelper.attachToRecyclerView(recyclerView)

            recyclerView?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                }

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    // Skip the first scroll event (layout-triggered)
                    if (isInit) {
                        isInit = false
                        return
                    }
                    val snapView = snapHelper.findSnapView(linearLayoutManager) ?: return
                    val position = linearLayoutManager!!.getPosition(snapView)
                    this@FontFragment.recyclerView?.post {
                        fontAdapter?.setSelected(position)
                    }
                }
            })

            // Scroll to the current font position
            if (fontIndex > 1) {
                recyclerView?.scrollToPosition(fontIndex - 1)
            } else if (fontIndex >= 0) {
                recyclerView?.scrollToPosition(fontIndex)
            }

            // Done button: apply the selected font
            root.findViewById<View>(R.id.btn_done).setOnClickListener {
                iFontCallback?.onDone(fontSelect, typeface)
            }

            // Cancel button: revert to the previous font
            root.findViewById<View>(R.id.btn_cancel).setOnClickListener {
                if (iFontCallback != null && lastFont != null && lastTypeface != null) {
                    iFontCallback!!.onCancel(lastFont, lastTypeface)
                }
            }
        } catch (_: Exception) {
            // Silently absorb layout inflation errors (e.g. missing views in testing)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        fragmentBinding = null
        iFontCallback = null
        instance = null
    }
}
