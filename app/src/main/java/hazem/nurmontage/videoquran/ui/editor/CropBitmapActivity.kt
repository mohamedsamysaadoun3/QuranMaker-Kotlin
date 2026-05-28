package hazem.nurmontage.videoquran.ui.editor

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import hazem.nurmontage.videoquran.databinding.ActivityCropBitmapBinding

class CropBitmapActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCropBitmapBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCropBitmapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // TODO: Implement full activity logic
    }
}
