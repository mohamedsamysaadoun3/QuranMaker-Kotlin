package hazem.nurmontage.videoquran.ui.engine

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import android.view.SurfaceView
import hazem.nurmontage.videoquran.utils.video.SmoothVideoAnimator
import hazem.nurmontage.videoquran.views.TrackEntityView
import hazem.nurmontage.videoquran.model.Template

/**
 * VideoPlayerController
 *
 * Encapsulates video background playback for the engine screen.
 * In the original EngineActivity, the video player logic was spread across:
 *   - start() / stop()  (EngineTimelineManager.kt — SmoothVideoAnimator for frame-by-frame)
 *   - pausePlayer()      (EngineActivity.kt — pauses audio MediaPlayers & timeline animation)
 *   - initTypeVideo()    (EngineActivity.kt — sets up video background from template)
 *   - handleVideo() / handleVideoRunnable()  (EngineUIHelper.kt — picks & extracts frames)
 *   - updateFrame()      (EngineTimelineManager.kt — seeks to a frame on scroll)
 *
 * This class provides the skeleton for consolidating all video-player lifecycle
 * operations.  Full implementation will be wired in a later refactoring pass
 * once cross-references are untangled.
 */
class VideoPlayerController(
    private val context: Context,
    private val previewView: SurfaceView? = null
) {
    private var mediaPlayer: MediaPlayer? = null
    private var _isPlaying: Boolean = false
    private var smoothVideoAnimator: SmoothVideoAnimator? = null

    // ──────────────────────────────────────────────
    //  Playback control
    // ──────────────────────────────────────────────

    /**
     * Start playback of a video file at [path].
     *
     * In the original code this was handled by `EngineActivity.start()` which
     * creates a [SmoothVideoAnimator] that reads pre-extracted frames and fires
     * per-frame callbacks.
     *
     * TODO: Wire to TrackEntityView + Template + frame update listener
     */
    fun startPlayback(path: String) {
        try {
            release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(path)
                if (previewView != null) {
                    setDisplay(previewView.holder)
                }
                setOnPreparedListener { mp ->
                    mp?.start()
                    _isPlaying = true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            Log.e(TAG, "startPlayback failed", e)
        }
    }

    /**
     * Start playback from a [Uri].
     *
     * TODO: Wire to the full SmoothVideoAnimator flow from EngineActivity.start()
     */
    fun startPlayback(uri: Uri) {
        try {
            release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, uri)
                if (previewView != null) {
                    setDisplay(previewView.holder)
                }
                setOnPreparedListener { mp ->
                    mp?.start()
                    _isPlaying = true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            Log.e(TAG, "startPlayback(uri) failed", e)
        }
    }

    /**
     * Pause the current video playback.
     *
     * Corresponds to `EngineActivity.pausePlayer()` which pauses all
     * EntityAudio MediaPlayers, the timeline animation, and updates UI.
     *
     * TODO: Also pause audio MediaPlayers (EntityAudio list) & timeline
     */
    fun pause() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.pause()
                }
            }
            smoothVideoAnimator?.stop()
            _isPlaying = false
        } catch (e: Exception) {
            Log.e(TAG, "pause failed", e)
        }
    }

    /**
     * Resume video playback after a pause.
     *
     * TODO: Resume audio MediaPlayers & timeline animation as well
     */
    fun resume() {
        try {
            mediaPlayer?.let {
                if (!it.isPlaying) {
                    it.start()
                    _isPlaying = true
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "resume failed", e)
        }
    }

    /**
     * Stop playback and reset the player state.
     *
     * Corresponds to `EngineActivity.stop()` which stops the SmoothVideoAnimator.
     */
    fun stop() {
        try {
            mediaPlayer?.stop()
            smoothVideoAnimator?.stop()
            _isPlaying = false
        } catch (e: Exception) {
            Log.e(TAG, "stop failed", e)
        }
    }

    /**
     * Release all player resources.  Call in onDestroy().
     *
     * Corresponds to `EngineActivity.onDestroy()` → `pausePlayer()` + cleanup.
     */
    fun release() {
        try {
            mediaPlayer?.release()
        } catch (e: Exception) {
            Log.e(TAG, "release failed", e)
        }
        mediaPlayer = null
        smoothVideoAnimator = null
        _isPlaying = false
    }

    // ──────────────────────────────────────────────
    //  Seek / query
    // ──────────────────────────────────────────────

    fun seekTo(ms: Int) {
        try {
            mediaPlayer?.seekTo(ms)
        } catch (e: Exception) {
            Log.e(TAG, "seekTo failed", e)
        }
    }

    fun isPlaying(): Boolean = _isPlaying

    fun getDuration(): Int = try { mediaPlayer?.duration ?: 0 } catch (_: Exception) { 0 }

    fun getCurrentPosition(): Int = try { mediaPlayer?.currentPosition ?: 0 } catch (_: Exception) { 0 }

    // ──────────────────────────────────────────────
    //  Smooth video animator (frame-by-frame bg)
    // ──────────────────────────────────────────────

    /**
     * Set the [SmoothVideoAnimator] used for frame-by-frame background playback.
     *
     * In the original code `EngineActivity.start()` creates the animator and
     * stores it in `animator_frame_video`.  This setter allows external wiring
     * until the full refactoring pass.
     */
    fun setSmoothVideoAnimator(animator: SmoothVideoAnimator?) {
        smoothVideoAnimator = animator
    }

    /**
     * Start the frame-by-frame animator.
     *
     * TODO: Move the full SmoothVideoAnimator creation logic from
     *       EngineActivity.start() here.
     */
    fun startFrameAnimator(trackView: TrackEntityView, template: Template) {
        // TODO: Implement full frame-animator startup
        //  (see EngineTimelineManager.kt → EngineActivity.start())
    }

    companion object {
        private const val TAG = "VideoPlayerController"
    }
}
