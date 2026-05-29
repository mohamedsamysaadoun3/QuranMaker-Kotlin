package hazem.nurmontage.videoquran.entity_timeline

import android.animation.ObjectAnimator
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.RectF
import android.media.MediaPlayer
import android.net.Uri
import hazem.nurmontage.videoquran.core.common.Common
import hazem.nurmontage.videoquran.model.EffectAudio
import hazem.nurmontage.videoquran.utils.waveform.WaveformBitmapRenderer
import hazem.nurmontage.videoquran.views.TrackEntityView

/**
 * Audio entity on the timeline editor.
 *
 * Represents a single audio clip in the multi-track timeline, with:
 * - Waveform rendering via [WaveformBitmapRenderer]
 * - Trim handles (left/right) for clipping the audio range
 * - [EffectAudio] chain for volume, reverb, echo, fade, speed, noise removal
 * - [MediaPlayer] lifecycle for preview playback
 * - Fade-in/fade-out animation via [ObjectAnimator]
 * - Split support for dividing the clip at the cursor position
 *
 * Coordinate system:
 * - [rect] defines the pixel bounds on the timeline canvas
 * - [start]/[end] define the audio time range in milliseconds
 * - [offset]/[offsetLeft]/[offsetRight] handle scroll offsets
 * - [scaleFactor] converts between pixel and time coordinates
 *
 * Converted from EntityAudio.java — all drawing and trim logic preserved exactly.
 */
class EntityAudio : Entity {

    // ── Audio metadata ───────────────────────────────────────────────
    internal var amps: FloatArray? = null
    internal var downX: Float = 0f
    internal var duration: Int = 0
    var effectAudio: EffectAudio = EffectAudio()
    internal var h: Float = 0f
    internal var iTrimLineCallback: TrackEntityView.ITrimLineCallback? = null
    internal var isApplyEffectInPreview: Boolean = false
    internal var isPlay: Boolean = false
    internal var isStartFadeIn: Boolean = false
    internal var isStartFadeOut: Boolean = false
    internal var lastLeft: Float = 0f
    internal var lastRight: Float = 0f
    var mediaPlayer: MediaPlayer? = null
    internal var minDuration: Int = 0
    internal var objectAnimator: ObjectAnimator? = null
    internal var paintLine: Paint? = null
    internal var paintPath: Paint? = null
    internal var path: Path? = null
    internal var pathFfmpeg: String? = null
    internal var pathFfmpegEffect: String? = null
    internal var pathsHttp: MutableList<String>? = null
    internal var renderer: WaveformBitmapRenderer? = null
    internal var scaleEffect: Float = 0f
    internal var secondInScreen: Float = 0f
    internal var tmpOffset: Float = 0f
    internal var uri: Uri? = null
    internal var videoPath: String? = null
    var waveformValues: ByteArray? = null

    // ── Constructors ─────────────────────────────────────────────────

    constructor(
        bitmap: Bitmap?,
        uri: Uri?,
        left: Float,
        top: Float,
        h: Float,
        right: Float,
        max: Float,
        secondInScreen: Float,
        durationSec: Int,
        offset: Float,
        offsetLeft: Float,
        offsetRight: Float
    ) : super(secondInScreen) {
        this.effectAudio = EffectAudio()
        setOffsetRight(offsetRight)
        setOffset(offset)
        setOffsetLeft(offsetLeft)
        this.duration = durationSec * 1000
        this.end = durationSec.toFloat()
        this.secondInScreen = secondInScreen
        setVisible(true)
        this.uri = uri
        this.max = max
        this.h = h
        this.rect = RectF(left, top, right, h)
        this.left = rect.left
        this.right = rect.right
        this.color = Common.COLOR_BLOCK_AUDIO
        initPaints(h)
    }

