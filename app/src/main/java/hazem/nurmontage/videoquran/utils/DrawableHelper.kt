package hazem.nurmontage.videoquran.utils

import hazem.nurmontage.videoquran.R
import java.util.Random

/**
 * Helper for resolving drawable resource IDs by name.
 * Provides static maps for icon and background drawable lookups,
 * plus a random background selector.
 *
 * Originally: DrawableHelper.java
 * Converted to: DrawableHelper.kt — idiomatic Kotlin, map-based lookups with fallbacks
 *
 * Used by adapters (IconQuranAdabters, BgAdapter) to resolve drawable
 * resource IDs from string identifiers without reflection.
 */
object DrawableHelper {

    /** Map of Quran reader/reciter icon names to drawable resource IDs. */
    private val drawableIconMap: Map<String, Int> = mapOf(
        "hafes" to R.drawable.hafes_icon,
        "warach" to R.drawable.warach_icon,
        "shamerli" to R.drawable.shamerli_icon,
        "nour_hode" to R.drawable.nour_hoda_icon,
        "amiri" to R.drawable.amiri_icon,
        "taha" to R.drawable.taha_icon
    )

    /** Map of background drawable names to resource IDs (bg_1 through bg_38). */
    private val drawableMap: Map<String, Int> = mapOf(
        "bg_1" to R.drawable.bg_1, "bg_2" to R.drawable.bg_2,
        "bg_3" to R.drawable.bg_3, "bg_4" to R.drawable.bg_4,
        "bg_5" to R.drawable.bg_5, "bg_6" to R.drawable.bg_6,
        "bg_7" to R.drawable.bg_7, "bg_8" to R.drawable.bg_8,
        "bg_9" to R.drawable.bg_9, "bg_10" to R.drawable.bg_10,
        "bg_11" to R.drawable.bg_11, "bg_12" to R.drawable.bg_12,
        "bg_13" to R.drawable.bg_13, "bg_14" to R.drawable.bg_14,
        "bg_15" to R.drawable.bg_15, "bg_16" to R.drawable.bg_16,
        "bg_17" to R.drawable.bg_17, "bg_18" to R.drawable.bg_18,
        "bg_19" to R.drawable.bg_19, "bg_20" to R.drawable.bg_20,
        "bg_21" to R.drawable.bg_21, "bg_22" to R.drawable.bg_22,
        "bg_23" to R.drawable.bg_23, "bg_24" to R.drawable.bg_24,
        "bg_25" to R.drawable.bg_25, "bg_26" to R.drawable.bg_26,
        "bg_27" to R.drawable.bg_27, "bg_28" to R.drawable.bg_28,
        "bg_29" to R.drawable.bg_29, "bg_30" to R.drawable.bg_30,
        "bg_31" to R.drawable.bg_31, "bg_32" to R.drawable.bg_32,
        "bg_33" to R.drawable.bg_33, "bg_34" to R.drawable.bg_34,
        "bg_35" to R.drawable.bg_35, "bg_36" to R.drawable.bg_36,
        "bg_37" to R.drawable.bg_37, "bg_38" to R.drawable.bg_38
    )

    /**
     * Resolves a platform identifier string to the corresponding social media icon.
     * Falls back to Instagram icon for null or unrecognized identifiers.
     *
     * @param platformId Platform identifier (e.g., "y_16:9" for YouTube, "t" for TikTok)
     * @return The drawable resource ID for the platform icon
     */
    fun getIdResource(platformId: String?): Int {
        if (platformId == null || platformId.contains("init")) {
            return R.drawable.ic_instagram
        }
        return when {
            platformId.contains("t") -> R.drawable.ic_tiktok
            platformId == "y_16:9" -> R.drawable.ic_youtube
            else -> R.drawable.ic_youtube_shorts_icon
        }
    }

    /**
     * Resolves a Quran reader icon name to its drawable resource ID.
     * Falls back to "hafes_icon" if the name is not found.
     *
     * @param name The icon name (e.g., "hafes", "warach", "amiri")
     * @return The drawable resource ID
     */
    fun getIDDrawableIconByName(name: String): Int {
        return drawableIconMap[name] ?: R.drawable.hafes_icon
    }

    /**
     * Resolves a background drawable name to its resource ID.
     * Falls back to "bg_24" if the name is not found.
     *
     * @param name The background name (e.g., "bg_1", "bg_24")
     * @return The drawable resource ID
     */
    fun getIDDrawableByName(name: String): Int {
        return drawableMap[name] ?: R.drawable.bg_24
    }

    /**
     * Returns a random background drawable entry from the map.
     * Used for default/random background selection in the editor.
     *
     * @return A random Map.Entry containing the name and resource ID
     */
    fun getRandomDrawableEntry(): Map.Entry<String, Int> {
        val entries = drawableMap.entries.toList()
        return entries[Random().nextInt(entries.size)]
    }
}
