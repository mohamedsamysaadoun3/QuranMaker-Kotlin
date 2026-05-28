package hazem.nurmontage.videoquran.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.utils.DrawableHelper

/**
 * RecyclerView adapter for displaying Quran reader/reciter icon thumbnails
 * in a horizontal picker.
 *
 * Originally: IconQuranAdabters.java (95 lines)
 * Converted to: IconQuranAdabters.kt — idiomatic Kotlin, full logic preserved
 *
 * Each item is a 48x48dp circular icon representing a Quran reciter
 * (e.g., Hafs, Warsh, Shamerli, etc.). The icon image is resolved from
 * the drawable name string via [DrawableHelper.getIDDrawableIconByName].
 *
 * When an icon is selected:
 * - It gets a circular highlighted background (R.drawable.circle_item_menu_select)
 * - The [IIconQuranCallback.onIcon] is called with the icon's string identifier
 * - The previously selected icon reverts to the default background
 *   (R.drawable.circle_effect)
 *
 * Tapping the already-selected icon does nothing (no toggle behavior).
 * The adapter also supports an [unselect] method that clears the current
 * selection, used when the user removes a Quran overlay from the timeline.
 *
 * @see IIconQuranCallback
 * @see DrawableHelper
 */
class IconQuranAdabters(
    private val iconQuranCallback: IIconQuranCallback?,
    private val list: List<String>,
    selected: Int
) : RecyclerView.Adapter<IconQuranAdabters.ViewHolder>() {

    /** The currently selected position (-1 = none selected). */
    private var select: Int = if (selected < list.size) selected else 0

    // ──────────────────────────────────────────────────────────────────────
    // Inner interface
    // ──────────────────────────────────────────────────────────────────────

    /**
     * Callback interface for Quran icon selection events.
     *
     * The hosting Fragment/Activity implements this to apply the
     * selected Quran reader/reciter overlay to the video canvas.
     */
    interface IIconQuranCallback {
        /**
         * Called when a Quran icon is selected.
         * @param iconName The string identifier of the selected icon
         *                 (e.g., "hafes", "warach", "amiri")
         */
        fun onIcon(iconName: String)
    }

    // ──────────────────────────────────────────────────────────────────────
    // Public API
    // ──────────────────────────────────────────────────────────────────────

    /** Returns the currently selected position. */
    fun getSelect(): Int = select

    /** Returns whether any icon is currently selected. */
    fun isHaveSelect(): Boolean = select != -1

    /**
     * Clear the current selection without triggering a callback.
     *
     * Used when the user removes a Quran overlay from the timeline,
     * so the icon picker should reflect that no icon is active.
     */
    fun unselect() {
        if (select == -1) return
        val prevSelect = select
        select = -1
        notifyItemChanged(prevSelect)
    }

    // ──────────────────────────────────────────────────────────────────────
    // Adapter overrides
    // ──────────────────────────────────────────────────────────────────────

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.row_anim, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Resolve the drawable resource ID from the icon name string
        holder.animationItem.setImageResource(
            DrawableHelper.getIDDrawableIconByName(list[position])
        )

        if (position == select) {
            holder.animationItem.setBackgroundResource(R.drawable.circle_item_menu_select)
        } else {
            holder.animationItem.setBackgroundResource(R.drawable.circle_effect)
        }
    }

    override fun getItemCount(): Int = list.size

    // ──────────────────────────────────────────────────────────────────────
    // ViewHolder
    // ──────────────────────────────────────────────────────────────────────

    /**
     * ViewHolder for a single Quran icon thumbnail.
     *
     * Contains:
     * - [animationItem]: The icon ImageView with circular background
     * - [disableView]: Premium lock overlay (visibility controlled externally)
     *
     * On click, if the icon is not already selected, the selection moves
     * to this position and [IIconQuranCallback.onIcon] is called.
     */
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val animationItem: ImageView = itemView.findViewById(R.id.anim_item)
        val disableView: ImageView = itemView.findViewById(R.id.iv_disable)

        init {
            itemView.setOnClickListener {
                val pos = adapterPosition
                if (pos == RecyclerView.NO_POSITION) return@setOnClickListener

                // Ignore if callback is null or already selected
                if (iconQuranCallback == null || select == pos) return@setOnClickListener

                val prevSelect = select
                select = pos
                notifyItemChanged(prevSelect)
                notifyItemChanged(select)
                iconQuranCallback.onIcon(list[pos])
            }
        }
    }
}
