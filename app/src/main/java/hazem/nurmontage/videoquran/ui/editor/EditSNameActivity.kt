package hazem.nurmontage.videoquran.ui.editor

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.adapter.ColorAdapter
import hazem.nurmontage.videoquran.constant.SurahNameStyle
import hazem.nurmontage.videoquran.core.base.BaseActivity
import hazem.nurmontage.videoquran.core.common.Constants
import hazem.nurmontage.videoquran.databinding.ActivityEditSnameBinding

/**
 * Activity for editing surah name style and text.
 *
 * Input (via intent extras):
 *   - "surah_name_style" (int) — ordinal of [SurahNameStyle]
 *   - "surah_name_text" (String) — current surah name text
 *   - "surah_name_color" (int) — current text color
 *   - "surah_name_has_bg" (boolean) — whether background is enabled
 *   - "surah_name_bg_color" (int) — background color
 *
 * Output (via result intent extras):
 *   - "surah_name_style" (int) — ordinal of selected [SurahNameStyle]
 *   - "surah_name_text" (String) — updated surah name text
 *   - "surah_name_color" (int) — selected text color
 *   - "surah_name_has_bg" (boolean) — whether background is enabled
 *   - "surah_name_bg_color" (int) — background color
 */
class EditSNameActivity : BaseActivity() {

    private lateinit var binding: ActivityEditSnameBinding

    private var selectedStyle: SurahNameStyle = SurahNameStyle.NONE
    private var selectedColorPos: Int = 0
    private var selectedBgColorPos: Int = 0
    private var hasBackground: Boolean = false

    private lateinit var colorAdapter: ColorAdapter
    private lateinit var bgColorAdapter: ColorAdapter

    companion object {
        const val EXTRA_SURAH_NAME_STYLE = "surah_name_style"
        const val EXTRA_SURAH_NAME_TEXT = "surah_name_text"
        const val EXTRA_SURAH_NAME_COLOR = "surah_name_color"
        const val EXTRA_SURAH_NAME_HAS_BG = "surah_name_has_bg"
        const val EXTRA_SURAH_NAME_BG_COLOR = "surah_name_bg_color"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditSnameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setStatusBarColor()

        // Read input extras
        val styleOrdinal = intent.getIntExtra(EXTRA_SURAH_NAME_STYLE, SurahNameStyle.NONE.ordinal)
        selectedStyle = SurahNameStyle.entries.getOrElse(styleOrdinal) { SurahNameStyle.NONE }
        val currentText = intent.getStringExtra(EXTRA_SURAH_NAME_TEXT) ?: ""
        val currentColor = intent.getIntExtra(EXTRA_SURAH_NAME_COLOR, -1)
        hasBackground = intent.getBooleanExtra(EXTRA_SURAH_NAME_HAS_BG, false)
        val currentBgColor = intent.getIntExtra(EXTRA_SURAH_NAME_BG_COLOR, 0)

        // Set initial values
        binding.edtReader.setText(currentText)
        updateStyleUI()

        // Back/cancel button
        binding.btnOnBack.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }

        // Done button
        binding.btnDone.setOnClickListener {
            returnResult()
        }

        // Style option 1 — NONE
        binding.tvOption1.setOnClickListener {
            selectedStyle = SurahNameStyle.NONE
            updateStyleUI()
        }

        // Style option 2 — ZAGHRAFAT
        binding.tvOption2.setOnClickListener {
            selectedStyle = SurahNameStyle.ZAGHRAFAT
            updateStyleUI()
        }

        // Background checkbox toggle
        binding.checkboxBg.setOnCheckedChangeListener { _, isChecked ->
            hasBackground = isChecked
            binding.rvColor.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
        binding.checkboxBg.isChecked = hasBackground

        // Setup color adapters
        setupColorAdapters(currentColor, currentBgColor)
    }

    /**
     * Update the UI to reflect the currently selected surah name style.
     */
    private fun updateStyleUI() {
        when (selectedStyle) {
            SurahNameStyle.NONE -> {
                binding.tvOption1.setBackgroundResource(R.drawable.bg_option_surah_selected)
                binding.tvOption2.setBackgroundResource(R.drawable.bg_option_surah_unselected)
                binding.tvOption1.setTextColor(0xFFFFFFFF.toInt())
                binding.tvOption2.setTextColor(0xFF888888.toInt())
            }
            SurahNameStyle.ZAGHRAFAT -> {
                binding.tvOption1.setBackgroundResource(R.drawable.bg_option_surah_unselected)
                binding.tvOption2.setBackgroundResource(R.drawable.bg_option_surah_selected)
                binding.tvOption1.setTextColor(0xFF888888.toInt())
                binding.tvOption2.setTextColor(0xFFFFFFFF.toInt())
            }
        }
    }

    /**
     * Set up the color palette RecyclerViews for text color and background color.
     */
    private fun setupColorAdapters(currentColor: Int, currentBgColor: Int) {
        val colors = Constants.MUSLIM_AYA_COLORS

        // Find the position of the current color
        selectedColorPos = colors.indexOfFirst { it == currentColor }.coerceAtLeast(0)
        selectedBgColorPos = colors.indexOfFirst { it == currentBgColor }.coerceAtLeast(0)

        // Text color adapter (not directly shown in layout but could be added)
        // The main rv_color is for background color
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
            layoutManager = LinearLayoutManager(this@EditSNameActivity, RecyclerView.HORIZONTAL, false)
            adapter = colorAdapter
            visibility = if (hasBackground) View.VISIBLE else View.GONE
        }
    }

    /**
     * Return the updated surah name data to the caller.
     */
    private fun returnResult() {
        val text = binding.edtReader.text?.toString()?.trim() ?: ""
        if (text.isEmpty()) {
            Toast.makeText(this, "Please enter a surah name", Toast.LENGTH_SHORT).show()
            return
        }

        val bgColor = if (hasBackground) {
            Constants.MUSLIM_AYA_COLORS.getOrElse(colorAdapter.getPosSelect()) { 0 }
        } else {
            0
        }

        val resultIntent = Intent().apply {
            putExtra(EXTRA_SURAH_NAME_STYLE, selectedStyle.ordinal)
            putExtra(EXTRA_SURAH_NAME_TEXT, text)
            putExtra(EXTRA_SURAH_NAME_COLOR, Constants.MUSLIM_AYA_COLORS.getOrElse(selectedColorPos) { -1 })
            putExtra(EXTRA_SURAH_NAME_HAS_BG, hasBackground)
            putExtra(EXTRA_SURAH_NAME_BG_COLOR, bgColor)
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }
}
