package hazem.nurmontage.videoquran.ui.render

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.PowerManager
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFmpegSession
import com.arthenica.ffmpegkit.FFmpegSessionCompleteCallback
import com.arthenica.ffmpegkit.ReturnCode
import com.arthenica.ffmpegkit.Statistics
import com.arthenica.ffmpegkit.StatisticsCallback
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.core.base.BaseActivity
import hazem.nurmontage.videoquran.model.EntityMedia
import hazem.nurmontage.videoquran.model.RenderManager
import hazem.nurmontage.videoquran.model.SquareBitmapModel
import hazem.nurmontage.videoquran.model.Template
import hazem.nurmontage.videoquran.utils.LocalPersistence
import hazem.nurmontage.videoquran.utils.LocaleHelper
import hazem.nurmontage.videoquran.utils.audio.AudioUtils
import hazem.nurmontage.videoquran.utils.audio.FfmpegCodecChecker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Semaphore
import kotlin.math.max

/**
 * Activity that displays export progress and orchestrates the FFmpeg rendering pipeline.
 *
 * This is a **thin UI shell** that:
 * - Displays a progress indicator during video export
 * - Delegates FFmpeg command composition to [ExportCommandBuilder]
 * - Executes pre-render steps (masked segments, video layers, timer overlay)
 * - Manages FFmpeg session lifecycle and cancellation
 *
 * **Threading model (Kotlin rewrite)**:
 * - All background work uses `lifecycleScope.launch(Dispatchers.IO)`
 * - UI updates use `withContext(Dispatchers.Main)`
 * - Pre-render methods still accept `CountDownLatch`/`Semaphore` because
 *   [ExportCommandBuilder.buildCommand] coordinates them with the legacy pattern;
 *   the Activity's own flow is fully coroutine-based.
 *
 * Originally: ProgressViewActivity.java (~1212 lines)
 * Converted to: ProgressViewActivity.kt — structural skeleton (~600 lines)
 */
class ProgressViewActivity : BaseActivity() {

    // ════════════════════════════════════════════════════════════════════
    //  Fields
    // ════════════════════════════════════════════════════════════════════

    private var cancelDialog: Dialog? = null
    private var isCancel: Boolean = false

    @Volatile
    private var isDestroy: Boolean = false

    private var mTemplate: Template? = null
    private var mUri: String? = null

    /** Progress bar — TODO: replace with SquareOutlineProgressBar once migrated. */
    private lateinit var progressIndicator: View

    private var statistics: Statistics? = null

    /** Overlay string builder for filter_complex composition. */
    private val overlay = StringBuilder()

    /** Weighted multi-step render progress tracker. */
    private val renderManager = RenderManager()

    /** Active FFmpeg session IDs for cancellation. */
    private val id_ffmpeg = mutableListOf<Long>()

    /** Smooth-progress animation state. */
    private var displayedProgress: Float = 0f
    private var targetProgress: Float = 0f
    private var isAnimating: Boolean = false

    /** Wake lock reference for screen-on during export. */
    private var wakeLock: PowerManager.WakeLock? = null

