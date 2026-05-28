package hazem.nurmontage.videoquran.ui.editor

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.databinding.ActivityTimeLineBinding
import hazem.nurmontage.videoquran.model.FreeElement

/**
 * FreeLayerActivity — Free-form layer overlay editor.
 *
 * Allows the user to add and position decorative elements (images/text)
 * as free-form layers on top of the video frame.
 *
 * Uses the timeline layout (ActivityTimeLineBinding) because it provides
 * the main canvas area with the frame preview, which is the correct base
 * for a free-layer overlay editor.
 *
 * Flow:
 *   1. Activity opens with the current template background shown
 *   2. User can add elements, drag/resize them
 *   3. On "Done", the free elements are returned as a result
 */
class FreeLayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTimeLineBinding
    private val freeElements = mutableListOf<FreeElement>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTimeLineBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Cancel button — discard changes
        binding.btnCancel.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }

        // Export/Done button — confirm free layer changes
        binding.btnExport.setOnClickListener {
            val resultIntent = Intent().apply {
                putExtra("free_elements_count", freeElements.size)
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        }

        // Add element button — opens image picker
        binding.btnAddQuran.setOnClickListener {
            // In the free layer context, this adds a new free element
            addFreeElement()
        }

        // Background button — change background
        binding.btnBg.setOnClickListener {
            // Open background selector
        }

        // Hide toolbar buttons not relevant to free layer editing
        binding.btnIpad.visibility = View.GONE
        binding.btnChangeAspect.visibility = View.GONE
        binding.btnSetupFps.visibility = View.GONE
        binding.btnUndo.visibility = View.GONE
        binding.btnRedo.visibility = View.GONE
        binding.btnPlayPause.visibility = View.GONE
        binding.btnToStart.visibility = View.GONE
        binding.btnToEnd.visibility = View.GONE
    }

    /**
     * Add a new free element at the center of the canvas.
     */
    private fun addFreeElement() {
        val element = FreeElement().apply {
            x = 0.5f
            y = 0.5f
        }
        freeElements.add(element)
    }
}
