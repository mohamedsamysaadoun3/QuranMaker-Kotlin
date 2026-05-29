package hazem.nurmontage.videoquran.utils.waveform

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF

/**
 * Pre-renders an audio waveform into a [Bitmap] and draws it on a [Canvas]
 * with horizontal scaling and translation for smooth timeline scrolling.
 *
 * Also supports drawing a highlight overlay on a portion of the waveform
 * (e.g., the played portion of the audio clip) via [drawOverlay].
 *
 * Rendering pipeline:
 * 1. **Constructor** receives the amplitude array and allocates a bitmap.
 * 2. [generateBitmap] draws vertical lines proportional to each amplitude value.
 *    The peak amplitude is normalized so the tallest bar fills 85% of the height.
 * 3. [draw] renders the bitmap onto the given canvas using a [Matrix] that applies
 *    horizontal scaling (zoom) and translation (scroll offset).
 * 4. [drawOverlay] renders a colored highlight overlay on the played portion
 *    of the waveform, from the start of the rect up to the position [f].
 * 5. [release] recycles the bitmap to free native memory.
 *
 * **Performance note**: The bitmap is generated once in the constructor and
 * reused for every frame. Only [setColor] triggers regeneration.
 *
 * Converted from WaveformBitmapRenderer.java — logic preserved exactly,
 * with drawOverlay implemented for real-time playback highlighting.
 */
class WaveformBitmapRenderer(
    private val amps: FloatArray?,
    bitmapWidth: Int,
    bitmapHeight: Int,
    waveColor: Int
) {
    private val bitmapWidth: Int = bitmapWidth
    private val bitmapHeight: Int = bitmapHeight
    private val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = waveColor
        alpha = 100
    }
    private var waveformBitmap: Bitmap? = null

    init {
        generateBitmap()
    }

    /**
     * Generate the waveform bitmap by drawing one vertical line per pixel column.
     *
     * Each column maps to an amplitude sample via linear interpolation.
     * The maximum amplitude in the array is used as the normalization factor
     * so that the tallest bar fills 85% of the bitmap height.
     *
     * Lines are drawn **bottom-up** from `(x, bitmapHeight)` to `(x, bitmapHeight - barHeight)`.
     */
    private fun generateBitmap() {
        val amplitudes = amps ?: return
        if (amplitudes.isEmpty()) return

        waveformBitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(waveformBitmap!!)

        val usableHeight = bitmapHeight * 0.85f

        // Find the peak amplitude for normalization
        var peak = 0f
        for (amp in amplitudes) {
            if (amp > peak) peak = amp
        }
        // Prevent division by zero — minimum normalization factor
        val normalizer = if (peak < 0.01f) 0.01f else peak

        // Draw one vertical line per pixel column
        for (x in 0 until bitmapWidth) {
            // Map pixel position to amplitude array index
            var index = (x.toFloat() / bitmapWidth * amplitudes.size).toInt()
            if (index >= amplitudes.size) {
                index = amplitudes.size - 1
            }
            val barHeight = (amplitudes[index] / normalizer) * usableHeight
            canvas.drawLine(x.toFloat(), bitmapHeight.toFloat(), x.toFloat(), bitmapHeight - barHeight, paint)
        }
    }

    /**
     * Draw the pre-rendered waveform bitmap onto the given canvas.
     *
     * The bitmap is scaled horizontally by [scaleFactor] and translated
     * so that it scrolls correctly with the timeline viewport.
     *
     * @param canvas    The target canvas
     * @param rect      The bounding rectangle of the audio entity on the timeline
     * @param scaleFactor Horizontal zoom factor (mScaleFactor + scaleEffect)
     * @param offset    Horizontal scroll offset (offset + offsetLeft + tmpOffset)
     */
    fun draw(canvas: Canvas, rect: RectF, scaleFactor: Float, offset: Float) {
        val bitmap = waveformBitmap ?: return
        val translateX = rect.left - (offset * scaleFactor)
        val matrix = Matrix().apply {
            postScale(scaleFactor, 1.0f)
            postTranslate(translateX, rect.top)
        }
        canvas.drawBitmap(bitmap, matrix, null)
    }

    /**
     * Draw a highlight overlay on the waveform to indicate the current
     * playback position.
     *
     * Renders vertical bars using the provided [paint] for the highlighted
     * portion of the waveform — from the start of [rect] up to progress
     * position [f] (0.0–1.0). This provides visual feedback during audio
     * playback where the played portion appears brighter while the unplayed
     * portion remains dimmed.
     *
     * @param canvas  The target canvas
     * @param rect    The bounding rectangle of the audio entity on the timeline
     * @param f       Playback progress as a fraction (0.0 to 1.0)
     * @param f2      Horizontal scroll offset (same as offset in [draw])
     * @param paint   The paint to use for the overlay bars (typically brighter color)
     */
    fun drawOverlay(canvas: Canvas, rect: RectF, f: Float, f2: Float, paint: Paint) {
        val amplitudes = amps ?: return
        if (amplitudes.isEmpty()) return

        val progress = f.coerceIn(0f, 1f)
        val barCount = (bitmapWidth * progress).toInt().coerceIn(0, bitmapWidth)
        if (barCount <= 0) return

        var peak = 0f
        for (amp in amplitudes) {
            if (amp > peak) peak = amp
        }
        val normalizer = if (peak < 0.01f) 0.01f else peak
        val usableHeight = rect.height() * 0.85f

        val scaleX = rect.width() / bitmapWidth
        val translateX = rect.left - (f2 * scaleX)

        for (x in 0 until barCount) {
            var index = (x.toFloat() / bitmapWidth * amplitudes.size).toInt()
            if (index >= amplitudes.size) {
                index = amplitudes.size - 1
            }
            val barHeight = (amplitudes[index] / normalizer) * usableHeight
            val drawX = translateX + x * scaleX
            canvas.drawLine(
                drawX,
                rect.top + rect.height(),
                drawX,
                rect.top + rect.height() - barHeight,
                paint
            )
        }
    }

    /**
     * Change the waveform color and regenerate the bitmap.
     */
    fun setColor(color: Int) {
        paint.color = color
        generateBitmap()
    }

    /**
     * Returns the pre-rendered waveform bitmap (for testing or external access).
     */
    fun getBitmap(): Bitmap? = waveformBitmap

    /**
     * Recycle the bitmap to free native memory.
     * Must be called when the parent EntityAudio is released.
     */
    fun release() {
        waveformBitmap?.let {
            if (!it.isRecycled) it.recycle()
        }
        waveformBitmap = null
    }
}
