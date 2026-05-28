package hazem.nurmontage.videoquran.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Application preferences manager using Android [SharedPreferences].
 *
 * Stores user preferences for the Quran video editor, including:
 * - Copyright acknowledgment state
 * - Scroll position persistence
 * - Crop/scale hint visibility
 * - Quran icon selection
 * - Bismilah inclusion preference
 * - About screen view state
 * - First-run detection
 *
 * All operations use [SharedPreferences.Editor.apply] for async disk writes.
 *
 * **Note**: The original Java class name `MyPrefereces` (misspelled) has been
 * corrected to `MyPreferences` in Kotlin.
 *
 * Converted from MyPrefereces.java — all preference keys preserved exactly
 * for backward compatibility with existing user data.
 */
object MyPreferences {

    private const val PREFS_NAME = "MyPrefs"
    private const val FIRST_RUN_KEY = "firstRun"
    private const val IS_VU_ABOUT = "is_about"
    private const val IS_VU_COPYRIGHT = "is_vu_copyright"
    private const val SCROLL_X = "scroll_view_x"
    private const val HINT_CROP_SCALE = "hint_crop_scale"
    private const val ICON_QURAN = "icon_quran"
    private const val INCLUDE_BISMILAH = "IncludeBismilah"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // ── Copyright ──────────────────────────────────────────────────────

    fun isCopyRight(context: Context): Boolean =
        prefs(context).getBoolean(IS_VU_COPYRIGHT, false)

    fun putVuCopyRight(context: Context) {
        prefs(context).edit().putBoolean(IS_VU_COPYRIGHT, true).apply()
    }

    // ── Scroll position ────────────────────────────────────────────────

    fun getScrollX(context: Context): Int =
        prefs(context).getInt(SCROLL_X, 0)

    fun putScrollX(context: Context, scrollX: Int) {
        prefs(context).edit().putInt(SCROLL_X, scrollX).apply()
    }

    // ── Crop/scale hint ────────────────────────────────────────────────

    fun isShowHint(context: Context): Boolean =
        prefs(context).getBoolean(HINT_CROP_SCALE, false)

    fun putShowHint(context: Context) {
        prefs(context).edit().putBoolean(HINT_CROP_SCALE, true).apply()
    }

    // ── Quran icon index ───────────────────────────────────────────────

    fun getLastIconIndex(context: Context): Int =
        prefs(context).getInt(ICON_QURAN, 0)

    fun putIndexLastIcon(context: Context, index: Int) {
        prefs(context).edit().putInt(ICON_QURAN, index).apply()
    }

    // ── Bismilah inclusion ─────────────────────────────────────────────

    fun isIncludeBismilah(context: Context): Boolean =
        prefs(context).getBoolean(INCLUDE_BISMILAH, false)

    fun putIncludeBismilah(context: Context, include: Boolean) {
        prefs(context).edit().putBoolean(INCLUDE_BISMILAH, include).apply()
    }

    // ── About screen ───────────────────────────────────────────────────

    fun isVueAbout(context: Context): Boolean =
        prefs(context).getBoolean(IS_VU_ABOUT, false)

    fun putVueAbout(context: Context) {
        prefs(context).edit().putBoolean(IS_VU_ABOUT, true).apply()
    }

    // ── First run ──────────────────────────────────────────────────────

    fun isFirstRun(context: Context): Boolean =
        prefs(context).getBoolean(FIRST_RUN_KEY, true)

    fun putFirstRun(context: Context) {
        prefs(context).edit().putBoolean(FIRST_RUN_KEY, false).apply()
    }
}
