package hazem.nurmontage.videoquran.model

import java.io.Serializable

/**
 * Data model representing a Quran ayah search result.
 * Used by [hazem.nurmontage.videoquran.adapter.AyaAdapter] to display
 * search results with highlighted text spans and multi-ayah selection.
 *
 * Serialization-compatible with the original Java class. Field names
 * and types are preserved to ensure data integrity across app versions.
 *
 * @property aya The full ayah text (Arabic), or null if only surah index is shown
 * @property surahName The name of the surah
 * @property to The ayah number within the surah (JADX: f434to → cleaned to _to, exposed via [to])
 * @property surahIndex The index of the surah (1-114)
 * @property startSpannable Start index for text highlighting, or -1 if no highlight
 * @property endSpannble End index for text highlighting (preserved original typo for serialization)
 */
data class ItemQuranSearch(
    val aya: String?,
    val surahName: String,
    private val _to: Int,
    val surahIndex: Int,
    val startSpannable: Int,
    val endSpannble: Int
) : Serializable {

    /** Public accessor matching the original Java getTo() method. */
    val to: Int get() = _to
}
