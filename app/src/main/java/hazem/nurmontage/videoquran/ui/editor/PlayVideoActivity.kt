package hazem.nurmontage.videoquran.ui.editor

import android.os.Bundle
import android.widget.MediaController
import androidx.appcompat.app.AppCompatActivity
import hazem.nurmontage.videoquran.databinding.ActivityPlayVideoBinding

class PlayVideoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlayVideoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        val videoPath = intent.getStringExtra("video_path")
        if (videoPath != null) {
            val mc = MediaController(this)
            mc.setAnchorView(binding.videoView)
            binding.videoView.setMediaController(mc)
            binding.videoView.setVideoPath(videoPath)
            binding.videoView.start()
        }
        binding.btnOnBack?.setOnClickListener { finish() }
    }
}
