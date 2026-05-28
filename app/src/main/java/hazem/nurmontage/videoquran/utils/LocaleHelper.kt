package hazem.nurmontage.videoquran.utils

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

/**
 * Locale management helper for Arabic/English language switching.
 *
 * Provides methods to:
 * - Persist the user's language choice across app restarts
 * - Apply the selected locale to the app's configuration
 * - Integrate with AndroidX [AppCompatDelegate] per-app language API
 *
 * Uses two mechanisms for locale application:
 * 1. **Modern** (API 24+): [AppCompatDelegate.setApplicationLocales] which
 *    handles per-app language without needing to override Context wrappers
 * 2. **Legacy** (API ≤ 24): [updateResourcesLegacy] which manually creates
 *    a configuration-context wrapper and calls [Resources.updateConfiguration]
 *
 * Converted from LocaleHelper.java — locale handling preserved exactly.
 */
object LocaleHelper {

    private const val SELECTED_LANGUAGE = "Locale.Helper.Selected.Language"
    private const val PREFS_NAME = "ActPreference"
    private const val USER_IS_CHOICE = "userIsChoice"

    /**
     * Called during Activity.attachBaseContext to apply the persisted locale.
     *
     * @param context The base context
     * @return A context with the correct locale applied
     */
    fun onAttach(context: Context): Context {
        return setLocale(context, getPersistedData(context, getLanguage(context)))
    }

    /**
     * Get the currently persisted language code (defaults to "en").
     */
    fun getLanguage(context: Context): String {
        return getPersistedData(context, "en")
    }

    /**
     * Set the app locale using the AndroidX per-app language API.
     *
     * This is the preferred method for setting the language on modern Android.
     *
     * @param languageTag The BCP 47 language tag (e.g. "ar", "en")
     */
    fun setLocale(languageTag: String) {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(languageTag))
    }

    /**
     * Set the locale and persist the choice.
     *
     * @param context  The context
     * @param language The language code to apply
     * @return A new context with the updated locale
     */
    fun setLocale(context: Context, language: String): Context {
        persist(context, language)
        return updateResources(context, language)
    }

    // ── Persistence ────────────────────────────────────────────────────

    fun getPersistedData(context: Context, default: String): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(SELECTED_LANGUAGE, default) ?: default
    }

    fun persist(context: Context, language: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(SELECTED_LANGUAGE, language)
            .apply()
    }

    fun userIsChoice(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(USER_IS_CHOICE, true)
            .apply()
    }

    fun getUserIsChoice(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(USER_IS_CHOICE, false)
    }

    // ── Resource update ────────────────────────────────────────────────

    /**
     * Update the context's locale for API 24+.
     *
     * Creates a new configuration context with the specified locale
     * and notifies AppCompatDelegate.
     */
    fun updateResources(context: Context, language: String): Context {
        val locale = Locale(language)
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.create(locale))
        val config = context.resources.configuration
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }

    /**
     * Update the context's locale for legacy devices (API ≤ 24).
     *
     * Also calls [Resources.updateConfiguration] which is deprecated but
     * necessary for older Android versions that don't support per-app language.
     */
    fun updateResourcesLegacy(context: Context, language: String): Context {
        val locale = Locale(language)
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.create(locale))
        val resources = context.resources
        val config = resources.configuration
        config.setLocale(locale)
        val newContext = context.createConfigurationContext(config)
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N) {
            @Suppress("DEPRECATION")
            resources.updateConfiguration(config, resources.displayMetrics)
        }
        return newContext
    }
}
