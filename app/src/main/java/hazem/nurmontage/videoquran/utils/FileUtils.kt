package hazem.nurmontage.videoquran.utils

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import androidx.media3.common.MimeTypes
import hazem.nurmontage.videoquran.core.common.Constants
import java.io.File

/**
 * File utility class for directory management, URI resolution, and video export paths.
 *
 * Originally: FileUtils.java
 * Converted to: FileUtils.kt — idiomatic Kotlin, null-safe, use{} for Cursor cleanup
 *
 * Provides three core functionalities:
 * 1. **Working directory management** — Creates timestamped work directories
 *    and video frame subdirectories for the rendering pipeline
 * 2. **URI-to-File resolution** — Resolves content URIs from SAF (Storage Access
 *    Framework), Downloads, and MediaStore to actual file paths
 * 3. **File existence checks** — Simple existence verification for paths
 *
 * Memory Management:
 * - Cursor resources are properly closed in finally blocks / use{} blocks
 * - No bitmap or stream caching (stateless utility)
 */
object FileUtils {

    private const val TAG = "FileUtils"

    /**
     * Checks whether a file exists at the given path.
     *
     * @param path The absolute file path to check
     * @return true if the file exists, false otherwise
     */
    fun checkFileExists(path: String): Boolean = File(path).exists()

    /**
     * Creates a new timestamped working directory in the app's external files directory.
     * Used by the rendering engine to organize output files per session.
     *
     * Directory structure: [externalFilesDir]/Work_[timestamp]/
     *
     * @param context Android context for accessing external files directory
     * @return The created working directory, or null on failure
     */
    fun getFile(context: Context): File? {
        val externalDir = context.getExternalFilesDir(null) ?: return null

        if (!externalDir.exists() && !externalDir.mkdirs()) {
            Log.e(TAG, "Failed to create external files directory")
            return null
        }

        val workDir = File(externalDir, "Work_${System.currentTimeMillis()}")
        if (workDir.exists() || workDir.mkdirs()) {
            return workDir
        }

        Log.e(TAG, "Failed to create work directory")
        return null
    }

    /**
     * Creates or retrieves the video frame output directory.
     * Used by the rendering pipeline to store individual video frames
     * before FFmpeg compilation.
     *
     * Directory structure: [basePath]/[Constants.VIDEO_FRAME_FOLDER]/
     *
     * @param basePath The parent directory path
     * @return The video frame directory, or null on failure
     */
    fun getFileVideo(basePath: String): File? {
        val baseDir = File(basePath)
        if (!baseDir.exists() && !baseDir.mkdirs()) {
            Log.e(TAG, "Failed to create base directory: $basePath")
            return null
        }

        val frameDir = File(baseDir, Constants.VIDEO_FRAME_FOLDER)
        if (frameDir.exists() || frameDir.mkdirs()) {
            return frameDir
        }

        Log.e(TAG, "Failed to create video frame directory")
        return null
    }

    /**
     * Resolves a content URI to an actual File object.
     * Handles multiple URI schemes:
     * - Document URIs (SAF): External storage, Downloads, Media documents
     * - Content URIs: Generic content provider queries
     * - File URIs: Direct file:// paths
     *
     * @param context Android context for content resolver access
     * @param uri The URI to resolve
     * @return The resolved File object
     * @throws Exception if the URI cannot be resolved
     */
    fun getFileFromUri(context: Context, uri: Uri): File {
        val filePath = when {
            DocumentsContract.isDocumentUri(context, uri) -> {
                resolveDocumentUri(context, uri)
            }
            uri.scheme?.equals("content", ignoreCase = true) == true -> {
                getDataColumn(context, uri, null, null)
            }
            uri.scheme?.equals("file", ignoreCase = true) == true -> {
                uri.path
            }
            else -> null
        }

        return File(filePath ?: throw Exception("Cannot resolve URI: $uri"))
    }

    /**
     * Resolves a document URI based on its type (external storage, downloads, or media).
     */
    private fun resolveDocumentUri(context: Context, uri: Uri): String? {
        return when {
            isExternalStorageDocument(uri) -> {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":")
                if (split[0].equals("primary", ignoreCase = true)) {
                    "${Environment.getExternalStorageDirectory()}/${split[1]}"
                } else null
            }
            isDownloadsDocument(uri) -> {
                val docId = DocumentsContract.getDocumentId(uri)
                val contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"),
                    docId.toLongOrNull() ?: return null
                )
                getDataColumn(context, contentUri, null, null)
            }
            isMediaDocument(uri) -> {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":")
                val contentUri = when (split[0]) {
                    "image" -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    "video" -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    "audio" -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    else -> return null
                }
                getDataColumn(context, contentUri, "_id=?", arrayOf(split[1]))
            }
            else -> null
        }
    }

    /**
     * Queries a content resolver for the "_data" column of the given URI.
     * Uses Kotlin's [use] extension for automatic Cursor cleanup.
     *
     * @param context Android context for content resolver
     * @param uri The content URI to query
     * @param selection Optional WHERE clause (without "WHERE")
     * @param selectionArgs Optional WHERE arguments
     * @return The file path string, or null if not found
     */
    fun getDataColumn(
        context: Context,
        uri: Uri,
        selection: String?,
        selectionArgs: Array<String>?
    ): String? {
        try {
            context.contentResolver.query(
                uri, arrayOf("_data"),
                selection, selectionArgs, null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    return cursor.getString(cursor.getColumnIndexOrThrow("_data"))
                }
            }
        } catch (_: Exception) {
            // Silently handle column not found or other query errors
        }
        return null
    }

    /** Checks if the URI authority belongs to external storage documents. */
    fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    /** Checks if the URI authority belongs to downloads documents. */
    fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    /** Checks if the URI authority belongs to media documents. */
    fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }
}
