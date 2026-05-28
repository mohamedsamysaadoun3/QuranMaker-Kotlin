package hazem.nurmontage.videoquran.model

import java.io.Serializable

/**
 * Represents a selected gallery item (image or video).
 *
 * Used by the gallery picker activities to track which media files
 * the user has selected for use as backgrounds or audio sources.
 *
 * Converted from the original GallerySelected.java.
 */
data class GallerySelected(
    val uri: String,
    val name: String,
    val path: String,
    val type: MediaType = MediaType.IMAGE,
    val duration: Long = 0L,
    val size: Long = 0L
) : Serializable {

    /**
     * Media type enum for distinguishing between images and videos.
     */
    enum class MediaType {
        IMAGE,
        VIDEO,
        AUDIO
    }

    /**
     * Check if this is a video selection.
     */
    fun isVideo(): Boolean = type == MediaType.VIDEO

    /**
     * Check if this is an image selection.
     */
    fun isImage(): Boolean = type == MediaType.IMAGE

    /**
     * Check if this is an audio selection.
     */
    fun isAudio(): Boolean = type == MediaType.AUDIO

    companion object {
        private const val serialVersionUID = 1L
    }
}
