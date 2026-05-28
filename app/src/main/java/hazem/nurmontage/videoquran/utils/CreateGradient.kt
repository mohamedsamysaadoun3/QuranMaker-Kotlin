package hazem.nurmontage.videoquran.utils

import android.graphics.LinearGradient
import android.graphics.RectF
import android.graphics.Shader

/**
 * Utility for creating LinearGradient shaders with angle-based direction.
 *
 * Originally: CreateGradient.java
 * Converted to: CreateGradient.kt — idiomatic Kotlin, preserved precision
 *
 * Used by the rendering engine to apply gradient backgrounds to entity
 * blocks (Quran ayahs, translations, bismillah) on the video canvas.
 * The angle parameter controls the gradient direction in degrees,
 * following the CSS gradient angle convention (0° = up, 90° = right, etc.).
 */
object CreateGradient {

    /**
     * Creates a LinearGradient that spans the given rectangle with the
     * specified angle, colors, and position stops.
     *
     * The gradient line is calculated from the center of the rectangle
     * using trigonometric projection based on the angle. The length
     * of the gradient line equals the hypotenuse of the rectangle's
     * half-dimensions, ensuring full coverage.
     *
     * @param rect The bounding rectangle for the gradient
     * @param angle The gradient angle in degrees (CSS convention)
     * @param colors Array of ARGB colors for the gradient stops
     * @param positions Array of position values (0.0-1.0) for each color stop
     * @return A configured LinearGradient shader
     */
    fun createLinearGradientWithAngle(
        rect: RectF,
        angle: Float,
        colors: IntArray,
        positions: FloatArray
    ): LinearGradient {
        val radians = Math.toRadians(angle.toDouble())
        val halfWidth = rect.width() / 2f
        val halfHeight = rect.height() / 2f
        val centerX = rect.centerX()
        val centerY = rect.centerY()

        val hypot = Math.hypot(halfWidth.toDouble(), halfHeight.toDouble()).toFloat()
        val cos = (Math.cos(radians) * hypot).toFloat()
        val sin = (Math.sin(radians) * hypot).toFloat()

        return LinearGradient(
            centerX - cos, centerY - sin,
            centerX + cos, centerY + sin,
            colors, positions,
            Shader.TileMode.CLAMP
        )
    }
}
