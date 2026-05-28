package hazem.nurmontage.videoquran.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.model.ExploreItem
import hazem.nurmontage.videoquran.views.image.SquareImageViewSimple
import hazem.nurmontage.videoquran.views.text.TextCustumFont
import java.io.File

/**
 * RecyclerView Adapter for displaying browsable file-system folders in the media explorer.
 *
 * Originally: ExploreAdabters.java (preserved typo in original package)
 * Converted to: ExploreAdabters.kt — idiomatic Kotlin, full logic preserved
 *
 * Features:
 * - Displays folders from the device's file system as a grid of thumbnail cards
 * - Each item shows a thumbnail preview image, folder name, and folder size
 * - Thumbnails are loaded from the first media file inside each folder via Glide
 * - Override size for thumbnail loading is controlled by the [size] parameter
 * - Square image thumbnails via [SquareImageViewSimple] for uniform grid layout
 * - Click on a folder fires [IExplore.folder] callback with the File object,
 *   folder name, and path string — allowing the hosting Activity to navigate
 *   into the selected folder
 * - Placeholder image shown while thumbnails are loading
 *
 * @property exploreItems List of file-system folder entries to display
 * @property size Override dimension (in pixels) for Glide thumbnail loading
 * @property iExplore Callback interface for folder navigation events
 * @property folderSelect The currently selected folder path (for highlighting)
 */
class ExploreAdabters(
    private val exploreItems: List<ExploreItem>?,
    private val size: Int,
    private val iExplore: IExplore?,
    private val folderSelect: String
) : RecyclerView.Adapter<ExploreAdabters.MyViewHolder>() {

    /** Callback interface for folder navigation events in the explorer. */
    interface IExplore {
        /** Called when the navigation or loading is complete. */
        fun done()

        /**
         * Called when a folder is clicked for navigation.
         * @param folder The File object of the selected folder
         * @param name The display name of the folder
         * @param path The absolute path string of the folder
         */
        fun folder(folder: File, name: String, path: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_explore, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = exploreItems?.get(position) ?: return

        // Load folder thumbnail from the first media file inside the folder
        Glide.with(holder.itemView)
            .load(item.firstFilePath)
            .override(size, size)
            .centerCrop()
            .placeholder(R.drawable.image_24px)
            .into(holder.imageView)

        holder.tvName.text = item.name
        holder.tvSize.text = item.size
    }

    override fun getItemCount(): Int = exploreItems?.size ?: 0

    /**
     * ViewHolder for folder explorer items.
     * Handles click events for folder navigation.
     */
    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: SquareImageViewSimple = itemView.findViewById(R.id.img)
        val tvName: TextCustumFont = itemView.findViewById(R.id.tv_name)
        val tvSize: TextCustumFont = itemView.findViewById(R.id.tv_size)

        init {
            itemView.setOnClickListener {
                val callback = iExplore ?: return@setOnClickListener
                val pos = adapterPosition
                if (pos == -1) return@setOnClickListener

                exploreItems?.get(pos)?.let { item ->
                    callback.folder(item.folder, item.name, item.path)
                }
            }
        }
    }
}
