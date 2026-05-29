package hazem.nurmontage.videoquran.model.data

import android.animation.ObjectAnimator
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.text.Layout
import android.text.SpannableString
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.graphics.ColorUtils
import androidx.core.view.ViewCompat
import hazem.nurmontage.videoquran.core.common.Constants.AyaTextPreset
import hazem.nurmontage.videoquran.core.common.Constants.FONT_QURAN
import hazem.nurmontage.videoquran.core.common.Constants.IpadType
import hazem.nurmontage.videoquran.core.common.Constants.TransitionType
import hazem.nurmontage.videoquran.model.EntityView
import hazem.nurmontage.videoquran.views.BlurredImageView
import hazem.nurmontage.videoquran.views.TrackEntityView
import java.io.Serializable
import java.lang.ref.WeakReference
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.round

/**
 * Standalone translation text entity rendered on the canvas.
 *
 * Unlike [QuranEntity], this class handles **only** translation text —
 * there is no aya/translation dual-layer.  It supports text presets
 * (outline, shadow, glow), ObjectAnimator-driven transitions (slide,
 * fade, zoom), optional background colour, and per-frame drawing with
 * an outline layer.
 *
 * All field names are preserved from the original JADX decompilation to
 * maintain serialization compatibility with saved project files.
 *
 * Originally: `hazem.nurmontage.videoquran.model.TranslationQuranEntity`
 */
class TranslationQuranEntity : EntityView, Serializable {

    companion object {
        private const val serialVersionUID = 1L
        private const val ALPHA_BG = 100
    }

    // ──────────────────────────────────────────────
    //  State fields (names preserved for serialization)
    // ──────────────────────────────────────────────
    internal var clrAya: Int = 0
    internal var index: Int = 0
    internal var ipad_type: Int = 0
    internal var isFadeIn: Boolean = false
    internal var isFadeOut: Boolean = false
    internal var isVisible: Boolean = false
    internal var mPreset: Int = 0
    internal var nameFont: String = FONT_QURAN
    internal var number: Int = 0
    internal var objectAnimator: ObjectAnimator? = null
    internal var offsetX: Float = 0f
    internal var otherAnimation: ObjectAnimator? = null
    private val paintAya: TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    private val paintAyaOutline: TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    private val paintAyaTrslOutline: TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    private val paintBg: TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    internal var spannableString: SpannableString? = null
    internal var staticLayout: StaticLayout? = null
    internal var staticLayoutOutline: StaticLayout? = null
    internal var txt: String? = null
    internal var viewWeakReference: WeakReference<TrackEntityView>? = null
    internal var viewWidth: Int = 0
    internal var weakBlurredImageView: WeakReference<BlurredImageView>? = null
    internal var clrBg: Int = ViewCompat.MEASURED_STATE_MASK
    internal var isHaveBg: Boolean = true
    internal var scaleX: Float = 1.0f

    // ──────────────────────────────────────────────
    //  Constructors
    // ──────────────────────────────────────────────

    /**
     * Full constructor: sets canvas dimensions, positions the rect at the
     * bottom of the canvas, calculates text size, and creates the static layout.
     */
    constructor(
        txt: String,
        rectF: RectF,
        typeface: Typeface,
        number: Int,
        color: Int,
        fontName: String,
        canvasW: Int,
        canvasH: Int
    ) {
        this.txt = txt
        this.nameFont = fontName
        this.number = number
        setCanvasWH(canvasW, canvasH)
        rect = RectF(0f, canvasH.toFloat() - rectF.height(), canvasW.toFloat(), canvasH.toFloat())
        setVisible(true)
        viewWidth = rectF.width().toInt()
        paintAya.typeface = typeface
        paintAya.color = color
        paintAya.textSize = calculateTextSize()
        paintBg.color = ViewCompat.MEASURED_STATE_MASK
        paintBg.alpha = ALPHA_BG
        setClrAya(color)
        maxH = (rect.height() * 0.85f).toInt()
        maxW = (rect.width() * 0.85f).toInt()
        createStaticLayout()
    }

    /**
     * Constructor with explicit canvas dimensions and pre-positioned rect.
     * Does NOT calculate text size or create layout (caller must do that).
     */
    constructor(
        canvasW: Int,
        canvasH: Int,
        txt: String,
        rectF: RectF,
        typeface: Typeface,
        number: Int,
        color: Int,
        fontName: String
    ) {
        setCanvasWH(canvasW, canvasH)
        this.txt = txt
        this.nameFont = fontName
        this.number = number
        rect = rectF
        setVisible(true)
        viewWidth = rectF.width().toInt()
        paintAya.typeface = typeface
        paintAya.color = color
        paintBg.color = ViewCompat.MEASURED_STATE_MASK
        paintBg.alpha = ALPHA_BG
        setClrAya(color)
        maxH = (rect.height() * 0.85f).toInt()
        maxW = (rect.width() * 0.85f).toInt()
    }

