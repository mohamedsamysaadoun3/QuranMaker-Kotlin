package hazem.nurmontage.videoquran.utils

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utility for extracting file metadata (name, dates) from URIs and file paths.
 *
 * The [getFileInfo] method resolves file information from multiple sources:
 * 1. **Content URI** — queries ContentResolver for `_display_name` and `date_modified`
 * 2. **File URI / direct path** — reads [File.getName] and [File.lastModified]
 * 3. **Fallback** — uses [Uri.getLastPathSegment] as the file name
 *
 * The `date_modified` column from MediaStore is in **seconds** (Unix epoch),
 * so it is multiplied by 1000 to convert to milliseconds for [Date] formatting.
 *
 * Converted from MFileUtils.java — JADX failed on getFileInfo(), reconstructed from smali.
 */
object MFileUtils {

    /**
     * Holds metadata about a file.
     *
     * @property name         The display name of the file
     * @property lastModified The last-modified timestamp in milliseconds (Unix epoch)
     * @property formattedDate Short date string (e.g. "Jan 15-2024")
     * @property timedDate    Time string (e.g. "14:30:45")
     */
    data class FileInfo(
        val name: String,
        val lastModified: Long,
        val formattedDate: String = formatDateShort(lastModified),
        val timedDate: String = if (lastModified > 0)
            SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(lastModified))
        else ""
    ) : Serializable

    /**
     * Extract file metadata from a URI string or file path.
     *
     * Resolution strategy (in order):
     * 1. If the URI scheme is `content://` -> query ContentResolver for `_display_name`
     *    and `date_modified` columns. The `date_modified` value is in seconds and
     *    is multiplied by 1000 to convert to milliseconds.
     * 2. If the name or date is still missing, try creating a [File] from the path
     *    (or URI path if the scheme is `file://`) and read its properties.
     * 3. If the name is still null, fall back to [Uri.getLastPathSegment].
     *
     * @param context The context for ContentResolver access
     * @param path    The URI string or file path
     * @return [FileInfo] with the resolved metadata, or null if path is null
     */
    fun getFileInfo(context: Context, path: String?): FileInfo? {
        if (path == null) return null

        val uri = Uri.parse(path)
        var name: String? = null
        var lastModified: Long = 0

        // -- Strategy 1: Content URI --
        if ("content".equals(uri.scheme, ignoreCase = true)) {
            var cursor: android.database.Cursor? = null
            try {
                cursor = context.contentResolver.query(
                    uri,
                    arrayOf("_display_name", "date_modified"),
                    null, null, null
                )
                if (cursor != null && cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex("_display_name")
                    val dateIndex = cursor.getColumnIndex("date_modified")

                    if (nameIndex != -1) {
                        name = cursor.getString(nameIndex)
                    }
                    if (dateIndex != -1) {
                        val dateValue = cursor.getLong(dateIndex)
                        if (dateValue > 0) {
                            // MediaStore date_modified is in seconds -> convert to ms
                            lastModified = dateValue * 1000
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                cursor?.close()
            }
        }

        // -- Strategy 2: File URI or direct path --
        if (name == null || lastModified == 0L) {
            try {
                val file = if ("file".equals(uri.scheme, ignoreCase = true)) {
                    File(uri.path ?: path)
                } else {
                    File(path)
                }
                if (file.exists()) {
                    if (name == null) name = file.name
                    if (lastModified == 0L) lastModified = file.lastModified()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // -- Strategy 3: Fallback to URI last path segment --
        if (name == null) name = uri.lastPathSegment

        return FileInfo(name ?: "unknown", lastModified)
    }

    /**
     * Format a timestamp as a short date string.
     *
     * Output format: "MMM dd-yyyy" (e.g. "Jan 15-2024") using English locale.
     *
     * @param timestamp The timestamp in milliseconds, or <= 0 for empty string
     * @return The formatted date string, or empty string if timestamp is invalid
     */
    fun formatDateShort(timestamp: Long): String {
        if (timestamp <= 0) return ""
        return SimpleDateFormat("MMM dd-yyyy", Locale.ENGLISH).format(Date(timestamp))
    }
}
