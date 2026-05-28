package hazem.nurmontage.videoquran.ui.editor

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.core.base.BaseActivity
import hazem.nurmontage.videoquran.databinding.ActivityCropBitmapBinding
import hazem.nurmontage.videoquran.utils.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * Activity for cropping a bitmap/image.
 *
 * Flow:
 *   1. Receives an image URI via intent extra "image_uri" or intent data
 *   2. Loads the image into a CropView with a draggable crop rectangle
 *   3. User adjusts the crop region
 *   4. On "Done", crops the bitmap and saves it to internal storage
 *   5. Returns the cropped image path via "cropped_uri" extra with RESULT_OK
 */
class CropBitmapActivity : BaseActivity() {

    private lateinit var binding: ActivityCropBitmapBinding
    private var sourceBitmap: Bitmap? = null

    companion object {
        const val EXTRA_IMAGE_URI = "image_uri"
        const val EXTRA_CROPPED_URI = "cropped_uri"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCropBitmapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setStatusBarColor()

        binding.btnCancel.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }

        binding.btnDone.setOnClickListener {
            cropAndReturn()
        }

        loadImage()
    }

    /**
     * Load the source image from the intent URI.
     */
    private fun loadImage() {
        val imageUri = intent.getStringExtra(EXTRA_IMAGE_URI)
            ?: intent.data?.toString()

        if (imageUri.isNullOrEmpty()) {
            Toast.makeText(this, "No image provided", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.tvTittleFragment.text = getString(R.string.app_name)

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val bitmap = loadBitmapFromUri(imageUri)
                if (bitmap == null) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@CropBitmapActivity, "Failed to load image", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    return@launch
                }

                sourceBitmap = bitmap

                withContext(Dispatchers.Main) {
                    binding.cropView.bitmap = bitmap
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@CropBitmapActivity, "Failed to load image", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    /**
     * Load a bitmap from a file path or content URI string.
     */
    private fun loadBitmapFromUri(uriString: String): Bitmap? {
        return try {
            if (uriString.startsWith("content://") || uriString.startsWith("android.resource://")) {
                val uri = Uri.parse(uriString)
                contentResolver.openInputStream(uri)?.use { inputStream ->
                    BitmapFactory.decodeStream(inputStream)
                }
            } else {
                val file = File(uriString)
                if (file.exists()) {
                    BitmapFactory.decodeFile(uriString)
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Crop the bitmap according to the crop rectangle, save the result,
     * and return the path to the caller.
     */
    private fun cropAndReturn() {
        val srcBitmap = sourceBitmap
        if (srcBitmap == null || srcBitmap.isRecycled) {
            Toast.makeText(this, "No image to crop", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val cropRect = binding.cropView.getCropRectOnBitmap()

                // Ensure valid crop bounds
                val left = cropRect.left.toInt().coerceIn(0, srcBitmap.width - 1)
                val top = cropRect.top.toInt().coerceIn(0, srcBitmap.height - 1)
                val right = cropRect.right.toInt().coerceIn(left + 1, srcBitmap.width)
                val bottom = cropRect.bottom.toInt().coerceIn(top + 1, srcBitmap.height)

                val width = right - left
                val height = bottom - top

                if (width <= 0 || height <= 0) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@CropBitmapActivity, "Invalid crop region", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                val croppedBitmap = Bitmap.createBitmap(srcBitmap, left, top, width, height)

                // Save to internal storage
                val workDir = FileUtils.getFile(this@CropBitmapActivity)
                val croppedFile = File(workDir, "cropped_${System.currentTimeMillis()}.png")

                FileOutputStream(croppedFile).use { fos ->
                    croppedBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                    fos.flush()
                }

                croppedBitmap.recycle()

                withContext(Dispatchers.Main) {
                    val resultIntent = Intent().apply {
                        putExtra(EXTRA_CROPPED_URI, croppedFile.absolutePath)
                    }
                    setResult(RESULT_OK, resultIntent)
                    finish()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@CropBitmapActivity,
                        "Failed to crop image",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sourceBitmap?.recycle()
        sourceBitmap = null
    }
}
