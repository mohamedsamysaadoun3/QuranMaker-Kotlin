package hazem.nurmontage.videoquran.multitouch

import android.content.Context
import android.view.MotionEvent
import kotlin.math.abs
import kotlin.math.atan2

/**
 * Detects two-finger shove (vertical push/pull) gestures.
 *
 * Extends [TwoFingerGestureDetector] to detect when both fingers
 * move vertically together. The shove delta is the change in the
 * average Y position of the two fingers.
 *
 * Adds an additional angle-based sloppiness filter: if the line
 * between the two fingers is too horizontal (angle near 0 or pi),
 * the gesture is rejected because it's likely a pinch/zoom, not a shove.
 */
class ShoveGestureDetector(
    context: Context,
    private val mListener: OnShoveGestureListener
) : TwoFingerGestureDetector(context) {

    /** Listener interface for shove gesture callbacks. */
    interface OnShoveGestureListener {
        /** Called during an ongoing shove. Return true to continue. */
        fun onShove(detector: ShoveGestureDetector): Boolean

        /** Called when a shove gesture begins. Return true to accept. */
        fun onShoveBegin(detector: ShoveGestureDetector): Boolean

        /** Called when the shove gesture ends. */
        fun onShoveEnd(detector: ShoveGestureDetector)
    }

    /** Convenience class with no-op defaults for [OnShoveGestureListener]. */
    open class SimpleOnShoveGestureListener : OnShoveGestureListener {
        override fun onShove(detector: ShoveGestureDetector): Boolean = false
        override fun onShoveBegin(detector: ShoveGestureDetector): Boolean = true
        override fun onShoveEnd(detector: ShoveGestureDetector) {}
    }

    /** Whether the current gesture is in the sloppy zone. */
    private var mSloppyGesture: Boolean = false

    /** Average Y of both fingers in the previous event. */
    private var mPrevAverageY: Float = 0f

    /** Average Y of both fingers in the current event. */
    private var mCurrAverageY: Float = 0f

    override fun handleStartProgressEvent(action: Int, event: MotionEvent) {
        when (action) {
            MotionEvent.ACTION_POINTER_DOWN -> {
                // Second finger just touched — initialize gesture
                resetState()
                mPrevEvent = MotionEvent.obtain(event)
                mTimeDelta = 0L
                updateStateByEvent(event)

                mSloppyGesture = isSloppyGesture(event)
                if (!mSloppyGesture) {
                    mGestureInProgress = mListener.onShoveBegin(this)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (mSloppyGesture) {
                    mSloppyGesture = isSloppyGesture(event)
                    if (mSloppyGesture) return
                    mGestureInProgress = mListener.onShoveBegin(this)
                }
            }
        }
    }

    override fun handleInProgressEvent(action: Int, event: MotionEvent) {
        when (action) {
            MotionEvent.ACTION_MOVE -> {
                updateStateByEvent(event)
                // Pressure check + minimum pixel threshold + listener acceptance
                if (mCurrPressure / mPrevPressure > PRESSURE_THRESHOLD
                    && abs(getShovePixelsDelta()) > 0.5f
                    && mListener.onShove(this)
                ) {
                    mPrevEvent?.recycle()
                    mPrevEvent = MotionEvent.obtain(event)
                }
            }
            MotionEvent.ACTION_CANCEL -> {
                if (!mSloppyGesture) {
                    mListener.onShoveEnd(this)
                }
                resetState()
            }
            MotionEvent.ACTION_POINTER_UP -> {
                updateStateByEvent(event)
                if (!mSloppyGesture) {
                    mListener.onShoveEnd(this)
                }
                resetState()
            }
        }
    }

    override fun updateStateByEvent(event: MotionEvent) {
        super.updateStateByEvent(event)
        val prev = mPrevEvent!!
        mPrevAverageY = (prev.getY(0) + prev.getY(1)) / 2f
        mCurrAverageY = (event.getY(0) + event.getY(1)) / 2f
    }

    /**
     * Determines whether the gesture is sloppy.
     *
     * In addition to the edge-slop check from [TwoFingerGestureDetector],
     * this also rejects gestures where the angle between the two fingers
     * is too horizontal (near 0 degrees or 180 degrees), since those are
     * pinch/zoom gestures, not shoves. A shove requires the fingers to be
     * roughly vertical (angle near 90 degrees).
     */
    override fun isSloppyGesture(event: MotionEvent): Boolean {
        if (super.isSloppyGesture(event)) return true

        val angle = abs(atan2(mCurrFingerDiffY.toDouble(), mCurrFingerDiffX.toDouble()))
        // Accept only if the angle is roughly vertical (between ~20 deg and ~160 deg)
        // Reject if near-horizontal: 0-20 deg or 160-180 deg
        val isNearHorizontal = (angle in 0.0..0.3499999940395355)
                || (angle in 2.7899999618530273..Math.PI)
        return isNearHorizontal
    }

    /**
     * Returns the vertical shove delta in pixels since the last event.
     * Positive = fingers moved down, negative = fingers moved up.
     */
    fun getShovePixelsDelta(): Float = mCurrAverageY - mPrevAverageY

    override fun resetState() {
        super.resetState()
        mSloppyGesture = false
        mPrevAverageY = 0f
        mCurrAverageY = 0f
    }
}
