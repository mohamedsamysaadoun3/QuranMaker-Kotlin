package hazem.nurmontage.videoquran.ui.gallery_photos

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import hazem.nurmontage.videoquran.databinding.ActivityGalleryPickerVideoBinding

class GalleryPickerOneImage : AppCompatActivity() {

    private lateinit var binding: ActivityGalleryPickerVideoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGalleryPickerVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // TODO: Implement full activity logic
    }
}
