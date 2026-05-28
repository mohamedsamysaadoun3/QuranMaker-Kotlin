package hazem.nurmontage.videoquran.views.text

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import hazem.nurmontage.videoquran.views.TypefaceCache

/**
 * Custom TextView that applies the "خط حفص" (Hafs) Ottoman font automatically.
 *
 * Originally: AyaCustumFont.java
 * Converted to: AyaCustumFont.kt — with shared [TypefaceCache] optimization
 *
 * This view is specifically designed for rendering Quran ayah text in the
 * Hafs recitation style, which is the most widely used Quranic script
 * in the Muslim world. The font is loaded from:
 * `assets/fonts/arabic/خط حفص.ttf`
 *
 * Used in Quran search results, ayah preview cards, and recitation views.
 *
 * @see TypefaceCache
 */
class AyaCustumFont : AppCompatTextView {

    constructor(context: Context) : super(context) { init() }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) { init() }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr) { init() }

    private fun init() {
        TypefaceCache.get(resources.assets, FONT_PATH)?.let { setTypeface(it) }
    }

    companion object {
        private const val FONT_PATH = "fonts/arabic/خط حفص.ttf"
    }
}
