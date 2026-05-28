package hazem.nurmontage.videoquran.dragdrop

import android.animation.ValueAnimator
import android.view.View
import android.view.animation.OvershootInterpolator
import kotlin.math.sin

/**
 * Spring-based animations for drag feedback.
 * Uses ValueAnimator with OvershootInterpolator as a spring approximation
 * (since AndroidX DynamicAnimation is not available).
 */
class DragAnimation {

    companion object {
        private const val PICK_UP_DURATION_MS = 250L
        private const val DROP_DURATION_MS = 300L
        private const val CANCEL_DURATION_MS = 200L
        private const val REJECT_DURATION_MS = 350L

        private const val PICK_UP_SCALE = 1.1f
        private const val PICK_UP_ELEVATION = 12f
        private const val NORMAL_SCALE = 1.0f
        private const val NORMAL_ELEVATION = 2f
    }

    private var currentAnimator: ValueAnimator? = null

    /**
     * Animate a view being picked up (scale up + elevation).
     */
    fun animatePickUp(view: View?) {
        if (view == null) return
        cancel()

        // Scale animation
        val scaleAnim = ValueAnimator.ofFloat(NORMAL_SCALE, PICK_UP_SCALE).apply {
            duration = PICK_UP_DURATION_MS
            interpolator = OvershootInterpolator(1.2f)
            addUpdateListener { animation ->
                val scale = animation.animatedValue as Float
                view.scaleX = scale
                view.scaleY = scale
            }
        }

        // Elevation animation
        val elevAnim = ValueAnimator.ofFloat(NORMAL_ELEVATION, PICK_UP_ELEVATION).apply {
            duration = PICK_UP_DURATION_MS
            addUpdateListener { animation ->
                val elevation = animation.animatedValue as Float
                view.elevation = elevation
            }
        }

        scaleAnim.start()
        elevAnim.start()
        currentAnimator = scaleAnim
    }

    /**
     * Animate a view being dropped into place (scale down + elevation reset)
     * with an optional callback when the animation completes.
     */
    fun animateDrop(view: View?, onComplete: (() -> Unit)? = null) {
        if (view == null) return
        cancel()

        val scaleAnim = ValueAnimator.ofFloat(PICK_UP_SCALE, NORMAL_SCALE).apply {
            duration = DROP_DURATION_MS
            interpolator = OvershootInterpolator(0.8f)
            addUpdateListener { animation ->
                val scale = animation.animatedValue as Float
                view.scaleX = scale
                view.scaleY = scale
            }
        }

        val elevAnim = ValueAnimator.ofFloat(PICK_UP_ELEVATION, NORMAL_ELEVATION).apply {
            duration = DROP_DURATION_MS
            addUpdateListener { animation ->
                val elevation = animation.animatedValue as Float
                view.elevation = elevation
            }
        }

        scaleAnim.start()
        elevAnim.start()
        currentAnimator = scaleAnim
    }

    /**
     * Animate a cancel (smooth return to original position).
     */
    fun animateCancel(view: View?, originalX: Float, originalY: Float) {
        if (view == null) return
        cancel()

        val currentX = view.translationX
        val currentY = view.translationY
        val currentScale = view.scaleX

        val anim = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = CANCEL_DURATION_MS
            interpolator = OvershootInterpolator(0.5f)
            addUpdateListener { animation ->
                val fraction = animation.animatedValue as Float
                view.translationX = currentX + (0f - currentX) * fraction
                view.translationY = currentY + (0f - currentY) * fraction
                val scale = currentScale + (NORMAL_SCALE - currentScale) * fraction
                view.scaleX = scale
                view.scaleY = scale
            }
        }

        anim.start()
        currentAnimator = anim
    }

    /**
     * Animate a rejection (shake + return to original position).
     */
    fun animateReject(view: View?, originalX: Float, originalY: Float) {
        if (view == null) return
        cancel()

        val currentX = view.translationX
        val currentY = view.translationY

        // Shake effect using keyframes
        val anim = ValueAnimator.ofFloat(0f, 0.25f, 0.5f, 0.75f, 1f).apply {
            duration = REJECT_DURATION_MS
            addUpdateListener { animation ->
                val fraction = animation.animatedValue as Float
                var shakeOffset = 0f
                if (fraction < 0.5f) {
                    // Shake phase
                    val shakeT = fraction * 2f
                    shakeOffset = sin(shakeT * Math.PI * 4).toFloat() * 20f * (1f - shakeT)
                }
                val returnFraction = (fraction * 2f).coerceAtMost(1f)
                view.translationX = currentX + (0f - currentX) * returnFraction + shakeOffset
                view.translationY = currentY + (0f - currentY) * returnFraction
                val scale = view.scaleX + (NORMAL_SCALE - view.scaleX) * returnFraction
                view.scaleX = scale
                view.scaleY = scale
            }
        }

        anim.start()
        currentAnimator = anim
    }

    private fun cancel() {
        currentAnimator?.let {
            if (it.isRunning) it.cancel()
        }
        currentAnimator = null
    }

    fun release() {
        cancel()
    }
}
