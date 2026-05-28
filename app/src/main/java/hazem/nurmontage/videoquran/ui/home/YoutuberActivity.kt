package hazem.nurmontage.videoquran.ui.home

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import hazem.nurmontage.videoquran.databinding.ActivityYoutuberBinding

class YoutuberActivity : AppCompatActivity() {

    private lateinit var binding: ActivityYoutuberBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityYoutuberBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // TODO: Implement full activity logic
    }
}
