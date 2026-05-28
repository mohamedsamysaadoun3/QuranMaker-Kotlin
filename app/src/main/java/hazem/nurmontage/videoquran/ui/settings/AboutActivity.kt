package hazem.nurmontage.videoquran.ui.settings

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
import com.bumptech.glide.Glide
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.core.base.BaseActivity
import hazem.nurmontage.videoquran.databinding.ActivityAboutBinding
import hazem.nurmontage.videoquran.utils.MyPreferences

/**
 * Data class representing an about/feature item.
 */
data class AboutItem(
    val title: String,
    val description: String,
    val iconRes: Int,
    val actionUrl: String? = null
)

/**
 * About page activity showing app information, version, and credits.
 *
 * Displays a list of features/credits in a RecyclerView.
 * This is an informational screen — no result is returned.
 */
class AboutActivity : BaseActivity() {

    private lateinit var binding: ActivityAboutBinding
    private lateinit var adapter: AboutAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setStatusBarColor()

        // Mark about as viewed
        MyPreferences.putVueAbout(this)

        // Back button
        binding.btnOnBack.setOnClickListener {
            finish()
        }

        // Setup RecyclerView with about items
        adapter = AboutAdapter(getAboutItems()) { item ->
            item.actionUrl?.let { url ->
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(intent)
                } catch (_: Exception) {
                    // No browser available
                }
            }
        }

        binding.rv.apply {
            layoutManager = LinearLayoutManager(this@AboutActivity)
            adapter = adapter
        }
    }

    /**
     * Returns the list of about/feature items to display.
     */
    private fun getAboutItems(): List<AboutItem> {
        val items = mutableListOf<AboutItem>()

        // App version
        try {
            val pi = packageManager.getPackageInfo(packageName, 0)
            items.add(
                AboutItem(
                    title = "NurMontage v${pi.versionName}",
                    description = "Video Quran Maker — Create beautiful Quran videos with ease",
                    iconRes = R.drawable.nurmontage_playstore,
                    actionUrl = "https://play.google.com/store/apps/details?id=$packageName"
                )
            )
        } catch (_: Exception) {
            items.add(
                AboutItem(
                    title = "NurMontage",
                    description = "Video Quran Maker — Create beautiful Quran videos with ease",
                    iconRes = R.drawable.nurmontage_playstore
                )
            )
        }

        // Features
        items.add(
            AboutItem(
                title = "Quran Video Editor",
                description = "Create stunning Quran verse videos with beautiful backgrounds",
                iconRes = R.drawable.ic_font
            )
        )

        items.add(
            AboutItem(
                title = "Multiple Reciters",
                description = "Choose from hundreds of Quran reciters for audio",
                iconRes = R.drawable.ic_audio_track
            )
        )

        items.add(
            AboutItem(
                title = "Custom Fonts",
                description = "30+ Arabic calligraphy fonts for Quran text",
                iconRes = R.drawable.ic_palette_color
            )
        )

        items.add(
            AboutItem(
                title = "Video Backgrounds",
                description = "Use images or videos as backgrounds for your Quran clips",
                iconRes = R.drawable.ic_instagram
            )
        )

        items.add(
            AboutItem(
                title = "Developer",
                description = "Hazem NurMontage — Contact us for feedback and support",
                iconRes = R.drawable.nurmontage_playstore,
                actionUrl = "https://wa.me/201017036022"
            )
        )

        items.add(
            AboutItem(
                title = "Rate the App",
                description = "If you enjoy NurMontage, please rate us on the Play Store",
                iconRes = R.drawable.nurmontage_playstore,
                actionUrl = "https://play.google.com/store/apps/details?id=$packageName"
            )
        )

        return items
    }

    /**
     * RecyclerView adapter for displaying about/feature items.
     */
    inner class AboutAdapter(
        private val items: List<AboutItem>,
        private val onItemClick: (AboutItem) -> Unit
    ) : RecyclerView.Adapter<AboutAdapter.ViewHolder>() {

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvTitle: TextView = itemView.findViewById(R.id.tv_feature)
            val ivIcon: ImageView = itemView.findViewById(R.id.btn_radio_year)

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
            holder.tvTitle.text = item.title

            try {
                Glide.with(holder.itemView.context)
                    .load(item.iconRes)
                    .centerInside()
                    .into(holder.ivIcon)
            } catch (_: Exception) {
                holder.ivIcon.setImageResource(item.iconRes)
            }
        }

        override fun getItemCount(): Int = items.size
    }
}
