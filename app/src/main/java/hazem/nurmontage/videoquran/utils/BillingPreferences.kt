package hazem.nurmontage.videoquran.utils

import android.content.Context

/**
 * Simple SharedPreferences wrapper for billing/subscription state.
 *
 * Currently [isSubscribed] always returns `true` (all features unlocked).
 * When real billing integration is added, this class will read from
 * Google Play BillingClient and cache the result locally.
 */
object BillingPreferences {

    private const val PREF_NAME = "BillingPrefs"
    private const val KEY_IS_SUBSCRIBED = "isSubscribed"

    fun isSubscribed(context: Context?): Boolean = true

    fun saveSubscriptionStatus(context: Context, isSubscribed: Boolean) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_IS_SUBSCRIBED, isSubscribed)
            .apply()
    }

    fun saveSubscribeAllItemValueTofalse(context: Context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_IS_SUBSCRIBED, false)
            .apply()
    }
}
