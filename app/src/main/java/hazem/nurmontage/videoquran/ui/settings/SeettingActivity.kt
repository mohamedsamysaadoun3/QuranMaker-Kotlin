package hazem.nurmontage.videoquran.ui.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import hazem.nurmontage.videoquran.databinding.ActivitySeettingBinding

class SeettingActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySeettingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySeettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // TODO: Implement full activity logic
    }
}
