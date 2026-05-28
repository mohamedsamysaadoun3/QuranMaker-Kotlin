package hazem.nurmontage.videoquran.ui.editor.audio

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.core.base.BaseActivity
import hazem.nurmontage.videoquran.databinding.ActivityAddReaderNameBinding

/**
 * Activity for adding or editing a reciter/reader name overlay.
 *
 * Input (via intent extras):
 *   - "reader_name" (String) — current reader name text
 *
 * Output (via result intent extras):
 *   - "reader_name" (String) — updated reader name text
 */
class AddReaderNameActivity : BaseActivity() {

    private lateinit var binding: ActivityAddReaderNameBinding

    companion object {
        const val EXTRA_READER_NAME = "reader_name"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddReaderNameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setStatusBarColor()

        // Read input extras
        val currentName = intent.getStringExtra(EXTRA_READER_NAME) ?: ""
        binding.edtReader.setText(currentName)

        // Cancel button
        binding.btnCancel.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }

        // Done button
        binding.btnDone.setOnClickListener {
            returnResult()
        }
    }

    /**
     * Return the updated reader name to the caller.
     */
    private fun returnResult() {
        val name = binding.edtReader.text?.toString()?.trim() ?: ""
        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter a reader name", Toast.LENGTH_SHORT).show()
            return
        }

        val resultIntent = Intent().apply {
            putExtra(EXTRA_READER_NAME, name)
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }
}
