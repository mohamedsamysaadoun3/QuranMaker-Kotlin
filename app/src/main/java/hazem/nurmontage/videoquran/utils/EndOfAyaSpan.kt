package hazem.nurmontage.videoquran.utils

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.VectorDrawable
import android.text.style.ReplacementSpan

/**
 * ReplacementSpan that draws an ayah end marker (۞) with the ayah number inside.
 *
 * Originally: EndOfAyaSpan.java (in Utils package)
 * Converted to: EndOfAyaSpan.kt — idiomatic Kotlin, preserved rendering logic
 *
 * This span replaces the " نص" (text) marker at the end of each Quran ayah
 * with a decorative circle containing the ayah number. The rendering pipeline:
 *
 * 1. If the text is NOT " نص" → draws normally (no replacement)
 * 2. If the text IS " نص" → replaces with:
 *    a. A VectorDrawable circle background (the ۞ symbol shape)
 *    b. The ayah number drawn on top in a smaller, bold font
 *
 * The VectorDrawable is tinted with the current text color via SRC_IN
 * PorterDuff mode, ensuring the marker matches the ayah text color
 * regardless of theme or user color selection.
 *
 * Text size is adjusted based on the number of digits:
 * - 1-2 digits: 70% of original size, positioned at 54% horizontal offset
 * - 3+ digits: 80% of original size, then 70% again, positioned at 40% offset
 *
 * @property vectorDrawable The ۞ shape drawable for the ayah end marker
 * @property fontNumber The Typeface for rendering the ayah number
 * @property number The ayah number string (e.g., "1", "42", "255")
 */
class EndOfAyaSpan(
    private val vectorDrawable: VectorDrawable,
    private val fontNumber: Typeface,
    private val number: String
) : ReplacementSpan() {

    override fun draw(
        canvas: Canvas,
        text: CharSequence,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        baseline: Int,
        bottom: Int,
        paint: Paint
    ) {
        val substring = text.substring(start, end)

        // Only replace the " نص" marker — draw other text normally
        if (substring != " نص") {
            canvas.drawText(text, start, end, x, baseline, paint)
            return
        }

        val textWidth = paint.measureText(substring)

        // Save original paint state
        val originalTypeface = paint.typeface
        val originalTextSize = paint.textSize

        // Apply ayah number font styling
        paint.typeface = fontNumber
        paint.isFakeBoldText = true

        // Adjust text size based on digit count
        if (number.length > 2) {
            paint.textSize = paint.textSize * 0.8f
        } else {
            paint.textSize = paint.textSize * 0.7f
        }

        // Measure ayah number bounds
        val textBounds = Rect()
        paint.getTextBounds(number, 0, number.length, textBounds)

        // Calculate drawable bounds (centered in the span area)
        val spanRect = RectF(x.toInt(), top, (textWidth + x).toInt(), bottom)
        val halfWidth = spanRect.width() * 0.43f
        val halfHeight = spanRect.height() * 0.42f
        vectorDrawable.setBounds(
            (spanRect.centerX() - halfWidth).toInt(),
            (spanRect.centerY() - halfHeight).toInt(),
            (spanRect.centerX() + halfWidth).toInt(),
            (spanRect.centerY() + halfHeight).toInt()
        )

        // Tint the drawable with the current text color
        vectorDrawable.setColorFilter(paint.color, PorterDuff.Mode.SRC_IN)
        vectorDrawable.draw(canvas)

        // Draw the ayah number on top of the circle
        if (number.length > 2) {
            paint.textSize = paint.textSize * 0.7f
            canvas.drawText(
                number,
                spanRect.centerX() - textBounds.width() * 0.4f,
                spanRect.centerY() + textBounds.height() * 0.35f,
                paint
            )
        } else {
            canvas.drawText(
                number,
                spanRect.centerX() - textBounds.width() * 0.54f,
                spanRect.centerY() + textBounds.height() * 0.4f,
                paint
            )
        }

        // Restore original paint state
        paint.typeface = originalTypeface
        paint.textSize = originalTextSize
        paint.isFakeBoldText = false
    }

    override fun getSize(
        paint: Paint,
        text: CharSequence,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ): Int {
        return Math.round(paint.measureText(text, start, end))
    }
}
