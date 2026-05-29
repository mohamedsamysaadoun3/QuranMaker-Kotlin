package hazem.nurmontage.videoquran.fragment

import android.content.res.Resources
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.databinding.FragmentEditMediaMultipleBinding
import hazem.nurmontage.videoquran.entity_timeline.Entity
import hazem.nurmontage.videoquran.views.TextCustumFont

/**
 * Bottom-sheet fragment for editing multiple selected entities on the timeline.
 *
 * Provides:
 * - **Delete**: Remove all selected entities
 * - **Cut** (split): Split entities at the playhead position (disabled by default,
 *   enabled via [checkSplit] when the playhead intersects an entity)
 *
 * The cut button is visually dimmed when unavailable (gray tint) and highlighted
 * when a valid split position is found. The delete button always remains active.
 *
 * Converted from EditMultipleEntityFragment.java (106 lines).
 */
class EditMultipleEntityFragment : Fragment {

    companion object {
        @Volatile
        @JvmStatic var instance: EditMultipleEntityFragment? = null

        private const val DISABLED_COLOR = -8355712  // 0x808080 — gray
        private const val ENABLED_COLOR = -1          // 0xFFFFFF — white

        fun getInstance(
            callback: IEditMultipleCallback?,
            resources: Resources?,
            countSelect: Int
        ): EditMultipleEntityFragment {
            if (instance == null) {
                instance = EditMultipleEntityFragment(callback, resources, countSelect)
            }
            return instance!!
        }
    }

    /**
     * Callback interface for multi-entity editing events.
     */
    interface IEditMultipleCallback {
        fun onDelete()
    }

    private var btnCut: LinearLayout? = null
    private var countSelect: Int = 0
    private var fragmentBinding: FragmentEditMediaMultipleBinding? = null
    private var iEditMediaCallback: IEditMultipleCallback? = null
    private var ivCut: ImageView? = null
    private var resourcesRef: Resources? = null
    private var tvCut: TextCustumFont? = null
    private var tvDelete: TextCustumFont? = null

    constructor()

    constructor(iEditMultipleCallback: IEditMultipleCallback?, resources: Resources?, countSelect: Int) {
        this.iEditMediaCallback = iEditMultipleCallback
        this.resourcesRef = resources
        this.countSelect = countSelect
    }

    fun setCount_select(count: Int) {
        // Intentionally empty — preserved from original Java source
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentEditMediaMultipleBinding.inflate(inflater, container, false)
        fragmentBinding = binding
        val root: RelativeLayout = binding.root

        if (iEditMediaCallback != null && resourcesRef != null) {
            ivCut = root.findViewById(R.id.iv_cut)
            ivCut?.setColorFilter(DISABLED_COLOR, PorterDuff.Mode.SRC_IN)

            tvDelete = root.findViewById(R.id.tv_delete) as TextCustumFont
            tvDelete?.text = resourcesRef!!.getString(R.string.delete)

            tvCut = root.findViewById(R.id.tv_cut) as TextCustumFont
            tvCut?.text = resourcesRef!!.getString(R.string.cut)
            tvCut?.setTextColor(DISABLED_COLOR)

            root.findViewById<View>(R.id.btn_delete).setOnClickListener {
                iEditMediaCallback?.onDelete()
            }
        }
        return root
    }

    /**
     * Checks whether the playhead position intersects the given entity's rect
     * and enables/disables the split (cut) button accordingly.
     *
     * **Note**: In the original Java source, the logic inside the `try` block
     * always falls through to the disabled state (setting disabled colors and
     * clickable=false after the conditional enable). This appears to be a bug
     * in the original app — the cut button is always disabled at runtime.
     * The Kotlin conversion preserves this exact behavior.
     *
     * @param entity      The entity to check for playhead intersection
     * @param playheadX   The playhead X position on the timeline
     */
    fun checkSplit(entity: Entity?, playheadX: Float) {
        if (entity == null) return
        try {
            if (entity.rect.left <= playheadX && entity.rect.right >= playheadX) {
                btnCut?.isClickable = true
                tvCut?.setTextColor(ENABLED_COLOR)
                ivCut?.setColorFilter(ENABLED_COLOR, PorterDuff.Mode.SRC_IN)
            }
            // Always falls through to disabled state (original bug preserved)
            tvCut?.setTextColor(DISABLED_COLOR)
            ivCut?.setColorFilter(DISABLED_COLOR, PorterDuff.Mode.SRC_IN)
            btnCut?.isClickable = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        fragmentBinding = null
        instance = null
        iEditMediaCallback = null
        super.onDestroyView()
    }
}
