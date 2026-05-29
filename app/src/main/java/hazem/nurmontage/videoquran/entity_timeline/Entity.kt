package hazem.nurmontage.videoquran.entity_timeline

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.RectF
import hazem.nurmontage.videoquran.core.common.Constants.EntityAction
import hazem.nurmontage.videoquran.core.common.StackEntity
import hazem.nurmontage.videoquran.model.EntityView
import java.util.Stack

/**
 * Abstract base class for every visual block on the timeline editor.
 *
 * Manages the block's bounding rectangle, trim handles, selection state,
 * undo/redo stack, scale factor, fade durations, and the rendering pipeline.
 * Concrete subclasses must implement drawing, hit-testing, and position
 * mutation methods.
 *
 * Originally: `hazem.nurmontage.videoquran.entity_timeline.Entity`
 */
abstract class Entity(secondInScreen: Float) {

    // ══════════════════════════════════════════════
    //  Protected state (shared with subclasses)
    // ══════════════════════════════════════════════

    var rect: RectF = RectF()
    protected var rectFLeft: RectF = RectF()
    protected var rectFRight: RectF = RectF()
    protected var selectTrim: RectF? = null
    var trimType: Int = -1
    var color: Int = 0
    var end: Float = 0f
    var left: Float = 0f
    var right: Float = 0f
    var max: Float = 0f
    var start: Float = 0f
    var secondInScreen: Float = secondInScreen
    var padding: Float = 0f
    var round: Float = 0f
    var isSelect: Boolean = false
    var isVisible: Boolean = true
    protected var indexStartThumbnail: Int = 0
    protected var indexEndThumbnail: Int = 0

    // ══════════════════════════════════════════════
    //  Private state
    // ══════════════════════════════════════════════

    private var audioId: String? = null
    private var currentStackEntity: StackEntity? = null
    private var entitiesGroup: List<Entity>? = null
    private var entityView: EntityView? = null
    private var fadeIn: Float = 0f
    private var fadeOut: Float = 0f
    private var frameId: String? = null
    private var index: Int = 0
    private var isSelectMultiple: Boolean = false
    private var isSplit: Boolean = false
    private var isTrimLeft: Boolean = false
    private var isVideo: Boolean = false
    private var offset: Float = 0f
    private var offsetLeft: Float = 0f
    private var offsetRight: Float = 0f
    private var onDown: Float = 0f
    private var onTapTime: Float = 0f
    private var scaleFactor: Float = 1.0f
    private var colorSelectMultiple: Int = -409555
    private var entityAction: EntityAction = EntityAction.ADD
    private var visible: Boolean = true

