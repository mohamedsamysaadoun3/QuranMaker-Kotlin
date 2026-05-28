package hazem.nurmontage.videoquran.model

/**
 * Reciter model for online audio source resolution.
 * Stores the server identifier, zero-padded surah index, ayah number, and tarteel flag.
 *
 * Serialization-critical field names preserved verbatim:
 *   identifer (typo preserved), surah_index, number_aya, isTarteel
 */
data class RecitersModel(
    val identifer: String,
    surahIdx: Int,
    ayaNum: Int
) {
    val surah_index: String
    val number_aya: String
    val isTarteel: Boolean

    init {
        surah_index = when {
            surahIdx < 10  -> "00$surahIdx"
            surahIdx < 100 -> "0$surahIdx"
            else           -> "$surahIdx"
        }
        number_aya = when {
            ayaNum < 10  -> "00$ayaNum"
            ayaNum < 100 -> "0$ayaNum"
            else         -> "$ayaNum"
        }
        isTarteel = !identifer.contains("_")
    }
}
