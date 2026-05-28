package hazem.nurmontage.videoquran.effects

/**
 * Multi-layer shadow, glow, and depth effects for text.
 * Generates FFmpeg filter chains for text effects in video export.
 *
 * The effect is rendered as a series of drawtext FFmpeg filters:
 * 1. Depth layers — multiple shadows at increasing offsets for 3D depth
 * 2. Main shadow — primary drop shadow
 * 3. Glow — blurred halo behind text (optional)
 * 4. Main text — the crisp text on top
 */
class CinematicEffect {

    companion object {
        /** Preset: deep drop shadow with 3 depth layers. */
        @JvmStatic
        fun deepShadow(): CinematicEffect = CinematicEffect().apply {
            name = "Deep Shadow"
            shadowOffsetX = 4
            shadowOffsetY = 4
            shadowBlur = 8
            depthLayers = 3
            depthSpread = 2f
        }

        /** Preset: soft luminous glow. */
        @JvmStatic
        fun softGlow(): CinematicEffect = CinematicEffect().apply {
            name = "Soft Glow"
            shadowBlur = 0
            glowRadius = 10
            glowOpacity = 50
        }

        /** Preset: neon tube glow with custom color. */
        @JvmStatic
        fun neonGlow(color: Int): CinematicEffect = CinematicEffect().apply {
            name = "Neon Glow"
            glowColor = color
            glowRadius = 15
            glowOpacity = 80
            shadowColor = color
            shadowBlur = 20
            shadowOffsetX = 0
            shadowOffsetY = 0
        }

        /** Preset: cinematic depth with glow and shadow. */
        @JvmStatic
        fun cinematic(): CinematicEffect = CinematicEffect().apply {
            name = "Cinematic"
            shadowOffsetX = 3
            shadowOffsetY = 3
            shadowBlur = 6
            depthLayers = 5
            depthSpread = 1.5f
            glowRadius = 5
            glowOpacity = 30
        }
    }

    var name: String = "Default"
    var shadowColor: Int = 0xFF000000.toInt()
    var shadowOffsetX: Int = 2
    var shadowOffsetY: Int = 2
    var shadowBlur: Int = 4
    var glowColor: Int = 0xFFFFFFFF.toInt()
    var glowRadius: Int = 0
    var glowOpacity: Int = 0
    var depthLayers: Int = 0
    var depthSpread: Float = 1f
    var enabled: Boolean = true

    // === FFmpeg Filter Generation ===

    /**
     * Generate FFmpeg drawtext filter with shadow effects.
     *
     * @param text      The text to render
     * @param x         X position
     * @param y         Y position
     * @param fontSize  Font size
     * @param fontColor Font color in hex (e.g., "white")
     * @param fontFile  Path to font file (can be null)
     * @return FFmpeg filter chain string
     */
    fun toFFmpegFilter(
        text: String, x: Int, y: Int, fontSize: Int,
        fontColor: String, fontFile: String?
    ): String {
        if (!enabled) {
            return buildDrawText(text, x, y, fontSize, fontColor, fontFile)
        }

        val sb = StringBuilder()

        // Depth layers (multiple shadows at increasing offsets)
        if (depthLayers > 0) {
            for (i in depthLayers downTo 1) {
                val offset = (i * depthSpread).toInt()
                val shadowHex = colorToHex(shadowColor, 0.3f)
                sb.append(buildDrawText(text,
                    x + shadowOffsetX + offset,
                    y + shadowOffsetY + offset,
                    fontSize, shadowHex, fontFile))
                sb.append(",")
            }
        }

        // Main shadow
        if (shadowBlur > 0 || shadowOffsetX != 0 || shadowOffsetY != 0) {
            val shadowHex = colorToHex(shadowColor, 0.7f)
            sb.append(buildDrawText(text,
                x + shadowOffsetX,
                y + shadowOffsetY,
                fontSize, shadowHex, fontFile))
            sb.append(",")
        }

        // Glow effect (blur behind text)
        if (glowRadius > 0 && glowOpacity > 0) {
            val glowHex = colorToHex(glowColor, glowOpacity / 100f)
            sb.append(buildDrawText(text, x, y, fontSize, glowHex, fontFile))
            if (shadowBlur > 0 || depthLayers > 0 || shadowOffsetX != 0) {
                // Add blur for glow
                sb.append(",boxblur=")
                sb.append(glowRadius)
                sb.append(",")
            }
            // Redraw text on top of glow
            sb.append(buildDrawText(text, x, y, fontSize, fontColor, fontFile))
        } else {
            // Main text
            sb.append(buildDrawText(text, x, y, fontSize, fontColor, fontFile))
        }

        return sb.toString()
    }

    private fun buildDrawText(
        text: String, x: Int, y: Int, fontSize: Int,
        fontColor: String, fontFile: String?
    ): String = buildString {
        append("drawtext=")
        if (!fontFile.isNullOrEmpty()) {
            append("fontfile=").append(fontFile).append(":")
        }
        append("text='").append(escapeText(text)).append("'")
        append(":fontcolor=").append(fontColor)
        append(":fontsize=").append(fontSize)
        append(":x=").append(x)
        append(":y=").append(y)
    }

    private fun escapeText(text: String): String =
        text.replace("'", "'\\''").replace(":", "\\:")

    private fun colorToHex(color: Int, alpha: Float): String {
        val r = (color shr 16) and 0xFF
        val g = (color shr 8) and 0xFF
        val b = color and 0xFF
        val a = (alpha * 255).toInt()
        return "0x%02X%02X%02X%02X".format(a, r, g, b)
    }

    // === Getters and Setters (kept for Java interop) ===

    fun getName(): String = name
    fun setName(name: String) { this.name = name }
    fun getShadowColor(): Int = shadowColor
    fun setShadowColor(shadowColor: Int) { this.shadowColor = shadowColor }
    fun getShadowOffsetX(): Int = shadowOffsetX
    fun setShadowOffsetX(shadowOffsetX: Int) { this.shadowOffsetX = shadowOffsetX }
    fun getShadowOffsetY(): Int = shadowOffsetY
    fun setShadowOffsetY(shadowOffsetY: Int) { this.shadowOffsetY = shadowOffsetY }
    fun getShadowBlur(): Int = shadowBlur
    fun setShadowBlur(shadowBlur: Int) { this.shadowBlur = shadowBlur }
    fun getGlowColor(): Int = glowColor
    fun setGlowColor(glowColor: Int) { this.glowColor = glowColor }
    fun getGlowRadius(): Int = glowRadius
    fun setGlowRadius(glowRadius: Int) { this.glowRadius = glowRadius }
    fun getGlowOpacity(): Int = glowOpacity
    fun setGlowOpacity(glowOpacity: Int) { this.glowOpacity = glowOpacity }
    fun getDepthLayers(): Int = depthLayers
    fun setDepthLayers(depthLayers: Int) { this.depthLayers = depthLayers }
    fun getDepthSpread(): Float = depthSpread
    fun setDepthSpread(depthSpread: Float) { this.depthSpread = depthSpread }
    fun isEnabled(): Boolean = enabled
    fun setEnabled(enabled: Boolean) { this.enabled = enabled }
}
