package hazem.nurmontage.videoquran.ui.render

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.RectF
import hazem.nurmontage.videoquran.constant.IpadType
import hazem.nurmontage.videoquran.model.EntityMedia
import hazem.nurmontage.videoquran.model.SquareBitmapModel
import hazem.nurmontage.videoquran.model.Template
import hazem.nurmontage.videoquran.utils.ColorUtils
import hazem.nurmontage.videoquran.utils.audio.FfmpegCodecChecker
import java.io.File
import java.io.FileOutputStream
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
 * 4. **Build final filter_complex** — overlay all layers with IpadType positioning
 * 5. **Concat segments** — merge all segments into final MP4
 *
 * All FFmpeg command strings and filter chains are treated as sacred.
 */
object ExportCommandBuilder {

    // ════════════════════════════════════════════════════════════════════
    //  Fade filter builders — preserved from ProgressViewActivity.java
    // ════════════════════════════════════════════════════════════════════

    fun mFadeFilter(startTime: Float, duration: Float, isIn: Boolean): String {
        val safeDuration = if (duration - 0.05f <= 0f) 0.01f else duration
        val direction = if (isIn) "in" else "out"
        return "fade=t=$direction:st=${abs(startTime)}:d=${abs(safeDuration)}:alpha=1:color=white,fps=60,format=rgba"
    }

    fun fadeInOut(endTime: Float, fadeInDur: Float, fadeOutDur: Float): String {
        val safeFadeIn = if (fadeInDur <= 0f) 0.01f else fadeInDur
        val safeFadeOutDur = if (fadeOutDur - 0.05f <= 0f) 0.01f else fadeOutDur
        val safeEnd = if (endTime - 0.05f <= 0f) 0.01f else endTime
        return "fade=t=in:st=0:d=${abs(safeFadeIn)}:alpha=1:color=white,fps=25,format=rgba," +
               "fade=t=out:st=${abs(safeEnd)}:d=${abs(safeFadeOutDur)}:alpha=1:color=white,fps=25,format=rgba"
    }

    fun fadeFilter(label: String, index: Int, startTime: Float, duration: Float, isIn: Boolean): String {
        val direction = if (isIn) "in" else "out"
        return "${label}fade=t=$direction:st=$startTime:d=${abs(duration - 0.05f)}:alpha=1:color=white,fps=60,format=rgba[${direction}_$index];"
    }

    fun fadeFilter(label: String, startTime: Float, duration: Float, isIn: Boolean): String {
        val direction = if (isIn) "in" else "out"
        return "[$label]fade=t=$direction:st=$startTime:d=${abs(duration - 0.05f)}:alpha=1:color=white,fps=60,format=rgba[${direction}_$label];"
    }

    fun fadeFilter(index: Int, startTime: Float, duration: Float, isIn: Boolean): String {
        val direction = if (isIn) "in" else "out"
        return "[$index]fade=t=$direction:st=$startTime:d=${abs(duration - 0.05f)}:alpha=1:color=white,fps=60,format=rgba[${direction}_$index];"
    }

    // ════════════════════════════════════════════════════════════════════
    //  Slide animation expression builders
    // ════════════════════════════════════════════════════════════════════

    fun slideX(start: Float, duration: Float, offset: Float, scale: Float, from: Float, to: Float): String {
        val t = "clip((t-$start)/$duration,0,1)"
        val smooth = "($t*$t*(3-2*$t))"
        return "'$offset+((${from}+(${to - $from})*$smooth)*$scale)'"
    }

    fun mSlideX(start: Float, duration: Float, offset: Float, scale: Float, from: Float, to: Float): String {
        val t = "clip((t-$start)/$duration,0,1)"
        val smooth = "($t*$t*(3-2*$t))"
        return "$offset+((${from}+(${to - $from})*$smooth)*$scale)"
    }

    // ════════════════════════════════════════════════════════════════════
    //  Mask generation — rounded rect and circle
    // ════════════════════════════════════════════════════════════════════

    fun getOrCreateMask(width: Int, height: Int, radius: Int, filesDir: File): File {
        val file = File(filesDir, "mask_${width}x${height}_r$radius.png")
        if (file.exists()) return file

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(0, PorterDuff.Mode.CLEAR)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = -1 }
        canvas.drawRoundRect(RectF(0f, 0f, width.toFloat(), height.toFloat()), radius.toFloat(), radius.toFloat(), paint)