    /**
     * Constructor with explicit text size and copy-rect.
     */
    constructor(
        txt: String,
        rectF: RectF,
        typeface: Typeface,
        number: Int,
        color: Int,
        fontName: String,
        textSize: Float
    ) {
        this.txt = txt
        this.nameFont = fontName
        this.number = number
        rect = RectF(rectF.left, rectF.top, rectF.right, rectF.bottom)
        setVisible(true)
        viewWidth = rectF.width().toInt()
        paintAya.typeface = typeface
        paintAya.color = color
        paintAya.textSize = textSize
        paintBg.color = ViewCompat.MEASURED_STATE_MASK
        paintBg.alpha = ALPHA_BG
        setClrAya(color)
        maxH = (rect.height() * 0.85f).toInt()
        maxW = (rect.width() * 0.85f).toInt()
    }

    /**
     * Constructor with explicit text size but without background setup.
     */
    constructor(
        txt: String,
        rectF: RectF,
        typeface: Typeface,
        number: Int,
        color: Int,
        fontName: String,
        textSize: Float,
        hasBg: Boolean
    ) {
        this.txt = txt
        this.nameFont = fontName
        this.number = number
        rect = RectF(rectF.left, rectF.top, rectF.right, rectF.bottom)
        setVisible(true)
        viewWidth = rectF.width().toInt()
        paintAya.typeface = typeface
        paintAya.color = color
        paintAya.textSize = textSize
        maxH = (rect.height() * 0.85f).toInt()
        maxW = (rect.width() * 0.85f).toInt()
        paintBg.color = ViewCompat.MEASURED_STATE_MASK
        paintBg.alpha = ALPHA_BG
    }

    // ──────────────────────────────────────────────
    //  Background
    // ──────────────────────────────────────────────

    fun getClrBg(): Int = clrBg

    fun setHaveBg(haveBg: Boolean) { isHaveBg = haveBg }

    fun isHaveBg(): Boolean = isHaveBg

    fun setClrBg(color: Int) {
        clrBg = color
        paintBg.color = color
        paintBg.alpha = ALPHA_BG
    }

    // ──────────────────────────────────────────────
    //  Preset accessors
    // ──────────────────────────────────────────────

    fun getmPreset(): Int = mPreset
    fun setmPreset(preset: Int) { mPreset = preset }
    fun setIpad_type(type: Int) { ipad_type = type }
    fun getIpad_type(): Int = ipad_type
    fun getTxt(): String? = txt

    // ──────────────────────────────────────────────
    //  Aya preset application
    // ──────────────────────────────────────────────

    /**
     * Applies the given [AyaTextPreset] effect to [paint].
     *
     * Replaces the JADX-generated switch-map synthetic class (`C22071`)
     * with idiomatic Kotlin `when` expression.
     */
    fun applyAyaPreset(
        paint: Paint,
        ayaTextPreset: AyaTextPreset,
        color: Int,
        typeface: Typeface,
        textSize: Float
    ) {
        var ts = textSize
        paint.reset()
        paint.typeface = typeface
        paint.textSize = ts
        paint.isAntiAlias = true
        paint.isSubpixelText = true
        paint.isDither = true
        paint.style = Paint.Style.FILL
        paint.color = color

        if (paint === paintAyaTrslOutline) {
            ts *= 1.35f
        }

        when (ayaTextPreset) {
            AyaTextPreset.OUTLINE -> {
                paint.style = Paint.Style.FILL_AND_STROKE
                paint.strokeWidth = ts * 0.12f
                paint.strokeCap = Paint.Cap.ROUND
                paint.strokeJoin = Paint.Join.ROUND
                val isLightFrame = ipad_type == IpadType.HEART.ordinal ||
                        ipad_type == IpadType.BATTERY.ordinal ||
                        ipad_type == IpadType.BLUE_TYPE.ordinal
                paint.color = if (isLightFrame) {
                    hazem.nurmontage.videoquran.utils.ColorUtils.lightenColor(color, 0.85f)
                } else {
                    hazem.nurmontage.videoquran.utils.ColorUtils.darkenColor(color, 0.85f)
                }
            }
            AyaTextPreset.SHADOW -> {
                val radius = 0.18f * ts
                val dx = ts * 0.08f
                val isLightFrame = ipad_type == IpadType.HEART.ordinal ||
                        ipad_type == IpadType.BATTERY.ordinal ||
                        ipad_type == IpadType.BLUE_TYPE.ordinal
                val shadowColor = ColorUtils.setAlphaComponent(
                    if (isLightFrame) -1 else ViewCompat.MEASURED_STATE_MASK, 120
                )
                paint.setShadowLayer(radius, dx, dx, shadowColor)
            }
            AyaTextPreset.GLOW -> {
                paint.setShadowLayer(ts * 0.45f, 0f, 0f, ColorUtils.setAlphaComponent(color, 255))
            }
            AyaTextPreset.NONE -> { /* no effect */ }
        }
    }

