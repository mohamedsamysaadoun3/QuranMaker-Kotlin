package hazem.nurmontage.videoquran.model

import java.io.Serializable

/**
 * Data model holding all configurable audio effect parameters
 * for a single [EntityAudio] in the timeline.
 *
 * This model is serialized when saving project state, so field names
 * that are serialization-critical (e.g. via Reflection/Gson) must
 * preserve their original names from the Java version.
 *
 * Key FFmpeg audio filter chain parameters:
 * - **volume** – normalized gain (1.0 = original)
 * - **speed** – playback tempo factor (1.0 = original)
 * - **reverbPreset** – raw FFmpeg aecho filter string (e.g. "aecho=0.9:0.4:900|1800:0.20|0.15")
 * - **fade_in / fade_out** – fade durations in milliseconds
 * - **delays / decays** – echo delay and decay parameters
 * - **isEnhance / isRemoveNoice** – toggle flags for audio enhancement and noise removal
 */
data class EffectAudio(
    var decays: Int = 0,
    var decays_cmd: String? = null,
    var delays: Int = 0,
    var delays_cmd: String? = null,
    var duration: Int = 0,
    var end: Float = 0f,
    var fade_in: Int = 0,
    var fade_out: Int = 0,
    var isEnhance: Boolean = false,
    var isRemoveNoice: Boolean = false,
    var outGain: Float = 0f,
    var reverbPreset: String? = null,
    var reverbPreset_index_list: Int = 0,
    var start: Float = 0f,
    var volume_echo: Int = 0,
    var volume: Float = 1.0f,
    var speed: Float = 1.0f
) : Serializable
