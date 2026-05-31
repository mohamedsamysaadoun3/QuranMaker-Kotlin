package hazem.nurmontage.videoquran.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Point
import android.graphics.PointF
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import hazem.nurmontage.videoquran.multitouch.MoveGestureDetector
import android.graphics.Typeface
import android.graphics.drawable.VectorDrawable
import android.text.Layout
import android.text.TextPaint
import android.util.AttributeSet

import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.ViewCompat
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.core.common.Constants.AyaTextPreset
import hazem.nurmontage.videoquran.constant.IpadType
import hazem.nurmontage.videoquran.constant.ResizeType
import hazem.nurmontage.videoquran.constant.SurahNameStyle
import hazem.nurmontage.videoquran.model.data.BismilahEntity
import hazem.nurmontage.videoquran.model.EntitySelectTool
import hazem.nurmontage.videoquran.model.EntityView
import hazem.nurmontage.videoquran.model.Gradient
import hazem.nurmontage.videoquran.model.data.QuranEntity
import hazem.nurmontage.videoquran.model.SurahNameEntity
import hazem.nurmontage.videoquran.model.Template
import hazem.nurmontage.videoquran.model.TimeModel
import hazem.nurmontage.videoquran.model.data.TranslationQuranEntity
import hazem.nurmontage.videoquran.model.Transition
import hazem.nurmontage.videoquran.utils.AspectRatioCalculator
import hazem.nurmontage.videoquran.utils.ColorSchemeGenerator
import hazem.nurmontage.videoquran.utils.ColorUtils
import hazem.nurmontage.videoquran.utils.CreateGradient
import hazem.nurmontage.videoquran.utils.FontUtils
import hazem.nurmontage.videoquran.utils.Utils
import hazem.nurmontage.videoquran.utils.UtilsFileLast
import nl.dionsegijn.konfetti.core.Angle
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Custom View that renders a blurred background image with overlay Quran text entities,
 * iPad-style frames, progress bars, bismillah, surah names, and watermarks.
 *
 * Supports multi-touch gestures (pinch-to-scale, drag-to-move) for entity manipulation,
 * selection tools, and various iPad frame types (classic, neumorphic, cassette, heart, battery, etc.)
 *
 * Originally: BlurredImageView.java (4,516 lines)
 * Converted to: BlurredImageView.kt — faithful Kotlin conversion
 */
class BlurredImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), View.OnTouchListener {

    // ═══════════════════════════════════════════════════════════════════
    //  Companion object (static constants and methods)
    // ═══════════════════════════════════════════════════════════════════

    companion object {
        private const val SNAP_FORCE = 0.2f
        private const val SNAP_THRESHOLD = 30.0f
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Interface
    // ═══════════════════════════════════════════════════════════════════

    interface IViewCallback {
        fun onDrawFinish()
        fun onEmtyClick()
        fun onEndMove()
        fun onEndScale()
        fun onSelect(entityView: EntityView)
        fun onSquare()
        fun onWattermark()
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Fields
    // ═══════════════════════════════════════════════════════════════════

    var backgroundPaint: Paint = Paint()
        private set
    internal var bismilahEntity: BismilahEntity? = null
    var bitmapBlured: Bitmap? = null
    internal var bitmapNotBlur: Bitmap? = null
    var bitmapOriginal: Bitmap? = null
    internal var bitmapSquare: Bitmap? = null
    internal var btmX: Float = 0f
    internal var btmY: Float = 0f
    internal var clr_aya: Int = 0
    internal var clr_trsl: Int = 0
    internal var color_bg_type_classic: Int = 0
    internal var color_gradient: Gradient? = null
    internal var color_ipad: Int = -1
    internal var color_line_bg: Int = 0
    internal var currentTime: String = "0:00"
    var darkShadowPaint: Paint = Paint()
        private set
    internal var entity_select: EntityView? = null
    internal var frameInterval: Long = 0L
    internal var gestureDetector: GestureDetectorCompat? = null
    internal var grayscalePaint: Paint = Paint()
    internal var iViewCallback: IViewCallback? = null
    internal var ipad_rect: RectF? = null
    internal var isAnimWatermk: Boolean = false
    internal var isDrawingSquareVideo: Boolean = false
    internal var isGlass: Boolean = false
    internal var isNotDraw: Boolean = false
    internal var isOnScale: Boolean = false
    internal var isPlaying: Boolean = false
    internal var isPro: Boolean = false
    internal var isRemoveWattermark: Boolean = false
    internal var isSquare: Boolean = false
    internal var isVideo: Boolean = false
    internal var isWattermark: Boolean = false
    internal var left_square: Float = 0f
    var lightShadowPaint: Paint = Paint()
        private set
    internal var linePaint: Paint = Paint()
    internal var linearGradient_classic: LinearGradient? = null
    internal var mCanvas_height: Int = 0
    internal var mCanvas_width: Int = 0
    internal var mDrawingTranslationX: Float = 0f
    internal var mDrawingTranslationY: Float = 0f
    internal var mIpadType: Int = IpadType.IPAD.ordinal
    internal var mIsti3adhaEntity: BismilahEntity? = null
    internal var mRectWattermark: RectF? = null
    internal var mResizetype: Int = 0
    internal var moveGestureDetector: MoveGestureDetector? = null
    internal var newLeft_txt: Float = 0f
    internal var paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    internal var paintClear: Paint = Paint()
    internal var paintIpad: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    internal var paintLecture: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    internal var paintText: TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    internal var paintWattermark: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    internal var prevDistance: Float = -1.0f
    internal var progress: Float = 0f
    private val quranEntities: MutableList<QuranEntity> = ArrayList()
    internal var radius_cursur: Float = 0f
    internal var radius_square: Int = 0
    internal var rectFAya: RectF? = null
    internal var rectFLecture: RectF? = null
    internal var rectFProgress: RectF? = null
    internal var rectFSurahName: RectF? = null
    internal var rectSquare: Rect? = null
    internal var remainingTime: String = "0:15"
    internal var scaleGestureDetector: ScaleGestureDetector? = null
    internal var scheme: ColorSchemeGenerator.Scheme? = null
    internal var selectTool: EntitySelectTool? = null
    internal var showCenterLineX: Boolean = false
    internal var showCenterLineY: Boolean = false
    internal var startTime: Long = -1L
    var surahNameEntity: SurahNameEntity? = null
    internal var top_square: Float = 0f
    private val translationEntities: MutableList<TranslationQuranEntity> = ArrayList()
    internal var txt_y: Float = 0f
    internal var wmAlpha: Float = 1.0f
    internal var wmScale: Float = 1.0f
    internal var wmTranslateY: Float = 0f

    // ═══════════════════════════════════════════════════════════════════
    //  Gesture listener (defined once, used by all constructors)
    // ═══════════════════════════════════════════════════════════════════

    private val gestureListener: GestureDetector.SimpleOnGestureListener =
        object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent): Boolean {
                if (!isPro && mRectWattermark != null && mRectWattermark!!.contains(e.x, e.y)) {
                    isWattermark = true
                }
                if (entity_select != null && entity_select!!.isVisible() && !isWattermark) {
                    if (selectTool!!.isApply(entity_select, e.x, e.y)) {
                        if (selectTool!!.isApply_Move()) {
                            iViewCallback!!.onEndMove()
                        }
                        if (selectTool!!.isApply_Scale()) {
                            iViewCallback!!.onEndScale()
                        }
                        selectTool!!.setClick_apply(true)
                        selectTool!!.reset()
                    } else {
                        selectTool!!.isScale(entity_select, e.x, e.y)
                    }
                    if (selectTool!!.isApply_Scale()) {
                        selectTool!!.setOnProgress(true)
                        prevDistance = distanceToCenter(e.x, e.y)
                    }
                }
                return true
            }

            override fun onSingleTapUp(e: MotionEvent): Boolean {
                if (entity_select != null && selectTool!!.isClick_apply()) {
                    selectTool!!.setClick_apply(false)
                    invalidate()
                    return true
                }
                if (!isWattermark) {
                    updateSelectionOnTap(e.x, e.y)
                }
                isOnScale = false
                if (iViewCallback != null) {
                    if (entity_select == null) {
                        if (isWattermark) {
                            iViewCallback!!.onWattermark()
                        } else if (isSquare) {
                            iViewCallback!!.onSquare()
                        } else {
                            iViewCallback!!.onEmtyClick()
                        }
                    } else if (selectTool != null && selectTool!!.isApply_Move() &&
                        ((entity_select is QuranEntity) || (entity_select is TranslationQuranEntity)) &&
                        !selectTool!!.isApply_all()
                    ) {
                        selectTool!!.setApply_all(true)
                        invalidate()
                    }
                    isWattermark = false
                    isSquare = false
                }
                return super.onSingleTapUp(e)
            }
        }

    // ═══════════════════════════════════════════════════════════════════
    //  init block
    // ═══════════════════════════════════════════════════════════════════

    init {
        init()
    }

    // ═══════════════════════════════════════════════════════════════════
    //  entity_select with custom setter logic
    // ═══════════════════════════════════════════════════════════════════

    fun setEntity_select(entityView: EntityView?) {
        if (this.entity_select != entityView) {
            selectTool?.reset()
        }
        this.entity_select = entityView
    }

    fun getEntity_select(): EntityView? = this.entity_select

    // ═══════════════════════════════════════════════════════════════════
    //  Simple getters and setters
    // ═══════════════════════════════════════════════════════════════════

