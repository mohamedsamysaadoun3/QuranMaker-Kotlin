package hazem.nurmontage.videoquran.views.image

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.core.common.Constants
import hazem.nurmontage.videoquran.views.TypefaceCache

/**
 * Square ImageView with multi-selection support for gallery photo picking.
 *
 * Originally: SquareImageView.java
 * Converted to: SquareImageView.kt — idiomatic Kotlin, shared TypefaceCache
 *
 * Features:
 * - **Square aspect ratio** enforced via onMeasure override
 * - **Selection overlay** with semi-transparent tint and colored border
 * - **Checkmark icon** drawn when selected (from R.drawable.check_24px)
 * - **Number badge** showing the selection order (1, 2, 3, ...)
 * - **Dynamic text centering** based on measured text width
 *
 * Used by [GalleryPickerAdabters] in the photo gallery picker to display
 * thumbnail images with multi-select capability. The number badge shows
 * the order in which photos were selected for the video timeline.
 *
 * JADX obfuscated names cleaned:
 * - f444cx → cx, f445cy → cy (circle center coordinates)
 * - f446r → radius (badge corner radius)
 * - f447x → x, f448y → y (checkmark position)
 *
 * @see TypefaceCache
 */
class SquareImageView : AppCompatImageView {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paintRect = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)

    private var isSelect: Boolean = false
    private var number: String? = null
    private var anInt: Int = 0
    private var cx: Float = 0f
    private var cy: Float = 0f
    private var radius: Float = 0f
    private var x: Float = 0f
    private var y: Float = 0f
    private var drawableDone: Drawable? = null

    /** Returns the selection order number. */
    fun getAnInt(): Int = anInt

    /**
     * Sets the selection order number and recalculates text centering.
     * @param number The selection order (1-based)
     */
    fun setNumber(number: Int) {
        if (number == 0) return
        this.anInt = number
        this.number = number.toString()
        this.cx = (width * 0.5f) - (textPaint.measureText(this.number) * 0.5f)
    }

    /** Returns whether this item is currently selected. */
    fun isMSelect(): Boolean = isSelect

    constructor(context: Context) : super(context) { init() }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) { init() }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr) { init() }

    private fun init() {
        textPaint.color = -1 // White
        textPaint.typeface = TypefaceCache.get(
            resources.assets,
            "fonts/${Constants.FONT_ENGLISH_APP}"  // "Poppins-Regular.ttf"
        )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = measuredWidth
        setMeasuredDimension(width, width)
    }

    override fun onSizeChanged(w: Int, h: Int, oldW: Int, oldH: Int) {
        super.onSizeChanged(w, h, oldW, oldH)

        paintRect.color = -1056964608 // Semi-transparent black overlay

        val size = w.toFloat()
        paint.strokeWidth = 0.02f * size

        if (!isSelect) {
            paint.color = -8355712     // Gray border
            paint.style = Paint.Style.STROKE
        } else {
            paint.color = -12190534    // Accent teal
            paint.style = Paint.Style.FILL
        }

        textPaint.textSize = 0.25f * size
        radius = 0.1f * size
        x = size - (1.2f * radius)
        y = radius + paint.strokeWidth

        if (number != null) {
            cx = (width * 0.5f) - (textPaint.measureText(number) * 0.5f)
        }
        cy = height * 0.5f

        // Bounds for the checkmark drawable
        val checkSize = (size * 0.3f).toInt()
        val centerX = (width * 0.5f).toInt()
        val halfCheck = checkSize
        val checkRect = Rect(
            centerX - halfCheck,
            (cy - halfCheck).toInt(),
            centerX + halfCheck,
            (cy + halfCheck).toInt()
        )
        val drawable = ContextCompat.getDrawable(context, R.drawable.check_24px)
        drawableDone = drawable
        drawable?.bounds = checkRect
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!isSelect) return

        // Draw semi-transparent overlay
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paintRect)

        // Draw checkmark icon
        drawableDone?.draw(canvas)

        // Draw selection number badge
        number?.let {
            canvas.drawText(it, cx, cy, textPaint)
        }
    }

    /**
     * Toggles the selection state with appropriate visual feedback.
     * @param selected true to select, false to deselect
     */
    fun onSelect(selected: Boolean) {
        isSelect = selected
        if (!selected) {
            paint.color = -8355712     // Gray border
            paint.style = Paint.Style.STROKE
        } else {
            paint.color = -12190534    // Accent teal
            paint.style = Paint.Style.FILL
        }
        invalidate()
    }
}
