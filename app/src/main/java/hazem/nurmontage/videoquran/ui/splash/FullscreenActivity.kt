package hazem.nurmontage.videoquran.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import hazem.nurmontage.videoquran.databinding.ActivityFullscreenBinding
import hazem.nurmontage.videoquran.ui.home.WorkUserActivity

/**
 * Splash / Launcher activity.
 *
 * Clean version — no purchase checks, no billing, no ads.
 * Simply animates the Quranic verse and transitions to the home screen.
 */
@SuppressLint("CustomSplashScreen")
class FullscreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFullscreenBinding
    private var keepSplash = true

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { keepSplash }

        super.onCreate(savedInstanceState)
        binding = ActivityFullscreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Hide status / navigation for immersive splash
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        )

        animateSplashAndNavigate()
    }

    private fun animateSplashAndNavigate() {
        // Fade-in the Quranic verse
        val fadeIn = AlphaAnimation(0f, 1f).apply {
            duration = SPLASH_ANIM_DURATION
            interpolator = DecelerateInterpolator()
            fillAfter = true
        }

        binding.nur.visibility = View.VISIBLE
        binding.nur.startAnimation(fadeIn)

        // Navigate to home after splash delay
        Handler(Looper.getMainLooper()).postDelayed({
            keepSplash = false
            startActivity(Intent(this, WorkUserActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }, SPLASH_TOTAL_DURATION)
    }

    companion object {
        private const val SPLASH_ANIM_DURATION = 1200L
        private const val SPLASH_TOTAL_DURATION = 2000L
    }
}
