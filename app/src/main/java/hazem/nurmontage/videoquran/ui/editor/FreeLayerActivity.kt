package hazem.nurmontage.videoquran.ui.editor

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import hazem.nurmontage.videoquran.databinding.ActivityTimeLineBinding

class FreeLayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTimeLineBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTimeLineBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // TODO: Implement full activity logic
    }
}
