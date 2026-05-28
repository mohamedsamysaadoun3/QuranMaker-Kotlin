package hazem.nurmontage.videoquran.ui.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import hazem.nurmontage.videoquran.databinding.ActivityAboutBinding

class AboutActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnOnBack.setOnClickListener { finish() }
        try {
            val pi = packageManager.getPackageInfo(packageName, 0)
            binding.tvVersion?.text = "NurMontage v${pi.versionName}"
        } catch (_: Exception) {}
    }
}
