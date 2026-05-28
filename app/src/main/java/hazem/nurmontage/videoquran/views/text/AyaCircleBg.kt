package hazem.nurmontage.videoquran.views.text

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import hazem.nurmontage.videoquran.views.TypefaceCache

/**
 * Custom TextView that renders Quran ayah text inside a gradient circle background.
 *
 * Originally: AyaCircleBg.java
 * Converted to: AyaCircleBg.kt — idiomatic Kotlin, shared TypefaceCache
 *
 * This view combines two visual elements:
 * 1. A **circular gradient background** drawn behind the text using a 3-color
 *    LinearGradient (purple → pink → gold), creating an Instagram-style
 *    story circle effect
 * 2. The **ayah text** rendered in "محمدي" (Mohammadi) Ottoman font on top
 *
 * The circle radius is calculated dynamically from the text dimensions to
 * ensure the background always fits the content. The gradient flows
 * horizontally across the circle.
 *
 * Used for ayah display cards, surah headers, and featured ayah highlights.
 *
 * @see TypefaceCache
 */
class AyaCircleBg : AppCompatTextView {

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    constructor(context: Context) : super(context) { init() }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) { init() }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr) { init() }

    private fun init() {
        TypefaceCache.get(resources.assets, FONT_PATH)?.let { setTypeface(it) }
    }

    override fun onDraw(canvas: Canvas) {
        val text = text.toString()
        val textPaint = paint
        val textWidth = textPaint.measureText(text)
        val fontMetrics = textPaint.fontMetrics

        // Calculate circle radius to fit both text width and height
        val contentSize = maxOf(textWidth, fontMetrics.descent - fontMetrics.ascent)
        val radius = contentSize / 2f + 20f

        val centerX = width / 2f
        val centerY = height / 2f
        val halfTextWidth = textWidth / 2f

        // Apply Instagram-style 3-color gradient (purple → pink → gold)
        bgPaint.shader = LinearGradient(
            centerX - halfTextWidth, centerY,
            centerX + halfTextWidth, centerY,
            intArrayOf(
                Color.parseColor("#B7833AB4"),  // Purple
                Color.parseColor("#E1306C"),    // Pink
                Color.parseColor("#BCF58529")   // Gold
            ),
            null,
            Shader.TileMode.CLAMP
        )

        canvas.drawCircle(centerX, centerY, radius, bgPaint)
        super.onDraw(canvas)
    }

    companion object {
        private const val FONT_PATH = "fonts/arabic/محمدي.ttf"
    }
}
