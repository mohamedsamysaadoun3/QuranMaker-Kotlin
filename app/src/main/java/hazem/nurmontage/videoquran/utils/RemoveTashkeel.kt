package hazem.nurmontage.videoquran.utils

/**
 * Arabic Tashkeel (diacritical marks) detection and removal utilities.
 *
 * Tashkeel marks are Unicode characters in the range U+064B–U+065C plus
 * U+0640 (Tatweel/Kashida) that appear above, below, or within Arabic letters
 * to indicate vowel sounds, nunation, shadda, etc.
 *
 * This class provides:
 * - [isTashkeel] — check if a character is a Tashkeel mark
 * - [removeTashkeel] — strip all Tashkeel marks from a string
 * - [removeTashkeelAndPoint] — strip Tashkeel + dots (for fuzzy matching)
 * - [countTashkeel] — count Tashkeel marks in a string
 * - [removeChar] — replace non-Tashkeel characters with spaces (inverse operation)
 *
 * The Tashkeel character set is initialized once in a static block and reused.
 *
 * Converted from RemoveTashkeel.java — character set and algorithms preserved exactly.
 */
object RemoveTashkeel {

    private const val SPECIFIC_POINT_CHAR_CODE = '.'

    /**
     * Set of all Arabic Tashkeel (diacritical) characters.
     *
     * Unicode codepoints: 1611–1621 (U+064B–U+0655), 1648 (U+0670), 1600 (U+0640)
     *
     * These include:
     * - Fathatan (1611), Dammatan (1612), Kasratan (1613)
     * - Fatha (1614), Damma (1615), Kasra (1616)
     * - Shadda (1617), Sukun (1618)
     * - Maddah (1619), Hamza above (1620), Hamza below (1621)
     * - Superscript Alef (1648), Tatweel/Kashida (1600)
     */
    private val TASHKEEL_SET: Set<Char> = setOf(
        1611.toChar(), 1612.toChar(), 1613.toChar(),
        1614.toChar(), 1615.toChar(), 1616.toChar(),
        1617.toChar(), 1618.toChar(), 1619.toChar(),
        1620.toChar(), 1621.toChar(), 1648.toChar(),
        1600.toChar()
    )

    /**
     * Reference list of Arabic vowel strings for display/lookup purposes.
     * Preserved from the original Java static initializer.
     */
    val arabicVOriginal: List<String> = listOf(
        "ؘ", "ؙ", "ؚ", "ؐ", "ؐؑ", "ؒ", "ؓ", "ؔ", "ؕ", "ؖ", "ؗ",
        "ؗ", "ﹰﹰ", "ﹲ", "ﹴ", "ﹸ", "ﹼ", "ﹾ", "ٍ", "ً", "ُ", "ِ",
        "َ", "ّ", "ٓ", "ٔ", "ْ", "ِ", "َّ", "َ", "َْ", "َ", "ً", "ٌ",
        "َ", "ُ", "ٍ", "َ", "ْ", "ِ", "ُ", "ّ", "ً"
    )

    /**
     * Check if the given character is a Tashkeel mark.
     *
     * @param c The character to check
     * @return true if the character is in the TASHKEEL_SET
     */
    fun isTashkeel(c: Char): Boolean = TASHKEEL_SET.contains(c)

    /**
     * Remove all Tashkeel marks from the input string.
     *
     * Used before text search operations to match base letters only,
     * since Quran text may or may not include Tashkeel depending on
     * the recitation style.
     *
     * @param text The input string (may be null)
     * @return A new string with all Tashkeel characters removed, or null if input is null
     */
    fun removeTashkeel(text: String?): String? {
        if (text == null) return null
        val sb = StringBuilder(text.length)
        for (c in text) {
            if (!isTashkeel(c)) {
                sb.append(c)
            }
        }
        return sb.toString()
    }

    /**
     * Count the number of Tashkeel marks in the input string.
     *
     * Useful for determining whether a string is voweled or unvoweled.
     *
     * @param text The input string (may be null)
     * @return The number of Tashkeel characters found
     */
    fun countTashkeel(text: String?): Int {
        if (text == null) return 0
        var count = 0
        for (c in text) {
            if (isTashkeel(c)) count++
        }
        return count
    }

    /**
     * Remove both Tashkeel marks AND periods from the input string.
     *
     * Used for the most aggressive fuzzy matching where even punctuation
     * should be ignored.
     *
     * @param text The input string (may be null)
     * @return A new string with Tashkeel and periods removed, or null if input is null
     */
    fun removeTashkeelAndPoint(text: String?): String? {
        if (text == null) return null
        val sb = StringBuilder(text.length)
        for (c in text) {
            if (!isTashkeel(c) && c != SPECIFIC_POINT_CHAR_CODE) {
                sb.append(c)
            }
        }
        return sb.toString()
    }

    /**
     * Replace all non-Tashkeel characters with spaces.
     *
     * This is the inverse of [removeTashkeel] — it preserves only the
     * Tashkeel positions, which can be useful for analyzing vowel patterns.
     *
     * @param text The input string (may be null)
     * @return A string where non-Tashkeel characters are replaced with spaces
     */
    fun removeChar(text: String?): String? {
        if (text == null) return null
        val sb = StringBuilder(text.length)
        for (c in text) {
            if (isTashkeel(c)) {
                sb.append(c)
            } else {
                sb.append(' ')
            }
        }
        return sb.toString()
    }
}
