package hazem.nurmontage.videoquran.model

import java.io.Serializable

/**
 * Timer display configuration for the video progress overlay.
 * Holds font size, color, positioning, and progress bar bitmap dimensions.
 *
 * Serialization field names preserved verbatim for backward compatibility.
 */
data class TimeModel(
    var width_bitmap_progress: Int = 0,
    var height_bitmap_progress: Int = 0,
    var size: Float = 0f,
    var color: String = "#FFFFFF",
    var posY: Float = 0f,
    var posXRight: Float = 0f,
    var progress_offset: Int = 0
) : Serializable {

    var widthShape: Int = 0
    var heightShape: Int = 0
    var startShape: Float = 0f
}
