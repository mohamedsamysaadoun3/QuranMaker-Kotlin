package hazem.nurmontage.videoquran.adapter

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import hazem.nurmontage.videoquran.R

/**
 * RecyclerView adapter for displaying solid color swatches as background
 * color choices.
 *
 * Originally: ColorBgAdabter.java (93 lines)
 * Converted to: ColorBgAdabter.kt — idiomatic Kotlin, full logic preserved
 *
 * Each item in the grid is a small square swatch filled with a solid color
 * from the [colors] array. When selected, the swatch gets a white border
 * stroke (3px, -1 color) and the item container gets a rounded rectangle
 * background with the same stroke.
 *
 * The adapter supports an [enabled] flag that can be used to disable
 * selection temporarily (e.g., while a background operation is in progress).
 * When disabled, clicks are silently ignored.
 *
 * The [IColor] callback reports the selected color value (as an Int)
 * and its position back to the hosting Fragment/Activity.
 *
 * Color swatches are drawn using [GradientDrawable] with OVAL shape
 * and 10dp corner radius, creating rounded squares that fit the app's
 * visual style.
 *
 * @see IColor
 */
class ColorBgAdabter(
    private val iColorCallback: IColor?,
    private val colors: IntArray,
    selectedPos: Int
) : RecyclerView.Adapter<ColorBgAdabter.ViewHolder>() {

    /** The currently selected position. */
    private var posSelect: Int = selectedPos

    /** Whether selection clicks are enabled. */
    private var enabled: Boolean = true

    // ──────────────────────────────────────────────────────────────────────
    // Inner interface
    // ──────────────────────────────────────────────────────────────────────

    /**
     * Callback interface for color selection events.
     *
     * The hosting Fragment/Activity implements this to receive the
     * selected color value and its position in the color array.
     */
    interface IColor {
        /**
         * Called when a color swatch is selected.
         * @param color    The selected color value (ARGB int)
         * @param position The position of the selected color in the array
         */
        fun onColor(color: Int, position: Int)
    }

    // ──────────────────────────────────────────────────────────────────────
    // Public API
    // ──────────────────────────────────────────────────────────────────────

    /**
     * Enable or disable color selection.
     *
     * When disabled, taps on color swatches are silently ignored.
     * This is useful during background operations where changing the
     * color would cause visual glitches.
     *
     * @param enabled true to enable selection, false to disable
     */
    fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
    }

    /** Returns the currently selected position. */
    fun getPosSelect(): Int = posSelect

    // ──────────────────────────────────────────────────────────────────────
    // Adapter overrides
    // ──────────────────────────────────────────────────────────────────────

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.row_color, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        setGradientBackground(
            holder.imageView,
            holder.itemView,
            colors[position],
            position == posSelect
        )
    }

    override fun getItemCount(): Int = colors.size

    // ──────────────────────────────────────────────────────────────────────
    // Visual state
    // ──────────────────────────────────────────────────────────────────────

    /**
     * Apply the color swatch and selection border to the views.
     *
     * The inner [ImageView] is filled with the solid color as a
     * rounded rectangle (10dp corner radius). If the item is selected,
     * the outer container also gets a rounded rectangle background
     * with a 3px white stroke border.
     *
     * @param view      The inner ImageView to fill with color
     * @param containerView The outer container for the selection border
     * @param color     The ARGB color value
     * @param isSelected Whether this item is currently selected
     */
    private fun setGradientBackground(
        view: ImageView,
        containerView: View,
        color: Int,
        isSelected: Boolean
    ) {
        // Selection border on the container
        if (isSelected) {
            val borderDrawable = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                cornerRadius = 10f
                setStroke(3, -1) // 3px white border
            }
            containerView.background = borderDrawable
        } else {
            containerView.background = null
        }

        // Color fill on the inner ImageView
        val colorDrawable = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            cornerRadius = 10f
            setColor(color)
        }
        view.background = colorDrawable
    }

    // ──────────────────────────────────────────────────────────────────────
    // ViewHolder
    // ──────────────────────────────────────────────────────────────────────

    /**
     * ViewHolder for a single color swatch.
     *
     * On click, if selection is [enabled] and the item is not already
     * selected, the selection moves to this position and [IColor.onColor]
     * is called with the new color value.
     */
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val imageView: ImageView = itemView.findViewById(R.id.image)

        init {
            itemView.setOnClickListener {
                val pos = adapterPosition
                if (pos == RecyclerView.NO_POSITION) return@setOnClickListener

                // Guard: ignore if callback is null, already selected, or disabled
                if (iColorCallback == null || posSelect == pos || !enabled) return@setOnClickListener

                val prevSelected = posSelect
                posSelect = pos
                notifyItemChanged(prevSelected)
                notifyItemChanged(posSelect)
                iColorCallback.onColor(colors[pos], pos)
            }
        }
    }
}
