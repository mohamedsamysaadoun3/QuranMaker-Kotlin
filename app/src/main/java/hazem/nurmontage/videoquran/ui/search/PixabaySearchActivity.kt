package hazem.nurmontage.videoquran.ui.search

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.adapter.ColorAdapter
import hazem.nurmontage.videoquran.databinding.ActivityGalleryPickerVideoBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * PixabaySearchActivity — Search and select images from Pixabay API.
 *
 * Reuses the gallery picker layout which has:
 *   - btn_onBack: Close button
 *   - rv: RecyclerView for grid display
 *   - view_progress: Loading indicator
 *   - tv_done: Done/confirm button
 *
 * Flow:
 *   1. User enters search query
 *   2. Activity searches Pixabay API
 *   3. Results displayed as image grid
 *   4. User selects image → returns URI as result
 */
class PixabaySearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGalleryPickerVideoBinding

    private val pixabayApiKey = "" // Set your Pixabay API key here
    private val imageUrls = mutableListOf<String>()
    private var selectedUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGalleryPickerVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Back button
        binding.btnOnBack.setOnClickListener { finish() }

        // Done button — return selected image
        binding.tvDone.setOnClickListener {
            if (selectedUrl != null) {
                val resultIntent = Intent().apply {
                    putExtra("pixabay_url", selectedUrl)
                }
                setResult(RESULT_OK, resultIntent)
            }
            finish()
        }

        // Setup RecyclerView
        binding.rv.layoutManager = GridLayoutManager(this, 3)
        binding.rv.adapter = PixabayAdapter(imageUrls) { url ->
            selectedUrl = url
        }

        // Show search hint
        binding.viewProgress.visibility = View.GONE
    }

    /**
     * Search Pixabay for images matching the query.
     */
    private fun searchPixabay(query: String) {
        if (pixabayApiKey.isEmpty()) {
            Toast.makeText(this, "Pixabay API key not configured", Toast.LENGTH_SHORT).show()
            return
        }

        binding.viewProgress.visibility = View.VISIBLE
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val url = URL(
                    "https://pixabay.com/api/?key=$pixabayApiKey&q=${java.net.URLEncoder.encode(query, "UTF-8")}" +
                    "&image_type=photo&per_page=50&safesearch=true"
                )
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 10000
                connection.readTimeout = 10000

                val response = connection.inputStream.bufferedReader().readText()
                val json = JSONObject(response)

                val hits = json.getJSONArray("hits")
                val urls = mutableListOf<String>()
                for (i in 0 until hits.length()) {
                    val hit = hits.getJSONObject(i)
                    urls.add(hit.getString("webformatURL"))
                }

                withContext(Dispatchers.Main) {
                    imageUrls.clear()
                    imageUrls.addAll(urls)
                    binding.rv.adapter?.notifyDataSetChanged()
                    binding.viewProgress.visibility = View.GONE
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.viewProgress.visibility = View.GONE
                    Toast.makeText(this@PixabaySearchActivity, "Search failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * Simple adapter for displaying Pixabay image URLs in a grid.
     */
    private class PixabayAdapter(
        private val urls: List<String>,
        private val onSelect: (String) -> Unit
    ) : RecyclerView.Adapter<PixabayAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
            val imageView = android.widget.ImageView(parent.context).apply {
                layoutParams = android.widget.GridLayoutManager.LayoutParams(
                    android.widget.GridLayoutManager.LayoutParams.MATCH_PARENT,
                    300
                )
                scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
                setPadding(2, 2, 2, 2)
            }
            return ViewHolder(imageView)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val imageView = holder.itemView as android.widget.ImageView
            val url = urls[position]
            com.bumptech.glide.Glide.with(holder.itemView.context)
                .load(url)
                .centerCrop()
                .into(imageView)
            holder.itemView.setOnClickListener { onSelect(url) }
        }

        override fun getItemCount() = urls.size
    }
}
