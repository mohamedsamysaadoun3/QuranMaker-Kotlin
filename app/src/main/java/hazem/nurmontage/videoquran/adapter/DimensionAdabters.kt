package hazem.nurmontage.videoquran.adapter

import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.constant.ResizeType
import hazem.nurmontage.videoquran.model.ItemDimension
import hazem.nurmontage.videoquran.views.text.TextCustumFont

/**
 * RecyclerView Adapter for displaying and selecting video/image dimension presets.
 *
 * Originally: DimensionAdabters.java (preserved typo in original package)
 * Converted to: DimensionAdabters.kt — idiomatic Kotlin, full logic preserved
 *
 * Features:
 * - Displays aspect ratio presets (TikTok 9:16, YouTube 16:9, Square, etc.)
 * - Each item shows an icon thumbnail, name, and dimension text
 * - Item width/height are controlled programmatically via [listDim] pairs
 * - Single-selection mode with visual highlight (rect_btn_select vs rect_btn)
 * - Callback reports the selected dimension with ResizeType, width, height, id, and image
 * - Supports programmatic selection via [setSelected]
 * - Provides [getResizeSelected] and [get] for querying current selection
 * - Safe cleanup via [clear]
 *
 * @property mDimensionList List of dimension preset data models
 * @property mIDimensionCallback Callback interface for dimension selection events
 * @property listDim List of (width, height) pairs that control each item's layout size
 * @property selected Currently selected dimension position
 */
class DimensionAdabters(
    private var mDimensionList: List<ItemDimension>?,
    private var mIDimensionCallback: IDimensionCallback?,
    private val listDim: List<Pair<Int, Int>>,
    private var selected: Int = 0
) : RecyclerView.Adapter<DimensionAdabters.ViewHolder>() {

    /** Callback interface for dimension preset selection events. */
    interface IDimensionCallback {
        /** Called when the selection process is complete. */
        fun done()

        /**
         * Called when a custom or preset size is selected.
         * @param isCustom Whether the size is a custom user-defined size
         * @param resizeType The [ResizeType] of the selected dimension
         */
        fun isCustomSize(isCustom: Boolean, resizeType: ResizeType)

        /**
         * Called when a specific dimension preset is selected.
         * @param w Width in pixels
         * @param h Height in pixels
         * @param resizeTypeOrdinal The ordinal of the [ResizeType] enum
         * @param id Short identifier string (e.g., "t" for TikTok)
         * @param image Drawable resource ID for the preset icon
         */
        fun onCustumSize(w: Int, h: Int, resizeTypeOrdinal: Int, id: String, image: Int)
    }

    /** Programmatically sets the selected position without triggering a callback. */
    fun setSelected(position: Int) {
        selected = position
    }

    /** Returns the currently selected position index. */
    fun getSelected(): Int = selected

    /**
     * Returns the ordinal of the [ResizeType] for the currently selected dimension.
     * Used by the hosting Fragment/Activity to determine the current canvas mode.
     */
    fun get(): Int = mDimensionList?.get(getSelected())?.resizeType?.ordinal
        ?: ResizeType.SQUARE.ordinal

    /**
     * Returns the [ResizeType] enum value for the currently selected dimension preset.
     * Falls back to [ResizeType.SQUARE] if no dimension list is available.
     */
    fun getResizeSelected(): ResizeType =
        mDimensionList?.get(getSelected())?.resizeType ?: ResizeType.SQUARE

    /**
     * Replaces the dimension list with a new one.
     * Used when switching between different dimension categories.
     *
     * @param list The new list of dimension presets
     */
    fun update(list: List<ItemDimension>) {
        mDimensionList?.let { (it as? MutableList)?.clear() }
        mDimensionList = list
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_aspect, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dimension = mDimensionList?.get(position) ?: return
        val pair = listDim[position]

        // Programmatically set the item layout width/height from the pair list
        holder.layout.layoutParams.width = pair.first
        holder.layout.layoutParams.height = pair.second

        // Split name by newline — first line is the preset name, second is dimensions
        val parts = dimension.name.split("\n")
        holder.name.text = parts[0]
        if (parts.size > 1) {
            holder.dimension.text = parts[1]
        }

        // Load the dimension preset icon with Glide
        Glide.with(holder.itemView)
            .asBitmap()
            .centerInside()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .load(dimension.image)
            .into(holder.imageView)

        // Apply selection state visual feedback
        if (position == selected) {
            holder.layout.setBackgroundResource(R.drawable.rect_btn_select)
        } else {
            holder.layout.setBackgroundResource(R.drawable.rect_btn)
        }
    }

    override fun getItemCount(): Int = mDimensionList?.size ?: 0

    /**
     * Clears the dimension list and callback reference to prevent memory leaks.
     * Should be called when the hosting Fragment/Activity is destroyed.
     */
    fun clear() {
        mDimensionList?.let { (it as? MutableList)?.clear() }
        mDimensionList = null
        mIDimensionCallback = null
    }

    /**
     * ViewHolder for dimension preset items.
     * Handles click events for dimension selection with callback notification.
     */
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val layout: FrameLayout = itemView.findViewById(R.id.layout)
        val imageView: ImageView = itemView.findViewById(R.id.icon)
        val name: TextCustumFont = itemView.findViewById(R.id.aspect_name)
        val dimension: TextCustumFont = itemView.findViewById(R.id.aspect_size)

        init {
            itemView.setOnClickListener {
                val callback = mIDimensionCallback ?: return@setOnClickListener
                val pos = adapterPosition
                if (pos == -1) return@setOnClickListener

                val oldSelected = selected
                selected = pos
                notifyItemChanged(oldSelected)
                notifyItemChanged(selected)

                mDimensionList?.get(pos)?.let { item ->
                    callback.onCustumSize(
                        item.w,
                        item.h,
                        item.resizeType.ordinal,
                        item.id,
                        item.image
                    )
                }
            }
        }
    }
}