    /**
     * Sets the text preset for the translation paint.
     * For OUTLINE preset, applies a two-layer approach: outline paint + fill paint on top.
     */
    fun setPreset(ayaTextPreset: AyaTextPreset) {
        mPreset = ayaTextPreset.ordinal
        if (ayaTextPreset == AyaTextPreset.OUTLINE) {
            applyAyaPreset(paintAyaOutline, AyaTextPreset.OUTLINE, clrAya, paintAya.typeface, paintAya.textSize)
            staticLayoutOutline = getStaticLayoutOutline()
            applyAyaPreset(paintAya, AyaTextPreset.NONE, clrAya, paintAya.typeface, paintAya.textSize)
        } else {
            applyAyaPreset(paintAya, ayaTextPreset, clrAya, paintAya.typeface, paintAya.textSize)
        }
    }

    /**
     * Converts an ordinal value back to its [AyaTextPreset] enum constant.
     */
    fun presetFromOrdinal(ordinal: Int): AyaTextPreset = when (ordinal) {
        AyaTextPreset.SHADOW.ordinal -> AyaTextPreset.SHADOW
        AyaTextPreset.OUTLINE.ordinal -> AyaTextPreset.OUTLINE
        AyaTextPreset.GLOW.ordinal -> AyaTextPreset.GLOW
        else -> AyaTextPreset.NONE
    }

    /** Backward-compatible alias kept for callers that use the original name. */
    fun get(ordinal: Int): AyaTextPreset = presetFromOrdinal(ordinal)

    /**
     * Initializes the preset from an ordinal, applying effects to the paint.
     */
    fun initPreset(ordinal: Int) {
        mPreset = ordinal
        val ayaTextPreset = presetFromOrdinal(ordinal)
        if (ayaTextPreset == AyaTextPreset.NONE) return
        if (ayaTextPreset == AyaTextPreset.OUTLINE) {
            applyAyaPreset(paintAyaOutline, AyaTextPreset.OUTLINE, clrAya, paintAya.typeface, paintAya.textSize)
            staticLayoutOutline = getStaticLayoutOutline()
            applyAyaPreset(paintAya, AyaTextPreset.NONE, clrAya, paintAya.typeface, paintAya.textSize)
        } else {
            applyAyaPreset(paintAya, ayaTextPreset, clrAya, paintAya.typeface, paintAya.textSize)
        }
    }

    fun initPresetAya(ordinal: Int) = initPreset(ordinal)

    // ──────────────────────────────────────────────
    //  View references
    // ──────────────────────────────────────────────

    fun setViewWeakReference(
        trackRef: WeakReference<TrackEntityView>?,
        blurRef: WeakReference<BlurredImageView>?
    ) {
        viewWeakReference = trackRef
        weakBlurredImageView = blurRef
    }

    /**
     * Calculates the fade animation duration based on the entity's visible time span.
     */
    fun getDuration_fade(): Int {
        val quran = getEntityQuran() ?: return 0
        return ((abs(quran.rect.right / quran.secondInScreen) -
                abs(quran.rect.left / quran.secondInScreen)) * 0.2f * 1000f).toInt()
    }

    fun getNameFont(): String = nameFont
    fun setIndex(index: Int) { this.index = index }
    fun getIndex(): Int = index

    // ──────────────────────────────────────────────
    //  Text & color
    // ──────────────────────────────────────────────

    fun setClrAya(color: Int) { clrAya = color }
    fun getClrAya(): Int = clrAya
    fun getPaintAya(): TextPaint = paintAya

    fun setTxt(txt: String) {
        this.txt = txt
        createStaticLayout()
    }

    // ──────────────────────────────────────────────
    //  Text-size calculation (binary search)
    // ──────────────────────────────────────────────

