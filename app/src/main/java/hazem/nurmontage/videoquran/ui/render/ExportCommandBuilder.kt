package hazem.nurmontage.videoquran.ui.render

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.RectF
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFmpegSessionCompleteCallback
import com.arthenica.ffmpegkit.Statistics
import com.arthenica.ffmpegkit.StatisticsCallback
import hazem.nurmontage.videoquran.model.SquareBitmapModel
import hazem.nurmontage.videoquran.model.Template
import hazem.nurmontage.videoquran.utils.ColorUtils
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Semaphore
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * FFmpeg export command builder for the ProgressViewActivity.
 *
 * This object encapsulates the video export pipeline that was originally
 * embedded in ProgressViewActivity.java. The commands are preserved EXACTLY
 * to ensure identical rendering output.
 *
 * The export pipeline has multiple stages:
 * 1. **Pre-render video segments** — crop, scale, apply masks (rounded/circle)
 * 2. **Generate timer overlay** — drawtext with elapsed/remaining time
 * 3. **Apply fade effects** — fade-in/fade-out on video segments
 * 4. **Build final filter_complex** — overlay all layers
 * 5. **Concat segments** — merge all segments into final MP4
 *
 * Each stage uses CountDownLatch + Semaphore for thread coordination,
 * ensuring that FFmpeg operations execute in the correct order without
 * overloading the CPU.
 *
 * All FFmpeg command strings and filter chains are treated as sacred.
 */
object ExportCommandBuilder {

    // ════════════════════════════════════════════════════════════════════
    //  Fade filter builders — preserved from ProgressViewActivity.java
    // ════════════════════════════════════════════════════════════════════

    /**
     * Build a simple fade filter string.
     *
     * @param startTime  Start time in seconds
     * @param duration   Fade duration in seconds (minimum 0.01)
     * @param isIn       true for fade-in, false for fade-out
     * @return FFmpeg fade filter string
     */
    fun mFadeFilter(startTime: Float, duration: Float, isIn: Boolean): String {
        val safeDuration = if (duration - 0.05f <= 0f) 0.01f else duration
        val direction = if (isIn) "in" else "out"
        return "fade=t=$direction:st=${abs(startTime)}:d=${abs(safeDuration)}:alpha=1:color=white,fps=60,format=rgba"
    }

    /**
     * Build a combined fade-in + fade-out filter string.
     *
     * @param endTime     Time at which fade-out begins
     * @param fadeInDur   Fade-in duration
     * @param fadeOutDur  Fade-out duration
     * @return Combined FFmpeg fade filter string
     */
    fun fadeInOut(endTime: Float, fadeInDur: Float, fadeOutDur: Float): String {
        val safeFadeIn = if (fadeInDur <= 0f) 0.01f else fadeInDur
        val safeFadeOutDur = if (fadeOutDur - 0.05f <= 0f) 0.01f else fadeOutDur
        val safeEnd = if (endTime - 0.05f <= 0f) 0.01f else endTime
        return "fade=t=in:st=0:d=${abs(safeFadeIn)}:alpha=1:color=white,fps=25,format=rgba," +
               "fade=t=out:st=${abs(safeEnd)}:d=${abs(safeFadeOutDur)}:alpha=1:color=white,fps=25,format=rgba"
    }

    /**
     * Build a fade filter with stream label for filter_complex.
     */
    fun fadeFilter(label: String, index: Int, startTime: Float, duration: Float, isIn: Boolean): String {
        val direction = if (isIn) "in" else "out"
        return "${label}fade=t=$direction:st=$startTime:d=${abs(duration - 0.05f)}:alpha=1:color=white,fps=60,format=rgba[${direction}_$index];"
    }

    /**
     * Build a fade filter with input stream label for filter_complex.
     */
    fun fadeFilter(label: String, startTime: Float, duration: Float, isIn: Boolean): String {
        val direction = if (isIn) "in" else "out"
        return "[$label]fade=t=$direction:st=$startTime:d=${abs(duration - 0.05f)}:alpha=1:color=white,fps=60,format=rgba[${direction}_$label];"
    }

    /**
     * Build a fade filter with numeric stream index for filter_complex.
     */
    fun fadeFilter(index: Int, startTime: Float, duration: Float, isIn: Boolean): String {
        val direction = if (isIn) "in" else "out"
        return "[$index]fade=t=$direction:st=$startTime:d=${abs(duration - 0.05f)}:alpha=1:color=white,fps=60,format=rgba[${direction}_$index];"
    }

    // ════════════════════════════════════════════════════════════════════
    //  Slide animation expression builders
    // ════════════════════════════════════════════════════════════════════

