package hazem.nurmontage.videoquran.ui.gallery

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.tabs.TabLayout
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.adapter.VideoGalleryAdapter
import hazem.nurmontage.videoquran.adapter.VideoFolder
import hazem.nurmontage.videoquran.adapter.VideoItem
import hazem.nurmontage.videoquran.core.base.BaseActivity
import hazem.nurmontage.videoquran.databinding.ActivityGalleryPickerVideoBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Activity for picking video files from the device gallery.
 *
 * Shows videos in a grid with folder tabs.
 * Returns the selected video URI via RESULT_OK.
 *
 * Output (via result intent extras):
 *   - "video_uri" (String) — path to the selected video file
 */
class GalleryPickerVideo : BaseActivity() {

    private lateinit var binding: ActivityGalleryPickerVideoBinding
    private lateinit var videoAdapter: VideoGalleryAdapter

    private val folders = mutableListOf<VideoFolder>()
    private var currentFolderIndex: Int = -1

    companion object {
        const val EXTRA_VIDEO_URI = "video_uri"
    }

    /** Permission request launcher for media access. */
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        if (allGranted) {
            loadVideos()
        } else {
            Toast.makeText(this, "Permission denied. Cannot access videos.", Toast.LENGTH_LONG).show()
            binding.viewProgress.visibility = View.GONE
            binding.toSetting.visibility = View.VISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGalleryPickerVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setStatusBarColor()

        // Back button
        binding.btnOnBack.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }

        // Done button (select current video — handled per-item click)
        binding.tvDone.setOnClickListener {
            // Done is only actionable after a video is selected via item click
        }

        // Setup RecyclerView
        videoAdapter = VideoGalleryAdapter { videoItem ->
            onVideoSelected(videoItem)
        }

        binding.rv.apply {
            layoutManager = GridLayoutManager(this@GalleryPickerVideo, 3)
            adapter = videoAdapter
        }

        // Tab layout for folder selection
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val position = tab.position
                if (position >= 0 && position < folders.size) {
                    currentFolderIndex = position
                    showFolderVideos(folders[position])
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        // Folders text button
        binding.tvFolders.setOnClickListener {
            if (binding.tabLayout.visibility == View.VISIBLE) {
                binding.tabLayout.visibility = View.GONE
            } else {
                binding.tabLayout.visibility = View.VISIBLE
            }
        }

        // Check permissions and load videos
        checkPermissionsAndLoad()
    }

    /**
     * Check if the required permissions are granted and load videos.
     */
    private fun checkPermissionsAndLoad() {
        val permissions = getRequiredPermissions()
        val allGranted = permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }

        if (allGranted) {
            loadVideos()
        } else {
            permissionLauncher.launch(permissions)
        }
    }

    /**
     * Get the required permissions based on API level.
     */
    private fun getRequiredPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_VIDEO
            )
        } else {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    }

    /**
     * Load video files from the MediaStore in the background.
     */
    private fun loadVideos() {
        binding.viewProgress.visibility = View.VISIBLE
        binding.tvFolders.visibility = View.INVISIBLE

        lifecycleScope.launch(Dispatchers.IO) {
            val videoFolders = VideoGalleryAdapter.queryVideos(this@GalleryPickerVideo)

            withContext(Dispatchers.Main) {
                binding.viewProgress.visibility = View.GONE
                binding.tvFolders.visibility = View.VISIBLE

                folders.clear()
                folders.addAll(videoFolders)

                setupFolderTabs()

                // Show the first folder's videos
                if (folders.isNotEmpty()) {
                    currentFolderIndex = 0
                    showFolderVideos(folders[0])
                }
            }
        }
    }

    /**
     * Set up the TabLayout with folder names.
     */
    private fun setupFolderTabs() {
        binding.tabLayout.removeAllTabs()

        if (folders.size <= 1) {
            binding.tabLayout.visibility = View.GONE
            binding.tvFolders.visibility = View.INVISIBLE
            return
        }

        for (folder in folders) {
            val tab = binding.tabLayout.newTab()
            tab.text = folder.name
            binding.tabLayout.addTab(tab)
        }

        binding.tvFolders.visibility = View.VISIBLE
    }

    /**
     * Display the videos from the specified folder in the RecyclerView.
     */
    private fun showFolderVideos(folder: VideoFolder) {
        videoAdapter.submitList(folder.videos)
    }

    /**
     * Handle video selection — return the video path to the caller.
     */
    private fun onVideoSelected(videoItem: VideoItem) {
        val resultIntent = Intent().apply {
            putExtra(EXTRA_VIDEO_URI, videoItem.path)
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }
}