    /**
     * Calculates the largest text size that fits [text] within [maxWidth] x [maxHeight]
     * using a 100-iteration binary search on [paint] text bounds.
     */
    fun calculateTextSize(text: String?, paint: Paint, maxWidth: Int, maxHeight: Int): Float {
        if (text.isNullOrEmpty() || maxWidth <= 0 || maxHeight <= 0) return 0f
        paint.textSize = 1.0f
        val bounds = Rect()
        paint.getTextBounds(text, 0, text.length, bounds)
        var low = 0f
        var high = 1000f
        repeat(100) {
            val mid = (low + high) / 2f
            paint.textSize = mid
            paint.getTextBounds(text, 0, text.length, bounds)
            if (bounds.width() > maxWidth || bounds.height() > maxHeight) {
                high = mid
            } else {
                low = mid
            }
        }
        return low
    }

    /**
     * Calculates the optimal text size using [StaticLayout] for accurate multi-line measurement.
     * Uses binary search between 5.0f and 1000.0f.
     */
    fun calculateOptimalTextSize(text: String, maxWidth: Int, maxHeight: Int, textPaint: TextPaint): Float {
        var low = 5f
        var high = 1000f
        var result = 5f
        while (low <= high) {
            val mid = (low + high) / 2f
            textPaint.textSize = mid
            val layout = StaticLayout.Builder.obtain(text, 0, text.length, textPaint, maxWidth)
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .setLineSpacing(0f, 1f)
                .setIncludePad(false)
                .build()
            val height = layout.height
            val maxLineWidth = getMaxLineWidth(layout)
            if (height > maxHeight || maxLineWidth > maxWidth) {
                high = mid - 0.03f
            } else {
                result = mid
                low = 0.03f + mid
            }
        }
        return result
    }

    /** Returns the maximum line width across all lines in [layout]. */
    private fun getMaxLineWidth(layout: StaticLayout): Float {
        var maxWidth = 0f
        for (i in 0 until layout.lineCount) {
            maxWidth = max(maxWidth, layout.getLineWidth(i))
        }
        return maxWidth
    }

    /**
     * Iteratively reduces text size from 28% to 8% of rect height until
     * the layout fits within [availableHeight].  Uses 1.12 line-spacing factor.
     */
    private fun createBestSizeLayout(text: String, textPaint: TextPaint, maxWidth: Int, availableHeight: Int): Float {
        val minSize = rect.height() * 0.08f
        var textSize = rect.height() * 0.28f
        while (textSize >= minSize) {
            textPaint.textSize = textSize
            val layout = StaticLayout.Builder.obtain(text, 0, text.length, textPaint, maxWidth)
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .setLineSpacing(0f, 1.12f)
                .setIncludePad(false)
                .build()
            if (layout.height <= availableHeight) {
                return textPaint.textSize
            }
            textSize -= 1.0f
        }
        textPaint.textSize = minSize
        return textPaint.textSize
    }

    /** Convenience: calculates text size using the entity's own rect. */
    fun calculateTextSize(): Float {
        return createBestSizeLayout(txt!!, paintAya, (rect.width() * 0.9f).toInt(), (rect.height() * 0.95f).toInt())
    }

    fun setTextSize(size: Float) { paintAya.textSize = size }

    /**
     * Sets the text size and rebuilds the static layout in a loop context
     * (used during iterative size adjustment).
     */
    fun setTextSizeInBoucle(size: Float) {
        paintAya.textSize = size
        val spannable = SpannableString(txt!!)
        staticLayout = StaticLayout.Builder.obtain(spannable, 0, spannable.length, paintAya, viewWidth)
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setLineSpacing(0f, 1f)
            .setIncludePad(false)
            .build()
        posY = rect.centerY() - (staticLayout!!.height * 0.5f)
        posX = rect.centerX() - (staticLayout!!.width * 0.5f)
    }

    // ──────────────────────────────────────────────
    //  Balanced layout
    // ──────────────────────────────────────────────

    /**
     * Creates a [StaticLayout] that reduces text size until the last line
     * has at least 2 words and occupies at least 25% of the available width,
     * preventing orphaned words.
     */
    private fun createBalancedLayout(
        text: String,
        textPaint: TextPaint,
        availableWidth: Int,
        initialTextSize: Float,
        minTextSize: Float
    ): StaticLayout {
        var textSize = initialTextSize
        val spannable = SpannableString(text)
        var layout: StaticLayout
        do {
            textPaint.textSize = textSize
            layout = StaticLayout.Builder.obtain(spannable, 0, spannable.length, textPaint, availableWidth)
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .setIncludePad(false)
                .setLineSpacing(0f, 1f)
                .build()
            val lineCount = layout.lineCount
            if (lineCount <= 1) break
            val lastLine = lineCount - 1
            val lastLineWidth = layout.getLineWidth(lastLine)
            val lastLineText = text.substring(layout.getLineStart(lastLine), layout.getLineEnd(lastLine)).trim()
            val wordCount = lastLineText.split("\\s+".toRegex()).filter { it.isNotEmpty() }.size
            val hasOrphan = lastLineWidth < availableWidth * 0.25f || wordCount > 1
            if (hasOrphan) break
            textSize -= 1.0f
        } while (textSize > minTextSize)
        return layout
    }

