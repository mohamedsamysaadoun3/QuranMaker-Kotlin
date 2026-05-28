package hazem.nurmontage.videoquran.effects

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator

/**
 * Keyframe-based animation with start and end keyframes for properties
 * like position, scale, and opacity.
 *
 * Define start and end [Keyframe] objects, set a duration, and optionally
 * enable repeating mode. The [KeyframeListener] receives interpolated
 * values on each frame.
 */
class KeyframeAnimator {

    companion object {
        private const val TAG = "KeyframeAnimator"
    }

    /**
     * Represents a single keyframe with a time position and property values.
     */
    data class Keyframe(
        val fraction: Float,   // 0.0 to 1.0
        val x: Float,
        val y: Float,
        val scaleX: Float,
        val scaleY: Float,
        val rotation: Float,
        val alpha: Float
    ) {
        companion object {
            /** Create a keyframe at fraction 0.0 (start of animation). */
            @JvmStatic
            fun atStart(x: Float, y: Float, scaleX: Float, scaleY: Float,
                        rotation: Float, alpha: Float): Keyframe =
                Keyframe(0f, x, y, scaleX, scaleY, rotation, alpha)

            /** Create a keyframe at fraction 1.0 (end of animation). */
            @JvmStatic
            fun atEnd(x: Float, y: Float, scaleX: Float, scaleY: Float,
                      rotation: Float, alpha: Float): Keyframe =
                Keyframe(1f, x, y, scaleX, scaleY, rotation, alpha)
        }
    }

    /** Listener for animation frame updates. */
    interface KeyframeListener {
        /** Called on each animation frame with interpolated property values. */
        fun onFrame(x: Float, y: Float, scaleX: Float, scaleY: Float, rotation: Float, alpha: Float)
        /** Called when the animation completes (not called for repeating animations). */
        fun onAnimationComplete()
    }

    private var startKeyframe: Keyframe? = null
    private var endKeyframe: Keyframe? = null
    private var durationMs: Long = 1000L
    private var animator: ValueAnimator? = null
    private var listener: KeyframeListener? = null
    private var isRepeating: Boolean = false

    fun setStartKeyframe(keyframe: Keyframe?) {
        this.startKeyframe = keyframe
    }

    fun setEndKeyframe(keyframe: Keyframe?) {
        this.endKeyframe = keyframe
    }

    fun setDuration(durationMs: Long) {
        this.durationMs = durationMs
    }

    fun setListener(listener: KeyframeListener?) {
        this.listener = listener
    }

    fun setRepeating(repeating: Boolean) {
        this.isRepeating = repeating
    }

    /** Start the keyframe animation. */
    fun start() {
        stop()

        val start = startKeyframe ?: return
        val end = endKeyframe ?: return

        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = durationMs
            if (isRepeating) {
                repeatCount = ValueAnimator.INFINITE
                repeatMode = ValueAnimator.RESTART
            }

            addUpdateListener { animation ->
                val theListener = listener ?: return@addUpdateListener
                val theStart = startKeyframe ?: return@addUpdateListener
                val theEnd = endKeyframe ?: return@addUpdateListener

                val fraction = animation.animatedValue as Float
                theListener.onFrame(
                    lerp(theStart.x, theEnd.x, fraction),
                    lerp(theStart.y, theEnd.y, fraction),
                    lerp(theStart.scaleX, theEnd.scaleX, fraction),
                    lerp(theStart.scaleY, theEnd.scaleY, fraction),
                    lerp(theStart.rotation, theEnd.rotation, fraction),
                    lerp(theStart.alpha, theEnd.alpha, fraction)
                )
            }

            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    listener?.onAnimationComplete()
                }
            })
        }

        animator?.start()
    }

    /** Stop the animation. */
    fun stop() {
        animator?.let {
            if (it.isRunning) it.cancel()
        }
        animator = null
    }

    /** Check if the animation is currently running. */
    fun isRunning(): Boolean = animator?.isRunning == true

    private fun lerp(start: Float, end: Float, fraction: Float): Float =
        start + (end - start) * fraction

    /** Release resources. */
    fun release() {
        stop()
        listener = null
    }
}
