package hazem.nurmontage.videoquran.constant

/**
 * Enum representing the different audio effect categories
 * that can be applied to an audio entity in the timeline.
 *
 * Used by [EditMediaFragment.IEditMediaCallback.updateEntity]
 * to identify which effect type was modified.
 */
enum class EffectAudioType {
    VOLUME,
    ECHO,
    REVERB,
    FADE,
    SPEED,
    ENHANCE,
    NOICE
}
