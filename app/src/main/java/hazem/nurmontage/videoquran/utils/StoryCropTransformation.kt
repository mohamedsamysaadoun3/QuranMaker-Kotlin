package hazem.nurmontage.videoquran.utils

import android.content.Context
import android.graphics.Bitmap
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapResource
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

/**
 * Glide [Transformation] that crops a bitmap to a target aspect ratio
 * (specified by [targetWidth] x [targetHeight]) with center-crop behavior.
 *
 * This is used for Story-format previews (9:16 portrait) and other
 * aspect-ratio-constrained image displays throughout the app.
 *
 * The crop algorithm:
 * - If the source is **wider** than the target ratio -> crop the sides (center-crop horizontally)
 * - If the source is **taller** than the target ratio -> crop top/bottom (center-crop vertically)
 * - If the source **matches** the target ratio -> return the original bitmap unchanged
 *
 * Usage with Glide:
 * ```kotlin
 * Glide.with(context)
 *     .asBitmap()
 *     .load(imageSource)
 *     .apply(RequestOptions.bitmapTransform(StoryCropTransformation(1080, 1920)))
 *     .override(1080, 1920)
 *     .submit()
 *     .get()
 * ```
 *
 * Originally an inner class of ImageLoader.java — extracted as a standalone class
 * for reusability. The empty `StoryCropTransformation` interface at the Utils level
 * was a JADX artifact (the smali confirms it's just an empty marker).
 *
 * Converted from ImageLoader.StoryCropTransformation — logic preserved exactly.
 */
class StoryCropTransformation(
    private val targetWidth: Int,
    private val targetHeight: Int
) : Transformation<Bitmap> {

    companion object {
        /** Cache key pattern for Glide's disk cache key. */
        private const val CACHE_KEY_TEMPLATE = "storyCrop(targetWidth=%d, targetHeight=%d)"
    }

    /**
     * Glide's modern transform method (3-parameter variant).
     *
     * Returns null to delegate to the 4-parameter [transform] method,
     * which contains the actual cropping logic. This follows Glide's
     * recommended pattern for backward compatibility.
     *
     * Note: Some Glide versions call the 4-parameter overload directly.
     */
    override fun transform(context: Context, resource: Resource<Bitmap>, outWidth: Int, outHeight: Int): Resource<Bitmap>? {
        return null
    }

    /**
     * Crop the bitmap to the target aspect ratio with center-crop behavior.
     *
     * Algorithm:
     * 1. Calculate the source and target aspect ratios
     * 2. If source is wider -> crop the left/right sides (keep full height)
     * 3. If source is taller -> crop the top/bottom (keep full width)
     * 4. If matching -> return the original resource unchanged
     *
     * The resulting bitmap is obtained via [BitmapResource.obtain], which
     * allows Glide's [BitmapPool] to recycle it when no longer needed.
     *
     * @param pool      The bitmap pool for recycling
     * @param resource  The source bitmap resource
     * @param outWidth  The target output width (unused -- targetWidth is used instead)
     * @param outHeight The target output height (unused -- targetHeight is used instead)
     * @return A new [Resource] containing the cropped bitmap, or the original if no crop needed
     */
    fun transform(pool: BitmapPool, resource: Resource<Bitmap>, outWidth: Int, outHeight: Int): Resource<Bitmap> {
        val bitmap = resource.get()
        val width = bitmap.width
        val height = bitmap.height

        val sourceRatio = width.toFloat() / height.toFloat()
        val targetRatio = targetWidth.toFloat() / targetHeight.toFloat()

        val croppedBitmap: Bitmap = when {
            sourceRatio > targetRatio -> {
                // Source is wider than target -> crop sides
                val newWidth = (height.toFloat() * targetRatio).toInt()
                Bitmap.createBitmap(bitmap, (width - newWidth) / 2, 0, newWidth, height)
            }
            sourceRatio < targetRatio -> {
                // Source is taller than target -> crop top/bottom
                val newHeight = (width.toFloat() / targetRatio).toInt()
                Bitmap.createBitmap(bitmap, 0, (height - newHeight) / 2, width, newHeight)
            }
            else -> {
                // Already matches target ratio
                return resource
            }
        }

        return BitmapResource.obtain(croppedBitmap, pool)
    }

    /**
     * Update the disk cache key with this transformation's identity.
     *
     * The key includes the target dimensions so that different crop
     * ratios produce different cache entries.
     *
     * @param messageDigest The message digest to update
     */
    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update(
            String.format(CACHE_KEY_TEMPLATE, targetWidth, targetHeight)
                .toByteArray(StandardCharsets.UTF_8)
        )
    }

    /**
     * Equality based on target dimensions — two transformations with the same
     * target width and height are considered equal for Glide's cache deduplication.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is StoryCropTransformation) return false
        return targetWidth == other.targetWidth && targetHeight == other.targetHeight
    }

    override fun hashCode(): Int {
        return 31 * targetWidth + targetHeight
    }
}
