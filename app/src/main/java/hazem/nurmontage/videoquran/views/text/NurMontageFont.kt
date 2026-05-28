package hazem.nurmontage.videoquran.views.text

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import hazem.nurmontage.videoquran.views.TypefaceCache

/**
 * Custom TextView with ReadexPro Medium font for the NurMontage branding.
 *
 * Originally: NurMontageFont.java
 * Converted to: NurMontageFont.kt — with shared [TypefaceCache] optimization
 *
 * Functionally identical to [TextCustumFont] (same font file), but kept
 * as a separate class for XML layout compatibility. Used specifically
 * for NurMontage brand text (app name, credits, about section).
 *
 * @see TextCustumFont
 * @see TypefaceCache
 */
class NurMontageFont : AppCompatTextView {

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
