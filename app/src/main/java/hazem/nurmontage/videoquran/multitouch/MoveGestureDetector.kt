package hazem.nurmontage.videoquran.multitouch

import android.content.Context
import android.graphics.PointF
import android.view.MotionEvent

/**
 * Detects move (drag / pan) gestures with one or more pointers.
 *
 * Tracks the focal point of all pointers and computes the delta
 * between consecutive events. The listener receives move begin,
 * ongoing move, and move end callbacks with the accumulated focus
 * position and per-step delta.
 *
 * Inherits from [BaseGestureDetector] directly (not [TwoFingerGestureDetector])
 * because a move gesture works with any number of pointers.
 */
class MoveGestureDetector(
    context: Context,
    private val mListener: OnMoveGestureListener
) : BaseGestureDetector(context) {

    /** Listener interface for move gesture callbacks. */
    interface OnMoveGestureListener {
        /** Called when a move is ongoing. Return true to continue receiving events. */
        fun onMove(detector: MoveGestureDetector): Boolean

        /** Called when a move gesture begins. Return true to accept, false to reject. */
        fun onMoveBegin(detector: MoveGestureDetector): Boolean

        /** Called when the move gesture ends. */
        fun onMoveEnd(detector: MoveGestureDetector)
    }

    /** Convenience class with no-op defaults for [OnMoveGestureListener]. */
    open class SimpleOnMoveGestureListener : OnMoveGestureListener {
        override fun onMove(detector: MoveGestureDetector): Boolean = false
        override fun onMoveBegin(detector: MoveGestureDetector): Boolean = true
        override fun onMoveEnd(detector: MoveGestureDetector) {}
    }

    /** Accumulated external focus position (pan offset). */
    private val mFocusExternal = PointF()

    /** Delta of the focus position since the last move event. */
    private var mFocusDeltaExternal: PointF = FOCUS_DELTA_ZERO

    /** Focal point of the current event (internal, computed per event). */
    private var mCurrFocusInternal: PointF? = null

    /** Focal point of the previous event (internal, computed per event). */
    private var mPrevFocusInternal: PointF? = null

    override fun handleStartProgressEvent(action: Int, event: MotionEvent) {
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                resetState()
                mPrevEvent = MotionEvent.obtain(event)
                mTimeDelta = 0L
                updateStateByEvent(event)
            }
            MotionEvent.ACTION_MOVE -> {
                mGestureInProgress = mListener.onMoveBegin(this)
            }
        }
    }

    override fun handleInProgressEvent(action: Int, event: MotionEvent) {
        when (action) {
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {
                mListener.onMoveEnd(this)
                resetState()
            }
            MotionEvent.ACTION_MOVE -> {
                updateStateByEvent(event)
                // Pressure check: reject if finger lifted too much
                if (mCurrPressure / mPrevPressure > PRESSURE_THRESHOLD && mListener.onMove(this)) {
                    mPrevEvent?.recycle()
                    mPrevEvent = MotionEvent.obtain(event)
                }
            }
        }
    }

    override fun updateStateByEvent(event: MotionEvent) {
        super.updateStateByEvent(event)
        val prev = mPrevEvent!!

        mCurrFocusInternal = determineFocalPoint(event)
        mPrevFocusInternal = determineFocalPoint(prev)

        // Compute focus delta only when pointer count hasn't changed
        mFocusDeltaExternal = if (prev.pointerCount != event.pointerCount) {
            FOCUS_DELTA_ZERO
        } else {
            PointF(
                mCurrFocusInternal!!.x - mPrevFocusInternal!!.x,
                mCurrFocusInternal!!.y - mPrevFocusInternal!!.y
            )
        }

        // Accumulate external focus
        mFocusExternal.x += mFocusDeltaExternal.x
        mFocusExternal.y += mFocusDeltaExternal.y
    }

    /**
     * Computes the centroid of all active pointers in the given event.
     * This is the "focal point" of the touch cluster.
     */
    private fun determineFocalPoint(event: MotionEvent): PointF {
        val count = event.pointerCount
        var sumX = 0f
        var sumY = 0f
        for (i in 0 until count) {
            sumX += event.getX(i)
            sumY += event.getY(i)
        }
        val n = count.toFloat()
        return PointF(sumX / n, sumY / n)
    }

    /** The accumulated X position of the focus (pan offset). */
    fun getFocusX(): Float = mFocusExternal.x

    /** The accumulated Y position of the focus (pan offset). */
    fun getFocusY(): Float = mFocusExternal.y

    /** The focus delta since the last move event. */
    fun getFocusDelta(): PointF = mFocusDeltaExternal

    companion object {
        /** Reusable zero-delta point to avoid allocations. */
        private val FOCUS_DELTA_ZERO = PointF()
    }
}
