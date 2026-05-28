package hazem.nurmontage.videoquran.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.AppCompatView

/**
 * Custom view that displays a bitmap with a movable crop region overlay.
 *
 * Used by [CropBitmapActivity] to allow users to crop an image.
 *
 * Features:
 *   - Displays the source bitmap scaled to fit the view
 *   - Shows a draggable crop rectangle with corner handles
 *   - Darkens the area outside the crop region
 *   - Supports moving the crop region and resizing via corner handles
 */
class CropView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatView(context, attrs, defStyleAttr) {

    /** The source bitmap to crop. */
    var bitmap: Bitmap? = null
        set(value) {
            field = value
            resetCropRect()
            invalidate()
        }

    /** The crop region in view coordinates. */
    val cropRect = RectF()

    /** Callback when crop region changes. */
    var onCropChanged: (() -> Unit)? = null

    private val paintBitmap = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val paintOverlay = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x99000000.toInt() }
    private val paintBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }
    private val paintHandle = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }
    private val paintGrid = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0x80FFFFFF.toInt()
        style = Paint.Style.STROKE
        strokeWidth = 1f
    }

    private val bitmapMatrix = Matrix()
    private val displayRect = RectF()

    private val handleSize = 24f
    private var activeHandle: Int = HANDLE_NONE
    private var lastTouchX = 0f
    private var lastTouchY = 0f

    private companion object {
        const val HANDLE_NONE = -1
        const val HANDLE_TOP_LEFT = 0
        const val HANDLE_TOP_RIGHT = 1
        const val HANDLE_BOTTOM_LEFT = 2
        const val HANDLE_BOTTOM_RIGHT = 3
        const val HANDLE_CENTER = 4
    }

    /** Reset the crop rectangle to cover 80% of the bitmap area. */
    fun resetCropRect() {
        val bmp = bitmap ?: return
        post {
            calculateDisplayRect()
            val marginX = displayRect.width() * 0.1f
            val marginY = displayRect.height() * 0.1f
            cropRect.set(
                displayRect.left + marginX,
                displayRect.top + marginY,
                displayRect.right - marginX,
                displayRect.bottom - marginY
            )
            invalidate()
        }
    }

    /**
     * Get the crop rectangle mapped back to the original bitmap coordinates.
     */
    fun getCropRectOnBitmap(): RectF {
        val bmp = bitmap ?: return RectF()
        val result = RectF()

        // Invert the display matrix to map view coords → bitmap coords
        val inverse = Matrix()
        if (bitmapMatrix.invert(inverse)) {
            inverse.mapRect(result, cropRect)
        } else {
            // Fallback: proportional mapping
            result.left = ((cropRect.left - displayRect.left) / displayRect.width()) * bmp.width
            result.top = ((cropRect.top - displayRect.top) / displayRect.height()) * bmp.height
            result.right = ((cropRect.right - displayRect.left) / displayRect.width()) * bmp.width
            result.bottom = ((cropRect.bottom - displayRect.top) / displayRect.height()) * bmp.height
        }

        // Clamp to bitmap bounds
        result.left = result.left.coerceIn(0f, bmp.width.toFloat())
        result.top = result.top.coerceIn(0f, bmp.height.toFloat())
        result.right = result.right.coerceIn(0f, bmp.width.toFloat())
        result.bottom = result.bottom.coerceIn(0f, bmp.height.toFloat())

        return result
    }

    private fun calculateDisplayRect() {
        val bmp = bitmap ?: return
        val viewWidth = width.toFloat()
        val viewHeight = height.toFloat()
        if (viewWidth <= 0f || viewHeight <= 0f) return

        val scaleX = viewWidth / bmp.width
        val scaleY = viewHeight / bmp.height
        val scale = minOf(scaleX, scaleY)

        val drawWidth = bmp.width * scale
        val drawHeight = bmp.height * scale
        val left = (viewWidth - drawWidth) / 2f
        val top = (viewHeight - drawHeight) / 2f

        displayRect.set(left, top, left + drawWidth, top + drawHeight)
        bitmapMatrix.setScale(scale, scale)
        bitmapMatrix.postTranslate(left, top)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        calculateDisplayRect()
        if (cropRect.isEmpty) resetCropRect()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val bmp = bitmap ?: return

        // Draw bitmap
        canvas.drawBitmap(bmp, bitmapMatrix, paintBitmap)

        // Draw dark overlay outside crop region
        val left = RectF(0f, 0f, width.toFloat(), cropRect.top)
        val right = RectF(0f, cropRect.bottom, width.toFloat(), height.toFloat())
        val topRect = RectF(0f, cropRect.top, cropRect.left, cropRect.bottom)
        val bottomRect = RectF(cropRect.right, cropRect.top, width.toFloat(), cropRect.bottom)

        canvas.drawRect(left, paintOverlay)
        canvas.drawRect(right, paintOverlay)
        canvas.drawRect(topRect, paintOverlay)
        canvas.drawRect(bottomRect, paintOverlay)

        // Draw crop border
        canvas.drawRect(cropRect, paintBorder)

        // Draw rule-of-thirds grid lines
        val thirdW = cropRect.width() / 3f
        val thirdH = cropRect.height() / 3f
        for (i in 1..2) {
            val x = cropRect.left + thirdW * i
            canvas.drawLine(x, cropRect.top, x, cropRect.bottom, paintGrid)
            val y = cropRect.top + thirdH * i
            canvas.drawLine(cropRect.left, y, cropRect.right, y, paintGrid)
        }

        // Draw corner handles
        drawHandle(canvas, cropRect.left, cropRect.top)
        drawHandle(canvas, cropRect.right, cropRect.top)
        drawHandle(canvas, cropRect.left, cropRect.bottom)
        drawHandle(canvas, cropRect.right, cropRect.bottom)
    }

    private fun drawHandle(canvas: Canvas, x: Float, y: Float) {
        canvas.drawCircle(x, y, handleSize / 2f, paintHandle)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchX = event.x
                lastTouchY = event.y
                activeHandle = detectHandle(event.x, event.y)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = event.x - lastTouchX
                val dy = event.y - lastTouchY
                lastTouchX = event.x
                lastTouchY = event.y

                when (activeHandle) {
                    HANDLE_TOP_LEFT -> {
                        cropRect.left = (cropRect.left + dx).coerceIn(displayRect.left, cropRect.right - handleSize * 2)
                        cropRect.top = (cropRect.top + dy).coerceIn(displayRect.top, cropRect.bottom - handleSize * 2)
                    }
                    HANDLE_TOP_RIGHT -> {
                        cropRect.right = (cropRect.right + dx).coerceIn(cropRect.left + handleSize * 2, displayRect.right)
                        cropRect.top = (cropRect.top + dy).coerceIn(displayRect.top, cropRect.bottom - handleSize * 2)
                    }
                    HANDLE_BOTTOM_LEFT -> {
                        cropRect.left = (cropRect.left + dx).coerceIn(displayRect.left, cropRect.right - handleSize * 2)
                        cropRect.bottom = (cropRect.bottom + dy).coerceIn(cropRect.top + handleSize * 2, displayRect.bottom)
                    }
                    HANDLE_BOTTOM_RIGHT -> {
                        cropRect.right = (cropRect.right + dx).coerceIn(cropRect.left + handleSize * 2, displayRect.right)
                        cropRect.bottom = (cropRect.bottom + dy).coerceIn(cropRect.top + handleSize * 2, displayRect.bottom)
                    }
                    HANDLE_CENTER -> {
                        val newLeft = cropRect.left + dx
                        val newTop = cropRect.top + dy
                        val newRight = cropRect.right + dx
                        val newBottom = cropRect.bottom + dy
                        if (newLeft >= displayRect.left && newRight <= displayRect.right &&
                            newTop >= displayRect.top && newBottom <= displayRect.bottom
                        ) {
                            cropRect.offset(dx, dy)
                        }
                    }
                }
                invalidate()
                onCropChanged?.invoke()
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                activeHandle = HANDLE_NONE
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun detectHandle(x: Float, y: Float): Int {
        val touchRadius = handleSize * 1.5f

        if (distance(x, y, cropRect.left, cropRect.top) < touchRadius) return HANDLE_TOP_LEFT
        if (distance(x, y, cropRect.right, cropRect.top) < touchRadius) return HANDLE_TOP_RIGHT
        if (distance(x, y, cropRect.left, cropRect.bottom) < touchRadius) return HANDLE_BOTTOM_LEFT
        if (distance(x, y, cropRect.right, cropRect.bottom) < touchRadius) return HANDLE_BOTTOM_RIGHT
        if (cropRect.contains(x, y)) return HANDLE_CENTER

        return HANDLE_NONE
    }

    private fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val dx = x1 - x2
        val dy = y1 - y2
        return kotlin.math.sqrt(dx * dx + dy * dy)
    }
}
