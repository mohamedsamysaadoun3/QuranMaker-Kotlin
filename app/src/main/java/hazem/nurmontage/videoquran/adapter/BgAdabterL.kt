package hazem.nurmontage.videoquran.adapter

import android.graphics.Bitmap
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
 * RecyclerView adapter for displaying landscape (long) background thumbnails.
 *
 * Originally: BgAdabterL.java (91 lines)
 * Converted to: BgAdabterL.kt — idiomatic Kotlin, full logic preserved
 *
 * This is the landscape/portrait counterpart of [BgAdapter]. The key
 * differences from [BgAdapter] are:
 * 1. Loads images as [Bitmap] (asBitmap) instead of [Drawable] — this
 *    allows the hosting Fragment to extract the bitmap for canvas rendering
 * 2. Uses slightly different corner radius (8 vs 10) and no margin
 * 3. Does NOT use stable IDs
 * 4. Does NOT apply alpha/opacity changes on selection — the visual
 *    selection state is managed entirely by the hosting Fragment
 * 5. Always calls [IBgCallback.onAdd] on click without re-selection logic
 *
 * The "L" suffix in the original name likely stands for "Landscape" or
 * "Long", indicating these backgrounds are used in a different aspect
 * ratio context (e.g., portrait video backgrounds vs. landscape).
 *
 * @see IBgCallback
 * @see BgItem
 * @see BgAdapter
 */
class BgAdabterL(
    private val appVersion: String,
    private val iBgCallback: IBgCallback?,
    private val images: List<BgItem>,
    private val size: Int
) : RecyclerView.Adapter<BgAdabterL.ViewHolder>() {

    /** The currently selected position. */
    private var selected: Int = 0

    // ──────────────────────────────────────────────────────────────────────
    // Public API
    // ──────────────────────────────────────────────────────────────────────

    /**
     * Add a new background item to the end of the list.
     *
     * In the original Java source, this is wrapped in a try-catch to
     * handle potential IndexOutOfBoundsException when the adapter is
     * accessed from multiple threads. The Kotlin version preserves
     * this safety guard.
     *
     * @param bgItem The background item to add
     */
    fun add(bgItem: BgItem) {
        try {
            (images as? MutableList)?.add(bgItem)
            notifyItemInserted(images.size)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /** Returns the currently selected position. */
    fun getPosSelect(): Int = selected

    // ──────────────────────────────────────────────────────────────────────
    // Adapter overrides
    // ──────────────────────────────────────────────────────────────────────

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.row_img_bg, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Glide.with(holder.imageView)
            .asBitmap()
            .load(images[position].id)
            .override(size, size)
            .signature(ObjectKey(appVersion))
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .transform(
                MultiTransformation(
                    CenterCrop(),
                    RoundedCornersTransformation(8, 0, RoundedCornersTransformation.CornerType.ALL)
                )
            )
            .into(holder.imageView)
    }

    override fun getItemCount(): Int = images.size

    // ──────────────────────────────────────────────────────────────────────
    // ViewHolder
    // ──────────────────────────────────────────────────────────────────────

    /**
     * ViewHolder for a single landscape background thumbnail.
     *
     * On click, updates the selection position and calls [IBgCallback.onAdd].
     * Unlike [BgAdapter.ViewHolder], this always calls onAdd without
     * checking for re-selection — the Fragment handles visual state.
     */
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val imageView: ImageView = itemView.findViewById(R.id.img)

        init {
            itemView.setOnClickListener {
                val pos = adapterPosition
                if (pos == RecyclerView.NO_POSITION) return@setOnClickListener

                if (iBgCallback != null) {
                    selected = pos
                    iBgCallback.onAdd(images[pos])
                }
            }
        }
    }
}
