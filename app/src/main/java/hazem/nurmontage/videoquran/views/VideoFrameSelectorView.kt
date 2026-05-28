package hazem.nurmontage.videoquran.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.media.MediaMetadataRetriever
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.AppCompatView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Custom view that displays a horizontal strip of video frames
 * and allows the user to scrub/seek to select a specific frame.
 *
 * Used by [ChoiceBgFromVideoActivity] to let users pick a video frame
 * as background image.
 *
 * The view draws a series of thumbnail frames from the video and
 * overlays a position indicator that the user can drag.
 */
class VideoFrameSelectorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatView(context, attrs, defStyleAttr) {

    /** Callback interface for frame seeking events. */
    interface OnFrameSeekListener {
        /** Called when the user scrubs to a new position. */
        fun onSeekTo(timeUs: Long)
    }

    private var seekListener: OnFrameSeekListener? = null
    private var durationUs: Long = 0L
    private var currentPositionUs: Long = 0L

    private val frameBitmaps = mutableListOf<Bitmap>()
    private val paintLine = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFC30B")
        strokeWidth = 4f
    }
    private val paintBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0x66000000
    }

    private var indicatorX: Float = 0f
    private var isDragging: Boolean = false
    private var frameJob: Job? = null

    /** Number of thumbnail frames to extract from the video. */
    private val THUMBNAIL_COUNT = 10

    fun setOnFrameSeekListener(listener: OnFrameSeekListener) {
        seekListener = listener
    }

    /**
     * Set the total duration of the video in microseconds.
     * Triggers thumbnail extraction if video path was already set.
     */
    fun setDuration(durationUs: Long) {
        this.durationUs = durationUs
        requestLayout()
        invalidate()
    }

    /**
     * Extract thumbnail frames from the given video path.
     */
    fun setVideoPath(videoPath: String) {
        frameJob?.cancel()
        frameJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(videoPath)

                val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                val durationMs = durationStr?.toLongOrNull() ?: 0L
                durationUs = durationMs * 1000L

                val newBitmaps = mutableListOf<Bitmap>()
                val interval = if (THUMBNAIL_COUNT > 1) durationMs / (THUMBNAIL_COUNT - 1) else 0L

                for (i in 0 until THUMBNAIL_COUNT) {
                    try {
                        val timeUs = (i * interval * 1000L).coerceAtMost(durationUs)
                        retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)?.let {
                            newBitmaps.add(it)
                        }
                    } catch (_: Exception) {
                        // Skip frames that fail to extract
                    }
                }

                try {
                    retriever.release()
                } catch (_: Exception) {}

                withContext(Dispatchers.Main) {
                    frameBitmaps.clear()
                    frameBitmaps.addAll(newBitmaps)
                    invalidate()
                }
            } catch (_: Exception) {
                // Failed to extract frames
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        indicatorX = 0f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()

        // Draw thumbnail frames
        if (frameBitmaps.isNotEmpty()) {
            val frameWidth = width / frameBitmaps.size
            for (i in frameBitmaps.indices) {
                val bitmap = frameBitmaps[i]
                val src = Rect(0, 0, bitmap.width, bitmap.height)
                val dst = Rect(
                    (i * frameWidth).toInt(), 0,
                    ((i + 1) * frameWidth).toInt(), height.toInt()
                )
                canvas.drawBitmap(bitmap, src, dst, null)
            }
        } else {
            canvas.drawRect(0f, 0f, width, height, paintBg)
        }

        // Draw indicator line
        if (durationUs > 0) {
            indicatorX = (currentPositionUs.toFloat() / durationUs.toFloat()) * width
        }
        canvas.drawLine(indicatorX, 0f, indicatorX, height, paintLine)

        // Draw top/bottom border
        paintLine.strokeWidth = 2f
        canvas.drawLine(0f, 0f, width, 0f, paintLine)
        canvas.drawLine(0f, height, width, height, paintLine)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isDragging = true
                updatePosition(event.x)
                parent.requestDisallowInterceptTouchEvent(true)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (isDragging) {
                    updatePosition(event.x)
                    return true
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isDragging = false
                parent.requestDisallowInterceptTouchEvent(false)
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun updatePosition(x: Float) {
        val width = width.toFloat()
        if (width <= 0f) return

        val ratio = (x / width).coerceIn(0f, 1f)
        currentPositionUs = (ratio * durationUs).toLong()
        indicatorX = x.coerceIn(0f, width)
        invalidate()

        seekListener?.onSeekTo(currentPositionUs)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        frameJob?.cancel()
        frameBitmaps.forEach { it.recycle() }
        frameBitmaps.clear()
    }
}
