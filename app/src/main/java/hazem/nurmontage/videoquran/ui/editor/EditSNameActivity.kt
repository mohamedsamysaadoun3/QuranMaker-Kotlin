package hazem.nurmontage.videoquran.ui.editor

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import hazem.nurmontage.videoquran.databinding.ActivityEditSnameBinding

class EditSNameActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditSnameBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditSnameBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnOnBack?.setOnClickListener { finish() }
    }
}
