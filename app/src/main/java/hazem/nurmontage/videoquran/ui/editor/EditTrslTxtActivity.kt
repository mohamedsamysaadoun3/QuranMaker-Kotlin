package hazem.nurmontage.videoquran.ui.editor

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import hazem.nurmontage.videoquran.databinding.ActivityEditTrslBinding

class EditTrslTxtActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditTrslBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditTrslBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnOnBack?.setOnClickListener { finish() }
    }
}
