package hazem.nurmontage.videoquran.views.widget

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatButton
import hazem.nurmontage.videoquran.views.TypefaceCache

/**
 * Custom Button for billing/subscription UI with ReadexPro Medium font.
 *
 * Originally: ButtonCustumFontBilling.java
 * Converted to: ButtonCustumFontBilling.kt — with shared [TypefaceCache] optimization
 *
 * Functionally identical to [ButtonCustumFont] (same font file), but kept
 * as a separate class for XML layout compatibility in billing screens.
 * Separation allows applying different styling (colors, padding) in
 * subscription dialogs without affecting regular buttons.
 *
 * @see ButtonCustumFont
 * @see TypefaceCache
 */
class ButtonCustumFontBilling : AppCompatButton {

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
