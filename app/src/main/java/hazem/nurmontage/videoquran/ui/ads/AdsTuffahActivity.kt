package hazem.nurmontage.videoquran.ui.ads

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import hazem.nurmontage.videoquran.databinding.ActivityAdsTuufahBinding

class AdsTuffahActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdsTuufahBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdsTuufahBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // TODO: Implement full activity logic
    }
}