    // ──────────────────────────────────────────────
    //  Scale & setup
    // ──────────────────────────────────────────────

    fun setupScale(factor: Float, canvasW: Int, canvasH: Int) {
        val canvasWf = canvasW.toFloat()
        viewWidth = (0.9f * canvasWf).toInt()
        staticLayout = createBalancedLayout(txt!!, paintAya, viewWidth, factor * canvasWf, 2.0f)
        val halfW = staticLayout!!.width * 0.5f
        val halfH = staticLayout!!.height * 0.5f
        val padding = rect.height() * 0.12f
        val centerY = rect.centerY()
        val centerX = rect.centerX()
        rect.set(centerX - halfW, centerY - halfH - padding, centerX + halfW, centerY + halfH + padding)
        maxH = round(rect.height() * 0.85f).toInt()
        maxW = round(rect.width() * 0.85f).toInt()
        posX = rect.centerX() - (staticLayout!!.width * 0.5f)
        posY = rect.centerY() - (staticLayout!!.height * 0.5f)
    }

    fun setupScaleSave(factor: Float, canvasW: Int) {
        viewWidth = round(rect.width()).toInt()
        staticLayout = createBalancedLayout(txt!!, paintAya, viewWidth, factor * canvasW, 2.0f)
        maxH = round(rect.height() * 0.85f).toInt()
        maxW = round(rect.width() * 0.85f).toInt()
        posX = rect.centerX() - (staticLayout!!.width * 0.5f)
        posY = rect.centerY() - (staticLayout!!.height * 0.5f)
    }

    fun updatePaint(textSize: Float, viewWidth: Int) {
        paintAya.textSize = textSize
        val spannable = SpannableString(txt!!)
        this.viewWidth = viewWidth
        staticLayout = StaticLayout.Builder.obtain(spannable, 0, spannable.length, paintAya, viewWidth)
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setLineSpacing(0f, 1f)
            .setIncludePad(false)
            .build()
        maxH = round(rect.height() * 0.85f).toInt()
        maxW = round(rect.width() * 0.85f).toInt()
        posX = rect.centerX() - (staticLayout!!.width * 0.5f)
        posY = rect.centerY() - (staticLayout!!.height * 0.5f)
    }

    override fun scale(factor: Float, canvasW: Int, canvasH: Int) {
        setFactorScale(factor)
        val height = rect.height() * factor
        val halfW = 0.46f * canvasW
        rect.left = rect.centerX() - halfW
        rect.right = rect.centerX() + halfW
        val halfH = height * 0.5f
        rect.top = rect.centerY() - halfH
        rect.bottom = rect.centerY() + halfH
        viewWidth = rect.width().toInt()
        paintAya.textSize = calculateTextSize()
        createStaticLayout()
        setFcSize(paintAya.textSize / canvasW)
        initPreset(getmPreset())
    }

    // ──────────────────────────────────────────────
    //  Apply-all helpers (batch position + size)
    // ──────────────────────────────────────────────

    fun applyAll(canvasW: Int, rectF: RectF, textSize: Float, fcSize: Float, reference: TranslationQuranEntity) {
        paintAya.textSize = textSize
        val spannable = SpannableString(txt!!)
        viewWidth = (maxOf(rectF.width(), round(paintAya.measureText(spannable.toString()))) * 1.1f).toInt()
        val layout = StaticLayout.Builder.obtain(spannable, 0, spannable.length, paintAya, viewWidth)
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setLineSpacing(0f, 1f)
            .setIncludePad(false)
            .build()
        staticLayout = layout
        val w = layout.width
        setFcSize(fcSize)
        val halfW = w * 0.5f
        val halfH = rect.height() * (w / rect.width()) * 0.5f
        rect.set(rectF.centerX() - halfW, rectF.centerY() - halfH, rectF.centerX() + halfW, rectF.centerY() + halfH)
        posX = rect.centerX() - (staticLayout!!.width * 0.5f)
        posY = rect.centerY() - (staticLayout!!.height * 0.5f)
        maxH = round(rect.height() * 0.85f).toInt()
        maxW = round(rect.width() * 0.85f).toInt()
        initPreset(getmPreset())
    }

