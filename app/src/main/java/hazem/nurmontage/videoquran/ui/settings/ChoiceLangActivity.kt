package hazem.nurmontage.videoquran.ui.settings

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.databinding.ActivityChoiceLangBinding
import hazem.nurmontage.videoquran.utils.LocaleHelper

/**
 * Language selection activity — Arabic / English.
 *
 * Clean version — directly connected to [LocaleHelper] for
 * persisting and applying the language change.
 */
class ChoiceLangActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChoiceLangBinding
    private var selectedLanguage: String = LocaleHelper.getLanguage(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChoiceLangBinding.inflate(layoutInflater)
        setContentView(binding.root)

        updateSelectionUI()
        setupClickListeners()
    }

    private fun updateSelectionUI() {
        if (selectedLanguage == "ar") {
            binding.layoutArabic.background = ContextCompat.getDrawable(this, R.drawable.bg_item_selected)
            binding.layoutEnglish.background = ContextCompat.getDrawable(this, R.drawable.bg_item_unselected)
        } else {
            binding.layoutEnglish.background = ContextCompat.getDrawable(this, R.drawable.bg_item_selected)
            binding.layoutArabic.background = ContextCompat.getDrawable(this, R.drawable.bg_item_unselected)
        }
    }

    private fun setupClickListeners() {
        binding.layoutEnglish.setOnClickListener {
            selectedLanguage = "en"
            updateSelectionUI()
        }

        binding.layoutArabic.setOnClickListener {
            selectedLanguage = "ar"
            updateSelectionUI()
        }

        binding.btnConfirm.setOnClickListener {
            if (selectedLanguage != LocaleHelper.getLanguage(this)) {
                LocaleHelper.setLocale(selectedLanguage)
                LocaleHelper.userIsChoice(this)

                // Recreate the activity to apply the new locale
                recreate()
                Toast.makeText(this, R.string.language_changed, Toast.LENGTH_SHORT).show()

                // Restart the app to apply locale everywhere
                val intent = packageManager.getLaunchIntentForPackage(packageName)
                intent?.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK or
                    android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            } else {
                finish()
            }
        }

        binding.tvCancel.setOnClickListener {
            finish()
        }
    }
}
