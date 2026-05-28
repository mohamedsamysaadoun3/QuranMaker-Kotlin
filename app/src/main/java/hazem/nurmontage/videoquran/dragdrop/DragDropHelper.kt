package hazem.nurmontage.videoquran.dragdrop

import android.content.Context
import android.graphics.Canvas
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

/**
 * DragDropHelper provides enhanced drag & drop functionality for RecyclerView items.
 * Extends ItemTouchHelper.Callback with vibration feedback, smooth animations, and
 * custom drag visual feedback for timeline entity reordering.
 *
 * Features:
 * - Haptic feedback on drag start, position change, and drop
 * - Visual elevation change during drag
 * - Configurable drag directions (vertical list / horizontal timeline)
 * - Callback interface for drag events
 * - Integration with SnapHelper for snap-to-grid behavior
 * - Support for drag constraints (min/max positions)
 */
class DragDropHelper(
    private val context: Context,
    private val callback: DragDropCallback
) : ItemTouchHelper.Callback() {

    companion object {
        /** Vibration duration for drag start in ms */
        private const val VIBRATE_DRAG_START = 30L

        /** Vibration duration for position swap in ms */
        private const val VIBRATE_SWAP = 15L

        /** Vibration duration for drop in ms */
        private val VIBRATE_DROP = 50L

        /** Elevation when dragging */
        private const val DRAG_ELEVATION = 12f

        /** Scale factor when dragging */
        private const val DRAG_SCALE = 1.05f
    }

    /** Callback interface for drag & drop events. */
    interface DragDropCallback {
        /** Called when items are moved. Return true if the move was successful. */
        fun onItemMove(fromPosition: Int, toPosition: Int): Boolean

        /** Called when a drag starts. */
        fun onDragStart(position: Int, viewHolder: RecyclerView.ViewHolder)

        /** Called when a drag ends. */
        fun onDragEnd(fromPosition: Int, toPosition: Int, viewHolder: RecyclerView.ViewHolder)

        /** Called when the drag position changes (for real-time preview). */
        fun onDragPositionChanged(currentPosition: Int)

        /** Called when an item is dropped at a snap point. */
        fun onSnapPosition(position: Int)
    }

    private var vibrator: Vibrator? = null
    private var snapHelper: SnapHelper? = null
    private var dragAnimation: DragAnimation = DragAnimation()

    private var isDragging: Boolean = false
    private var vibrationEnabled: Boolean = true
    private var dragFromPosition: Int = -1
    private var dragToPosition: Int = -1
    private var minDragPosition: Int = -1
    private var maxDragPosition: Int = -1

    init {
        initVibrator()
    }

    /**
     * Initialize the vibrator service.
     * Uses VibratorManager on API 31+ and falls back to Vibrator on older versions.
     */
    private fun initVibrator() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager =
                    context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibrator = vibratorManager?.defaultVibrator
            } else {
                vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (e: Exception) {
            vibrator = null
        }
    }

    /**
     * Set the SnapHelper for snap-to-grid behavior during drags.
     */
    fun setSnapHelper(snapHelper: SnapHelper) {
        this.snapHelper = snapHelper
    }

    /**
     * Set drag position constraints.
     *
     * @param minPosition Minimum position an item can be dragged to (-1 for no limit)
     * @param maxPosition Maximum position an item can be dragged to (-1 for no limit)
     */
    fun setDragConstraints(minPosition: Int, maxPosition: Int) {
        this.minDragPosition = minPosition
        this.maxDragPosition = maxPosition
    }

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        // Allow drag up and down (for vertical lists)
        var dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN

        // Allow left-right drag for horizontal timeline
        val layoutManager = recyclerView.layoutManager
        if (layoutManager != null && layoutManager.canScrollHorizontally()) {
            dragFlags = ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        }

        // No swipe by default
        val swipeFlags = 0

        return makeMovementFlags(dragFlags, swipeFlags)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        val fromPosition = viewHolder.adapterPosition
        val toPosition = target.adapterPosition

        // Check constraints
        if (minDragPosition >= 0 && toPosition < minDragPosition) {
            return false
        }
        if (maxDragPosition >= 0 && toPosition > maxDragPosition) {
            return false
        }

        if (dragFromPosition == -1) {
            dragFromPosition = fromPosition
        }
        dragToPosition = toPosition

        // Notify callback of position change
        callback.onDragPositionChanged(toPosition)

        return callback.onItemMove(fromPosition, toPosition)
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        // Not supported — no swipe
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)

        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
            isDragging = true

            // Apply visual feedback
            if (viewHolder != null) {
                val itemView = viewHolder.itemView
                itemView.elevation = DRAG_ELEVATION
                itemView.animate()
                    .scaleX(DRAG_SCALE)
                    .scaleY(DRAG_SCALE)
                    .setDuration(150)
                    .start()

                // Haptic feedback
                vibrate(VIBRATE_DRAG_START)
                callback.onDragStart(viewHolder.adapterPosition, viewHolder)
            }
        } else if (actionState == ItemTouchHelper.ACTION_STATE_IDLE) {
            isDragging = false

            if (viewHolder != null) {
                val itemView = viewHolder.itemView

                // Reset visual state with animation
                dragAnimation.animateDrop(itemView) {
                    itemView.elevation = 0f
                }

                // Haptic feedback for drop
                vibrateLong(VIBRATE_DROP)

                val finalFrom = if (dragFromPosition != -1) dragFromPosition else viewHolder.adapterPosition
                val finalTo = if (dragToPosition != -1) dragToPosition else viewHolder.adapterPosition

                callback.onDragEnd(finalFrom, finalTo, viewHolder)

                // Check snap position
                val snap = snapHelper
                if (snap != null) {
                    val snapPos = snap.calculateSnapPosition(finalTo)
                    if (snapPos != finalTo) {
                        callback.onSnapPosition(snapPos)
                    }
                }

                // Reset positions
                dragFromPosition = -1
                dragToPosition = -1
            }
        }
    }

    override fun onMoved(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        fromPos: Int,
        target: RecyclerView.ViewHolder,
        toPos: Int,
        x: Int,
        y: Int
    ) {
        super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y)

        // Light vibration on swap
        vibrate(VIBRATE_SWAP)
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        var dx = dX
        var dy = dY

        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
            // Apply snap positions if available (when finger released)
            val snap = snapHelper
            if (snap != null && !isCurrentlyActive) {
                val snapped = snap.snapDragPosition(dx, dy, viewHolder)
                dx = snapped[0]
                dy = snapped[1]
            }
        }

        super.onChildDraw(c, recyclerView, viewHolder, dx, dy, actionState, isCurrentlyActive)
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)

        // Reset all visual changes
        val itemView = viewHolder.itemView
        itemView.elevation = 0f
        itemView.scaleX = 1f
        itemView.scaleY = 1f
    }

    override fun isLongPressDragEnabled(): Boolean = true

    override fun isItemViewSwipeEnabled(): Boolean = false

    /**
     * Perform a short vibration.
     *
     * @param durationMs Duration in milliseconds
     */
    private fun vibrate(durationMs: Long) {
        val vib = vibrator ?: return
        if (!vibrationEnabled || !vib.hasVibrator()) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vib.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vib.vibrate(durationMs)
            }
        } catch (e: Exception) {
            // Ignore vibration errors
        }
    }

    /**
     * Perform a longer vibration pattern for drop.
     */
    private fun vibrateLong(durationMs: Long?) {
        val vib = vibrator ?: return
        val duration = durationMs ?: return
        if (!vibrationEnabled || !vib.hasVibrator()) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vib.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vib.vibrate(duration)
            }
        } catch (e: Exception) {
            // Ignore vibration errors
        }
    }

    // ==================== Configuration ====================

    fun setVibrationEnabled(enabled: Boolean) {
        this.vibrationEnabled = enabled
    }

    fun isDragging(): Boolean = isDragging

    fun getDragFromPosition(): Int = dragFromPosition

    fun getDragToPosition(): Int = dragToPosition

    /**
     * Attach this helper to a RecyclerView.
     *
     * @param recyclerView The RecyclerView to attach to
     */
    fun attachToRecyclerView(recyclerView: RecyclerView) {
        val touchHelper = ItemTouchHelper(this)
        touchHelper.attachToRecyclerView(recyclerView)
    }
}
