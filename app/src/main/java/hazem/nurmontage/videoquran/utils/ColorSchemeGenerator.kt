package hazem.nurmontage.videoquran.utils

import android.graphics.Color

/**
 * Color scheme generator for creating harmonious UI color palettes.
 * Generates complete color schemes (screen, body, shadow, label, accent)
 * from a single base color using HSV color space manipulations.
 *
 * Originally: ColorSchemeGenerator.java
 * Converted to: ColorSchemeGenerator.kt — idiomatic Kotlin with data class
 *
 * Used by the UI engine to dynamically generate color palettes for
 * templates, backgrounds, and text styling based on user-selected colors.
 */
object ColorSchemeGenerator {

    /**
     * Data class representing a complete UI color scheme.
     * Each property maps to a specific UI element role.
     *
     * @property screen1 Primary screen/background color
     * @property screen2 Secondary screen/background color (lighter)
     * @property body Main body/content area color (complementary)
     * @property shadow Shadow color derived from body (darker)
     * @property label Text label color (high contrast against body)
     * @property accent Accent/highlight color
     * @property circle Circular element color (complementary, desaturated)
     */
    data class Scheme(
        var screen1: Int = 0,
        var screen2: Int = 0,
        var body: Int = 0,
        var shadow: Int = 0,
        var label: Int = 0,
        var accent: Int = 0,
        var circle: Int = 0
    )

    /**
     * Generates a complete color scheme from a base color with hue rotation.
     * The hue is rotated by [hueOffset] degrees before generating complementary colors.
     *
     * @param baseColor The starting ARGB color
     * @param hueOffset Degrees to rotate the hue before generating the scheme
     * @return A complete [Scheme] with all color roles filled
     */
    fun generateScheme(baseColor: Int, hueOffset: Float): Scheme {
        val rotatedColor = rotateHue(baseColor, hueOffset)
        return Scheme(
            screen1 = baseColor,
            screen2 = lightenColor(rotatedColor, 0.15f),
            body = getComplementaryColor(rotatedColor),
            shadow = darkenColor(getComplementaryColor(rotatedColor), 0.25f),
            label = generateLabelColor(getComplementaryColor(rotatedColor)),
            accent = darkenColor(getComplementaryColor(rotatedColor), 0.15f)
        )
    }

    /**
     * Generates a complete color scheme from a base color without hue rotation.
     * Uses the base color directly to derive complementary and accent colors.
     *
     * @param baseColor The starting ARGB color
     * @return A complete [Scheme] with all color roles filled
     */
    fun generateScheme(baseColor: Int): Scheme {
        val bodyColor = getComplementaryColor(baseColor)
        return Scheme(
            screen1 = baseColor,
            screen2 = lightenColor(baseColor, 0.15f),
            body = bodyColor,
            shadow = darkenColor(bodyColor, 0.25f),
            label = generateLabelColor(bodyColor),
            accent = darkenColor(getComplementaryColor(bodyColor), 0.15f)
        )
    }

    /**
     * Generates a circular element color by shifting the hue 180 degrees
     * and desaturating to 0.4 maximum with brightness 0.95.
     *
     * @param color The base ARGB color
     * @return The generated circular element color
     */
    fun generateCircleColor(color: Int): Int {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        hsv[0] = (hsv[0] + 180f) % 360f
        hsv[1] = minOf(0.4f, hsv[1])
        hsv[2] = 0.95f
        return Color.HSVToColor(hsv)
    }

    /**
     * Rotates the hue of a color by the specified offset in degrees.
     * Handles negative offsets by wrapping around 360 degrees.
     *
     * @param color The base ARGB color
     * @param degrees Hue rotation in degrees (positive or negative)
     * @return The hue-rotated color
     */
    fun rotateHue(color: Int, degrees: Float): Int {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        val newHue = (hsv[0] + degrees) % 360f
        hsv[0] = if (newHue < 0f) newHue + 360f else newHue
        return Color.HSVToColor(hsv)
    }

    /**
     * Lightens a color by increasing its HSV brightness (value) component.
     *
     * @param color The base ARGB color
     * @param amount Amount to increase brightness (0.0 - 1.0)
     * @return The lightened color
     */
    fun lightenColor(color: Int, amount: Float): Int {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        hsv[2] = minOf(1.0f, hsv[2] + amount)
        return Color.HSVToColor(hsv)
    }

    /**
     * Darkens a color by decreasing its HSV brightness (value) component.
     *
     * @param color The base ARGB color
     * @param amount Amount to decrease brightness (0.0 - 1.0)
     * @return The darkened color
     */
    fun darkenColor(color: Int, amount: Float): Int {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        hsv[2] = maxOf(0.0f, hsv[2] - amount)
        return Color.HSVToColor(hsv)
    }

    /**
     * Returns the complementary color by shifting the hue 180 degrees.
     *
     * @param color The base ARGB color
     * @return The complementary color
     */
    fun getComplementaryColor(color: Int): Int {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        hsv[0] = (hsv[0] + 180f) % 360f
        return Color.HSVToColor(hsv)
    }

    /**
     * Generates a label/text color with reduced saturation and boosted brightness.
     * Ensures text readability by guaranteeing a minimum brightness of 0.85.
     *
     * @param color The base ARGB color
     * @return The label-optimized color
     */
    fun generateLabelColor(color: Int): Int {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        val originalHue = hsv[0]
        hsv[1] = maxOf(0.0f, hsv[1] * 0.4f)
        var brightness = minOf(1.0f, hsv[2] + 0.25f)
        hsv[2] = brightness
        if (brightness < 0.75f) {
            hsv[2] = 0.85f
        }
        hsv[0] = originalHue
        return Color.HSVToColor(hsv)
    }

    /**
     * Generates an accent color by analyzing the base color's hue
     * and overriding it with warm defaults (hue=30, sat=0.8, val=0.9).
     *
     * @param color The base ARGB color
     * @return The accent color
     */
    fun generateAccentColor(color: Int): Int {
        val hsv = floatArrayOf(30f, 0.8f, 0.9f)
        Color.colorToHSV(color, hsv)
        return Color.HSVToColor(hsv)
    }
}
