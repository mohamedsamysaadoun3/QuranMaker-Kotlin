package hazem.nurmontage.videoquran.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.signature.ObjectKey
import hazem.nurmontage.videoquran.R

/**
 * RecyclerView adapter for displaying a horizontal list of background images.
 *
 * Each item is loaded via Glide with the app version as a cache signature,
 * ensuring images refresh on app updates. Stable IDs are derived from the
 * drawable resource integers so that RecyclerView can efficiently recycle
 * views when the data set changes.
 *
 * @property appVersion Version string used as Glide cache signature
 * @property images     List of drawable resource IDs to display
 * @property size       Override dimension (width = height = size) for Glide
 *
 * Converted from ImgAdapter.java (63 lines).
 */
class ImgAdapter(
    private val appVersion: String,
    private val images: List<Int>?,
    private val size: Int
) : RecyclerView.Adapter<ImgAdapter.ViewHolder>() {

    init {
        setHasStableIds(true)
    }

    // ── ViewHolder ─────────────────────────────────────────────────────

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.img)
    }

    // ── Adapter overrides ──────────────────────────────────────────────

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_img_bg, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Glide.with(holder.imageView)
            .load(images?.get(position))
            .override(size, size)
            .signature(ObjectKey(appVersion))
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .centerCrop()
            .into(holder.imageView)
    }

    override fun getItemCount(): Int = images?.size ?: 0

    override fun getItemId(position: Int): Long = images?.get(position)?.toLong() ?: 0L
}
