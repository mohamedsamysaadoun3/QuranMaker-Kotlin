package hazem.nurmontage.videoquran.model

import java.io.Serializable

/**
 * Serializable RectF wrapper — preserves exact field names for serialization compatibility.
 * JADX obfuscated names cleaned: f435b→b, f436l→l, f437r→r, f438t→t
 */
data class MRectF(
    var l: Float = 0f,
    var t: Float = 0f,
    var r: Float = 0f,
    var b: Float = 0f
) : Serializable
