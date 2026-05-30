package hazem.nurmontage.videoquran.utils

import android.content.Context

/**
 * Manages the app's general-purpose SharedPreferences.
 *
 * Stores boolean flags and integer values that control various UI behaviors
 * and one-time actions throughout the app. All preferences are stored in
 * the "MyPrefs" SharedPreferences file.
 *
 * Managed preferences:
 * - **firstRun** — Whether this is the first time the app has been launched
 * - **is_about** — Whether the About screen has been viewed
 * - **is_vu_copyright** — Whether the Copyright dialog has been dismissed
 * - **hint_crop_scale** — Whether the crop/scale hint has been shown
 * - **icon_quran** — The last selected Quran icon index
 * - **IncludeBismilah** — Whether to include the Bismillah in ayah text
 * - **scroll_view_x** — Horizontal scroll position for restoring state
 *
 * All write operations use [SharedPreferences.Editor.apply] for async disk writes.
 *
 * Converted from MyPrefereces.java — logic preserved exactly.
 * Note: The original Java class name had a typo ("Prefereces" instead of "Preferences").
 * This Kotlin version uses the correct spelling.
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

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // ════════════════════════════════════════════════════════════════════
    //  Copyright dialog
    // ════════════════════════════════════════════════════════════════════

    /**
     * Check whether the Copyright dialog has been viewed and dismissed.
     *
     * @return `true` if the copyright dialog has been shown, `false` otherwise
     */
    fun isCopyRight(context: Context): Boolean =
        prefs(context).getBoolean(IS_VU_COPYRIGHT, false)

    /**
     * Mark the Copyright dialog as viewed so it won't be shown again.
     */
    fun putVuCopyRight(context: Context) {
        prefs(context).edit().putBoolean(IS_VU_COPYRIGHT, true).apply()
    }

    // ════════════════════════════════════════════════════════════════════
    //  Scroll position persistence
    // ════════════════════════════════════════════════════════════════════

    /**
     * Get the saved horizontal scroll position.
     *
     * Used to restore the scroll state of a ScrollView across activity
     * recreation (e.g., screen rotation).
     *
     * @return The saved X scroll offset in pixels, or 0 if not set
     */
    fun getScrollX(context: Context): Int =
        prefs(context).getInt(SCROLL_X, 0)

    /**
     * Save the horizontal scroll position for later restoration.
     *
     * @param x The current X scroll offset in pixels
     */
    fun putScrollX(context: Context, x: Int) {
        prefs(context).edit().putInt(SCROLL_X, x).apply()
    }

    // ════════════════════════════════════════════════════════════════════
    //  Crop/Scale hint
    // ════════════════════════════════════════════════════════════════════

    /**
     * Check whether the crop/scale hint has been shown.
     *
     * @return `true` if the hint has been shown, `false` otherwise
     */
    fun isShowHint(context: Context): Boolean =
        prefs(context).getBoolean(HINT_CROP_SCALE, false)

    /**
     * Mark the crop/scale hint as shown so it won't appear again.
     */
    fun putShowHint(context: Context) {
        prefs(context).edit().putBoolean(HINT_CROP_SCALE, true).apply()
    }

    // ════════════════════════════════════════════════════════════════════
    //  Quran icon selection
    // ════════════════════════════════════════════════════════════════════

    /**
     * Get the last selected Quran icon index.
     *
     * Used to restore the icon selection in the Quran picker across
     * activity recreation.
     *
     * @return The icon index (0-based), or 0 if not set
     */
    fun getLastIconIndex(context: Context): Int =
        prefs(context).getInt(ICON_QURAN, 0)

    /**
     * Save the selected Quran icon index.
     *
     * @param index The icon index (0-based)
     */
    fun putIndexLastIcon(context: Context, index: Int) {
        prefs(context).edit().putInt(ICON_QURAN, index).apply()
    }

    // ════════════════════════════════════════════════════════════════════
    //  Bismillah inclusion
    // ════════════════════════════════════════════════════════════════════

    /**
     * Check whether the Bismillah should be included in ayah text output.
     *
     * When enabled, the Bismillah line is prepended to the first ayah
     * of each surah (except At-Tawbah, surah 9).
     *
     * @return `true` if Bismillah should be included, `false` otherwise
     */
    fun isIncludeBismilah(context: Context): Boolean =
        prefs(context).getBoolean(INCLUDE_BISMILAH, false)

    /**
     * Set whether the Bismillah should be included in ayah text output.
     *
     * @param include `true` to include Bismillah, `false` to exclude it
     */
    fun putIncludeBismilah(context: Context, include: Boolean) {
        prefs(context).edit().putBoolean(INCLUDE_BISMILAH, include).apply()
    }

    // ════════════════════════════════════════════════════════════════════
    //  About screen
    // ════════════════════════════════════════════════════════════════════

    /**
     * Check whether the About screen has been viewed.
     *
     * @return `true` if the About screen has been visited, `false` otherwise
     */
    fun isVueAbout(context: Context): Boolean =
        prefs(context).getBoolean(IS_VU_ABOUT, false)

    /**
     * Mark the About screen as viewed.
     */
    fun putVueAbout(context: Context) {
        prefs(context).edit().putBoolean(IS_VU_ABOUT, true).apply()
    }

    // ════════════════════════════════════════════════════════════════════
    //  First run
    // ════════════════════════════════════════════════════════════════════

    /**
     * Check whether this is the first time the app has been launched.
     *
     * Returns `true` by default (i.e., before [putFirstRun] is called),
     * making it easy to show onboarding screens or initial setup dialogs.
     *
     * @return `true` if this is the first run, `false` if the app has been
     *         launched before
     */
    fun isFirstRun(context: Context): Boolean =
        prefs(context).getBoolean(FIRST_RUN_KEY, true)

    /**
     * Mark the first run as complete.
     *
     * After calling this method, [isFirstRun] will return `false`
     * for all subsequent app launches.
     */
    fun putFirstRun(context: Context) {
        prefs(context).edit().putBoolean(FIRST_RUN_KEY, false).apply()
    }
}
    fun isProVersion(): Boolean = true // Billing removed - all features unlocked
