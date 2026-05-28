package hazem.nurmontage.videoquran.ui.share

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import hazem.nurmontage.videoquran.databinding.ActivityShareWithMeBinding

class ShareWithMeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityShareWithMeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShareWithMeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // TODO: Implement full activity logic
    }
}
