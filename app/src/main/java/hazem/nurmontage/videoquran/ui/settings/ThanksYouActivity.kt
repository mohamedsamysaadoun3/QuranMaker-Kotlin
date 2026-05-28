package hazem.nurmontage.videoquran.ui.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import hazem.nurmontage.videoquran.databinding.ActivityThanksYouBinding

class ThanksYouActivity : AppCompatActivity() {
    private lateinit var binding: ActivityThanksYouBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityThanksYouBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnOnBack.setOnClickListener { finish() }
    }
}
