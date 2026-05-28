package hazem.nurmontage.videoquran.model

import java.io.Serializable

/**
 * Data model representing a gradient color preset.
 * Used by [hazem.nurmontage.videoquran.adapter.PresetAdapter] to display
 * and apply gradient background presets on the video canvas.
 *
 * Serialization-compatible with the original Java class. The three color
 * stops (primary, secondary, tertiary) define the gradient, and the angle
 * controls its direction (default 81 degrees).
 *
 * @property color The primary (first) gradient color
 * @property second The secondary (middle) gradient color
 * @property three The tertiary (end) gradient color
 * @property angle The gradient angle in degrees (default: 81)
 */
data class Gradient(
    val color: Int,
    val second: Int,
    val three: Int,
    var angle: Int = 81
) : Serializable
