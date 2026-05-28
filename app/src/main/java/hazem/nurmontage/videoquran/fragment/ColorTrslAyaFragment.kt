package hazem.nurmontage.videoquran.fragment

import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.adapter.ColorAdapter
import hazem.nurmontage.videoquran.constant.AyaTextPreset
import hazem.nurmontage.videoquran.core.common.Constants
import hazem.nurmontage.videoquran.databinding.FragmentColorAyaBinding
import hazem.nurmontage.videoquran.model.data.TranslationQuranEntity
import hazem.nurmontage.videoquran.utils.Utils
import hazem.nurmontage.videoquran.views.TextCustumFont

/**
 * Fragment for selecting the color and text preset (None/Outline/Shadow/Glow)
 * for a translation Aya entity.
 *
 * Displays a horizontal color palette via [ColorAdapter] with the
 * [Constants.MUSLIM_AYA_COLORS] array, plus four preset buttons below.
 * When a color is selected, [IEditEntityCallback.updateAya] is called.
 * When a preset button is tapped, [IEditEntityCallback.updatePreset] is called.
 *
 * The "Done" button confirms the selection via [IEditEntityCallback.onDone].
 *
 * Converted from ColorTrslAyaFragment.java (158 lines).
 */
class ColorTrslAyaFragment : Fragment {

    companion object {
        @Volatile
        private var instance: ColorTrslAyaFragment? = null

        fun getInstance(
            callback: IEditEntityCallback?,
            entity: TranslationQuranEntity?,
            resources: Resources?
        ): ColorTrslAyaFragment {
            if (instance == null) {
                instance = ColorTrslAyaFragment(callback, entity, resources)
            }
            return instance!!
        }
    }

    // IEditEntityCallback is now defined in EditTrslEntityFragment.
    // This typealias preserves backward compatibility for existing callers.
    typealias IEditEntityCallback = EditTrslEntityFragment.IEditEntityCallback

    private var adapter: ColorAdapter? = null
    private var binding: FragmentColorAyaBinding? = null
    private var entitySelect: TranslationQuranEntity? = null
    private var iColor: ColorAdapter.IColor? = null
    private var iEditEntityCallback: IEditEntityCallback? = null
    private var recyclerView: RecyclerView? = null
    private var resourcesRef: Resources? = null

    constructor()

    constructor(
        iEditEntityCallback: IEditEntityCallback?,
        entity: TranslationQuranEntity?,
        resources: Resources?
    ) {
        this.iEditEntityCallback = iEditEntityCallback
        this.entitySelect = entity
        this.resourcesRef = resources
    }

    private fun setupPresetButtons(view: View) {
        val btnNone = view.findViewById<TextCustumFont>(R.id.btnNone)
        val btnOutline = view.findViewById<TextCustumFont>(R.id.btnOutline)
        val btnShadow = view.findViewById<TextCustumFont>(R.id.btnShadow)
        val btnGlow = view.findViewById<TextCustumFont>(R.id.btnGlow)

        btnNone.text = resourcesRef!!.getString(R.string.preset_none)
        btnOutline.text = resourcesRef!!.getString(R.string.preset_outline)
        btnShadow.text = resourcesRef!!.getString(R.string.preset_shadow)
        btnGlow.text = resourcesRef!!.getString(R.string.preset_glow)

        val buttons = arrayOf<TextView>(btnNone, btnOutline, btnShadow, btnGlow)
        val presets = arrayOf(
            AyaTextPreset.NONE, AyaTextPreset.OUTLINE,
            AyaTextPreset.SHADOW, AyaTextPreset.GLOW
        )

        for (i in 0..3) {
            buttons[i].setOnClickListener {
                selectPreset(buttons, i)
                iEditEntityCallback?.updatePreset(presets[i])
            }
        }

        // Determine which preset is currently active
        val entity = entitySelect ?: return
        val currentPreset = entity.get(entity.getmPreset())
        val selectedIndex = when (currentPreset) {
            AyaTextPreset.OUTLINE -> 1
            AyaTextPreset.SHADOW -> 2
            AyaTextPreset.GLOW -> 3
            else -> 0
        }
        selectPreset(buttons, selectedIndex)
    }

    private fun selectPreset(buttons: Array<TextView>, selected: Int) {
        for (i in buttons.indices) {
            buttons[i].isSelected = i == selected
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val bind = FragmentColorAyaBinding.inflate(inflater, container, false)
        binding = bind
        val root: LinearLayout = bind.root

        if (iEditEntityCallback != null && entitySelect != null && resourcesRef != null) {
            recyclerView = root.findViewById(R.id.rv_color)

            iColor = object : ColorAdapter.IColor {
                override fun onColor(color: Int, position: Int) {
                    if (iEditEntityCallback == null) return
                    scrollToSelectedPosition()
                    iEditEntityCallback!!.updateAya(color)
                }
            }

            adapter = ColorAdapter(
                iColor,
                Constants.MUSLIM_AYA_COLORS,
                Utils.indexOf(Constants.MUSLIM_AYA_COLORS, entitySelect!!.getClrAya())
            )
            recyclerView?.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            recyclerView?.itemAnimator = null
            recyclerView?.setHasFixedSize(true)
            recyclerView?.adapter = adapter

            if (adapter!!.getPosSelect() > 2) {
                scrollToSelectedPosition(adapter!!.getPosSelect() - 2)
            }

            root.findViewById<View>(R.id.tab_layout).visibility = View.GONE
            setupPresetButtons(root)

            root.findViewById<View>(R.id.btn_done).setOnClickListener {
                iEditEntityCallback?.onDone()
            }
        }
        return root
    }

    fun scrollToSelectedPosition(position: Int) {
        val lm = recyclerView?.layoutManager as? LinearLayoutManager ?: return
        lm.scrollToPositionWithOffset(position, recyclerView!!.width / 2)
    }

    fun scrollToSelectedPosition() {
        val lm = recyclerView?.layoutManager as? LinearLayoutManager ?: return
        lm.scrollToPositionWithOffset(
            adapter?.getPosSelect() ?: 0,
            (recyclerView?.width ?: 0) / 2 - 50
        )
    }

    override fun onDestroyView() {
        binding = null
        instance = null
        iColor = null
        super.onDestroyView()
    }
}
