package hazem.nurmontage.videoquran.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

/**
 * Haptic feedback helper for the timeline editor.
 *
 * Provides vibration feedback when the user performs actions like:
 * - Reaching trim handle boundaries
 * - Snapping to grid positions
 * - Long-press on entities
 *
 * Uses [VibrationEffect.createOneShot] on API 26+ and the deprecated
 * [Vibrator.vibrate] on older devices.
 *
 * Converted from MyVibrationHelper.java — vibration logic preserved exactly.
 */
class MyVibrationHelper(context: Context) {

    private val vibrator: Vibrator? =
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator

    /**
     * Vibrate for the default duration (30ms).
     */
    fun vibrate() {
        vibrate(30L)
    }

    /**
     * Vibrate for the specified [durationMs].
     *
     * On API 26+ (Oreo), uses [VibrationEffect.createOneShot] with
     * default intensity (-1 = VibrationEffect.DEFAULT_AMPLITUDE).
     * On older devices, falls back to the deprecated but functional
     * [Vibrator.vibrate] method.
     *
     * @param durationMs Vibration duration in milliseconds
     */
    fun vibrate(durationMs: Long) {
        vibrator?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                it.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                it.vibrate(durationMs)
            }
        }
    }

    /**
     * Cancel any ongoing vibration immediately.
     */
    fun cancelVibration() {
        vibrator?.cancel()
    }
}
