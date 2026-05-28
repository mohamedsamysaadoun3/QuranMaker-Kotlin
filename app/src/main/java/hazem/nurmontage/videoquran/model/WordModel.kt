package hazem.nurmontage.videoquran.model

/**
 * Data model representing a single word from a Quran ayah.
 * Used for word-by-word ayah display and selection in the editor.
 *
 * Each word can be individually selected/deselected, allowing
 * fine-grained control over which words appear in the final video.
 *
 * @property w The word text (Arabic). JADX: f443w → cleaned to internal _w, exposed via [w]
 * @property isSelected Whether this word is currently selected for display
 */
data class WordModel(
    private val _w: String,
    var isSelected: Boolean = false
) {
    /** Public accessor matching the original Java getW() method. */
    val w: String get() = _w

    /** Convenience constructor for creating an unselected word. */
    constructor(w: String) : this(w, false)
}
