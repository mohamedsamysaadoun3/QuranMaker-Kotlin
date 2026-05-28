package hazem.nurmontage.videoquran.ui.settings

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.core.base.BaseActivity
import hazem.nurmontage.videoquran.databinding.ActivityThanksYouBinding
import nl.dionsegijn.konfetti.models.Shape
import nl.dionsegijn.konfetti.models.Size

/**
 * Thank you / credits screen.
 *
 * Displays a celebratory message with confetti animation.
 * This is an informational screen — no result is returned.
 */
class ThanksYouActivity : BaseActivity() {

    private lateinit var binding: ActivityThanksYouBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityThanksYouBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setStatusBarColor()

        // Back button
        binding.btnOnBack.setOnClickListener {
            finish()
        }

        // Set donation text
        try {
            val pi = packageManager.getPackageInfo(packageName, 0)
            binding.tvPriceDonate.text = getString(R.string.donate_hint, pi.versionName)
        } catch (_: Exception) {
            binding.tvPriceDonate.text = getString(R.string.donate_hint, "")
        }

        binding.tvThnksDonate.text = getString(R.string.thanks_hint)

        // Launch confetti animation after layout is complete
        binding.konfettiView.post {
            startConfetti()
        }
    }

    /**
     * Start the confetti celebration animation.
     */
    private fun startConfetti() {
        try {
            binding.konfettiView.build()
                .addColors(
                    android.graphics.Color.YELLOW,
                    android.graphics.Color.GREEN,
                    android.graphics.Color.MAGENTA,
                    android.graphics.Color.RED,
                    android.graphics.Color.CYAN
                )
                .setDirection(0.0, 359.0)
                .setSpeed(1f, 5f)
                .setFadeOutEnabled(true)
                .setTimeToLive(2000L)
                .addShapes(Shape.Square, Shape.Circle)
                .addSizes(Size(12), Size(16), Size(20))
                .setPosition(
                    binding.konfettiView.width * 0.5f,
                    binding.konfettiView.height * 0.5f
                )
                .burst(100)

            // Second burst after a short delay
            Handler(Looper.getMainLooper()).postDelayed({
                try {
                    binding.konfettiView.build()
                        .addColors(
                            android.graphics.Color.BLUE,
                            android.graphics.Color.GREEN,
                            android.graphics.Color.RED,
                            android.graphics.Color.YELLOW
                        )
                        .setDirection(0.0, 359.0)
                        .setSpeed(2f, 6f)
                        .setFadeOutEnabled(true)
                        .setTimeToLive(2500L)
                        .addShapes(Shape.Circle, Shape.Square)
                        .addSizes(Size(10), Size(14), Size(18))
                        .setPosition(
                            binding.konfettiView.width * 0.3f,
                            binding.konfettiView.height * 0.5f
                        )
                        .burst(80)
                } catch (_: Exception) {
                    // Konfetti view may be detached
                }
            }, 500)
        } catch (_: Exception) {
            // Konfetti library may not be properly initialized
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // KonfettiView handles its own cleanup
    }
}