    /**
     * Build a smooth slide-X expression for FFmpeg overlay positioning.
     *
     * Uses a smoothstep-like function: `clip((t-start)/duration, 0, 1)`
     * with a cubic easing: `x * x * (3 - 2 * x)`.
     *
     * @return Quoted FFmpeg expression string for overlay x/y positioning
     */
    fun slideX(start: Float, duration: Float, offset: Float, scale: Float, from: Float, to: Float): String {
        val t = "clip((t-$start)/$duration,0,1)"
        val smooth = "($t*$t*(3-2*$t))"
        return "'$offset+((${from}+(${to - $from})*$smooth)*$scale)'"
    }

    /**
     * Build a numeric slide-X expression (unquoted) for FFmpeg overlay.
     */
    fun mSlideX(start: Float, duration: Float, offset: Float, scale: Float, from: Float, to: Float): String {
        val t = "clip((t-$start)/$duration,0,1)"
        val smooth = "($t*$t*(3-2*$t))"
        return "$offset+((${from}+(${to - $from})*$smooth)*$scale)"
    }

    // ════════════════════════════════════════════════════════════════════
    //  Mask generation — rounded rect and circle
    // ════════════════════════════════════════════════════════════════════

    /**
     * Create a rounded-rectangle mask PNG file for FFmpeg alphamerge.
     *
     * The mask is a white rounded rectangle on a transparent background.
     * Cached by dimensions and radius — if the file exists, it is reused.
     *
     * @param width   Mask width in pixels
     * @param height  Mask height in pixels
     * @param radius  Corner radius in pixels
     * @param filesDir Directory to store the mask file
     * @return The mask [File]
     */
    fun getOrCreateMask(width: Int, height: Int, radius: Int, filesDir: File): File {
        val file = File(filesDir, "mask_${width}x${height}_r$radius.png")
        if (file.exists()) return file

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(0, PorterDuff.Mode.CLEAR)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = -1 } // White
        canvas.drawRoundRect(RectF(0f, 0f, width.toFloat(), height.toFloat()), radius.toFloat(), radius.toFloat(), paint)

        FileOutputStream(file).use { fos ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
        }

