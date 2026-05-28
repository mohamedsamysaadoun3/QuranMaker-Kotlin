package hazem.nurmontage.videoquran.ui.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.core.base.BaseActivity
import hazem.nurmontage.videoquran.databinding.ActivityYoutuberBinding
import hazem.nurmontage.videoquran.utils.DrawableHelper

/**
 * Data class representing a YouTuber/tutorial creator profile.
 */
data class YoutuberItem(
    val name: String,
    val handle: String,
    val platformId: String,
    val profileImageUrl: String? = null,
    val channelUrl: String
)

/**
 * Activity for displaying a list of YouTuber/tutorial creator profiles.
 *
 * Shows social media links and allows users to visit their channels.
 * This is an informational screen — no result is returned.
 */
class YoutuberActivity : BaseActivity() {

    private lateinit var binding: ActivityYoutuberBinding
    private lateinit var adapter: YoutuberAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityYoutuberBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setStatusBarColor()

        // Back button
        binding.btnOnBack.setOnClickListener {
            finish()
        }

        // Send link / add tutorial button
        binding.btnSendLnk.setOnClickListener {
            openAddTutorialLink()
        }

        // Setup RecyclerView
        adapter = YoutuberAdapter(getYoutubers()) { item ->
            openChannel(item.channelUrl)
        }

        binding.rv.apply {
            layoutManager = LinearLayoutManager(this@YoutuberActivity)
            adapter = adapter
        }
    }

    /**
     * Open the "add your tutorial" link.
     */
    private fun openAddTutorialLink() {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/201017036022"))
            startActivity(intent)
        } catch (_: Exception) {
            // No browser available
        }
    }

    /**
     * Open a YouTube/social media channel URL.
     */
    private fun openChannel(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (_: Exception) {
            // No browser available
        }
    }

    /**
     * Returns the list of featured YouTuber/tutorial creators.
     */
    private fun getYoutubers(): List<YoutuberItem> {
        return listOf(
            YoutuberItem(
                name = "Helal Tube",
                handle = "@helaltube",
                platformId = "y_16:9",
                channelUrl = "https://www.youtube.com/@helaltube"
            ),
            YoutuberItem(
                name = "Mohammed Qasadi",
                handle = "@mohammed_qasadi",
                platformId = "y_16:9",
                channelUrl = "https://www.youtube.com/@mohammed_qasadi"
            ),
            YoutuberItem(
                name = "Hecham",
                handle = "@he_x55",
                platformId = "t",
                channelUrl = "https://www.tiktok.com/@he_x55"
            ),
            YoutuberItem(
                name = "Earn with Asmat",
                handle = "@EarnwithAsmat",
                platformId = "y_16:9",
                channelUrl = "https://www.youtube.com/@EarnwithAsmat"
            )
        )
    }

    /**
     * RecyclerView adapter for displaying YouTuber profile cards.
     */
    inner class YoutuberAdapter(
        private val items: List<YoutuberItem>,
        private val onItemClick: (YoutuberItem) -> Unit
    ) : RecyclerView.Adapter<YoutuberAdapter.ViewHolder>() {

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvName: TextView = itemView.findViewById(R.id.tv_feature)
            val ivPlatform: ImageView = itemView.findViewById(R.id.btn_radio_year)

            init {
                itemView.setOnClickListener {
                    val pos = adapterPosition
                    if (pos != RecyclerView.NO_POSITION) {
                        onItemClick(items[pos])
                    }
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.row_feature, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.tvName.text = "${item.name}  ${item.handle}"
            holder.ivPlatform.setImageResource(DrawableHelper.getIdResource(item.platformId))
        }

        override fun getItemCount(): Int = items.size
    }
}
