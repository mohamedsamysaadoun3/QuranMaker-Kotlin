package hazem.nurmontage.videoquran.model

import java.io.Serializable

/**
 * Square/circle bitmap crop model for iPad frame video regions.
 * Holds crop coordinates, square dimensions, position, and corner radius.
 *
 * Serialization field names preserved verbatim: lef_square, width_sqaure (typo preserved).
 */
data class SquareBitmapModel(
    var lef_square: Float = 0f,
    var top_square: Float = 0f,
    var right: Float = 0f,
    var bottom: Float = 0f,
    var width_sqaure: Float = 50f,
    var height_square: Float = 50f,
    var raduis: Float = 0f
) : Serializable {

    var posX: Float = 0f
    var posY: Float = 0f

    /** Bulk update for all 9 fields */
    fun set(
        posX: Float, posY: Float,
        lef_square: Float, top_square: Float,
        right: Float, bottom: Float,
        width_sqaure: Float, height_square: Float,
        raduis: Float
    ) {
        this.posX = posX
        this.posY = posY
        this.lef_square = lef_square
        this.top_square = top_square
        this.right = right
        this.bottom = bottom
        this.width_sqaure = width_sqaure
        this.height_square = height_square
        this.raduis = raduis
    }
}