        return file
    }

    /**
     * Create a circle mask PNG file for FFmpeg alphamerge.
     */
    fun getOrCreateMaskCircle(width: Int, height: Int, templateDir: String): File {
        val file = File(templateDir, "circle_${width}x${height}.png")

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = -1 }
        canvas.drawCircle(width / 2.0f, height / 2.0f, min(width, height) / 2.0f, paint)

        FileOutputStream(file).use { fos ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
        }

        return file
    }

    // ════════════════════════════════════════════════════════════════════
    //  Pre-render command builders
    // ════════════════════════════════════════════════════════════════════

    /**
     * Build the FFmpeg command for pre-rendering a rounded-rect masked video segment.
     *
     * Filter complex:
     * `[0:v]scale+crop+scale[v];[v][1:v]alphamerge,format=rgba`
     */
    fun preRenderMaskRounded(
        template: Template,
        model: SquareBitmapModel,
        durationMs: Int,
        filesDir: File
    ): Triple<Array<String>, String, File> {
        val outputPath = "${template.getFolder_template()}/rounded_${System.currentTimeMillis()}.mov"
        val maxSize = max(template.getWidth(), template.getHeight())
        val right = Math.round(model.getRight())
        val bottom = Math.round(model.getBottom())
        val left = Math.round(model.getLef_square())
        val top = Math.round(model.getTop_square())
        var w = Math.round(model.getWidth_sqaure())
        var h = Math.round(model.getHeight_square())
        if ((w and 1) == 1) w++
        if ((h and 1) == 1) h++

        val maskFile = getOrCreateMask(w, h, model.getRaduis().toInt(), filesDir)
        val filterComplex = "[0:v]scale=$maxSize:$maxSize:force_original_aspect_ratio=increase," +
                           "crop=$right:$bottom:$left:$top," +
                           "scale=$w:$h:flags=lanczos[v];[v][1:v]alphamerge,format=rgba"

        val args = buildPreRenderArgs(template.getUri_media_video(), maskFile.absolutePath,
            filterComplex, durationMs, outputPath, isAlpha = true, codec = null)
        return Triple(args, outputPath, maskFile)
    }

    /**
     * Build the FFmpeg command for pre-rendering a video segment with background overlay.
     */
    fun preRenderVideo(
        template: Template,
        durationMs: Int,
        codec: String?
    ): Pair<Array<String>?, String> {
        val outputPath = "${template.getFolder_template()}/layer_video_${System.currentTimeMillis()}.mp4"
        val maxSize = max(template.getWidth(), template.getHeight())
        val filterComplex = "[0:v]scale=$maxSize:$maxSize:force_original_aspect_ratio=increase:flags=lanczos," +
                           "crop=${template.getWidth()}:${template.getHeight()}:" +
                           "(iw-${template.getWidth()})/2:(ih-${template.getHeight()})/2[v];" +
                           "[v][1:v]overlay,format=rgba"

        val bgFile = File(template.getUri_bg_ffmpeg())
        if (!bgFile.exists() || !bgFile.isFile) {
            return Pair(null, outputPath)
        }

        val args = arrayListOf(
            "-hide_banner", "-y",
            "-stream_loop", "-1",
            "-i", template.getUri_media_video(),
            "-i", template.getUri_bg_ffmpeg(),
            "-filter_complex", filterComplex
        )

        if (codec != null) {
            args.addAll(listOf("-threads", "0", "-c:v", codec, "-preset", "fast", "-crf", "18"))
        } else {
            args.addAll(listOf("-b:v", "4M"))
        }

        args.addAll(listOf(
            "-r", template.getFps().toString(),
            "-t", "${max(durationMs, 500)}ms",
            "-movflags", "+faststart",
            "-an",
            outputPath
        ))

        return Pair(args.toTypedArray(), outputPath)
    }

    /**
     * Build the FFmpeg command for generating a timer overlay video.
     *
     * Uses `drawtext` filter to render elapsed and remaining time.
     *
     * Filter:
     * `drawtext=fontfile='<font>':text='%{eif\:trunc(t/60)\:d\:2}\:%{eif\:trunc(mod(t,60))\:d\:2}'`
     */
    fun generateVideoTimerArgs(
        template: Template,
        durationMs: Int
    ): Pair<Array<String>, String> {
        val outputPath = "${template.getFolder_template()}/timer.mov"
        val maxSeconds = max(durationMs / 1000, 1)
        val timeModel = template.getmTimeModel()
        val fontPath = "${template.getFolder_template()}/NotoNaskhArabic.ttf"
        val bgColor = if (ColorUtils.isColorDark(android.graphics.Color.parseColor(timeModel.getColor()))) "black@0" else "white@0"

        val args = arrayOf(
            "-y",
            "-f", "lavfi",
            "-i", "color=size=${Math.round(timeModel.getWidth_bitmap_progress() * 1.3f)}x${timeModel.getHeight_bitmap_progress()}:rate=10:duration=$maxSeconds:color=$bgColor,format=rgba",
            "-vf", "drawtext=fontfile='$fontPath':text='%{eif\\:trunc(t/60)\\:d\\:2}\\:%{eif\\:trunc(mod(t\\,60))\\:d\\:2}':x=0.0:y=0.0:fontsize=${timeModel.getSize()}:fontcolor=${timeModel.getColor()}," +
                    "drawtext=fontfile='$fontPath':text='-%{eif\\:trunc(($maxSeconds+1-t)/60)\\:d\\:2}\\:%{eif\\:trunc(mod($maxSeconds+1-t\\,60))\\:d\\:2}':x=${timeModel.getPosXRight()}:y=0.0:fontsize=${timeModel.getSize()}:fontcolor=${timeModel.getColor()}",
            "-c:v", "qtrle",
            "-pix_fmt", "argb",
            "-preset", "veryfast",
            "-avoid_negative_ts", "make_zero",
            outputPath
        )

        return Pair(args, outputPath)
    }

    // ════════════════════════════════════════════════════════════════════
    //  Helper: build pre-render args
    // ════════════════════════════════════════════════════════════════════

    private fun buildPreRenderArgs(
        inputVideo: String,
        maskPath: String?,
        filterComplex: String,
        durationMs: Int,
        outputPath: String,
        isAlpha: Boolean,
        codec: String?
    ): Array<String> {
        val args = mutableListOf("-hide_banner", "-y", "-stream_loop", "-1", "-i", inputVideo)

        if (maskPath != null) {
            args.addAll(listOf("-i", maskPath))
        }

        args.addAll(listOf("-filter_complex", filterComplex))

        when {
            isAlpha -> {
                args.addAll(listOf("-c:v", "qtrle", "-pix_fmt", "rgba"))
            }
            codec != null -> {
                args.addAll(listOf("-threads", "0", "-c:v", codec, "-preset", "fast", "-crf", "18"))
            }
            else -> {
                args.addAll(listOf("-b:v", "4M"))
            }
        }

        args.addAll(listOf("-r", "25", "-t", "${max(durationMs, 500)}ms"))

        if (!isAlpha) {
            args.addAll(listOf("-movflags", "+faststart"))
        }

        args.add(outputPath)

        return args.toTypedArray()
    }

    /**
     * Build the AAC encoder test command.
     *
     * Command: `-y -f lavfi -i anullsrc=channel_layout=stereo:sample_rate=44100 -t 1 -c:a aac -b:a 64k <outputPath>`
     */
    fun buildAacTestArgs(outputPath: String): String {
        return "-y -f lavfi -i anullsrc=channel_layout=stereo:sample_rate=44100 -t 1 -c:a aac -b:a 64k $outputPath"
    }
}
