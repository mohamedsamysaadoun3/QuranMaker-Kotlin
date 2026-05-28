package hazem.nurmontage.videoquran.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import hazem.nurmontage.videoquran.databinding.RowWorkUserBinding
import hazem.nurmontage.videoquran.ui.home.WorkUserActivity.ProjectItem
import java.io.File

/**
 * RecyclerView adapter for the Home screen's saved projects list.
 *
 * Uses ViewBinding ([RowWorkUserBinding]) and Glide for video thumbnail loading.
 * Clean version — no billing, no premium checks.
 */
class WorkUserAdapter(
    private val items: List<ProjectItem>,
    private val onItemClick: (ProjectItem) -> Unit
) : RecyclerView.Adapter<WorkUserAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: RowWorkUserBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ProjectItem) {
            binding.tvName.text = item.name
            binding.tvDate.text = "${item.date}  •  ${item.size}"

            // Load video thumbnail with Glide
            val videoFile = File(item.path)
            if (videoFile.exists()) {
                Glide.with(binding.root.context)
                    .load(videoFile)
                    .centerCrop()
                    .into(binding.imageView)
            }

            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RowWorkUserBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}
