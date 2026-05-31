package hazem.nurmontage.videoquran.ui.engine

import android.content.Context
import android.util.Log
import hazem.nurmontage.videoquran.model.*
import hazem.nurmontage.videoquran.core.common.Constants
import hazem.nurmontage.videoquran.utils.LocalPersistence
import hazem.nurmontage.videoquran.views.BlurredImageView
import hazem.nurmontage.videoquran.views.TrackEntityView
import java.io.File
import java.util.concurrent.Executor

/**
 * ExportPipeline
 *
 * Encapsulates the export / save flow for the engine screen.
 * In the original code this was spread across:
 *   - save()             (EngineActivity.kt:2998 — main export entry point)
 *   - saveTemplateTmp()  (EngineActivity.kt:3310 — auto-save on onPause)
 *   - saveTemplate()     (EngineActivity.kt:3548 — full template serialization before render)
 *
 * The export flow is:
 *   1. [save()] — prepares bitmap dimensions, crops, and iPad frames at export resolution
 *   2. [saveTemplate()] — serialises all entities (Quran, translation, bismilah, surah name,
 *      media, effects) from TrackEntityView into the Template object, then writes to disk
 *   3. Launches ProgressViewActivity for the FFmpeg render
 *
 * This class provides the skeleton for consolidating export operations.
 * Full implementation will be wired in a later refactoring pass.
 */
class ExportPipeline(
    private val context: Context,
    private val executor: Executor,
    private val onProgress: (Int) -> Unit,
    private val onSuccess: (File) -> Unit,
    private val onError: (String) -> Unit
) {
    // References set externally before calling save()
    var template: Template? = null
    var blurredImageView: BlurredImageView? = null
    var trackViewEntity: TrackEntityView? = null
    var uriBg: String? = null

    // Prevents double-export
    var isExporting: Boolean = false

    // ──────────────────────────────────────────────
    //  Main export entry point
    // ──────────────────────────────────────────────

    /**
     * Start the export pipeline.
     *
     * Corresponds to `EngineActivity.save()` (line ~2998).
     *
     * This method:
     *  1. Guards against double-export
     *  2. Prepares the BlurredImageView for export resolution
     *  3. Re-crops background and iPad frames at the target resolution
     *  4. Serialises the template via [saveTemplate]
     *  5. Calls [onSuccess] with the template file
     *
     * TODO: Move full body from EngineActivity.save()
     */
    fun save() {
        if (isExporting) return
        isExporting = true

        executor.execute {
            try {
                val tmpl = template ?: throw IllegalStateException("Template is null")
                val biv = blurredImageView ?: throw IllegalStateException("BlurredImageView is null")

                // TODO: Move bitmap preparation logic from EngineActivity.save()
                //  1. Calculate max dimension
                //  2. Handle HEART/BATTERY types (solid black bg)
                //  3. Handle video square + layered types
                //  4. Handle standard types — reload bg at export resolution
                //  5. Crop to aspect ratio, set iPad frame, blur
                //  6. Call setupBitmapDraw() to write bg file for FFmpeg

                saveTemplate()

                val templateFile = File(tmpl.idTemplate ?: "")
                onProgress(100)
                onSuccess(templateFile)
            } catch (e: Exception) {
                Log.e(TAG, "save failed", e)
                onError(e.message ?: "Export failed")
            } finally {
                isExporting = false
            }
        }
    }

    // ──────────────────────────────────────────────
    //  Template serialisation
    // ──────────────────────────────────────────────

    /**
     * Fully serialise the current project state into the template and write to disk.
     *
     * Corresponds to `EngineActivity.saveTemplate()` (line ~3548).
     *
     * Iterates through all entities in [TrackEntityView] and writes their
     * properties into the Template's serialisable lists:
     *   - [EntityQuranTemplate] list (Quran verses)
     *   - [EntityTranslationTemplate] list (translations)
     *   - [EntityBismilahTemplate] (isti3adha & bismilah)
     *   - [EntitySurahTemplate] (surah name)
     *   - [EntityMedia] list (audio/video media)
     *   - Effect audio list
     *
     * Then writes the Template object to local storage via [LocalPersistence].
     *
     * TODO: Move full body from EngineActivity.saveTemplate()
     */
    fun saveTemplate() {
        try {
            val tmpl = template ?: return
            val tve = trackViewEntity ?: return
            val biv = blurredImageView ?: return

            tmpl.setNewCode()
            tmpl.isGlass = biv.isGlass
            tmpl.currentCursur = tve.current_cursur_position
            tmpl.scale_timeline = tve.scaleFactor
            tmpl.duration = tve.maxTime
            tmpl.gradient = biv.color_gradient
            tmpl.color_ipad = biv.colorIpad()
            tmpl.quranEntityList.clear()
            tmpl.translationTemplateList.clear()
            tmpl.uri_bg = uriBg

            // TODO: Move full entity serialisation loop from EngineActivity.saveTemplate()
            //  - Iterate trackViewEntity.entityListQuran → EntityQuranTemplate
            //  - Iterate trackViewEntity.entityListTrslQuran → EntityTranslationTemplate
            //  - Serialise isti3adha / bismilah / surah name
            //  - Serialise media entities
            //  - Serialise effect audio list

            LocalPersistence.writeTemplate(context, tmpl, tmpl.idTemplate ?: "template_tmp", tmpl.idTemplate ?: "template_tmp")
        } catch (e: Exception) {
            Log.e(TAG, "saveTemplate failed", e)
        }
    }

    /**
     * Auto-save a temporary copy of the template (called on onPause).
     *
     * Corresponds to `EngineActivity.saveTemplateTmp()` (line ~3310).
     *
     * Similar to [saveTemplate] but writes to a fixed "template_tmp" key
     * and does NOT include media paths that would trigger re-download.
     *
     * TODO: Move full body from EngineActivity.saveTemplateTmp()
     */
    fun saveTemplateTmp() {
        try {
            val tmpl = template ?: return
            val tve = trackViewEntity ?: return
            val biv = blurredImageView ?: return

            if (tmpl.idTemplate == null) {
                tmpl.idTemplate = Constants.TEMPLATE_TMP
            }
            tmpl.setNewCode()
            tmpl.isGlass = biv.isGlass
            tmpl.currentCursur = tve.current_cursur_position
            tmpl.scale_timeline = tve.scaleFactor
            tmpl.gradient = biv.color_gradient
            tmpl.duration = tve.maxTime
            tmpl.color_ipad = biv.colorIpad()
            tmpl.quranEntityList.clear()
            tmpl.translationTemplateList.clear()
            tmpl.uri_bg = uriBg

            // TODO: Move full entity serialisation loop from EngineActivity.saveTemplateTmp()

            LocalPersistence.writeTemplate(context, tmpl, Constants.TEMPLATE_TMP, Constants.TEMPLATE_TMP)
        } catch (e: Exception) {
            Log.e(TAG, "saveTemplateTmp failed", e)
        }
    }

    // ──────────────────────────────────────────────
    //  Utility
    // ──────────────────────────────────────────────

    /**
     * Reset the export guard (e.g. when returning from render).
     */
    fun resetExportFlag() {
        isExporting = false
    }

    companion object {
        private const val TAG = "ExportPipeline"

        // Mirrored from EngineActivity companion
        private const val TEMPLATE_TMP = "template_tmp"
    }
}
