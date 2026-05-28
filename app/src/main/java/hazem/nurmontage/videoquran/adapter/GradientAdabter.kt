package hazem.nurmontage.videoquran.adapter

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.model.Gradient

/**
 * RecyclerView adapter for displaying gradient color presets as background
 * choices.
 *
 * Originally: GradientAdabter.java (119 lines)
 * Converted to: GradientAdabter.kt — idiomatic Kotlin, full logic preserved
 *
 * Each item displays a circular swatch filled with a 3-color gradient
 * defined by a [Gradient] model (primary, secondary, tertiary colors).
 * The gradient is drawn using [GradientDrawable] with a fully rounded
 * shape (100dp corner radius → circle).
 *
 * **Subscription gating**: If the user is not subscribed ([isSubscribe] = false),
 * only the first 2 gradient presets are selectable. Items beyond index 1
 * show a crown icon overlay (R.drawable.crown_24px via `R.id.layer`)
 * to indicate premium content. Tapping a locked item is silently ignored.
 *
 * When a gradient is selected:
 * 1. The previous selection border is removed
 * 2. The new selection gets a white stroke border (3px)
 * 3. [IColor.onGradient] is called with the selected [Gradient] and position
 *
 * The adapter also provides a utility method [setGradientBackground]
 * (single-color variant) that creates a solid-color circle, used by
 * the ViewHolder to set the layer icon background.
 *
 * @see IColor
 * @see Gradient
 */
class GradientAdabter(
    private val iColorCallback: IColor?,
    private val colors: List<Gradient>,
    private val isSubscribe: Boolean,
    selectedPos: Int
) : RecyclerView.Adapter<GradientAdabter.ViewHolder>() {

    /** The currently selected position. */
    private var posSelect: Int = selectedPos

    /** Maximum number of free gradient presets (index 0 and 1). */
    private val maxFree: Int = 1

    // ──────────────────────────────────────────────────────────────────────
    // Inner interface
    // ──────────────────────────────────────────────────────────────────────

    /**
     * Callback interface for gradient selection events.
     *
     * Unlike [ColorBgAdabter.IColor] which reports a raw color int,
     * this callback provides the full [Gradient] object so the
     * hosting Fragment can extract all three color stops and the angle.
     */
    interface IColor {
        /**
         * Called when a gradient preset is selected.
         * @param gradient The selected gradient preset
         * @param position The adapter position of the selection
         */
        fun onGradient(gradient: Gradient, position: Int)
    }

    // ──────────────────────────────────────────────────────────────────────
    // Public API
    // ──────────────────────────────────────────────────────────────────────

    /** Returns the currently selected [Gradient], or null if none selected. */
    fun getSelect(): Gradient? {
        return if (posSelect >= 0 && posSelect < colors.size) colors[posSelect] else null
    }

    /** Returns the currently selected position. */
    fun getPosSelect(): Int = posSelect

    /**
     * Apply a gradient background to the given view using the three
     * color stops from a [Gradient] model.
     *
     * If [isSelected] is true, a white stroke border is applied to
     * the [containerView]. The gradient is drawn as a fully rounded
     * shape (100dp corner radius → circle).
     *
     * @param view           The inner ImageView for the gradient fill
     * @param containerView  The outer container for the selection border
     * @param gradient       The gradient model with 3 color stops
     * @param isSelected     Whether this item is currently selected
     */
    fun setGradientBackground(
        view: ImageView,
        containerView: View,
        gradient: Gradient,
        isSelected: Boolean
    ) {
        // Selection border
        if (isSelected) {
            val borderDrawable = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                cornerRadius = 100f
                setStroke(3, -1) // 3px white border
            }
            containerView.background = borderDrawable
        } else {
            containerView.background = null
        }

        // Gradient fill
        val gradientDrawable = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            cornerRadius = 100f
            colors = intArrayOf(gradient.color, gradient.second, gradient.three)
        }
        view.background = gradientDrawable
    }

    /**
     * Apply a solid color circular background to the given view.
     *
     * Used internally to set the lock icon overlay background.
     *
     * @param view  The view to apply the background to
     * @param color The solid color
     */
    fun setGradientBackground(view: View, color: Int) {
        val drawable = GradientDrawable().apply {
            setColor(color)
            shape = GradientDrawable.OVAL
            cornerRadius = 100f
        }
        view.background = drawable
    }

    // ──────────────────────────────────────────────────────────────────────
    // Adapter overrides
    // ──────────────────────────────────────────────────────────────────────

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.row_color, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        setGradientBackground(holder.imageView, holder.itemView, colors[position], position == posSelect)

        // Show lock icon for non-subscribers beyond the free tier
        if (!isSubscribe && position > maxFree) {
            holder.imageLayer.visibility = View.VISIBLE
        } else {
            holder.imageLayer.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = colors.size

    // ──────────────────────────────────────────────────────────────────────
    // ViewHolder
    // ──────────────────────────────────────────────────────────────────────

    /**
     * ViewHolder for a single gradient swatch.
     *
     * Contains two ImageViews:
     * - [imageView]: The gradient fill swatch (R.id.image)
     * - [imageLayer]: The lock/crown overlay for premium items (R.id.layer)
     *
     * On click, if the user is subscribed or the item is within the
     * free tier (position <= 1), and the item is not already selected,
     * the selection moves and [IColor.onGradient] is called.
     */
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val imageView: ImageView = itemView.findViewById(R.id.image)
        val imageLayer: ImageView = itemView.findViewById(R.id.layer)

        init {
            // Set a semi-transparent dark background on the lock icon overlay
            setGradientBackground(imageLayer, -1895825408) // 0x8F000000

            itemView.setOnClickListener {
                val pos = adapterPosition
                if (pos == RecyclerView.NO_POSITION) return@setOnClickListener

                if (iColorCallback == null) return@setOnClickListener

                // Subscription gate: non-subscribers can only select positions 0-1
                val canSelect = isSubscribe || pos <= maxFree
                if (!canSelect || posSelect == pos) return@setOnClickListener

                val prevSelected = posSelect
                posSelect = pos
                notifyItemChanged(prevSelected)
                notifyItemChanged(posSelect)
                iColorCallback.onGradient(colors[pos], pos)
            }
        }
    }
}
