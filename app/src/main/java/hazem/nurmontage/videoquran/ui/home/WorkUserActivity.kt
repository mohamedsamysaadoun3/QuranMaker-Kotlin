package hazem.nurmontage.videoquran.ui.home

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import hazem.nurmontage.videoquran.databinding.ActivityWorkUserBinding

class WorkUserActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWorkUserBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWorkUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // TODO: Implement full activity logic
    }
}