    fun applyAll(canvasW: Int, rectF: RectF, textSize: Float, fcSize: Float) {
        viewWidth = round(rectF.width()).toInt()
        val layout = createBalancedLayout(txt!!, paintAya, viewWidth, textSize, 2.0f)
        staticLayout = layout
        val w = layout.width
        setFcSize(fcSize)
        val padding = rect.height() * 0.12f
        val halfW = w * 0.5f
        val halfH = staticLayout!!.height * 0.5f
        rect.set(rectF.centerX() - halfW, rectF.centerY() - halfH - padding, rectF.centerX() + halfW, rectF.centerY() + halfH + padding)
        posX = rect.centerX() - (staticLayout!!.width * 0.5f)
        posY = rect.centerY() - (staticLayout!!.height * 0.5f)
        maxH = round(rect.height() * 0.85f).toInt()
        maxW = round(rect.width() * 0.85f).toInt()
        initPreset(getmPreset())
    }

    // ──────────────────────────────────────────────
    //  Translate
    // ──────────────────────────────────────────────

    override fun postTranslate(dx: Float, dy: Float) {
        rect.offset(dx, dy)
        posX = rect.centerX() - (staticLayout!!.width * 0.5f)
        posY = rect.centerY() - (staticLayout!!.height * 0.5f)
    }

    fun setTranslate(cx: Float, cy: Float) {
        val halfW = rect.width() * 0.5f
        val halfH = rect.height() * 0.5f
        rect.left = cx - halfW
        rect.right = cx + halfW
        rect.top = cy - halfH
        rect.bottom = cy + halfH
        posX = rect.centerX() - (staticLayout!!.width * 0.5f)
        posY = rect.centerY() - (staticLayout!!.height * 0.5f)
    }

    /** Returns the width of the text at a reference text size of 3.0f. */
    fun getWidth(): Float {
        paintAya.textSize = 3.0f
        return paintAya.measureText(txt)
    }

    // ──────────────────────────────────────────────
    //  Static-layout creation
    // ──────────────────────────────────────────────

    fun createStaticLayout() {
        staticLayout = createBalancedLayout(txt!!, paintAya, viewWidth, paintAya.textSize, 2.0f)
        posY = rect.centerY() - (staticLayout!!.height * 0.5f)
        posX = rect.centerX() - (staticLayout!!.width * 0.5f)
    }

    private fun buildStaticLayout(text: String, textPaint: TextPaint, width: Int): StaticLayout =
        StaticLayout.Builder.obtain(text, 0, text.length, textPaint, width)
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setLineSpacing(0f, 1f)
            .setIncludePad(false)
            .build()

    fun setStaticLayout() {
        val spannable = SpannableString(txt!!)
        staticLayout = StaticLayout.Builder.obtain(spannable, 0, spannable.length, paintAya, viewWidth)
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setLineSpacing(0f, 1f)
            .setIncludePad(false)
            .build()
        posX = rect.centerX() - (staticLayout!!.width * 0.5f)
        posY = rect.centerY() - (staticLayout!!.height * 0.5f)
    }

    private fun getStaticLayoutOutline(): StaticLayout {
        val spannable = SpannableString(txt!!)
        return StaticLayout.Builder.obtain(spannable, 0, spannable.length, paintAyaOutline, viewWidth)
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setLineSpacing(0f, 1f)
            .setIncludePad(false)
            .build()
    }

    fun updateStaticLayout() {
        val ss = spannableString ?: return
        staticLayout = StaticLayout.Builder.obtain(ss, 0, ss.length, paintAya, viewWidth)
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setLineSpacing(0f, 1f)
            .setIncludePad(false)
            .build()
    }

    // ──────────────────────────────────────────────
    //  Fade / animation state
    // ──────────────────────────────────────────────

    fun setFadeIn(fadeIn: Boolean) { isFadeIn = fadeIn }
    fun setFadeOut(fadeOut: Boolean) { isFadeOut = fadeOut }
    fun isFadeIn(): Boolean = isFadeIn
    fun isFadeOut(): Boolean = isFadeOut

    fun isAnimRun(): Boolean {
        val a1 = objectAnimator
        val a2 = otherAnimation
        return (a1 != null && a1.isRunning) || (a2 != null && a2.isRunning)
    }

