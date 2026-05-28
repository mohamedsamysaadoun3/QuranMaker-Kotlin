package hazem.nurmontage.videoquran.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.Utils.FontProvider
import hazem.nurmontage.videoquran.fragment.FontFragment
import hazem.nurmontage.videoquran.views.TextCustumFont

/**
 * RecyclerView Adapter for displaying and selecting Ottoman/Arabic font families.
 *
 * Originally: FontTextAdabters.java (preserved typo in original package)
 * Converted to: FontAdapter.kt — clean naming, idiomatic Kotlin
 *
 * Features:
 * - Displays font name previews in their actual typeface
 * - Single-selection mode with visual highlight
 * - Callback delivers both the full font name and the loaded Typeface
 * - Supports programmatic selection via [setSelected]
 * - Safe callback cleanup via [clear]
 *
 * @property fontProvider Provides font loading (full name + Typeface) from font identifiers
 * @property fontCallback Callback for font selection events
 * @property fontList List of font identifier strings
 * @property selected Currently selected font index
 */
class FontAdapter(
    private val fontProvider: FontProvider,
    private var fontCallback: FontFragment.IFontCallback?,
    private val fontList: List<String>?,
    private var selected: Int
) : RecyclerView.Adapter<FontAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_font, parent, false)
        return ViewHolder(view)
    }

    /**
     * Programmatically sets the selected font index and triggers the callback.
     * Notifies both old and new positions for smooth animation.
     *
     * @param index The new selected font index
     */
    fun setSelected(index: Int) {
        try {
            val oldSelected = selected
            selected = index
            notifyItemChanged(oldSelected)
            notifyItemChanged(selected)

            fontList?.let { list ->
                val fontName = list[index]
                fontCallback?.onAdd(fontProvider.getFullName(fontName), fontProvider.getTypeface(fontName))
            }
        } catch (_: Exception) {
            // Silently handle index out of bounds or null font
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        fontList?.let { list ->
            val fontName = list[position]
            holder.nameFont.text = fontName
            holder.tvNumber.text = (position + 1).toString()

            try {
                holder.nameFont.typeface = fontProvider.getTypeface(fontName)

                if (selected == position) {
                    holder.nameFont.setTextColor(-14540254) // Accent color
                    holder.nameFont.setBackgroundResource(R.drawable.btn_item_font_state)
                } else {
                    holder.nameFont.setTextColor(-1) // White
                    holder.nameFont.background = null
                }
            } catch (_: Exception) {
                // Font loading may fail for unsupported identifiers
            }
        }
    }

    override fun getItemCount(): Int = fontList?.size ?: 0

    /**
     * Clears the callback reference to prevent memory leaks.
     * Should be called when the hosting Fragment/Activity is destroyed.
     */
    fun clear() {
        fontCallback = null
    }

    /**
     * ViewHolder for font preview items.
     * Handles click events for font selection with immediate visual feedback.
     */
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameFont: TextCustumFont = itemView.findViewById(R.id.tv_font)
        val tvNumber: TextCustumFont = itemView.findViewById(R.id.tv_number)

        init {
            nameFont.setOnClickListener {
                val callback = fontCallback ?: return@setOnClickListener
                val pos = adapterPosition
                if (selected == pos) return@setOnClickListener

                val oldSelected = selected
                selected = pos
                notifyItemChanged(oldSelected)
                notifyItemChanged(selected)

                fontList?.let { list ->
                    val fontName = list[selected]
                    callback.onAdd(fontProvider.getFullName(fontName), fontProvider.getTypeface(fontName))
                }
            }
        }
    }
}
