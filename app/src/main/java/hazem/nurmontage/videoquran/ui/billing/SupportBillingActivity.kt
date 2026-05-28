package hazem.nurmontage.videoquran.ui.billing

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import hazem.nurmontage.videoquran.databinding.ActivitySupportBillingBinding

class SupportBillingActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySupportBillingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySupportBillingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // TODO: Implement full activity logic
    }
}
