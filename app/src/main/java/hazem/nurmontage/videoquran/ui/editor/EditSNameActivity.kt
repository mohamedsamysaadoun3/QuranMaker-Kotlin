package hazem.nurmontage.videoquran.ui.editor

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import hazem.nurmontage.videoquran.databinding.ActivityEditSnameBinding

class EditS_NameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditSnameBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditSnameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // TODO: Implement full activity logic
    }
}
