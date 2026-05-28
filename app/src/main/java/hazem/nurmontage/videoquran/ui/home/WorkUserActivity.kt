package hazem.nurmontage.videoquran.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import hazem.nurmontage.videoquran.adapter.WorkUserAdapter
import hazem.nurmontage.videoquran.databinding.ActivityWorkUserBinding
import hazem.nurmontage.videoquran.ui.engine.EngineActivity
import hazem.nurmontage.videoquran.ui.settings.SeettingActivity
import hazem.nurmontage.videoquran.utils.FileUtils
import java.io.File

/**
 * Home / Main activity — displays saved projects and "Create New Project" button.
 *
 * Clean version — no billing, no ads, no premium checks.
 * RecyclerView shows cached project thumbnails from the app's output directory.
 * "Create Video" button launches [EngineActivity] directly.
 */
class WorkUserActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWorkUserBinding
    private lateinit var adapter: WorkUserAdapter
    private val projectList = mutableListOf<ProjectItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWorkUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupCreateButton()
        setupSettingsButton()
        loadSavedProjects()
    }

    override fun onResume() {
        super.onResume()
        loadSavedProjects()
    }

    private fun setupRecyclerView() {
        adapter = WorkUserAdapter(projectList) { project ->
            // Click on existing project → open in engine for preview
            val intent = Intent(this, EngineActivity::class.java)
            intent.putExtra("project_path", project.path)
            startActivity(intent)
        }
        binding.rv.layoutManager = LinearLayoutManager(this)
        binding.rv.adapter = adapter
    }

    private fun setupCreateButton() {
        binding.btnToStudio.setOnClickListener {
            val intent = Intent(this, EngineActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupSettingsButton() {
        binding.btnMenu.setOnClickListener {
            val intent = Intent(this, SeettingActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadSavedProjects() {
        projectList.clear()

        // Scan the app's output directory for saved projects
        val outputDir = File(getExternalFilesDir(android.os.Environment.DIRECTORY_MOVIES), "NurMontage")
        if (outputDir.exists() && outputDir.isDirectory) {
            val videoFiles = outputDir.listFiles { file ->
                file.extension.lowercase() in listOf("mp4", "mkv", "avi")
            }?.sortedByDescending { it.lastModified() }

            videoFiles?.forEach { file ->
                projectList.add(
                    ProjectItem(
                        name = file.nameWithoutExtension,
                        path = file.absolutePath,
                        date = formatDateTime(file.lastModified()),
                        size = formatFileSize(file.length())
                    )
                )
            }
        }

        // Update UI
        if (projectList.isEmpty()) {
            binding.rv.visibility = View.GONE
        } else {
            binding.rv.visibility = View.VISIBLE
        }

        adapter.notifyDataSetChanged()
    }

    // ── Data class for project items ──────────────────────────────

    data class ProjectItem(
        val name: String,
        val path: String,
        val date: String,
        val size: String
    )

    // ── Utility methods ───────────────────────────────────────────

    private fun formatDateTime(timestamp: Long): String {
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(timestamp))
    }

    private fun formatFileSize(size: Long): String {
        return when {
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> "${size / 1024} KB"
            else -> "${size / (1024 * 1024)} MB"
        }
    }
}
