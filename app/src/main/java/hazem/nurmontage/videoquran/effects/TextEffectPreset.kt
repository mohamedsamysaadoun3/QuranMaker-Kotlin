package hazem.nurmontage.videoquran.effects

/**
 * Preset text effects for video production.
 * Each preset generates an appropriate CinematicEffect and FFmpeg filter configuration.
 */
enum class TextEffectPreset(
    val displayName: String,
    val description: String
) {
    GLOW("Glow", "Soft luminous glow around text"),
    SHADOW("Shadow", "Drop shadow beneath text"),
    OUTLINE("Outline", "Thin outline around text characters"),
    NEON("Neon", "Bright neon tube effect"),
    CINEMATIC_FADE("Cinematic Fade", "Fade in with depth shadow"),
    TYPEWRITER("Typewriter", "Character-by-character reveal effect");

    /**
     * Create a CinematicEffect configured for this preset.
     */
    fun createEffect(): CinematicEffect = when (this) {
        GLOW           -> CinematicEffect.softGlow()
        SHADOW         -> CinematicEffect.deepShadow()
        OUTLINE        -> CinematicEffect().apply {
            setName("Outline")
            setShadowOffsetX(1)
            setShadowOffsetY(1)
            setShadowBlur(1)
            setDepthLayers(0)
        }
        NEON           -> CinematicEffect.neonGlow(0xFF00FFFF.toInt())
        CINEMATIC_FADE -> CinematicEffect.cinematic()
        TYPEWRITER     -> CinematicEffect().apply {
            setName("Typewriter")
            setShadowOffsetX(1)
            setShadowOffsetY(1)
            setShadowBlur(2)
            setDepthLayers(0)
        }
    }

    /**
     * Get FFmpeg filter string for this preset applied to text.
     */
    fun getFFmpegFilter(
        text: String, x: Int, y: Int, fontSize: Int,
        fontColor: String, fontFile: String?
    ): String {
        val effect = createEffect()
        val baseFilter = effect.toFFmpegFilter(text, x, y, fontSize, fontColor, fontFile)

        return when (this) {
            TYPEWRITER     -> baseFilter
            CINEMATIC_FADE -> "$baseFilter,fade=t=in:st=0:d=1.5"
            else           -> baseFilter
        }
    }

    companion object {
        /** Get all preset names as a string array. */
        @JvmStatic
        fun getPresetNames(): Array<String> = values().map { it.displayName }.toTypedArray()
    }
}