    constructor(
        bitmap: Bitmap?,
        uri: Uri?,
        left: Float,
        top: Float,
        h: Float,
        right: Float,
        max: Float,
        secondInScreen: Float,
        durationSec: Int
    ) : super(secondInScreen) {
        this.effectAudio = EffectAudio()
        setOffsetRight(0f)
        setOffset(0f)
        this.duration = durationSec * 1000
        this.end = durationSec.toFloat()
        this.secondInScreen = secondInScreen
        setVisible(true)
        this.uri = uri
        this.max = max
        this.h = h
        this.rect = RectF(left, top, right, h)
        this.left = rect.left
        this.right = rect.right
        this.color = Common.COLOR_BLOCK_AUDIO
        initPaints(h)
    }

    /**
     * Initialize Paint objects for the audio block border and waveform fill.
     */
    private fun initPaints(height: Float) {
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = -2434342
            style = Paint.Style.STROKE
            strokeWidth = 0.01f * height
        }
        this.paintLine = linePaint

        val pathPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = -1236326096
            style = Paint.Style.FILL
        }
        this.paintPath = pathPaint
        this.path = Path()

        this.rectFLeft = RectF(0f, 0f, 0.46f * height, height)
        this.rectFRight = RectF(0f, 0f, rectFLeft.width(), height)
        this.round = rectFRight.width() * 0.5f
        this.padding = height * 0.07f
    }

    // ── EffectAudio delegation ───────────────────────────────────────

    fun getEffectAudio(): EffectAudio = effectAudio

    fun updateEffect() {
        effectAudio.start = start
        effectAudio.end = end
        effectAudio.duration = (end - start).toInt()
    }

    fun setEffectAudio(newEffect: EffectAudio?) {
        if (newEffect == null) return
        effectAudio.reverbPreset = newEffect.reverbPreset
        effectAudio.speed = newEffect.speed
        effectAudio.volume = newEffect.volume
        effectAudio.fade_in = newEffect.fade_in
        effectAudio.fade_out = newEffect.fade_out
        effectAudio.decays = newEffect.decays
        effectAudio.isRemoveNoice = newEffect.isRemoveNoice
        effectAudio.delays_cmd = newEffect.delays_cmd
        effectAudio.delays = newEffect.delays
        effectAudio.decays_cmd = newEffect.decays_cmd
        effectAudio.outGain = newEffect.outGain
        effectAudio.volume_echo = newEffect.volume_echo
        effectAudio.isEnhance = newEffect.isEnhance
        effectAudio.reverbPreset_index_list = newEffect.reverbPreset_index_list
    }

    // ── MediaPlayer ──────────────────────────────────────────────────

    fun setMediaPlayer(mp: MediaPlayer?) { mediaPlayer = mp }
    fun getMediaPlayer(): MediaPlayer? = mediaPlayer

    // ── FFmpeg paths ─────────────────────────────────────────────────

    fun setPathFfmpeg(path: String?) {
        this.pathFfmpeg = path
        this.pathFfmpegEffect = path
    }

    fun getPathFfmpeg(): String? = pathFfmpeg

    fun getPathFfmpegEffect(): String? = pathFfmpegEffect
    fun setPathFfmpegEffect(path: String?) { this.pathFfmpegEffect = path }

    // ── HTTP paths ───────────────────────────────────────────────────

    fun addPathHttp(paths: List<String>?) {
        if (paths == null) return
        if (pathsHttp == null) pathsHttp = mutableListOf()
        pathsHttp!!.addAll(paths)
    }

    fun setPathHttp(paths: List<String>?) { this.pathsHttp = paths?.toMutableList() }
    fun getPathsHttp(): List<String>? = pathsHttp

    // ── Preview effect flag ──────────────────────────────────────────

    fun setApplyEffectInPreview(apply: Boolean) { isApplyEffectInPreview = apply }
    fun isApplyEffectInPreview(): Boolean = isApplyEffectInPreview

    // ── Scale effect ─────────────────────────────────────────────────

    fun setScaleEffect(scale: Float) { scaleEffect = scale }
    fun getScaleEffect(): Float = scaleEffect

    // ── Duration ─────────────────────────────────────────────────────

    fun getDuration(): Int = duration
    fun setDuration(dur: Int) { this.duration = dur }

    // ── Min duration ─────────────────────────────────────────────────

    fun getMinDuration(): Int = minDuration
    fun setMinDuration(min: Int) { this.minDuration = min }

    // ── Video path ───────────────────────────────────────────────────

    fun setVideoPath(path: String?) { this.videoPath = path }
    fun getVideoPath(): String? = videoPath

    // ── URI ──────────────────────────────────────────────────────────

    fun getUri(): Uri? = uri

    // ── Play state ───────────────────────────────────────────────────

    fun isPlay(): Boolean = isPlay
    fun setPlay(play: Boolean) { this.isPlay = play }

    // ── Amplitudes & Waveform ────────────────────────────────────────

    fun getAmps(): FloatArray? = amps
    fun getRenderer(): WaveformBitmapRenderer? = renderer

    fun setRenderer(r: WaveformBitmapRenderer?) { this.renderer = r }

    fun setAmps(a: FloatArray?) { this.amps = a }

    fun setAmps(a: FloatArray?, width: Int, height: Int) {
        this.amps = a
        this.renderer = WaveformBitmapRenderer(a, width, height, Common.COLOR_WAVE_INT)
    }

    // ── Fade animation state ─────────────────────────────────────────

    fun isStartFadeIn(): Boolean = isStartFadeIn
    fun isStartFadeOut(): Boolean = isStartFadeOut
    fun setStartFadeIn(start: Boolean) { isStartFadeIn = start }
    fun setStartFadeOut(start: Boolean) { isStartFadeOut = start }

    fun setITrimLineCallback(callback: TrackEntityView.ITrimLineCallback?) {
        this.iTrimLineCallback = callback
    }

    // ── Fade-in animation ────────────────────────────────────────────

    fun setFadeInDelta(delta: Float) {
        iTrimLineCallback?.fadeInAudio(delta)
    }

    fun startFadeIn() {
        objectAnimator?.end()
        val fadeDuration = getFadeIn() * 1000f
        objectAnimator = ObjectAnimator.ofFloat(this, "FadeInDelta", 0f, 1f).apply {
            duration = fadeDuration.toLong()
            start()
        }
    }

    // ── Fade-out animation ───────────────────────────────────────────

    fun setFadeOutDelta(delta: Float) {
        iTrimLineCallback?.fadeOutAudio(delta)
    }

    fun startFadeOut() {
        objectAnimator?.end()
        val fadeDuration = getFadeOut() * 1000f
        objectAnimator = ObjectAnimator.ofFloat(this, "FadeOutDelta", 1f, 0f).apply {
            duration = fadeDuration.toLong()
            start()
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  Entity overrides — position, trim, hit-test
    // ══════════════════════════════════════════════════════════════════

    override fun setSecondInScreen(sis: Float) { this.secondInScreen = sis }

    override fun getSecondInScreen(): Float = secondInScreen * getScaleFactor()

    override fun getH(): Float = h

    override fun getLeft(): Float = left

    override fun setLastLeft(ll: Float) { lastLeft = ll }
    override fun setLastRight(lr: Float) { lastRight = lr }

    override fun setX(x: Float) {
        val clamped = if (x < 0f) 0f else x
        left = clamped
        rect.left = clamped
    }

    override fun getRight(): Float = right

    override fun setRight(r: Float) {
        rect.right = r
        right = r
    }

    override fun setY(y: Float) {
        rect.top = y
        rect.bottom = h + rect.top
    }

    override fun getRect(): RectF = rect

    override fun getDownX(): Float = downX

    override fun getTrimType(): Int = trimType

    override fun getSelectTrim(): RectF? = selectTrim

    // ── Trim logic ───────────────────────────────────────────────────

    override fun onUpRight() {
        val round = (Math.round(getRect().right / getSecondInScreen()) * 1000).toFloat() - getOnTapTime()
        setOffsetRight(
            ((getRect().left / getScaleFactor()) - getOffsetLeft()) + getMax() - (getRect().right / getScaleFactor())
        )
        end += round
        if (end > duration) end = duration.toFloat()
        right = lastRight
    }

    override fun updateStartTrim() {
        tmpOffset = Math.abs(getRect().left / getScaleFactor()) - Math.abs(getOnDown() / getScaleFactor())
    }

    override fun onUpLeft() {
        start = Math.round(
            (Math.abs(Math.round((getRect().left / getSecondInScreen()) * 1000f)) - getOnTapTime()).toFloat() + start
        )
        setOffsetLeft(getOffsetLeft() + tmpOffset)
        tmpOffset = 0f
        if (start < minDuration) start = minDuration.toFloat()
        left = lastLeft
    }

    // ── Hit test ─────────────────────────────────────────────────────

    override fun onTouch(point: PointF): Boolean {
        selectTrim = null
        downX = point.x
        trimType = -1
        if (rectFLeft.contains(point.x, point.y)) {
            selectTrim = rectFLeft
            trimType = 0
            isSelect = true
        } else if (rectFRight.contains(point.x, point.y)) {
            selectTrim = rectFRight
            trimType = 1
            isSelect = true
        }
        return true
    }

    override fun contains(point: PointF): Boolean {
        if (isSelect) onTouch(point)
        isSelect = rect.contains(point.x, point.y)
        return isSelect
    }

    // ── Selection ────────────────────────────────────────────────────

    override fun setSelect(select: Boolean) { isSelect = select }

    // ── Split ────────────────────────────────────────────────────────

    fun split(cursorX: Float): EntityAudio {
        return EntityAudio(
            null, uri, cursorX, getRect().top, h, getRect().right,
            ((getRect().right / getScaleFactor()) + getOffsetRight()) - (cursorX / getScaleFactor()),
            getSecondInScreen(), (duration / 1000), 0f, 0f, 0f
        ).also { split ->
            split.setFadeOut(getFadeOut())
            split.setFadeIn(getFadeIn())
            split.getRect().bottom = getRect().bottom
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  Drawing — waveform rendering
    // ══════════════════════════════════════════════════════════════════

    private fun drawWave(canvas: Canvas, rect: RectF) {
        if (amps == null || renderer == null) return
        val offset = getOffset() + getOffsetLeft() + tmpOffset
        renderer!!.draw(canvas, rect, getScaleFactor() + scaleEffect, offset)
    }

    override fun draw(canvas: Canvas, w: Int, h: Int) {
        try { drawWave(canvas, rect) } catch (_: Exception) {}
    }

    override fun draw(canvas: Canvas) {
        try { drawWave(canvas, rect) } catch (_: Exception) {}
    }

    // ── Visibility ───────────────────────────────────────────────────

    fun isVisible(): Boolean = isVisible

    override fun setVisible(visible: Boolean) { isVisible = visible }

    // ── Max ──────────────────────────────────────────────────────────

    fun setMax(max: Float) { this.max = max }
    fun getMax(): Float = max

    // ── Start/End overrides ──────────────────────────────────────────

    fun getAudioStart(): Float = start
    fun setAudioStart(s: Float) { start = s }

    fun getAudioEnd(): Float = end
    fun setAudioEnd(e: Float) { end = e }

    // ══════════════════════════════════════════════════════════════════
    //  Cleanup
    // ══════════════════════════════════════════════════════════════════

    override fun release() {
        super.release()
        try {
            mediaPlayer?.let { mp ->
                if (mp.isPlaying) mp.pause()
                mp.release()
                mediaPlayer = null
            }
            renderer?.release()
        } catch (_: Exception) {}
    }
}
