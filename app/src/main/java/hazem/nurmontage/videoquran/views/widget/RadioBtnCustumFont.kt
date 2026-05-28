package hazem.nurmontage.videoquran.views.widget

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatRadioButton
import hazem.nurmontage.videoquran.views.TypefaceCache

/**
 * Custom RadioButton that applies the ReadexPro Medium font automatically.
 *
 * Originally: RadioBtnCustumFont.java
 * Converted to: RadioBtnCustumFont.kt — with shared [TypefaceCache] optimization
 *
 * Used for radio button groups in settings and export dialogs
 * that need the custom Arabic/Latin font for their text labels.
 *
 * @see TypefaceCache
 */
class RadioBtnCustumFont : AppCompatRadioButton {

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
