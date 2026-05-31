package hazem.nurmontage.videoquran.views

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Insets
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Handler
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.Scroller
import androidx.core.content.ContextCompat
import androidx.core.view.GestureDetectorCompat
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.utils.CanvasUtils
import hazem.nurmontage.videoquran.core.common.Common
import hazem.nurmontage.videoquran.constant.EntityAction
import hazem.nurmontage.videoquran.entity_timeline.Entity
import hazem.nurmontage.videoquran.entity_timeline.EntityAudio
import hazem.nurmontage.videoquran.entity_timeline.EntityBismilahTimeline
import hazem.nurmontage.videoquran.entity_timeline.EntityQuranTimeline
import hazem.nurmontage.videoquran.entity_timeline.EntityTrslTimeline
import hazem.nurmontage.videoquran.model.data.BismilahEntity
import hazem.nurmontage.videoquran.model.EntityView
import hazem.nurmontage.videoquran.model.data.QuranEntity
import hazem.nurmontage.videoquran.model.Transition
import java.util.Locale
import java.util.Stack
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

/**
 * Custom [FrameLayout] that serves as the main timeline track editor for the
 * Quran video maker. It displays audio, Quran verse, translation, and
 * Bismilah entities on a horizontal timeline with pinch-to-zoom, scroll/fling
 * gestures, entity selection, drag-to-move, and trim operations.
 *
 * Features:
 * - Horizontal timeline with time markers and playhead cursor
 * - Pinch-to-zoom scaling (0.09x – 8x)
 * - Scroll and fling gestures for timeline navigation
 * - Entity selection, multi-select, drag, and trim
 * - Undo/redo stacks for entity actions
 * - Auto-scroll during playback and trim operations
 * - Snap-to-edge and snap-to-entity alignment guides
 *
 * Converted from TrackEntityView.java (4,582 lines).
 */
@Suppress("DEPRECATION")
class TrackEntityView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle), View.OnTouchListener {

    companion object {
        private const val DEFAULT_SCALE = 0.5f
        private const val MAX_SCALE = 8.0f
        private const val MIN_SCALE = 0.09f
        private const val FACTOR_VITESSE = 180.0f
        private const val CLR_DEFAULT_BG = -13421771   // 0x333333
        private const val CLR_SELECT = -794718          // 0x3D3D32
        private const val CLR_BTN_DEFAULT = -13421771
    }

    // ── Callback interface ──────────────────────────────────────────────

    interface ITrimLineCallback {
        fun enableRedo(enabled: Boolean)
        fun enableUndo(enabled: Boolean)
        fun fadeInAudio(delta: Float)
        fun fadeOutAudio(delta: Float)
        fun onAddStack(entityAction: EntityAction)
        fun onDelete(entityView: EntityView)
        fun onEmptySelect()
        fun onMove()
        fun onPlayVibration()
        fun onSeekPlayer(position: Float)
        fun onSelectEntity(entity: Entity, delta: Float)
        fun onSelectMultiple(count: Int)
        fun onUp()
        fun onUpdate()
        fun onUpdatePlayerAudio(entityAudio: EntityAudio)
        fun onUpdateTime()
        fun pause()
        fun progress(show: Boolean)
    }

    // ── Fields ────────────────────────────────────────────────────────

    internal var DETECT_LEFT_MOVE: Float = 0f
    internal var DETECT_RIGHT_MOVE: Float = 0f
    internal var SPEED: Float = 0f
    internal var TOLERANCE_X: Float = 0.95f

    internal var autoMoveRunnable: Runnable? = null
    internal var autoScrollHandler: Handler = Handler()
    internal var autoScrollRunnable: Runnable? = null

    var bismilahTimeline: EntityBismilahTimeline? = null
    internal var btn_redo: ImageButton? = null
    internal var btn_undo: ImageButton? = null
    internal var canvas_top_Y: Float = 0f
    internal var centerX: Float = 0f
    internal var clr_btn_audio: Int = CLR_BTN_DEFAULT
    internal var clr_btn_quran: Int = CLR_BTN_DEFAULT
    internal var clr_btn_trsl: Int = CLR_BTN_DEFAULT
    internal var countMove: Int = 0
    internal var currentEventX: Float = 0f
    @JvmField
    internal var currentPosition: Float = 0f
    internal var current_cursur_position: Int = 0
    internal var duration: Int = 0
    internal var dx: Float = 0f

    internal var entityList: Stack<Pair<Entity, EntityAction>> = Stack()
    var entityListAudio: MutableList<EntityAudio> = ArrayList()
    val entityListQuran: MutableList<EntityQuranTimeline> = ArrayList()
    val entityListTrslQuran: MutableList<EntityTrslTimeline> = ArrayList()

    internal var eventX: Float = 0f
    internal var eventY: Float = 0f
    var exclusionRects: MutableList<Rect> = ArrayList()

    internal var gestureDetector: GestureDetectorCompat? = null

    internal var iTrimLineCallback: ITrimLineCallback? = null
    var isArabic_lang: Boolean = false
    internal var isAutoMove: Boolean = false
    internal var isAutoScroll: Boolean = false
    internal var isCheckLine: Boolean = false
    internal var isCheckLineCursur: Boolean = false
    internal var isDetectChange: Boolean = false
    internal var isFling: Boolean = false
    internal var isMove: Boolean = false
    internal var isOnUp: Boolean = false
    internal var isPassScroll: Boolean = true
    var isPlaying: Boolean = false
    internal var isProgress: Boolean = false
    internal var isScaleListener: Boolean = false

    internal var lasX: Float = 0f
    internal var lastDifference: Long = 0L
    internal var lastTime: Long = 0L
    var mIsi3adaTimeline: EntityBismilahTimeline? = null
    internal var mScrollY: Float = 0f
    internal var m_pos_y_marker: Float = 0f
    internal var markerHeight: Float = 0f
    internal var maxBottom: Float = 0f
    internal var maxTime: Int = -1
        set(value) {
            field = value
            timeLineW = (value * getSecond_in_screen()) / 1000.0f
        }
    internal var max_trim: Float = 0f
    internal var objectAnimator: ObjectAnimator? = null
    internal var onThink: Boolean = true
    internal var p: Float = 0f
    internal var paddingCursur: Float = 0f

    internal var paintCursur: Paint? = null
    private val paintItem: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    internal var paintLineCheck: Paint? = null
    internal var paintMaker: Paint? = null
    internal var paint_time: Paint? = null
    internal var pass: Boolean = false

    internal var pathItemAudio: Path? = null
    internal var pathItemQuran: Path? = null
    internal var pathItemTrslQuran: Path? = null
    internal var posY: Float = 0f
    internal var radius: Float = 0f

    internal var rectFItemQuran: RectF? = null
    internal var rectFItemTrslQuran: RectF? = null
    internal var rectItemAudio: RectF? = null
    internal var rectSquareAudio: RectF? = null
    internal var rectSquareQuran: RectF? = null
    internal var rectSquareTrslQuran: RectF? = null

    var scaleFactor: Float = DEFAULT_SCALE
        set(value) {
            field = value
            scrolled_with_zoom = value * currentPosition
        }

    internal var scaleGestureDetector: ScaleGestureDetector? = null
    internal var scrolled_with_zoom: Float = 0f
    internal var scroller: Scroller? = null
    @JvmField
    var second_in_screen: Float = 0f
    var selectedEntity: Entity? = null
    internal var signeX: Float = -1f
    internal var signeY: Float = -1f
    internal var startXLine: Float = 0f
    internal var start_y_draw: Float = 0f
    internal var target: Float = 0f
    internal var timeLineW: Float = 0f
    internal var time_start: Long = 0L
    internal var undoEntityList: Stack<Pair<Entity, EntityAction>> = Stack()
    internal var w_time_item: Float = 0f
    internal var width_screen: Int = 0
    var entityY: Float = 0f

    // ── Gesture listener (extracted from duplicated constructors) ─────

    private val gestureListener = object : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(motionEvent: MotionEvent): Boolean {
            pauseScroll()
            val pointF = PointF(motionEvent.x, motionEvent.y)
            isPassScroll = true
            if (selectedEntity != null) {
                val contains = selectedEntity!!.contains(pointF)
                isPassScroll = !contains && selectedEntity!!.trimType == -1
                selectedEntity!!.isSelect = true
                if (!isPassScroll && iTrimLineCallback != null) {
                    if (selectedEntity!!.trimType == 0) {
                        selectedEntity!!.setCurrentRect()
                        selectedEntity!!.setOnTapTime(
                            round(selectedEntity!!.rect.left / getSecond_in_screen()) * 1000,
                            selectedEntity!!.rect.left
                        )
                        iTrimLineCallback!!.onPlayVibration()
                    } else if (selectedEntity!!.trimType == 1) {
                        selectedEntity!!.setCurrentRect()
                        selectedEntity!!.setOnTapTime(
                            round(selectedEntity!!.rect.right / getSecond_in_screen()) * 1000,
                            selectedEntity!!.rect.right
                        )
                        iTrimLineCallback!!.onPlayVibration()
                    } else if (contains) {
                        selectedEntity!!.setCurrentRect()
                        iTrimLineCallback!!.onSelectEntity(selectedEntity!!, 0.0f)
                    }
                }
            }
            return true
        }

        override fun onSingleTapUp(motionEvent: MotionEvent): Boolean {
            if (!isPlaying) {
                if (handleItemInteraction(
                        motionEvent.x + paddingLeft + (centerX - radius * 0.5f) + scrolled_with_zoom,
                        motionEvent.y
                    )) {
                    return true
                }
            } else if (clr_btn_quran != CLR_BTN_DEFAULT || clr_btn_audio != CLR_BTN_DEFAULT || clr_btn_trsl != CLR_BTN_DEFAULT) {
                clr_btn_trsl = CLR_BTN_DEFAULT
                clr_btn_quran = CLR_BTN_DEFAULT
                clr_btn_audio = CLR_BTN_DEFAULT
            }
            if (isPassScroll) {
                updateSelectionOnTap(motionEvent)
            }
            return true
        }

        override fun onScroll(motionEvent: MotionEvent?, motionEvent2: MotionEvent, f: Float, f2: Float): Boolean {
            if (isProgress || !isPassScroll || (selectedEntity != null && selectedEntity!!.trimType != -1)) {
                return super.onScroll(motionEvent, motionEvent2, f, f2)
            }
            if (!isScaleListener && motionEvent2.eventTime - motionEvent!!.eventTime >= 107 && isPass(motionEvent2)) {
                if (isPlaying) {
                    isPlaying = false
                }
                if (eventX == 0.0f) {
                    eventX = motionEvent2.rawX
                    eventY = motionEvent2.rawY
                    return true
                }
                val rawX = motionEvent2.rawX - eventX
                currentPosition += rawX / scaleFactor
                if (currentPosition > 0.0f) {
                    currentPosition = 0.0f
                }
                scrolled_with_zoom = currentPosition * scaleFactor
                if (iTrimLineCallback != null) {
                    iTrimLineCallback!!.onSeekPlayer(scrolled_with_zoom)
                }
                eventX = motionEvent2.rawX
                eventY = motionEvent2.rawY
                invalidate()
            }
            return true
        }

