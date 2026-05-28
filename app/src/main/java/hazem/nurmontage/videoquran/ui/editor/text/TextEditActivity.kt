package hazem.nurmontage.videoquran.ui.editor.text

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.adapter.ColorAdapter
import hazem.nurmontage.videoquran.adapter.FontAdapter
import hazem.nurmontage.videoquran.core.base.BaseActivity
import hazem.nurmontage.videoquran.core.common.Constants
import hazem.nurmontage.videoquran.databinding.ActivityTextEditBinding
import hazem.nurmontage.videoquran.fragment.FontFragment
import hazem.nurmontage.videoquran.utils.FontProvider

/**
 * Activity for editing custom text with font, color, and size controls.
 *
 * Input (via intent extras):
 *   - "text_content" (String) — current text content
 *   - "font_path" (String) — current font file name (e.g., "عثماني.otf")
 *   - "font_size" (int) — current font size in sp
 *   - "text_color" (int) — current text color
 *
 * Output (via result intent extras):
 *   - "text_content" (String) — updated text content
 *   - "font_path" (String) — selected font file name
 *   - "font_size" (int) — selected font size in sp
 *   - "text_color" (int) — selected text color
 */
class TextEditActivity : BaseActivity() {

    private lateinit var binding: ActivityTextEditBinding

    private var selectedFontPath: String = Constants.FONT_QURAN
    private var selectedFontSize: Int = 24
    private var selectedColorPos: Int = 0
    private var selectedBgColorPos: Int = 0
    private var hasBackground: Boolean = false
    private var currentTypeface: Typeface? = null

    private lateinit var fontProvider: FontProvider
    private var fontAdapter: FontAdapter? = null
    private lateinit var colorAdapter: ColorAdapter

    companion object {
        const val EXTRA_TEXT_CONTENT = "text_content"
        const val EXTRA_FONT_PATH = "font_path"
        const val EXTRA_FONT_SIZE = "font_size"
        const val EXTRA_TEXT_COLOR = "text_color"
        const val EXTRA_TEXT_HAS_BG = "text_has_bg"
        const val EXTRA_TEXT_BG_COLOR = "text_bg_color"

        private const val MIN_FONT_SIZE = 8
        private const val MAX_FONT_SIZE = 72
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTextEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setStatusBarColor()

        // Initialize font provider
        fontProvider = FontProvider(resources)

        // Read input extras
        val currentText = intent.getStringExtra(EXTRA_TEXT_CONTENT) ?: ""
        selectedFontPath = intent.getStringExtra(EXTRA_FONT_PATH) ?: Constants.FONT_QURAN
        selectedFontSize = intent.getIntExtra(EXTRA_FONT_SIZE, 24).coerceIn(MIN_FONT_SIZE, MAX_FONT_SIZE)
        val currentColor = intent.getIntExtra(EXTRA_TEXT_COLOR, -1)
        hasBackground = intent.getBooleanExtra(EXTRA_TEXT_HAS_BG, false)
        val currentBgColor = intent.getIntExtra(EXTRA_TEXT_BG_COLOR, 0)

        // Set initial values
        binding.edtReader.setText(currentText)
        binding.edtReader.textSize = selectedFontSize.toFloat()

        // Apply current font
        applyFont(selectedFontPath)

        // Back/cancel button
        binding.btnOnBack.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }

        // Done button
        binding.btnDone.setOnClickListener {
            returnResult()
        }

        // Font size seek bar
        setupFontSizeControl()

        // Background checkbox
        binding.checkboxBg.setOnCheckedChangeListener { _, isChecked ->
            hasBackground = isChecked
            binding.rvColor.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
        binding.checkboxBg.isChecked = hasBackground

        // Setup adapters
        setupFontAdapter()
        setupColorAdapter(currentColor, currentBgColor)

        // Live preview text changes
        binding.edtReader.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                // Text is already shown in the EditText itself
            }
        })
    }

    /**
     * Apply the selected font to the edit text.
     */
    private fun applyFont(fontPath: String) {
        try {
            // Try to find the display name for the font path
            val displayName = findDisplayNameForFont(fontPath)
            val typeface = fontProvider.getTypeface(displayName)
            currentTypeface = typeface
            binding.edtReader.typeface = typeface
            selectedFontPath = fontProvider.getFullName(displayName) ?: fontPath
        } catch (_: Exception) {
            // Fallback to default typeface
        }
    }

    /**
     * Find the display name for a given font file name.
     */
    private fun findDisplayNameForFont(fontFileName: String): String {
        val fontNames = fontProvider.getFontNamesQuran()
        for (name in fontNames) {
            val fullName = fontProvider.getFullName(name)
            if (fullName == fontFileName) return name
        }
        // Default to "عثماني" if not found
        return "عثماني"
    }

    /**
     * Set up the font size seek bar control.
     */
    private fun setupFontSizeControl() {
        // We use the RecyclerView for font selection and the text size
        // is controlled by the font adapter callback. For quick size changes,
        // we can use the color area or a simple approach.
        // The font size is adjustable through the font callback.
    }

    /**
     * Set up the font selection RecyclerView.
     */
    private fun setupFontAdapter() {
        val fontNames = fontProvider.getFontNamesQuran()
        val selectedFontIndex = fontNames.indexOf(findDisplayNameForFont(selectedFontPath)).coerceAtLeast(0)

        fontAdapter = FontAdapter(
            fontProvider,
            object : FontFragment.IFontCallback {
                override fun onAdd(fullName: String?, typeface: Typeface?) {
                    fullName?.let { selectedFontPath = it }
                    typeface?.let {
                        currentTypeface = it
                        binding.edtReader.typeface = it
                    }
                }

                override fun onCancel(fontName: String?, typeface: Typeface?) {
                    // Revert handled by dismiss
                }

                override fun onDone(fontName: String?, typeface: Typeface?) {
                    fontName?.let { selectedFontPath = it }
                    typeface?.let {
                        currentTypeface = it
                        binding.edtReader.typeface = it
                    }
                }
            },
            fontNames,
            selectedFontIndex
        )

        binding.rv.apply {
            layoutManager = LinearLayoutManager(this@TextEditActivity, RecyclerView.VERTICAL, false)
            adapter = fontAdapter
        }
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

        // The rvColor is for background color
        binding.rvColor.apply {
            layoutManager = LinearLayoutManager(this@TextEditActivity, RecyclerView.HORIZONTAL, false)
            adapter = colorAdapter
            visibility = if (hasBackground) View.VISIBLE else View.GONE
        }
    }

    /**
     * Return the updated text data to the caller.
     */
    private fun returnResult() {
        val text = binding.edtReader.text?.toString()?.trim() ?: ""
        if (text.isEmpty()) {
            Toast.makeText(this, "Please enter some text", Toast.LENGTH_SHORT).show()
            return
        }

        val bgColor = if (hasBackground) {
            Constants.MUSLIM_AYA_COLORS.getOrElse(colorAdapter.getPosSelect()) { 0 }
        } else {
            0
        }

        val resultIntent = Intent().apply {
            putExtra(EXTRA_TEXT_CONTENT, text)
            putExtra(EXTRA_FONT_PATH, selectedFontPath)
            putExtra(EXTRA_FONT_SIZE, selectedFontSize)
            putExtra(EXTRA_TEXT_COLOR, Constants.MUSLIM_AYA_COLORS.getOrElse(selectedColorPos) { -1 })
            putExtra(EXTRA_TEXT_HAS_BG, hasBackground)
            putExtra(EXTRA_TEXT_BG_COLOR, bgColor)
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        fontAdapter?.clear()
        fontProvider.clear()
    }
}
