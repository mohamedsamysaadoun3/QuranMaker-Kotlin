package hazem.nurmontage.videoquran.adapter

import android.content.Context
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.signature.ObjectKey
import hazem.nurmontage.videoquran.R

/**
 * RecyclerView Adapter for displaying the About/Pro version content list.
 *
 * Originally: AboutAdabters.java (preserved typo in original package)
 * Converted to: AboutAdabters.kt — idiomatic Kotlin, full logic preserved
 *
 * Features:
 * - Displays a mix of text and image rows for the About/Pro screen
 * - Each row can contain HTML-formatted text rendered via [HtmlCompat]
 * - Text gravity, size, and bold state are configurable per row
 * - Optional image loaded with Glide (version-based cache signature)
 * - Images are overridden to specific dimensions ([mDimensionW] x [mDimensionH])
 * - When no image is set for a row, the ImageView is hidden and Glide is cleared
 * - The inner [ModelAbout] class defines the data model for each row
 *
 * @property mContext Context for resource access and Glide loading
 * @property APP_VERSION Version string used as Glide cache signature
 * @property mModelAboutList List of about content items
 * @property mDimensionW Override width for images
 * @property mDimensionH Override height for images
 */
class AboutAdabters(
    private val mContext: Context,
    private val APP_VERSION: String,
    private val mModelAboutList: List<ModelAbout>?,
    private val mDimensionW: Int,
    private val mDimensionH: Int
) : RecyclerView.Adapter<AboutAdabters.ViewHolder>() {

    /**
     * Data model for a single row in the About/Pro content list.
     *
     * Each item defines:
     * - [text]: A Pair of (HTML string, gravity constant) for the text content
     * - [image_1]: Drawable resource ID for the primary image, or -1 if none
     * - [image_2]: Drawable resource ID for a secondary image, or -1 if none (reserved)
     * - [sizeText]: Text size in SP (default 16)
     *
     * The [sizeText] value 19 is used as a signal for bold text (matching the
     * original app's convention of using size 19 for section headers).
     *
     * Multiple constructors are provided to support various combinations
     * of text, image, and size parameters — matching the original Java class.
     */
    class ModelAbout(
        val text: Pair<String, Int>,
        val image_1: Int = -1,
        val image_2: Int = -1,
        val sizeText: Int = 16
    ) {
        /** Convenience: text + primary image. */
        constructor(text: Pair<String, Int>, image_1: Int) : this(text, image_1, -1, 16)

        /** Convenience: size + text + primary image. */
        constructor(sizeText: Int, text: Pair<String, Int>, image_1: Int) : this(text, image_1, -1, sizeText)

        /** Convenience: two images + text (size defaults to 16). */
        constructor(image_1: Int, image_2: Int, text: Pair<String, Int>) : this(text, image_1, image_2, 16)

        /** Convenience: text + primary image + text size. */
        constructor(text: Pair<String, Int>, image_1: Int, sizeText: Int) : this(text, image_1, -1, sizeText)

        /** Convenience: size + text only (no images). */
        constructor(sizeText: Int, text: Pair<String, Int>) : this(text, -1, -1, sizeText)

        /** Returns the HTML text string from the pair. */
        fun getText(): String = text.first

        /** Returns the gravity constant from the pair. */
        fun geGravity(): Int = text.second
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_billing, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val modelAbout = mModelAboutList?.get(position) ?: return

        // Apply text gravity from the model
        holder.textView.gravity = modelAbout.geGravity()

        // Size 19 is the original app's convention for bold/header text
        if (modelAbout.sizeText == 19) {
            holder.textView.paint.isFakeBoldText = true
        } else {
            holder.textView.paint.isFakeBoldText = false
        }

        // Set text size in SP (TypedValue.COMPLEX_UNIT_SP = 2)
        holder.textView.setTextSize(2, modelAbout.sizeText.toFloat())

        // Render HTML content (supports bold, links, colors, etc.)
        holder.textView.text = HtmlCompat.fromHtml(modelAbout.getText(), 0)

        // Load or hide the image based on the model
        if (modelAbout.image_1 != -1) {
            holder.imageView1.visibility = View.VISIBLE
            Glide.with(mContext)
                .asBitmap()
                .load(modelAbout.image_1)
                .override(mDimensionW, mDimensionH)
                .centerInside()
                .signature(ObjectKey(APP_VERSION))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(holder.imageView1)
        } else {
            holder.imageView1.visibility = View.GONE
            Glide.with(mContext).clear(holder.imageView1)
        }
    }

    override fun getItemCount(): Int = mModelAboutList?.size ?: 0

    /**
     * ViewHolder for About content items.
     * Contains a text view and an optional image view.
     */
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.tv)
        val imageView1: ImageView = itemView.findViewById(R.id.img)
    }
}
