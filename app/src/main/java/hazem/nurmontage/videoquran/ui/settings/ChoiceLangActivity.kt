package hazem.nurmontage.videoquran.ui.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import hazem.nurmontage.videoquran.databinding.ActivityChoiceLangBinding

class ChoiceLangActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChoiceLangBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChoiceLangBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // TODO: Implement full activity logic
    }
}
