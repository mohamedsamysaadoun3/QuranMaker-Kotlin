package hazem.nurmontage.videoquran.utils

import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import java.io.File

/**
 * Scans a media file so that it appears in the Android Gallery / MediaStore.
 *
 * After exporting a video or saving an image, the file may not immediately
 * appear in the system's media database (Gallery, Photos app, etc.).
 * This class uses [MediaScannerConnection] to trigger a scan, which causes
 * the MediaStore ContentProvider to index the file and make it visible.
 *
 * Usage:
 * ```kotlin
 * FileMediaScanner(context, File("/storage/emulated/0/Movies/output.mp4"))
 * ```
 *
 * The scan is fire-and-forget — the connection auto-disconnects after
 * [onScanCompleted] is called.
 *
 * Converted from FileMediaScanner.java — logic preserved exactly.
 */
class FileMediaScanner(
    context: Context,
    private val file: File
) : MediaScannerConnection.MediaScannerConnectionClient {

    private val mediaScannerConnection: MediaScannerConnection =
        MediaScannerConnection(context, this)

    init {
        mediaScannerConnection.connect()
    }

    /**
     * Called when the MediaScannerService connects.
     *
     * Triggers the actual scan of [file]'s absolute path with null MIME type
     * (auto-detect from file extension).
     */
    override fun onMediaScannerConnected() {
        mediaScannerConnection.scanFile(file.absolutePath, null)
    }

    /**
     * Called when the scan of the file completes.
     *
     * Disconnects the [MediaScannerConnection] since the scan is done.
     *
     * @param path  The file path that was scanned
     * @param uri   The content URI of the scanned file (may be null if not found)
     */
    override fun onScanCompleted(path: String?, uri: Uri?) {
        mediaScannerConnection.disconnect()
    }
}
