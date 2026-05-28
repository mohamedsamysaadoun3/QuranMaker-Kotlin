package hazem.nurmontage.videoquran.model

import hazem.nurmontage.videoquran.core.common.Constants

/**
 * Data model representing a frame/iPad template item.
 * Used by [hazem.nurmontage.videoquran.adapter.FrameAdapter] to display
 * available frame types (iPad, Round Rect, Border, Gradient, etc.).
 *
 * Each IpadItem holds a drawable resource ID and an [Constants.IpadType] enum
 * that determines which frame style is applied to the video canvas.
 *
 * @property img Drawable resource ID for the frame preview thumbnail
 * @property ipadType The frame type enum determining the visual style
 */
data class IpadItem(
    val img: Int,
    val ipadType: Constants.IpadType
)
