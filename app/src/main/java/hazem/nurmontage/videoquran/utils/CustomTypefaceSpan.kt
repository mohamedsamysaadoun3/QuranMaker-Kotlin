package hazem.nurmontage.videoquran.utils

import android.graphics.Paint
import android.graphics.Typeface
import android.text.TextPaint
import android.text.style.MetricAffectingSpan

/**
 * Span that applies a custom Typeface to a section of text.
 *
 * Originally: CustomTypefaceSpan.java (in Utils package)
 * Converted to: CustomTypefaceSpan.kt — idiomatic Kotlin
 *
 * This span affects both the measurement and drawing of text, ensuring
 * that the custom Typeface is applied consistently during layout
 * calculation and rendering. Extends [MetricAffectingSpan] so that
 * line height and text bounds are recalculated with the new font.
 *
 * Used to apply Quran Ottoman fonts to specific portions of Spannable
 * strings (e.g., mixing Arabic and Latin text in a single TextView).
 *
 * @property typeface The custom Typeface to apply
 */
class CustomTypefaceSpan(
    private val typeface: Typeface
) : MetricAffectingSpan() {

    override fun updateDrawState(textPaint: TextPaint) {
        applyCustomTypeFace(textPaint, typeface)
    }

    override fun updateMeasureState(textPaint: TextPaint) {
        applyCustomTypeFace(textPaint, typeface)
    }

    companion object {
        /**
         * Applies the custom Typeface to the given Paint.
         * Extracted as a static helper for consistency between measure and draw.
         */
        private fun applyCustomTypeFace(paint: Paint, typeface: Typeface) {
            paint.typeface = typeface
        }
    }
}
