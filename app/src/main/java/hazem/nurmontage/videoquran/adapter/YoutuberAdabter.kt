package hazem.nurmontage.videoquran.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.signature.ObjectKey
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.model.YoutuberModel
import jp.wasabeef.glide.transformations.RoundedCornersTransformation

/**
 * RecyclerView adapter for displaying YouTuber / reciter profile thumbnails
 * in a horizontal list. Each item shows a rounded-corner thumbnail with a
 * play-overlay indicator and fires [IYoutuber.onClick] with the reciter's
 * link when tapped.
 *
 * @property iYoutuber  Callback for item click events
 * @property images     List of [YoutuberModel] items to display
 * @property appVersion Version string used as Glide cache signature
 * @property w          Override width for Glide
 * @property h          Override height for Glide
 *
 * Converted from YoutuberAdabter.java (73 lines).
 */
class YoutuberAdabter(
    private val iYoutuber: IYoutuber?,
    private val images: List<YoutuberModel>?,
    private val appVersion: String,
    private val w: Int,
    private val h: Int
) : RecyclerView.Adapter<YoutuberAdabter.ViewHolder>() {

    /**
     * Callback interface for YouTuber item click events.
     */
    interface IYoutuber {
        fun onClick(link: String)
    }

    // ── ViewHolder ─────────────────────────────────────────────────────

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.img)

        init {
            itemView.findViewById<View>(R.id.thumbnail_ytb).visibility = View.VISIBLE
            itemView.setOnClickListener {
                val pos = adapterPosition
                if (iYoutuber != null && pos != RecyclerView.NO_POSITION) {
                    images?.get(pos)?.let { item ->
                        iYoutuber.onClick(item.lnk)
                    }
                }
            }
        }
    }

    // ── Adapter overrides ──────────────────────────────────────────────

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_img_bg, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = images?.get(position) ?: return
        Glide.with(holder.imageView)
            .asBitmap()
            .load(item.img)
            .override(w, h)
            .signature(ObjectKey(appVersion))
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .transform(
                MultiTransformation(
                    RoundedCornersTransformation(8, 0, RoundedCornersTransformation.CornerType.ALL)
                )
            )
            .into(holder.imageView)
    }

    override fun getItemCount(): Int = images?.size ?: 0
}
