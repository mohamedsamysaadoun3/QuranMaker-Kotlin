package hazem.nurmontage.videoquran.dragdrop

import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs
import kotlin.math.round

/**
 * Snap-to-grid and snap-to-entity-boundary behavior for drag operations.
 * Configurable grid size, snap threshold, and snap strength.
 *
 * Also provides drag-position snapping for ItemTouchHelper callbacks
 * and snap-position calculation for drop targets.
 */
class SnapHelper {

    /** Grid cell size in pixels. */
    var gridSize: Int = 8

    /** Maximum distance (px) at which snapping activates. */
    var snapThreshold: Float = 16f

    /** Snap strength from 0.0 (no snap) to 1.0 (full snap). Clamped on set. */
    var snapStrength: Float = 0.8f
        set(value) { field = value.coerceIn(0f, 1f) }

    /** Whether snap-to-grid is enabled. */
    var isSnapToGrid: Boolean = true

    /** Whether snap-to-entity-boundary is enabled. */
    var isSnapToEntities: Boolean = true

    constructor()

    constructor(gridSize: Int, snapThreshold: Float, snapStrength: Float) {
        this.gridSize = gridSize
        this.snapThreshold = snapThreshold
        this.snapStrength = snapStrength.coerceIn(0f, 1f)
        this.isSnapToGrid = true
        this.isSnapToEntities = true
    }

    /**
     * Calculate the snapped X position based on current settings.
     */
    fun snapX(x: Float): Float {
        if (!isSnapToGrid) return x

        val nearestGrid = round(x / gridSize) * gridSize
        val distance = abs(x - nearestGrid)

        return if (distance <= snapThreshold) {
            x + (nearestGrid - x) * snapStrength
        } else {
            x
        }
    }

    /**
     * Calculate the snapped Y position based on current settings.
     */
    fun snapY(y: Float): Float {
        if (!isSnapToGrid) return y

        val nearestGrid = round(y / gridSize) * gridSize
        val distance = abs(y - nearestGrid)

        return if (distance <= snapThreshold) {
            y + (nearestGrid - y) * snapStrength
        } else {
            y
        }
    }

    /**
     * Snap to the nearest entity boundary (edge of another view).
     *
     * @param currentPos      The current position
     * @param entityPositions Array of entity edge positions to snap to
     * @return The snapped position
     */
    fun snapToEntity(currentPos: Float, entityPositions: FloatArray?): Float {
        if (!isSnapToEntities || entityPositions == null || entityPositions.isEmpty()) {
            return currentPos
        }

        var nearest = currentPos
        var minDistance = snapThreshold

        for (pos in entityPositions) {
            val distance = abs(currentPos - pos)
            if (distance < minDistance) {
                minDistance = distance
                nearest = pos
            }
        }

        return if (nearest != currentPos) {
            currentPos + (nearest - currentPos) * snapStrength
        } else {
            currentPos
        }
    }

    /**
     * Calculate the snap position for an adapter index after a drop.
     * Snaps the index to the nearest grid-aligned position based on [gridSize].
     *
     * @param position The raw adapter position after drop.
     * @return The snapped position (may be the same if no snapping needed).
     */
    fun calculateSnapPosition(position: Int): Int {
        if (!isSnapToGrid) return position
        val gridStep = gridSize.coerceAtLeast(1)
        val snapped = round(position.toFloat() / gridStep) * gridStep
        return snapped.coerceAtLeast(0)
    }

    /**
     * Snap the drag displacement (dX, dY) during an ItemTouchHelper drag.
     * Applies grid snapping to the translation offsets.
     *
     * @param dX          Horizontal displacement from ItemTouchHelper
     * @param dY          Vertical displacement from ItemTouchHelper
     * @param viewHolder  The ViewHolder being dragged (for context)
     * @return FloatArray of [snappedDX, snappedDY]
     */
    fun snapDragPosition(
        dX: Float,
        dY: Float,
        viewHolder: RecyclerView.ViewHolder
    ): FloatArray {
        val snappedDX = snapX(dX)
        val snappedDY = snapY(dY)
        return floatArrayOf(snappedDX, snappedDY)
    }
}
