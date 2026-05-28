package hazem.nurmontage.videoquran.ui.editor.audio

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import hazem.nurmontage.videoquran.databinding.ActivityAddReaderNameBinding

class AddReaderNameActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddReaderNameBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddReaderNameBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnOnBack?.setOnClickListener { finish() }
    }
}
