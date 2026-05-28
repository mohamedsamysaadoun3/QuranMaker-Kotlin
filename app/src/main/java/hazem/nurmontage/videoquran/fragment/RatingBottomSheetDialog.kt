package hazem.nurmontage.videoquran.fragment

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.views.text.TextCustumFont
import hazem.nurmontage.videoquran.views.widget.ButtonCustumFont

/**
 * BottomSheet dialog for requesting the user to rate the app on the Play Store.
 *
 * Originally: RatingBottomSheetDialog.java
 * Converted to: RatingBottomSheetDialog.kt — idiomatic Kotlin, full logic preserved
 *
 * Features:
 * - Three action buttons: "Rate Now", "Later", and "No Thanks"
 * - "Rate Now" opens the Google Play Store listing for the app
 * - "No Thanks" sets a flag so the dialog is never shown again
 * - "Later" simply dismisses the dialog (will show again next time)
 * - SharedPreferences-based persistence for the "never ask again" flag
 * - Static helper [shouldShowRatingDialog] to check if the dialog should be shown
 * - Static helper [setNeverAskAgain] to set the "never ask again" flag
 * - Optional Resources constructor for localized string injection
 *
 * The dialog uses custom views ([TextCustumFont] and [ButtonCustumFont])
 * to maintain consistent Arabic/RTL typography throughout the app.
 */
class RatingBottomSheetDialog : BottomSheetDialogFragment {

    companion object {
        private const val KEY_NEVER_ASK_AGAIN = "never_ask_again_new"
        private const val PREFS_NAME = "app_prefs_new_mars"

        /**
         * Sets the "never ask again" flag in SharedPreferences.
         * When set to true, [shouldShowRatingDialog] will return false,
         * preventing the rating dialog from being shown again.
         *
         * @param context The application context
         * @param neverAsk Whether to suppress the rating dialog permanently
         */
        fun setNeverAskAgain(context: Context, neverAsk: Boolean) {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_NEVER_ASK_AGAIN, neverAsk)
                .apply()
        }

        /**
         * Checks whether the rating dialog should be shown to the user.
         * Returns false if the user previously chose "No Thanks",
         * true otherwise (including first-time users).
         *
         * @param context The application context
         * @return true if the dialog should be shown, false if suppressed
         */
        fun shouldShowRatingDialog(context: Context): Boolean {
            return !context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_NEVER_ASK_AGAIN, false)
        }
    }

    private var res: android.content.res.Resources? = null

    /** Default constructor required by the Fragment manager. */
    constructor()

    /**
     * Constructor with Resources injection for localized string access.
     * Used when the hosting component wants to provide specific Resources
     * (e.g., for dynamic locale changes).
     *
     * @param resources The Resources object for string lookups
     */
    constructor(resources: android.content.res.Resources) : this() {
        this.res = resources
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.rating_bottom_sheet, container, false)

        // If no Resources were injected, show the layout without custom text
        val resources = res ?: return view

        val rateButton: ButtonCustumFont = view.findViewById(R.id.rateButton)
        val laterButton: ButtonCustumFont = view.findViewById(R.id.laterButton)
        val neverButton: ButtonCustumFont = view.findViewById(R.id.neverButton)
        val tvTitle: TextCustumFont = view.findViewById(R.id.tv_tittle)
        val tvSubtitle: TextCustumFont = view.findViewById(R.id.tv_subtittle)

        // Set localized button labels from resources
        rateButton.text = resources.getString(R.string.rate_now)
        laterButton.text = resources.getString(R.string.later)
        neverButton.text = resources.getString(R.string.no_thanks)

        // Set localized title and subtitle
        tvTitle.text = resources.getString(R.string.enjoying_the_app)
        tvSubtitle.text = resources.getString(R.string.moment_to_rate)

        // "Rate Now" — open Play Store and permanently dismiss
        rateButton.setOnClickListener {
            context?.let { ctx ->
                openPlayStore(ctx)
                setNeverAskAgain(ctx, true)
            }
            dismiss()
        }

        // "Later" — just dismiss, will show again next time
        laterButton.setOnClickListener {
            dismiss()
        }

        // "No Thanks" — never show again and dismiss
        neverButton.setOnClickListener {
            context?.let { ctx -> setNeverAskAgain(ctx, true) }
            dismiss()
        }

        return view
    }

    /**
     * Opens the app's Play Store listing for rating.
     * Tries the Play Store app first (market:// URI scheme),
     * falls back to the web URL if the Play Store app is not installed.
     *
     * @param context The context for starting the activity
     */
    private fun openPlayStore(context: Context) {
        val packageName = context.packageName
        try {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=$packageName")
                )
            )
        } catch (_: ActivityNotFoundException) {
            // Play Store app not installed — open web version
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                )
            )
        }
    }
}
