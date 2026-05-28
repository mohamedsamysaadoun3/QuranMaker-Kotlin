package hazem.nurmontage.videoquran.ui.editor

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.MediaController
import androidx.appcompat.app.AppCompatActivity
import hazem.nurmontage.videoquran.databinding.ActivityVideoViewBinding

class VideoViewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVideoViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        val videoPath = intent.getStringExtra("video_path")
        if (videoPath != null) {
            val mediaController = MediaController(this)
            mediaController.setAnchorView(binding.videoView)
            binding.videoView.setMediaController(mediaController)
            binding.videoView.setVideoPath(videoPath)
            binding.videoView.start()
        }
        binding.btnOnBack?.setOnClickListener { finish() }
    }
}
