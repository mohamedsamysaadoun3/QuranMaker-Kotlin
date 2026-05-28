package hazem.nurmontage.videoquran.utils

import android.content.Context
import android.content.res.AssetManager
import android.content.res.Resources
import android.graphics.Typeface
import android.text.TextUtils
import hazem.nurmontage.videoquran.core.common.Constants
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.TreeMap

/**
 * Font provider and cache manager for Ottoman/Arabic font families.
 *
 * Originally: FontProvider.java + FontUtils.java (merged for cohesion)
 * Converted to: FontProvider.kt — clean naming, memory-safe LruCache, idiomatic Kotlin
 *
 * Architecture:
 * - Loads 29 Arabic font mappings (displayName → fileName) from assets
 * - Caches loaded Typefaces in an LruCache to prevent OOM crashes
 * - Provides font name list sorted alphabetically (TreeSet → List)
 * - Includes utility to copy fonts to internal storage for FFmpeg access
 * - Safe cleanup via [clear] to release all cached Typefaces and references
 *
 * Memory Management:
 * - Uses LruCache with capacity limit (max 30 entries) instead of unbounded HashMap
 * - Each Typeface is ~50-200KB in memory; LruCache auto-evicts least-recently-used
 * - [clear] releases all references for garbage collection
 *
 * @property resources Android Resources for asset access
 */
class FontProvider(resources: Resources) {

    private var resources: Resources? = resources
    private var fontNameToTypefaceFileQuran: MutableMap<String, String>
    private var fontNamesQuran: MutableList<String>

    /**
     * LruCache for Typefaces — prevents unbounded memory growth.
     * Original Java used HashMap which could grow without limit.
     * Capacity: 30 entries (app has 29 fonts + 1 buffer for safety).
     */
    private val typefaceCache = object : android.util.LruCache<String, Typeface>(30) {
        override fun sizeOf(key: String, value: Typeface): Int = 1 // Entry count based
    }

    /** The default font name, set externally by the hosting component. */
    var defaultFontName: String? = null

    init {
        fontNameToTypefaceFileQuran = mutableMapOf()
        loadQuranFont()
        // Sorted list of font display names (TreeSet → alphabetical order)
        fontNamesQuran = TreeMap(fontNameToTypefaceFileQuran).keys.toList().toMutableList()
    }

    /**
     * Loads the Quran font mapping (displayName → fileName).
     * All 29 Ottoman/Arabic fonts are stored in assets/fonts/arabic/.
     */
    private fun loadQuranFont() {
        fontNameToTypefaceFileQuran.apply {
            put("المجد", "المجد.ttf")
            put("جنة", "جنة.ttf")
            put("محمدي", "محمدي.ttf")
            put("خط الثلث مزخرف", "الثلث مزخرف.ttf")
            put("باك تايب أجراك", "باك تايب أجراك.ttf")
            put("باك تايب تحرير", "باك تايب تحرير.ttf")
            put("باك تايب نسخ", "باك تايب نسخ.ttf")
            put("خط نسخ عثماني", "خط نسخ عثماني.otf")
            put("عثماني", Constants.FONT_QURAN) // "عثماني.otf"
            put("خط القيروان", "خط القيروان.ttf")
            put("خط حفص", "خط حفص.ttf")
            put("خط ورش", "خط ورش.ttf")
            put("قالون", "قالون.ttf")
            put("مريم", "مريم.ttf")
            put("الأقصى", "الأقصى.ttf")
            put("أجنادين", "أجنادين.ttf")
            put("بيبو", "بيبو.ttf")
            put("بيسان لايت", "بيسان لايت.ttf")
            put("تبيان", "تبيان.ttf")
            put("تجمع كوفي", "تجمع كوفي.ttf")
            put("تريكا", "تريكا.ttf")
            put("خط تجمع المصممين", "خط تجمع المصممين.ttf")
            put("شمائل", "شمائل.ttf")
            put("عصومي", "عصومي.ttf")
            put("فرشة", "فرشة.ttf")
            put("فسيح", "فسيح.ttf")
            put("كوفي", "كوفي.ttf")
            put("مطرية", "مطرية.ttf")
            put("نمر", "نمر.ttf")
            put("هيفن", "هيفن.ttf")
            put("لفتا بلاك", "لفتا بلاك.otf")
            put("خط الإبل", "خط الإبل.otf")
        }
    }

    /**
     * Returns the full font file name for the given display name.
     * Example: "عثماني" → "عثماني.otf"
     *
     * @param displayName The font's display name as shown in the UI
     * @return The font file name, or null if not found
     */
    fun getFullName(displayName: String): String? = fontNameToTypefaceFileQuran[displayName]

    /**
     * Returns a cached Typeface for the given display name.
     * Loads from assets on first access, then caches for reuse.
     * Falls back to [Typeface.DEFAULT] on error or null input.
     *
     * Memory-safe: LruCache auto-evicts old entries when capacity is reached.
     *
     * @param displayName The font's display name as shown in the UI
     * @return The loaded Typeface, or Typeface.DEFAULT on failure
     */
    fun getTypeface(displayName: String?): Typeface {
        if (displayName == null || TextUtils.isEmpty(displayName)) {
            return Typeface.DEFAULT
        }

        // Check cache first
        typefaceCache.get(displayName)?.let { return it }

        return try {
            val fileName = fontNameToTypefaceFileQuran[displayName] ?: return Typeface.DEFAULT
            val typeface = Typeface.createFromAsset(
                resources?.assets ?: return Typeface.DEFAULT,
                "fonts/arabic/$fileName"
            )
            typefaceCache.put(displayName, typeface)
            typeface
        } catch (_: Exception) {
            Typeface.DEFAULT
        }
    }

    /** Returns the alphabetically sorted list of font display names. */
    fun getFontNamesQuran(): List<String> = fontNamesQuran

    /** Returns the Resources reference (for external font loading). */
    fun getResources(): Resources? = resources

    /**
     * Releases all cached Typefaces, font mappings, and resource references.
     * Must be called when the hosting component is destroyed to prevent memory leaks.
     * After calling [clear], this instance should not be reused.
     */
    fun clear() {
        fontNameToTypefaceFileQuran.clear()
        fontNameToTypefaceFileQuran = mutableMapOf() // Reassign to empty
        fontNamesQuran.clear()
        fontNamesQuran = mutableListOf()
        typefaceCache.evictAll()
        resources = null
    }

    companion object {
        /**
         * Copies a font file from assets to internal storage.
         * Used by FFmpeg which requires file paths (cannot read from assets directly).
         *
         * The font is only copied if it doesn't already exist in internal storage,
         * avoiding redundant I/O operations on subsequent calls.
         *
         * @param context Android context for file and asset access
         * @param fontFileName The font file name (e.g., "عثماني.otf")
         */
        fun copyFontToInternalStorage(context: Context, fontFileName: String) {
            val targetFile = File(context.filesDir, fontFileName)
            if (targetFile.exists()) return

            try {
                context.assets.open("fonts/arabic/$fontFileName").use { inputStream ->
                    FileOutputStream(targetFile).use { outputStream ->
                        val buffer = ByteArray(1024)
                        var bytesRead: Int
                        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                            outputStream.write(buffer, 0, bytesRead)
                        }
                        outputStream.flush()
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}
