package hazem.nurmontage.videoquran.ui.gallery_photos

import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.adapter.WorkUserAdapter
import hazem.nurmontage.videoquran.databinding.ActivityGalleryPickerVideoBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * GalleryPickerOneImage — Pick a single image from the device gallery.
 *
 * Reuses the gallery picker video layout which has:
 *   - btn_onBack: Close button
 *   - rv: RecyclerView for grid display
 *   - view_progress: Loading indicator
 *   - tv_done: Done/confirm button
 *
 * Flow:
 *   1. Activity loads device images via MediaStore
 *   2. Images displayed as thumbnail grid
 *   3. User selects one image → returns URI as result
 */
class GalleryPickerOneImage : AppCompatActivity() {

    private lateinit var binding: ActivityGalleryPickerVideoBinding
    private var selectedImageUri: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGalleryPickerVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Back button
        binding.btnOnBack.setOnClickListener { finish() }

        // Done button — return selected image URI
        binding.tvDone.setOnClickListener {
            if (selectedImageUri != null) {
                val resultIntent = Intent().apply {
                    // Use setData() instead of putExtra() — the caller (EngineActivity)
                    // reads the URI via data.data (intent.data), not from extras
                    data = android.net.Uri.parse(selectedImageUri)
                }
                setResult(RESULT_OK, resultIntent)
            }
            finish()
        }

        // Setup grid layout for image gallery
        binding.rv.layoutManager = GridLayoutManager(this, 3)

        // Load images from MediaStore
        loadGalleryImages()
    }

    /**
     * Load all images from the device's MediaStore and display them.
     */
    private fun loadGalleryImages() {
        binding.viewProgress.visibility = View.VISIBLE

        lifecycleScope.launch(Dispatchers.IO) {
            val images = mutableListOf<ImageItem>()

            val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATA
            )

            val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

            contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val name = cursor.getString(nameColumn)
                    val data = cursor.getString(dataColumn)
                    val uri = android.content.ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id
                    )
                    images.add(ImageItem(uri.toString(), name, data))
                }
            }

            withContext(Dispatchers.Main) {
                binding.rv.adapter = GalleryImageAdapter(images) { imageItem ->
                    selectedImageUri = imageItem.uri
                }
                binding.viewProgress.visibility = View.GONE
            }
        }
    }

    /**
     * Data class representing a gallery image item.
     */
    data class ImageItem(
        val uri: String,
        val name: String,
        val path: String
    )

    /**
     * RecyclerView adapter for displaying image thumbnails in a grid.
     */
    private class GalleryImageAdapter(
        private val images: List<ImageItem>,
        private val onSelect: (ImageItem) -> Unit
    ) : androidx.recyclerview.widget.RecyclerView.Adapter<GalleryImageAdapter.ViewHolder>() {

        class ViewHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view)

        override fun onCreateViewHolder(
            parent: android.view.ViewGroup,
            viewType: Int
        ): ViewHolder {
            val imageView = android.widget.ImageView(parent.context).apply {
                layoutParams = androidx.recyclerview.widget.GridLayoutManager.LayoutParams(
                    androidx.recyclerview.widget.GridLayoutManager.LayoutParams.MATCH_PARENT,
                    300
                )
                scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
                setPadding(2, 2, 2, 2)
            }
            return ViewHolder(imageView)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val imageView = holder.itemView as android.widget.ImageView
            val image = images[position]

            com.bumptech.glide.Glide.with(holder.itemView.context)
                .load(image.uri)
                .centerCrop()
                .into(imageView)

            holder.itemView.setOnClickListener { onSelect(image) }
        }

        override fun getItemCount() = images.size
    }
}
