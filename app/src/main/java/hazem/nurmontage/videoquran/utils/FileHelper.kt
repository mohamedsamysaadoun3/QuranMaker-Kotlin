package hazem.nurmontage.videoquran.utils

import android.content.Context
import android.os.Environment
import java.io.File

/**
 * Helper for creating and managing video output directories.
 *
 * Provides two folder creation strategies:
 * - **App-private**: Uses [Context.getExternalFilesDir] (deleted on app uninstall)
 * - **Public**: Uses [Environment.getExternalStoragePublicDirectory] (persists after uninstall)
 *
 * Both methods ensure the folder exists by creating it (and any parent directories)
 * if necessary, and return the [File] reference on success or `null` on failure.
 *
 * Converted from FileHelper.java — logic preserved exactly.
 */
class FileHelper(private val context: Context) {

    /**
     * Create a video folder in the app's private external storage.
     *
     * The folder is created under `Movies/<subFolder>` within the app-specific
     * external directory. Files here are deleted when the app is uninstalled.
     *
     * Example path: `/storage/emulated/0/Android/data/<pkg>/files/Movies/<subFolder>`
     *
     * @param subFolder The sub-folder name under the Movies directory
     * @return The created [File] directory, or `null` if creation failed
     */
    fun createVideoFolder(subFolder: String): File? {
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_MOVIES), subFolder)
        if (!file.exists()) {
            if (file.mkdirs()) {
                println("Folder created successfully: ${file.absolutePath}")
            } else {
                System.err.println("Failed to create folder: ${file.absolutePath}")
                return null
            }
        }
        return file
    }

    /**
     * Create a video folder in the public external storage.
     *
     * The folder is created under `Movies/<subFolder>` in the shared public
     * storage directory. Files here persist even after the app is uninstalled.
     *
     * Example path: `/storage/emulated/0/Movies/<subFolder>`
     *
     * @param subFolder The sub-folder name under the public Movies directory
     * @return The created [File] directory, or `null` if creation failed
     */
    fun createPublicVideoFolder(subFolder: String): File? {
        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), subFolder)
        if (!file.exists()) {
            if (file.mkdirs()) {
                println("Folder created successfully: ${file.absolutePath}")
            } else {
                System.err.println("Failed to create folder: ${file.absolutePath}")
                return null
            }
        }
        return file
    }
}
