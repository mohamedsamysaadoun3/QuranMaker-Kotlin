package hazem.nurmontage.videoquran.multitouch

import android.content.Context
import android.view.MotionEvent
import android.view.ViewConfiguration
import kotlin.math.sqrt

/**
 * Abstract base class for two-finger gesture detectors.
 *
 * Extends [BaseGestureDetector] by tracking the spatial relationship
 * between two pointer fingers — their X/Y differences and the span
 * (Euclidean distance) between them. Also provides edge-slop detection
 * to reject gestures that start too close to screen edges.
 *
 * Subclasses ([RotateGestureDetector], [ShoveGestureDetector]) use
 * the finger-diff values to compute rotation angles, shove deltas, etc.
 */
abstract class TwoFingerGestureDetector(context: Context) : BaseGestureDetector(context) {

    /** X difference between the two fingers in the previous event. */
    protected var mPrevFingerDiffX: Float = 0f

    /** Y difference between the two fingers in the previous event. */
    protected var mPrevFingerDiffY: Float = 0f

    /** X difference between the two fingers in the current event. */
    protected var mCurrFingerDiffX: Float = 0f

    /** Y difference between the two fingers in the current event. */
    protected var mCurrFingerDiffY: Float = 0f

    /** Cached span for the previous event (-1 = not yet computed). */
    private var mPrevLen: Float = -1f

    /** Cached span for the current event (-1 = not yet computed). */
    private var mCurrLen: Float = -1f

    /** Edge slop from [ViewConfiguration] — ignored zone near screen edges. */
    private val mEdgeSlop: Float = ViewConfiguration.get(context).scaledEdgeSlop.toFloat()

    /** Right edge of the acceptable touch zone. */
    private var mRightSlopEdge: Float = 0f

    /** Bottom edge of the acceptable touch zone. */
    private var mBottomSlopEdge: Float = 0f

    override fun updateStateByEvent(event: MotionEvent) {
        super.updateStateByEvent(event)
        val prev = mPrevEvent!!

        // Invalidate cached spans
        mCurrLen = -1f
        mPrevLen = -1f

        // Previous finger diff
        val prevX0 = prev.getX(0)
        val prevY0 = prev.getY(0)
        val prevX1 = prev.getX(1)
        mPrevFingerDiffX = prevX1 - prevX0
        mPrevFingerDiffY = prev.getY(1) - prevY0

        // Current finger diff
        val currX0 = event.getX(0)
        val currY0 = event.getY(0)
        val currX1 = event.getX(1)
        mCurrFingerDiffX = currX1 - currX0
        mCurrFingerDiffY = event.getY(1) - currY0
    }

    /**
     * Returns the Euclidean distance (span) between the two fingers
     * in the current event. Lazy-computed and cached.
     */
    fun getCurrentSpan(): Float {
        if (mCurrLen == -1f) {
            val dx = mCurrFingerDiffX
            val dy = mCurrFingerDiffY
            mCurrLen = sqrt(dx * dx + dy * dy)
        }
        return mCurrLen
    }

    /**
     * Returns the Euclidean distance (span) between the two fingers
     * in the previous event. Lazy-computed and cached.
     */
    fun getPreviousSpan(): Float {
        if (mPrevLen == -1f) {
            val dx = mPrevFingerDiffX
            val dy = mPrevFingerDiffY
            mPrevLen = sqrt(dx * dx + dy * dy)
        }
        return mPrevLen
    }

    /**
     * Determines whether the current gesture is "sloppy" — i.e., at least
     * one finger is too close to the screen edge, which can cause
     * unintended behavior. Subclasses may override to add extra checks.
     */
    protected open fun isSloppyGesture(event: MotionEvent): Boolean {
        val displayMetrics = mContext.resources.displayMetrics
        mRightSlopEdge = displayMetrics.widthPixels - mEdgeSlop
        mBottomSlopEdge = displayMetrics.heightPixels - mEdgeSlop

        val edgeSlop = mEdgeSlop
        val rightEdge = mRightSlopEdge
        val bottomEdge = mBottomSlopEdge

        val rawX0 = event.rawX
        val rawY0 = event.rawY
        val rawX1 = getRawX(event, 1)
        val rawY1 = getRawY(event, 1)

        val pointer0Sloppy = rawX0 < edgeSlop || rawY0 < edgeSlop || rawX0 > rightEdge || rawY0 > bottomEdge
        val pointer1Sloppy = rawX1 < edgeSlop || rawY1 < edgeSlop || rawX1 > rightEdge || rawY1 > bottomEdge

        // Either pointer being sloppy makes the gesture sloppy
        return (pointer0Sloppy && pointer1Sloppy) || pointer0Sloppy || pointer1Sloppy
    }

    companion object {
        /**
         * Computes the raw X coordinate for a pointer at the given index.
         * [MotionEvent.getRawX] only returns data for pointer 0, so we
         * compute the offset from local coordinates for other pointers.
         */
        @JvmStatic
        protected fun getRawX(event: MotionEvent, pointerIndex: Int): Float {
            val offset = event.x - event.rawX
            return if (pointerIndex < event.pointerCount) {
                event.getX(pointerIndex) + offset
            } else {
                0f
            }
        }

        /**
         * Computes the raw Y coordinate for a pointer at the given index.
         * Same offset technique as [getRawX].
         */
        @JvmStatic
        protected fun getRawY(event: MotionEvent, pointerIndex: Int): Float {
            val offset = event.y - event.rawY
            return if (pointerIndex < event.pointerCount) {
                event.getY(pointerIndex) + offset
            } else {
                0f
            }
        }
    }
}
