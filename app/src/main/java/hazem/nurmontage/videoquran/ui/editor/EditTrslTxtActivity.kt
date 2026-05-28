package hazem.nurmontage.videoquran.ui.editor

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.adapter.ColorAdapter
import hazem.nurmontage.videoquran.core.base.BaseActivity
import hazem.nurmontage.videoquran.core.common.Constants
import hazem.nurmontage.videoquran.databinding.ActivityEditTrslBinding

/**
 * Activity for editing translation text with style options.
 *
 * Input (via intent extras):
 *   - "trsl_text" (String) — current translation text
 *   - "trsl_number" (int) — verse/translation number
 *   - "trsl_color" (int) — current text color
 *   - "trsl_has_bg" (boolean) — whether background is enabled
 *   - "trsl_bg_color" (int) — background color
 *
 * Output (via result intent extras):
 *   - "trsl_text" (String) — updated translation text
 *   - "trsl_number" (int) — translation number
 *   - "trsl_color" (int) — selected text color
 *   - "trsl_has_bg" (boolean) — whether background is enabled
 *   - "trsl_bg_color" (int) — background color
 */
class EditTrslTxtActivity : BaseActivity() {

    private lateinit var binding: ActivityEditTrslBinding

    private var selectedColorPos: Int = 0
    private var selectedBgColorPos: Int = 0
    private var hasBackground: Boolean = false
    private var trslNumber: Int = 0

    private lateinit var colorAdapter: ColorAdapter

    companion object {
        const val EXTRA_TRSL_TEXT = "trsl_text"
        const val EXTRA_TRSL_NUMBER = "trsl_number"
        const val EXTRA_TRSL_COLOR = "trsl_color"
        const val EXTRA_TRSL_HAS_BG = "trsl_has_bg"
        const val EXTRA_TRSL_BG_COLOR = "trsl_bg_color"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditTrslBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setStatusBarColor()

        // Read input extras
        val currentText = intent.getStringExtra(EXTRA_TRSL_TEXT) ?: ""
        trslNumber = intent.getIntExtra(EXTRA_TRSL_NUMBER, 0)
        val currentColor = intent.getIntExtra(EXTRA_TRSL_COLOR, -1)
        hasBackground = intent.getBooleanExtra(EXTRA_TRSL_HAS_BG, false)
        val currentBgColor = intent.getIntExtra(EXTRA_TRSL_BG_COLOR, 0)

        // Set initial values
        binding.edtReader.setText(currentText)

        // Back/cancel button
        binding.btnOnBack.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }

        // Done button
        binding.btnDone.setOnClickListener {
            returnResult()
        }

        // Background checkbox toggle
        binding.checkboxBg.setOnCheckedChangeListener { _, isChecked ->
            hasBackground = isChecked
            binding.rvColor.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
        binding.checkboxBg.isChecked = hasBackground

        // Setup color adapter
        setupColorAdapter(currentColor, currentBgColor)
    }

    /**
     * Set up the color palette RecyclerView for background color selection.
     */
    private fun setupColorAdapter(currentColor: Int, currentBgColor: Int) {
        val colors = Constants.MUSLIM_AYA_COLORS

        selectedColorPos = colors.indexOfFirst { it == currentColor }.coerceAtLeast(0)
        selectedBgColorPos = colors.indexOfFirst { it == currentBgColor }.coerceAtLeast(0)

        colorAdapter = ColorAdapter(
            object : ColorAdapter.IColor {
                override fun onColor(color: Int, position: Int) {
                    selectedBgColorPos = position
                }
            },
            colors,
            selectedBgColorPos
        )

        binding.rvColor.apply {
            layoutManager = LinearLayoutManager(this@EditTrslTxtActivity, RecyclerView.HORIZONTAL, false)
            adapter = colorAdapter
            visibility = if (hasBackground) View.VISIBLE else View.GONE
        }
    }

    /**
     * Return the updated translation data to the caller.
     */
    private fun returnResult() {
        val text = binding.edtReader.text?.toString()?.trim() ?: ""
        if (text.isEmpty()) {
            Toast.makeText(this, "Please enter translation text", Toast.LENGTH_SHORT).show()
            return
        }

        val bgColor = if (hasBackground) {
            Constants.MUSLIM_AYA_COLORS.getOrElse(colorAdapter.getPosSelect()) { 0 }
        } else {
            0
        }

        val resultIntent = Intent().apply {
            putExtra(EXTRA_TRSL_TEXT, text)
            putExtra(EXTRA_TRSL_NUMBER, trslNumber)
            putExtra(EXTRA_TRSL_COLOR, Constants.MUSLIM_AYA_COLORS.getOrElse(selectedColorPos) { -1 })
            putExtra(EXTRA_TRSL_HAS_BG, hasBackground)
            putExtra(EXTRA_TRSL_BG_COLOR, bgColor)
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }
}
