package hazem.nurmontage.videoquran.adapter

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.views.image.SquareImageView
import java.io.File
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Data class representing a video file from the device gallery.
 */
data class VideoItem(
    val id: Long,
    val uri: android.net.Uri,
    val name: String,
    val path: String,
    val duration: Long,
    val size: Long,
    val dateAdded: Long,
    val folderName: String
)

/**
 * Data class representing a folder containing videos.
 */
data class VideoFolder(
    val name: String,
    val path: String,
    val coverUri: android.net.Uri?,
    val videoCount: Int,
    val videos: MutableList<VideoItem> = mutableListOf()
)

/**
 * RecyclerView adapter for displaying video files in a grid gallery.
 *
 * Shows video thumbnails with duration overlay.
 * Supports folder-based grouping and single selection.
 */
class VideoGalleryAdapter(
    private val onItemClick: (VideoItem) -> Unit
) : RecyclerView.Adapter<VideoGalleryAdapter.ViewHolder>() {

    private val items = mutableListOf<VideoItem>()

    fun submitList(newItems: List<VideoItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_gallery_select, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val img: SquareImageView = itemView.findViewById(R.id.img)
        private val tvTime: TextView? = itemView.findViewById(R.id.tv_time)

        fun bind(item: VideoItem) {
            Glide.with(itemView.context)
                .asBitmap()
                .load(item.uri)
                .centerCrop()
                .into(img)

            tvTime?.text = formatDuration(item.duration)
            tvTime?.visibility = View.VISIBLE

            itemView.setOnClickListener {
                onItemClick(item)
            }
        }

        private fun formatDuration(durationMs: Long): String {
            val hours = TimeUnit.MILLISECONDS.toHours(durationMs)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMs) % 60
            val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMs) % 60
            return if (hours > 0) {
                String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
            }
        }
    }

    companion object {
        /**
         * Query the MediaStore for all video files on the device,
         * grouped by folder.
         */
        fun queryVideos(context: Context): List<VideoFolder> {
            val folderMap = mutableMapOf<String, VideoFolder>()

            val projection = arrayOf(
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.DATE_ADDED,
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Video.Media.BUCKET_ID
            )

            val selection = "${MediaStore.Video.Media.IS_PENDING} = 0"
            val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"

            try {
                context.contentResolver.query(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    selection,
                    null,
                    sortOrder
                )?.use { cursor ->
                    val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                    val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                    val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
                    val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                    val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
                    val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
                    val bucketCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)

                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(idCol)
                        val name = cursor.getString(nameCol) ?: continue
                        val path = cursor.getString(dataCol) ?: continue
                        val duration = cursor.getLong(durationCol)
                        val size = cursor.getLong(sizeCol)
                        val dateAdded = cursor.getLong(dateCol)
                        val folderName = cursor.getString(bucketCol) ?: "Videos"

                        if (!File(path).exists()) continue

                        val contentUri = ContentUris.withAppendedId(
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id
                        )

                        val videoItem = VideoItem(
                            id = id,
                            uri = contentUri,
                            name = name,
                            path = path,
                            duration = duration,
                            size = size,
                            dateAdded = dateAdded,
                            folderName = folderName
                        )

                        val folder = folderMap.getOrPut(folderName) {
                            VideoFolder(
                                name = folderName,
                                path = path.substringBeforeLast("/"),
                                coverUri = contentUri,
                                videoCount = 0
                            )
                        }
                        folder.videos.add(videoItem)
                        folder.videoCount = folder.videos.size
                    }
                }
            } catch (_: Exception) {
                // MediaStore query failed
            }

            return folderMap.values.sortedByDescending { it.videoCount }
        }
    }
}
