package hazem.nurmontage.videoquran.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import hazem.nurmontage.videoquran.R

/**
 * Spinner adapter for displaying surah names in a dropdown selector.
 *
 * Originally: SurahSpinnerAdapter.java (50 lines)
 * Converted to: SurahSpinnerAdapter.kt — idiomatic Kotlin, full logic preserved
 *
 * This adapter populates a Spinner with surah names from the Quran.
 * The surah name strings follow the format:
 * "Arabic Name - English Name" (e.g., "Al-Fatiha - Al-Fatiha").
 *
 * The adapter displays different portions of the name based on the
 * [isArabic] flag:
 * - **Arabic mode** ([isArabic] = true): Shows the Arabic name portion
 *   (before the " - " separator)
 * - **English mode** ([isArabic] = false): Shows the English name portion
 *   (after the " - " separator)
 *
 * @see ArrayAdapter
 */
class SurahSpinnerAdapter(
    context: Context,
    private val surahNames: Array<String>,
    private val isArabic: Boolean
) : ArrayAdapter<String>(context, R.layout.row_spinner_aya, surahNames) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getCustomView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getCustomView(position, convertView, parent)
    }

    /**
     * Inflate or reuse a row view and populate it with the surah name.
     *
     * The surah name string is split on the " - " separator to extract
     * the Arabic and English portions.
     */
    private fun getCustomView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView
            ?: LayoutInflater.from(context).inflate(R.layout.row_spinner_aya, parent, false)

        val textView: TextView = view.findViewById(R.id.spinner_text)

        val parts = surahNames[position].split(" - ")
        val displayName = if (isArabic) {
            parts.getOrElse(0) { surahNames[position] }
        } else {
            parts.getOrElse(1) { surahNames[position] }
        }

        textView.text = displayName
        return view
    }
}
