package hazem.nurmontage.videoquran.multitouch

import android.content.Context
import android.view.MotionEvent
import kotlin.math.atan2

/**
 * Detects two-finger rotation gestures.
 *
 * Extends [TwoFingerGestureDetector] to track the angle between
 * the two fingers across events. The rotation delta is computed
 * via [atan2] on the finger-diff vectors, converted to degrees.
 *
 * Includes sloppy-gesture detection: rotation will not begin while
 * either finger is too close to the screen edge.
 */
class RotateGestureDetector(
    context: Context,
    private val mListener: OnRotateGestureListener
) : TwoFingerGestureDetector(context) {

    /** Listener interface for rotation gesture callbacks. */
    interface OnRotateGestureListener {
        /** Called during an ongoing rotation. Return true to continue. */
        fun onRotate(detector: RotateGestureDetector): Boolean

        /** Called when a rotation gesture begins. Return true to accept. */
        fun onRotateBegin(detector: RotateGestureDetector): Boolean

        /** Called when the rotation gesture ends. */
        fun onRotateEnd(detector: RotateGestureDetector)
    }

    /** Convenience class with no-op defaults for [OnRotateGestureListener]. */
    open class SimpleOnRotateGestureListener : OnRotateGestureListener {
        override fun onRotate(detector: RotateGestureDetector): Boolean = false
        override fun onRotateBegin(detector: RotateGestureDetector): Boolean = true
        override fun onRotateEnd(detector: RotateGestureDetector) {}
    }

    /** Whether the current gesture is in the sloppy zone (near edges). */
    private var mSloppyGesture: Boolean = false

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
                    mGestureInProgress = mListener.onRotateBegin(this)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (mSloppyGesture) {
                    mSloppyGesture = isSloppyGesture(event)
                    if (mSloppyGesture) return
                    mGestureInProgress = mListener.onRotateBegin(this)
                }
            }
        }
    }

    override fun handleInProgressEvent(action: Int, event: MotionEvent) {
        when (action) {
            MotionEvent.ACTION_MOVE -> {
                updateStateByEvent(event)
                // Pressure check + listener acceptance
                if (mCurrPressure / mPrevPressure > PRESSURE_THRESHOLD && mListener.onRotate(this)) {
                    mPrevEvent?.recycle()
                    mPrevEvent = MotionEvent.obtain(event)
                }
            }
            MotionEvent.ACTION_CANCEL -> {
                if (!mSloppyGesture) {
                    mListener.onRotateEnd(this)
                }
                resetState()
            }
            MotionEvent.ACTION_POINTER_UP -> {
                updateStateByEvent(event)
                if (!mSloppyGesture) {
                    mListener.onRotateEnd(this)
                }
                resetState()
            }
        }
    }

    override fun resetState() {
        super.resetState()
        mSloppyGesture = false
    }

    /**
     * Returns the rotation delta in degrees since the last event.
     * Computed as the angle change between the previous and current
     * finger-diff vectors using [atan2].
     *
     * Positive values indicate clockwise rotation.
     */
    fun getRotationDegreesDelta(): Float {
        val prevAngle = atan2(mPrevFingerDiffY, mPrevFingerDiffX)
        val currAngle = atan2(mCurrFingerDiffY, mCurrFingerDiffX)
        return (prevAngle - currAngle) * 180.0f / Math.PI.toFloat()
    }
}