    private val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paintStroke: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colorSelectMultiple
        style = Paint.Style.STROKE
    }
    private val path: Path = Path()
    private val undoRect: Stack<StackEntity> = Stack()
    private val rectList: Stack<StackEntity> = Stack()

    // ══════════════════════════════════════════════
    //  Abstract contract
    // ══════════════════════════════════════════════

    abstract fun contains(point: PointF): Boolean
    abstract fun draw(canvas: Canvas)
    abstract fun draw(canvas: Canvas, w: Int, h: Int)
    abstract fun getDownX(): Float
    abstract fun getH(): Float
    abstract fun getLeft(): Float
    abstract fun getRect(): RectF
    abstract fun getRight(): Float
    abstract fun getSelectTrim(): RectF?
    abstract fun getTrimType(): Int
    abstract fun onTouch(point: PointF): Boolean
    abstract fun onUpLeft()
    abstract fun onUpRight()
    abstract fun setDownX(downX: Float)
    abstract fun setLastLeft(lastLeft: Float)
    abstract fun setLastRight(lastRight: Float)
    abstract fun setRight(right: Float)
    abstract fun setSelect(select: Boolean)
    abstract fun setX(x: Float)
    abstract fun setY(y: Float)
    abstract fun updateStartTrim()

    open fun release() {}

    // ══════════════════════════════════════════════
    //  Entity group
    // ══════════════════════════════════════════════

    fun getEntitiesGroup(): List<Entity>? = entitiesGroup
    fun setEntitiesGroup(entitiesGroup: List<Entity>?) { this.entitiesGroup = entitiesGroup }

    // ══════════════════════════════════════════════
    //  Selection state
    // ══════════════════════════════════════════════

    fun setSelectMultiple(selectMultiple: Boolean) { isSelectMultiple = selectMultiple }
    fun isSelectMultiple(): Boolean = isSelectMultiple
    fun isSelect(): Boolean = isSelect

    fun setVisible(visible: Boolean) { isVisible = visible }

    // ══════════════════════════════════════════════
    //  Color & appearance
    // ══════════════════════════════════════════════

    fun setColorSelectMultiple(color: Int) { colorSelectMultiple = color }

    // ══════════════════════════════════════════════
    //  Second-in-screen
    // ══════════════════════════════════════════════

    fun getSecondInScreen(): Float = secondInScreen
    fun setSecondInScreen(secondInScreen: Float) { this.secondInScreen = secondInScreen }

    // ══════════════════════════════════════════════
    //  Audio & frame IDs
    // ══════════════════════════════════════════════

    fun getAudioId(): String? = audioId
    fun setAudioId(audioId: String?) { this.audioId = audioId }
    fun getFrameId(): String? = frameId
    fun setFrameId(frameId: String?) { this.frameId = frameId }

    // ══════════════════════════════════════════════
    //  Thumbnail indices
    // ══════════════════════════════════════════════

    fun getIndexEndThumbnail(): Int = indexEndThumbnail
    fun getIndexStartThumbnail(): Int = indexStartThumbnail

    // ══════════════════════════════════════════════
    //  Trim & split
    // ══════════════════════════════════════════════

    fun setTrimLeft(trimLeft: Boolean) { isTrimLeft = trimLeft }
    fun isTrimLeft(): Boolean = isTrimLeft
    fun setSplit(split: Boolean) { isSplit = split }
    fun isSplit(): Boolean = isSplit
    fun getRound(): Float = round
    fun resetTrimType() { trimType = -1 }

    // ══════════════════════════════════════════════
    //  Fade
    // ══════════════════════════════════════════════

    fun getFadeIn(): Float = fadeIn
    fun getFadeOut(): Float = fadeOut
    fun setFadeIn(fadeIn: Float) { this.fadeIn = fadeIn }
    fun setFadeOut(fadeOut: Float) { this.fadeOut = fadeOut }

    // ══════════════════════════════════════════════
    //  Action & index
    // ══════════════════════════════════════════════

    fun setEntityAction(entityAction: EntityAction) { this.entityAction = entityAction }
    fun setIndex(index: Int) { this.index = index }
    fun getIndex(): Int = index

    // ══════════════════════════════════════════════
    //  EntityView association
    // ══════════════════════════════════════════════

    fun setEntityView(entityView: EntityView?) { this.entityView = entityView }
    fun getEntityView(): EntityView? = entityView

    // ══════════════════════════════════════════════
    //  Scale factor
    // ══════════════════════════════════════════════

    fun setScaleFactor(scaleFactor: Float) { this.scaleFactor = scaleFactor }
    fun getScaleFactor(): Float = scaleFactor

    // ══════════════════════════════════════════════
    //  Visibility (non-select)
    // ══════════════════════════════════════════════

    fun visible(): Boolean = visible
    fun visible(visible: Boolean) { this.visible = visible }

    // ══════════════════════════════════════════════
    //  Offset & tap time
    // ══════════════════════════════════════════════

    fun getOffset(): Float = offset
    fun setOffset(offset: Float) { this.offset = offset }
    fun getOffsetLeft(): Float = offsetLeft
    fun setOffsetLeft(offsetLeft: Float) { this.offsetLeft = offsetLeft }
    fun getOffsetRight(): Float = offsetRight
    fun setOffsetRight(offsetRight: Float) { this.offsetRight = offsetRight }
    fun getOnDown(): Float = onDown
    fun setOnTapTime(onTapTime: Float, onDown: Float) {
        this.onTapTime = onTapTime
        this.onDown = onDown
    }
    fun getOnTapTime(): Float = onTapTime

    // ══════════════════════════════════════════════
    //  Current stack entity
    // ══════════════════════════════════════════════

    fun getCurrentStackEntity(): StackEntity? = currentStackEntity

    fun setCurrentRect() {
        if (currentStackEntity != null) return
        currentStackEntity = StackEntity(
            RectF(
                rect.left / scaleFactor,
                rect.top / scaleFactor,
                rect.right / scaleFactor,
                rect.bottom / scaleFactor
            ),
            offset, end, start, left, right, max,
            getOffsetRight(), getOffsetLeft()
        )
    }

    // ══════════════════════════════════════════════
    //  Scale update
    // ══════════════════════════════════════════════

    fun updateRect(newScale: Float) {
        if (newScale == scaleFactor) return
        rect.left = (rect.left / scaleFactor) * newScale
        rect.right = (rect.right / scaleFactor) * newScale
        setX(rect.left)
        setRight(rect.right)
        scaleFactor = newScale
    }

    // ══════════════════════════════════════════════
    //  Render pipeline
    // ══════════════════════════════════════════════

    /**
     * Main render pass — draws the block background, text content,
     * and selection/trim handles.
     */
    fun update(canvas: Canvas) {
        paint.color = color
        if (!isVideo) {
            canvas.drawRoundRect(rect, round, round, paint)
            canvas.save()
            canvas.clipRect(rect)
            draw(canvas)
            canvas.restore()
        } else {
            canvas.save()
            path.reset()
            path.addRoundRect(rect, round, round, Path.Direction.CW)
            canvas.clipPath(path)
            draw(canvas)
            canvas.restore()
        }
        if (isSelect) {
            paintStroke.strokeWidth = rect.height() * 0.05f
            paintStroke.color = colorSelectMultiple
            when (getTrimType()) {
                0 -> {
                    rectFLeft.left = rect.left - rectFLeft.width()
                    rectFLeft.right = rect.left
                    rectFLeft.top = rect.top + padding
                    rectFLeft.bottom = rect.bottom - padding
                    paint.color = colorSelectMultiple
                    canvas.drawRoundRect(rectFLeft, round, round, paint)
                }
                1 -> {
                    rectFRight.right = rect.right + rectFRight.width()
                    rectFRight.left = rect.right
                    rectFRight.top = rect.top + padding
                    rectFRight.bottom = rect.bottom - padding
                    paint.color = colorSelectMultiple
                    canvas.drawRoundRect(rectFRight, round, round, paint)
                }
                else -> {
                    if (isSelectMultiple) return
                    rectFRight.right = rect.right + rectFRight.width()
                    rectFRight.left = rect.right
                    rectFRight.top = rect.top + padding
                    rectFRight.bottom = rect.bottom - padding
                    rectFLeft.left = rect.left - rectFLeft.width()
                    rectFLeft.right = rect.left
                    rectFLeft.top = rect.top + padding
                    rectFLeft.bottom = rect.bottom - padding
                    paint.color = colorSelectMultiple
                    canvas.drawRoundRect(rectFLeft, round, round, paint)
                    canvas.drawRoundRect(rectFRight, round, round, paint)
                }
            }
        } else if (isVideo) {
            paintStroke.strokeWidth = rect.height() * 0.025f
            paintStroke.color = -8355712
            canvas.drawRoundRect(rect, round, round, paintStroke)
        }
    }

    /**
     * Render pass with additional dimension parameters for thumbnail rendering.
     */
    fun update(canvas: Canvas, w: Int, h: Int) {
        paint.color = color
        if (!isVideo) {
            canvas.drawRoundRect(rect, round, round, paint)
            canvas.save()
            canvas.clipRect(rect)
            draw(canvas, w, h)
            canvas.restore()
        } else {
            canvas.save()
            path.reset()
            path.addRoundRect(rect, round, round, Path.Direction.CW)
            canvas.clipPath(path)
            draw(canvas, w, h)
            canvas.restore()
        }
        if (isSelect) {
            paintStroke.strokeWidth = rect.height() * 0.05f
            paint.color = colorSelectMultiple
            paintStroke.color = colorSelectMultiple
            when (getTrimType()) {
                0 -> {
                    rectFLeft.left = rect.left - rectFLeft.width()
                    rectFLeft.right = rect.left
                    rectFLeft.top = rect.top + padding
                    rectFLeft.bottom = rect.bottom - padding
                    canvas.drawRoundRect(rectFLeft, round, round, paint)
                }
                1 -> {
                    rectFRight.right = rect.right + rectFRight.width()
                    rectFRight.left = rect.right
                    rectFRight.top = rect.top + padding
                    rectFRight.bottom = rect.bottom - padding
                    canvas.drawRoundRect(rectFRight, round, round, paint)
                }
                else -> {
                    if (!isSelectMultiple()) {
                        rectFRight.right = rect.right + rectFRight.width()
                        rectFRight.left = rect.right
                        rectFRight.top = rect.top + padding
                        rectFRight.bottom = rect.bottom - padding
                        rectFLeft.left = rect.left - rectFLeft.width()
                        rectFLeft.right = rect.left
                        rectFLeft.top = rect.top + padding
                        rectFLeft.bottom = rect.bottom - padding
                        canvas.drawRoundRect(rectFLeft, round, round, paint)
                        canvas.drawRoundRect(rectFRight, round, round, paint)
                    }
                }
            }
            canvas.drawRoundRect(rect, round, round, paintStroke)
        } else if (isVideo) {
            paintStroke.strokeWidth = rect.height() * 0.025f
            paintStroke.color = -8355712
            canvas.drawRoundRect(rect, round, round, paintStroke)
        }
    }

    // ══════════════════════════════════════════════
    //  Undo / Redo
    // ══════════════════════════════════════════════

    fun onChange() {
        val current = currentStackEntity ?: return
        rectList.push(current)
        rectList.push(
            StackEntity(
                RectF(
                    rect.left / scaleFactor,
                    rect.top / scaleFactor,
                    rect.right / scaleFactor,
                    rect.bottom / scaleFactor
                ),
                offset, end, start, left, right, max,
                getOffsetRight(), getOffsetLeft()
            )
        )
        currentStackEntity = null
    }

    fun undo() {
        try {
            if (rectList.isEmpty()) return
            val top = rectList.pop()
            val previous = rectList.pop()
            undoRect.push(top)
            undoRect.push(previous)
            restoreFromStackEntity(previous)
        } catch (_: Exception) {
        }
    }

    fun redo() {
        try {
            if (undoRect.isEmpty()) return
            val top = undoRect.pop()
            val next = undoRect.pop()
            rectList.push(top)
            rectList.push(next)
            restoreFromStackEntity(next)
        } catch (_: Exception) {
        }
    }

    private fun restoreFromStackEntity(se: StackEntity) {
        offsetLeft = se.offset_left
        offsetRight = se.offset_right
        rect = RectF(
            se.rectF.left * scaleFactor,
            se.rectF.top * scaleFactor,
            se.rectF.right * scaleFactor,
            se.rectF.bottom * scaleFactor
        )
        offset = se.offset
        end = se.end
        start = se.start
        right = se.right
        left = se.left
        max = se.max
    }
}
