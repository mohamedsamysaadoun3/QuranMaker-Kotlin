package hazem.nurmontage.videoquran.adapter

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.signature.ObjectKey
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.model.BgItem
import jp.wasabeef.glide.transformations.RoundedCornersTransformation

/**
 * RecyclerView adapter for displaying background image thumbnails in a
 * horizontal scrolling picker.
 *
 * Originally: BgAdapter.java (140 lines)
 * Converted to: BgAdapter.kt — idiomatic Kotlin, full logic preserved
 *
 * This adapter displays a list of [BgItem] backgrounds as rounded-corner
 * thumbnails in a horizontal LinearLayoutManager. It supports single-selection
 * with visual feedback:
 * - **Selected**: Full opacity (1.0f) with a highlighted border
 *   (R.drawable.ipad_selected)
 * - **Unselected**: Reduced opacity (0.65f) with no border
 *
 * The adapter uses [setHasStableIds] for efficient RecyclerView updates,
 * with [BgItem.id] as the stable identifier.
 *
 * When a background is tapped:
 * 1. If the same item is tapped again, [IBgCallback.onAdd] is called
 *    without changing the selection (allows re-applying the same bg)
 * 2. If a different item is tapped, the selection moves with animated
 *    partial-bind updates (payload "alpha") and [IBgCallback.onAdd]
 *    is called with the new [BgItem]
 *
 * The images are loaded from drawable resources via [BgItem.id] using Glide
 * with CenterCrop + RoundedCornersTransformation for a polished look.
 *
 * @see IBgCallback
 * @see BgItem
 */
class BgAdapter(
    private val appVersion: String,
    private val iBgCallback: IBgCallback?,
    private val images: List<BgItem>,
    private val size: Int,
    selected: Int
) : RecyclerView.Adapter<BgAdapter.ViewHolder>() {

    /** The currently selected position (-1 = none selected). */
    private var selected: Int = selected

    init {
        setHasStableIds(true)
    }

    // ──────────────────────────────────────────────────────────────────────
    // Public API
    // ──────────────────────────────────────────────────────────────────────

    /**
     * Add a new background item to the end of the list.
     *
     * @param bgItem The background item to add
     */
    fun add(bgItem: BgItem) {
        val insertPos = images.size
        (images as? MutableList)?.add(bgItem)
        notifyItemInserted(insertPos)
    }

    /** Returns the adapter position of the currently selected item. */
    fun getSelectedPosition(): Int = selected

    // ──────────────────────────────────────────────────────────────────────
    // Adapter overrides
    // ──────────────────────────────────────────────────────────────────────

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.row_img_bg, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        applyState(holder, position)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isNotEmpty()) {
            // Partial bind: only update the selection visual state
            applyState(holder, position)
        } else {
            // Full bind: load image + apply state
            Glide.with(holder.imageView)
                .load(images[position].id)
                .override(size, size)
                .signature(ObjectKey(appVersion))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .transform(
                    MultiTransformation(
                        CenterCrop(),
                        RoundedCornersTransformation(10, 8)
                    )
                )
                .into(holder.imageView)
            applyState(holder, position)
        }
    }

    override fun getItemCount(): Int = images.size

    override fun getItemId(position: Int): Long = images[position].id.toLong()

    // ──────────────────────────────────────────────────────────────────────
    // Selection state
    // ──────────────────────────────────────────────────────────────────────

    /**
     * Apply the visual selection state to a ViewHolder.
     *
     * Selected items have full opacity (1.0f) and a highlighted border
     * background. Unselected items have reduced opacity (0.65f) and a
     * transparent background.
     *
     * @param holder   The ViewHolder to update
     * @param position The adapter position
     */
    private fun applyState(holder: ViewHolder, position: Int) {
        val isSelected = position == selected
        holder.imageView.alpha = if (isSelected) 1.0f else 0.65f

        if (isSelected) {
            holder.itemView.setBackgroundResource(R.drawable.ipad_selected)
        } else {
            holder.itemView.setBackgroundColor(0)
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    // ViewHolder
    // ──────────────────────────────────────────────────────────────────────

    /**
     * ViewHolder for a single background thumbnail.
     *
     * Clicking selects this background:
     * - If already selected, calls [IBgCallback.onAdd] (re-apply)
     * - If different, updates selection with partial-bind animations
     *   and calls [IBgCallback.onAdd] with the new item
     */
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val imageView: ImageView = itemView.findViewById(R.id.img)

        init {
            itemView.setOnClickListener {
                val pos = adapterPosition
                if (pos == RecyclerView.NO_POSITION) return@setOnClickListener

                if (pos == selected) {
                    // Tapped the already-selected item: re-apply it
                    iBgCallback?.onAdd(images[pos])
                    return@setOnClickListener
                }

                // Selection changed: update with partial-bind for smooth animation
                val prevSelected = selected
                selected = pos

                if (prevSelected != -1) {
                    notifyItemChanged(prevSelected, "alpha")
                }
                notifyItemChanged(selected, "alpha")

                iBgCallback?.onAdd(images[pos])
            }
        }
    }
}