    fun setOpacityFade(alpha: Int) {
        paintAya.alpha = alpha
        paintAyaTrslOutline.alpha = paintAya.alpha
        paintAyaOutline.alpha = paintAya.alpha
        if (isAnimTest()) {
            weakBlurredImageView?.get()?.invalidate()
        } else {
            viewWeakReference?.get()?.invalidate()
        }
    }

    override fun endAnimator() {
        try {
            objectAnimator?.takeIf { it.isRunning }?.end()
            otherAnimation?.takeIf { it.isRunning }?.end()
        } catch (_: Exception) {
        }
        objectAnimator = null
        otherAnimation = null
        setFadeIn(false)
        setFadeOut(false)
        offsetX = 0f
        paintAya.alpha = 255
        paintAyaTrslOutline.alpha = paintAya.alpha
        paintAyaOutline.alpha = paintAya.alpha
    }

    // ──────────────────────────────────────────────
    //  ObjectAnimator property setters
    // ──────────────────────────────────────────────

    fun setSlideX(value: Float) {
        offsetX = value
        paintAya.alpha = round((1f - abs(value)) * 255f)
        paintAyaTrslOutline.alpha = paintAya.alpha
        paintAyaOutline.alpha = paintAya.alpha
        if (isAnimTest()) weakBlurredImageView?.get()?.invalidate()
    }

    fun setSlideXOut(value: Float) {
        offsetX = value
        paintAya.alpha = round((1f - abs(value)) * 255f)
        paintAyaTrslOutline.alpha = paintAya.alpha
        paintAyaOutline.alpha = paintAya.alpha
        if (isAnimTest()) weakBlurredImageView?.get()?.invalidate()
    }

    fun setFactorSize(factor: Float) {
        scaleX = factor
        if (isAnimTest()) weakBlurredImageView?.get()?.invalidate()
    }

    // ──────────────────────────────────────────────
    //  Transition animations
    // ──────────────────────────────────────────────

    fun slidToLeft(duration: Int, repeat: Boolean) {
        val anim = ObjectAnimator.ofFloat(this, "SlideX", 1f, 0f)
        otherAnimation = anim
        anim.duration = duration.toLong()
        if (repeat) { anim.repeatMode = ObjectAnimator.RESTART; anim.repeatCount = -1 }
        anim.start()
    }

    fun slidToRightOut(duration: Int, repeat: Boolean) {
        val anim = ObjectAnimator.ofFloat(this, "SlideXOut", 0f, 1f)
        otherAnimation = anim
        anim.duration = duration.toLong()
        if (repeat) { anim.repeatMode = ObjectAnimator.RESTART; anim.repeatCount = -1 }
        anim.start()
    }

    fun slidToLeftOut(duration: Int, repeat: Boolean) {
        val anim = ObjectAnimator.ofFloat(this, "SlideXOut", 0f, -1f)
        otherAnimation = anim
        anim.duration = duration.toLong()
        if (repeat) { anim.repeatMode = ObjectAnimator.RESTART; anim.repeatCount = -1 }
        anim.start()
    }

    fun slidToRight(duration: Int, repeat: Boolean) {
        val anim = ObjectAnimator.ofFloat(this, "SlideX", -1f, 0f)
        otherAnimation = anim
        anim.duration = duration.toLong()
        if (repeat) { anim.repeatMode = ObjectAnimator.RESTART; anim.repeatCount = -1 }
        anim.start()
    }

    fun zoomIn_In(duration: Int, repeat: Boolean) {
        val anim = ObjectAnimator.ofFloat(this, "FactorSize", 0f, 1f)
        otherAnimation = anim
        anim.duration = duration.toLong()
        if (repeat) { anim.repeatMode = ObjectAnimator.RESTART; anim.repeatCount = -1 }
        anim.start()
    }

    fun runIn(duration: Int, repeat: Boolean, transition: String) {
        when (transition) {
            TransitionType.SLIDE_TO_LEFT.value -> slidToLeft(duration, repeat)
            TransitionType.SLIDE_TO_RIGHT.value -> slidToRight(duration, repeat)
            TransitionType.ZOOM_IN.value -> zoomIn_In(duration, repeat)
            TransitionType.FADE_IN.value -> fadeIn(duration, repeat)
        }
    }

    private fun fadeIn(duration: Int, repeat: Boolean) {
        val anim = ObjectAnimator.ofInt(this, "OpacityFade", 0, 255)
        objectAnimator = anim
        anim.duration = duration.toLong()
        if (repeat) { anim.repeatMode = ObjectAnimator.RESTART; anim.repeatCount = -1 }
        anim.start()
    }

