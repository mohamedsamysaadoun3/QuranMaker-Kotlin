package hazem.nurmontage.videoquran.views.text

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import hazem.nurmontage.videoquran.views.TypefaceCache

/**
 * Custom EditText that applies the "خط الإبل" (Al-Ibil) Arabic font automatically.
 *
 * Originally: EditTextCustumFont.java
 * Converted to: EditTextCustumFont.kt — with shared [TypefaceCache] optimization
 *
 * This view is used for Arabic text input fields where the user expects
 * to see their input rendered in an Ottoman calligraphic style. The
 * "خط الإبل" font is specifically chosen for text editing contexts
 * where readability at smaller sizes is important.
 *
 * The font is loaded from: `assets/fonts/arabic/خط الإبل.otf`
 *
 * @see TypefaceCache
 */
class EditTextCustumFont : AppCompatEditText {

    constructor(context: Context) : super(context) { init() }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) { init() }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr) { init() }

    private fun init() {
        TypefaceCache.get(resources.assets, FONT_PATH)?.let { setTypeface(it) }
    }

    companion object {
        private const val FONT_PATH = "fonts/arabic/خط الإبل.otf"
    }
}
