package hazem.nurmontage.videoquran.ui.share

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.core.base.BaseActivity
import hazem.nurmontage.videoquran.databinding.ActivityShareWithMeBinding
import hazem.nurmontage.videoquran.ui.engine.EngineActivity
import hazem.nurmontage.videoquran.utils.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * Activity for handling incoming shared media from other apps.
 *
 * Accepts SEND intent with the following MIME types:
 *   - audio/* — Audio files (recitations, nasheeds, etc.)
 *   - video/* — Video files (backgrounds, clips)
 *   - image/* — Image files (backgrounds, overlays)
 *
 * Flow:
 *   1. Receives the shared content URI from the intent
 *   2. Copies the shared file to internal storage for reliable access
 *   3. Launches EngineActivity with the shared URI as template background
 */
class ShareWithMeActivity : BaseActivity() {

    private lateinit var binding: ActivityShareWithMeBinding

    companion object {
        const val EXTRA_SHARED_URI = "shared_uri"
        const val EXTRA_SHARED_TYPE = "shared_type"

        const val TYPE_IMAGE = 0
        const val TYPE_VIDEO = 1
        const val TYPE_AUDIO = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShareWithMeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        handleSharedContent()
    }

    /**
     * Process the incoming shared content from the intent.
     */
    private fun handleSharedContent() {
        val intent = intent
        val action = intent?.action

        var sharedUri: Uri? = null
        var sharedType: Int = TYPE_IMAGE

        when (action) {
            Intent.ACTION_SEND -> {
                // Single item shared
                sharedUri = if (intent.clipData != null && intent.clipData!!.itemCount > 0) {
                    intent.clipData!!.getItemAt(0).uri
                } else {
                    intent.getParcelableExtra(Intent.EXTRA_STREAM)
                }

                // Determine media type from MIME type
                val mimeType = intent.type ?: ""
                sharedType = when {
                    mimeType.startsWith("video/") -> TYPE_VIDEO
                    mimeType.startsWith("audio/") -> TYPE_AUDIO
                    else -> TYPE_IMAGE // Default to image for image/* and anything else
                }
            }
            Intent.ACTION_VIEW -> {
                // View intent (e.g., opening a file)
                sharedUri = intent.data
                val mimeType = intent.type ?: ""
                sharedType = when {
                    mimeType.startsWith("video/") -> TYPE_VIDEO
                    mimeType.startsWith("audio/") -> TYPE_AUDIO
                    else -> TYPE_IMAGE
                }
            }
            else -> {
                // Try to get URI from data
                sharedUri = intent?.data
            }
        }

        if (sharedUri == null) {
            Toast.makeText(this, "No content to share", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Copy shared file to internal storage and launch engine
        processSharedFile(sharedUri, sharedType)
    }

    /**
     * Copy the shared content URI to internal storage for reliable access,
     * then launch EngineActivity with the file path.
     */
    private fun processSharedFile(sourceUri: Uri, mediaType: Int) {
        binding.progressHorizontal.visibility = android.view.View.VISIBLE

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Persist URI permission
                try {
                    contentResolver.takePersistableUriPermission(
                        sourceUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (_: Exception) {
                    // Not all URIs support persistent permissions
                }

                // Determine file extension based on MIME type
                val extension = when (mediaType) {
                    TYPE_VIDEO -> ".mp4"
                    TYPE_AUDIO -> ".mp3"
                    else -> ".png"
                }

                // Try to get the original file name
                val fileName = getFileName(sourceUri) ?: "shared_${System.currentTimeMillis()}$extension"
                val safeFileName = if (fileName.contains(".")) fileName else "$fileName$extension"

                // Copy to internal storage
                val workDir = FileUtils.getFile(this@ShareWithMeActivity)
                val destFile = File(workDir, safeFileName)

                contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                    FileOutputStream(destFile).use { outputStream ->
                        val buffer = ByteArray(8192)
                        var bytesRead: Int
                        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                            outputStream.write(buffer, 0, bytesRead)
                        }
                        outputStream.flush()
                    }
                }

                withContext(Dispatchers.Main) {
                    binding.progressHorizontal.visibility = android.view.View.GONE
                    launchEngine(destFile.absolutePath, mediaType)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.progressHorizontal.visibility = android.view.View.GONE
                    Toast.makeText(
                        this@ShareWithMeActivity,
                        "Failed to process shared content",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }
        }
    }

    /**
     * Get the display name of a content URI.
     */
    private fun getFileName(uri: Uri): String? {
        var name: String? = null
        try {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0) {
                        name = cursor.getString(nameIndex)
                    }
                }
            }
        } catch (_: Exception) {
            // Query failed
        }

        if (name == null) {
            name = uri.lastPathSegment
        }

        return name
    }

    /**
     * Launch the EngineActivity with the shared file as template background.
     */
    private fun launchEngine(filePath: String, mediaType: Int) {
        val intent = Intent(this, EngineActivity::class.java).apply {
            putExtra(EXTRA_SHARED_URI, filePath)
            putExtra(EXTRA_SHARED_TYPE, mediaType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(intent)
        finish()
    }
}