    private fun fadeOut(duration: Int, repeat: Boolean) {
        val anim = ObjectAnimator.ofInt(this, "OpacityFade", 255, 0)
        objectAnimator = anim
        anim.duration = duration.toLong()
        if (repeat) { anim.repeatMode = ObjectAnimator.RESTART; anim.repeatCount = -1 }
        anim.start()
    }

    fun runOut(duration: Int, repeat: Boolean, transition: String) {
        when (transition) {
            TransitionType.SLIDE_TO_LEFT.value -> slidToLeftOut(duration, repeat)
            TransitionType.SLIDE_TO_RIGHT.value -> slidToRightOut(duration, repeat)
            TransitionType.FADE_OUT.value -> fadeOut(duration, repeat)
        }
    }

    // ──────────────────────────────────────────────
    //  Number & update helpers
    // ──────────────────────────────────────────────

    fun getNumber(): Int = number
    fun setNumber(number: Int) { this.number = number }

    fun update(rectF: RectF, maxW: Int, maxH: Int) {
        rect = rectF
        this.maxH = maxH
        this.maxW = maxW
        viewWidth = rect.width().toInt()
    }

    fun onResize(rectF: RectF, maxW: Int, maxH: Int) {
        rect = RectF(0f, getCanvasH() - rectF.height(), getCanvasW().toFloat(), getCanvasH().toFloat())
        this.maxH = maxH
        this.maxW = maxW
        viewWidth = rect.width().toInt()
    }

    // ──────────────────────────────────────────────
    //  EntityView overrides
    // ──────────────────────────────────────────────

    override fun getMaxH(): Int = maxH
    override fun getMaxW(): Int = maxW

    fun getStaticLayout(): StaticLayout? = staticLayout

    fun setTypeface(typeface: Typeface, fontName: String) {
        paintAya.typeface = typeface
        nameFont = fontName
    }

    fun setTypefaceOneAya(typeface: Typeface, fontName: String) {
        paintAya.typeface = typeface
        nameFont = fontName
    }

    fun setColor(color: Int) {
        setClrAya(color)
        paintAya.color = color
    }

    // ──────────────────────────────────────────────
    //  Drawing
    // ──────────────────────────────────────────────

    fun draw(canvas: Canvas) {
        val layout = staticLayout ?: return
        if (isHaveBg) {
            canvas.drawRect(0f, rect.top, canvas.width.toFloat(), rect.bottom, paintBg)
        }
        canvas.save()
        canvas.translate(posX + (offsetX * layout.width), posY)
        canvas.scale(scaleX, scaleX)
        if (mPreset == AyaTextPreset.OUTLINE.ordinal && staticLayoutOutline != null) {
            paintAyaOutline.textSize = paintAya.textSize
            staticLayoutOutline!!.draw(canvas)
        }
        layout.draw(canvas)
        canvas.restore()
    }

    fun setupCanvasDraw(canvas: Canvas) {
        val layout = staticLayout ?: return
        posY = (canvas.height - layout.height) * 0.5f
        posX = rect.centerX() - (layout.width * 0.5f)
        canvas.save()
        if (isHaveBg) {
            canvas.drawRect(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), paintBg)
        }
        canvas.translate(posX, posY)
    }

    fun restoreCanvas(canvas: Canvas) {
        try { canvas.restore() } catch (_: Exception) {}
    }

    fun singleDraw(canvas: Canvas, alpha: Int) {
        val layout = staticLayout ?: return
        paintAya.alpha = alpha
        layout.draw(canvas)
    }

    fun singleDraw(canvas: Canvas, alpha: Int, offsetFraction: Float) {
        val layout = staticLayout ?: return
        canvas.save()
        canvas.translate(offsetFraction * layout.width, 0f)
        paintAya.alpha = alpha
        layout.draw(canvas)
        canvas.restore()
    }

    fun singleDraw(canvas: Canvas) {
        val layout = staticLayout ?: return
        if (mPreset == AyaTextPreset.OUTLINE.ordinal && staticLayoutOutline != null) {
            paintAyaOutline.textSize = paintAya.textSize
            staticLayoutOutline!!.draw(canvas)
        }
        layout.draw(canvas)
    }

    // ──────────────────────────────────────────────
    //  Visibility & accessors
    // ──────────────────────────────────────────────

    override fun isVisible(): Boolean = isVisible
    override fun setVisible(visible: Boolean) { isVisible = visible }

    fun getX(): Float = posX
    fun getY(): Float = posY

    override fun setAnimTest(animTest: Boolean) { super.setAnimTest(animTest) }

    fun setUnderLine(underline: Boolean) { paintAya.isUnderlineText = underline }
}
