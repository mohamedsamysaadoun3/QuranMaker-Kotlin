package hazem.nurmontage.videoquran.multitouch

import android.content.Context
import android.view.MotionEvent

/**
 * Base abstract class for all gesture detectors.
 * Provides core state tracking (previous/current events, pressure, time delta)
 * and the template method pattern for handling gesture lifecycle.
 *
 * Subclasses must implement [handleStartProgressEvent] and [handleInProgressEvent]
 * to define their specific gesture recognition logic.
 */
abstract class BaseGestureDetector(protected val mContext: Context) {

    companion object {
        /** Minimum pressure ratio to consider the touch valid. */
        protected const val PRESSURE_THRESHOLD = 0.67f
    }

    /** Previous motion event (recycled on reset). */
    protected var mPrevEvent: MotionEvent? = null

    /** Current motion event (recycled on update). */
    protected var mCurrEvent: MotionEvent? = null

    /** Whether a gesture is currently in progress. */
    protected var mGestureInProgress: Boolean = false

    /** Time difference between current and previous event in milliseconds. */
    protected var mTimeDelta: Long = 0L

    /** Pressure of the current touch point. */
    protected var mCurrPressure: Float = 0f

    /** Pressure of the previous touch point. */
    protected var mPrevPressure: Float = 0f

    /**
     * Called when a motion event arrives while no gesture is in progress.
     * Subclasses use this to detect the start of their specific gesture.
     *
     * @param action   The masked action from [MotionEvent.getAction].
     * @param event    The raw [MotionEvent].
     */
    protected abstract fun handleStartProgressEvent(action: Int, event: MotionEvent)

    /**
     * Called when a motion event arrives while a gesture is already in progress.
     * Subclasses use this to track and update their specific gesture.
     *
     * @param action   The masked action from [MotionEvent.getAction].
     * @param event    The raw [MotionEvent].
     */
    protected abstract fun handleInProgressEvent(action: Int, event: MotionEvent)

    /**
     * Main touch event dispatcher. Routes the event to either
     * [handleStartProgressEvent] or [handleInProgressEvent] depending on state.
     * Only processes single-pointer events at this base level; multi-pointer
     * events are handled by subclasses that override this method.
     *
     * @param event The raw [MotionEvent] from the view.
     * @return Always true to indicate the event was consumed.
     */
    open fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.pointerCount > 1) {
            return false
        }
        val action = event.action and MotionEvent.ACTION_MASK
        if (!mGestureInProgress) {
            handleStartProgressEvent(action, event)
        } else {
            handleInProgressEvent(action, event)
        }
        return true
    }

    /**
     * Updates internal state based on the incoming [MotionEvent].
     * Recycles the previous current event, obtains a new one, and computes
     * time delta and pressure values.
     *
     * Must be called by subclasses after they handle their own state.
     *
     * @param event The raw [MotionEvent].
     */
    protected open fun updateStateByEvent(event: MotionEvent) {
        val prev = mPrevEvent
        val curr = mCurrEvent
        if (curr != null) {
            curr.recycle()
            mCurrEvent = null
        }
        mCurrEvent = MotionEvent.obtain(event)
        mTimeDelta = event.eventTime - prev!!.eventTime
        mCurrPressure = event.getPressure(event.actionIndex)
        mPrevPressure = prev.getPressure(prev.actionIndex)
    }

    /**
     * Resets all internal state. Recycles held [MotionEvent] objects
     * and clears the in-progress flag.
     */
    protected open fun resetState() {
        mPrevEvent?.recycle()
        mPrevEvent = null
        mCurrEvent?.recycle()
        mCurrEvent = null
        mGestureInProgress = false
    }

    /** Whether a gesture is currently being tracked. */
    fun isInProgress(): Boolean = mGestureInProgress

    /** Time delta between the last two events in milliseconds. */
    fun getTimeDelta(): Long = mTimeDelta

    /** Event time of the current [MotionEvent]. */
    fun getEventTime(): Long = mCurrEvent!!.eventTime
}
