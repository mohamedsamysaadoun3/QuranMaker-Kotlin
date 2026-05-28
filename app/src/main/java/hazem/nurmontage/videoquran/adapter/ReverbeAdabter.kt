package hazem.nurmontage.videoquran.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.fragment.audio_effect.Reverbe
import hazem.nurmontage.videoquran.views.text.TextCustumFont

/**
 * RecyclerView adapter for displaying audio reverb/effect presets.
 *
 * Originally: ReverbeAdabter.java (91 lines)
 * Converted to: ReverbeAdabter.kt — idiomatic Kotlin, full logic preserved
 *
 * Each row displays a reverb preset name with a play/pause icon.
 * The adapter uses a toggle-selection model:
 *
 * - **First tap**: Selects the preset and starts playing the audio preview.
 *   The row background changes to `R.drawable.item_reverb_select` and the
 *   icon switches from play to pause. The FFmpeg command associated with
 *   the preset is sent via [IReverbPresetCallback.cmd].
 *
 * - **Second tap** (on the same row): Deselects the preset and pauses
 *   the audio. The selection is cleared (position set to -1), the row
 *   reverts to `R.drawable.round_btn_in_dark`, and the icon switches
 *   back to play. [IReverbPresetCallback.pause] is called.
 *
 * - **Tap a different row**: Switches selection from the current preset
 *   to the new one. The previous row is deselected and the new one is
 *   selected, with both visual states updated. A new FFmpeg command is
 *   issued for the new preset.
 *
 * The [IReverbPresetCallback.pause] method is always called before any
 * selection change, ensuring the previous audio preview stops before
 * a new one starts or the playback is paused entirely.
 *
 * @see IReverbPresetCallback
 * @see Reverbe
 */
class ReverbeAdabter(
    private val list: List<Reverbe>,
    private val iReverbCallback: IReverbPresetCallback?,
    selected: Int
) : RecyclerView.Adapter<ReverbeAdabter.ViewHolder>() {

    /** The currently selected position (-1 = none selected / paused). */
    private var select: Int = selected

    // ──────────────────────────────────────────────────────────────────────
    // Inner interface
    // ──────────────────────────────────────────────────────────────────────

    /**
     * Callback interface for reverb preset interactions.
     *
     * The hosting Fragment (e.g., ReverbePresetFragment) implements this
     * to control audio playback and apply FFmpeg effects.
     */
    interface IReverbPresetCallback {
        /**
         * Execute an FFmpeg audio filter command for the selected preset.
         *
         * @param cmdFfmpeg The FFmpeg filter string (e.g., "aecho=0.8:0.88:60:0.4")
         * @param position  The adapter position of the selected preset
         */
        fun cmd(cmdFfmpeg: String, position: Int)

        /**
         * Pause the current audio preview playback.
         * Called before any selection change to stop the previous preview.
         */
        fun pause()
    }

    // ──────────────────────────────────────────────────────────────────────
    // Public API
    // ──────────────────────────────────────────────────────────────────────

    /** Returns the full list of reverb presets. */
    fun getList(): List<Reverbe> = list

    // ──────────────────────────────────────────────────────────────────────
    // Adapter overrides
    // ──────────────────────────────────────────────────────────────────────

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.row_reverbe, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.text.text = list[position].name

        if (select == position) {
            // Selected state: highlighted background + pause icon
            holder.itemView.setBackgroundResource(R.drawable.item_reverb_select)
            holder.ivBtnPlay.setImageResource(R.drawable.pause_24px)
        } else {
            // Default state: dark background + play icon
            holder.itemView.setBackgroundResource(R.drawable.round_btn_in_dark)
            holder.ivBtnPlay.setImageResource(R.drawable.play_arrow_24px)
        }
    }

    override fun getItemCount(): Int = list.size

    // ──────────────────────────────────────────────────────────────────────
    // ViewHolder
    // ──────────────────────────────────────────────────────────────────────

    /**
     * ViewHolder for a single reverb preset row.
     *
     * Contains:
     * - [ivBtnPlay]: Play/pause icon toggle
     * - [text]: The preset name label
     *
     * Click behavior:
     * 1. Always calls [IReverbPresetCallback.pause] first
     * 2. If the same preset is tapped again -> deselect (select = -1)
     * 3. If a different preset is tapped -> switch selection + apply new effect
     */
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val text: TextCustumFont = itemView.findViewById(R.id.word_aya)
        val ivBtnPlay: ImageView = itemView.findViewById(R.id.iv_btn_play)

        init {
            itemView.setOnClickListener {
                if (iReverbCallback == null) return@setOnClickListener

                // Always pause the current preview before any change
                iReverbCallback.pause()

                val pos = adapterPosition
                if (pos == RecyclerView.NO_POSITION) return@setOnClickListener

                if (select == pos) {
                    // Toggle off: deselect the current preset
                    val prevSelect = select
                    select = -1
                    notifyItemChanged(prevSelect)
                    notifyItemChanged(pos)
                } else {
                    // Switch to a new preset
                    val prevSelect = select
                    select = pos
                    if (prevSelect != -1) {
                        notifyItemChanged(prevSelect)
                    }
                    notifyItemChanged(select)
                    iReverbCallback.cmd(list[pos].cmdFfmpeg, pos)
                }
            }
        }
    }
}
