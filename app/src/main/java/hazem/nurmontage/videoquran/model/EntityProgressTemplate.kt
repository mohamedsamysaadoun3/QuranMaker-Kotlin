package hazem.nurmontage.videoquran.model

import java.io.Serializable

/**
 * Progress bar overlay position template for export rendering.
 * Defines the (left, top) anchor point of the progress indicator.
 */
data class EntityProgressTemplate(
    var left: Float = 0f,
    var top: Float = 0f
) : Serializable