    fun isRemoveWattermark(): Boolean = this.isRemoveWattermark
    fun setRemoveWattermark(z: Boolean) { this.isRemoveWattermark = z }
    fun setBitmapNotBlur(bitmap: Bitmap?) { this.bitmapNotBlur = bitmap }
    fun getBitmapNotBlur(): Bitmap? = this.bitmapNotBlur
    fun isVideo(): Boolean = this.isVideo
    fun setVideo(z: Boolean) { this.isVideo = z }
    fun setDrawingSquareVideo(z: Boolean) { this.isDrawingSquareVideo = z }
    fun isDrawingSquareVideo(): Boolean = this.isDrawingSquareVideo
    fun setPlaying(z: Boolean) { this.isPlaying = z }
    fun isPlaying(): Boolean = this.isPlaying
    fun setPro(z: Boolean) { this.isPro = z }
    fun isPro(): Boolean = this.isPro



    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (this.selectTool == null) {
            this.selectTool = EntitySelectTool(w, context)
        }
    }

    fun getColor_ipad(): Int = this.color_ipad
    fun setColor_ipad(color: Int) { setColorIpad(color) }
    fun setResizetype(i: Int) { this.mResizetype = i }
    fun getmIpadType(): Int = this.mIpadType
    fun setIpad_rect(rectF: RectF?) { this.ipad_rect = rectF }
    fun setRectSquare(rect: Rect?) { this.rectSquare = rect }
    fun getBtmX(): Float = this.btmX
    fun getBtmY(): Float = this.btmY
    fun getmDrawingTranslationY(): Float = this.mDrawingTranslationY
    fun getmDrawingTranslationX(): Float = this.mDrawingTranslationX
    fun getmCanvas_height(): Int = this.mCanvas_height
    fun getmCanvas_width(): Int = this.mCanvas_width

    // ═══════════════════════════════════════════════════════════════════
    //  Canvas dimension initialization
    // ═══════════════════════════════════════════════════════════════════

    fun initCanvasDimension(i: Int, i2: Int, i3: Int) {
        if (i3 == ResizeType.SOCIAL_STORY.ordinal) {
            this.mCanvas_height = i2
            this.mCanvas_width = AspectRatioCalculator.calculateWidth(i2)
        } else if (i3 == ResizeType.SQUARE.ordinal) {
            val minVal = min(i, i2)
            this.mCanvas_width = minVal
            this.mCanvas_height = minVal
        } else {
            this.mCanvas_width = i
            this.mCanvas_height = AspectRatioCalculator.calculateHeight_Youtube(i)
        }
    }

    fun getW(): Int = (width - paddingStart) - paddingEnd
    fun getH(): Int = (height - paddingTop) - paddingBottom

    // ═══════════════════════════════════════════════════════════════════
    //  updatePosCanvas
    // ═══════════════════════════════════════════════════════════════════

    fun updatePosCanvas(bitmap: Bitmap?) {
        if (bitmap == null) return
        val w = (width - paddingStart - paddingEnd).toFloat()
        val h = (height - paddingTop - paddingBottom).toFloat()
        this.mDrawingTranslationX = (w - this.mCanvas_width) / 2.0f
        this.mDrawingTranslationY = (h - this.mCanvas_height) / 2.0f
        this.btmX = ((w - bitmap.width) / 2.0f) - this.mDrawingTranslationX
        this.btmY = ((h - bitmap.height) / 2.0f) - this.mDrawingTranslationY
    }

    fun updatePosCanvas(i: Int, i2: Int, bitmap: Bitmap?) {
        if (bitmap == null) return
        this.mDrawingTranslationX = (i - this.mCanvas_width) / 2.0f
        this.mDrawingTranslationY = (i2 - this.mCanvas_height) / 2.0f
        this.btmX = ((i - bitmap.width) / 2.0f) - this.mDrawingTranslationX
        this.btmY = ((i2 - bitmap.height) / 2.0f) - this.mDrawingTranslationY
    }

    fun getProgress(): Float = this.progress

    // ═══════════════════════════════════════════════════════════════════
    //  addEntity
    // ═══════════════════════════════════════════════════════════════════

    fun addEntity(quranEntity: QuranEntity) {
        this.quranEntities.add(quranEntity)
        quranEntity.setIndex(this.quranEntities.size - 1)
    }

    fun addEntity(translationQuranEntity: TranslationQuranEntity) {
        this.translationEntities.add(translationQuranEntity)
        translationQuranEntity.setIndex(this.translationEntities.size - 1)
    }

    fun getQuranEntities(): List<QuranEntity> = this.quranEntities
    fun getPaintLecture(): Paint = this.paintLecture

    fun addEntity(quranEntity: QuranEntity, i: Int) {
        if (i < this.quranEntities.size) {
            this.quranEntities.add(i, quranEntity)
        } else {
            this.quranEntities.add(quranEntity)
        }
        quranEntity.setIndex(i)
    }

    fun addEntity(translationQuranEntity: TranslationQuranEntity, i: Int) {
        if (i < this.translationEntities.size) {
            this.translationEntities.add(i, translationQuranEntity)
        } else {
            this.translationEntities.add(translationQuranEntity)
        }
        translationQuranEntity.setIndex(i)
    }

    fun getBitmapSquare(): Bitmap? = this.bitmapSquare
    fun setClr_aya(i: Int) { this.clr_aya = i }
    fun setClr_trsl(i: Int) { this.clr_trsl = i }
    fun getClr_aya(): Int = this.clr_aya
    fun getClr_trsl(): Int = this.clr_trsl
    fun getColor_gradient(): Gradient? = this.color_gradient
    fun setColor_gradient(gradient: Gradient?) { this.color_gradient = gradient }
    fun colorIpad(): Int = this.color_ipad

    // ═══════════════════════════════════════════════════════════════════
    //  changeColorIpad
    // ═══════════════════════════════════════════════════════════════════

    fun changeColorIpad() {
        if (getColor_gradient() != null) {
            setColorIpad(getColor_gradient()!!)
        } else {
            setColorIpad(colorIpad())
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  setColorIpad(int)
    // ═══════════════════════════════════════════════════════════════════

    fun setColorIpad(i: Int) {
        setColor_gradient(null)
        this.paintIpad.shader = null
        this.color_ipad = i
        if (this.mIpadType == IpadType.IPAD_CLASSIC.ordinal) {
            this.color_bg_type_classic = ColorUtils.lightenColor(i, 0.4f)
            this.paintIpad.color = ColorUtils.darkenColor(i, 0.2f)
        } else {
            this.paintIpad.color = i
        }
        if (this.mIpadType == IpadType.BORDER.ordinal) {
            this.color_line_bg = ColorUtils.darkenColor(i, 0.4f)
            this.paintLecture.color = i
        } else if (this.mIpadType == IpadType.BLUE_TYPE.ordinal) {
            this.paintLecture.color = ColorUtils.convertToEnergyColor(i)
            this.color_line_bg = ColorUtils.darkenColor(this.paintLecture.color, 0.7f)
        } else if (this.mIpadType == IpadType.CASSET.ordinal ||
            this.mIpadType == IpadType.CASSET_IMG.ordinal ||
            this.mIpadType == IpadType.CASSET_IMG_BLUR.ordinal
        ) {
            val generateScheme = ColorSchemeGenerator.generateScheme(i)
            this.scheme = generateScheme
            if (ColorUtils.isColorDark(generateScheme.label)) {
                this.paintLecture.color = -1
            } else {
                this.paintLecture.color = ViewCompat.MEASURED_STATE_MASK
            }
            this.color_line_bg = ColorUtils.darkenColor(this.paintLecture.color, 0.7f)
        } else {
            this.color_line_bg = ColorUtils.darkenColor(i, 0.4f)
            this.paintIpad.alpha = 190
            if (ColorUtils.isColorDark(this.paintIpad.color)) {
                this.paintLecture.color = -1
            } else {
                this.paintLecture.color = ViewCompat.MEASURED_STATE_MASK
            }
        }
        this.paintText.color = this.paintLecture.color
    }

    // ═══════════════════════════════════════════════════════════════════
    //  setColorIpad(Gradient)
    // ═══════════════════════════════════════════════════════════════════

    fun setColorIpad(gradient: Gradient) {
        setColor_gradient(gradient)
        val color = gradient.color
        if (this.mIpadType == IpadType.IPAD_CLASSIC.ordinal) {
            this.paintIpad.shader = null
            this.linearGradient_classic = CreateGradient.createLinearGradientWithAngle(
                this.ipad_rect!!,
                gradient.angle.toFloat(),
                intArrayOf(
                    ColorUtils.lightenColor(gradient.color, 0.4f),
                    ColorUtils.lightenColor(gradient.second, 0.4f),
                    ColorUtils.lightenColor(gradient.three, 0.4f)
                ),
                floatArrayOf(0.0f, 0.7f, 1.0f)
            )
            this.paintIpad.color = ColorUtils.darkenColor(gradient.second, 0.2f)
        } else {
            val createLinearGradientWithAngle = CreateGradient.createLinearGradientWithAngle(
                this.ipad_rect!!,
                gradient.angle.toFloat(),
                intArrayOf(gradient.color, gradient.second, gradient.three),
                floatArrayOf(0.0f, 0.7f, 1.0f)
            )
            this.linearGradient_classic = createLinearGradientWithAngle
            this.paintIpad.shader = createLinearGradientWithAngle
            this.paintIpad.color = color
        }
        this.color_line_bg = ColorUtils.darkenColor(color, 0.4f)
        if (this.mIpadType == IpadType.BORDER.ordinal) {
            this.paintLecture.color = color
        } else if (this.mIpadType == IpadType.BLUE_TYPE.ordinal) {
            this.paintLecture.color = ColorUtils.lightenColor(color, 0.7f)
        } else if (this.mIpadType == IpadType.CASSET.ordinal ||
            this.mIpadType == IpadType.CASSET_IMG.ordinal ||
            this.mIpadType == IpadType.CASSET_IMG_BLUR.ordinal
        ) {
            val generateScheme = ColorSchemeGenerator.generateScheme(color, gradient.angle.toFloat())
            this.scheme = generateScheme
            if (ColorUtils.isColorDark(generateScheme.label)) {
                this.paintLecture.color = -1
            } else {
                this.paintLecture.color = ViewCompat.MEASURED_STATE_MASK
            }
        } else {
            this.paintIpad.alpha = 190
            if (ColorUtils.isColorDark(this.paintIpad.color)) {
                this.paintLecture.color = -1
            } else {
                this.paintLecture.color = ViewCompat.MEASURED_STATE_MASK
            }
        }
        this.paintText.color = this.paintLecture.color
    }

    // ═══════════════════════════════════════════════════════════════════
    //  setIcon
    // ═══════════════════════════════════════════════════════════════════

    fun setIcon(str: String, vectorDrawable: VectorDrawable) {
        for (quranEntity in this.quranEntities) {
            if (quranEntity.getIcon() != null && quranEntity.getIcon() != str && quranEntity.getNumber() != -1) {
                quranEntity.setVectorDrawable(vectorDrawable)
                quranEntity.setIcon(str)
                quranEntity.updateIconDraw()
            }
        }
        updateSizeAya()
        invalidate()
    }

    // ═══════════════════════════════════════════════════════════════════
    //  setTypeface
    // ═══════════════════════════════════════════════════════════════════

    fun setTypeface(typeface: Typeface, str: String) {
        val entityView = this.entity_select
        if (entityView is QuranEntity) {
            for (quranEntity in this.quranEntities) {
                if (quranEntity.getNameFont() != null && quranEntity.getNameFont() != str) {
                    quranEntity.setTypeface(typeface, str)
                }
            }
            updateSizeAyaResize()
        } else if (entityView is TranslationQuranEntity) {
            for (translationQuranEntity in this.translationEntities) {
                if (translationQuranEntity.getNameFont() != null && translationQuranEntity.getNameFont() != str) {
                    translationQuranEntity.setTypeface(typeface, str)
                }
            }
            updateSizeTrslAyaResize()
        }
        invalidate()
    }

    // ═══════════════════════════════════════════════════════════════════
    //  setPreset
    // ═══════════════════════════════════════════════════════════════════

    fun setPreset(ayaTextPreset: AyaTextPreset) {
        for (quranEntity in this.quranEntities) {
            quranEntity.setPreset(ayaTextPreset)
        }
        if (this.mIsti3adhaEntity != null && this.mIsti3adhaEntity!!.getBismilahTimeline()!!.visible()) {
            this.mIsti3adhaEntity!!.setPreset(ayaTextPreset)
        }
        if (this.bismilahEntity != null && this.bismilahEntity!!.getBismilahTimeline()!!.visible()) {
            this.bismilahEntity!!.setPreset(ayaTextPreset)
        }
        invalidate()
    }

    // ═══════════════════════════════════════════════════════════════════
    //  setTrslPreset
    // ═══════════════════════════════════════════════════════════════════

    fun setTrslPreset(ayaTextPreset: AyaTextPreset) {
        for (t in this.translationEntities) {
            t.setPreset(ayaTextPreset)
        }
        invalidate()
    }

    // ═══════════════════════════════════════════════════════════════════
    //  setColorAya
    // ═══════════════════════════════════════════════════════════════════

    fun setColorAya(i: Int) {
        setClr_aya(i)
        for (q in this.quranEntities) {
            q.setColor(i)
        }
        if (this.mIsti3adhaEntity != null && this.mIsti3adhaEntity!!.getBismilahTimeline()!!.visible()) {
            this.mIsti3adhaEntity!!.setColor(i)
        }
        if (this.bismilahEntity != null && this.bismilahEntity!!.getBismilahTimeline()!!.visible()) {
            this.bismilahEntity!!.setColor(i)
        }
        invalidate()
    }

    // ═══════════════════════════════════════════════════════════════════
    //  setColorTrsl
    // ═══════════════════════════════════════════════════════════════════

    fun setColorTrsl(i: Int) {
        setClr_trsl(i)
        for (t in this.translationEntities) {
            t.setColor(i)
        }
        invalidate()
    }

    // ═══════════════════════════════════════════════════════════════════
    //  applyAll
    // ═══════════════════════════════════════════════════════════════════

    fun applyAll(f: Float, rectF: RectF, i: Int, i2: Int) {
        val entityView = this.entity_select ?: return
        if (entityView is QuranEntity) {
            val quranEntity = entityView as QuranEntity
            for (quranEntity2 in this.quranEntities) {
                if (quranEntity2 !== quranEntity) {
                    quranEntity2.applyAll(
                        getmCanvas_width(), rectF,
                        quranEntity.getPaintAya().textSize,
                        quranEntity.factorSize
                    )
                }
            }
            invalidate()
            return
        }
        if (entityView is TranslationQuranEntity) {
            val translationQuranEntity = entityView as TranslationQuranEntity
            for (translationQuranEntity2 in this.translationEntities) {
                if (translationQuranEntity2 !== translationQuranEntity) {
                    translationQuranEntity2.applyAll(
                        getmCanvas_width(), rectF,
                        translationQuranEntity.getPaintAya().textSize,
                        translationQuranEntity.factorSize
                    )
                }
            }
            invalidate()
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  setCurrentTime
    // ═══════════════════════════════════════════════════════════════════

    fun setCurrentTime(str: String, str2: String) {
        this.currentTime = str
        this.remainingTime = "-$str2"
    }

    // ═══════════════════════════════════════════════════════════════════
    //  init
    // ═══════════════════════════════════════════════════════════════════

    private fun init() {
        setOnTouchListener(this)
        this.moveGestureDetector = MoveGestureDetector(getContext(), MoveListener())
        this.scaleGestureDetector = ScaleGestureDetector(getContext(), ScaleListener())
        this.gestureDetector = GestureDetectorCompat(getContext(), this.gestureListener)
        this.grayscalePaint = Paint()
        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(0.0f)
        this.grayscalePaint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        val pw = Paint(Paint.ANTI_ALIAS_FLAG)
        this.paintWattermark = pw
        pw.color = ViewCompat.MEASURED_STATE_MASK
        this.paintWattermark.alpha = 25
        this.paintWattermark.typeface = UtilsFileLast.loadFontFromAsset(getContext(), "fonts/ReadexPro_Medium.ttf")
        this.paintWattermark.isFakeBoldText = true
        val lp = Paint()
        this.linePaint = lp
        lp.isAntiAlias = true
        this.paintLecture = Paint(Paint.ANTI_ALIAS_FLAG)
        this.paintIpad = Paint(Paint.ANTI_ALIAS_FLAG)
        this.paintText = TextPaint(Paint.ANTI_ALIAS_FLAG)
        val cp = Paint()
        this.paintClear = cp
        cp.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        this.paintText.typeface = UtilsFileLast.loadFontFromAsset(getContext(), "fonts/arabic/NotoNaskhArabic.ttf")
    }

    // ═══════════════════════════════════════════════════════════════════
    //  setBitmap / updateBitmap (with int color)
    // ═══════════════════════════════════════════════════════════════════

    fun setBitmap(bitmap: Bitmap?, bitmap2: Bitmap?, i: Int, i2: Int, i3: Int, rect: Rect?) {
        this.bitmapBlured = bitmap
        if (bitmap2 != null) {
            this.bitmapSquare = bitmap2
        }
        this.rectSquare = rect
        this.mIpadType = i2
        if (i != -1) {
            setColorIpad(i)
        } else if (bitmap2 != null) {
            setColorIpad(ColorUtils.getAverageColor(bitmap2))
        }
        this.mResizetype = i3
        if (this.mIpadType == IpadType.BOTTOM_RECT.ordinal) {
            this.paintText.textSize = min(this.ipad_rect!!.width(), this.ipad_rect!!.height()) * 0.07f
        } else if (this.mIpadType == IpadType.BORDER.ordinal) {
            this.paintText.textSize = min(this.ipad_rect!!.width(), this.ipad_rect!!.height()) * 0.027f
        } else {
            this.paintText.textSize = this.ipad_rect!!.width() * 0.0388f
        }
        createRect()
    }

    fun updateBitmap(bitmap: Bitmap?, bitmap2: Bitmap?, i: Int, i2: Int, i3: Int, rect: Rect?) {
        this.bitmapBlured = bitmap
        if (bitmap2 != null) {
            this.bitmapSquare = bitmap2
        }
        this.rectSquare = rect
        this.mIpadType = i2
        if (i != -1) {
            setColorIpad(i)
        } else if (bitmap2 != null) {
            setColorIpad(ColorUtils.getAverageColor(bitmap2))
        }
        this.mResizetype = i3
        if (this.mIpadType == IpadType.BOTTOM_RECT.ordinal) {
            this.paintText.textSize = min(this.ipad_rect!!.width(), this.ipad_rect!!.height()) * 0.07f
        } else if (this.mIpadType == IpadType.BORDER.ordinal) {
            this.paintText.textSize = min(this.ipad_rect!!.width(), this.ipad_rect!!.height()) * 0.027f
        } else {
            this.paintText.textSize = this.ipad_rect!!.width() * 0.0388f
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  setBitmap / updateBitmap (with Gradient)
    // ═══════════════════════════════════════════════════════════════════

    fun setBitmap(bitmap: Bitmap?, bitmap2: Bitmap?, gradient: Gradient, i: Int, i2: Int, rect: Rect?) {
        this.bitmapBlured = bitmap
        if (bitmap2 != null) {
            this.bitmapSquare = bitmap2
        }
        this.rectSquare = rect
        this.mIpadType = i
        setColorIpad(gradient)
        this.mResizetype = i2
        if (this.mIpadType == IpadType.BOTTOM_RECT.ordinal) {
            this.paintText.textSize = min(this.ipad_rect!!.width(), this.ipad_rect!!.height()) * 0.07f
        } else if (this.mIpadType == IpadType.BORDER.ordinal) {
            this.paintText.textSize = min(this.ipad_rect!!.width(), this.ipad_rect!!.height()) * 0.027f
        } else {
            this.paintText.textSize = this.ipad_rect!!.width() * 0.0388f
        }
        createRect()
    }

    fun updateBitmap(bitmap: Bitmap?, bitmap2: Bitmap?, gradient: Gradient, i: Int, i2: Int, rect: Rect?) {
        this.bitmapBlured = bitmap
        if (bitmap2 != null) {
            this.bitmapSquare = bitmap2
        }
        this.rectSquare = rect
        this.mIpadType = i
        setColorIpad(gradient)
        this.mResizetype = i2
        if (this.mIpadType == IpadType.BOTTOM_RECT.ordinal) {
            this.paintText.textSize = min(this.ipad_rect!!.width(), this.ipad_rect!!.height()) * 0.07f
        } else if (this.mIpadType == IpadType.BORDER.ordinal) {
            this.paintText.textSize = min(this.ipad_rect!!.width(), this.ipad_rect!!.height()) * 0.027f
        } else {
            this.paintText.textSize = this.ipad_rect!!.width() * 0.0388f
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  updateIpad
    // ═══════════════════════════════════════════════════════════════════

    fun updateIpad(bitmap: Bitmap?, i: Int, i2: Int) {
        this.bitmapBlured = bitmap
        this.mIpadType = i
        this.mResizetype = i2
        this.bitmapSquare = null
        createRectWithoutSurahName()
        invalidate()
    }

    fun updateIpad() {
        createRectWithoutSurahName()
        invalidate()
    }

    // ═══════════════════════════════════════════════════════════════════
    //  createRect — sets up all RectF regions based on mIpadType
    // ═══════════════════════════════════════════════════════════════════

    fun createRect() {
        val w = mCanvas_width.toFloat()
        val h = mCanvas_height.toFloat()
        when (mIpadType) {
            IpadType.IPAD.ordinal,
            IpadType.IPAD_UNBLUR.ordinal -> {
                val margin = w * 0.05f
                val ipadH = h * 0.55f
                val ipadTop = (h - ipadH) / 2.0f
                ipad_rect = RectF(margin, ipadTop, w - margin, ipadTop + ipadH)
                rectFAya = RectF(
                    ipad_rect!!.left + w * 0.04f,
                    ipad_rect!!.top + ipadH * 0.15f,
                    ipad_rect!!.right - w * 0.04f,
                    ipad_rect!!.bottom - ipadH * 0.25f
                )
                rectFProgress = RectF(
                    ipad_rect!!.left + w * 0.08f,
                    ipad_rect!!.bottom - ipadH * 0.15f,
                    ipad_rect!!.right - w * 0.08f,
                    ipad_rect!!.bottom - ipadH * 0.08f
                )
                rectFLecture = RectF(
                    ipad_rect!!.left, ipad_rect!!.top,
                    ipad_rect!!.right, ipad_rect!!.bottom
                )
                radius_cursur = rectFProgress!!.height() * 0.5f
                txt_y = rectFProgress!!.centerY() + paintText.textSize * 0.35f
                newLeft_txt = rectFProgress!!.right
            }
            IpadType.IPAD_CLASSIC.ordinal -> {
                val margin = w * 0.04f
                val ipadH = h * 0.6f
                val ipadTop = (h - ipadH) / 2.0f
                ipad_rect = RectF(margin, ipadTop, w - margin, ipadTop + ipadH)
                rectFAya = RectF(
                    ipad_rect!!.left + w * 0.06f,
                    ipad_rect!!.top + ipadH * 0.12f,
                    ipad_rect!!.right - w * 0.06f,
                    ipad_rect!!.bottom - ipadH * 0.22f
                )
                rectFProgress = RectF(
                    ipad_rect!!.left + w * 0.06f,
                    ipad_rect!!.bottom - ipadH * 0.15f,
                    ipad_rect!!.right - w * 0.06f,
                    ipad_rect!!.bottom - ipadH * 0.08f
                )
                rectFLecture = RectF(ipad_rect!!.left, ipad_rect!!.top, ipad_rect!!.right, ipad_rect!!.bottom)
                radius_cursur = rectFProgress!!.height() * 0.5f
                txt_y = rectFProgress!!.centerY() + paintText.textSize * 0.35f
                newLeft_txt = rectFProgress!!.right
            }
            IpadType.ROUND_RECT.ordinal,
            IpadType.RECT.ordinal -> {
                val margin = w * 0.03f
                val rectH = h * 0.65f
                val rectTop = (h - rectH) / 2.0f
                ipad_rect = RectF(margin, rectTop, w - margin, rectTop + rectH)
                rectFAya = RectF(
                    ipad_rect!!.left + w * 0.05f,
                    ipad_rect!!.top + rectH * 0.08f,
                    ipad_rect!!.right - w * 0.05f,
                    ipad_rect!!.bottom - rectH * 0.18f
                )
                rectFProgress = RectF(
                    ipad_rect!!.left + w * 0.08f,
                    ipad_rect!!.bottom - rectH * 0.12f,
                    ipad_rect!!.right - w * 0.08f,
                    ipad_rect!!.bottom - rectH * 0.06f
                )
                rectFLecture = RectF(ipad_rect!!.left, ipad_rect!!.top, ipad_rect!!.right, ipad_rect!!.bottom)
                radius_cursur = rectFProgress!!.height() * 0.5f
                txt_y = rectFProgress!!.centerY() + paintText.textSize * 0.35f
                newLeft_txt = rectFProgress!!.right
            }
            IpadType.BOTTOM_RECT.ordinal -> {
                val rectH = h * 0.22f
                ipad_rect = RectF(0f, h - rectH, w, h)
                rectFAya = RectF(
                    w * 0.04f, ipad_rect!!.top + rectH * 0.1f,
                    w * 0.96f, ipad_rect!!.bottom - rectH * 0.1f
                )
                rectFProgress = RectF(
                    w * 0.06f, ipad_rect!!.top + rectH * 0.03f,
                    w - w * 0.06f, ipad_rect!!.top + rectH * 0.08f
                )
                rectFLecture = RectF(ipad_rect!!.left, ipad_rect!!.top, ipad_rect!!.right, ipad_rect!!.bottom)
                radius_cursur = rectFProgress!!.height() * 0.5f
                txt_y = rectFProgress!!.centerY() + paintText.textSize * 0.35f
                newLeft_txt = rectFProgress!!.right
            }
            IpadType.BORDER.ordinal -> {
                val margin = w * 0.04f
                val rectH = h * 0.6f
                val rectTop = (h - rectH) / 2.0f
                ipad_rect = RectF(margin, rectTop, w - margin, rectTop + rectH)
                rectFAya = RectF(
                    ipad_rect!!.left + w * 0.06f,
                    ipad_rect!!.top + rectH * 0.12f,
                    ipad_rect!!.right - w * 0.06f,
                    ipad_rect!!.bottom - rectH * 0.22f
                )
                rectFProgress = RectF(
                    ipad_rect!!.left + w * 0.06f,
                    ipad_rect!!.bottom - rectH * 0.15f,
                    ipad_rect!!.right - w * 0.06f,
                    ipad_rect!!.bottom - rectH * 0.08f
                )
                rectFLecture = RectF(ipad_rect!!.left, ipad_rect!!.top, ipad_rect!!.right, ipad_rect!!.bottom)
                radius_cursur = rectFProgress!!.height() * 0.5f
                txt_y = rectFProgress!!.centerY() + paintText.textSize * 0.35f
                newLeft_txt = rectFProgress!!.right
            }
            IpadType.BLACK_LAYER.ordinal,
            IpadType.GRADIENT.ordinal -> {
                ipad_rect = RectF(0f, 0f, w, h)
                val ayaMargin = w * 0.06f
                val ayaH = h * 0.55f
                val ayaTop = (h - ayaH) / 2.0f
                rectFAya = RectF(ayaMargin, ayaTop, w - ayaMargin, ayaTop + ayaH)
                rectFProgress = RectF(
                    w * 0.1f, h * 0.88f,
                    w - w * 0.1f, h * 0.92f
                )
                rectFLecture = RectF(0f, 0f, w, h)
                radius_cursur = rectFProgress!!.height() * 0.5f
                txt_y = rectFProgress!!.centerY() + paintText.textSize * 0.35f
                newLeft_txt = rectFProgress!!.right
            }
            IpadType.MASK_BRUSH.ordinal -> {
                ipad_rect = RectF(0f, 0f, w, h)
                val ayaMargin = w * 0.06f
                val ayaH = h * 0.5f
                val ayaTop = (h - ayaH) / 2.0f
                rectFAya = RectF(ayaMargin, ayaTop, w - ayaMargin, ayaTop + ayaH)
                rectFProgress = RectF(
                    w * 0.1f, h * 0.88f,
                    w - w * 0.1f, h * 0.92f
                )
                rectFLecture = RectF(0f, 0f, w, h)
                radius_cursur = rectFProgress!!.height() * 0.5f
                txt_y = rectFProgress!!.centerY() + paintText.textSize * 0.35f
                newLeft_txt = rectFProgress!!.right
            }
            IpadType.BLUE_TYPE.ordinal -> {
                ipad_rect = RectF(0f, 0f, w, h)
                val ayaMargin = w * 0.06f
                val ayaH = h * 0.5f
                val ayaTop = (h - ayaH) / 2.0f
                rectFAya = RectF(ayaMargin, ayaTop, w - ayaMargin, ayaTop + ayaH)
                rectFProgress = RectF(
                    w * 0.1f, h * 0.88f,
                    w - w * 0.1f, h * 0.92f
                )
                rectFLecture = RectF(0f, 0f, w, h)
                radius_cursur = rectFProgress!!.height() * 0.5f
                txt_y = rectFProgress!!.centerY() + paintText.textSize * 0.35f
                newLeft_txt = rectFProgress!!.right
            }
            IpadType.IPAD_NEOMORPHIC.ordinal -> {
                val margin = w * 0.07f
                val ipadH = h * 0.55f
                val ipadTop = (h - ipadH) / 2.0f
                ipad_rect = RectF(margin, ipadTop, w - margin, ipadTop + ipadH)
                rectFAya = RectF(
                    ipad_rect!!.left + w * 0.04f,
                    ipad_rect!!.top + ipadH * 0.2f,
                    ipad_rect!!.right - w * 0.04f,
                    ipad_rect!!.bottom - ipadH * 0.25f
                )
                rectFProgress = RectF(
                    ipad_rect!!.left + w * 0.1f,
                    ipad_rect!!.bottom - ipadH * 0.15f,
                    ipad_rect!!.right - w * 0.1f,
                    ipad_rect!!.bottom - ipadH * 0.08f
                )
                rectFLecture = RectF(ipad_rect!!.left, ipad_rect!!.top, ipad_rect!!.right, ipad_rect!!.bottom)
                radius_cursur = rectFProgress!!.height() * 0.5f
                txt_y = rectFProgress!!.centerY() + paintText.textSize * 0.35f
                newLeft_txt = rectFProgress!!.right
            }
            IpadType.HEART.ordinal -> {
                ipad_rect = RectF(0f, 0f, w, h)
                val ayaMargin = w * 0.12f
                val ayaH = h * 0.35f
                val ayaTop = h * 0.35f
                rectFAya = RectF(ayaMargin, ayaTop, w - ayaMargin, ayaTop + ayaH)
                rectFProgress = RectF(
                    w * 0.15f, h * 0.82f,
                    w - w * 0.15f, h * 0.86f
                )
                rectFLecture = RectF(0f, 0f, w, h)
                radius_cursur = rectFProgress!!.height() * 0.5f
                txt_y = rectFProgress!!.centerY() + paintText.textSize * 0.35f
                newLeft_txt = rectFProgress!!.right
            }
            IpadType.BATTERY.ordinal -> {
                ipad_rect = RectF(0f, 0f, w, h)
                val ayaMargin = w * 0.1f
                val ayaH = h * 0.38f
                val ayaTop = h * 0.3f
                rectFAya = RectF(ayaMargin, ayaTop, w - ayaMargin, ayaTop + ayaH)
                rectFProgress = RectF(
                    w * 0.12f, h * 0.84f,
                    w - w * 0.12f, h * 0.88f
                )
                rectFLecture = RectF(0f, 0f, w, h)
                radius_cursur = rectFProgress!!.height() * 0.5f
                txt_y = rectFProgress!!.centerY() + paintText.textSize * 0.35f
                newLeft_txt = rectFProgress!!.right
            }
            IpadType.CASSET.ordinal,
            IpadType.CASSET_IMG.ordinal,
            IpadType.CASSET_IMG_BLUR.ordinal -> {
                val margin = w * 0.04f
                val cassetteH = h * 0.6f
                val cassetteTop = (h - cassetteH) / 2.0f
                ipad_rect = RectF(margin, cassetteTop, w - margin, cassetteTop + cassetteH)
                rectFAya = RectF(
                    ipad_rect!!.left + w * 0.08f,
                    ipad_rect!!.top + cassetteH * 0.25f,
                    ipad_rect!!.right - w * 0.08f,
                    ipad_rect!!.bottom - cassetteH * 0.35f
                )
                rectFProgress = RectF(
                    ipad_rect!!.left + w * 0.12f,
                    ipad_rect!!.top + cassetteH * 0.12f,
                    ipad_rect!!.right - w * 0.12f,
                    ipad_rect!!.top + cassetteH * 0.2f
                )
                rectFLecture = RectF(ipad_rect!!.left, ipad_rect!!.top, ipad_rect!!.right, ipad_rect!!.bottom)
                radius_cursur = rectFProgress!!.height() * 0.5f
                txt_y = rectFProgress!!.centerY() + paintText.textSize * 0.35f
                newLeft_txt = rectFProgress!!.right
            }
        }
        rectFSurahName = if (rectFAya != null) {
            RectF(
                rectFAya!!.left, rectFAya!!.top - h * 0.06f,
                rectFAya!!.right, rectFAya!!.top
            )
        } else {
            RectF(0f, 0f, w * 0.5f, h * 0.05f)
        }
        surahNameEntity?.setCopyRect()
        invalidate()
    }

    // ═══════════════════════════════════════════════════════════════════
    //  createRectWithoutSurahName
    // ═══════════════════════════════════════════════════════════════════

    fun createRectWithoutSurahName() {
        val w = mCanvas_width.toFloat()
        val h = mCanvas_height.toFloat()
        when (mIpadType) {
            IpadType.IPAD.ordinal,
            IpadType.IPAD_UNBLUR.ordinal -> {
                val margin = w * 0.05f
                val ipadH = h * 0.55f
                val ipadTop = (h - ipadH) / 2.0f
                ipad_rect = RectF(margin, ipadTop, w - margin, ipadTop + ipadH)
                rectFAya = RectF(
                    ipad_rect!!.left + w * 0.04f,
                    ipad_rect!!.top + ipadH * 0.15f,
                    ipad_rect!!.right - w * 0.04f,
                    ipad_rect!!.bottom - ipadH * 0.25f
                )
                rectFProgress = RectF(
                    ipad_rect!!.left + w * 0.08f,
                    ipad_rect!!.bottom - ipadH * 0.15f,
                    ipad_rect!!.right - w * 0.08f,
                    ipad_rect!!.bottom - ipadH * 0.08f
                )
                rectFLecture = RectF(ipad_rect!!.left, ipad_rect!!.top, ipad_rect!!.right, ipad_rect!!.bottom)
                radius_cursur = rectFProgress!!.height() * 0.5f
                txt_y = rectFProgress!!.centerY() + paintText.textSize * 0.35f
                newLeft_txt = rectFProgress!!.right
            }
            IpadType.IPAD_CLASSIC.ordinal -> {
                val margin = w * 0.04f
                val ipadH = h * 0.6f
                val ipadTop = (h - ipadH) / 2.0f
                ipad_rect = RectF(margin, ipadTop, w - margin, ipadTop + ipadH)
                rectFAya = RectF(
                    ipad_rect!!.left + w * 0.06f,
                    ipad_rect!!.top + ipadH * 0.12f,
                    ipad_rect!!.right - w * 0.06f,
                    ipad_rect!!.bottom - ipadH * 0.22f
                )
                rectFProgress = RectF(
                    ipad_rect!!.left + w * 0.06f,
                    ipad_rect!!.bottom - ipadH * 0.15f,
                    ipad_rect!!.right - w * 0.06f,
                    ipad_rect!!.bottom - ipadH * 0.08f
                )
                rectFLecture = RectF(ipad_rect!!.left, ipad_rect!!.top, ipad_rect!!.right, ipad_rect!!.bottom)
                radius_cursur = rectFProgress!!.height() * 0.5f
                txt_y = rectFProgress!!.centerY() + paintText.textSize * 0.35f
                newLeft_txt = rectFProgress!!.right
            }
            IpadType.ROUND_RECT.ordinal,
            IpadType.RECT.ordinal -> {
                val margin = w * 0.03f
                val rectH = h * 0.65f
                val rectTop = (h - rectH) / 2.0f
                ipad_rect = RectF(margin, rectTop, w - margin, rectTop + rectH)
                rectFAya = RectF(
                    ipad_rect!!.left + w * 0.05f,
                    ipad_rect!!.top + rectH * 0.08f,
                    ipad_rect!!.right - w * 0.05f,
                    ipad_rect!!.bottom - rectH * 0.18f
                )
                rectFProgress = RectF(
                    ipad_rect!!.left + w * 0.08f,
                    ipad_rect!!.bottom - rectH * 0.12f,
                    ipad_rect!!.right - w * 0.08f,
                    ipad_rect!!.bottom - rectH * 0.06f
                )
                rectFLecture = RectF(ipad_rect!!.left, ipad_rect!!.top, ipad_rect!!.right, ipad_rect!!.bottom)
                radius_cursur = rectFProgress!!.height() * 0.5f
                txt_y = rectFProgress!!.centerY() + paintText.textSize * 0.35f
                newLeft_txt = rectFProgress!!.right
            }
            IpadType.BOTTOM_RECT.ordinal -> {
                val rectH = h * 0.22f
                ipad_rect = RectF(0f, h - rectH, w, h)
                rectFAya = RectF(
                    w * 0.04f, ipad_rect!!.top + rectH * 0.1f,
                    w * 0.96f, ipad_rect!!.bottom - rectH * 0.1f
                )
                rectFProgress = RectF(
                    w * 0.06f, ipad_rect!!.top + rectH * 0.03f,
                    w - w * 0.06f, ipad_rect!!.top + rectH * 0.08f
                )
                rectFLecture = RectF(ipad_rect!!.left, ipad_rect!!.top, ipad_rect!!.right, ipad_rect!!.bottom)
                radius_cursur = rectFProgress!!.height() * 0.5f
                txt_y = rectFProgress!!.centerY() + paintText.textSize * 0.35f
                newLeft_txt = rectFProgress!!.right
            }
            IpadType.BORDER.ordinal -> {
                val margin = w * 0.04f
                val rectH = h * 0.6f
                val rectTop = (h - rectH) / 2.0f
                ipad_rect = RectF(margin, rectTop, w - margin, rectTop + rectH)
                rectFAya = RectF(
                    ipad_rect!!.left + w * 0.06f,
                    ipad_rect!!.top + rectH * 0.12f,
                    ipad_rect!!.right - w * 0.06f,
                    ipad_rect!!.bottom - rectH * 0.22f
                )
                rectFProgress = RectF(
                    ipad_rect!!.left + w * 0.06f,
                    ipad_rect!!.bottom - rectH * 0.15f,
                    ipad_rect!!.right - w * 0.06f,
                    ipad_rect!!.bottom - rectH * 0.08f
                )
                rectFLecture = RectF(ipad_rect!!.left, ipad_rect!!.top, ipad_rect!!.right, ipad_rect!!.bottom)
                radius_cursur = rectFProgress!!.height() * 0.5f
                txt_y = rectFProgress!!.centerY() + paintText.textSize * 0.35f
                newLeft_txt = rectFProgress!!.right
            }
            IpadType.BLACK_LAYER.ordinal,
            IpadType.GRADIENT.ordinal -> {
                ipad_rect = RectF(0f, 0f, w, h)
                val ayaMargin = w * 0.06f
                val ayaH = h * 0.55f
                val ayaTop = (h - ayaH) / 2.0f
                rectFAya = RectF(ayaMargin, ayaTop, w - ayaMargin, ayaTop + ayaH)
                rectFProgress = RectF(w * 0.1f, h * 0.88f, w - w * 0.1f, h * 0.92f)
                rectFLecture = RectF(0f, 0f, w, h)
                radius_cursur = rectFProgress!!.height() * 0.5f
                txt_y = rectFProgress!!.centerY() + paintText.textSize * 0.35f
                newLeft_txt = rectFProgress!!.right
            }
            IpadType.MASK_BRUSH.ordinal -> {
                ipad_rect = RectF(0f, 0f, w, h)
                val ayaMargin = w * 0.06f
                val ayaH = h * 0.5f
                val ayaTop = (h - ayaH) / 2.0f
                rectFAya = RectF(ayaMargin, ayaTop, w - ayaMargin, ayaTop + ayaH)
                rectFProgress = RectF(w * 0.1f, h * 0.88f, w - w * 0.1f, h * 0.92f)
                rectFLecture = RectF(0f, 0f, w, h)
                radius_cursur = rectFProgress!!.height() * 0.5f
                txt_y = rectFProgress!!.centerY() + paintText.textSize * 0.35f
                newLeft_txt = rectFProgress!!.right
            }
            IpadType.BLUE_TYPE.ordinal -> {
                ipad_rect = RectF(0f, 0f, w, h)
                val ayaMargin = w * 0.06f
                val ayaH = h * 0.5f
                val ayaTop = (h - ayaH) / 2.0f
                rectFAya = RectF(ayaMargin, ayaTop, w - ayaMargin, ayaTop + ayaH)
                rectFProgress = RectF(w * 0.1f, h * 0.88f, w - w * 0.1f, h * 0.92f)
                rectFLecture = RectF(0f, 0f, w, h)
                radius_cursur = rectFProgress!!.height() * 0.5f
                txt_y = rectFProgress!!.centerY() + paintText.textSize * 0.35f
                newLeft_txt = rectFProgress!!.right
            }
            IpadType.IPAD_NEOMORPHIC.ordinal -> {
                val margin = w * 0.07f
                val ipadH = h * 0.55f
                val ipadTop = (h - ipadH) / 2.0f
                ipad_rect = RectF(margin, ipadTop, w - margin, ipadTop + ipadH)
                rectFAya = RectF(
                    ipad_rect!!.left + w * 0.04f,
                    ipad_rect!!.top + ipadH * 0.2f,
                    ipad_rect!!.right - w * 0.04f,
                    ipad_rect!!.bottom - ipadH * 0.25f
                )
                rectFProgress = RectF(
                    ipad_rect!!.left + w * 0.1f,
                    ipad_rect!!.bottom - ipadH * 0.15f,
                    ipad_rect!!.right - w * 0.1f,
                    ipad_rect!!.bottom - ipadH * 0.08f
                )
                rectFLecture = RectF(ipad_rect!!.left, ipad_rect!!.top, ipad_rect!!.right, ipad_rect!!.bottom)
                radius_cursur = rectFProgress!!.height() * 0.5f
                txt_y = rectFProgress!!.centerY() + paintText.textSize * 0.35f
                newLeft_txt = rectFProgress!!.right
            }
            IpadType.HEART.ordinal -> {
                ipad_rect = RectF(0f, 0f, w, h)
                val ayaMargin = w * 0.12f
                val ayaH = h * 0.35f
                val ayaTop = h * 0.35f
                rectFAya = RectF(ayaMargin, ayaTop, w - ayaMargin, ayaTop + ayaH)
                rectFProgress = RectF(w * 0.15f, h * 0.82f, w - w * 0.15f, h * 0.86f)
                rectFLecture = RectF(0f, 0f, w, h)
                radius_cursur = rectFProgress!!.height() * 0.5f
                txt_y = rectFProgress!!.centerY() + paintText.textSize * 0.35f
                newLeft_txt = rectFProgress!!.right
            }
            IpadType.BATTERY.ordinal -> {
                ipad_rect = RectF(0f, 0f, w, h)
                val ayaMargin = w * 0.1f
                val ayaH = h * 0.38f
                val ayaTop = h * 0.3f
                rectFAya = RectF(ayaMargin, ayaTop, w - ayaMargin, ayaTop + ayaH)
                rectFProgress = RectF(w * 0.12f, h * 0.84f, w - w * 0.12f, h * 0.88f)
                rectFLecture = RectF(0f, 0f, w, h)
                radius_cursur = rectFProgress!!.height() * 0.5f
                txt_y = rectFProgress!!.centerY() + paintText.textSize * 0.35f
                newLeft_txt = rectFProgress!!.right
            }
            IpadType.CASSET.ordinal,
            IpadType.CASSET_IMG.ordinal,
            IpadType.CASSET_IMG_BLUR.ordinal -> {
                val margin = w * 0.04f
                val cassetteH = h * 0.6f
                val cassetteTop = (h - cassetteH) / 2.0f
                ipad_rect = RectF(margin, cassetteTop, w - margin, cassetteTop + cassetteH)
                rectFAya = RectF(
                    ipad_rect!!.left + w * 0.08f,
                    ipad_rect!!.top + cassetteH * 0.25f,
                    ipad_rect!!.right - w * 0.08f,
                    ipad_rect!!.bottom - cassetteH * 0.35f
                )
                rectFProgress = RectF(
                    ipad_rect!!.left + w * 0.12f,
                    ipad_rect!!.top + cassetteH * 0.12f,
                    ipad_rect!!.right - w * 0.12f,
                    ipad_rect!!.top + cassetteH * 0.2f
                )
                rectFLecture = RectF(ipad_rect!!.left, ipad_rect!!.top, ipad_rect!!.right, ipad_rect!!.bottom)
                radius_cursur = rectFProgress!!.height() * 0.5f
                txt_y = rectFProgress!!.centerY() + paintText.textSize * 0.35f
                newLeft_txt = rectFProgress!!.right
            }
        }
        invalidate()
    }

    // ═══════════════════════════════════════════════════════════════════
    //  onDraw (reconstructed from smali)
    // ═══════════════════════════════════════════════════════════════════

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        try {
            if (isNotDraw) {
                if (isPlaying && iViewCallback != null) {
                    iViewCallback!!.onDrawFinish()
                }
                return
            }
            canvas.save()
            canvas.translate(mDrawingTranslationX, mDrawingTranslationY)
            canvas.clipRect(0, 0, mCanvas_width, mCanvas_height)
            canvas.drawColor(Color.BLACK)
            if (bitmapBlured != null && !bitmapBlured!!.isRecycled) {
                when (mIpadType) {
                    IpadType.GRADIENT.ordinal, IpadType.MASK_BRUSH.ordinal,
                    IpadType.BLACK_LAYER.ordinal, IpadType.CASSET_IMG.ordinal -> {
                        if (!isVideo && bitmapNotBlur != null && !bitmapNotBlur!!.isRecycled) {
                            canvas.drawBitmap(bitmapNotBlur!!, btmX, btmY, paint)
                        }
                    }
                    IpadType.BLUE_TYPE.ordinal -> {
                        if (!isVideo && bitmapNotBlur != null && !bitmapNotBlur!!.isRecycled) {
                            canvas.drawBitmap(bitmapNotBlur!!, btmX, btmY, grayscalePaint)
                        }
                    }
                    IpadType.CASSET_IMG_BLUR.ordinal -> {
                        if (!isVideo) {
                            canvas.drawBitmap(bitmapBlured!!, btmX, btmY, paint)
                        }
                    }
                    IpadType.IPAD_CLASSIC.ordinal -> {
                        if (color_gradient != null) {
                            paint.shader = linearGradient_classic
                            canvas.drawPaint(paint)
                            paint.shader = null
                        } else {
                            canvas.drawColor(color_bg_type_classic)
                        }
                    }
                    IpadType.IPAD_NEOMORPHIC.ordinal, IpadType.HEART.ordinal,
                    IpadType.BATTERY.ordinal, IpadType.CASSET.ordinal -> {
                        // No background bitmap draw needed for these types
                    }
                    IpadType.IPAD_UNBLUR.ordinal -> {
                        canvas.drawBitmap(bitmapNotBlur!!, btmX, btmY, paint)
                    }
                    else -> {
                        canvas.drawBitmap(bitmapBlured!!, btmX, btmY, paint)
                    }
                }
                if (bitmapSquare != null) {
                    drawIpad(canvas, true)
                } else {
                    drawProgress(canvas)
                }
            }
            drawLineHelper(canvas)
            drawBismilah(canvas)
            drawEntity(canvas)
            drawNameSurah(canvas)
            if (entity_select != null && selectTool != null && entity_select!!.isVisible) {
                val ev = entity_select!!
                if (ev is SurahNameEntity || ev is BismilahEntity ||
                    (ev.entityQuran != null && ev.entityQuran!!.visible()) ||
                    (ev.entityTrslTimeline != null && ev.entityTrslTimeline!!.visible())
                ) {
                    selectTool!!.draw(canvas, ev)
                }
            }
            if (!isPro && !isRemoveWattermark) {
                drawWattermark(canvas, false)
            }
            canvas.restore()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (isPlaying && iViewCallback != null) {
                iViewCallback!!.onDrawFinish()
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  drawWattermark
    // ═══════════════════════════════════════════════════════════════════

    fun drawWattermark(canvas: Canvas, isVideoFrame: Boolean) {
        val text = "QuranMaker"
        val w = mCanvas_width.toFloat()
        val h = mCanvas_height.toFloat()
        paintWattermark.textSize = w * 0.03f
        val textWidth = paintWattermark.measureText(text)
        val x = w - textWidth - w * 0.04f
        val y = h - w * 0.03f
        if (isVideoFrame) {
            canvas.drawText(text, x, y, paintWattermark)
        } else {
            if (mRectWattermark == null) {
                mRectWattermark = RectF(x, y - paintWattermark.textSize, x + textWidth, y)
            }
            canvas.drawText(text, x, y, paintWattermark)
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  calculateTextSize
    // ═══════════════════════════════════════════════════════════════════

    fun calculateTextSize(text: String, paint: Paint, maxWidth: Int, maxHeight: Int): Float {
        if (text.isEmpty() || maxWidth <= 0 || maxHeight <= 0) return 0f
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

    // ═══════════════════════════════════════════════════════════════════
    //  drawProgress
    // ═══════════════════════════════════════════════════════════════════

    fun drawProgress(canvas: Canvas) {
        if (rectFProgress == null) return
        val progressWidth = rectFProgress!!.width() * progress
        canvas.drawRect(rectFProgress!!, paintLecture)
        val progressRect = RectF(
            rectFProgress!!.left, rectFProgress!!.top,
            rectFProgress!!.left + progressWidth, rectFProgress!!.bottom
        )
        paintIpad.alpha = 255
        canvas.drawRect(progressRect, paintIpad)
        if (currentTime != null) {
            val timeText = "$currentTime $remainingTime"
            paintText.textAlign = Paint.Align.LEFT
            canvas.drawText(timeText, newLeft_txt, txt_y, paintText)
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  AccelerateDecelerateInterpolator
    // ═══════════════════════════════════════════════════════════════════

    private fun accelerateDecelerateInterpolator(input: Float): Float {
        return (1.0f - cos((input + 1.0f) * Math.PI).toFloat()) / 2.0f
    }

    // ═══════════════════════════════════════════════════════════════════
    //  saveProgressBitmap
    // ═══════════════════════════════════════════════════════════════════

    fun saveProgressBitmap(file: File, cursorSize: Float) {
        if (rectFProgress == null) return
        val progressBitmap = Bitmap.createBitmap(
            rectFProgress!!.width().toInt(),
            (rectFProgress!!.height() * 1.5f).toInt(),
            Bitmap.Config.ARGB_8888
        )
        val c = Canvas(progressBitmap)
        c.drawRect(
            0f, 0f, progressBitmap.width.toFloat(), progressBitmap.height.toFloat(),
            paintLecture
        )
        val cursorPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        cursorPaint.color = paintIpad.color
        if (cursorSize > 0f) {
            cursorPaint.strokeWidth = cursorSize
            cursorPaint.strokeCap = Paint.Cap.ROUND
            c.drawPoint(cursorSize / 2f, progressBitmap.height / 2f, cursorPaint)
        }
        saveBitmap(file, "progress.png", progressBitmap)
    }

    // ═══════════════════════════════════════════════════════════════════
    //  saveProgressCassetBitmap
    // ═══════════════════════════════════════════════════════════════════

    fun saveProgressCassetBitmap(file: File) {
        if (rectFProgress == null) return
        val progressBitmap = Bitmap.createBitmap(
            rectFProgress!!.width().toInt(),
            rectFProgress!!.height().toInt(),
            Bitmap.Config.ARGB_8888
        )
        val c = Canvas(progressBitmap)
        c.drawRect(
            0f, 0f, progressBitmap.width.toFloat(), progressBitmap.height.toFloat(),
            paintLecture
        )
        saveBitmap(file, "progress.png", progressBitmap)
    }

    // ═══════════════════════════════════════════════════════════════════
    //  saveProgressBitmapTypeIPAD_NEOMORPHIC
    // ═══════════════════════════════════════════════════════════════════

    fun saveProgressBitmapTypeIPAD_NEOMORPHIC(file: File, bgBitmap: Bitmap) {
        if (rectFProgress == null) return
        val progressBitmap = Bitmap.createBitmap(
            rectFProgress!!.width().toInt(),
            (rectFProgress!!.height() * 1.5f).toInt(),
            Bitmap.Config.ARGB_8888
        )
        val c = Canvas(progressBitmap)
        c.drawRect(
            0f, 0f, progressBitmap.width.toFloat(), progressBitmap.height.toFloat(),
            paintLecture
        )
        saveBitmap(file, "progress.png", progressBitmap)
    }

    // ═══════════════════════════════════════════════════════════════════
    //  saveProgressBitmapTypeBlue
    // ═══════════════════════════════════════════════════════════════════

    fun saveProgressBitmapTypeBlue(file: File) {
        if (rectFProgress == null) return
        val progressBitmap = Bitmap.createBitmap(
            rectFProgress!!.width().toInt(),
            (rectFProgress!!.height() * 1.5f).toInt(),
            Bitmap.Config.ARGB_8888
        )
        val c = Canvas(progressBitmap)
        c.drawRect(
            0f, 0f, progressBitmap.width.toFloat(), progressBitmap.height.toFloat(),
            paintLecture
        )
        saveBitmap(file, "progress.png", progressBitmap)
    }

    // ═══════════════════════════════════════════════════════════════════
    //  saveProgressBitmapTypeHeart
    // ═══════════════════════════════════════════════════════════════════

    fun saveProgressBitmapTypeHeart(file: File, bgBitmap: Bitmap): Pair<Float, Int> {
        if (rectFProgress == null) return Pair(0f, 0)
        val progressBitmap = Bitmap.createBitmap(
            rectFProgress!!.width().toInt(),
            (rectFProgress!!.height() * 1.5f).toInt(),
            Bitmap.Config.ARGB_8888
        )
        val c = Canvas(progressBitmap)
        c.drawRect(
            0f, 0f, progressBitmap.width.toFloat(), progressBitmap.height.toFloat(),
            paintLecture
        )
        saveBitmap(file, "progress.png", progressBitmap)
        return Pair(rectFProgress!!.left, progressBitmap.height)
    }

    // ═══════════════════════════════════════════════════════════════════
    //  saveProgressBitmapTypeBattery
    // ═══════════════════════════════════════════════════════════════════

    fun saveProgressBitmapTypeBattery(file: File, bgBitmap: Bitmap): Pair<Float, Point> {
        if (rectFProgress == null) return Pair(0f, Point(0, 0))
        val progressBitmap = Bitmap.createBitmap(
            rectFProgress!!.width().toInt(),
            (rectFProgress!!.height() * 1.5f).toInt(),
            Bitmap.Config.ARGB_8888
        )
        val c = Canvas(progressBitmap)
        c.drawRect(
            0f, 0f, progressBitmap.width.toFloat(), progressBitmap.height.toFloat(),
            paintLecture
        )
        saveBitmap(file, "progress.png", progressBitmap)
        return Pair(rectFProgress!!.left, Point(progressBitmap.width, progressBitmap.height))
    }

    // ═══════════════════════════════════════════════════════════════════
    //  saveBitmap (uses Kotlin use{} for safe resource cleanup)
    // ═══════════════════════════════════════════════════════════════════

    private fun saveBitmap(file: File, name: String, bitmap: Bitmap) {
        val outFile = File(file, name)
        try {
            FileOutputStream(outFile).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                fos.flush()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  setProgress
    // ═══════════════════════════════════════════════════════════════════

    fun setProgress(p: Float) {
        this.progress = p
        invalidate()
    }

    // ═══════════════════════════════════════════════════════════════════
    //  drawEntity
    // ═══════════════════════════════════════════════════════════════════

    fun drawEntity(canvas: Canvas) {
        for (quranEntity in quranEntities) {
            if (quranEntity.isVisible) {
                quranEntity.draw(canvas)
            }
        }
        for (translationEntity in translationEntities) {
            if (translationEntity.isVisible) {
                translationEntity.draw(canvas)
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Animation helper methods
    // ═══════════════════════════════════════════════════════════════════

    fun slideInToLeft(entityView: EntityView, duration: Int) {
        // Animation: entity slides in from the right to its current position
        val rect = entityView.rect
        val targetLeft = rect.left
        rect.left = rect.right
        entityView.postTranslate(targetLeft - rect.left, 0f)
    }

    fun slideInToRight(entityView: EntityView, duration: Int) {
        val rect = entityView.rect
        val targetRight = rect.right
        rect.right = rect.left
        entityView.postTranslate(targetRight - rect.right, 0f)
    }

    fun slideOutToRight(entityView: EntityView, duration: Int) {
        val rect = entityView.rect
        val shift = mCanvas_width.toFloat() - rect.left
        entityView.postTranslate(shift, 0f)
    }

    fun slideOutToLeft(entityView: EntityView, duration: Int) {
        val rect = entityView.rect
        val shift = -rect.right
        entityView.postTranslate(shift, 0f)
    }

    fun fadeIn(entityView: EntityView, duration: Int) {
        // No-op stub — actual animation handled by ObjectAnimator in entity classes
    }

    fun fadeOut(entityView: EntityView, duration: Int) {
        // No-op stub — actual animation handled by ObjectAnimator in entity classes
    }

    // ═══════════════════════════════════════════════════════════════════
    //  drawEntityBitmap
    // ═══════════════════════════════════════════════════════════════════

    fun drawEntityBitmap(file: File, bgWidth: Int, bgHeight: Int) {
        val entityBitmap = Bitmap.createBitmap(bgWidth, bgHeight, Bitmap.Config.ARGB_8888)
        val c = Canvas(entityBitmap)
        for (quranEntity in quranEntities) {
            if (quranEntity.isVisible) {
                quranEntity.singleDraw(c)
            }
        }
        for (translationEntity in translationEntities) {
            if (translationEntity.isVisible) {
                translationEntity.singleDraw(c)
            }
        }
        bismilahEntity?.let {
            if (it.getBismilahTimeline()?.visible() == true) {
                it.singleDraw(c)
            }
        }
        mIsti3adhaEntity?.let {
            if (it.getBismilahTimeline()?.visible() == true) {
                it.singleDraw(c)
            }
        }
        surahNameEntity?.let {
            if (it.isVisible) {
                it.singleDraw(c)
            }
        }
        saveBitmap(file, "entity.png", entityBitmap)
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Drawing helper methods
    // ═══════════════════════════════════════════════════════════════════

    fun drawRectWithShadow(canvas: Canvas, rect: RectF, radius: Float, paint: Paint, shadowPaint: Paint) {
        canvas.drawRoundRect(rect, radius, radius, shadowPaint)
        canvas.drawRoundRect(rect, radius, radius, paint)
    }

    fun drawRectBottom(canvas: Canvas, rect: RectF, paint: Paint) {
        canvas.drawRect(rect, paint)
    }

    fun drawBitmapWithShadow(canvas: Canvas, bitmap: Bitmap, left: Float, top: Float, shadowPaint: Paint) {
        canvas.drawBitmap(bitmap, left + 2f, top + 2f, shadowPaint)
        canvas.drawBitmap(bitmap, left, top, paint)
    }

    fun drawBitmapWithShadowTypeBottom(canvas: Canvas, bitmap: Bitmap, left: Float, top: Float, shadowPaint: Paint) {
        canvas.drawBitmap(bitmap, left, top + 2f, shadowPaint)
        canvas.drawBitmap(bitmap, left, top, paint)
    }

    fun drawBitmapWithShadowTypeBottomSave(
        canvas: Canvas, bitmap: Bitmap, left: Float, top: Float,
        shadowPaint: Paint, progressPaint: Paint
    ) {
        canvas.drawBitmap(bitmap, left, top + 2f, shadowPaint)
        canvas.drawBitmap(bitmap, left, top, paint)
    }

    // ═══════════════════════════════════════════════════════════════════
    //  drawNeumorphicRect
    // ═══════════════════════════════════════════════════════════════════

    fun drawNeumorphicRect(
        canvas: Canvas, left: Float, top: Float, right: Float, bottom: Float,
        radius: Float, baseColor: Int, offset: Float
    ) {
        val darkColor = ColorUtils.darkenColor(baseColor, 0.15f)
        val lightColor = ColorUtils.lightenColor(baseColor, 0.15f)
        val rect = RectF(left, top, right, bottom)

        // Light shadow (top-left)
        lightShadowPaint.color = lightColor
        val lightRect = RectF(left - offset, top - offset, right - offset, bottom - offset)
        canvas.drawRoundRect(lightRect, radius, radius, lightShadowPaint)

        // Dark shadow (bottom-right)
        darkShadowPaint.color = darkColor
        val darkRect = RectF(left + offset, top + offset, right + offset, bottom + offset)
        canvas.drawRoundRect(darkRect, radius, radius, darkShadowPaint)

        // Base
        val basePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        basePaint.color = baseColor
        canvas.drawRoundRect(rect, radius, radius, basePaint)
    }

    // ═══════════════════════════════════════════════════════════════════
    //  drawCaset / drawCasetNoBg / drawInnerGear
    // ═══════════════════════════════════════════════════════════════════

    fun drawCaset(canvas: Canvas) {
        if (ipad_rect == null) return
        val r = ipad_rect!!
        val w = r.width()
        val h = r.height()

        // Body
        canvas.drawRoundRect(r, w * 0.04f, w * 0.04f, paintIpad)

        // Label strip
        val labelRect = RectF(r.left + w * 0.08f, r.top + h * 0.08f, r.right - w * 0.08f, r.top + h * 0.2f)
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        labelPaint.color = ColorUtils.lightenColor(paintIpad.color, 0.3f)
        canvas.drawRect(labelRect, labelPaint)

        // Reels
        val reelRadius = h * 0.1f
        val leftReelX = r.left + w * 0.28f
        val rightReelX = r.left + w * 0.72f
        val reelY = r.top + h * 0.5f

        val reelPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        reelPaint.color = ViewCompat.MEASURED_STATE_MASK
        canvas.drawCircle(leftReelX, reelY, reelRadius, reelPaint)
        drawInnerGear(canvas, leftReelX, reelY, reelRadius * 0.3f, reelRadius * 0.45f, 8)
        canvas.drawCircle(rightReelX, reelY, reelRadius, reelPaint)
        drawInnerGear(canvas, rightReelX, reelY, reelRadius * 0.3f, reelRadius * 0.45f, 8)
    }

    fun drawCasetNoBg(canvas: Canvas) {
        if (ipad_rect == null) return
        val r = ipad_rect!!
        val w = r.width()
        val h = r.height()

        // Label strip only
        val labelRect = RectF(r.left + w * 0.08f, r.top + h * 0.08f, r.right - w * 0.08f, r.top + h * 0.2f)
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        labelPaint.color = paintLecture.color
        labelPaint.alpha = 60
        canvas.drawRect(labelRect, labelPaint)
    }

    private fun drawInnerGear(canvas: Canvas, cx: Float, cy: Float, innerR: Float, outerR: Float, teeth: Int) {
        val path = Path()
        val totalPoints = teeth * 2
        val angleStep = 2.0 * Math.PI / totalPoints
        for (i in 0 until totalPoints) {
            val angle = i * angleStep
            val r = if (i % 2 == 0) innerR else outerR
            val x = cx + (cos(angle) * r).toFloat()
            val y = cy + (sin(angle) * r).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        val holePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        holePaint.color = paintIpad.color
        canvas.drawPath(path, holePaint)
    }

    // ═══════════════════════════════════════════════════════════════════
    //  drawProgressNeumorphic / drawLectureNeumorphic
    // ═══════════════════════════════════════════════════════════════════

    fun drawProgressNeumorphic(canvas: Canvas) {
        if (rectFProgress == null) return
        val baseColor = paintIpad.color
        drawNeumorphicRect(
            canvas,
            rectFProgress!!.left, rectFProgress!!.top,
            rectFProgress!!.right, rectFProgress!!.bottom,
            rectFProgress!!.height() * 0.5f, baseColor,
            rectFProgress!!.height() * 0.15f
        )
        // Progress fill
        val progressWidth = rectFProgress!!.width() * progress
        val fillRect = RectF(
            rectFProgress!!.left, rectFProgress!!.top,
            rectFProgress!!.left + progressWidth, rectFProgress!!.bottom
        )
        canvas.drawRoundRect(fillRect, rectFProgress!!.height() * 0.5f, rectFProgress!!.height() * 0.5f, paintLecture)
    }

    fun drawLectureNeumorphic(canvas: Canvas) {
        if (rectFLecture == null) return
        val baseColor = paintIpad.color
        drawNeumorphicRect(
            canvas,
            rectFLecture!!.left, rectFLecture!!.top,
            rectFLecture!!.right, rectFLecture!!.bottom,
            rectFLecture!!.width() * 0.06f, baseColor,
            rectFLecture!!.width() * 0.01f
        )
    }

    // ═══════════════════════════════════════════════════════════════════
    //  drawIpad (2 overloads)
    // ═══════════════════════════════════════════════════════════════════

    fun drawIpad(canvas: Canvas, isDrawProgress: Boolean) {
        when (mIpadType) {
            IpadType.IPAD.ordinal, IpadType.IPAD_UNBLUR.ordinal -> {
                drawRectWithShadow(canvas, ipad_rect!!, ipad_rect!!.width() * 0.04f, paintIpad, darkShadowPaint)
                drawProgress(canvas)
                drawAya(canvas)
                drawLecture(canvas)
            }
            IpadType.IPAD_CLASSIC.ordinal -> {
                if (color_gradient != null) {
                    paint.shader = linearGradient_classic
                    canvas.drawRoundRect(ipad_rect!!, ipad_rect!!.width() * 0.04f, ipad_rect!!.width() * 0.04f, paint)
                    paint.shader = null
                } else {
                    canvas.drawRoundRect(ipad_rect!!, ipad_rect!!.width() * 0.04f, ipad_rect!!.width() * 0.04f, paintIpad)
                }
                drawProgress(canvas)
                drawAya(canvas)
                drawLecture(canvas)
            }
            IpadType.ROUND_RECT.ordinal -> {
                drawRectWithShadow(canvas, ipad_rect!!, ipad_rect!!.width() * 0.06f, paintIpad, darkShadowPaint)
                drawProgress(canvas)
                drawAya(canvas)
                drawLecture(canvas)
            }
            IpadType.RECT.ordinal -> {
                canvas.drawRect(ipad_rect!!, paintIpad)
                drawProgress(canvas)
                drawAya(canvas)
                drawLecture(canvas)
            }
            IpadType.BOTTOM_RECT.ordinal -> {
                canvas.drawRect(ipad_rect!!, paintIpad)
                drawProgress(canvas)
                drawAya(canvas)
            }
            IpadType.BORDER.ordinal -> {
                val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
                borderPaint.style = Paint.Style.STROKE
                borderPaint.strokeWidth = ipad_rect!!.width() * 0.015f
                borderPaint.color = paintIpad.color
                canvas.drawRoundRect(ipad_rect!!, ipad_rect!!.width() * 0.04f, ipad_rect!!.width() * 0.04f, borderPaint)
                drawProgress(canvas)
                drawAya(canvas)
                drawLecture(canvas)
            }
            IpadType.BLACK_LAYER.ordinal -> drawBlackLayer(canvas)
            IpadType.GRADIENT.ordinal -> drawGradientLayer(canvas)
            IpadType.BLUE_TYPE.ordinal -> drawBlueType(canvas)
            IpadType.MASK_BRUSH.ordinal -> {
                drawProgress(canvas)
                drawAya(canvas)
            }
            IpadType.IPAD_NEOMORPHIC.ordinal -> {
                drawLectureNeumorphic(canvas)
                drawProgressNeumorphic(canvas)
                drawAya(canvas)
            }
            IpadType.HEART.ordinal -> drawHeartType(canvas)
            IpadType.BATTERY.ordinal -> drawBatteryType(canvas)
            IpadType.CASSET.ordinal -> {
                drawCaset(canvas)
                drawProgress(canvas)
                drawAya(canvas)
            }
            IpadType.CASSET_IMG.ordinal -> {
                drawCasetNoBg(canvas)
                drawProgress(canvas)
                drawAya(canvas)
            }
            IpadType.CASSET_IMG_BLUR.ordinal -> {
                drawCasetNoBg(canvas)
                drawProgress(canvas)
                drawAya(canvas)
            }
        }
    }

    fun drawIpad(canvas: Canvas) {
        drawIpad(canvas, true)
    }

    // ═══════════════════════════════════════════════════════════════════
    //  drawMaskedBitmap
    // ═══════════════════════════════════════════════════════════════════

    fun drawMaskedBitmap(canvas: Canvas) {
        if (bitmapSquare == null || rectSquare == null) return
        val src = Rect(0, 0, bitmapSquare!!.width, bitmapSquare!!.height)
        canvas.drawBitmap(bitmapSquare!!, src, RectF(rectSquare!!), paint)
    }

    // ═══════════════════════════════════════════════════════════════════
    //  drawGradientLayer
    // ═══════════════════════════════════════════════════════════════════

    fun drawGradientLayer(canvas: Canvas) {
        if (color_gradient != null) {
            paint.shader = linearGradient_classic
            canvas.drawRect(0f, 0f, mCanvas_width.toFloat(), mCanvas_height.toFloat(), paint)
            paint.shader = null
        } else {
            canvas.drawRect(0f, 0f, mCanvas_width.toFloat(), mCanvas_height.toFloat(), paintIpad)
        }
        drawProgress(canvas)
        drawAya(canvas)
    }

    // ═══════════════════════════════════════════════════════════════════
    //  drawHeartType
    // ═══════════════════════════════════════════════════════════════════

    fun drawHeartType(canvas: Canvas) {
        val w = mCanvas_width.toFloat()
        val h = mCanvas_height.toFloat()
        val heartPath = Path()
        val cx = w / 2f
        val cy = h * 0.35f
        val heartW = w * 0.45f
        val heartH = h * 0.35f

        heartPath.moveTo(cx, cy + heartH)
        heartPath.cubicTo(cx - heartW, cy + heartH * 0.3f, cx - heartW, cy - heartH * 0.3f, cx, cy)
        heartPath.cubicTo(cx + heartW, cy - heartH * 0.3f, cx + heartW, cy + heartH * 0.3f, cx, cy + heartH)
        heartPath.close()

        canvas.drawPath(heartPath, paintIpad)
        drawProgress(canvas)
        drawAya(canvas)
    }

    // ═══════════════════════════════════════════════════════════════════
    //  drawBatteryType
    // ═══════════════════════════════════════════════════════════════════

    fun drawBatteryType(canvas: Canvas) {
        val w = mCanvas_width.toFloat()
        val h = mCanvas_height.toFloat()
        val batteryLeft = w * 0.1f
        val batteryTop = h * 0.25f
        val batteryRight = w * 0.9f
        val batteryBottom = h * 0.75f
        val batteryRect = RectF(batteryLeft, batteryTop, batteryRight, batteryBottom)
        val radius = w * 0.03f

        // Battery body
        canvas.drawRoundRect(batteryRect, radius, radius, paintIpad)

        // Battery terminal
        val terminalW = w * 0.12f
        val terminalH = h * 0.1f
        val terminalRect = RectF(batteryRight, (batteryTop + batteryBottom) / 2f - terminalH / 2f, batteryRight + terminalW, (batteryTop + batteryBottom) / 2f + terminalH / 2f)
        canvas.drawRoundRect(terminalRect, radius * 0.5f, radius * 0.5f, paintIpad)

        drawProgress(canvas)
        drawAya(canvas)
    }

    // ═══════════════════════════════════════════════════════════════════
    //  drawBlueType
    // ═══════════════════════════════════════════════════════════════════

    fun drawBlueType(canvas: Canvas) {
        val w = mCanvas_width.toFloat()
        val h = mCanvas_height.toFloat()
        paintIpad.alpha = 224
        canvas.drawRect(0f, 0f, w, h, paintIpad)
        drawProgress(canvas)
        drawAya(canvas)
    }

    // ═══════════════════════════════════════════════════════════════════
    //  drawBlackLayer
    // ═══════════════════════════════════════════════════════════════════

    fun drawBlackLayer(canvas: Canvas) {
        val w = mCanvas_width.toFloat()
        val h = mCanvas_height.toFloat()
        paintIpad.alpha = 224
        canvas.drawRect(0f, 0f, w, h, paintIpad)
        drawProgress(canvas)
        drawAya(canvas)
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Bismilah entity helpers
    // ═══════════════════════════════════════════════════════════════════

    fun getmIsti3adhaEntity(): BismilahEntity? = mIsti3adhaEntity

    fun addIsti3adhaEntity(entity: BismilahEntity) {
        this.mIsti3adhaEntity = entity
    }

    fun getBismilahEntity(): BismilahEntity? = bismilahEntity

    fun addBismilahEntity(entity: BismilahEntity) {
        this.bismilahEntity = entity
    }

    // ═══════════════════════════════════════════════════════════════════
    //  SurahNameEntity helpers
    // ═══════════════════════════════════════════════════════════════════

    
    fun setSurahNameEntity(
        name: String,
        readerName: String,
        bitmap: Any?,
        scale: Float,
        fontPath: String,
        clrAya: Int,
        preset: Int,
        style: Int,
        indexSurah: Int,
        haveBg: Boolean,
        clrBg: Int
    ) {
        val entity = surahNameEntity
        if (entity != null) {
            entity.name = name
            entity.reader = readerName
            entity.clrBg = clrBg
            entity.isHaveBg = haveBg
            entity.index_surah = indexSurah
        } else {
            surahNameEntity = SurahNameEntity(
                Layout.Alignment.ALIGN_CENTER,
                name,
                readerName,
                RectF(),
                Typeface.DEFAULT,
                clrAya,
                1.0f,
                "",
                0,
                null,
                0,
                indexSurah,
                0,
                haveBg,
                clrBg
            )
        }
        invalidate()
    }

    fun updateAlignmentSurah(textValue: String): Layout.Alignment {
        if (mIpadType == IpadType.IPAD_NEOMORPHIC.ordinal || mIpadType == IpadType.CASSET.ordinal || mIpadType == IpadType.CASSET_IMG.ordinal || mIpadType == IpadType.CASSET_IMG_BLUR.ordinal) {
            return Layout.Alignment.ALIGN_CENTER
        }
        return if (!Utils.isProbablyLArabic(textValue)) {
            Layout.Alignment.ALIGN_NORMAL
        } else {
            Layout.Alignment.ALIGN_OPPOSITE
        }
    }


    // ═══════════════════════════════════════════════════════════════════
    //  drawBismilah
    // ═══════════════════════════════════════════════════════════════════

    fun drawBismilah(canvas: Canvas) {
        mIsti3adhaEntity?.let {
            if (it.getBismilahTimeline()?.visible() == true) {
                it.draw(canvas)
            }
        }
        bismilahEntity?.let {
            if (it.getBismilahTimeline()?.visible() == true) {
                it.draw(canvas)
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  drawNameSurah
    // ═══════════════════════════════════════════════════════════════════

    fun drawNameSurah(canvas: Canvas) {
        surahNameEntity?.let {
            if (it.isVisible) {
                it.draw(canvas)
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  drawAya
    // ═══════════════════════════════════════════════════════════════════

    fun drawAya(canvas: Canvas) {
        if (rectFAya == null) return
        val ayaPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        ayaPaint.color = color_line_bg
        ayaPaint.alpha = 40
        canvas.drawRect(rectFAya!!, ayaPaint)
    }

    // ═══════════════════════════════════════════════════════════════════
    //  drawLecture
    // ═══════════════════════════════════════════════════════════════════

    fun drawLecture(canvas: Canvas) {
        // Draw lecture/reading area indicator
    }

    // ═══════════════════════════════════════════════════════════════════
    //  setupBitmapDraw (reconstructed from smali)
    // ═══════════════════════════════════════════════════════════════════

    fun setupBitmapDraw(bitmap1: Bitmap, bitmap2: Bitmap, template: Template): String {
        frameInterval = 1000L / template.fps
        bitmapBlured = bitmap1
        bitmapSquare = bitmap2
        surahNameEntity?.setCopyRect()
        createRect()
        val bgName = "bg_${System.currentTimeMillis()}.png"
        val file = File(template.folder_template!!)
        val bgBitmap = getBitmapDraw(template.isVideoSquare, file)
        FontUtils.copyFontToInternalStorage(context, "NotoNaskhArabic.ttf")
        var cursorSize = linePaint.strokeWidth * 4.2f
        val ipadType = template.ipad_type
        if (ipadType == IpadType.BLACK_LAYER.ordinal || ipadType == IpadType.BLUE_TYPE.ordinal ||
            ipadType == IpadType.GRADIENT.ordinal || ipadType == IpadType.MASK_BRUSH.ordinal ||
            ipadType == IpadType.HEART.ordinal || mIpadType == IpadType.BATTERY.ordinal
        ) {
            cursorSize = 0f
        }
        var startShape = 0f
        var widthShape = 0
        var heightShape = 0
        when (ipadType) {
            IpadType.BLUE_TYPE.ordinal -> {
                saveProgressBitmapTypeBlue(file)
            }
            IpadType.IPAD_NEOMORPHIC.ordinal -> {
                saveProgressBitmapTypeIPAD_NEOMORPHIC(file, bgBitmap)
            }
            IpadType.HEART.ordinal -> {
                val pair = saveProgressBitmapTypeHeart(file, bgBitmap)
                startShape = pair.first
                heightShape = pair.second
            }
            IpadType.BATTERY.ordinal -> {
                val pair = saveProgressBitmapTypeBattery(file, bgBitmap)
                startShape = pair.first
                widthShape = pair.second.x
                heightShape = pair.second.y
            }
            IpadType.CASSET.ordinal, IpadType.CASSET_IMG.ordinal, IpadType.CASSET_IMG_BLUR.ordinal -> {
                startShape = rectFProgress!!.left
                widthShape = rectFProgress!!.top.toInt()
                heightShape = rectFProgress!!.right.toInt()
            }
            else -> {
                saveProgressBitmap(file, cursorSize)
            }
        }
        drawEntityBitmap(file, bgBitmap.width, bgBitmap.height)
        saveBg(bgName, bgBitmap, file)
        val timeModel = template.mTimeModel
        val progressOffset = Math.round(cursorSize * 1.98f)
        if (timeModel == null) {
            val newTimeModel = TimeModel(
                rectFProgress!!.width().toInt(),
                (rectFProgress!!.height() * 1.5f).toInt(),
                paintText.textSize * 0.96f,
                if (paintText.color == -1) "white" else "black",
                txt_y,
                newLeft_txt,
                progressOffset
            )
            template.mTimeModel = newTimeModel
        } else {
            timeModel.color = if (paintText.color == -1) "white" else "black"
            timeModel.posXRight = newLeft_txt
            timeModel.posY = txt_y
            timeModel.height_bitmap_progress = (rectFProgress!!.height() * 1.5f).toInt()
            timeModel.width_bitmap_progress = rectFProgress!!.width().toInt()
            timeModel.size = paintText.textSize * 0.96f
            timeModel.progress_offset = progressOffset
        }
        template.mTimeModel!!.startShape = startShape
        template.mTimeModel!!.widthShape = widthShape
        template.mTimeModel!!.heightShape = heightShape
        return "${file.absolutePath}/$bgName"
    }

    // ═══════════════════════════════════════════════════════════════════
    //  saveBg
    // ═══════════════════════════════════════════════════════════════════

    fun saveBg(bgName: String, bgBitmap: Bitmap, file: File) {
        saveBitmap(file, bgName, bgBitmap)
    }

    // ═══════════════════════════════════════════════════════════════════
    //  getBitmapDraw
    // ═══════════════════════════════════════════════════════════════════

    fun getBitmapDraw(isVideoSquare: Boolean, file: File): Bitmap {
        val w = mCanvas_width
        val h = mCanvas_height
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        if (bitmapBlured != null && !bitmapBlured!!.isRecycled) {
            canvas.drawBitmap(bitmapBlured!!, btmX, btmY, paint)
        }
        drawIpad(canvas, true)
        drawLineHelper(canvas)
        drawBismilah(canvas)
        drawEntity(canvas)
        drawNameSurah(canvas)
        if (!isPro && !isRemoveWattermark) {
            drawWattermark(canvas, true)
        }
        return bitmap
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Entity list helpers
    // ═══════════════════════════════════════════════════════════════════

    fun getLastAdd(): QuranEntity? = if (quranEntities.isEmpty()) null else quranEntities[quranEntities.size - 1]

    fun getLastAddTrsl(): TranslationQuranEntity? =
        if (translationEntities.isEmpty()) null else translationEntities[translationEntities.size - 1]

    fun countEntityQuran(): Int = quranEntities.size

    fun countEntityTrsl(): Int = translationEntities.size

    // ═══════════════════════════════════════════════════════════════════
    //  Size update methods
    // ═══════════════════════════════════════════════════════════════════

    fun updateSizeAyaSave() {
        for (q in quranEntities) {
            q.setupScaleSave(q.factorSize, mCanvas_width)
        }
    }

    fun updateSizeTrslSave() {
        for (t in translationEntities) {
            t.setupScaleSave(t.factorSize, mCanvas_width)
        }
    }

    fun updateSizeAya() {
        for (q in quranEntities) {
            q.setupScale(q.factorSize, mCanvas_width, mCanvas_height)
        }
    }

    fun updateSizeAyaTrsl() {
        for (t in translationEntities) {
            t.setupScale(t.factorSize, mCanvas_width, mCanvas_height)
        }
    }

    fun updateSizeAyaResize() {
        for (q in quranEntities) {
            q.scale(q.factorScale, mCanvas_width, mCanvas_height)
        }
    }

    fun updateSizeTrslAyaResize() {
        for (t in translationEntities) {
            t.scale(t.factorScale, mCanvas_width, mCanvas_height)
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  updatePosSurahName
    // ═══════════════════════════════════════════════════════════════════

    fun updatePosSurahName() {
        surahNameEntity?.let {
            it.update(rectFSurahName!!)
            it.setCopyRect()
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  updateBismilahEntity
    // ═══════════════════════════════════════════════════════════════════

    fun updateBismilahEntity(rectF: RectF) {
        bismilahEntity?.update(rectF, (rectF.width() * 0.85f).toInt(), (rectF.height() * 0.85f).toInt())
        mIsti3adhaEntity?.update(rectF, (rectF.width() * 0.85f).toInt(), (rectF.height() * 0.85f).toInt())
    }

    fun updateBismilahEntity(rectF: RectF, maxW: Int, maxH: Int) {
        bismilahEntity?.update(rectF, maxW, maxH)
        mIsti3adhaEntity?.update(rectF, maxW, maxH)
    }

    // ═══════════════════════════════════════════════════════════════════
    //  resizeEntity
    // ═══════════════════════════════════════════════════════════════════

    fun resizeEntity() {
        for (q in quranEntities) {
            q.scale(q.factorScale, mCanvas_width, mCanvas_height)
        }
        for (t in translationEntities) {
            t.scale(t.factorScale, mCanvas_width, mCanvas_height)
        }
        surahNameEntity?.scale(surahNameEntity!!.factorScale, mCanvas_width, mCanvas_height)
    }

    // ═══════════════════════════════════════════════════════════════════
    //  findEntityAtPoint
    // ═══════════════════════════════════════════════════════════════════

    fun findEntityAtPoint(x: Float, y: Float): EntityView? {
        // Check translation entities first (they are on top)
        for (i in translationEntities.indices.reversed()) {
            val t = translationEntities[i]
            if (t.isVisible && t.rect.contains(x, y)) {
                return t
            }
        }
        // Check quran entities
        for (i in quranEntities.indices.reversed()) {
            val q = quranEntities[i]
            if (q.isVisible && q.rect.contains(x, y)) {
                return q
            }
        }
        // Check bismilah
        bismilahEntity?.let {
            if (it.isVisible && it.getBismilahTimeline()?.visible() == true && it.rect.contains(x, y)) {
                return it
            }
        }
        mIsti3adhaEntity?.let {
            if (it.isVisible && it.getBismilahTimeline()?.visible() == true && it.rect.contains(x, y)) {
                return it
            }
        }
        // Check surah name
        surahNameEntity?.let {
            if (it.isVisible && it.rect.contains(x, y)) {
                return it
            }
        }
        return null
    }

    // ═══════════════════════════════════════════════════════════════════
    //  updateSelectionOnTap
    // ═══════════════════════════════════════════════════════════════════

    fun updateSelectionOnTap(x: Float, y: Float) {
        val entity = findEntityAtPoint(x, y)
        if (entity != null) {
            setEntity_select(entity)
            iViewCallback?.onSelect(entity)
            if (selectTool != null) {
                selectTool!!.setApply_all(false)
                if (selectTool!!.isScale(entity, x, y)) {
                    selectTool!!.setApply_Scale(true)
                }
            }
        } else {
            entity_select = null
            selectTool?.reset()
            iViewCallback?.onEmtyClick()
        }
        invalidate()
    }

    // ═══════════════════════════════════════════════════════════════════
    //  setNotDraw
    // ═══════════════════════════════════════════════════════════════════

    fun setNotDraw(notDraw: Boolean) {
        this.isNotDraw = notDraw
    }

    // ═══════════════════════════════════════════════════════════════════
    //  handleTranslate
    // ═══════════════════════════════════════════════════════════════════

    fun handleTranslate(dx: Float, dy: Float) {
        entity_select?.postTranslate(dx, dy)
        invalidate()
    }

    // ═══════════════════════════════════════════════════════════════════
    //  drawLineHelper
    // ═══════════════════════════════════════════════════════════════════

    fun drawLineHelper(canvas: Canvas) {
        val w = mCanvas_width.toFloat()
        val h = mCanvas_height.toFloat()
        linePaint.color = Color.WHITE
        linePaint.alpha = 50
        if (showCenterLineX) {
            canvas.drawLine(w / 2f, 0f, w / 2f, h, linePaint)
        }
        if (showCenterLineY) {
            canvas.drawLine(0f, h / 2f, w, h / 2f, linePaint)
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  distanceToCenter
    // ═══════════════════════════════════════════════════════════════════

    fun distanceToCenter(entityView: EntityView): Float {
        val cx = mCanvas_width / 2f
        val cy = mCanvas_height / 2f
        val rect = entityView.rect
        val dx = rect.centerX() - cx
        val dy = rect.centerY() - cy
        return sqrt(dx * dx + dy * dy)
    }

    // ═══════════════════════════════════════════════════════════════════
    //  onTouch
    // ═══════════════════════════════════════════════════════════════════

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        if (gestureDetector != null) {
            gestureDetector!!.onTouchEvent(event)
        }
        if (moveGestureDetector != null) {
            moveGestureDetector!!.onTouchEvent(event)
        }
        if (scaleGestureDetector != null) {
            scaleGestureDetector!!.onTouchEvent(event)
        }
        if (event.action == MotionEvent.ACTION_UP) {
            isOnScale = false
            iViewCallback?.onEndMove()
            iViewCallback?.onEndScale()
        }
        return true
    }

    // ═══════════════════════════════════════════════════════════════════
    //  setiViewCallback
    // ═══════════════════════════════════════════════════════════════════

    fun setiViewCallback(callback: IViewCallback?) {
        this.iViewCallback = callback
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Additional accessors
    // ═══════════════════════════════════════════════════════════════════

    fun getRadius_square(): Int = radius_square
    fun getRectSquare(): Rect? = rectSquare
    fun setRadius_square(radius: Int) { this.radius_square = radius }
    fun setBitmapSquare(bitmap: Bitmap?) { this.bitmapSquare = bitmap }
    fun getRectFProgress(): RectF? = rectFProgress
    fun getRectFAya(): RectF? = rectFAya
    fun getRectFLecture(): RectF? = rectFLecture
    fun getLinePaint(): Paint = linePaint
    fun getPaintText(): TextPaint = paintText
    fun isSquare(): Boolean = isSquare
    fun setSquare(square: Boolean) { isSquare = square }
    fun isWattermark(): Boolean = isWattermark
    fun setWattermark(wattermark: Boolean) { isWattermark = wattermark }
    fun isAnimWatermk(): Boolean = isAnimWatermk
    fun setAnimWatermk(animWatermk: Boolean) { isAnimWatermk = animWatermk }
    fun isNotDraw(): Boolean = isNotDraw
    fun isOnScale(): Boolean = isOnScale
    fun getSelectTool(): EntitySelectTool? = selectTool
    fun getStartTime(): Long = startTime
    fun setStartTime(startTime: Long) { this.startTime = startTime }
    fun getFrameInterval(): Long = frameInterval
    fun getIpad_rect(): RectF? = ipad_rect
    fun getScheme(): ColorSchemeGenerator.Scheme? = scheme
    fun setShowCenterLineX(show: Boolean) { this.showCenterLineX = show }
    fun setShowCenterLineY(show: Boolean) { this.showCenterLineY = show }
    fun getTranslationEntities(): List<TranslationQuranEntity> = translationEntities

    // ═══════════════════════════════════════════════════════════════════
    //  Inner classes: MoveListener and ScaleListener
    // ═══════════════════════════════════════════════════════════════════

    private inner class MoveListener : MoveGestureDetector.SimpleOnMoveGestureListener() {
        override fun onMove(detector: MoveGestureDetector): Boolean {
            handleTranslate(detector.getFocusDelta().x, detector.getFocusDelta().y)
            return true
        }

        override fun onMoveEnd(detector: MoveGestureDetector) {
            super.onMoveEnd(detector)
            if (entity_select == null || selectTool == null) return
            selectTool!!.setApply_all(true)
        }
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            if (entity_select != null) {
                isOnScale = true
                selectTool!!.setApply_Scale(true)
                selectTool!!.setOnProgress(true)
            }
            return super.onScaleBegin(detector)
        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            if (entity_select == null) return true
            entity_select!!.scale(detector.scaleFactor, mCanvas_width, mCanvas_height)
            invalidate()
            return true
        }

        override fun onScaleEnd(detector: ScaleGestureDetector) {
            if (entity_select is QuranEntity) {
                selectTool!!.setApply_all(true)
                selectTool!!.setOnProgress(false)
            }
            super.onScaleEnd(detector)
        }
    }
}
