package hazem.nurmontage.videoquran.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import hazem.nurmontage.videoquran.model.Template

/**
 * Template persistence layer using Gson serialization via [SharedPreferences].
 *
 * Templates are serialized to JSON using [Gson] and stored as string values
 * in a shared preferences file named "MTemplate". Each template is identified
 * by a unique string key.
 *
 * Operations:
 * - [readObjectFromFile] — Deserialize a template from SharedPreferences
 * - [writeTemplate] — Serialize and save, removing the old key
 * - [duplicateTemplate] — Serialize and save under a new key
 * - [deleteTemplate] — Remove a template by key
 *
 * Converted from LocalPersistence.java — serialization logic preserved exactly.
 */
object LocalPersistence {

    private const val PREFS_NAME = "MTemplate"

    /**
     * Read and deserialize a [Template] from SharedPreferences.
     *
     * Uses [GsonBuilder] without pretty-printing for maximum compatibility
     * with the original serialization format.
     *
     * @param context The application context
     * @param key     The preference key under which the template is stored
     * @return The deserialized [Template], or null on error
     */
    fun readObjectFromFile(context: Context, key: String): Any? {
        return try {
            val json = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(key, "") ?: ""
            GsonBuilder().create().fromJson(json, Template::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Write a template to SharedPreferences, replacing the old key.
     *
     * The old key is removed first, then the object is serialized under
     * the new key. This supports template rename operations.
     *
     * @param context The application context
     * @param obj     The object to serialize (typically a [Template])
     * @param oldKey  The previous key to remove (may be the same as newKey)
     * @param newKey  The new key under which to store the serialized data
     */
    fun writeTemplate(context: Context, obj: Any?, oldKey: String, newKey: String) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val json = Gson().toJson(obj)
            prefs.edit()
                .remove(oldKey)
                .putString(newKey, json)
                .apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Duplicate a template by serializing it under a new key.
     *
     * Unlike [writeTemplate], this does not remove any existing key.
     *
     * @param context The application context
     * @param obj     The object to serialize
     * @param key     The key under which to store the duplicate
     */
    fun duplicateTemplate(context: Context, obj: Any?, key: String) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val json = Gson().toJson(obj)
            prefs.edit().putString(key, json).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Delete a template by removing its key from SharedPreferences.
     *
     * @param context The application context
     * @param key     The key to remove
     */
    fun deleteTemplate(context: Context, key: String) {
        try {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .remove(key)
                .apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
