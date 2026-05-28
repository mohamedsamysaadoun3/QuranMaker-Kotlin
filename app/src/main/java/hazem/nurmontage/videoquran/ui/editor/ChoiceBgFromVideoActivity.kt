package hazem.nurmontage.videoquran.ui.editor

import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.core.base.BaseActivity
import hazem.nurmontage.videoquran.databinding.ActivityChoiceBgFromVideoBinding
import hazem.nurmontage.videoquran.utils.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * Activity for selecting a video frame as a background image.
 *
 * Flow:
 *   1. Receives a video URI via intent extra "video_uri"
 *   2. Shows the video preview and a frame scrubber
 *   3. User scrubs to the desired frame
 *   4. On "Done", extracts the frame bitmap and saves it to internal storage
 *   5. Returns the saved frame URI and bg_type via RESULT_OK
 */
class ChoiceBgFromVideoActivity : BaseActivity() {

    private lateinit var binding: ActivityChoiceBgFromVideoBinding

    private var videoUri: String? = null
    private var videoDurationUs: Long = 0L
    private var currentFrameTimeUs: Long = 0L
    private var currentBitmap: Bitmap? = null
    private var retriever: MediaMetadataRetriever? = null

    companion object {
        const val EXTRA_VIDEO_URI = "video_uri"
        const val EXTRA_BG_URI = "bg_uri"
        const val EXTRA_BG_TYPE = "bg_type"
        const val BG_TYPE_VIDEO_FRAME = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChoiceBgFromVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setStatusBarColor()
        hideSystemBars()

        videoUri = intent.getStringExtra(EXTRA_VIDEO_URI)

        if (videoUri.isNullOrEmpty()) {
            // Also try to get from intent data URI
            val dataUri = intent.data?.toString()
            if (dataUri.isNullOrEmpty()) {
                Toast.makeText(this, "No video provided", Toast.LENGTH_SHORT).show()
                finish()
                return
            }
            videoUri = dataUri
        }

        binding.tvTittleFragment.text = getString(R.string.app_name)

        binding.btnCancel.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }

        binding.btnDone.setOnClickListener {
            saveFrameAndReturn()
        }

        initVideoRetriever()
    }

    /**
     * Initialize the MediaMetadataRetriever and set up the frame selector.
     */
    private fun initVideoRetriever() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                retriever = MediaMetadataRetriever()
                retriever?.setDataSource(videoUri)

                val durationStr = retriever?.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                videoDurationUs = (durationStr?.toLongOrNull() ?: 0L) * 1000L // ms to us

                withContext(Dispatchers.Main) {
                    setupFrameSelector()
                    showFrameAt(0L)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ChoiceBgFromVideoActivity,
                        "Failed to load video",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }
        }
    }

    /**
     * Set up the VideoFrameSelectorView with callback for frame scrubbing.
     */
    private fun setupFrameSelector() {
        binding.frameSelectorView.setOnFrameSeekListener(object : hazem.nurmontage.videoquran.views.VideoFrameSelectorView.OnFrameSeekListener {
            override fun onSeekTo(timeUs: Long) {
                currentFrameTimeUs = timeUs.coerceIn(0L, videoDurationUs)
                showFrameAt(currentFrameTimeUs)
            }
        })
        binding.frameSelectorView.setDuration(videoDurationUs)
        videoUri?.let { binding.frameSelectorView.setVideoPath(it) }
    }

    /**
     * Display the video frame at the specified time position.
     */
    private fun showFrameAt(timeUs: Long) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val bitmap = retriever?.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                bitmap?.let {
                    val oldBitmap = currentBitmap
                    currentBitmap = it
                    oldBitmap?.recycle()
                    withContext(Dispatchers.Main) {
                        binding.ivView.setImageBitmap(it)
                    }
                }
            } catch (_: Exception) {
                // Frame extraction failed for this timestamp — keep previous frame
            }
        }
    }

    /**
     * Save the currently selected frame to internal storage and return the result.
     */
    private fun saveFrameAndReturn() {
        val bitmap = currentBitmap
        if (bitmap == null) {
            Toast.makeText(this, "No frame selected", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val workDir = FileUtils.getFile(this@ChoiceBgFromVideoActivity)
                val frameFile = File(workDir, "bg_frame_${System.currentTimeMillis()}.png")

                FileOutputStream(frameFile).use { fos ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                    fos.flush()
                }

                withContext(Dispatchers.Main) {
                    val resultIntent = Intent().apply {
                        putExtra(EXTRA_BG_URI, frameFile.absolutePath)
                        putExtra(EXTRA_BG_TYPE, BG_TYPE_VIDEO_FRAME)
                    }
                    setResult(RESULT_OK, resultIntent)
                    finish()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ChoiceBgFromVideoActivity,
                        "Failed to save frame",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            retriever?.release()
        } catch (_: Exception) {
            // Retriever may already be released
        }
        retriever = null
        currentBitmap?.recycle()
        currentBitmap = null
    }
}
