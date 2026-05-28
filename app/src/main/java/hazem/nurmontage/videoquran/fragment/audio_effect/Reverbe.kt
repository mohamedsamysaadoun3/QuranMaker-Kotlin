package hazem.nurmontage.videoquran.fragment.audio_effect

/**
 * Data model representing an audio reverb preset.
 * Used by [hazem.nurmontage.videoquran.adapter.SoundAdapter] to display
 * and apply reverb/audio effect presets on the audio track.
 *
 * Each Reverbe holds a human-readable name and an FFmpeg command string
 * that applies the corresponding audio effect when executed.
 *
 * @property name Display name of the reverb preset (e.g., "Hall", "Room", "Cathedral")
 * @property cmdFfmpeg The FFmpeg filter command string for this reverb effect
 */
data class Reverbe(
    val name: String,
    val cmdFfmpeg: String
)
