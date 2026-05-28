package hazem.nurmontage.videoquran.views.widget

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatButton
import hazem.nurmontage.videoquran.views.TypefaceCache

/**
 * Custom Button that applies the ReadexPro Medium font automatically.
 *
 * Originally: ButtonCustumFont.java
 * Converted to: ButtonCustumFont.kt — with shared [TypefaceCache] optimization
 *
 * Used for all app buttons that need the custom Arabic/Latin font.
 * Shares the same cached Typeface as [TextCustumFont] to prevent
 * redundant native allocations.
 *
 * @see hazem.nurmontage.videoquran.views.text.TextCustumFont
 * @see TypefaceCache
 */
class ButtonCustumFont : AppCompatButton {

    constructor(context: Context) : super(context) { init() }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) { init() }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr) { init() }

    private fun init() {
        TypefaceCache.get(resources.assets, FONT_PATH)?.let { setTypeface(it) }
    }

    companion object {
        private const val FONT_PATH = "fonts/ReadexPro_Medium.ttf"
    }
}