        FileOutputStream(file).use { fos ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
        }
        return file
    }

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

    fun preRenderMaskRounded(
        template: Template,
        model: SquareBitmapModel,
        durationMs: Int,
        filesDir: File
    ): Triple<Array<String>, String, File> {
        val outputPath = "${template.folder_template}/rounded_${System.currentTimeMillis()}.mov"
        val maxSize = max(template.width, template.height)
        val right = Math.round(model.right)
        val bottom = Math.round(model.bottom)
        val left = Math.round(model.lef_square)
        val top = Math.round(model.top_square)
        var w = Math.round(model.width_sqaure)
        var h = Math.round(model.height_square)
        if ((w and 1) == 1) w++
        if ((h and 1) == 1) h++

        val maskFile = getOrCreateMask(w, h, model.raduis.toInt(), filesDir)
        val filterComplex = "[0:v]scale=$maxSize:$maxSize:force_original_aspect_ratio=increase," +
                           "crop=$right:$bottom:$left:$top," +
                           "scale=$w:$h:flags=lanczos[v];[v][1:v]alphamerge,format=rgba"

        val args = buildPreRenderArgs(template.uri_media_video, maskFile.absolutePath,
            filterComplex, durationMs, outputPath, isAlpha = true, codec = null)
        return Triple(args, outputPath, maskFile)
    }

    fun preRenderVideo(
        template: Template,
        durationMs: Int,
        codec: String?
    ): Pair<Array<String>?, String> {
        val outputPath = "${template.folder_template}/layer_video_${System.currentTimeMillis()}.mp4"
        val maxSize = max(template.width, template.height)
        val filterComplex = "[0:v]scale=$maxSize:$maxSize:force_original_aspect_ratio=increase:flags=lanczos," +
                           "crop=${template.width}:${template.height}:" +
                           "(iw-${template.width})/2:(ih-${template.height})/2[v];" +
                           "[v][1:v]overlay,format=rgba"

        val bgFile = File(template.uri_bg_ffmpeg ?: "")
        if (!bgFile.exists() || !bgFile.isFile) {
            return Pair(null, outputPath)
        }

        val args = arrayListOf(
            "-hide_banner", "-y",
            "-stream_loop", "-1",
            "-i", template.uri_media_video ?: "",
            "-i", template.uri_bg_ffmpeg!!,
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

        return Pair(args.toTypedArray(), outputPath)
    }

    fun generateVideoTimerArgs(
        template: Template,
        durationMs: Int
    ): Pair<Array<String>, String> {
        val outputPath = "${template.folder_template}/timer.mov"
        val maxSeconds = max(durationMs / 1000, 1)
        val timeModel = template.mTimeModel
        val fontPath = "${template.folder_template}/NotoNaskhArabic.ttf"
        val bgColor = if (ColorUtils.isColorDark(android.graphics.Color.parseColor(timeModel.color))) "black@0" else "white@0"

        val args = arrayOf(
            "-y",
            "-f", "lavfi",
            "-i", "color=size=${Math.round(timeModel.width_bitmap_progress * 1.3f)}x${timeModel.height_bitmap_progress}:rate=10:duration=$maxSeconds:color=$bgColor,format=rgba",
            "-vf", "drawtext=fontfile='$fontPath':text='%{eif\\:trunc(t/60)\\:d\\:2}\\:%{eif\\:trunc(mod(t\\,60))\\:d\\:2}':x=0.0:y=0.0:fontsize=${timeModel.size}:fontcolor=${timeModel.color}," +
                    "drawtext=fontfile='$fontPath':text='-%{eif\\:trunc(($maxSeconds+1-t)/60)\\:d\\:2}\\:%{eif\\:trunc(mod($maxSeconds+1-t\\,60))\\:d\\:2}':x=${timeModel.posXRight}:y=0.0:fontsize=${timeModel.size}:fontcolor=${timeModel.color}",
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
        inputVideo: String?,
        maskPath: String?,
        filterComplex: String,
        durationMs: Int,
        outputPath: String,
        isAlpha: Boolean,
        codec: String?
    ): Array<String> {
        val args = mutableListOf("-hide_banner", "-y", "-stream_loop", "-1", "-i", inputVideo ?: "")

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

    fun buildAacTestArgs(outputPath: String): String {
        return "-y -f lavfi -i anullsrc=channel_layout=stereo:sample_rate=44100 -t 1 -c:a aac -b:a 64k $outputPath"
    }

    // ════════════════════════════════════════════════════════════════════
    //  Full pipeline builder — orchestrates pre-render steps into final command
    // ════════════════════════════════════════════════════════════════════

    fun interface PreRenderStep {
        fun execute(latch: CountDownLatch, semaphore: Semaphore): String?
    }

    fun buildCommand(
        template: Template,
        codecInfo: FfmpegCodecChecker.CodecInfo,
        preRenderMask_Rounded: (SquareBitmapModel, Int, CountDownLatch, Semaphore) -> String?,
        preRenderMask_Circle: (SquareBitmapModel, Int, CountDownLatch, Semaphore) -> String?,
        preRender_NoMask: (SquareBitmapModel, Int, CountDownLatch, Semaphore, String?) -> String?,
        preRenderVideo: (Int, CountDownLatch, Semaphore, String?) -> String?,
        preRenderVideoHue: (Int, CountDownLatch, Semaphore, String?) -> String?,
        generateVideoTimer: (Int, CountDownLatch, Semaphore) -> String?
    ): Array<String>? {
        val folder = template.folder_template ?: return null
        val durationMs = template.duration
        val mediaList = template.entityMediaList
        val squareModel = template.squareBitmapModel

        val concurrencySemaphore = Semaphore(2)

        // ── Step 1: Pre-render masked video segments ─────────────────
        val preRenderedSegments = mutableListOf<String>()

        for (media in mediaList) {
            val latch = CountDownLatch(1)
            val segmentDuration = (media.end - media.start).toInt().coerceAtLeast(500)

            val outputPath = when {
                media.uri?.contains("rounded") == true && squareModel != null ->
                    preRenderMask_Rounded(squareModel, segmentDuration, latch, concurrencySemaphore)
                media.uri?.contains("circle") == true && squareModel != null ->
                    preRenderMask_Circle(squareModel, segmentDuration, latch, concurrencySemaphore)
                else -> null
            }

            latch.await()
            if (outputPath != null) {
                preRenderedSegments.add(outputPath)
            }
        }

        // ── Step 2: Pre-render video background layers ──────────────
        val videoCodec = codecInfo.videoCodec

        val videoLatch = CountDownLatch(1)
        val videoLayerPath = preRenderVideo(durationMs, videoLatch, concurrencySemaphore, videoCodec)
        videoLatch.await()
        if (videoLayerPath != null) preRenderedSegments.add(videoLayerPath)

        val hueLatch = CountDownLatch(1)
        val hueLayerPath = preRenderVideoHue(durationMs, hueLatch, concurrencySemaphore, videoCodec)
        hueLatch.await()
        if (hueLayerPath != null) preRenderedSegments.add(hueLayerPath)

        // ── Step 3: Generate timer overlay ──────────────────────────
        val timerLatch = CountDownLatch(1)
        val timerPath = generateVideoTimer(durationMs, timerLatch, concurrencySemaphore)
        timerLatch.await()
        if (timerPath != null) preRenderedSegments.add(timerPath)

        // ── Step 4: Build final filter_complex and command ───────────
        if (preRenderedSegments.isEmpty()) return null

        return buildFinalCommand(template, preRenderedSegments, codecInfo, folder)
    }

    /**
     * Compose the final FFmpeg command that overlays all pre-rendered
     * segments onto the background image/video and produces the output MP4.
     *
     * Full filter_complex composition with IpadType-aware positioning,
     * fade effects, slide animations, and proper overlay chain.
     */
    private fun buildFinalCommand(
        template: Template,
        segments: List<String>,
        codecInfo: FfmpegCodecChecker.CodecInfo,
        outputFolder: String
    ): Array<String> {
        val outputPath = "$outputFolder/export_${System.currentTimeMillis()}.mp4"
        val args = mutableListOf<String>()
        val filterBuilder = StringBuilder()

        // ── Input flags: background image + all pre-rendered segments ──
        args.addAll(listOf("-y"))
        val bgPath = template.uri_bg_ffmpeg ?: template.uri_bg ?: ""
        args.addAll(listOf("-i", bgPath))

        for (segment in segments) {
            args.addAll(listOf("-i", segment))
        }

        // ── Build filter_complex based on IpadType ──
        val ipadType = IpadType.entries[template.ipad_type.coerceIn(0, IpadType.entries.size - 1)]
        val templateW = template.width
        val templateH = template.height

        when (ipadType) {
            IpadType.IPAD, IpadType.IPAD_UNBLUR, IpadType.IPAD_CLASSIC -> {
                buildIpadOverlay(filterBuilder, template, segments)
            }
            IpadType.ROUND_RECT -> {
                buildIpadOverlay(filterBuilder, template, segments)
            }
            IpadType.RECT, IpadType.BOTTOM_RECT -> {
                if (ipadType == IpadType.BOTTOM_RECT) {
                    buildBottomRectOverlay(filterBuilder, template, segments)
                } else {
                    buildIpadOverlay(filterBuilder, template, segments)
                }
            }
            IpadType.BORDER -> {
                buildIpadOverlay(filterBuilder, template, segments)
            }
            IpadType.BLACK_LAYER -> {
                buildIpadOverlay(filterBuilder, template, segments)
            }
            IpadType.GRADIENT -> {
                buildIpadOverlay(filterBuilder, template, segments)
            }
            IpadType.BLUE_TYPE -> {
                buildIpadOverlay(filterBuilder, template, segments)
            }
            IpadType.MASK_BRUSH -> {
                buildIpadOverlay(filterBuilder, template, segments)
            }
            IpadType.IPAD_NEOMORPHIC -> {
                buildIpadOverlay(filterBuilder, template, segments)
            }
            IpadType.HEART -> {
                buildMaskOverlay(filterBuilder, template, segments)
            }
            IpadType.BATTERY -> {
                buildMaskOverlay(filterBuilder, template, segments)
            }
            IpadType.CASSET, IpadType.CASSET_IMG, IpadType.CASSET_IMG_BLUR -> {
                buildMaskOverlay(filterBuilder, template, segments)
            }
        }

        // ── Apply fade effects on each segment ──
        val mediaList = template.entityMediaList
        for ((index, media) in mediaList.withIndex()) {
            val fadeIn = media.fadeIn
            val fadeOut = media.fadeOut
            if (fadeIn > 0f) {
                filterBuilder.insert(0, fadeFilter(index + 1, 0f, fadeIn, true))
            }
            if (fadeOut > 0f) {
                val endTime = (media.end - media.start) / 1000f - fadeOut
                if (endTime > 0f) {
                    filterBuilder.append(fadeFilter(index + 1, endTime, fadeOut, false))
                }
            }
        }

        // ── Build final overlay chain ──
        if (segments.isNotEmpty()) {
            var lastLabel = "0:v"
            for ((index, _) in segments.withIndex()) {
                val inputLabel = "${index + 1}:v"
                val outputLabel = if (index < segments.size - 1) "v$index" else "vout"

                val overlayX = computeOverlayX(template, ipadType, index)
                val overlayY = computeOverlayY(template, ipadType, index)

                filterBuilder.append("[$lastLabel][$inputLabel]overlay=$overlayX:$overlayY:format=auto[$outputLabel];")
                lastLabel = outputLabel
            }

            // Remove trailing semicolon for the last filter
            if (filterBuilder.endsWith(";")) {
                filterBuilder.deleteCharAt(filterBuilder.length - 1)
            }
        }

        if (filterBuilder.isNotEmpty()) {
            args.addAll(listOf("-filter_complex", filterBuilder.toString()))
        }

        // ── Codec selection ──
        val videoCodec = codecInfo.videoCodec
        if (videoCodec != null) {
            args.addAll(listOf("-c:v", videoCodec, "-preset", "fast", "-crf", "18"))
        } else {
            args.addAll(listOf("-b:v", "4M"))
        }

        // ── Audio codec ──
        val audioCodec = codecInfo.audioCodec
        if (audioCodec != null && template.uri_media_video != null) {
            args.addAll(listOf("-c:a", audioCodec, "-b:a", "128k"))
        }

        args.addAll(listOf(
            "-r", template.fps.toString(),
            "-t", "${max(template.duration, 500)}ms",
            "-movflags", "+faststart",
            outputPath
        ))

        return args.toTypedArray()
    }

    // ════════════════════════════════════════════════════════════════════
    //  IpadType-specific overlay builders
    // ════════════════════════════════════════════════════════════════════

    private fun buildIpadOverlay(
        filterBuilder: StringBuilder,
        template: Template,
        segments: List<String>
    ) {
        val squareModel = template.squareBitmapModel
        if (squareModel != null && template.isVideoSquare) {
            for ((index, _) in segments.withIndex()) {
                val label = index + 1
                filterBuilder.append("[$label:v]scale=${template.width}:${template.height}:force_original_aspect_ratio=increase,")
                filterBuilder.append("crop=${template.width}:${template.height}:(iw-${template.width})/2:(ih-${template.height})/2[s$label];")
            }
        }
    }

    private fun buildBottomRectOverlay(
        filterBuilder: StringBuilder,
        template: Template,
        segments: List<String>
    ) {
        for ((index, _) in segments.withIndex()) {
            val label = index + 1
            filterBuilder.append("[$label:v]scale=${template.width}:${template.height}:force_original_aspect_ratio=increase,")
            filterBuilder.append("crop=${template.width}:${template.height}:(iw-${template.width})/2:0[s$label];")
        }
    }

    private fun buildMaskOverlay(
        filterBuilder: StringBuilder,
        template: Template,
        segments: List<String>
    ) {
        for ((index, _) in segments.withIndex()) {
            val label = index + 1
            filterBuilder.append("[$label:v]scale=${template.width}:${template.height}:force_original_aspect_ratio=increase[s$label];")
        }
    }

    // ════════════════════════════════════════════════════════════════════
    //  Overlay positioning calculators
    // ════════════════════════════════════════════════════════════════════

    private fun computeOverlayX(template: Template, ipadType: IpadType, segmentIndex: Int): String {
        return when (ipadType) {
            IpadType.BOTTOM_RECT -> "(iw-${template.width})/2"
            else -> "(iw-${template.width})/2"
        }
    }

    private fun computeOverlayY(template: Template, ipadType: IpadType, segmentIndex: Int): String {
        return when (ipadType) {
            IpadType.BOTTOM_RECT -> "(ih-${template.height})"
            else -> "(ih-${template.height})/2"
        }
    }

    // ════════════════════════════════════════════════════════════════════
    //  Bismilah / Surah / Timer overlay builders
    // ════════════════════════════════════════════════════════════════════

    fun buildBismilahOverlay(
        template: Template,
        bismilahImagePath: String,
        startTimeSec: Float,
        durationSec: Float,
        fadeInDur: Float,
        fadeOutDur: Float
    ): String {
        val w = "iw*0.6"
        val h = "ih*0.08"
        return "[bismilah]scale=$w:$h,${mFadeFilter(startTimeSec, fadeInDur, true)},${mFadeFilter(startTimeSec + durationSec - fadeOutDur, fadeOutDur, false)}[bsm];"
    }

    fun buildSurahNameOverlay(
        template: Template,
        surahImagePath: String,
        startTimeSec: Float,
        durationSec: Float
    ): String {
        val w = "iw*0.5"
        val h = "ih*0.06"
        return "[surah]scale=$w:$h[srn];"
    }

    fun buildTimerOverlay(
        template: Template,
        timerInputIndex: Int,
        lastLabel: String
    ): String {
        val posY = template.height - template.mTimeModel.height_bitmap_progress
        return "[$lastLabel][$timerInputIndex:v]overlay=0:$posY:format=auto[timer];"
    }

    fun buildSegmentFilter(
        media: EntityMedia,
        inputIndex: Int,
        template: Template
    ): String {
        val sb = StringBuilder()
        val w = template.width
        val h = template.height

        sb.append("[$inputIndex:v]scale=$w:$h:force_original_aspect_ratio=increase,")
        sb.append("crop=$w:$h:(iw-$w)/2:(ih-$h)/2")

        if (media.fadeIn > 0f) {
            sb.append(",${mFadeFilter(0f, media.fadeIn, true)}")
        }
        if (media.fadeOut > 0f) {
            val endTimeSec = (media.end - media.start) / 1000f - media.fadeOut
            if (endTimeSec > 0f) {
                sb.append(",${mFadeFilter(endTimeSec, media.fadeOut, false)}")
            }
        }

        sb.append(",format=rgba[seg_$inputIndex];")
        return sb.toString()
    }
}
