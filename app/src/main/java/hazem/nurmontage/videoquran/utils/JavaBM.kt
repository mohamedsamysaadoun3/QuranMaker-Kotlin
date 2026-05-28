package hazem.nurmontage.videoquran.utils

import java.util.Locale

/**
 * Boyer-Moore string search algorithm optimized for Arabic text.
 *
 * Uses a skip table sized for the Arabic Unicode range (U+0622–U+064A, 1570–1610)
 * plus a catch-all slot for non-Arabic characters. This allows efficient
 * bad-character shifts during pattern matching on Quranic text.
 *
 * Two search modes are provided:
 * 1. **Instance-based** — [match(text)] reuses the pattern set via [setPattern]
 * 2. **Static** — [match(pattern, text)] performs a one-shot search returning
 *    all match positions
 *
 * The skip table maps each character to its rightmost position in the pattern.
 * Characters outside the Arabic range are mapped to index 41 (the catch-all slot).
 *
 * Converted from JavaBM.java — algorithm preserved exactly to maintain
 * identical search behavior for Quran text matching.
 */
class JavaBM {

    private var mPattern: String? = null
    private var mText: String = ""
    private val skipTable: IntArray = IntArray(42)

    constructor() {
        this.mText = ""
    }

    constructor(text: String) {
        this.mText = text
    }

    /**
     * Set the search pattern and rebuild the skip table.
     *
     * The skip table maps each Arabic character (U+0622–U+064A) to its
     * rightmost position in the pattern. Non-Arabic characters are mapped
     * to the catch-all slot at index 41.
     */
    fun setPattern(pattern: String) {
        mPattern = pattern
        buildSkipTable(pattern, skipTable)
    }

    fun getPattern(): String? = mPattern

    /**
     * Search for the pattern in [text] using Boyer-Moore with the
     * Arabic-optimized skip table.
     *
     * @return The starting index of the first match, or -1 if not found
     */
    fun match(text: String): Int {
        val pattern = mPattern ?: return -1
        var i = 0
        val patternLen = pattern.length
        val textLen = text.length

        while (i <= textLen - patternLen) {
            var j = patternLen - 1
            var mismatchChar: Char = 1570.toChar() // Arabic Alef as default

            while (j >= 0) {
                val patternChar = pattern[j]
                val textChar = text[i + j]
                if (patternChar != textChar) {
                    mismatchChar = textChar
                    break
                }
                j--
                mismatchChar = textChar
            }

            // Map non-Arabic characters to the catch-all index
            var mappedChar = mismatchChar.code
            if (mappedChar < 1570 || mappedChar > 1610) {
                mappedChar = 1611
            }

            if (j < 0) {
                return i // Match found
            }

            i += Math.max(j - skipTable[mappedChar - 1570], 1)
        }

        return -1
    }

    /**
     * Build the bad-character skip table for the given pattern.
     *
     * Index 0–40 correspond to Arabic characters U+0622–U+064A.
     * Index 41 is the catch-all for non-Arabic characters.
     * Each entry stores the rightmost position of that character in the pattern,
     * or -1 if the character does not appear.
     */
    private fun buildSkipTable(pattern: String, table: IntArray) {
        table.fill(-1)
        for (i in pattern.indices) {
            val c = pattern[i].code
            if (c < 1570 || c > 1610) {
                table[41] = i
            } else {
                table[c - 1570] = i
            }
        }
    }

    companion object {
        /**
         * Static Boyer-Moore search that returns ALL match positions.
         *
         * Uses a [HashMap]-based bad-character shift table instead of the
         * Arabic-optimized fixed-size array, making it suitable for general text.
         *
         * @param pattern The search pattern
         * @param text    The text to search within
         * @return List of all starting indices where the pattern was found
         */
        fun match(pattern: String, text: String): List<Int> {
            val result = mutableListOf<Int>()
            val textLen = text.length
            val patternLen = pattern.length
            val badCharShift = preprocessForBadCharacterShift(pattern)

            var patternIdx = patternLen - 1
            if (patternIdx >= textLen) return result

            var textIdx = 0
            while (true) {
                if (patternIdx >= 0) {
                    val textPos = textIdx + patternIdx
                    if (textPos >= textLen) break

                    val textChar = text[textPos]
                    val patternChar = pattern[patternIdx]

                    if (textChar != patternChar) {
                        // Bad character shift
                        val shift = badCharShift[textChar]
                        if (shift == null) {
                            textIdx = textPos + 1
                        } else {
                            var delta = textPos - (shift + textIdx)
                            if (delta <= 0) delta = 1
                            textIdx += delta
                        }
                    } else {
                        if (patternIdx == 0) {
                            result.add(textIdx)
                            textIdx++
                        }
                        patternIdx--
                    }
                }
            }

            return result
        }

        /**
         * Preprocess the pattern to build the bad-character shift table.
         *
         * For each character in the pattern, stores its rightmost position.
         * Only the first occurrence (from right to left) is kept.
         */
        private fun preprocessForBadCharacterShift(pattern: String): Map<Char, Int> {
            val map = HashMap<Char, Int>()
            for (i in pattern.length - 1 downTo 0) {
                val c = pattern[i]
                if (!map.containsKey(c)) {
                    map[c] = i
                }
            }
            return map
        }
    }
}
