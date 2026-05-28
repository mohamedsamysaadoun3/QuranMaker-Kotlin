package hazem.nurmontage.videoquran.model

import hazem.nurmontage.videoquran.constant.IpadType
import hazem.nurmontage.videoquran.constant.ResizeType
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable

/**
 * Master template that encapsulates the entire video project state:
 *   - Canvas dimensions, FPS, resolution, resize type
 *   - iPad frame type, color, gradient, glass effect
 *   - Background URIs (image, video, FFmpeg pre-rendered)
 *   - Quran ayah entities, translations, bismilah, surah name
 *   - Media entities (audio/video clips)
 *   - Progress bar overlay configuration
 *   - Free-form decorative elements
 *
 * Serialization-critical field names preserved verbatim:
 *   uri_bg, uri_bg_ffmpeg, uri_media_video, uri_video, folder_template,
 *   frame_bg, name_drawable, extension, isVideoSquare, isGlass, isNewCode,
 *   scale_timeline, imgResize, resizeType, ipad_type, color_ipad, index_color,
 *   currentCursur, duration, duration_video_media, idTemplate
 *
 * Note: `FileInfo` inner class from MFileUtils is kept as a nullable
 *       Serializable placeholder until MFileUtils is migrated.
 */
class Template : Serializable {

    // ── Canvas dimensions ────────────────────────────────────────
    var width: Int = 0
    var height: Int = 0

    // ── Project metadata ─────────────────────────────────────────
    var idTemplate: String? = null
    var folder_template: String? = null
    var name_drawable: String? = null
    var extension: String? = null
    var fileInfo: Serializable? = null  // MFileUtils.FileInfo placeholder

    // ── Timeline state ───────────────────────────────────────────
    var currentCursur: Int = 0
    var duration: Int = 0
    var duration_video_media: Int = 0
    var scale_timeline: Float = 0.5f
    var isNewCode: Boolean = false

    // ── Resolution / FPS / Resize ────────────────────────────────
    var resolution: String = "720p"
    var fps: Int = 30
    var resizeType: Int = ResizeType.SOCIAL_STORY.ordinal
    var imgResize: String = "i_9:16"

    // ── Background URIs ──────────────────────────────────────────
    var uri_bg: String? = null
    var uri_bg_ffmpeg: String? = null
    var uri_media_video: String? = null
    var uri_video: String? = null
    var uri_original_upload_video: String? = null
    var uri_upload_extract_audio_video: String? = null
    var frame_bg: String? = null

    // ── iPad frame configuration ─────────────────────────────────
    var ipad_type: Int = IpadType.IPAD.ordinal()
    var color_ipad: Int = -1
    var index_color: Int = -1
    var gradient: Gradient? = null
    var isGlass: Boolean = false
    var isVideoSquare: Boolean = false

    // ── Square/circle crop region ────────────────────────────────
    var x_square: Float = 0.3f
    var y_square: Float = 0.2f
    var width_square: Float = 0.37218544f
    var height_square: Float = 0.41986755f
    var squareBitmapModel: SquareBitmapModel? = null
        get() = field ?: SquareBitmapModel().also { field = it }

    // ── Drawing translation offset ───────────────────────────────
    var mDrawingTranslationX: Float = 0f
    var mDrawingTranslationY: Float = 0f

    // ── Timer / progress overlay ─────────────────────────────────
    var mTimeModel: TimeModel? = null
    var entityProgressTemplate: EntityProgressTemplate? = null

    // ── Bismilah / Isti3ada templates ────────────────────────────
    var entityBismilahTemplate: EntityBismilahTemplate? = null
    var entityIsti3adaTemplate: EntityBismilahTemplate? = null

    // ── Surah name template ──────────────────────────────────────
    var entitySurahTemplate: EntitySurahTemplate? = null

    // ── Entity lists ─────────────────────────────────────────────
    private val entityMediaList: MutableList<EntityMedia> = arrayListOf()
    private val quranEntityList: MutableList<EntityQuranTemplate> = arrayListOf()
    private val translationTemplateList: MutableList<EntityTranslationTemplate> = arrayListOf()
    private val freeElementList: MutableList<FreeElement> = arrayListOf()

    // ── Media entity helpers ─────────────────────────────────────
    fun addMedia(entity: EntityMedia) { entityMediaList.add(entity) }
    fun getEntityMediaList(): List<EntityMedia> = entityMediaList

    // ── Quran entity helpers ─────────────────────────────────────
    fun addQuranEntityList(entity: EntityQuranTemplate) { quranEntityList.add(entity) }
    fun getQuranEntityList(): List<EntityQuranTemplate> = quranEntityList

    // ── Translation entity helpers ───────────────────────────────
    fun addTrslEntityList(entity: EntityTranslationTemplate) { translationTemplateList.add(entity) }
    fun getTranslationTemplateList(): List<EntityTranslationTemplate> = translationTemplateList

    // ── Free element helpers ─────────────────────────────────────
    fun addFreeElement(element: FreeElement) { freeElementList.add(element) }
    fun getFreeElementList(): List<FreeElement> = freeElementList
    fun removeFreeElement(element: FreeElement) { freeElementList.remove(element) }

    // ── Dimension helpers ────────────────────────────────────────
    fun setWidthAndHeight(w: Int, h: Int) { width = w; height = h }
    fun setDrawingTranslation(dx: Float, dy: Float) { mDrawingTranslationX = dx; mDrawingTranslationY = dy }

    fun geTypeResize(): Int = resizeType

    /** Deep-copy via serialization round-trip (same as original Java). */
    fun duplicate(): Template? {
        return try {
            ByteArrayOutputStream().use { baos ->
                ObjectOutputStream(baos).use { oos ->
                    oos.writeObject(this)
                    oos.flush()
                }
                ObjectInputStream(ByteArrayInputStream(baos.toByteArray())).use { ois ->
                    ois.readObject() as Template
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}