    /** Back-press handler — triggers cancel dialog instead of immediate exit. */
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            showCancelDialog()
        }
    }

    // ════════════════════════════════════════════════════════════════════
    //  Lifecycle
    // ════════════════════════════════════════════════════════════════════

    override fun onCreate(savedInstanceState: Bundle?) {
        // FLAG_KEEP_SCREEN_ON (0x00000400) | FLAG_TURN_SCREEN_ON (0x02000000)
        // Original Java used 1536 which is 0x600 — preserved verbatim.
        @Suppress("MagicNumber")
        window.setFlags(1536, 1536)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_progress_view)

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        // Full dark system bars
        setStatusBarColor(ViewCompat.MEASURED_STATE_MASK)
        setNavigationBarColor(ViewCompat.MEASURED_STATE_MASK)

        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = false
        insetsController.isAppearanceLightNavigationBars = false

        // Edge-to-edge insets
        val mainView = findViewById<View>(R.id.main)
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView) { view, insets ->
                val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
                insets
            }
        }

        wakeLockAcquire()

        // TODO: Replace with SquareOutlineProgressBar when custom view is migrated
        progressIndicator = findViewById(R.id.progress_horizontal)
        findViewById<View>(R.id.btn_cancel)?.setOnClickListener { showCancelDialog() }

        try {
            startExport()
        } catch (e: Exception) {
            toStudio()
        }
    }

    override fun onPause() {
        super.onPause()
        // Preserve partial wake lock if export is running
    }

    override fun onDestroy() {
        isDestroy = true
        clearFFmpeg()
        cancelDialog?.dismiss()
        cancelDialog = null
        releaseWakeLock()
        super.onDestroy()
    }

    // ════════════════════════════════════════════════════════════════════
    //  Wake lock
    // ════════════════════════════════════════════════════════════════════

    /**
     * Acquire a partial wake lock so the CPU stays active during export.
     *
     * Falls back to window-flag based screen-on if [PowerManager] is
     * unavailable.
     */
    private fun wakeLockAcquire() {
        try {
            val pm = getSystemService(POWER_SERVICE) as? PowerManager ?: run {
                // Fallback to BaseActivity's window-flag approach
                super.wakeLockAcquire()
                return
            }
            wakeLock = pm.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "QuranMaker::ExportWakeLock"
            ).apply {
                acquire(10 * 60 * 1000L) // 10 min max
            }
        } catch (_: Exception) {
            // Window flag keeps screen on as fallback
            super.wakeLockAcquire()
        }
    }

    private fun releaseWakeLock() {
        try {
            wakeLock?.let {
                if (it.isHeld) it.release()
            }
        } catch (_: Exception) {
            // Ignore
        }
        wakeLock = null
    }

    // ════════════════════════════════════════════════════════════════════
    //  Export pipeline
    // ════════════════════════════════════════════════════════════════════

    /**
     * Entry point for the export pipeline.
     *
     * Reads the [Template] from the intent, prepares all media files,
     * detects available FFmpeg codecs, then calls [setupCommand].
     */
    private fun startExport() {
        val templateKey = intent.getStringExtra("template_key")
            ?: intent.getStringExtra("idTemplate")
        if (templateKey != null) {
            mTemplate = LocalPersistence.readObjectFromFile(this, templateKey) as? Template
        }
        if (mTemplate == null) {
            // Try reading from intent serializable extra
            @Suppress("DEPRECATION")
            mTemplate = intent.getSerializableExtra("template") as? Template
        }

        val template = mTemplate ?: run {
            toStudio()
            return
        }

        mUri = template.uri_video ?: template.uri_media_video

        lifecycleScope.launch(Dispatchers.IO) {
            // Step 1: Download / copy all media assets to local storage
            prepareAllMedia(template.getEntityMediaList(), null)

            // Step 2: Detect FFmpeg codecs (callback on main thread)
            withContext(Dispatchers.Main) {
                FfmpegCodecChecker.detectCodecsAsync { codecInfo ->
                    setupCommand(codecInfo)
                }
            }
        }
    }

    /**
     * Prepare all media files — downloads from HTTPS or copies from local URIs.
     *
     * Replaces the original `Executor + Handler` pattern with a coroutine-based
     * approach. Runs on [Dispatchers.IO]; calls [callback] when all media is ready.
     *
     * @param list     Media entities to prepare, or null for no-op
     * @param callback Optional callback after all media is prepared
     */
    private suspend fun prepareAllMedia(list: List<EntityMedia>?, callback: Runnable?) {
        if (list.isNullOrEmpty()) {
            callback?.run()
            return
        }

        val latch = CountDownLatch(list.size)
        for (media in list) {
            val paths = media.paths_https
            if (!paths.isNullOrEmpty()) {
                lifecycleScope.launch(Dispatchers.IO) {
                    for (url in paths) {
                        try {
                            val localPath = downloadToCache(url, media)
                            if (localPath != null) {
                                media.path_ffmpeg = localPath
                            }
                        } catch (_: Exception) {
                            // Continue with remaining URLs
                        }
                    }
                    latch.countDown()
                }
            } else {
                // Already local — nothing to prepare
                latch.countDown()
            }
        }

        // Wait for all downloads/copies to complete
        latch.await()
        callback?.run()
    }

    /**
     * Download a remote file to the template cache directory.
     * @return The local path, or null on failure.
     */
    private fun downloadToCache(url: String, media: EntityMedia): String? {
        val template = mTemplate ?: return null
        val destDir = template.folder_template ?: cacheDir.absolutePath
        return AudioUtils.copyFromUri(this, android.net.Uri.parse(url), destDir)
    }

    /**
     * **DELEGATES** command composition to [ExportCommandBuilder].
     *
     * After the builder produces the final FFmpeg command array,
     * it is handed to [export] for execution.
     */
    private fun setupCommand(codecInfo: FfmpegCodecChecker.CodecInfo) {
        val template = mTemplate ?: return

        lifecycleScope.launch(Dispatchers.IO) {
            val command = ExportCommandBuilder.buildCommand(
                template = template,
                codecInfo = codecInfo,
                preRenderMask_Rounded = { model, duration, latch, sem ->
                    preRenderMask_Rounded(model, duration, latch, sem)
                },
                preRenderMask_Circle = { model, duration, latch, sem ->
                    preRenderMask_Circle(model, duration, latch, sem)
                },
                preRender_NoMask = { model, duration, latch, sem, codec ->
                    preRender_NoMask(model, duration, latch, sem, codec)
                },
                preRenderVideo = { duration, latch, sem, codec ->
                    preRenderVideo(duration, latch, sem, codec)
                },
                preRenderVideoHue = { duration, latch, sem, codec ->
                    preRenderVideoHue(duration, latch, sem, codec)
                },
                generateVideoTimer = { duration, latch, sem ->
                    generateVideoTimer(duration, latch, sem)
                }
            )

            withContext(Dispatchers.Main) {
                if (command != null) {
                    export(command)
                } else {
                    toStudio()
                }
            }
        }
    }

    // ════════════════════════════════════════════════════════════════════
    //  FFmpeg execution
    // ════════════════════════════════════════════════════════════════════

    /**
     * Execute the final FFmpeg command with progress tracking.
     *
     * Registers a [StatisticsCallback] for progress updates and a
     * [FFmpegSessionCompleteCallback] for completion handling.
     */
    private fun export(command: Array<String>) {
        if (isCancel || isDestroy) return

        val template = mTemplate ?: return
        @Suppress("unused")
        val durationSec = max(template.duration / 1000, 1)

        val session: FFmpegSession = FFmpegKit.executeWithArgumentsAsync(
            command,
            FFmpegSessionCompleteCallback { session ->
                if (session == null || isDestroy) return@FFmpegSessionCompleteCallback

                val returnCode = session.returnCode
                if (ReturnCode.isSuccess(returnCode)) {
                    // Export complete — navigate to result
                    lifecycleScope.launch(Dispatchers.Main) {
                        updateProgressSmooth(100f)
                        onExportComplete()
                    }
                } else {
                    // Export failed or cancelled
                    if (!isCancel && !ReturnCode.isCancel(returnCode)) {
                        lifecycleScope.launch(Dispatchers.Main) {
                            toStudio()
                        }
                    }
                }
            },
            StatisticsCallback { stats ->
                if (stats != null) {
                    statistics = stats
                    updateProgressDialog(stats)
                }
            },
            null // Log callback
        )

        id_ffmpeg.add(session.sessionId)
    }

    /**
     * Update the progress indicator from FFmpeg [Statistics].
     *
     * Calculates progress as `time_ms / total_duration_ms` and
     * feeds it through the [RenderManager] for weighted multi-step tracking.
     */
    private fun updateProgressDialog(stats: Statistics) {
        val template = mTemplate ?: return
        val totalDurationMs = max(template.duration, 500)
        val currentTimeMs = stats.time

        val localProgress = (currentTimeMs.toFloat() / totalDurationMs.toFloat()).coerceIn(0f, 1f)
        val globalProgress = renderManager.updateLocalProgress(localProgress)
        val percent = globalProgress * 100f

        updateProgressSmooth(percent)
    }

    /**
     * Smoothly animate the progress indicator toward [targetPercent].
     *
     * Uses a simple lerp approach — increments the displayed value
     * toward the target each frame instead of jumping.
     */
    private fun updateProgressSmooth(targetPercent: Float) {
        targetProgress = targetPercent
        if (isAnimating) return
        isAnimating = true

        // Simple step-based animation on main thread
        lifecycleScope.launch(Dispatchers.Main) {
            while (displayedProgress < targetProgress - 0.5f) {
                displayedProgress += (targetProgress - displayedProgress) * 0.15f + 0.1f
                displayedProgress = displayedProgress.coerceIn(0f, 100f)
                setProgressVisual(displayedProgress)
                delay(16) // ~60fps
            }
            displayedProgress = targetProgress
            setProgressVisual(displayedProgress)
            isAnimating = false
        }
    }

    /**
     * Set the visual progress on the progress indicator.
     * TODO: Wire up SquareOutlineProgressBar when migrated.
     */
    private fun setProgressVisual(percent: Float) {
        (progressIndicator as? ProgressBar)?.let {
            it.progress = percent.toInt()
        } ?: run {
            // Fallback: update progress text if available
            (progressIndicator as? TextView)?.text = "${percent.toInt()}%"
        }
    }

    // ════════════════════════════════════════════════════════════════════
    //  Pre-render methods — execute individual FFmpeg pipeline steps
    //  Each method builds the FFmpeg args, executes asynchronously,
    //  waits on a CountDownLatch, then signals via the outer latch/semaphore.
    // ════════════════════════════════════════════════════════════════════

    /**
     * Pre-render a rounded-rectangle masked video segment.
     *
     * Uses [ExportCommandBuilder.preRenderMaskRounded] to build the command,
     * then executes it via [FFmpegKit]. The [latch] is counted down and
     * [semaphore] is released when done so [ExportCommandBuilder] can proceed.
     *
     * @return Output file path, or null on failure
     */
    private fun preRenderMask_Rounded(
        model: SquareBitmapModel,
        durationMs: Int,
        latch: CountDownLatch,
        semaphore: Semaphore
    ): String? {
        val template = mTemplate ?: return null.also { updateNext(latch, semaphore) }

        try {
            val (args, outputPath, _) = ExportCommandBuilder.preRenderMaskRounded(
                template, model, durationMs, filesDir
            )

            val syncLatch = CountDownLatch(1)
            val session = FFmpegKit.executeWithArgumentsAsync(args) { completedSession ->
                completedSession?.sessionId?.let { id_ffmpeg.remove(it) }
                syncLatch.countDown()
            }
            id_ffmpeg.add(session.sessionId)

            syncLatch.await()
            return if (File(outputPath).exists()) outputPath else null
        } catch (e: Exception) {
            return null
        } finally {
            updateNext(latch, semaphore)
        }
    }

    /**
     * Pre-render a circle-masked video segment.
     *
     * @return Output file path, or null on failure
     */
    private fun preRenderMask_Circle(
        model: SquareBitmapModel,
        durationMs: Int,
        latch: CountDownLatch,
        semaphore: Semaphore
    ): String? {
        val template = mTemplate ?: return null.also { updateNext(latch, semaphore) }

        try {
            val outputPath = "${template.folder_template}/circle_${System.currentTimeMillis()}.mov"
            val maxSize = max(template.width, template.height)
            var w = Math.round(model.width_sqaure)
            var h = Math.round(model.height_square)
            if ((w and 1) == 1) w++
            if ((h and 1) == 1) h++

            val maskFile = ExportCommandBuilder.getOrCreateMaskCircle(
                w, h, template.folder_template ?: filesDir.absolutePath
            )

            val filterComplex = "[0:v]scale=$maxSize:$maxSize:force_original_aspect_ratio=increase," +
                    "crop=${Math.round(model.right)}:${Math.round(model.bottom)}:" +
                    "${Math.round(model.lef_square)}:${Math.round(model.top_square)}," +
                    "scale=$w:$h:flags=lanczos[v];[v][1:v]alphamerge,format=rgba"

            val args = arrayOf(
                "-hide_banner", "-y",
                "-stream_loop", "-1",
                "-i", template.uri_media_video ?: "",
                "-i", maskFile.absolutePath,
                "-filter_complex", filterComplex,
                "-c:v", "qtrle", "-pix_fmt", "rgba",
                "-r", "25",
                "-t", "${max(durationMs, 500)}ms",
                outputPath
            )

            val syncLatch = CountDownLatch(1)
            val session = FFmpegKit.executeWithArgumentsAsync(args) { completedSession ->
                completedSession?.sessionId?.let { id_ffmpeg.remove(it) }
                syncLatch.countDown()
            }
            id_ffmpeg.add(session.sessionId)

            syncLatch.await()
            return if (File(outputPath).exists()) outputPath else null
        } catch (e: Exception) {
            return null
        } finally {
            updateNext(latch, semaphore)
        }
    }

    /**
     * Pre-render a video segment without mask (direct overlay).
     *
     * @return Output file path, or null on failure
     */
    private fun preRender_NoMask(
        model: SquareBitmapModel,
        durationMs: Int,
        latch: CountDownLatch,
        semaphore: Semaphore,
        codec: String?
    ): String? {
        val template = mTemplate ?: return null.also { updateNext(latch, semaphore) }

        try {
            val outputPath = "${template.folder_template}/nomask_${System.currentTimeMillis()}.mov"
            val maxSize = max(template.width, template.height)
            var w = Math.round(model.width_sqaure)
            var h = Math.round(model.height_square)
            if ((w and 1) == 1) w++
            if ((h and 1) == 1) h++

            val filterComplex = "[0:v]scale=$maxSize:$maxSize:force_original_aspect_ratio=increase," +
                    "crop=${Math.round(model.right)}:${Math.round(model.bottom)}:" +
                    "${Math.round(model.lef_square)}:${Math.round(model.top_square)}," +
                    "scale=$w:$h:flags=lanczos,format=rgba"

            val args = mutableListOf(
                "-hide_banner", "-y",
                "-stream_loop", "-1",
                "-i", template.uri_media_video ?: "",
                "-filter_complex", filterComplex
            )

            if (codec != null) {
                args.addAll(listOf("-threads", "0", "-c:v", codec, "-preset", "fast", "-crf", "18"))
            } else {
                args.addAll(listOf("-c:v", "qtrle", "-pix_fmt", "rgba"))
            }

            args.addAll(listOf("-r", "25", "-t", "${max(durationMs, 500)}ms", outputPath))

            val syncLatch = CountDownLatch(1)
            val session = FFmpegKit.executeWithArgumentsAsync(args.toTypedArray()) { completedSession ->
                completedSession?.sessionId?.let { id_ffmpeg.remove(it) }
                syncLatch.countDown()
            }
            id_ffmpeg.add(session.sessionId)

            syncLatch.await()
            return if (File(outputPath).exists()) outputPath else null
        } catch (e: Exception) {
            return null
        } finally {
            updateNext(latch, semaphore)
        }
    }

    /**
     * Pre-render the main video layer with background overlay.
     *
     * Delegates to [ExportCommandBuilder.preRenderVideo] for command composition.
     *
     * @return Output file path, or null on failure
     */
    private fun preRenderVideo(
        durationMs: Int,
        latch: CountDownLatch,
        semaphore: Semaphore,
        codec: String?
    ): String? {
        val template = mTemplate ?: return null.also { updateNext(latch, semaphore) }

        try {
            val (args, outputPath) = ExportCommandBuilder.preRenderVideo(template, durationMs, codec)
            if (args == null) return null.also { updateNext(latch, semaphore) }

            val syncLatch = CountDownLatch(1)
            val session = FFmpegKit.executeWithArgumentsAsync(args) { completedSession ->
                completedSession?.sessionId?.let { id_ffmpeg.remove(it) }
                syncLatch.countDown()
            }
            id_ffmpeg.add(session.sessionId)

            syncLatch.await()
            return if (File(outputPath).exists()) outputPath else null
        } catch (e: Exception) {
            return null
        } finally {
            updateNext(latch, semaphore)
        }
    }

    /**
     * Pre-render a video segment with hue/saturation adjustment.
     *
     * Applies `hue=s=X:b=Y` filter to shift the color tone.
     *
     * @return Output file path, or null on failure
     */
    private fun preRenderVideoHue(
        durationMs: Int,
        latch: CountDownLatch,
        semaphore: Semaphore,
        codec: String?
    ): String? {
        val template = mTemplate ?: return null.also { updateNext(latch, semaphore) }

        try {
            val outputPath = "${template.folder_template}/hue_${System.currentTimeMillis()}.mp4"
            val maxSize = max(template.width, template.height)
            val filterComplex = "[0:v]scale=$maxSize:$maxSize:force_original_aspect_ratio=increase:flags=lanczos," +
                    "crop=${template.width}:${template.height}:" +
                    "(iw-${template.width})/2:(ih-${template.height})/2," +
                    "hue=s=1.2:b=0.05[v];" +
                    "[v][1:v]overlay,format=rgba"

            val bgPath = template.uri_bg_ffmpeg
            if (bgPath == null || !File(bgPath).let { it.exists() && it.isFile }) {
                return null.also { updateNext(latch, semaphore) }
            }

            val args = mutableListOf(
                "-hide_banner", "-y",
                "-stream_loop", "-1",
                "-i", template.uri_media_video ?: "",
                "-i", bgPath,
                "-filter_complex", filterComplex
            )

            if (codec != null) {
                args.addAll(listOf("-threads", "0", "-c:v", codec, "-preset", "fast", "-crf", "18"))
            } else {
                args.addAll(listOf("-b:v", "4M"))
            }

            args.addAll(listOf(
                "-r", template.fps.toString(),
                "-t", "${max(durationMs, 500)}ms",
                "-movflags", "+faststart",
                "-an",
                outputPath
            ))

            val syncLatch = CountDownLatch(1)
            val session = FFmpegKit.executeWithArgumentsAsync(args.toTypedArray()) { completedSession ->
                completedSession?.sessionId?.let { id_ffmpeg.remove(it) }
                syncLatch.countDown()
            }
            id_ffmpeg.add(session.sessionId)

            syncLatch.await()
            return if (File(outputPath).exists()) outputPath else null
        } catch (e: Exception) {
            return null
        } finally {
            updateNext(latch, semaphore)
        }
    }

    /**
     * Generate a timer overlay video using `drawtext` filter.
     *
     * Delegates to [ExportCommandBuilder.generateVideoTimerArgs] for command composition.
     *
     * @return Output file path, or null on failure
     */
    private fun generateVideoTimer(
        durationMs: Int,
        latch: CountDownLatch,
        semaphore: Semaphore
    ): String? {
        val template = mTemplate ?: return null.also { updateNext(latch, semaphore) }

        try {
            val (args, outputPath) = ExportCommandBuilder.generateVideoTimerArgs(template, durationMs)

            val syncLatch = CountDownLatch(1)
            val session = FFmpegKit.executeWithArgumentsAsync(args) { completedSession ->
                completedSession?.sessionId?.let { id_ffmpeg.remove(it) }
                syncLatch.countDown()
            }
            id_ffmpeg.add(session.sessionId)

            syncLatch.await()
            return if (File(outputPath).exists()) outputPath else null
        } catch (e: Exception) {
            return null
        } finally {
            updateNext(latch, semaphore)
        }
    }

    // ════════════════════════════════════════════════════════════════════
    //  FFmpeg session management
    // ════════════════════════════════════════════════════════════════════

    /**
     * Cancel all active FFmpeg sessions and clear the session ID list.
     */
    private fun clearFFmpeg() {
        for (id in id_ffmpeg) {
            try {
                FFmpegKit.cancel(id)
            } catch (_: Exception) {
                // Session may have already completed
            }
        }
        id_ffmpeg.clear()
        try {
            FFmpegKit.cancel()
        } catch (_: Exception) {
            // Ignore
        }
    }

    // ════════════════════════════════════════════════════════════════════
    //  Cancel dialog
    // ════════════════════════════════════════════════════════════════════

    /**
     * Show a confirmation dialog before cancelling the export.
     *
     * Localized for Arabic and English:
     * - Arabic: "هل أنت متأكد من مغادرة هذا العمل؟" / "مغادرة" / "متابعة"
     * - English: "Are you sure want to leave this work ?" / "Leave" / "Continue"
     */
    private fun showCancelDialog() {
        if (cancelDialog?.isShowing == true) return

        val isArabic = LocaleHelper.getLanguage(this) == "ar"

        val title = if (isArabic) "خروج..." else "Exit..."
        val message = if (isArabic)
            "هل أنت متأكد من مغادرة هذا العمل؟"
        else
            "Are you sure want to leave this work ?"
        val positiveBtn = if (isArabic) "مغادرة" else "Leave"
        val negativeBtn = if (isArabic) "متابعة" else "Continue"

        cancelDialog = android.app.AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(positiveBtn) { _, _ ->
                isCancel = true
                clearFFmpeg()
                toStudio()
            }
            .setNegativeButton(negativeBtn) { d, _ ->
                d.dismiss()
            }
            .setCancelable(false)
            .create()
            .also { it.show() }
    }

    // ════════════════════════════════════════════════════════════════════
    //  Navigation
    // ════════════════════════════════════════════════════════════════════

    /**
     * Navigate back to the studio / engine screen with a result code.
     *
     * Sets the result to `RESULT_CANCELED` (user cancelled or error)
     * and finishes the activity.
     */
    private fun toStudio() {
        if (isDestroy) return
        setResult(RESULT_CANCELED)
        finish()
    }

    /**
     * Called when the FFmpeg export completes successfully.
     * Sets the output URI as a result extra and finishes.
     */
    private fun onExportComplete() {
        val template = mTemplate ?: run {
            toStudio()
            return
        }

        // The final output video is stored in the template folder
        val outputUri = mUri ?: template.uri_video ?: template.uri_media_video

        val resultIntent = Intent().apply {
            putExtra("output_uri", outputUri)
            putExtra("template_key", template.idTemplate)
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    // ════════════════════════════════════════════════════════════════════
    //  RenderManager coordination helpers
    // ════════════════════════════════════════════════════════════════════

    /**
     * Advance the [RenderManager] to the next step and release the semaphore.
     *
     * Called at the end of each pre-render method to signal completion
     * to [ExportCommandBuilder]'s coordination logic.
     */
    private fun updateNext(latch: CountDownLatch, semaphore: Semaphore) {
        renderManager.nextTask()
        latch.countDown()
        semaphore.release()
    }

    // ════════════════════════════════════════════════════════════════════
    //  File helpers
    // ════════════════════════════════════════════════════════════════════

    /**
     * Create a transparent background PNG for FFmpeg overlay operations.
     *
     * @param width  Image width in pixels
     * @param height Image height in pixels
     * @return The generated [File]
     */
    @Suppress("SameParameterValue")
    private fun createTransparentBg(width: Int, height: Int): File {
        val template = mTemplate ?: return File(cacheDir, "transparent.png")
        val dir = template.folder_template ?: cacheDir.absolutePath
        val file = File(dir, "transparent_${width}x${height}.png")
        if (file.exists()) return file

        val bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        canvas.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR)

        java.io.FileOutputStream(file).use { fos ->
            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, fos)
        }
        return file
    }

    /**
     * Concatenate multiple video segments into a single output file.
     *
     * Uses FFmpeg concat demuxer with a temporary file list.
     *
     * @param segments List of segment file paths to concatenate
     * @return Output file path, or null on failure
     */
    private fun concatVideoSegments(segments: List<String>): String? {
        if (segments.isEmpty()) return null
        if (segments.size == 1) return segments[0]

        val template = mTemplate ?: return null
        val dir = template.folder_template ?: return null
        val outputFile = "$dir/concat_${System.currentTimeMillis()}.mp4"

        // Write concat list file
        val concatList = File(dir, "concat_list.txt")
        concatList.writeText(segments.joinToString("\n") { "file '$it'" })

        val args = arrayOf(
            "-y",
            "-f", "concat",
            "-safe", "0",
            "-i", concatList.absolutePath,
            "-c", "copy",
            "-movflags", "+faststart",
            outputFile
        )

        val syncLatch = CountDownLatch(1)
        var success = false

        val session = FFmpegKit.executeWithArgumentsAsync(args) { completedSession ->
            success = ReturnCode.isSuccess(completedSession?.returnCode)
            completedSession?.sessionId?.let { id_ffmpeg.remove(it) }
            syncLatch.countDown()
        }
        id_ffmpeg.add(session.sessionId)

        syncLatch.await()

        // Clean up temp list
        concatList.delete()

        return if (success && File(outputFile).exists()) outputFile else null
    }

    // ════════════════════════════════════════════════════════════════════
    //  Fade / slide helpers — preserved for ExportCommandBuilder interop
    // ════════════════════════════════════════════════════════════════════

    /**
     * Build a simple fade filter string.
     * Delegates to [ExportCommandBuilder.mFadeFilter].
     */
    @Suppress("unused")
    fun mFadeFilter(startTime: Float, duration: Float, isIn: Boolean): String {
        return ExportCommandBuilder.mFadeFilter(startTime, duration, isIn)
    }

    /**
     * Build a combined fade-in + fade-out filter string.
     * Delegates to [ExportCommandBuilder.fadeInOut].
     */
    @Suppress("unused")
    fun fadeInOut(endTime: Float, fadeInDur: Float, fadeOutDur: Float): String {
        return ExportCommandBuilder.fadeInOut(endTime, fadeInDur, fadeOutDur)
    }

    /**
     * Build a slide-X expression for FFmpeg overlay positioning.
     * Delegates to [ExportCommandBuilder.slideX].
     */
    @Suppress("unused")
    fun slideX(start: Float, duration: Float, offset: Float, scale: Float, from: Float, to: Float): String {
        return ExportCommandBuilder.slideX(start, duration, offset, scale, from, to)
    }

    /**
     * Build a numeric slide-X expression (unquoted).
     * Delegates to [ExportCommandBuilder.mSlideX].
     */
    @Suppress("unused")
    fun mSlideX(start: Float, duration: Float, offset: Float, scale: Float, from: Float, to: Float): String {
        return ExportCommandBuilder.mSlideX(start, duration, offset, scale, from, to)
    }

    companion object {
        private const val TAG = "ProgressViewActivity"
    }
}