        override fun onFling(motionEvent: MotionEvent?, motionEvent2: MotionEvent, f: Float, f2: Float): Boolean {
            if (isProgress) return true
            if (isPlaying) isPlaying = false
            if (motionEvent2.eventTime - motionEvent!!.eventTime > 107) return true
            if (eventX == 0.0f) {
                eventX = motionEvent!!.rawX
                eventY = motionEvent!!.rawY
            }
            val abs = abs(motionEvent2.rawX - eventX)
            val abs2 = abs(motionEvent2.rawY - eventY)
            eventX = motionEvent2.rawX
            eventY = motionEvent2.rawY
            var velocityX = f
            if (if (motionEvent2.rawX > motionEvent!!.rawX) velocityX < 0.0f else velocityX > 0.0f) {
                velocityX *= -1.0f
            }
            if (abs2 > abs * 1.2f) {
                target = f2
                flingY()
            } else {
                scroller?.fling(
                    currentPosition.toInt(), 0, velocityX.toInt(), 0,
                    (-timeLineW).toInt(), 0, 0, 0
                )
                invalidate()
            }
            return true
        }
    }

    // ── Init ────────────────────────────────────────────────────────

    init {
        maxTime = -1
        TOLERANCE_X = 0.95f
        entityListAudio = ArrayList()
        lastTime = 0L
        lastDifference = 0L
        setWillNotDraw(false)
        initAutoScroll()
        setOnTouchListener(this)
        scaleGestureDetector = ScaleGestureDetector(context, ScaleListener())
        gestureDetector = GestureDetectorCompat(context, gestureListener)
        scroller = Scroller(context)
    }

    // ── ScaleListener inner class ──────────────────────────────────────

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            scaleFactor = max(MIN_SCALE, min(scaleFactor * detector.scaleFactor, MAX_SCALE))
            scrolled_with_zoom = scaleFactor * currentPosition
            invalidate()
            return true
        }

        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            isScaleListener = true
            if (iTrimLineCallback != null) {
                iTrimLineCallback!!.pause()
            }
            return super.onScaleBegin(detector)
        }

        override fun onScaleEnd(detector: ScaleGestureDetector) {
            super.onScaleEnd(detector)
        }
    }

    // ── Auto-scroll initialization ──────────────────────────────────

    private fun initAutoScroll() {
        autoScrollHandler = Handler()
        lateinit var scrollRunnable: Runnable
        scrollRunnable = Runnable {
            if (isAutoScroll) {
                var currentTimeMillis = (System.currentTimeMillis() - time_start).toFloat() / FACTOR_VITESSE
                if (SPEED < 0.0f) currentTimeMillis *= -1.0f
                val f = currentTimeMillis + SPEED
                if (selectedEntity == null) return@Runnable
                if (selectedEntity!!.trimType == 1) {
                    val rect = selectedEntity!!.rect
                    val f2 = rect.right + f
                    rect.right = f2
                    if (rect.right - selectedEntity!!.rect.left <= max_trim) {
                        selectedEntity!!.rect.right = selectedEntity!!.rect.left + max_trim
                        selectedEntity!!.setLastRight(selectedEntity!!.rect.right)
                        invalidate()
                        autoScrollHandler.removeCallbacks(autoScrollRunnable!!)
                        return@Runnable
                    }
                    if (selectedEntity is EntityQuranTimeline) {
                        val eqt = selectedEntity as EntityQuranTimeline
                        if (eqt.index + 1 < entityListQuran.size) {
                            val next = getPreviewOrNextEntityQuran(entityListQuran, eqt.index + 1, true)
                            if (next != null && f2 > next.rect.left) {
                                selectedEntity!!.rect.right = next.rect.left
                                selectedEntity!!.setLastRight(selectedEntity!!.rect.right)
                                autoScrollHandler.removeCallbacks(autoScrollRunnable!!)
                                isAutoScroll = false
                                invalidate()
                                return@Runnable
                            }
                        }
                    }
                    if (selectedEntity is EntityTrslTimeline) {
                        val etl = selectedEntity as EntityTrslTimeline
                        if (etl.index + 1 < entityListTrslQuran.size) {
                            val next = getPreviewOrNextEntityTrslQuran(entityListTrslQuran, etl.index + 1, true)
                            if (next != null && f2 > next.rect.left) {
                                selectedEntity!!.rect.right = next.rect.left
                                selectedEntity!!.setLastRight(selectedEntity!!.rect.right)
                                autoScrollHandler.removeCallbacks(autoScrollRunnable!!)
                                isAutoScroll = false
                                invalidate()
                                return@Runnable
                            }
                        }
                    }
                    selectedEntity!!.rect.right = f2
                    selectedEntity!!.setLastRight(selectedEntity!!.rect.right)
                } else if (selectedEntity!!.trimType == 0) {
                    val rect2 = selectedEntity!!.rect
                    val f3 = rect2.left + f
                    rect2.left = f3
                    if (f3 < 0.0f) {
                        selectedEntity!!.rect.left = 0.0f
                        selectedEntity!!.setLastLeft(selectedEntity!!.rect.left)
                        selectedEntity!!.updateStartTrim()
                        isAutoScroll = false
                        autoScrollHandler.removeCallbacks(autoScrollRunnable!!)
                        invalidate()
                        return@Runnable
                    }
                    if (selectedEntity!!.rect.right - f3 <= max_trim) {
                        val f4 = selectedEntity!!.rect.right - max_trim
                        isAutoScroll = false
                        selectedEntity!!.rect.left = f4
                        selectedEntity!!.setLastLeft(selectedEntity!!.rect.left)
                        selectedEntity!!.updateStartTrim()
                        autoScrollHandler.removeCallbacks(autoScrollRunnable!!)
                        invalidate()
                        return@Runnable
                    }
                    if (selectedEntity is EntityQuranTimeline) {
                        val eqt2 = selectedEntity as EntityQuranTimeline
                        if (eqt2.index > 0) {
                            val prev = getPreviewOrNextEntityQuran(entityListQuran, eqt2.index - 1, false)
                            if (prev != null && f3 <= prev.rect.right) {
                                selectedEntity!!.rect.left = prev.rect.right
                                selectedEntity!!.setLastLeft(selectedEntity!!.rect.left)
                                selectedEntity!!.updateStartTrim()
                                autoScrollHandler.removeCallbacks(autoScrollRunnable!!)
                                isAutoScroll = false
                                invalidate()
                                return@Runnable
                            }
                        }
                    }
                    if (selectedEntity is EntityTrslTimeline) {
                        val etl2 = selectedEntity as EntityTrslTimeline
                        if (etl2.index > 0) {
                            val prev = getPreviewOrNextEntityTrslQuran(entityListTrslQuran, etl2.index - 1, false)
                            if (prev != null && f3 <= prev.rect.right) {
                                selectedEntity!!.rect.left = prev.rect.right
                                selectedEntity!!.setLastLeft(selectedEntity!!.rect.left)
                                selectedEntity!!.updateStartTrim()
                                autoScrollHandler.removeCallbacks(autoScrollRunnable!!)
                                isAutoScroll = false
                                invalidate()
                                return@Runnable
                            }
                        }
                    }
                    selectedEntity!!.rect.left = f3
                    selectedEntity!!.setLastLeft(selectedEntity!!.rect.left)
                    selectedEntity!!.updateStartTrim()
                }
                currentPosition -= f / scaleFactor
                if (currentPosition > 0.0f) {
                    currentPosition = 0.0f
                    scrolled_with_zoom = currentPosition * scaleFactor
                    isAutoScroll = false
                    autoScrollHandler.removeCallbacks(autoScrollRunnable!!)
                    invalidate()
                    return@Runnable
                }
                scrolled_with_zoom = currentPosition * scaleFactor
                invalidate()
                autoScrollHandler.postDelayed(scrollRunnable, 100L)
            }
        }
        autoScrollRunnable = scrollRunnable

        lateinit var moveRunnable: Runnable
        moveRunnable = Runnable {
            if (isAutoMove) {
                var currentTimeMillis = (System.currentTimeMillis() - time_start).toFloat() / FACTOR_VITESSE
                if (SPEED < 0.0f) currentTimeMillis *= -1.0f
                val f = currentTimeMillis + SPEED
                val width = selectedEntity!!.rect.width()
                var f2 = selectedEntity!!.rect.left + f
                if (f2 < 0.0f) f2 = 0.0f
                val f3 = f2 + width
                if (selectedEntity is EntityQuranTimeline) {
                    val eqt = selectedEntity as EntityQuranTimeline
                    if (eqt.index > 0) {
                        val prev = getPreviewOrNextEntityQuran(entityListQuran, eqt.index - 1, false)
                        if (prev != null && f2 <= prev.rect.right) {
                            selectedEntity!!.setX(prev.rect.right)
                            selectedEntity!!.right = prev.rect.right + width
                            pass = false
                            invalidate()
                            isAutoMove = false
                            autoScrollHandler.removeCallbacks(autoMoveRunnable!!)
                            return@Runnable
                        }
                    }
                    if (eqt.index + 1 < entityListQuran.size) {
                        val next = getPreviewOrNextEntityQuran(entityListQuran, eqt.index + 1, true)
                        if (next != null && f3 >= next.rect.left) {
                            selectedEntity!!.setX(next.rect.left - width)
                            selectedEntity!!.right = next.rect.left
                            pass = false
                            invalidate()
                            isAutoMove = false
                            autoScrollHandler.removeCallbacks(autoMoveRunnable!!)
                            return@Runnable
                        }
                    }
                }
                if (selectedEntity is EntityTrslTimeline) {
                    val etl = selectedEntity as EntityTrslTimeline
                    if (etl.index > 0) {
                        val prev = getPreviewOrNextEntityTrslQuran(entityListTrslQuran, etl.index - 1, false)
                        if (prev != null && f2 <= prev.rect.right) {
                            selectedEntity!!.setX(prev.rect.right)
                            selectedEntity!!.right = prev.rect.right + width
                            pass = false
                            invalidate()
                            isAutoMove = false
                            autoScrollHandler.removeCallbacks(autoMoveRunnable!!)
                            return@Runnable
                        }
                    }
                    if (etl.index + 1 < entityListTrslQuran.size) {
                        val next = getPreviewOrNextEntityTrslQuran(entityListTrslQuran, etl.index + 1, true)
                        if (next != null && f3 >= next.rect.left) {
                            selectedEntity!!.setX(next.rect.left - width)
                            selectedEntity!!.right = next.rect.left
                            pass = false
                            invalidate()
                            isAutoMove = false
                            autoScrollHandler.removeCallbacks(autoMoveRunnable!!)
                            return@Runnable
                        }
                    }
                }
                if (selectedEntity is EntityAudio) {
                    val ea = selectedEntity as EntityAudio
                    if (ea.index > 0) {
                        val prev = getPreviewOrNextEntityAudio(entityListAudio, ea.index - 1, false)
                        if (prev != null && f2 <= prev.rect.right) {
                            selectedEntity!!.setX(prev.rect.right)
                            selectedEntity!!.right = prev.rect.right + width
                            pass = false
                            invalidate()
                            isAutoMove = false
                            autoScrollHandler.removeCallbacks(autoMoveRunnable!!)
                            return@Runnable
                        }
                    }
                    if (ea.index + 1 < entityListAudio.size) {
                        val next = getPreviewOrNextEntityAudio(entityListAudio, ea.index + 1, true)
                        if (next != null && f3 >= next.rect.left) {
                            selectedEntity!!.setX(next.rect.left - width)
                            selectedEntity!!.right = next.rect.left
                            pass = false
                            invalidate()
                            isAutoMove = false
                            autoScrollHandler.removeCallbacks(autoMoveRunnable!!)
                            return@Runnable
                        }
                    }
                }
                currentPosition -= f / scaleFactor
                if (currentPosition > 0.0f) {
                    currentPosition = 0.0f
                    scrolled_with_zoom = currentPosition * scaleFactor
                    isAutoMove = false
                    autoScrollHandler.removeCallbacks(autoMoveRunnable!!)
                    invalidate()
                    return@Runnable
                }
                scrolled_with_zoom = currentPosition * scaleFactor
                selectedEntity!!.rect.left = f2
                selectedEntity!!.rect.right = f3
                isMove = true
                invalidate()
                autoScrollHandler.postDelayed(moveRunnable, 100L)
            }
        }
        autoMoveRunnable = moveRunnable
    }

    // ── Public API ──────────────────────────────────────────────────

    private fun setupFade(entityAudio: EntityAudio) {
        // Empty — fade is handled by the audio entity
    }

    fun getDefaultScale(): Float = DEFAULT_SCALE

    fun setmIsi3adaTimeline(entityBismilahTimeline: EntityBismilahTimeline?) {
        mIsi3adaTimeline = entityBismilahTimeline
    }

    fun getmIsi3adaTimeline(): EntityBismilahTimeline? = mIsi3adaTimeline

    fun getEntityAudioNotDeleted(i: Int): Pair<Int, EntityAudio>? {
        var idx = i
        while (idx < entityListAudio.size) {
            val entityAudio = entityListAudio[idx]
            if (entityAudio.visible()) {
                return Pair(idx, entityAudio)
            }
            idx++
        }
        return null
    }

    fun clearAudio() {
        if (entityListAudio.isEmpty()) return
        entityListAudio.clear()
        val stack = Stack<Pair<Entity, EntityAction>>()
        val it = entityList.iterator()
        while (it.hasNext()) {
            val next = it.next()
            if (next.first !is EntityAudio) {
                stack.push(next)
            }
        }
        entityList.clear()
        entityList = stack
    }

    fun init(i: Int, i2: Int) {
        if (i <= 0 || i2 <= 0) return
        val f = i.toFloat()
        SPEED = 0.04f * f
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint_time = paint
        paint.color = -8355712
        paint_time!!.typeface = Typeface.createFromAsset(resources.assets, "fonts/ReadexPro_Medium.ttf")
        radius = 0.006f * f
        paint_time!!.textSize = f * 0.023f
        val paint2 = Paint(Paint.ANTI_ALIAS_FLAG)
        paintMaker = paint2
        paint2.color = -1
        paintMaker!!.strokeWidth = radius * 0.5f
        markerHeight = radius * 3.0f
        m_pos_y_marker = paintMaker!!.strokeWidth * 4.0f
        paddingCursur = 4.0f * radius
        centerX = width_screen * 0.5f - radius * 0.5f
        DETECT_RIGHT_MOVE = 0.4f * centerX
        DETECT_LEFT_MOVE = centerX * 0.45f
        val paint3 = Paint(Paint.ANTI_ALIAS_FLAG)
        paintCursur = paint3
        paint3.strokeWidth = radius
        val strokeWidth = paintCursur!!.strokeWidth * 2.8f
        val paint4 = Paint(Paint.ANTI_ALIAS_FLAG)
        paintLineCheck = paint4
        paint4.color = -16121
        paintLineCheck!!.strokeWidth = paintCursur!!.strokeWidth
        paintLineCheck!!.pathEffect = DashPathEffect(floatArrayOf(strokeWidth, strokeWidth), 0.0f)
        w_time_item = paint_time!!.measureText("999") * 0.5f
    }

    private fun drawIconDrawable(canvas: Canvas) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = -14540254
        val width = (canvas.width * 0.015f).toInt()
        val width2 = (canvas.width * 0.104f).toInt()
        val width3 = (canvas.width * 0.03f).toInt()
        val rectF = RectF(width3.toFloat(), start_y_draw.toInt().toFloat(), (width3 + width2).toFloat(), (start_y_draw.toInt() + width2).toFloat())
        canvas.drawRoundRect(rectF, width.toFloat(), width.toFloat(), paint)
        val drawable = ContextCompat.getDrawable(context, R.drawable.add_audio)
        drawable?.setTint(-1052689)
        drawable?.setBounds(rectF.left.toInt(), rectF.top.toInt(), rectF.right.toInt(), rectF.bottom.toInt())
        drawable?.draw(canvas)
        val rectF2 = RectF(rectF.left, rectF.bottom + width3, rectF.right, (start_y_draw.toInt() + width2).toFloat())
        canvas.drawRoundRect(rectF2, width.toFloat(), width.toFloat(), paint)
        val drawable2 = ContextCompat.getDrawable(context, R.drawable.add_quran)
        drawable2?.setTint(-1052689)
        drawable2?.setBounds(rectF2.left.toInt(), rectF2.top.toInt(), rectF2.right.toInt(), rectF2.bottom.toInt())
        drawable2?.draw(canvas)
    }



    fun setSecond_in_screen(f: Float, i: Int, i2: Int) {
        second_in_screen = f
        duration = i
        width_screen = i2
        val f2 = 0.03f * f
        dx = f2
        TOLERANCE_X = f2
        max_trim = f * 0.2f
    }

    fun getTextSize(): Float {
        val paint = paint_time ?: return 1.0f
        return paint.textSize * 1.42f
    }

    override fun onLayout(z: Boolean, i: Int, i2: Int, i3: Int, i4: Int) {
        if (z) updateGestureExclusion()
    }

    private fun updateGestureExclusion() {
        try {
            if (Build.VERSION.SDK_INT >= 29) {
                exclusionRects.clear()
                val systemGestureInsets = rootWindowInsets?.systemGestureInsets ?: return
                val rect = Rect(0, 0, systemGestureInsets.left, height)
                val rect2 = Rect(right - systemGestureInsets.right, 0, right, height)
                exclusionRects.add(rect)
                exclusionRects.add(rect2)
                setSystemGestureExclusionRects(exclusionRects)
            }
        } catch (_: Exception) {
        }
    }

    override fun onWindowSystemUiVisibilityChanged(i: Int) {
        super.onWindowSystemUiVisibilityChanged(i)
        updateGestureExclusion()
    }

    override fun onSizeChanged(i: Int, i2: Int, i3: Int, i4: Int) {
        super.onSizeChanged(i, i2, i3, i4)
        if (i2 < 1 || i < 1) return
        val f = i2.toFloat()
        maxBottom = 0.78f * f
        start_y_draw = 0.18f * f
        canvas_top_Y = 0.1f * f
        posY = 0.05f * f
        p = f * 0.026f
    }

    private fun drawItemBtn(canvas: Canvas) {
        try {
            val audio = getAudio()
            if (audio != null) {
                val f = audio.rect.top
                val width = canvas.width * 0.15f
                val f2 = audio.rect.bottom
                if (rectItemAudio == null) {
                    val rectF = RectF(0.0f, f, width, f2)
                    rectItemAudio = rectF
                    val width2 = rectF.width() * 0.15f
                    val height = rectItemAudio!!.height() * 0.6f
                    val f3 = width - width2
                    val f4 = f3 - height
                    val f5 = height / 2.0f
                    rectSquareAudio = RectF(f4, rectItemAudio!!.centerY() - f5, f3, rectItemAudio!!.centerY() + f5)
                    pathItemAudio = CanvasUtils.drawCustomRoundedRect(canvas, 0.0f, f, width, f2, 100.0f, 100.0f)
                }
                paintItem.color = clr_btn_audio
                canvas.drawPath(pathItemAudio!!, paintItem)
                paintItem.color = Common.COLOR_BLOCK_AUDIO
                canvas.drawRoundRect(rectSquareAudio!!, 2.0f, 2.0f, paintItem)
                val i = (rectItemAudio!!.right - rectSquareAudio!!.right).toInt()
                if (clr_btn_audio != CLR_BTN_DEFAULT) {
                    val drawable = ContextCompat.getDrawable(context, R.drawable.checked_timeline)
                    drawable?.setBounds(i, rectSquareAudio!!.top.toInt(), (i + rectSquareAudio!!.width()).toInt(), rectSquareAudio!!.bottom.toInt())
                    drawable?.draw(canvas)
                }
            }
            val isExist = isExist(bismilahTimeline)
            val isExist2 = isExist(mIsi3adaTimeline)
            if (!isExist && !isExist2) {
                val quran = getQuran()
                if (quran != null) {
                    val f6 = quran.rect.top
                    val width3 = canvas.width * 0.15f
                    val f7 = quran.rect.bottom
                    val rectF2 = rectFItemQuran
                    if (rectF2 == null || rectF2.top != f6) {
                        val rectF3 = RectF(0.0f, f6, width3, f7)
                        rectFItemQuran = rectF3
                        val width4 = rectF3.width() * 0.15f
                        val height2 = rectFItemQuran!!.height() * 0.6f
                        val f8 = width3 - width4
                        val f9 = f8 - height2
                        val f10 = height2 / 2.0f
                        rectSquareQuran = RectF(f9, rectFItemQuran!!.centerY() - f10, f8, rectFItemQuran!!.centerY() + f10)
                        pathItemQuran = CanvasUtils.drawCustomRoundedRect(canvas, 0.0f, f6, width3, f7, 100.0f, 100.0f)
                    }
                    paintItem.color = clr_btn_quran
                    canvas.drawPath(pathItemQuran!!, paintItem)
                    paintItem.color = Common.COLOR_BLOCK_QURAN
                    canvas.drawRoundRect(rectSquareQuran!!, 2.0f, 2.0f, paintItem)
                    if (clr_btn_quran != CLR_BTN_DEFAULT) {
                        val drawable2 = ContextCompat.getDrawable(context, R.drawable.checked_timeline)
                        val i2 = (rectFItemQuran!!.right - rectSquareQuran!!.right).toInt()
                        drawable2?.setBounds(i2, rectSquareQuran!!.top.toInt(), (i2 + rectSquareQuran!!.width()).toInt(), rectSquareQuran!!.bottom.toInt())
                        drawable2?.draw(canvas)
                    }
                }
                val trslQuran = getTrslQuran()
                if (trslQuran != null) {
                    val f11 = trslQuran.rect.top
                    val width5 = canvas.width * 0.15f
                    val f12 = trslQuran.rect.bottom
                    val rectF4 = rectFItemTrslQuran
                    if (rectF4 == null || rectF4.top != f11) {
                        val rectF5 = RectF(0.0f, f11, width5, f12)
                        rectFItemTrslQuran = rectF5
                        val width6 = rectF5.width() * 0.15f
                        val height3 = rectFItemTrslQuran!!.height() * 0.6f
                        val f13 = width5 - width6
                        val f14 = f13 - height3
                        val f15 = height3 / 2.0f
                        rectSquareTrslQuran = RectF(f14, rectFItemTrslQuran!!.centerY() - f15, f13, rectFItemTrslQuran!!.centerY() + f15)
                        pathItemTrslQuran = CanvasUtils.drawCustomRoundedRect(canvas, 0.0f, f11, width5, f12, 100.0f, 100.0f)
                    }
                    paintItem.color = clr_btn_trsl
                    canvas.drawPath(pathItemTrslQuran!!, paintItem)
                    paintItem.color = Common.COLOR_BLOCK_TRANSLATION
                    canvas.drawRoundRect(rectSquareTrslQuran!!, 2.0f, 2.0f, paintItem)
                    if (clr_btn_trsl != CLR_BTN_DEFAULT) {
                        val drawable3 = ContextCompat.getDrawable(context, R.drawable.checked_timeline)
                        val i3 = (rectFItemTrslQuran!!.right - rectSquareTrslQuran!!.right).toInt()
                        drawable3?.setBounds(i3, rectSquareTrslQuran!!.top.toInt(), (i3 + rectSquareTrslQuran!!.width()).toInt(), rectSquareTrslQuran!!.bottom.toInt())
                        drawable3?.draw(canvas)
                    }
                }
            } else {
                val entityBismilahTimeline = mIsi3adaTimeline
                if (entityBismilahTimeline != null) {
                    val f16 = entityBismilahTimeline.rect.top
                    val width7 = canvas.width * 0.15f
                    val f17 = entityBismilahTimeline.rect.bottom
                    val rectF6 = rectFItemQuran
                    if (rectF6 == null || rectF6.top != f16) {
                        val rectF7 = RectF(0.0f, f16, width7, f17)
                        rectFItemQuran = rectF7
                        val width8 = rectF7.width() * 0.15f
                        val height4 = rectFItemQuran!!.height() * 0.6f
                        val f18 = width7 - width8
                        val f19 = f18 - height4
                        val f20 = height4 / 2.0f
                        rectSquareQuran = RectF(f19, rectFItemQuran!!.centerY() - f20, f18, rectFItemQuran!!.centerY() + f20)
                        pathItemQuran = CanvasUtils.drawCustomRoundedRect(canvas, 0.0f, f16, width7, f17, 100.0f, 100.0f)
                    }
                    paintItem.color = clr_btn_quran
                    canvas.drawPath(pathItemQuran!!, paintItem)
                    paintItem.color = Common.COLOR_BLOCK_QURAN
                    canvas.drawRoundRect(rectSquareQuran!!, 2.0f, 2.0f, paintItem)
                    if (clr_btn_quran != CLR_BTN_DEFAULT) {
                        val drawable4 = ContextCompat.getDrawable(context, R.drawable.checked_timeline)
                        val i4 = (rectFItemQuran!!.right - rectSquareQuran!!.right).toInt()
                        drawable4?.setBounds(i4, rectSquareQuran!!.top.toInt(), (i4 + rectSquareQuran!!.width()).toInt(), rectSquareQuran!!.bottom.toInt())
                        drawable4?.draw(canvas)
                    }
                }
                val trslQuran = getTrslQuran()
                if (trslQuran != null) {
                    val f11 = trslQuran.rect.top
                    val width5 = canvas.width * 0.15f
                    val f12 = trslQuran.rect.bottom
                    val rectF4 = rectFItemTrslQuran
                    if (rectF4 == null || rectF4.top != f11) {
                        val rectF5 = RectF(0.0f, f11, width5, f12)
                        rectFItemTrslQuran = rectF5
                        val width6 = rectF5.width() * 0.15f
                        val height3 = rectFItemTrslQuran!!.height() * 0.6f
                        val f13 = width5 - width6
                        val f14 = f13 - height3
                        val f15 = height3 / 2.0f
                        rectSquareTrslQuran = RectF(f14, rectFItemTrslQuran!!.centerY() - f15, f13, rectFItemTrslQuran!!.centerY() + f15)
                        pathItemTrslQuran = CanvasUtils.drawCustomRoundedRect(canvas, 0.0f, f11, width5, f12, 100.0f, 100.0f)
                    }
                    paintItem.color = clr_btn_trsl
                    canvas.drawPath(pathItemTrslQuran!!, paintItem)
                    paintItem.color = Common.COLOR_BLOCK_TRANSLATION
                    canvas.drawRoundRect(rectSquareTrslQuran!!, 2.0f, 2.0f, paintItem)
                    if (clr_btn_trsl != CLR_BTN_DEFAULT) {
                        val drawable3 = ContextCompat.getDrawable(context, R.drawable.checked_timeline)
                        val i3 = (rectFItemTrslQuran!!.right - rectSquareTrslQuran!!.right).toInt()
                        drawable3?.setBounds(i3, rectSquareTrslQuran!!.top.toInt(), (i3 + rectSquareTrslQuran!!.width()).toInt(), rectSquareTrslQuran!!.bottom.toInt())
                        drawable3?.draw(canvas)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("mException", "drawItemBtn")
        }
    }

    override fun onDraw(canvas: Canvas) {
        if (paint_time == null || isProgress) return
        try {
            mDraw(canvas)
            if (!isPlaying) {
                drawItemBtn(canvas)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.onDraw(canvas)
    }

    val second_in_screenNoScale: Float get() = second_in_screen

    fun getSecond_in_screen(): Float = second_in_screen * scaleFactor

    fun setSecond_in_screen(f: Float) {
        second_in_screen = f
        dx = 0.03f * f
        max_trim = f * 0.2f
    }

    private fun mDraw(canvas: Canvas) {
        canvas.drawColor(-15658735)
        canvas.save()
        val secondInScreen = getSecond_in_screen()
        canvas.translate(centerX + scrolled_with_zoom, paddingTop.toFloat())
        var abs1 = ((abs(scrolled_with_zoom) - centerX) / secondInScreen).toInt()
        val abs2 = ((abs(scrolled_with_zoom) + centerX) / secondInScreen).toInt() + 1
        if (abs1 < 0) abs1 = 0
        drawTimeBar(canvas, abs1, abs2, secondInScreen)
        canvas.clipRect(-second_in_screen, canvas_top_Y, width - scrolled_with_zoom, height - mScrollY)
        canvas.translate(0.0f, mScrollY)
        drawAllEntities(canvas, abs1, abs2)
        if (isCheckLine) {
            canvas.drawLine(startXLine, 0.0f, startXLine, height - mScrollY, paintLineCheck!!)
        }
        canvas.restore()
        if (isCheckLineCursur) {
            paintCursur!!.color = paintLineCheck!!.color
            canvas.drawLine(centerX + paintMaker!!.strokeWidth, posY + m_pos_y_marker + paintMaker!!.strokeWidth, centerX, height.toFloat(), paintCursur!!)
        } else {
            paintCursur!!.color = -1
            canvas.drawLine(centerX + paintMaker!!.strokeWidth, posY + m_pos_y_marker + paintMaker!!.strokeWidth, centerX, height.toFloat(), paintCursur!!)
        }
    }

    private fun drawMarker(canvas: Canvas, f: Float, f2: Float) {
        val strokeWidth = f + paintMaker!!.strokeWidth
        val f3 = posY + m_pos_y_marker
        canvas.drawLine(strokeWidth, f3, strokeWidth, f3 + f2, paintMaker!!)
    }

    fun setiTrimLineCallback(callback: ITrimLineCallback?) {
        iTrimLineCallback = callback
    }

    private fun drawTimeBar(canvas: Canvas, start: Int, end: Int, secondInScreen: Float) {
        val f2 = scaleFactor
        var f3 = 4.0f
        if (f2 >= 4.0f) {
            f3 = 0.25f
        } else if (f2 >= 2.0f) {
            f3 = 0.5f
        } else if (f2 >= 0.8f) {
            f3 = 2.0f
        } else if (f2 < 0.4f) {
            f3 = if (f2 > 0.25f) 6.0f else 8.0f
        }
        val f4 = start.toFloat()
        val f5 = secondInScreen * f3 * 0.2f
        var f6 = f4 - f4 % f3
        while (f6 <= end) {
            val f7 = f6 * secondInScreen
            val f8 = f7 / secondInScreen
            drawMarker(canvas, f7, markerHeight)
            val label = if (isArabic_lang) formatTimeLabelArabic(f8) else formatTimeLabel(f8)
            canvas.drawText(label, f7 - w_time_item, posY, paint_time!!)
            for (i3 in 1..4) {
                drawMarker(canvas, i3 * f5 + f7, markerHeight / 2.0f)
            }
            f6 += f3
        }
    }

    private fun formatTimeLabel(f: Float): String {
        if (f < 60.0f) {
            if (abs(f - 14.0f) < 0.01) return String.format(Locale.ENGLISH, "14s")
            if (abs(f - round(f)) < 0.01) return String.format(Locale.ENGLISH, "%ds", f.toInt())
            return String.format(Locale.ENGLISH, "%.2fs", f)
        }
        val i = (f / 60.0f).toInt()
        val round = round(f % 60.0f).toInt()
        return if (round == 0) String.format(Locale.ENGLISH, "%dm", i)
        else String.format(Locale.ENGLISH, "%dm %ds", i, round)
    }

    private fun formatTimeLabelArabic(f: Float): String {
        if (f < 60.0f) {
            if (abs(f - 14.0f) < 0.01) return String.format(Locale.ENGLISH, "14ث")
            if (abs(f - round(f)) < 0.01) return String.format(Locale.ENGLISH, "%dث", f.toInt())
            return String.format(Locale.ENGLISH, "%.2fث", f)
        }
        val i = (f / 60.0f).toInt()
        val round = round(f % 60.0f).toInt()
        return if (round == 0) String.format(Locale.ENGLISH, "%dد", i)
        else String.format(Locale.ENGLISH, "%dد %dث", i, round)
    }

    fun getCurrentPosition(): Float = scrolled_with_zoom



    fun isExist(entityBismilahTimeline: EntityBismilahTimeline?): Boolean {
        return entityBismilahTimeline != null && entityBismilahTimeline.visible()
    }

    private fun drawBasmala(canvas: Canvas, rectF: RectF): Float {
        var f: Float
        if (isExist(bismilahTimeline)) {
            bismilahTimeline!!.updateRect(scaleFactor)
            if (bismilahTimeline!!.getEntityView() != null) {
                if (bismilahTimeline!!.getEntityView()!!.isVisible) {
                    if (round(getCurrentPosition() + bismilahTimeline!!.rect.left) > 0.0f || round(getCurrentPosition() + bismilahTimeline!!.rect.right) <= 0.0f) {
                        bismilahTimeline!!.getEntityView()!!.isVisible = false
                        bismilahTimeline!!.quranEntity.endAnimator()
                        iTrimLineCallback!!.onUpdate()
                    } else {
                        setupAnimation(bismilahTimeline!!.quranEntity)
                    }
                } else if (round(bismilahTimeline!!.rect.left + getCurrentPosition()) <= 0.0f && round(bismilahTimeline!!.rect.right + getCurrentPosition()) > 0.0f) {
                    setupAnimation(bismilahTimeline!!.quranEntity)
                    bismilahTimeline!!.getEntityView()!!.isVisible = true
                    iTrimLineCallback!!.onUpdate()
                }
            }
            bismilahTimeline!!.setY(entityY)
            if (RectF.intersects(rectF, bismilahTimeline!!.rect)) {
                bismilahTimeline!!.update(canvas)
            }
            f = bismilahTimeline!!.rect.bottom
        } else {
            f = 0.0f
        }
        if (!isExist(mIsi3adaTimeline)) return f
        mIsi3adaTimeline!!.updateRect(scaleFactor)
        if (mIsi3adaTimeline!!.getEntityView() != null) {
            if (mIsi3adaTimeline!!.getEntityView()!!.isVisible) {
                if (round(getCurrentPosition() + mIsi3adaTimeline!!.rect.left) > 0.0f || round(getCurrentPosition() + mIsi3adaTimeline!!.rect.right) <= 0.0f) {
                    mIsi3adaTimeline!!.getEntityView()!!.isVisible = false
                    mIsi3adaTimeline!!.quranEntity.endAnimator()
                    iTrimLineCallback!!.onUpdate()
                } else {
                    setupAnimation(mIsi3adaTimeline!!.quranEntity)
                }
            } else if (round(mIsi3adaTimeline!!.rect.left + getCurrentPosition()) <= 0.0f && round(mIsi3adaTimeline!!.rect.right + getCurrentPosition()) > 0.0f) {
                setupAnimation(mIsi3adaTimeline!!.quranEntity)
                mIsi3adaTimeline!!.getEntityView()!!.isVisible = true
                iTrimLineCallback!!.onUpdate()
            }
        }
        mIsi3adaTimeline!!.setY(entityY)
        if (RectF.intersects(rectF, mIsi3adaTimeline!!.rect)) {
            mIsi3adaTimeline!!.update(canvas)
        }
        return mIsi3adaTimeline!!.rect.bottom
    }

    private fun drawAllEntities(canvas: Canvas, start: Int, end: Int) {
        var f7 = start_y_draw
        entityY = f7
        val f8 = scrolled_with_zoom
        val f9 = centerX
        val rectF = RectF(-f8 - f9, -mScrollY + entityY, -f8 + f9, canvas.height - mScrollY.toFloat())
        for (i3 in entityListAudio.indices) {
            val entityAudio = entityListAudio[i3]
            if (entityAudio.visible()) {
                if (selectedEntity === entityAudio && !isPlaying) {
                    selectedEntity!!.setY(entityY)
                    selectedEntity!!.updateRect(scaleFactor)
                    f7 = entityAudio.rect.bottom + p
                } else {
                    entityAudio.updateRect(scaleFactor)
                    if (entityAudio.isVisible) {
                        if (round(getCurrentPosition() + entityAudio.rect.left) > 0.0f || round(getCurrentPosition() + entityAudio.rect.right) <= 0.0f) {
                            entityAudio.isVisible = false
                        } else {
                            setupFade(entityAudio)
                        }
                    } else if (round(entityAudio.rect.left + getCurrentPosition()) <= 0.0f && round(entityAudio.rect.right + getCurrentPosition()) > 0.0f) {
                        setupFade(entityAudio)
                        entityAudio.isVisible = true
                        iTrimLineCallback?.onUpdatePlayerAudio(entityAudio)
                    }
                    entityAudio.setY(entityY)
                    if (RectF.intersects(rectF, entityAudio.rect)) {
                        entityAudio.update(canvas)
                    }
                    f7 = entityAudio.rect.bottom + p
                }
            }
        }
        entityY = f7
        var maxVal = max(start_y_draw, drawBasmala(canvas, rectF) + p)
        for (i4 in entityListQuran.indices) {
            val eqt = entityListQuran[i4]
            if (eqt.visible()) {
                if (selectedEntity === eqt && !isPlaying) {
                    eqt.updateRect(scaleFactor)
                    selectedEntity!!.setY(entityY)
                    maxVal = eqt.rect.bottom + p
                } else {
                    eqt.updateRect(scaleFactor)
                    if (eqt.getEntityView() != null) {
                        if (eqt.getEntityView()!!.isVisible) {
                            if (round(getCurrentPosition() + eqt.rect.left) > 0.0f || round(getCurrentPosition() + eqt.rect.right) <= 0.0f) {
                                eqt.getEntityView()!!.isVisible = false
                                eqt.quranEntity.endAnimator()
                                iTrimLineCallback?.onUpdate()
                            } else {
                                setupAnimation(eqt.quranEntity)
                            }
                        } else if (round(eqt.rect.left + getCurrentPosition()) <= 0.0f && round(eqt.rect.right + getCurrentPosition()) > 0.0f) {
                            setupAnimation(eqt.quranEntity)
                            eqt.getEntityView()!!.isVisible = true
                            iTrimLineCallback?.onUpdate()
                        }
                    }
                    eqt.setY(entityY)
                    if (RectF.intersects(rectF, eqt.rect)) {
                        eqt.update(canvas)
                    }
                    maxVal = eqt.rect.bottom + p
                }
            }
        }
        entityY = maxVal
        for (i5 in entityListTrslQuran.indices) {
            val etl = entityListTrslQuran[i5]
            if (etl.visible()) {
                if (selectedEntity === etl && !isPlaying) {
                    etl.updateRect(scaleFactor)
                    selectedEntity!!.setY(entityY)
                    maxVal = etl.rect.bottom + p
                } else {
                    etl.updateRect(scaleFactor)
                    if (etl.getEntityView() != null) {
                        if (etl.getEntityView()!!.isVisible) {
                            if (round(getCurrentPosition() + etl.rect.left) > 0.0f || round(getCurrentPosition() + etl.rect.right) <= 0.0f) {
                                etl.getEntityView()!!.isVisible = false
                                etl.quranEntity.endAnimator()
                                iTrimLineCallback?.onUpdate()
                            }
                        } else if (round(etl.rect.left + getCurrentPosition()) <= 0.0f && round(etl.rect.right + getCurrentPosition()) > 0.0f) {
                            etl.getEntityView()!!.isVisible = true
                            iTrimLineCallback?.onUpdate()
                        }
                    }
                    etl.setY(entityY)
                    if (RectF.intersects(rectF, etl.rect)) {
                        etl.update(canvas)
                    }
                    maxVal = etl.rect.bottom + p
                }
            }
        }
        entityY = maxVal
        if (selectedEntity == null || isPlaying || !selectedEntity!!.visible()) return
        if (RectF.intersects(rectF, selectedEntity!!.rect)) {
            val entity = selectedEntity!!
            if (entity is EntityAudio) {
                if (round(entity.rect.left + getCurrentPosition()) <= 0.0f && round(selectedEntity!!.rect.right + getCurrentPosition()) > 0.0f) {
                    selectedEntity!!.isVisible = true
                } else {
                    selectedEntity!!.isVisible = false
                }
            } else if (entity.getEntityView() != null) {
                if (round(selectedEntity!!.rect.left + getCurrentPosition()) <= 0.0f && round(selectedEntity!!.rect.right + getCurrentPosition()) > 0.0f) {
                    selectedEntity!!.getEntityView()!!.endAnimator()
                    if (!selectedEntity!!.getEntityView()!!.isVisible) {
                        selectedEntity!!.getEntityView()!!.isVisible = true
                        iTrimLineCallback?.onUpdate()
                    }
                } else if (selectedEntity!!.getEntityView()!!.isVisible) {
                    selectedEntity!!.getEntityView()!!.endAnimator()
                    selectedEntity!!.getEntityView()!!.isVisible = false
                    iTrimLineCallback?.onUpdate()
                }
            }
            selectedEntity!!.update(canvas, start, end)
            return
        }
        if (selectedEntity!!.getEntityView() == null || !selectedEntity!!.getEntityView()!!.isVisible) return
        selectedEntity!!.getEntityView()!!.endAnimator()
        selectedEntity!!.getEntityView()!!.isVisible = false
        iTrimLineCallback?.onUpdate()
    }

    private fun setupAnimation(quranEntity: QuranEntity) {
        if (!isPlaying || quranEntity.entityQuran?.getTransition() == null || quranEntity.isAnimRun()) return
        val transition = quranEntity.entityQuran?.getTransition() ?: return
        val abs = abs(round(getCurrentPosition() / getSecond_in_screen() * 1000.0f))
        if (transition.isIn) {
            val round = round(quranEntity.entityQuran!!.rect.left / getSecond_in_screen() * 1000.0f).toInt()
            val durationIn = (transition.duration_in * 1000.0f).toInt()
            val f = round.toFloat()
            if (abs < durationIn * 0.5f + f) {
                quranEntity.runIn(durationIn, false, transition.type_in)
            } else if (!transition.isOut && (abs < f || abs >= round + durationIn)) {
                quranEntity.endAnimator()
            }
        }
        if (!quranEntity.isAnimRun() && transition.isOut) {
            val secondInScreen = round(quranEntity.entityQuran!!.rect.right / getSecond_in_screen() * 1000.0f).toInt()
            val durationOut = (transition.duration_out * 1000.0f).toInt()
            val f2 = secondInScreen - durationOut
            val f3 = durationOut * 0.5f + f2
            if (abs >= f2 && abs < f3) {
                quranEntity.runOut(durationOut, false, transition.type_out)
            } else if (abs >= secondInScreen) {
                quranEntity.endAnimator()
            }
        }
    }

    private fun setupAnimation(bismilahEntity: BismilahEntity) {
        if (!isPlaying || bismilahEntity.bismilahTimeline?.getTransition() == null || bismilahEntity.isAnimRun()) return
        val transition = bismilahEntity.bismilahTimeline?.getTransition() ?: return
        val abs = abs(round(getCurrentPosition() / getSecond_in_screen() * 1000.0f))
        if (transition.isIn) {
            val round = round(bismilahEntity.bismilahTimeline!!.rect.left / getSecond_in_screen() * 1000.0f).toInt()
            val durationIn = (transition.duration_in * 1000.0f).toInt()
            val f = round.toFloat()
            if (abs < durationIn * 0.5f + f) {
                bismilahEntity.runIn(durationIn, false, transition.type_in)
            } else if (!transition.isOut && (abs < f || abs >= round + durationIn)) {
                bismilahEntity.endAnimator()
            }
        }
        if (!bismilahEntity.isAnimRun() && transition.isOut) {
            val secondInScreen = round(bismilahEntity.bismilahTimeline!!.rect.right / getSecond_in_screen() * 1000.0f).toInt()
            val durationOut = (transition.duration_out * 1000.0f).toInt()
            val f2 = secondInScreen - durationOut
            val f3 = durationOut * 0.5f + f2
            if (abs >= f2 && abs < f3) {
                bismilahEntity.runOut(durationOut, false, transition.type_out)
            } else if (abs >= secondInScreen) {
                bismilahEntity.endAnimator()
            }
        }
    }

    fun translateFromNow() {
        val secondInScreen = getSecond_in_screen() * 0.5f
        val entity = selectedEntity ?: return
        if (entity is EntityQuranTimeline) {
            val eqt = entity
            var absVal = abs(getCurrentPosition())
            if (eqt.rect.right - absVal < secondInScreen) return
            if (eqt.index - 1 >= 0) {
                val prev = getPreviewOrNextEntityQuran(entityListQuran, eqt.index - 1, false)
                if (prev != null) {
                    if (absVal < prev.rect.left + getSecond_in_screen()) {
                        absVal = getSecond_in_screen() + prev.rect.left
                    }
                    eqt.setCurrentRect()
                    eqt.setX(absVal)
                    if (eqt.rect.left < prev.rect.right) {
                        prev.setCurrentRect()
                        prev.right = eqt.rect.left
                        prev.onChange()
                        entityList.push(Pair(prev, EntityAction.MOVE))
                        iTrimLineCallback?.onAddStack(EntityAction.MOVE)
                    }
                    invalidate()
                    selectedEntity!!.onChange()
                    entityList.push(Pair(selectedEntity!!, EntityAction.TRIM))
                    iTrimLineCallback?.onAddStack(EntityAction.TRIM)
                    return
                }
            }
            if (isExist(bismilahTimeline)) {
                if (absVal < bismilahTimeline!!.rect.left + getSecond_in_screen()) {
                    absVal = bismilahTimeline!!.rect.left + getSecond_in_screen()
                }
                eqt.setCurrentRect()
                eqt.setX(absVal)
                if (eqt.rect.left < bismilahTimeline!!.rect.right) {
                    bismilahTimeline!!.setCurrentRect()
                    bismilahTimeline!!.right = eqt.rect.left
                    bismilahTimeline!!.onChange()
                    entityList.push(Pair(bismilahTimeline!!, EntityAction.MOVE))
                    iTrimLineCallback?.onAddStack(EntityAction.MOVE)
                }
                invalidate()
                selectedEntity!!.onChange()
                entityList.push(Pair(selectedEntity!!, EntityAction.TRIM))
                iTrimLineCallback?.onAddStack(EntityAction.TRIM)
                return
            }
            if (isExist(mIsi3adaTimeline)) {
                if (absVal < mIsi3adaTimeline!!.rect.left + getSecond_in_screen()) {
                    absVal = mIsi3adaTimeline!!.rect.left + getSecond_in_screen()
                }
                eqt.setCurrentRect()
                eqt.setX(absVal)
                if (eqt.rect.left < mIsi3adaTimeline!!.rect.right) {
                    mIsi3adaTimeline!!.setCurrentRect()
                    mIsi3adaTimeline!!.right = eqt.rect.left
                    mIsi3adaTimeline!!.onChange()
                    entityList.push(Pair(mIsi3adaTimeline!!, EntityAction.MOVE))
                    iTrimLineCallback?.onAddStack(EntityAction.MOVE)
                }
                invalidate()
                selectedEntity!!.onChange()
                entityList.push(Pair(selectedEntity!!, EntityAction.TRIM))
                iTrimLineCallback?.onAddStack(EntityAction.TRIM)
                return
            }
            eqt.setCurrentRect()
            selectedEntity!!.setX(absVal)
            invalidate()
            selectedEntity!!.onChange()
            entityList.push(Pair(selectedEntity!!, EntityAction.TRIM))
            iTrimLineCallback?.onAddStack(EntityAction.TRIM)
            return
        }
        if (entity is EntityBismilahTimeline) {
            val ebt = entity
            var absVal = abs(getCurrentPosition())
            if (ebt.rect.right - absVal < secondInScreen) return
            ebt.setCurrentRect()
            selectedEntity!!.setX(absVal)
            if (entity === bismilahTimeline && mIsi3adaTimeline != null && bismilahTimeline!!.rect.left < mIsi3adaTimeline!!.rect.right) {
                mIsi3adaTimeline!!.setCurrentRect()
                mIsi3adaTimeline!!.right = ebt.rect.left
                mIsi3adaTimeline!!.onChange()
                entityList.push(Pair(mIsi3adaTimeline!!, EntityAction.MOVE))
                iTrimLineCallback?.onAddStack(EntityAction.MOVE)
            }
            invalidate()
            selectedEntity!!.onChange()
            entityList.push(Pair(selectedEntity!!, EntityAction.TRIM))
            iTrimLineCallback?.onAddStack(EntityAction.TRIM)
            return
        }
        if (entity is EntityTrslTimeline) {
            val etl = entity
            var absVal = abs(getCurrentPosition())
            if (etl.rect.right - absVal < secondInScreen) return
            if (etl.index - 1 >= 0) {
                val prev = getPreviewOrNextEntityTrslQuran(entityListTrslQuran, etl.index - 1, false)
                if (prev != null) {
                    if (absVal < prev.rect.left + getSecond_in_screen()) {
                        absVal = getSecond_in_screen() + prev.rect.left
                    }
                    etl.setCurrentRect()
                    etl.setX(absVal)
                    if (etl.rect.left < prev.rect.right) {
                        prev.setCurrentRect()
                        prev.right = etl.rect.left
                        prev.onChange()
                        entityList.push(Pair(prev, EntityAction.MOVE))
                        iTrimLineCallback?.onAddStack(EntityAction.MOVE)
                    }
                    invalidate()
                    selectedEntity!!.onChange()
                    entityList.push(Pair(selectedEntity!!, EntityAction.TRIM))
                    iTrimLineCallback?.onAddStack(EntityAction.TRIM)
                    return
                }
            }
            etl.setCurrentRect()
            selectedEntity!!.setX(absVal)
            invalidate()
            selectedEntity!!.onChange()
            entityList.push(Pair(selectedEntity!!, EntityAction.TRIM))
            iTrimLineCallback?.onAddStack(EntityAction.TRIM)
        }
    }

    fun translateToRight(isIsi3ada: Boolean) {
        val ebt: EntityBismilahTimeline = if (isIsi3ada) mIsi3adaTimeline!! else bismilahTimeline ?: return
        val f = ebt.rect.right
        if (isIsi3ada && isExist(bismilahTimeline) && f >= bismilahTimeline!!.rect.left) {
            val bisml = bismilahTimeline!!
            val width = bisml.rect.width() + f
            val f2 = f - bisml.rect.left
            bisml.setCurrentRect()
            bisml.setX(f)
            bisml.right = width
            for (index in bisml.index until entityListQuran.size) {
                val eqt = entityListQuran[index]
                if (eqt.visible()) {
                    val f3 = eqt.rect.left + f2
                    val width2 = eqt.rect.width() + f3
                    eqt.setCurrentRect()
                    eqt.setX(f3)
                    eqt.right = width2
                }
            }
            return
        }
        val next = getPreviewOrNextEntityQuran(entityListQuran, ebt.index, true) ?: return
        if (f >= next.rect.left) {
            val f4 = f - next.rect.left
            val width3 = next.rect.width() + f
            next.setCurrentRect()
            next.setX(f)
            next.right = width3
            for (index2 in ebt.index + 1 until entityListQuran.size) {
                val eqt2 = entityListQuran[index2]
                if (eqt2.visible()) {
                    val f5 = eqt2.rect.left + f4
                    val width4 = eqt2.rect.width() + f5
                    eqt2.setCurrentRect()
                    eqt2.setX(f5)
                    eqt2.right = width4
                }
            }
        }
    }

    fun translateToRight() {
        val ebt = bismilahTimeline ?: return
        val f = ebt.rect.right
        val next = getPreviewOrNextEntityQuran(entityListQuran, ebt.index, true) ?: return
        if (f >= next.rect.left) {
            val f2 = f - next.rect.left
            val width = next.rect.width() + f
            next.setCurrentRect()
            next.setX(f)
            next.right = width
            for (index in ebt.index + 1 until entityListQuran.size) {
                val eqt = entityListQuran[index]
                if (eqt.visible()) {
                    val f3 = eqt.rect.left + f2
                    val width2 = eqt.rect.width() + f3
                    eqt.setCurrentRect()
                    eqt.setX(f3)
                    eqt.right = width2
                }
            }
        }
    }

    fun translateFromStart() {
        val entity = selectedEntity ?: return
        if (entity is EntityQuranTimeline) {
            val eqt = entity
            if (eqt.index - 1 >= 0) {
                val prev = getPreviewOrNextEntityQuran(entityListQuran, eqt.index - 1, false)
                if (prev != null) {
                    eqt.setCurrentRect()
                    eqt.setX(prev.rect.right)
                    invalidate()
                    selectedEntity!!.onChange()
                    entityList.push(Pair(selectedEntity!!, EntityAction.TRIM))
                    iTrimLineCallback?.onAddStack(EntityAction.TRIM)
                    return
                }
            }
            if (isExist(bismilahTimeline)) {
                val bisml = bismilahTimeline!!
                eqt.setCurrentRect()
                eqt.setX(bisml.rect.right)
                invalidate()
                selectedEntity!!.onChange()
                entityList.push(Pair(selectedEntity!!, EntityAction.TRIM))
                iTrimLineCallback?.onAddStack(EntityAction.TRIM)
                return
            }
            if (isExist(mIsi3adaTimeline)) {
                val isi3ada = mIsi3adaTimeline!!
                eqt.setCurrentRect()
                eqt.setX(isi3ada.rect.right)
                invalidate()
                selectedEntity!!.onChange()
                entityList.push(Pair(selectedEntity!!, EntityAction.TRIM))
                iTrimLineCallback?.onAddStack(EntityAction.TRIM)
                return
            }
            eqt.setCurrentRect()
            selectedEntity!!.setX(0.0f)
            invalidate()
            selectedEntity!!.onChange()
            entityList.push(Pair(selectedEntity!!, EntityAction.TRIM))
            iTrimLineCallback?.onAddStack(EntityAction.TRIM)
            return
        }
        if (entity is EntityBismilahTimeline) {
            val ebt = entity
            if (entity === bismilahTimeline && isExist(mIsi3adaTimeline)) {
                val isi3ada = mIsi3adaTimeline!!
                ebt.setCurrentRect()
                ebt.setX(isi3ada.rect.right)
                invalidate()
                selectedEntity!!.onChange()
                entityList.push(Pair(selectedEntity!!, EntityAction.TRIM))
                iTrimLineCallback?.onAddStack(EntityAction.TRIM)
                return
            }
            ebt.setCurrentRect()
            selectedEntity!!.setX(0.0f)
            invalidate()
            selectedEntity!!.onChange()
            entityList.push(Pair(selectedEntity!!, EntityAction.TRIM))
            iTrimLineCallback?.onAddStack(EntityAction.TRIM)
            return
        }
        if (entity is EntityTrslTimeline) {
            val etl = entity
            if (etl.index - 1 >= 0) {
                val prev = getPreviewOrNextEntityTrslQuran(entityListTrslQuran, etl.index - 1, false)
                if (prev != null) {
                    etl.setCurrentRect()
                    etl.setX(prev.rect.right)
                    invalidate()
                    selectedEntity!!.onChange()
                    entityList.push(Pair(selectedEntity!!, EntityAction.TRIM))
                    iTrimLineCallback?.onAddStack(EntityAction.TRIM)
                    return
                }
            }
            etl.setCurrentRect()
            selectedEntity!!.setX(0.0f)
            invalidate()
            selectedEntity!!.onChange()
            entityList.push(Pair(selectedEntity!!, EntityAction.TRIM))
            iTrimLineCallback?.onAddStack(EntityAction.TRIM)
        }
    }

    fun translateUntilNow() {
        val secondInScreen = getSecond_in_screen() * 0.5f
        val entity = selectedEntity ?: return
        if (entity is EntityQuranTimeline) {
            val eqt = entity
            var absVal = abs(getCurrentPosition())
            if (absVal - eqt.rect.left < secondInScreen) return
            if (eqt.index + 1 < entityListQuran.size) {
                val next = getPreviewOrNextEntityQuran(entityListQuran, eqt.index + 1, true)
                if (next != null) {
                    eqt.setCurrentRect()
                    eqt.right = absVal
                    if (eqt.rect.right > next.rect.left) {
                        val width = eqt.rect.right + next.rect.width()
                        val f = eqt.rect.right - next.rect.left
                        next.setCurrentRect()
                        next.setX(eqt.rect.right)
                        next.right = width
                        next.onChange()
                        entityList.push(Pair(next, EntityAction.MOVE))
                        iTrimLineCallback?.onAddStack(EntityAction.MOVE)
                        for (index in eqt.index + 2 until entityListQuran.size) {
                            val eqt2 = entityListQuran[index]
                            eqt2.setCurrentRect()
                            val f2 = eqt2.rect.left + f
                            val width2 = eqt2.rect.width() + f2
                            eqt2.setX(f2)
                            eqt2.right = width2
                            invalidate()
                            eqt2.onChange()
                            entityList.push(Pair(eqt2, EntityAction.MOVE))
                            iTrimLineCallback?.onAddStack(EntityAction.MOVE)
                        }
                    }
                    invalidate()
                    selectedEntity!!.onChange()
                    entityList.push(Pair(selectedEntity!!, EntityAction.TRIM))
                    iTrimLineCallback?.onAddStack(EntityAction.TRIM)
                    return
                }
            }
            eqt.setCurrentRect()
            selectedEntity!!.right = absVal
            invalidate()
            selectedEntity!!.onChange()
            entityList.push(Pair(selectedEntity!!, EntityAction.TRIM))
            iTrimLineCallback?.onAddStack(EntityAction.TRIM)
            return
        }
        if (entity is EntityBismilahTimeline) {
            val ebt = entity
            var absVal = abs(getCurrentPosition())
            if (absVal - ebt.rect.left < secondInScreen) return
            if (ebt === mIsi3adaTimeline && isExist(bismilahTimeline)) {
                ebt.setCurrentRect()
                ebt.right = absVal
                if (ebt.rect.right > bismilahTimeline!!.rect.left) {
                    val width = ebt.rect.right + bismilahTimeline!!.rect.width()
                    val f = ebt.rect.right - bismilahTimeline!!.rect.left
                    bismilahTimeline!!.setCurrentRect()
                    bismilahTimeline!!.setX(ebt.rect.right)
                    bismilahTimeline!!.right = width
                    bismilahTimeline!!.onChange()
                    entityList.push(Pair(bismilahTimeline!!, EntityAction.MOVE))
                    iTrimLineCallback?.onAddStack(EntityAction.MOVE)
                    for (index in bismilahTimeline!!.index until entityListQuran.size) {
                        val eqt3 = entityListQuran[index]
                        eqt3.setCurrentRect()
                        val f4 = eqt3.rect.left + f
                        val width4 = eqt3.rect.width() + f4
                        eqt3.setX(f4)
                        eqt3.right = width4
                        invalidate()
                        eqt3.onChange()
                        entityList.push(Pair(eqt3, EntityAction.MOVE))
                        iTrimLineCallback?.onAddStack(EntityAction.MOVE)
                    }
                }
                invalidate()
                selectedEntity!!.onChange()
                entityList.push(Pair(selectedEntity!!, EntityAction.TRIM))
                iTrimLineCallback?.onAddStack(EntityAction.TRIM)
                return
            }
            if (ebt.index < entityListQuran.size) {
                val next = getPreviewOrNextEntityQuran(entityListQuran, ebt.index, true)
                if (next != null) {
                    ebt.setCurrentRect()
                    ebt.right = absVal
                    if (ebt.rect.right > next.rect.left) {
                        val width = ebt.rect.right + next.rect.width()
                        val f = ebt.rect.right - next.rect.left
                        next.setCurrentRect()
                        next.setX(ebt.rect.right)
                        next.right = width
                        next.onChange()
                        entityList.push(Pair(next, EntityAction.MOVE))
                        iTrimLineCallback?.onAddStack(EntityAction.MOVE)
                        for (index in ebt.index + 1 until entityListQuran.size) {
                            val eqt4 = entityListQuran[index]
                            eqt4.setCurrentRect()
                            val f6 = eqt4.rect.left + f
                            val width6 = eqt4.rect.width() + f6
                            eqt4.setX(f6)
                            eqt4.right = width6
                            invalidate()
                            eqt4.onChange()
                            entityList.push(Pair(eqt4, EntityAction.MOVE))
                            iTrimLineCallback?.onAddStack(EntityAction.MOVE)
                        }
                    }
                    invalidate()
                    selectedEntity!!.onChange()
                    entityList.push(Pair(selectedEntity!!, EntityAction.TRIM))
                    iTrimLineCallback?.onAddStack(EntityAction.TRIM)
                    return
                }
            }
            ebt.setCurrentRect()
            selectedEntity!!.right = absVal
            invalidate()
            selectedEntity!!.onChange()
            entityList.push(Pair(selectedEntity!!, EntityAction.TRIM))
            iTrimLineCallback?.onAddStack(EntityAction.TRIM)
            return
        }
        if (entity is EntityTrslTimeline) {
            val etl = entity
            var absVal = abs(getCurrentPosition())
            if (absVal - etl.rect.left < secondInScreen) return
            if (etl.index + 1 < entityListTrslQuran.size) {
                val next = getPreviewOrNextEntityTrslQuran(entityListTrslQuran, etl.index + 1, true)
                if (next != null) {
                    etl.setCurrentRect()
                    etl.right = absVal
                    if (etl.rect.right > next.rect.left) {
                        val width = etl.rect.right + next.rect.width()
                        val f = etl.rect.right - next.rect.left
                        next.setCurrentRect()
                        next.setX(etl.rect.right)
                        next.right = width
                        next.onChange()
                        entityList.push(Pair(next, EntityAction.MOVE))
                        iTrimLineCallback?.onAddStack(EntityAction.MOVE)
                        for (index in etl.index + 2 until entityListTrslQuran.size) {
                            val etl2 = entityListTrslQuran[index]
                            etl2.setCurrentRect()
                            val f8 = etl2.rect.left + f
                            val width8 = etl2.rect.width() + f8
                            etl2.setX(f8)
                            etl2.right = width8
                            invalidate()
                            etl2.onChange()
                            entityList.push(Pair(etl2, EntityAction.MOVE))
                            iTrimLineCallback?.onAddStack(EntityAction.MOVE)
                        }
                    }
                    invalidate()
                    selectedEntity!!.onChange()
                    entityList.push(Pair(selectedEntity!!, EntityAction.TRIM))
                    iTrimLineCallback?.onAddStack(EntityAction.TRIM)
                    return
                }
            }
            etl.setCurrentRect()
            selectedEntity!!.right = absVal
            invalidate()
            selectedEntity!!.onChange()
            entityList.push(Pair(selectedEntity!!, EntityAction.TRIM))
            iTrimLineCallback?.onAddStack(EntityAction.TRIM)
        }
    }

    fun translateToRightBismilah(entityBismilahTimeline: EntityBismilahTimeline) {
        if (abs(getCurrentPosition()) - entityBismilahTimeline.rect.left < second_in_screen && entityBismilahTimeline.index < entityListQuran.size) {
            val next = getPreviewOrNextEntityQuran(entityListQuran, entityBismilahTimeline.index, true)
            if (next != null && entityBismilahTimeline.rect.right > next.rect.left) {
                val width = entityBismilahTimeline.rect.right + next.rect.width()
                val f = entityBismilahTimeline.rect.right - next.rect.left
                next.setCurrentRect()
                next.setX(entityBismilahTimeline.rect.right)
                next.right = width
                next.onChange()
                entityList.push(Pair(next, EntityAction.MOVE))
                iTrimLineCallback?.onAddStack(EntityAction.MOVE)
                for (index in entityBismilahTimeline.index + 1 until entityListQuran.size) {
                    val eqt = entityListQuran[index]
                    eqt.setCurrentRect()
                    val f2 = eqt.rect.left + f
                    val width2 = eqt.rect.width() + f2
                    eqt.setX(f2)
                    eqt.right = width2
                    invalidate()
                    eqt.onChange()
                    entityList.push(Pair(eqt, EntityAction.MOVE))
                    iTrimLineCallback?.onAddStack(EntityAction.MOVE)
                }
            }
            invalidate()
        }
    }

    fun translateEndNow() {
        val entity = selectedEntity ?: return
        if (entity is EntityQuranTimeline) {
            val eqt = entity
            if (eqt.index + 1 < entityListQuran.size) {
                val next = getPreviewOrNextEntityQuran(entityListQuran, eqt.index + 1, true)
                if (next != null) {
                    eqt.setCurrentRect()
                    eqt.right = next.rect.left
                    invalidate()
                    selectedEntity!!.onChange()
                    entityList.push(Pair(selectedEntity!!, EntityAction.TRIM))
                    iTrimLineCallback?.onAddStack(EntityAction.TRIM)
                    return
                }
            }
            eqt.setCurrentRect()
            selectedEntity!!.right = timeLineW * scaleFactor
            invalidate()
            selectedEntity!!.onChange()
            entityList.push(Pair(selectedEntity!!, EntityAction.TRIM))
            iTrimLineCallback?.onAddStack(EntityAction.TRIM)
            return
        }
        if (entity is EntityBismilahTimeline) {
            val ebt = entity
            if (ebt === mIsi3adaTimeline && isExist(bismilahTimeline)) {
                ebt.setCurrentRect()
                ebt.right = bismilahTimeline!!.rect.left
                invalidate()
                selectedEntity!!.onChange()
                entityList.push(Pair(selectedEntity!!, EntityAction.TRIM))
                iTrimLineCallback?.onAddStack(EntityAction.TRIM)
                return
            }
            if (ebt.index < entityListQuran.size) {
                val next = getPreviewOrNextEntityQuran(entityListQuran, ebt.index, true)
                if (next != null) {
                    ebt.setCurrentRect()
                    ebt.right = next.rect.left
                    invalidate()
                    selectedEntity!!.onChange()
                    entityList.push(Pair(selectedEntity!!, EntityAction.TRIM))
                    iTrimLineCallback?.onAddStack(EntityAction.TRIM)
                    return
                }
            }
            ebt.setCurrentRect()
            selectedEntity!!.right = timeLineW * scaleFactor
            invalidate()
            selectedEntity!!.onChange()
            entityList.push(Pair(selectedEntity!!, EntityAction.TRIM))
            iTrimLineCallback?.onAddStack(EntityAction.TRIM)
            return
        }
        if (entity is EntityTrslTimeline) {
            val etl = entity
            if (etl.index + 1 < entityListTrslQuran.size) {
                val next = getPreviewOrNextEntityTrslQuran(entityListTrslQuran, etl.index + 1, true)
                if (next != null) {
                    etl.setCurrentRect()
                    etl.right = next.rect.left
                    invalidate()
                    selectedEntity!!.onChange()
                    entityList.push(Pair(selectedEntity!!, EntityAction.TRIM))
                    iTrimLineCallback?.onAddStack(EntityAction.TRIM)
                    return
                }
            }
            etl.setCurrentRect()
            selectedEntity!!.right = timeLineW * scaleFactor
            invalidate()
            selectedEntity!!.onChange()
            entityList.push(Pair(selectedEntity!!, EntityAction.TRIM))
            iTrimLineCallback?.onAddStack(EntityAction.TRIM)
        }
    }

    fun translateToStart() {
        current_cursur_position = 0
        currentPosition = 0.0f
        scrolled_with_zoom = 0.0f
        invalidate()
    }

    fun translateToEnd() {
        current_cursur_position = maxTime
        val f = (-maxTime * second_in_screen) / 1000.0f
        currentPosition = f
        scrolled_with_zoom = f * scaleFactor
        invalidate()
    }

    fun translateToStart(entity: Entity?) {
        if (entity == null) return
        current_cursur_position = (round(entity.rect.left / getSecond_in_screen()) * 1000).toInt()
        val f = (-current_cursur_position * getSecond_in_screen()) / 1000.0f
        currentPosition = f
        scrolled_with_zoom = f * scaleFactor
        invalidate()
    }

    fun translateToEnd(entity: Entity?) {
        if (entity == null) return
        current_cursur_position = (round(entity.rect.right / getSecond_in_screen()) * 1000).toInt()
        val f = (-current_cursur_position * getSecond_in_screen()) / 1000.0f
        currentPosition = f
        scrolled_with_zoom = f * scaleFactor
        invalidate()
    }

    fun previewEntity(entity: Entity?) {
        if (entity == null) return
        current_cursur_position = (round(entity.rect.left / getSecond_in_screen()) * 1000).toInt()
        val f = (-current_cursur_position * getSecond_in_screen()) / 1000.0f
        currentPosition = f
        scrolled_with_zoom = f * scaleFactor
        maxTime = (entity.rect.right / getSecond_in_screen() * 1000.0f).toInt()
        timeLineW = entity.rect.right / scaleFactor
    }

    fun updateCursurToSelectEntity() {
        val entity = selectedEntity ?: return
        if (entity.getEntityView()?.isVisible != false) return
        current_cursur_position = (round((entity.rect.left + selectedEntity!!.rect.width() * 0.5f) / getSecond_in_screen()) * 1000).toInt()
        val f = (-current_cursur_position * second_in_screen) / 1000.0f
        currentPosition = f
        scrolled_with_zoom = f * scaleFactor
        invalidate()
    }

    fun addStack(entity: Entity, entityAction: EntityAction) {
        entityList.push(Pair(entity, entityAction))
    }

    fun selectEntity(entity: Entity?, invalidate: Boolean) {
        selectedEntity?.isSelect = false
        entity?.isSelect = true
        selectedEntity = entity
        if (invalidate) {
            this.invalidate()
        }
    }

    fun stackSplit(entity: Entity) {
        entityList.push(Pair(entity, EntityAction.SPLIT))
        iTrimLineCallback?.onAddStack(EntityAction.SPLIT)
    }

    fun splitAudio(entityAudio: EntityAudio, i: Int) {
        if (i < entityListAudio.size) {
            entityListAudio.add(i, entityAudio)
        } else {
            entityListAudio.add(entityAudio)
        }
        selectEntity(entityAudio, false)
    }

    fun deleteEntity(isTrsl: Boolean) {
        try {
            val entity = selectedEntity
            if (entity != null) {
                entity.visible(false)
                iTrimLineCallback?.onDelete(selectedEntity!!.getEntityView()!!)
                entityList.push(Pair(selectedEntity!!, EntityAction.DELETE))
                iTrimLineCallback?.onAddStack(EntityAction.DELETE)
                selectedEntity = null
                if (isTrsl) updateTrslIndex() else updateIndex()
            }
        } catch (_: Exception) {
        }
        invalidate()
    }

    fun deleteEntityAllSelect() {
        try {
            if (entityListQuran.isNotEmpty()) {
                val arrayList = ArrayList<EntityQuranTimeline>()
                var entityQuranTimeline: EntityQuranTimeline? = null
                for (eqt in entityListQuran) {
                    if (eqt.visible() && eqt.isSelect) {
                        if (entityQuranTimeline == null) entityQuranTimeline = eqt
                        else arrayList.add(eqt)
                        eqt.visible(false)
                        eqt.isSelect = false
                        iTrimLineCallback?.onDelete(eqt.getEntityView()!!)
                        iTrimLineCallback?.onAddStack(EntityAction.DELETE)
                    }
                }
                if (entityQuranTimeline != null) {
                    entityList.push(Pair(entityQuranTimeline, EntityAction.DELETE_MULTIPLE))
                    if (arrayList.isNotEmpty()) entityQuranTimeline.setEntitiesGroup(arrayList)
                }
                updateIndex()
            }
            if (entityListTrslQuran.isNotEmpty()) {
                val arrayList2 = ArrayList<EntityTrslTimeline>()
                var entityTrslTimeline: EntityTrslTimeline? = null
                for (etl in entityListTrslQuran) {
                    if (etl.visible() && etl.isSelect) {
                        if (entityTrslTimeline == null) entityTrslTimeline = etl
                        else arrayList2.add(etl)
                        etl.visible(false)
                        etl.isSelect = false
                        iTrimLineCallback?.onDelete(etl.getEntityView()!!)
                        iTrimLineCallback?.onAddStack(EntityAction.DELETE)
                    }
                }
                if (entityTrslTimeline != null) {
                    entityList.push(Pair(entityTrslTimeline, EntityAction.DELETE_MULTIPLE))
                    if (arrayList2.isNotEmpty()) entityTrslTimeline.setEntitiesGroup(arrayList2)
                }
                updateTrslIndex()
            }
            if (isExist(bismilahTimeline) && bismilahTimeline!!.isSelect) {
                bismilahTimeline!!.visible(false)
                bismilahTimeline!!.isSelect = false
                bismilahTimeline!!.setSelectMultiple(false)
                iTrimLineCallback?.onDelete(bismilahTimeline!!.getEntityView()!!)
                entityList.push(Pair(bismilahTimeline!!, EntityAction.DELETE_MULTIPLE))
            }
            if (isExist(mIsi3adaTimeline) && mIsi3adaTimeline!!.isSelect) {
                mIsi3adaTimeline!!.visible(false)
                mIsi3adaTimeline!!.isSelect = false
                mIsi3adaTimeline!!.setSelectMultiple(false)
                iTrimLineCallback?.onDelete(mIsi3adaTimeline!!.getEntityView()!!)
                entityList.push(Pair(mIsi3adaTimeline!!, EntityAction.DELETE_MULTIPLE))
            }
            if (entityListAudio.isNotEmpty()) {
                val arrayList3 = ArrayList<EntityAudio>()
                var entityAudio: EntityAudio? = null
                for (ea in entityListAudio) {
                    if (ea.visible() && ea.isSelect) {
                        ea.visible(false)
                        ea.isSelect = false
                        if (entityAudio == null) entityAudio = ea
                        else arrayList3.add(ea)
                        iTrimLineCallback?.onAddStack(EntityAction.DELETE)
                    }
                }
                if (entityAudio != null) {
                    entityList.push(Pair(entityAudio, EntityAction.DELETE_MULTIPLE))
                    if (arrayList3.isNotEmpty()) entityAudio.setEntitiesGroup(arrayList3)
                }
                updateMediaIndex()
            }
        } catch (_: Exception) {
        }
        clr_btn_audio = CLR_BTN_DEFAULT
        clr_btn_quran = CLR_BTN_DEFAULT
        clr_btn_trsl = CLR_BTN_DEFAULT
    }

    fun deleteMediaEntity() {
        try {
            val entity = selectedEntity
            if (entity != null) {
                entity.visible(false)
                entityList.push(Pair(selectedEntity!!, EntityAction.DELETE))
                iTrimLineCallback?.onAddStack(EntityAction.DELETE)
                selectedEntity = null
                updateMediaIndex()
            }
        } catch (_: Exception) {
        }
        invalidate()
    }

    fun addAudio(entityAudio: EntityAudio, i: Int) {
        if (i < entityListAudio.size) {
            entityAudio.index = i
            entityListAudio.add(i, entityAudio)
            var f = entityAudio.rect.right
            var idx = i + 1
            while (idx < entityListAudio.size) {
                val ea = entityListAudio[idx]
                if (ea.visible()) {
                    val width = ea.rect.width()
                    ea.setCurrentRect()
                    ea.setX(f)
                    ea.right = f + width
                    ea.index = idx
                    f = ea.rect.right
                }
                idx++
            }
        } else {
            entityAudio.index = i
            entityListAudio.add(entityAudio)
        }
        entityList.push(Pair(entityAudio, EntityAction.ADD))
        iTrimLineCallback?.onAddStack(EntityAction.ADD)
    }

    fun addAudio(entityAudio: EntityAudio) {
        entityListAudio.add(entityAudio)
        entityAudio.index = entityListAudio.size - 1
        entityList.push(Pair(entityAudio, EntityAction.ADD))
        iTrimLineCallback?.onAddStack(EntityAction.ADD)
    }

    fun addQuran(entityQuranTimeline: EntityQuranTimeline) {
        entityListQuran.add(entityQuranTimeline)
        entityQuranTimeline.index = entityListQuran.size - 1
        entityList.push(Pair(entityQuranTimeline, EntityAction.ADD))
        iTrimLineCallback?.onAddStack(EntityAction.ADD)
    }

    fun addTrslQuran(entityTrslTimeline: EntityTrslTimeline) {
        entityListTrslQuran.add(entityTrslTimeline)
        entityTrslTimeline.index = entityListTrslQuran.size - 1
        entityList.push(Pair(entityTrslTimeline, EntityAction.ADD))
        iTrimLineCallback?.onAddStack(EntityAction.ADD)
    }

    fun addTrslQuran(entityTrslTimeline: EntityTrslTimeline, i: Int) {
        if (i < entityListTrslQuran.size) {
            entityTrslTimeline.index = i
            entityListTrslQuran.add(i, entityTrslTimeline)
            var f = entityTrslTimeline.rect.right
            var idx = i + 1
            while (idx < entityListTrslQuran.size) {
                val etl = entityListTrslQuran[idx]
                if (etl.visible()) {
                    val width = etl.rect.width()
                    etl.setCurrentRect()
                    etl.setX(f)
                    etl.right = f + width
                    etl.index = idx
                    f = etl.rect.right
                }
                idx++
            }
        } else {
            entityTrslTimeline.index = i
            entityListTrslQuran.add(entityTrslTimeline)
        }
        entityList.push(Pair(entityTrslTimeline, EntityAction.ADD))
        iTrimLineCallback?.onAddStack(EntityAction.ADD)
    }

    fun addQuran(entityQuranTimeline: EntityQuranTimeline, i: Int) {
        if (i < entityListQuran.size) {
            entityQuranTimeline.index = i
            entityListQuran.add(i, entityQuranTimeline)
            var f = entityQuranTimeline.rect.right
            var idx = i + 1
            while (idx < entityListQuran.size) {
                val eqt = entityListQuran[idx]
                if (eqt.visible()) {
                    val width = eqt.rect.width()
                    eqt.setCurrentRect()
                    eqt.setX(f)
                    eqt.right = f + width
                    eqt.index = idx
                    f = eqt.rect.right
                }
                idx++
            }
        } else {
            entityQuranTimeline.index = i
            entityListQuran.add(entityQuranTimeline)
        }
        entityList.push(Pair(entityQuranTimeline, EntityAction.ADD))
        iTrimLineCallback?.onAddStack(EntityAction.ADD)
    }

    fun addQuran_split(entityQuranTimeline: EntityQuranTimeline, i: Int) {
        if (i < entityListQuran.size) {
            entityQuranTimeline.index = i
            entityListQuran.add(i, entityQuranTimeline)
            var idx = i + 1
            while (idx < entityListQuran.size) {
                val eqt = entityListQuran[idx]
                if (eqt.visible()) eqt.index = idx
                idx++
            }
        } else {
            entityQuranTimeline.index = i
            entityListQuran.add(entityQuranTimeline)
        }
        entityList.push(Pair(entityQuranTimeline, EntityAction.SPLIT))
        iTrimLineCallback?.onAddStack(EntityAction.SPLIT)
    }

    fun addQuran_split(entityTrslTimeline: EntityTrslTimeline, i: Int) {
        if (i < entityListTrslQuran.size) {
            entityTrslTimeline.index = i
            entityListTrslQuran.add(i, entityTrslTimeline)
            var idx = i + 1
            while (idx < entityListTrslQuran.size) {
                val etl = entityListTrslQuran[idx]
                if (etl.visible()) etl.index = idx
                idx++
            }
        } else {
            entityTrslTimeline.index = i
            entityListTrslQuran.add(entityTrslTimeline)
        }
        entityList.push(Pair(entityTrslTimeline, EntityAction.SPLIT))
        iTrimLineCallback?.onAddStack(EntityAction.SPLIT)
    }



    fun getXCursur(): Float = (-currentPosition) * scaleFactor



    fun setOnProgress(progress: Boolean) { isProgress = progress }

    fun isPass(motionEvent: MotionEvent): Boolean {
        val eventTime = motionEvent.eventTime
        val j = lastTime
        val j2 = eventTime - j
        if (isDetectChange || j == 0L) {
            val i = countMove + 1
            countMove = i
            if (i > 3) {
                isDetectChange = false
                countMove = 0
            }
        } else if (j2 > lastDifference * 2.88) {
            isDetectChange = true
        }
        if (isDetectChange) return false
        lastTime = motionEvent.eventTime
        lastDifference = j2
        return true
    }

    private fun updateMediaIndex() {
        for (i in entityListAudio.indices) entityListAudio[i].index = i
    }

    private fun updateIndex() {
        for (i in entityListQuran.indices) {
            val eqt = entityListQuran[i]
            eqt.index = i
            eqt.quranEntity.index = i
        }
    }

    private fun updateTrslIndex() {
        for (i in entityListTrslQuran.indices) {
            val etl = entityListTrslQuran[i]
            etl.index = i
            etl.quranEntity.index = i
        }
    }

    fun updateSelectionOnTap(motionEvent: MotionEvent) {
        val pointF = PointF(motionEvent.x, motionEvent.y)
        isPassScroll = true
        val entity = selectedEntity
        var foundEntity: Entity? = null
        if (entity != null) {
            val contains3 = entity.contains(pointF)
            isPassScroll = !contains3 && selectedEntity!!.trimType == -1
            if (contains3 || selectedEntity!!.trimType != -1) {
                selectedEntity!!.setCurrentRect()
                if (iTrimLineCallback != null) {
                    if (selectedEntity!!.trimType == 0) {
                        selectedEntity!!.setOnTapTime(round(selectedEntity!!.rect.left / getSecond_in_screen()) * 1000, selectedEntity!!.rect.left)
                        iTrimLineCallback!!.onPlayVibration()
                    } else if (selectedEntity!!.trimType == 1) {
                        selectedEntity!!.setOnTapTime(round(selectedEntity!!.rect.right / getSecond_in_screen()) * 1000, selectedEntity!!.rect.right)
                        iTrimLineCallback!!.onPlayVibration()
                    } else {
                        iTrimLineCallback!!.onSelectEntity(selectedEntity!!, 0.0f)
                    }
                }
                if (selectedEntity!!.isSelect) return
                selectedEntity!!.isSelect = true
                invalidate()
                return
            }
        }
        // Search Quran entities
        for (eqt in entityListQuran) {
            if (eqt !== selectedEntity && eqt.visible()) {
                val contains4 = eqt.contains(pointF)
                isPassScroll = !contains4 && eqt.trimType == -1
                if (contains4 || eqt.trimType != -1) {
                    eqt.setCurrentRect()
                    eqt.isSelect = true
                    eqt.setDownX(pointF.x)
                    if (iTrimLineCallback != null) {
                        if (eqt.trimType == 0) {
                            eqt.setOnTapTime(round(eqt.rect.left / getSecond_in_screen()) * 1000, eqt.rect.left)
                            iTrimLineCallback!!.onPlayVibration()
                        } else if (eqt.trimType == 1) {
                            eqt.setOnTapTime(round(eqt.rect.right / getSecond_in_screen()) * 1000, eqt.rect.right)
                            iTrimLineCallback!!.onPlayVibration()
                        } else {
                            iTrimLineCallback!!.onSelectEntity(eqt, 0.0f)
                        }
                    }
                    foundEntity = eqt
                }
            }
        }
        // Search Trsl entities
        if (foundEntity == null) {
            for (etl in entityListTrslQuran) {
                if (etl !== selectedEntity && etl.visible()) {
                    val contains5 = etl.contains(pointF)
                    isPassScroll = !contains5 && etl.trimType == -1
                    if (contains5 || etl.trimType != -1) {
                        etl.setCurrentRect()
                        etl.isSelect = true
                        etl.setDownX(pointF.x)
                        if (iTrimLineCallback != null) {
                            if (etl.trimType == 0) {
                                etl.setOnTapTime(round(etl.rect.left / getSecond_in_screen()) * 1000, etl.rect.left)
                                iTrimLineCallback!!.onPlayVibration()
                            } else if (etl.trimType == 1) {
                                etl.setOnTapTime(round(etl.rect.right / getSecond_in_screen()) * 1000, etl.rect.right)
                                iTrimLineCallback!!.onPlayVibration()
                            } else {
                                iTrimLineCallback!!.onSelectEntity(etl, 0.0f)
                            }
                        }
                        foundEntity = etl
                    }
                }
            }
        }
        // Search Audio entities
        if (foundEntity == null) {
            for (ea in entityListAudio) {
                if (ea !== selectedEntity && ea.visible()) {
                    val contains6 = ea.contains(pointF)
                    isPassScroll = !contains6 && ea.trimType == -1
                    if (contains6 || ea.trimType != -1) {
                        ea.setCurrentRect()
                        ea.isSelect = true
                        ea.setDownX(pointF.x)
                        if (iTrimLineCallback != null) {
                            if (ea.trimType == 0) {
                                ea.setOnTapTime(round(ea.rect.left / getSecond_in_screen()) * 1000, ea.rect.left)
                                iTrimLineCallback!!.onPlayVibration()
                            } else if (ea.trimType == 1) {
                                ea.setOnTapTime(round(ea.rect.right / getSecond_in_screen()) * 1000, ea.rect.right)
                                iTrimLineCallback!!.onPlayVibration()
                            } else {
                                iTrimLineCallback!!.onSelectEntity(ea, 0.0f)
                            }
                        }
                        foundEntity = ea
                    }
                }
            }
        }
        // Search Bismilah
        if (foundEntity == null && isExist(bismilahTimeline)) {
            val contains2 = bismilahTimeline!!.contains(pointF)
            isPassScroll = contains2 && bismilahTimeline!!.trimType == -1
            if (!contains2 || bismilahTimeline!!.trimType != -1) {
                foundEntity = bismilahTimeline
                foundEntity!!.setCurrentRect()
                foundEntity!!.isSelect = true
                foundEntity!!.setDownX(pointF.x)
                if (iTrimLineCallback != null) {
                    if (foundEntity!!.trimType == 0) {
                        foundEntity!!.setOnTapTime(round(foundEntity!!.rect.left / getSecond_in_screen()) * 1000, foundEntity!!.rect.left)
                        iTrimLineCallback!!.onPlayVibration()
                    } else if (foundEntity!!.trimType == 1) {
                        foundEntity!!.setOnTapTime(round(foundEntity!!.rect.right / getSecond_in_screen()) * 1000, foundEntity!!.rect.right)
                        iTrimLineCallback!!.onPlayVibration()
                    } else {
                        iTrimLineCallback!!.onSelectEntity(foundEntity!!, 0.0f)
                    }
                }
            }
        }
        // Search Isi3ada
        if (foundEntity == null && isExist(mIsi3adaTimeline)) {
            val contains = mIsi3adaTimeline!!.contains(pointF)
            isPassScroll = !contains && mIsi3adaTimeline!!.trimType == -1
            if (!contains || mIsi3adaTimeline!!.trimType != -1) {
                foundEntity = mIsi3adaTimeline
                foundEntity!!.setCurrentRect()
                foundEntity!!.isSelect = true
                foundEntity!!.setDownX(pointF.x)
                if (iTrimLineCallback != null) {
                    if (foundEntity!!.trimType == 0) {
                        foundEntity!!.setOnTapTime(round(foundEntity!!.rect.left / getSecond_in_screen()) * 1000, foundEntity!!.rect.left)
                        iTrimLineCallback!!.onPlayVibration()
                    } else if (foundEntity!!.trimType == 1) {
                        foundEntity!!.setOnTapTime(round(foundEntity!!.rect.right / getSecond_in_screen()) * 1000, foundEntity!!.rect.right)
                        iTrimLineCallback!!.onPlayVibration()
                    } else {
                        iTrimLineCallback!!.onSelectEntity(foundEntity!!, 0.0f)
                    }
                }
            }
        }
        if (foundEntity != null) {
            if (selectedEntity != null) {
                unselectEntity()
                invalidate()
            }
        } else if (selectedEntity !== foundEntity) {
            unselectEntity()
            selectedEntity = foundEntity
            invalidate()
        }
        if (selectedEntity == null) {
            iTrimLineCallback?.onEmptySelect()
        }
    }

    fun finishScroll() {
        try {
            val sc = scroller
            if (sc != null && !sc.isFinished) sc.abortAnimation()
            scroller = null
        } catch (_: Exception) {
        }
    }

    override fun computeScroll() {
        val sc = scroller
        if (sc == null || isProgress || !sc.computeScrollOffset()) return
        if (currentPosition != 0.0f || sc.currX > 0) {
            val currX = sc.currX.toFloat()
            currentPosition = currX
            if (currX > 0.0f) currentPosition = 0.0f
            val f = currentPosition * scaleFactor
            scrolled_with_zoom = f
            if (!isPlaying) {
                iTrimLineCallback?.onSeekPlayer(f)
            }
            invalidate()
        }
    }

    fun pauseScroll() {
        val sc = scroller
        if (sc == null || sc.isFinished) return
        sc.abortAnimation()
    }

    fun updateWhenEffect(entityAudio: EntityAudio) {
        if (entityAudio.index + 1 >= entityListAudio.size) return
        val next = getPreviewOrNextEntityAudio(entityListAudio, entityAudio.index + 1, true) ?: return
        if (entityAudio.rect.right <= next.rect.left) return
        val width = next.rect.width() + entityAudio.rect.right
        val f = entityAudio.rect.right - next.rect.left
        next.setCurrentRect()
        next.setX(entityAudio.rect.right)
        next.right = width
        for (index in entityAudio.index + 2 until entityListAudio.size) {
            val ea = entityListAudio[index]
            if (ea.visible()) {
                val f2 = ea.rect.left + f
                val width2 = ea.rect.width() + f2
                ea.setCurrentRect()
                ea.setX(f2)
                ea.right = width2
            }
        }
    }

    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        if (motionEvent == null || isProgress) return false
        motionEvent.setLocation(
            motionEvent.x + paddingLeft - (centerX - radius * 0.5f + scrolled_with_zoom),
            motionEvent.y + paddingTop - mScrollY
        )
        if (motionEvent.pointerCount > 1) return scaleGestureDetector!!.onTouchEvent(motionEvent)
        if (isScaleListener) {
            if (motionEvent.action == MotionEvent.ACTION_UP) isScaleListener = false
            return true
        }
        val action = motionEvent.action
        if (action == MotionEvent.ACTION_UP) {
            eventY = 0.0f
            eventX = 0.0f
            signeY = -1.0f
            signeX = -1.0f
            lastTime = 0L
            lastDifference = 0L
            countMove = 0
            isDetectChange = false
            isPassScroll = true
            isAutoMove = false
            if (selectedEntity != null) {
                if (isMove) {
                    current_cursur_position = (round(currentPosition * 1000.0f / second_in_screen * -1.0f)).toInt()
                    isAutoScroll = false
                    isOnUp = true
                    isCheckLineCursur = false
                    isCheckLine = false
                    invalidate()
                    selectedEntity!!.onChange()
                    entityList.push(Pair(selectedEntity!!, EntityAction.MOVE))
                    iTrimLineCallback?.let {
                        it.onUpdateTime()
                        it.onAddStack(EntityAction.MOVE)
                    }
                }
                if (selectedEntity!!.selectTrim != null) {
                    isAutoScroll = false
                    iTrimLineCallback?.onUp()
                    pass = true
                    onThink = true
                    lasX = 0.0f
                    isOnUp = true
                    isCheckLineCursur = false
                    isCheckLine = false
                    if (selectedEntity!!.trimType == 0) {
                        selectedEntity!!.onChange()
                        entityList.push(Pair(selectedEntity!!, EntityAction.TRIM))
                        iTrimLineCallback?.onAddStack(EntityAction.TRIM)
                        selectedEntity!!.onUpLeft()
                    }
                    if (selectedEntity!!.trimType == 1) {
                        val entity2 = selectedEntity!!
                        if (entity2 is EntityQuranTimeline) {
                            for (eqt in entityListQuran) {
                                if (eqt.visible() && eqt.getCurrentStackEntity() != null && eqt !== selectedEntity) {
                                    eqt.onChange()
                                    entityList.push(Pair(eqt, EntityAction.MOVE))
                                }
                            }
                        } else if (entity2 is EntityAudio) {
                            for (ea in entityListAudio) {
                                if (ea.visible() && ea.getCurrentStackEntity() != null && ea !== selectedEntity) {
                                    ea.onChange()
                                    entityList.push(Pair(ea, EntityAction.MOVE))
                                }
                            }
                        } else if (entity2 is EntityBismilahTimeline && entity2.getCurrentStackEntity() != null) {
                            selectedEntity!!.onChange()
                            entityList.push(Pair(selectedEntity!!, EntityAction.MOVE))
                        }
                        selectedEntity!!.onChange()
                        entityList.push(Pair(selectedEntity!!, EntityAction.TRIM))
                        iTrimLineCallback?.onAddStack(EntityAction.TRIM)
                        selectedEntity!!.onUpRight()
                    }
                    selectedEntity!!.resetTrimType()
                    invalidate()
                }
                selectedEntity!!.setX(selectedEntity!!.rect.left)
                selectedEntity!!.right = selectedEntity!!.rect.right
                if (iTrimLineCallback != null && !isMove) {
                    iTrimLineCallback!!.onUpdateTime()
                }
                isMove = false
                autoScrollHandler.removeCallbacks(autoMoveRunnable!!)
                autoScrollHandler.removeCallbacks(autoScrollRunnable!!)
            }
            iTrimLineCallback?.onUp()
        } else if (action == MotionEvent.ACTION_MOVE && selectedEntity != null && !isPassScroll) {
            if (selectedEntity!!.selectTrim != null) {
                if (!isPass(motionEvent)) return true
                iTrimLineCallback?.onMove()
                // Trim left (trim_type == 0)
                if (selectedEntity!!.trimType == 0 && onThink) {
                    if (abs(motionEvent.x - lasX) <= TOLERANCE_X) return false
                    lasX = motionEvent.x
                    val x = motionEvent.x - selectedEntity!!.getDownX()
                    if (x == 0.0f) return false
                    selectedEntity!!.setTrimLeft(true)
                    var left = selectedEntity!!.left + x
                    val isValidTrim = selectedEntity!!.rect.right - left > max_trim
                    if (left < 0.0f) left = 0.0f
                    else if (!isValidTrim) left = selectedEntity!!.rect.right - max_trim
                    // EntityAudio trim left
                    if (selectedEntity is EntityAudio) {
                        val ea = selectedEntity as EntityAudio
                        val offsetRight = ea.getOffsetRight() * ea.scaleFactor
                        val f2 = selectedEntity!!.rect.right + offsetRight - left
                        val max = ea.max * ea.scaleFactor
                        if (f2 > max) {
                            selectedEntity!!.setX(selectedEntity!!.rect.right + offsetRight - max)
                            ea.updateStartTrim()
                            invalidate()
                            return true
                        }
                        if (ea.index > 0) {
                            val prev = getPreviewOrNextEntityAudio(entityListAudio, ea.index - 1, false)
                            if (prev != null && left <= prev.rect.right) {
                                val width = prev.rect.right + selectedEntity!!.rect.width()
                                selectedEntity!!.setX(prev.rect.right)
                                ea.updateStartTrim()
                                selectedEntity!!.right = width
                                pass = false
                                invalidate()
                                return true
                            }
                        }
                    }
                    // EntityQuranTimeline trim left
                    if (selectedEntity is EntityQuranTimeline) {
                        val eqt = selectedEntity as EntityQuranTimeline
                        if (eqt.index > 0) {
                            val prev = getPreviewOrNextEntityQuran(entityListQuran, eqt.index - 1, false)
                            if (prev != null && left <= prev.rect.right) {
                                selectedEntity!!.setX(prev.rect.right)
                                pass = false
                                invalidate()
                                return true
                            }
                        }
                        if (isExist(bismilahTimeline) && left <= bismilahTimeline!!.rect.right) {
                            selectedEntity!!.setX(bismilahTimeline!!.rect.right)
                            pass = false
                            invalidate()
                            return true
                        }
                        if (isExist(mIsi3adaTimeline) && left <= mIsi3adaTimeline!!.rect.right) {
                            selectedEntity!!.setX(mIsi3adaTimeline!!.rect.right)
                            pass = false
                            invalidate()
                            return true
                        }
                    }
                    // EntityTrslTimeline trim left
                    if (selectedEntity is EntityTrslTimeline) {
                        val etl = selectedEntity as EntityTrslTimeline
                        if (etl.index > 0) {
                            val prev = getPreviewOrNextEntityTrslQuran(entityListTrslQuran, etl.index - 1, false)
                            if (prev != null && left <= prev.rect.right) {
                                selectedEntity!!.setX(prev.rect.right)
                                pass = false
                                invalidate()
                                return true
                            }
                        }
                    }
                    // EntityBismilahTimeline trim left
                    if (selectedEntity is EntityBismilahTimeline && selectedEntity === bismilahTimeline && isExist(mIsi3adaTimeline) && left <= mIsi3adaTimeline!!.rect.right) {
                        selectedEntity!!.setX(mIsi3adaTimeline!!.rect.right)
                        pass = false
                        invalidate()
                        return true
                    }
                    // Snap to cursor line
                    if (onThink && pass) {
                        val f3 = selectedEntity!!.rect.left
                        val f4 = scrolled_with_zoom
                        val f5 = f3 + f4
                        if (f5 >= -TOLERANCE_X && f5 < TOLERANCE_X) {
                            onThink = false
                            val f7 = -f4
                            selectedEntity!!.setX(f7)
                            selectedEntity!!.updateStartTrim()
                            if (selectedEntity is EntityAudio) {
                                selectedEntity!!.right = f7 + selectedEntity!!.rect.width()
                            }
                            isCheckLineCursur = true
                            startXLine = selectedEntity!!.rect.left
                            invalidate()
                            iTrimLineCallback?.onPlayVibration()
                            Handler().postDelayed({
                                selectedEntity?.setDownX(motionEvent.x)
                                onThink = true
                                pass = false
                                isCheckLineCursur = false
                            }, 500L)
                            return false
                        }
                        // Snap to other entities
                        val it = entityList.iterator()
                        while (it.hasNext()) {
                            val next = it.next()
                            val nextEntity = next.first
                            if (nextEntity.rect.top != selectedEntity!!.rect.top && nextEntity !== selectedEntity && (next.second == EntityAction.ADD || next.second == EntityAction.SPLIT)) {
                                if (!nextEntity.visible()) continue
                                if (selectedEntity!!.rect.left >= nextEntity.rect.left - TOLERANCE_X && selectedEntity!!.rect.left <= nextEntity.rect.left + TOLERANCE_X) {
                                    onThink = false
                                    selectedEntity!!.setX(nextEntity.rect.left)
                                    selectedEntity!!.updateStartTrim()
                                    if (selectedEntity is EntityAudio) {
                                        selectedEntity!!.right = nextEntity.rect.left + selectedEntity!!.rect.width()
                                    }
                                    isCheckLine = true
                                    startXLine = selectedEntity!!.rect.left
                                    invalidate()
                                    iTrimLineCallback?.onPlayVibration()
                                    Handler().postDelayed({
                                        selectedEntity?.setDownX(motionEvent.x)
                                        onThink = true
                                        pass = false
                                        isCheckLine = false
                                    }, 500L)
                                    return false
                                }
                                if (selectedEntity!!.rect.left >= nextEntity.rect.right - TOLERANCE_X && selectedEntity!!.rect.left <= nextEntity.rect.right + TOLERANCE_X) {
                                    onThink = false
                                    selectedEntity!!.setX(nextEntity.rect.right)
                                    if (selectedEntity is EntityAudio) {
                                        selectedEntity!!.right = nextEntity.rect.right + selectedEntity!!.rect.width()
                                        selectedEntity!!.updateStartTrim()
                                    }
                                    isCheckLine = true
                                    startXLine = selectedEntity!!.rect.left
                                    invalidate()
                                    iTrimLineCallback?.onPlayVibration()
                                    Handler().postDelayed({
                                        selectedEntity?.setDownX(motionEvent.x)
                                        onThink = true
                                        pass = false
                                        isCheckLine = false
                                    }, 500L)
                                    return false
                                }
                            }
                        }
                    }
                    // Apply trim left for EntityAudio
                    if (selectedEntity is EntityAudio) {
                        selectedEntity!!.rect.left = left
                        selectedEntity!!.setLastLeft(selectedEntity!!.left + x)
                        selectedEntity!!.updateStartTrim()
                        autoScrollHandler.removeCallbacks(autoScrollRunnable!!)
                        isAutoScroll = false
                    } else if (isValidTrim) {
                        // Auto-scroll right trim left
                        if (selectedEntity!!.rect.left < left) {
                            if (selectedEntity!!.rect.left + getCurrentPosition() > DETECT_RIGHT_MOVE) {
                                if (!isAutoScroll) {
                                    if (left > selectedEntity!!.rect.left) {
                                        if (SPEED < 0.0f) SPEED *= -1.0f
                                    } else {
                                        if (SPEED > 0.0f) SPEED *= -1.0f
                                    }
                                    isAutoScroll = true
                                    time_start = System.currentTimeMillis()
                                    autoScrollHandler.postDelayed(autoScrollRunnable!!, 100L)
                                } else if (left < selectedEntity!!.rect.left && isAutoScroll) {
                                    isAutoScroll = false
                                    autoScrollHandler.removeCallbacks(autoScrollRunnable!!)
                                }
                            } else if (isAutoScroll) {
                                isAutoScroll = false
                                autoScrollHandler.removeCallbacks(autoScrollRunnable!!)
                            }
                        } else if (selectedEntity!!.rect.left > 0.0f && selectedEntity!!.rect.left + getCurrentPosition() < -DETECT_LEFT_MOVE) {
                            if (!isAutoScroll) {
                                if (SPEED < 0.0f) SPEED *= -1.0f
                                isAutoScroll = true
                                time_start = System.currentTimeMillis()
                                autoScrollHandler.postDelayed(autoScrollRunnable!!, 100L)
                            } else {
                                if (SPEED > 0.0f) SPEED *= -1.0f
                            }
                        } else if (isAutoScroll) {
                            isAutoScroll = false
                            autoScrollHandler.removeCallbacks(autoScrollRunnable!!)
                        }
                    }
                    if (!isAutoScroll) {
                        if (left > selectedEntity!!.rect.left) {
                            selectedEntity!!.rect.left = left + TOLERANCE_X
                        } else {
                            selectedEntity!!.rect.left = left - TOLERANCE_X
                        }
                    }
                    val strokeWidth = paintCursur!!.strokeWidth * 0.3f
                    pass = selectedEntity!!.rect.left < startXLine - strokeWidth || selectedEntity!!.rect.left > startXLine + strokeWidth
                    invalidate()
                } else if (selectedEntity!!.trimType == 1 && onThink) {
                    // Trim right
                    if (abs(motionEvent.x - lasX) <= TOLERANCE_X) return false
                    lasX = motionEvent.x
                    val x2 = motionEvent.x - selectedEntity!!.getDownX()
                    if (x2 == 0.0f) return false
                    var right = selectedEntity!!.right + x2
                    val isValidTrim = right - selectedEntity!!.rect.left > max_trim
                    if (!isValidTrim) right = selectedEntity!!.rect.left + max_trim
                    var f: Float = -1.0f
                    if (selectedEntity is EntityAudio) {
                        val ea = selectedEntity as EntityAudio
                        f = right - selectedEntity!!.rect.left
                        val max2 = ea.max * ea.scaleFactor - ea.getOffsetLeft() * ea.scaleFactor
                        if (f > max2) right = selectedEntity!!.rect.left + max2
                        else if (ea.index + 1 < entityListAudio.size) {
                            val next = getPreviewOrNextEntityAudio(entityListAudio, ea.index + 1, true)
                            if (next != null && right > next.rect.left) {
                                selectedEntity!!.rect.right = right
                                if (f == -1.0f) selectedEntity!!.setLastRight(selectedEntity!!.right + x2)
                                else selectedEntity!!.setLastRight(selectedEntity!!.rect.right)
                                val width2 = next.rect.width() + right
                                val f12 = right - next.rect.left
                                next.setCurrentRect()
                                next.setX(right)
                                next.right = width2
                                for (index in ea.index + 2 until entityListAudio.size) {
                                    val ea2 = entityListAudio[index]
                                    if (ea2.visible()) {
                                        val f13 = ea2.rect.left + f12
                                        val width3 = ea2.rect.width() + f13
                                        ea2.setCurrentRect()
                                        ea2.setX(f13)
                                        ea2.right = width3
                                    }
                                }
                                pass = false
                                invalidate()
                                return true
                            }
                        }
                    }
                    // Snap to cursor line for right trim
                    if (onThink && pass) {
                        val f14 = selectedEntity!!.rect.right
                        val f15 = scrolled_with_zoom
                        val f16 = f14 + f15
                        if (f16 >= -TOLERANCE_X && f16 < TOLERANCE_X) {
                            onThink = false
                            val f18 = -f15 + TOLERANCE_X
                            if (selectedEntity is EntityAudio) {
                                selectedEntity!!.setX(selectedEntity!!.rect.right - selectedEntity!!.rect.width())
                            }
                            selectedEntity!!.right = f18
                            isCheckLineCursur = true
                            startXLine = selectedEntity!!.rect.right
                            invalidate()
                            iTrimLineCallback?.onPlayVibration()
                            Handler().postDelayed({
                                selectedEntity?.setDownX(motionEvent.x)
                                onThink = true
                                pass = false
                                isCheckLineCursur = false
                            }, 500L)
                            return false
                        }
                        // Snap to other entities for right trim
                        val it2 = entityList.iterator()
                        while (it2.hasNext()) {
                            val next2 = it2.next()
                            val nextEntity2 = next2.first
                            if (nextEntity2.rect.top != selectedEntity!!.rect.top && nextEntity2 !== selectedEntity && (next2.second == EntityAction.ADD || next2.second == EntityAction.SPLIT) && nextEntity2.visible()) {
                                if (selectedEntity!!.rect.right >= nextEntity2.rect.left - TOLERANCE_X && selectedEntity!!.rect.right <= nextEntity2.rect.left + TOLERANCE_X) {
                                    onThink = false
                                    selectedEntity!!.right = nextEntity2.rect.left
                                    if (selectedEntity is EntityAudio) {
                                        selectedEntity!!.setX(nextEntity2.rect.left - selectedEntity!!.rect.width())
                                    }
                                    isCheckLine = true
                                    startXLine = selectedEntity!!.rect.right
                                    invalidate()
                                    iTrimLineCallback?.onPlayVibration()
                                    Handler().postDelayed({
                                        selectedEntity?.setDownX(motionEvent.x)
                                        onThink = true
                                        pass = false
                                        isCheckLine = false
                                    }, 500L)
                                    return false
                                }
                                if (selectedEntity!!.rect.right >= nextEntity2.rect.right - TOLERANCE_X && selectedEntity!!.rect.right <= nextEntity2.rect.right + TOLERANCE_X) {
                                    onThink = false
                                    selectedEntity!!.right = nextEntity2.rect.right
                                    if (selectedEntity is EntityAudio) {
                                        selectedEntity!!.setX(nextEntity2.rect.right - selectedEntity!!.rect.width())
                                    }
                                    isCheckLine = true
                                    startXLine = selectedEntity!!.rect.right
                                    invalidate()
                                    iTrimLineCallback?.onPlayVibration()
                                    Handler().postDelayed({
                                        selectedEntity?.setDownX(motionEvent.x)
                                        onThink = true
                                        pass = false
                                        isCheckLine = false
                                    }, 500L)
                                    return false
                                }
                            }
                        }
                    }
                    // Apply right trim for EntityAudio
                    if (selectedEntity is EntityAudio) {
                        selectedEntity!!.rect.right = right
                        if (f == -1.0f) selectedEntity!!.setLastRight(selectedEntity!!.right + x2)
                        else selectedEntity!!.setLastRight(selectedEntity!!.rect.right)
                        autoScrollHandler.removeCallbacks(autoScrollRunnable!!)
                        isAutoScroll = false
                    }
                    // Right trim collision for Quran
                    if (selectedEntity is EntityQuranTimeline) {
                        val eqt = selectedEntity as EntityQuranTimeline
                        if (eqt.index < entityListQuran.size) {
                            val next = getPreviewOrNextEntityQuran(entityListQuran, eqt.index + 1, true)
                            if (next != null && right > next.rect.left) {
                                val width = next.rect.width() + right
                                val f19 = right - next.rect.left
                                next.setCurrentRect()
                                next.setX(right)
                                next.right = width
                                for (index in eqt.index + 2 until entityListQuran.size) {
                                    val eqt2 = entityListQuran[index]
                                    if (eqt2.visible()) {
                                        val f20 = eqt2.rect.left + f19
                                        val width5 = eqt2.rect.width() + f20
                                        eqt2.setCurrentRect()
                                        eqt2.setX(f20)
                                        eqt2.right = width5
                                    }
                                }
                                pass = false
                                selectedEntity!!.rect.right = right
                                invalidate()
                                return true
                            }
                        }
                        // Auto-scroll for Quran right trim
                        if (isValidTrim) {
                            if (selectedEntity!!.rect.right < right) {
                                if (selectedEntity!!.rect.right + getCurrentPosition() > DETECT_RIGHT_MOVE) {
                                    if (!isAutoScroll) {
                                        if (right > selectedEntity!!.rect.right) { if (SPEED < 0.0f) SPEED *= -1.0f }
                                        else { if (SPEED > 0.0f) SPEED *= -1.0f }
                                        isAutoScroll = true
                                        time_start = System.currentTimeMillis()
                                        autoScrollHandler.postDelayed(autoScrollRunnable!!, 100L)
                                    } else if (right < selectedEntity!!.rect.right && isAutoScroll) {
                                        isAutoScroll = false
                                        autoScrollHandler.removeCallbacks(autoScrollRunnable!!)
                                    }
                                } else if (isAutoScroll) {
                                    isAutoScroll = false
                                    autoScrollHandler.removeCallbacks(autoScrollRunnable!!)
                                }
                            } else if (selectedEntity!!.rect.right > 0.0f && selectedEntity!!.rect.right + getCurrentPosition() < -DETECT_LEFT_MOVE) {
                                if (!isAutoScroll) {
                                    if (SPEED < 0.0f) SPEED *= -1.0f
                                    isAutoScroll = true
                                    time_start = System.currentTimeMillis()
                                    autoScrollHandler.postDelayed(autoScrollRunnable!!, 100L)
                                } else { if (SPEED > 0.0f) SPEED *= -1.0f }
                            } else if (isAutoScroll) {
                                isAutoScroll = false
                                autoScrollHandler.removeCallbacks(autoScrollRunnable!!)
                            }
                        }
                    }
                    // Right trim collision for Trsl
                    if (selectedEntity is EntityTrslTimeline) {
                        val etl = selectedEntity as EntityTrslTimeline
                        if (etl.index < entityListTrslQuran.size) {
                            val next = getPreviewOrNextEntityTrslQuran(entityListTrslQuran, etl.index + 1, true)
                            if (next != null && right > next.rect.left) {
                                val width = next.rect.width() + right
                                val f = right - next.rect.left
                                next.setCurrentRect()
                                next.setX(right)
                                next.right = width
                                for (index in etl.index + 2 until entityListTrslQuran.size) {
                                    val etl2 = entityListTrslQuran[index]
                                    if (etl2.visible()) {
                                        val f26 = etl2.rect.left + f
                                        val width7 = etl2.rect.width() + f26
                                        etl2.setCurrentRect()
                                        etl2.setX(f26)
                                        etl2.right = width7
                                    }
                                }
                                pass = false
                                selectedEntity!!.rect.right = right
                                invalidate()
                                return true
                            }
                        }
                        if (isValidTrim) {
                            if (selectedEntity!!.rect.right < right) {
                                if (selectedEntity!!.rect.right + getCurrentPosition() > DETECT_RIGHT_MOVE) {
                                    if (!isAutoScroll) {
                                        if (right > selectedEntity!!.rect.right) { if (SPEED < 0.0f) SPEED *= -1.0f }
                                        else { if (SPEED > 0.0f) SPEED *= -1.0f }
                                        isAutoScroll = true
                                        time_start = System.currentTimeMillis()
                                        autoScrollHandler.postDelayed(autoScrollRunnable!!, 100L)
                                    } else if (right < selectedEntity!!.rect.right && isAutoScroll) {
                                        isAutoScroll = false
                                        autoScrollHandler.removeCallbacks(autoScrollRunnable!!)
                                    }
                                } else if (isAutoScroll) {
                                    isAutoScroll = false
                                    autoScrollHandler.removeCallbacks(autoScrollRunnable!!)
                                }
                            } else if (selectedEntity!!.rect.right > 0.0f && selectedEntity!!.rect.right + getCurrentPosition() < -DETECT_LEFT_MOVE) {
                                if (!isAutoScroll) {
                                    if (SPEED < 0.0f) SPEED *= -1.0f
                                    isAutoScroll = true
                                    time_start = System.currentTimeMillis()
                                    autoScrollHandler.postDelayed(autoScrollRunnable!!, 100L)
                                } else { if (SPEED > 0.0f) SPEED *= -1.0f }
                            } else if (isAutoScroll) {
                                isAutoScroll = false
                                autoScrollHandler.removeCallbacks(autoScrollRunnable!!)
                            }
                        }
                    }
                    // Right trim collision for Bismilah
                    if (selectedEntity is EntityBismilahTimeline) {
                        val ebt = selectedEntity as EntityBismilahTimeline
                        if (ebt === mIsi3adaTimeline && isExist(bismilahTimeline) && right >= bismilahTimeline!!.rect.left) {
                            val f = right - bismilahTimeline!!.rect.left
                            val width = bismilahTimeline!!.rect.width() + right
                            bismilahTimeline!!.setCurrentRect()
                            bismilahTimeline!!.setX(right)
                            bismilahTimeline!!.right = width
                            bismilahTimeline!!.onChange()
                            entityList.push(Pair(bismilahTimeline!!, EntityAction.MOVE))
                            iTrimLineCallback?.onAddStack(EntityAction.MOVE)
                            for (index in bismilahTimeline!!.index until entityListQuran.size) {
                                val eqt = entityListQuran[index]
                                if (eqt.visible()) {
                                    val f34 = eqt.rect.left + f
                                    val width11 = eqt.rect.width() + f34
                                    eqt.setCurrentRect()
                                    eqt.setX(f34)
                                    eqt.right = width11
                                }
                            }
                            pass = false
                            selectedEntity!!.rect.right = right
                            invalidate()
                            return true
                        }
                        if (ebt.index < entityListQuran.size) {
                            val next = getPreviewOrNextEntityQuran(entityListQuran, ebt.index, true)
                            if (next != null && right >= next.rect.left) {
                                val f = right - next.rect.left
                                val width = next.rect.width() + right
                                next.setCurrentRect()
                                next.setX(right)
                                next.right = width
                                next.onChange()
                                entityList.push(Pair(next, EntityAction.MOVE))
                                iTrimLineCallback?.onAddStack(EntityAction.MOVE)
                                for (index in ebt.index + 1 until entityListQuran.size) {
                                    val eqt = entityListQuran[index]
                                    if (eqt.visible()) {
                                        val f34 = eqt.rect.left + f
                                        val width11 = eqt.rect.width() + f34
                                        eqt.setCurrentRect()
                                        eqt.setX(f34)
                                        eqt.right = width11
                                    }
                                }
                                pass = false
                                selectedEntity!!.rect.right = right
                                invalidate()
                                return true
                            }
                        }
                        if (isValidTrim) {
                            if (selectedEntity!!.rect.right < right) {
                                if (selectedEntity!!.rect.right + getCurrentPosition() > DETECT_RIGHT_MOVE) {
                                    if (!isAutoScroll) {
                                        if (right > selectedEntity!!.rect.right) { if (SPEED < 0.0f) SPEED *= -1.0f }
                                        else { if (SPEED > 0.0f) SPEED *= -1.0f }
                                        isAutoScroll = true
                                        time_start = System.currentTimeMillis()
                                        autoScrollHandler.postDelayed(autoScrollRunnable!!, 100L)
                                    } else if (right < selectedEntity!!.rect.right && isAutoScroll) {
                                        isAutoScroll = false
                                        autoScrollHandler.removeCallbacks(autoScrollRunnable!!)
                                    }
                                } else if (isAutoScroll) {
                                    isAutoScroll = false
                                    autoScrollHandler.removeCallbacks(autoScrollRunnable!!)
                                }
                            } else if (selectedEntity!!.rect.right > 0.0f && selectedEntity!!.rect.right + getCurrentPosition() < -DETECT_LEFT_MOVE) {
                                if (!isAutoScroll) {
                                    if (SPEED < 0.0f) SPEED *= -1.0f
                                    isAutoScroll = true
                                    time_start = System.currentTimeMillis()
                                    autoScrollHandler.postDelayed(autoScrollRunnable!!, 100L)
                                } else { if (SPEED > 0.0f) SPEED *= -1.0f }
                            } else if (isAutoScroll) {
                                isAutoScroll = false
                                autoScrollHandler.removeCallbacks(autoScrollRunnable!!)
                            }
                        }
                    }
                    if (!isAutoScroll) {
                        if (right > selectedEntity!!.rect.right) {
                            selectedEntity!!.rect.right = right + TOLERANCE_X
                        } else {
                            selectedEntity!!.rect.right = right - TOLERANCE_X
                        }
                    }
                    val strokeWidth2 = paintCursur!!.strokeWidth * 0.3f
                    pass = selectedEntity!!.rect.right < startXLine - strokeWidth2 || selectedEntity!!.rect.right > startXLine + strokeWidth2
                    invalidate()
                }
            } else {
                // Move entity (not trim)
                if (abs(motionEvent.x - lasX) <= TOLERANCE_X) return false
                lasX = motionEvent.x
                val x3 = motionEvent.x - selectedEntity!!.getDownX()
                if (x3 == 0.0f) return false
                val width12 = selectedEntity!!.rect.width()
                var left2 = x3 + selectedEntity!!.left
                if (left2 < 0.0f) left2 = 0.0f
                val f39 = left2 + width12
                // Collision detection for move
                if (selectedEntity is EntityQuranTimeline) {
                    val eqt = selectedEntity as EntityQuranTimeline
                    if (eqt.index > 0) {
                        val prev = getPreviewOrNextEntityQuran(entityListQuran, eqt.index - 1, false)
                        if (prev != null && left2 <= prev.rect.right) {
                            selectedEntity!!.setX(prev.rect.right)
                            selectedEntity!!.right = prev.rect.right + width12
                            pass = false
                            invalidate()
                            return true
                        }
                    }
                    if (eqt.index + 1 < entityListQuran.size) {
                        val next = getPreviewOrNextEntityQuran(entityListQuran, eqt.index + 1, true)
                        if (next != null && f39 >= next.rect.left) {
                            selectedEntity!!.setX(next.rect.left - width12)
                            selectedEntity!!.right = next.rect.left
                            pass = false
                            invalidate()
                            return true
                        }
                    }
                    if (isExist(bismilahTimeline) && left2 <= bismilahTimeline!!.rect.right) {
                        selectedEntity!!.setX(bismilahTimeline!!.rect.right)
                        selectedEntity!!.right = bismilahTimeline!!.rect.right + width12
                        pass = false
                        invalidate()
                        return true
                    }
                    if (isExist(mIsi3adaTimeline) && left2 <= mIsi3adaTimeline!!.rect.right) {
                        selectedEntity!!.setX(mIsi3adaTimeline!!.rect.right)
                        selectedEntity!!.right = mIsi3adaTimeline!!.rect.right + width12
                        pass = false
                        invalidate()
                        return true
                    }
                }
                if (selectedEntity is EntityTrslTimeline) {
                    val etl = selectedEntity as EntityTrslTimeline
                    if (etl.index > 0) {
                        val prev = getPreviewOrNextEntityTrslQuran(entityListTrslQuran, etl.index - 1, false)
                        if (prev != null && left2 <= prev.rect.right) {
                            selectedEntity!!.setX(prev.rect.right)
                            selectedEntity!!.right = prev.rect.right + width12
                            pass = false
                            invalidate()
                            return true
                        }
                    }
                    if (etl.index + 1 < entityListTrslQuran.size) {
                        val next = getPreviewOrNextEntityTrslQuran(entityListTrslQuran, etl.index + 1, true)
                        if (next != null && f39 >= next.rect.left) {
                            selectedEntity!!.setX(next.rect.left - width12)
                            selectedEntity!!.right = next.rect.left
                            pass = false
                            invalidate()
                            return true
                        }
                    }
                }
                if (selectedEntity is EntityBismilahTimeline) {
                    val ebt = selectedEntity as EntityBismilahTimeline
                    if (ebt === mIsi3adaTimeline && isExist(bismilahTimeline) && f39 >= bismilahTimeline!!.rect.left) {
                        selectedEntity!!.setX(bismilahTimeline!!.rect.left - width12)
                        selectedEntity!!.right = bismilahTimeline!!.rect.left
                        pass = false
                        invalidate()
                        return true
                    }
                    if (ebt === bismilahTimeline && isExist(mIsi3adaTimeline) && left2 <= mIsi3adaTimeline!!.rect.right) {
                        selectedEntity!!.setX(mIsi3adaTimeline!!.rect.right)
                        selectedEntity!!.right = mIsi3adaTimeline!!.rect.right + width12
                        pass = false
                        invalidate()
                        return true
                    }
                    val next = getPreviewOrNextEntityQuran(entityListQuran, ebt.index, true)
                    if (next != null && f39 >= next.rect.left) {
                        selectedEntity!!.setX(next.rect.left - width12)
                        selectedEntity!!.right = next.rect.left
                        pass = false
                        invalidate()
                        return true
                    }
                }
                if (selectedEntity is EntityAudio) {
                    val ea = selectedEntity as EntityAudio
                    if (ea.index > 0) {
                        val prev = getPreviewOrNextEntityAudio(entityListAudio, ea.index - 1, false)
                        if (prev != null && left2 <= prev.rect.right) {
                            selectedEntity!!.setX(prev.rect.right)
                            selectedEntity!!.right = prev.rect.right + width12
                            pass = false
                            invalidate()
                            return true
                        }
                    }
                    if (ea.index + 1 < entityListAudio.size) {
                        val next = getPreviewOrNextEntityAudio(entityListAudio, ea.index + 1, true)
                        if (next != null && f39 >= next.rect.left) {
                            selectedEntity!!.setX(next.rect.left - width12)
                            selectedEntity!!.right = next.rect.left
                            pass = false
                            invalidate()
                            return true
                        }
                    }
                }
                // Auto-move
                if (selectedEntity!!.rect.right < f39) {
                    if (selectedEntity!!.rect.left + getCurrentPosition() > DETECT_RIGHT_MOVE) {
                        if (!isAutoMove) {
                            if (SPEED > 0.0f) SPEED *= -1.0f
                            isAutoMove = true
                            time_start = System.currentTimeMillis()
                            autoScrollHandler.postDelayed(autoMoveRunnable!!, 100L)
                        } else { if (SPEED < 0.0f) SPEED *= -1.0f }
                    } else if (isAutoMove) {
                        isAutoMove = false
                        autoScrollHandler.removeCallbacks(autoMoveRunnable!!)
                    }
                } else if (selectedEntity!!.rect.left > 0.0f && selectedEntity!!.rect.left + getCurrentPosition() < -DETECT_LEFT_MOVE) {
                    if (!isAutoMove) {
                        if (SPEED < 0.0f) SPEED *= -1.0f
                        isAutoMove = true
                        time_start = System.currentTimeMillis()
                        autoScrollHandler.postDelayed(autoMoveRunnable!!, 100L)
                    } else { if (SPEED > 0.0f) SPEED *= -1.0f }
                } else if (isAutoMove) {
                    isAutoMove = false
                    autoScrollHandler.removeCallbacks(autoMoveRunnable!!)
                }
                if (!isAutoMove) {
                    selectedEntity!!.rect.left = left2
                    selectedEntity!!.rect.right = f39
                    isMove = true
                }
                pass = selectedEntity!!.rect.left < -TOLERANCE_X || selectedEntity!!.rect.left >= TOLERANCE_X
                invalidate()
            }
        }
        return gestureDetector!!.onTouchEvent(motionEvent)
    }

    fun flingY() {
        val ofFloat = ObjectAnimator.ofFloat(this, "FlingY", target, 0.0f)
        objectAnimator = ofFloat
        ofFloat.duration = 1000L
        objectAnimator!!.start()
    }

    fun setFlingY(f: Float) {
        target = f
        if (f <= 0.0f) {
            if (entityY + mScrollY >= height) {
                val f2 = mScrollY + target / 100.0f
                mScrollY = f2
                if (entityY + f2 < height) mScrollY = (height - entityY).toFloat()
                invalidate()
            }
            return
        }
        if (mScrollY < 0.0f) {
            val f4 = mScrollY + f / 100.0f
            mScrollY = f4
            if (f4 > 0.0f) mScrollY = 0.0f
            invalidate()
        }
    }

    fun getPreviewOrNextEntityAudio(list: List<EntityAudio>, i: Int, next: Boolean): EntityAudio? {
        if (next) {
            var idx = i
            while (idx < list.size) {
                if (list[idx].visible()) return list[idx]
                idx++
            }
            return null
        }
        var idx = i
        while (idx >= 0 && idx < list.size) {
            if (list[idx].visible()) return list[idx]
            idx--
        }
        return null
    }

    fun getPreviewOrNextEntityQuran(list: List<EntityQuranTimeline>, i: Int, next: Boolean): EntityQuranTimeline? {
        if (next) {
            var idx = i
            while (idx < list.size) {
                if (list[idx].visible()) return list[idx]
                idx++
            }
            return null
        }
        var idx = i
        while (idx >= 0 && idx < list.size) {
            if (list[idx].visible()) return list[idx]
            idx--
        }
        return null
    }

    fun getPreviewOrNextEntityTrslQuran(list: List<EntityTrslTimeline>, i: Int, next: Boolean): EntityTrslTimeline? {
        if (next) {
            var idx = i
            while (idx < list.size) {
                if (list[idx].visible()) return list[idx]
                idx++
            }
            return null
        }
        var idx = i
        while (idx >= 0 && idx < list.size) {
            if (list[idx].visible()) return list[idx]
            idx--
        }
        return null
    }

    fun getAudio(): EntityAudio? {
        for (size in entityListAudio.size - 1 downTo 0) {
            val ea = entityListAudio[size]
            if (ea.visible()) return ea
        }
        return null
    }

    fun getLastAyaQuran(): EntityQuranTimeline? {
        if (entityListQuran.isEmpty()) return null
        return entityListQuran[entityListQuran.size - 1]
    }

    fun getQuran(): EntityQuranTimeline? {
        for (size in entityListQuran.size - 1 downTo 0) {
            val eqt = entityListQuran[size]
            if (eqt.visible()) return eqt
        }
        return null
    }

    fun getTrslQuran(): EntityTrslTimeline? {
        for (size in entityListTrslQuran.size - 1 downTo 0) {
            val etl = entityListTrslQuran[size]
            if (etl.visible()) return etl
        }
        return null
    }

    fun calculMaxTime() {
        val audio = getAudio()
        var f: Float = 0.0f
        var f3: Float = 0.0f
        if (audio == null || audio.rect == null) {
            f = 0.0f
        } else if (audio.scaleFactor != scaleFactor) {
            f = audio.rect.right / audio.scaleFactor * scaleFactor
        } else {
            f = audio.rect.right
        }
        val quran = getQuran()
        if (quran == null || quran.rect == null) {
            if (isExist(bismilahTimeline)) {
                if (bismilahTimeline!!.scaleFactor != scaleFactor) {
                    f3 = scaleFactor * bismilahTimeline!!.rect.right / bismilahTimeline!!.scaleFactor
                } else {
                    f3 = bismilahTimeline!!.rect.right
                }
            } else if (isExist(mIsi3adaTimeline)) {
                if (mIsi3adaTimeline!!.scaleFactor != scaleFactor) {
                    f3 = scaleFactor * mIsi3adaTimeline!!.rect.right / mIsi3adaTimeline!!.scaleFactor
                } else {
                    f3 = mIsi3adaTimeline!!.rect.right
                }
            }
        } else if (quran.scaleFactor != scaleFactor) {
            f3 = quran.rect.right / quran.scaleFactor * scaleFactor
        } else {
            f3 = quran.rect.right
        }
        val trslQuran = getTrslQuran()
        if (trslQuran != null && trslQuran.rect != null) {
            if (trslQuran.scaleFactor != scaleFactor) {
                f3 = max(trslQuran.rect.right / trslQuran.scaleFactor * scaleFactor, f3)
            } else {
                f3 = max(trslQuran.rect.right, f3)
            }
        }
        val maxVal = max(f3, f)
        val secondInScreen = (maxVal / getSecond_in_screen() * 1000.0f).toInt()
        maxTime = secondInScreen
        duration = (secondInScreen / 1000.0f).toInt()
        timeLineW = maxVal / scaleFactor
    }

    fun update_current_cursur_position(i: Int) { current_cursur_position = i }





    fun unselectEntity() {
        val entity = selectedEntity
        if (entity != null) {
            entity.isSelect = false
            selectedEntity = null
        }
    }



    fun updateCursur(i: Int) {
        current_cursur_position = i
        val f = (-i * second_in_screen) / 1000.0f
        currentPosition = f
        scrolled_with_zoom = f * scaleFactor
        invalidate()
    }

    fun setPosCursur(i: Int) {
        current_cursur_position = i
        val f = (-i * second_in_screen) / 1000.0f
        currentPosition = f
        scrolled_with_zoom = f * scaleFactor
        invalidate()
    }

    fun updateCursur(f: Float) {
        val f2 = -f
        currentPosition = f2
        scrolled_with_zoom = f2 * scaleFactor
        invalidate()
    }

    fun setRedoUndo(imageButton: ImageButton, imageButton2: ImageButton) {
        btn_redo = imageButton
        btn_undo = imageButton2
    }

    fun undo() {
        try {
            if (entityList.isEmpty()) return
            val pop = entityList.pop()
            if (pop.second == EntityAction.DELETE) {
                pop.first.visible(true)
                val ev1 = pop.first.getEntityView()
                if (iTrimLineCallback != null && ev1 != null) {
                    iTrimLineCallback!!.onDelete(ev1)
                }
            } else if (pop.second == EntityAction.DELETE_MULTIPLE) {
                if (iTrimLineCallback != null) {
                    pop.first.visible(true)
                    val ev2 = pop.first.getEntityView()
                    if (ev2 != null) {
                        iTrimLineCallback!!.onDelete(ev2)
                    }
                    if (pop.first.getEntitiesGroup() != null) {
                        for (entity in pop.first.getEntitiesGroup()!!) {
                            entity.visible(true)
                            val ev3 = entity.getEntityView()
                            if (ev3 != null) {
                                iTrimLineCallback!!.onDelete(ev3)
                            }
                        }
                    }
                }
            } else if (pop.second == EntityAction.SPLIT) {
                pop.first.visible(false)
                undoEntityList.push(pop)
                val pop2 = entityList.pop()
                pop2.first.undo()
            } else if (pop.second != EntityAction.ADD) {
                pop.first.undo()
            } else {
                pop.first.visible(false)
                if (pop.first.getEntityView() != null) {
                    val ev = pop.first.getEntityView()!!
                    ev.isVisible = false
                    iTrimLineCallback?.onUpdate()
                }
            }
            undoEntityList.push(pop)
            if (iTrimLineCallback != null) {
                if (entityList.isEmpty()) iTrimLineCallback!!.enableUndo(false)
                iTrimLineCallback!!.enableRedo(true)
                iTrimLineCallback!!.onUpdateTime()
                val entity2 = selectedEntity
                if (entity2 != null && !entity2.visible()) {
                    unselectEntity()
                    iTrimLineCallback!!.onEmptySelect()
                }
            }
            invalidate()
        } catch (e: Exception) {
            Log.e("m_undo_expection", "" + e.message)
        }
    }

    fun redo() {
        try {
            if (undoEntityList.isEmpty()) return
            val pop = undoEntityList.pop()
            if (pop.second == EntityAction.DELETE) {
                pop.first.visible(false)
                val ev1 = pop.first.getEntityView()
                if (iTrimLineCallback != null && ev1 != null) {
                    iTrimLineCallback!!.onDelete(ev1)
                }
            } else if (pop.second == EntityAction.DELETE_MULTIPLE) {
                if (iTrimLineCallback != null) {
                    pop.first.visible(false)
                    val ev2 = pop.first.getEntityView()
                    if (ev2 != null) {
                        iTrimLineCallback!!.onDelete(ev2)
                    }
                    if (pop.first.getEntitiesGroup() != null) {
                        for (entity in pop.first.getEntitiesGroup()!!) {
                            entity.visible(false)
                            val ev3 = entity.getEntityView()
                            if (ev3 != null) {
                                iTrimLineCallback!!.onDelete(ev3)
                            }
                        }
                    }
                }
            } else if (pop.second == EntityAction.SPLIT) {
                pop.first.redo()
                entityList.push(pop)
                val pop2 = undoEntityList.pop()
                pop2.first.visible(true)
            } else if (pop.second != EntityAction.ADD) {
                pop.first.redo()
                pop.first.visible(true)
            } else {
                pop.first.visible(true)
            }
            entityList.push(pop)
            if (iTrimLineCallback != null) {
                if (undoEntityList.isEmpty()) iTrimLineCallback!!.enableRedo(false)
                iTrimLineCallback!!.enableUndo(true)
                iTrimLineCallback!!.onUpdateTime()
                val entity2 = selectedEntity
                if (entity2 != null && !entity2.visible()) {
                    unselectEntity()
                    iTrimLineCallback!!.onEmptySelect()
                }
            }
            invalidate()
        } catch (e: Exception) {
            Log.e("m_redo_expection", "" + e.message)
        }
    }

    fun handleItemInteraction(f: Float, f2: Float): Boolean {
        val z2 = rectFItemQuran?.contains(f, f2) == true
        val z3 = rectItemAudio?.contains(f, f2) == true
        val z4 = rectFItemTrslQuran?.contains(f, f2) == true
        var i = 0
        val z: Boolean
        if (z2 || z3 || z4) {
            selectedEntity?.isSelect = false
            var processQuranItemsSelection = if (z2) processQuranItemsSelection() else 0
            if (z3) processQuranItemsSelection += processAudioItemsSelection()
            if (z4) processQuranItemsSelection += processTrslQuranItemsSelection()
            i = processQuranItemsSelection
            z = true
        } else {
            z = deselectAllQuranItems() || deselectAllAudioItems() || deselectAllTrslQuranItems()
        }
        if (z) {
            if (iTrimLineCallback != null && (z2 || z3 || z4)) {
                selectedEntity = null
                iTrimLineCallback!!.onSelectMultiple(i)
            }
            invalidate()
        }
        return z2 || z3 || z4
    }

    private fun processQuranItemsSelection(): Int {
        var i = 0
        for (eqt in entityListQuran) {
            if (eqt.visible()) {
                val isSelect = eqt.isSelect
                eqt.isSelect = !isSelect
                eqt.setSelectMultiple(!isSelect)
                if (eqt.isSelect) i++
            }
        }
        if (isExist(bismilahTimeline)) {
            val isSelect2 = bismilahTimeline!!.isSelect
            bismilahTimeline!!.isSelect = !isSelect2
            bismilahTimeline!!.setSelectMultiple(!isSelect2)
            if (bismilahTimeline!!.isSelect) i++
        }
        if (isExist(mIsi3adaTimeline)) {
            val isSelect3 = mIsi3adaTimeline!!.isSelect
            mIsi3adaTimeline!!.isSelect = !isSelect3
            mIsi3adaTimeline!!.setSelectMultiple(!isSelect3)
            if (mIsi3adaTimeline!!.isSelect) i++
        }
        if (i > 0) clr_btn_quran = CLR_SELECT
        else clr_btn_quran = CLR_BTN_DEFAULT
        return i
    }

    private fun processTrslQuranItemsSelection(): Int {
        var i = 0
        for (etl in entityListTrslQuran) {
            if (etl.visible()) {
                val isSelect = etl.isSelect
                etl.isSelect = !isSelect
                etl.setSelectMultiple(!isSelect)
                if (etl.isSelect) i++
            }
        }
        if (i > 0) clr_btn_trsl = CLR_SELECT
        else clr_btn_trsl = CLR_BTN_DEFAULT
        return i
    }

    private fun processAudioItemsSelection(): Int {
        var i = 0
        for (ea in entityListAudio) {
            if (ea.visible()) {
                val isSelect = ea.isSelect
                ea.isSelect = !isSelect
                ea.setSelectMultiple(!isSelect)
                if (ea.isSelect) i++
            }
        }
        if (i > 0) clr_btn_audio = CLR_SELECT
        else clr_btn_audio = CLR_BTN_DEFAULT
        return i
    }

    private fun deselectAllQuranItems(): Boolean {
        var z = false
        if (isExist(bismilahTimeline) && bismilahTimeline!!.isSelect) {
            bismilahTimeline!!.isSelect = false
            bismilahTimeline!!.setSelectMultiple(false)
            z = true
        }
        if (isExist(mIsi3adaTimeline) && mIsi3adaTimeline!!.isSelect) {
            mIsi3adaTimeline!!.isSelect = false
            mIsi3adaTimeline!!.setSelectMultiple(false)
            z = true
        }
        for (eqt in entityListQuran) {
            if (eqt.visible() && eqt.isSelect) {
                eqt.isSelect = false
                eqt.setSelectMultiple(false)
                z = true
            }
        }
        if (z) clr_btn_quran = CLR_BTN_DEFAULT
        return z
    }

    private fun deselectAllTrslQuranItems(): Boolean {
        var z = false
        for (etl in entityListTrslQuran) {
            if (etl.visible() && etl.isSelect) {
                etl.isSelect = false
                etl.setSelectMultiple(false)
                z = true
            }
        }
        if (z) clr_btn_trsl = CLR_BTN_DEFAULT
        return z
    }

    private fun deselectAllAudioItems(): Boolean {
        var z = false
        for (ea in entityListAudio) {
            if (ea.visible() && ea.isSelect) {
                ea.isSelect = false
                ea.setSelectMultiple(false)
                z = true
            }
        }
        if (z) clr_btn_audio = CLR_BTN_DEFAULT
        return z
    }
}
