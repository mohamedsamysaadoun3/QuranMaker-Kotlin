package hazem.nurmontage.videoquran.ui.editor

import android.net.Uri
import android.os.Bundle
import android.widget.MediaController
import android.widget.Toast
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.core.base.BaseActivity
import hazem.nurmontage.videoquran.databinding.ActivityPlayVideoBinding

/**
 * Simple video playback preview activity.
 *
 * Accepts a video URI (as string path or content URI) and plays it
 * in a VideoView with standard media controls.
 *
 * Input (via intent extras):
 *   - "video_path" (String) — file path to the video
 *   - "video_uri" (String) — content URI string for the video
 *   - Or intent data URI
 */
class PlayVideoActivity : BaseActivity() {

    private lateinit var binding: ActivityPlayVideoBinding
    private var mediaController: MediaController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setStatusBarColor()
        hideSystemBars()

        // Back button
        binding.btnOnBack.setOnClickListener {
            finish()
        }

        // Get video path/URI from intent
        val videoPath = intent.getStringExtra("video_path")
            ?: intent.getStringExtra("video_uri")
            ?: intent.data?.toString()

        if (videoPath.isNullOrEmpty()) {
            Toast.makeText(this, "No video to play", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupVideoView(videoPath)
    }

    /**
     * Set up the VideoView with media controls and start playback.
     */
    private fun setupVideoView(videoPath: String) {
        mediaController = MediaController(this).also {
            it.setAnchorView(binding.videoView)
            it.setMediaPlayer(binding.videoView)
        }

        binding.videoView.setMediaController(mediaController)

        binding.videoView.setOnPreparedListener { mediaPlayer ->
            mediaPlayer.setOnVideoSizeChangedListener { _, _, _ ->
                mediaController?.setAnchorView(binding.videoView)
            }
        }

        binding.videoView.setOnCompletionListener {
            // Return to start when playback completes
            binding.videoView.seekTo(0)
            mediaController?.show(0)
        }

        binding.videoView.setOnErrorListener { _, what, extra ->
            Toast.makeText(
                this,
                "Error playing video (what=$what, extra=$extra)",
                Toast.LENGTH_SHORT
            ).show()
            true // Error handled
        }

        // Set the video source
        try {
            if (videoPath.startsWith("content://") || videoPath.startsWith("http://") || videoPath.startsWith("https://")) {
                binding.videoView.setVideoURI(Uri.parse(videoPath))
            } else {
                binding.videoView.setVideoPath(videoPath)
            }
            binding.videoView.start()
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to play video", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPause() {
        super.onPause()
        if (binding.videoView.isPlaying) {
            binding.videoView.pause()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.videoView.stopPlayback()
        mediaController = null
    }
}
