package hazem.nurmontage.videoquran.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.model.WordModel
import hazem.nurmontage.videoquran.views.text.TextCustumFont

/**
 * RecyclerView Adapter for displaying Quran ayah words in a Karaoke-style layout.
 *
 * Originally: WordAyaAdabter.java (preserved typo in original package)
 * Converted to: WordAyaAdabter.kt — idiomatic Kotlin, full logic preserved
 *
 * Features:
 * - Displays individual words from a Quran ayah as separate selectable chips
 * - Each word toggles between selected (highlighted) and unselected (dark) states
 * - Selected words show with a Quran-style accent color and custom background
 * - Unselected words show with a dark background and white text
 * - Click toggles the [WordModel.isSelected] state and notifies the adapter
 * - Optional [IWordAya] callback fires after each toggle for the hosting
 *   Fragment/Activity to react (e.g., rebuild the visible ayah text)
 * - Supports full list replacement via [setList]
 * - Exposes the current word list via [getList]
 *
 * This adapter is the core of the Karaoke word-by-word feature: the user
 * can toggle individual words on/off to control which words appear in the
 * final video output, enabling precise timing and visual control.
 *
 * @property list List of word models representing the ayah's words
 * @property iWordAya Optional callback for word toggle events
 */
class WordAyaAdabter(
    private var list: List<WordModel>?,
    private val iWordAya: IWordAya? = null
) : RecyclerView.Adapter<WordAyaAdabter.ViewHolder>() {

    /** Callback interface for word-by-word ayah toggle events. */
    interface IWordAya {
        /** Called when a word is toggled (selected or deselected). */
        fun onClick()
    }

    /**
     * Replaces the entire word list and refreshes the display.
     * Used when a new ayah is loaded or when the word list is rebuilt.
     *
     * @param newList The new list of word models
     */
    fun setList(newList: List<WordModel>) {
        list = newList
        notifyDataSetChanged()
    }

    /** Returns the current list of word models. */
    fun getList(): List<WordModel>? = list

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_word_aya, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val wordModel = list?.get(position) ?: return

        holder.text.text = wordModel.w

        if (wordModel.isSelected) {
            // Selected state: Quran accent color with highlighted background
            holder.text.setBackgroundResource(R.drawable.round_btn_quran_select)
            holder.text.setTextColor(-12434878) // Teal accent color
        } else {
            // Unselected state: dark background with white text
            holder.text.setBackgroundResource(R.drawable.round_btn_in_dark)
            holder.text.setTextColor(-1) // White
        }
    }

    override fun getItemCount(): Int = list?.size ?: 0

    /**
     * ViewHolder for individual ayah word items.
     * Handles click events to toggle word selection state.
     */
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val text: TextCustumFont = itemView.findViewById(R.id.word_aya)

        init {
            text.setOnClickListener {
                val pos = adapterPosition
                if (pos == -1) return@setOnClickListener

                // Toggle the selection state of the clicked word
                list?.get(pos)?.let { word ->
                    word.isSelected = !word.isSelected
                    notifyItemChanged(pos)

                    // Notify the hosting component that a word was toggled
                    iWordAya?.onClick()
                }
            }
        }
    }
}
