package hazem.nurmontage.videoquran.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.databinding.ActivitySeettingBinding
import hazem.nurmontage.videoquran.utils.LocaleHelper

/**
 * Settings activity — CLEAN version.
 *
 * Only contains the "Switch Language" option (Arabic/English).
 * All billing, pro, rate, share, youtuber, ads, and copyright options removed.
 * Connected to [LocaleHelper] for language persistence.
 */
class SeettingActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySeettingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySeettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupLanguageButton()
        hideRemovedOptions()
        setupVersionText()
    }

    private fun setupToolbar() {
        binding.btnOnBack.setOnClickListener {
            finish()
        }
    }

    private fun setupLanguageButton() {
        binding.btnLang.setOnClickListener {
            val intent = Intent(this, ChoiceLangActivity::class.java)
            startActivity(intent)
        }
    }

    private fun hideRemovedOptions() {
        // Hide billing/pro options
        binding.btnToPro.visibility = View.GONE
        binding.btnRestore.visibility = View.GONE

        // Hide about, more apps, share, rate, youtuber, copyright, social links
        binding.btnAbout.visibility = View.GONE
        binding.btnMoreApp.visibility = View.GONE
        binding.btnShare.visibility = View.GONE
        binding.btnRateApp.visibility = View.GONE
        binding.btnImBloger.visibility = View.GONE
        binding.btnCopyRight.visibility = View.GONE

        // Hide social media buttons
        binding.btnInstagram.visibility = View.GONE
        binding.btnYoutbe.visibility = View.GONE
        binding.btnTicktock.visibility = View.GONE
        binding.btnWhatsap.visibility = View.GONE

        // Hide Instagram promo section
        binding.toInstagram.visibility = View.GONE
    }

    private fun setupVersionText() {
        try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            binding.tvVersion.text = getString(R.string.app_name) + " v${packageInfo.versionName}"
        } catch (e: Exception) {
            binding.tvVersion.text = getString(R.string.app_name)
        }
    }
}
