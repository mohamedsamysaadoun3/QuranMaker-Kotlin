package hazem.nurmontage.videoquran.ui.billing

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.EdgeToEdge
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.ProductDetailsResponseListener
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesResponseListener
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.google.android.material.card.MaterialCardView
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.adapter.FeaturesAdabter
import hazem.nurmontage.videoquran.adapter.ImgAdapter
import hazem.nurmontage.videoquran.core.base.BaseActivity
import hazem.nurmontage.videoquran.core.common.Constants
import hazem.nurmontage.videoquran.fragment.ProgressViewFragment
import hazem.nurmontage.videoquran.model.ModelFeatures
import hazem.nurmontage.videoquran.model.Template
import hazem.nurmontage.videoquran.ui.engine.EngineActivity
import hazem.nurmontage.videoquran.utils.AppUtils
import hazem.nurmontage.videoquran.utils.BillingPreferences
import hazem.nurmontage.videoquran.utils.LocalPersistence
import hazem.nurmontage.videoquran.utils.LocaleHelper
import hazem.nurmontage.videoquran.utils.MyVibrationHelper
import hazem.nurmontage.videoquran.utils.NetworkUtils
import hazem.nurmontage.videoquran.utils.ScreenUtils
import hazem.nurmontage.videoquran.views.ButtonCustumFontBilling
import hazem.nurmontage.videoquran.views.TextCustumFont
import hazem.nurmontage.videoquran.views.text.TextCustumFontBold
import nl.dionsegijn.konfetti.core.PartyFactory
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.Spread
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.core.models.Shape
import nl.dionsegijn.konfetti.xml.KonfettiView
import nl.dionsegijn.konfetti.xml.image.ImageUtil
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Activity that presents the Pro / Premium subscription screen.
 *
 * Offers three purchase options — a yearly subscription, a monthly
 * subscription, and a one-time "forever" purchase — using the Google
 * Play Billing Library.  Handles subscription verification, purchase
 * restoration, and acknowledges purchases.  On successful purchase the
 * user sees a confetti animation and a thank-you message.
 */
class ProVersionActivity : BaseActivity(), PurchasesUpdatedListener {

    // ──────────────────────────────────────────────
    //  SKU constants
    // ──────────────────────────────────────────────

    companion object {
        private const val PRODUCT_ID_FOREIVER = "sku.nurmontage.foreiver"
        private const val PRODUCT_ID_MONTH = "sku.nurmontage.month"
        private const val PRODUCT_ID_YEAR = "sku.nurmontage.year"
        private const val VIDEO_ID = "DY76bAh7i3M"
        private const val TAG = "Billing"

        private const val COLOR_STROKE_SELECTED = -932849        // 0xFFF1C417 gold
        private const val COLOR_STROKE_UNSELECTED = -13617603   // 0xFF301D3D dark purple
    }

    // ──────────────────────────────────────────────
    //  State
    // ──────────────────────────────────────────────

    private var productIdCurrent: String = PRODUCT_ID_YEAR
    private var hasPurchasedForever: Boolean = false
    private var isBtnRestore: Boolean = false
    private var isClick: Boolean = false
    private var isUserScrolling: Boolean = false

    private val productDetailsMap = mutableMapOf<String, ProductDetails>()

    // ──────────────────────────────────────────────
    //  Views
    // ──────────────────────────────────────────────

    private lateinit var billingClient: BillingClient
    private lateinit var btnContinue: ButtonCustumFontBilling
    private lateinit var btnForeiver: MaterialCardView
    private lateinit var btnRestore: Button
    private lateinit var btnYear: MaterialCardView
    private lateinit var ivForeiver: ImageView
    private lateinit var ivYear: ImageView
    private lateinit var mResources: Resources
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvByMonth: TextCustumFont
    private lateinit var tvNoCommitmentAr: TextCustumFont
    private lateinit var tvNoCommitmentEn: TextCustumFont
    private lateinit var tvBest: TextView
    private lateinit var tvPriceForeiver: TextCustumFontBold
    private lateinit var tvPriceYear: TextCustumFontBold

    private var dialog: Dialog? = null
    private var mTemplate: Template? = null

    /** Handles back-press: navigates to EngineActivity if a template was passed. */
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (mTemplate != null) {
                toTrackAct()
            }
            finish()
        }
    }

    // ──────────────────────────────────────────────
    //  Auto-scroll
    // ──────────────────────────────────────────────

    private val autoScrollHandler = Handler(Looper.getMainLooper())

    private val autoScrollRunnable = object : Runnable {
        override fun run() {
            try {
                val rv = recyclerView ?: return
                if (isUserScrolling) return
                val layoutManager = rv.layoutManager as? LinearLayoutManager ?: return
                val firstVisible = layoutManager.findFirstVisibleItemPosition()
                if (firstVisible == RecyclerView.NO_POSITION) {
                    autoScrollHandler.postDelayed(this, 16L)
                    return
                }
                val third = layoutManager.itemCount / 3
                when {
                    firstVisible >= third * 2 -> rv.scrollToPosition(firstVisible - third)
                    firstVisible < third -> rv.scrollToPosition(firstVisible + third)
                    else -> rv.scrollBy(2, 0)
                }
                autoScrollHandler.postDelayed(this, 16L)
            } catch (_: Exception) {
                // Layout may not be ready
            }
        }
    }

    // ──────────────────────────────────────────────
    //  Lifecycle
    // ──────────────────────────────────────────────

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EdgeToEdge.enable(this)
        setContentView(R.layout.activity_pro_version)
        setStatusBarColor(ViewCompat.MEASURED_STATE_MASK)
        setNavigationBarColor(ViewCompat.MEASURED_STATE_MASK)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        // Dark system bars
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = false
        insetsController.isAppearanceLightNavigationBars = false

        // Edge-to-edge insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, windowInsets ->
            val insets: Insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom)
            windowInsets
        }

        mResources = resources
        if (mResources == null) {
            finish()
        }

        wakeLockAcquire()

        (findViewById<TextView>(R.id.mtittle)).text = mResources.getString(R.string.enjoy_all_premium_features)
        (findViewById<TextView>(R.id.hint_review)).text = mResources.getString(R.string._4_8_434_reviews_28k_users)

        // Restore template from intent
        if (intent != null) {
            val stringExtra = intent.getStringExtra(Constants.TEMPLATE)
            if (stringExtra != null) {
                mTemplate = LocalPersistence.readObjectFromFile(this, stringExtra) as? Template
            }
        }

        // Back button
        findViewById<View>(R.id.btn_on_back).setOnClickListener {
            onBackPressedCallback.handleOnBackPressed()
        }

        val isSubscribed = BillingPreferences.isSubscribed(applicationContext)
        setupImg()

        if (isSubscribed) {
            thnks()
            return
        }

        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, mResources.getString(R.string.no_internet), Toast.LENGTH_SHORT).show()
        }

        // Billing client
        billingClient = BillingClient.newBuilder(this)
            .setListener(this)
            .enablePendingPurchases()
            .build()
        startBillingConnection()

        // Restore button
        btnRestore = findViewById(R.id.restore)
        btnRestore.text = mResources.getString(R.string.restort_subscribe)
        btnRestore.setOnClickListener {
            try {
                isBtnRestore = true
                if (billingClient.isReady) {
                    showProgress()
                    checkUserSubscriptionStatus()
                } else {
                    startBillingConnection()
                }
            } catch (_: Exception) {
            }
        }

        // Price views
        tvBest = findViewById(R.id.tv_best_value)
        tvBest.text = mResources.getString(R.string.best_value)
        tvPriceYear = findViewById(R.id.tv_price_year)
        tvPriceForeiver = findViewById(R.id.tv_price_month)
        tvByMonth = findViewById(R.id.tv_year_bymonth)
        btnForeiver = findViewById(R.id.btn_month)
        btnYear = findViewById(R.id.btn_year)
        ivForeiver = findViewById(R.id.btn_radio_month)
        ivYear = findViewById(R.id.btn_radio_year)

        // Forever / month selection
        btnForeiver.setOnClickListener {
            if (productIdCurrent == PRODUCT_ID_MONTH) return@setOnClickListener
            btnForeiver.strokeColor = COLOR_STROKE_SELECTED
            btnYear.strokeColor = COLOR_STROKE_UNSELECTED
            ivForeiver.setImageResource(R.drawable.checked)
            tvPriceForeiver.setTextColor(-1)
            tvPriceYear.setTextColor(-1)
            ivYear.setImageResource(R.drawable.unchecked)
            productIdCurrent = PRODUCT_ID_MONTH
            tvBest.setBackgroundResource(R.drawable.bg_badge_inactive)
        }

        // Year selection
        btnYear.setOnClickListener {
            if (productIdCurrent == PRODUCT_ID_YEAR) return@setOnClickListener
            btnYear.strokeColor = COLOR_STROKE_SELECTED
            btnForeiver.strokeColor = COLOR_STROKE_UNSELECTED
            productIdCurrent = PRODUCT_ID_YEAR
            ivYear.setImageResource(R.drawable.checked)
            ivForeiver.setImageResource(R.drawable.unchecked)
            tvPriceYear.setTextColor(-1)
            tvPriceForeiver.setTextColor(-1)
            tvBest.setBackgroundResource(R.drawable.bg_best_value_badge)
        }

        // Continue / subscribe button
        btnContinue = findViewById(R.id.btn_continue)
        btnContinue.text = mResources.getString(R.string.subscribe_now)
        btnContinue.setOnClickListener {
            try {
                if (isClick) return@setOnClickListener
                isClick = true
                showProgress()
                val productDetails = productDetailsMap[productIdCurrent] ?: return@setOnClickListener
                if (productIdCurrent == PRODUCT_ID_FOREIVER) {
                    launchPurchaseFlowINAPP(productDetails)
                } else {
                    launchPurchaseFlowSUB(productDetails)
                }
            } catch (_: Exception) {
            }
        }

        // No-commitment hint
        tvNoCommitmentAr = findViewById(R.id.tv_hint_ar)
        tvNoCommitmentEn = findViewById(R.id.tv_hint_en)
        if (LocaleHelper.getLanguage(this) == "ar") {
            tvNoCommitmentAr.visibility = View.VISIBLE
            tvNoCommitmentAr.text = mResources.getString(R.string.no_commitment)
        } else {
            tvNoCommitmentEn.visibility = View.VISIBLE
            tvNoCommitmentEn.text = mResources.getString(R.string.no_commitment)
        }
    }

    override fun onPause() {
        super.onPause()
        cancelDialog()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAutoScroll()
        if (billingClient.isReady) {
            billingClient.endConnection()
        }
    }

    // ──────────────────────────────────────────────
    //  Navigation
    // ──────────────────────────────────────────────

    /**
     * Navigates to [EngineActivity] with the current template id so the
     * user can continue editing after subscribing.
     */
    private fun toTrackAct() {
        val intent = Intent(this, EngineActivity::class.java)
        mTemplate?.let { intent.putExtra(Constants.TEMPLATE, it.idTemplate) }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        @Suppress("DEPRECATION")
        overridePendingTransition(0, 0)
    }

    // ──────────────────────────────────────────────
    //  Dialog helpers
    // ──────────────────────────────────────────────

    private fun showFeatures() {
        try {
            val d = Dialog(this)
            dialog = d
            d.setCancelable(true)
            d.requestWindowFeature(1)
            d.window?.setLayout(-1, -2)
            d.window?.setBackgroundDrawable(ColorDrawable(0))
            val view = LayoutInflater.from(this).inflate(R.layout.layout_dialog_premuim, null as ViewGroup?)
            d.setContentView(view)
            view.findViewById<View>(R.id.dialog_title).visibility = View.GONE
            view.findViewById<View>(R.id.dialog_no).setOnClickListener { cancelDialog() }
            d.show()
        } catch (_: Exception) {
        }
    }

    private fun cancelDialog() {
        dialog?.dismiss()
        dialog = null
    }

    // ──────────────────────────────────────────────
    //  Contact / Help
    // ──────────────────────────────────────────────

    private fun help() {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://chat.whatsapp.com/F0kqOjZS1VuBAvoiOG4XEZ")
                setPackage("com.whatsapp")
            }
            startActivity(intent)
        } catch (_: Exception) {
        }
    }

    private fun initBtnHelp(z: Boolean) {
        findViewById<View>(R.id.layout_help).visibility = View.VISIBLE
        val typeface = Typeface.createFromAsset(assets, "fonts/ReadexPro_Medium.ttf")
        val button: Button = findViewById(R.id.btn_help)
        button.typeface = typeface
        button.setOnClickListener { contact() }
    }

    private fun isGmailAvailable(context: Context): Boolean {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            setPackage("com.google.android.gm")
        }
        return !context.packageManager.queryIntentActivities(intent, 0).isEmpty()
    }

    fun contact() {
        var subject = mResources.getString(R.string.support_team)
        if (BillingPreferences.isSubscribed(this)) {
            subject = "$subject : "
        }
        val emailArr = arrayOf("nurmontage.contact@gmail.com")

        if (isGmailAvailable(this)) {
            try {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    putExtra(Intent.EXTRA_EMAIL, emailArr)
                    putExtra(Intent.EXTRA_BCC, emailArr)
                    putExtra(Intent.EXTRA_SUBJECT, subject)
                    type = "message/rfc822"
                    setPackage("com.google.android.gm")
                }
                startActivity(intent)
                return
            } catch (_: Exception) {
            }
        }
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                putExtra(Intent.EXTRA_EMAIL, emailArr)
                putExtra(Intent.EXTRA_BCC, emailArr)
                putExtra(Intent.EXTRA_SUBJECT, subject)
                type = "message/rfc822"
            }
            startActivity(Intent.createChooser(intent, "Send email using"))
        } catch (_: Exception) {
        }
    }

    // ──────────────────────────────────────────────
    //  Features list
    // ──────────────────────────────────────────────

    private fun initImgFeatures() {
        val rv: RecyclerView = findViewById(R.id.rv)
        rv.layoutManager = LinearLayoutManager(this)
        rv.isNestedScrollingEnabled = false
        rv.setHasFixedSize(false)
        rv.itemAnimator = null

        val list = arrayListOf<ModelFeatures>()
        for (feature in mResources.getStringArray(R.array.feature_list)) {
            list.add(ModelFeatures(feature))
        }
        val adapter = FeaturesAdabter(list)
        rv.adapter = adapter
    }

    // ──────────────────────────────────────────────
    //  Billing connection
    // ──────────────────────────────────────────────

    /**
     * Starts the BillingClient connection.  On success queries
     * subscriptions, one-time products, and existing purchases.
     * On disconnection retries automatically.
     */
    private fun startBillingConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    checkUserSubscriptionStatus()
                    querySubscribe()
                    queryPurchases()
                }
            }

            override fun onBillingServiceDisconnected() {
                startBillingConnection()
            }
        })
    }

    // ──────────────────────────────────────────────
    //  Product queries
    // ──────────────────────────────────────────────

    /**
     * Queries the one-time "forever" SKU (inapp).
     */
    private fun queryProducts() {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_ID_FOREIVER)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )
        billingClient.queryProductDetailsAsync(
            QueryProductDetailsParams.newBuilder().setProductList(productList).build(),
            object : ProductDetailsResponseListener {
                override fun onProductDetailsResponse(
                    billingResult: BillingResult,
                    productDetailsList: List<ProductDetails>?
                ) {
                    if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) return
                    if (productDetailsList == null) return
                    for (details in productDetailsList) {
                        productDetailsMap[details.productId] = details
                        val captured = details
                        runOnUiThread { updateUI(captured) }
                    }
                }
            }
        )
    }

    /**
     * Queries the subscription SKUs (year and month).
     */
    private fun querySubscribe() {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_ID_YEAR)
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_ID_MONTH)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )
        billingClient.queryProductDetailsAsync(
            QueryProductDetailsParams.newBuilder().setProductList(productList).build(),
            object : ProductDetailsResponseListener {
                override fun onProductDetailsResponse(
                    billingResult: BillingResult,
                    productDetailsList: List<ProductDetails>?
                ) {
                    if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) return
                    if (productDetailsList == null) return
                    for (details in productDetailsList) {
                        productDetailsMap[details.productId] = details
                        val captured = details
                        runOnUiThread { updateUI(captured) }
                    }
                }
            }
        )
    }

    /**
     * Queries existing one-time (inapp) purchases to detect the
     * "forever" SKU.
     */
    private fun queryPurchases() {
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build(),
            object : PurchasesResponseListener {
                override fun onQueryPurchasesResponse(
                    billingResult: BillingResult,
                    purchases: List<Purchase>
                ) {
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        for (purchase in purchases) {
                            handlePurchase(purchase)
                        }
                    }
                }
            }
        )
    }

    /**
     * Queries INAPP purchases specifically to check for the forever SKU.
     */
    private fun queryUserPurchases() {
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build(),
            object : PurchasesResponseListener {
                override fun onQueryPurchasesResponse(
                    billingResult: BillingResult,
                    purchases: List<Purchase>
                ) {
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        hasPurchasedForever = false
                        for (purchase in purchases) {
                            if (purchase.products.contains(PRODUCT_ID_FOREIVER) &&
                                purchase.purchaseState == Purchase.PurchaseState.PURCHASED
                            ) {
                                hasPurchasedForever = true
                                break
                            }
                        }
                    } else {
                        Log.w(TAG, "Error querying INAPP purchases: ${billingResult.debugMessage}")
                    }
                    Log.e("hasPurchasedForever", "$hasPurchasedForever")
                    checkInAppPurchases()
                }
            }
        )
    }

    // ──────────────────────────────────────────────
    //  UI update from product details
    // ──────────────────────────────────────────────

    /**
     * Updates the price labels once product details are fetched from
     * Google Play.  Handles both one-time (inapp) and subscription
     * (subs) product types.
     */
    private fun updateUI(productDetails: ProductDetails) {
        if (productDetails.productType == BillingClient.ProductType.INAPP) {
            val offerDetails = productDetails.oneTimePurchaseOfferDetails
            if (offerDetails != null) {
                tvPriceForeiver.text = formatPriceWithSymbol(
                    offerDetails.priceAmountMicros,
                    offerDetails.priceCurrencyCode
                )
            } else {
                tvPriceForeiver.text = "N/A"
            }
            return
        }
        if (productDetails.productType == BillingClient.ProductType.SUBS) {
            val subOfferDetails = productDetails.subscriptionOfferDetails?.firstOrNull()
                ?: return
            val pricingPhase = subOfferDetails.pricingPhases.pricingPhaseList.firstOrNull()
                ?: return
            val priceAmountMicros = pricingPhase.priceAmountMicros
            val priceCurrencyCode = pricingPhase.priceCurrencyCode
            val productId = productDetails.productId

            if (PRODUCT_ID_YEAR == productId) {
                tvPriceYear.text = formatPriceWithSymbol(priceAmountMicros, priceCurrencyCode)
                val perMonth = formatPriceWithSymbol(
                    Math.round(priceAmountMicros / 12.0),
                    priceCurrencyCode
                )
                val sb = StringBuilder()
                if (LocaleHelper.getLanguage(applicationContext) == "ar") {
                    sb.append("فقط ").append(perMonth).append(" /شهر")
                } else {
                    sb.append("Only ").append(perMonth).append(" /month")
                }
                tvByMonth.text = sb
                return
            }
            if (PRODUCT_ID_MONTH == productId) {
                tvPriceForeiver.text = formatPriceWithSymbol(priceAmountMicros, priceCurrencyCode)
            }
        }
    }

    /**
     * Formats a micros-amount with the given currency code into a
     * human-readable price string (e.g. "$9.99").
     */
    private fun formatPriceWithSymbol(micros: Long, currencyCode: String): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale.US)
        formatter.currency = Currency.getInstance(currencyCode)
        formatter.maximumFractionDigits = 2
        formatter.minimumFractionDigits = 2
        return formatter.format(micros / 1_000_000.0)
    }

    /**
     * Alternative UI update that uses the pre-formatted price string
     * from Google Play directly.
     */
    private fun updateUILast(productDetails: ProductDetails) {
        var formattedPrice: String? = null
        if (productDetails.productType == BillingClient.ProductType.INAPP) {
            formattedPrice = productDetails.oneTimePurchaseOfferDetails?.formattedPrice
            tvPriceForeiver.text = formattedPrice ?: "N/A"
            return
        }
        if (productDetails.productType == BillingClient.ProductType.SUBS) {
            val subOfferDetails = productDetails.subscriptionOfferDetails
            if (subOfferDetails != null && subOfferDetails.isNotEmpty()) {
                val phases = subOfferDetails[0].pricingPhases?.pricingPhaseList
                if (phases != null && phases.isNotEmpty()) {
                    formattedPrice = phases[0].formattedPrice
                }
            }
            if (PRODUCT_ID_YEAR == productDetails.productId) {
                tvPriceYear.text = formattedPrice ?: "N/A"
            }
        }
    }

    // ──────────────────────────────────────────────
    //  Purchase flow
    // ──────────────────────────────────────────────

    private fun launchPurchaseFlowINAPP(productDetails: ProductDetails) {
        val paramsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .build()
        )
        billingClient.launchBillingFlow(
            this,
            BillingFlowParams.newBuilder().setProductDetailsParamsList(paramsList).build()
        )
    }

    private fun launchPurchaseFlowSUB(productDetails: ProductDetails) {
        val offerToken = findOfferToken(productDetails) ?: return
        val paramsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .setOfferToken(offerToken)
                .build()
        )
        billingClient.launchBillingFlow(
            this,
            BillingFlowParams.newBuilder().setProductDetailsParamsList(paramsList).build()
        )
    }

    private fun findOfferToken(productDetails: ProductDetails): String? {
        val subOfferDetails = productDetails.subscriptionOfferDetails
        if (subOfferDetails.isNullOrEmpty()) return null
        return subOfferDetails[0].offerToken
    }

    // ──────────────────────────────────────────────
    //  PurchasesUpdatedListener
    // ──────────────────────────────────────────────

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        hideProgressFragment()
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        } else {
            billingResult.responseCode
        }
    }

    // ──────────────────────────────────────────────
    //  Purchase handling
    // ──────────────────────────────────────────────

    private fun handlePurchase(purchase: Purchase) {
        when (purchase.purchaseState) {
            Purchase.PurchaseState.PURCHASED -> {
                if (purchase.isAcknowledged) return
                billingClient.acknowledgePurchase(
                    AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                        .build()
                ) { billingResult ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        Log.e(TAG, "$PRODUCT_ID_FOREIVER acknowledged.")
                    } else {
                        Log.e(TAG, "Failed to acknowledge $PRODUCT_ID_FOREIVER: ${billingResult.debugMessage}")
                    }
                }
                BillingPreferences.saveSubscriptionStatus(applicationContext, true)
                playVibration()
                thnks()
            }
            Purchase.PurchaseState.PENDING -> {
                runOnUiThread {
                    Toast.makeText(applicationContext, "Purchase is pending", Toast.LENGTH_SHORT).show()
                }
            }
            else -> {
                runOnUiThread {
                    Toast.makeText(applicationContext, "Purchase is in unknown state", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // ──────────────────────────────────────────────
    //  Subscription status checks
    // ──────────────────────────────────────────────

    /**
     * Checks whether the user has an active subscription.  If not,
     * falls through to [checkInAppPurchases] to look for the one-time
     * "forever" purchase.
     */
    private fun checkUserSubscriptionStatus() {
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        ) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                if (purchases.isNotEmpty()) {
                    handleSubscriptionPurchases(purchases)
                } else {
                    checkInAppPurchases()
                }
            } else {
                checkInAppPurchases()
            }
        }
    }

    /**
     * Checks for the one-time "forever" purchase after subscription
     * check comes back empty.
     */
    private fun checkInAppPurchases() {
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        ) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                hasPurchasedForever = false
                for (purchase in purchases) {
                    if (purchase.products.contains(PRODUCT_ID_FOREIVER) &&
                        purchase.purchaseState == Purchase.PurchaseState.PURCHASED
                    ) {
                        hasPurchasedForever = true
                        break
                    }
                }
            }
            if (!hasPurchasedForever) {
                handleNoPurchases()
            } else {
                runOnUiThread { hideProgressFragment() }
                BillingPreferences.saveSubscriptionStatus(applicationContext, true)
                thnks()
            }
        }
    }

    /**
     * If any subscription purchase is in the PURCHASED state, mark the
     * user as subscribed.  Otherwise, fall through to INAPP check.
     */
    private fun handleSubscriptionPurchases(purchases: List<Purchase>) {
        for (purchase in purchases) {
            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                hideProgressFragment()
                BillingPreferences.saveSubscriptionStatus(applicationContext, true)
                thnks()
                return
            }
        }
        BillingPreferences.saveSubscriptionStatus(applicationContext, false)
        checkInAppPurchases()
    }

    /**
     * Called when no active subscription or one-time purchase was found.
     * Hides the progress spinner and optionally shows a toast if this
     * was a restore attempt.
     */
    private fun handleNoPurchases() {
        runOnUiThread {
            hideProgressFragment()
            if (isBtnRestore) {
                Toast.makeText(
                    applicationContext,
                    mResources.getString(R.string.not_have_susbcribe),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        BillingPreferences.saveSubscriptionStatus(applicationContext, false)
    }

    // ──────────────────────────────────────────────
    //  Progress fragment
    // ──────────────────────────────────────────────

    private fun showProgress() {
        findViewById<View>(R.id.container_progress).visibility = View.VISIBLE
        if (isFinishing || supportFragmentManager.isDestroyed) return
        supportFragmentManager.beginTransaction()
            .replace(R.id.container_progress, ProgressViewFragment.getInstance())
            .commit()
    }

    private fun hideProgressFragment() {
        try {
            isClick = false
            if (!isFinishing && !supportFragmentManager.isDestroyed) {
                val fm: FragmentManager = supportFragmentManager
                val tx: FragmentTransaction = fm.beginTransaction()
                val fragment = fm.findFragmentById(R.id.container_progress)
                if (fragment != null) {
                    tx.remove(fragment)
                }
                tx.commit()
            }
        } catch (_: Exception) {
        }
        findViewById<View>(R.id.container_progress).visibility = View.GONE
    }

    // ──────────────────────────────────────────────
    //  Confetti / celebration
    // ──────────────────────────────────────────────

    /**
     * Launches a confetti burst using the heart drawable and
     * konfetti library.
     */
    fun explode() {
        val heartShape = ImageUtil.loadDrawable(
            ContextCompat.getDrawable(applicationContext, R.drawable.ic_heart),
            true,
            true
        )
        val konfettiView: KonfettiView = findViewById(R.id.konfettiView)
        konfettiView.visibility = View.VISIBLE
        val party = PartyFactory(
            Emitter(3500L, TimeUnit.MILLISECONDS).max(200)
        )
            .spread(Spread.ROUND)
            .shapes(listOf(heartShape))
            .colors(listOf(-1216136524, -1124760279, -2019220, -1124760279))
            .setSpeedBetween(0f, 30f)
            .position(Position.Relative(0.5, 0.3))
            .getParty()
        konfettiView.start(party)
    }

    // ──────────────────────────────────────────────
    //  Thank-you state
    // ──────────────────────────────────────────────

    /**
     * Switches the UI into the "thank you" state: hides pricing,
     * shows the thank-you message, contact help button, and fires
     * the confetti animation.
     */
    private fun thnks() {
        try {
            runOnUiThread {
                findViewById<View>(R.id.tv_hint_ar).visibility = View.GONE
                findViewById<View>(R.id.tv_hint_en).visibility = View.GONE
                findViewById<View>(R.id.btn_continue).visibility = View.GONE
                findViewById<View>(R.id.layout_price).visibility = View.GONE
                val tvThanks: TextCustumFont = findViewById(R.id.tv_thanks)
                tvThanks.visibility = View.VISIBLE
                tvThanks.text = mResources.getString(R.string.thanks_hint)
                initBtnHelp(true)
                explode()
            }
        } catch (_: Exception) {
        }
    }

    // ──────────────────────────────────────────────
    //  Image gallery with auto-scroll
    // ──────────────────────────────────────────────

    private fun setupImg() {
        val rv: RecyclerView = findViewById(R.id.rv_img)
        recyclerView = rv
        rv.post {
            var height = (rv.height * 0.95f).toInt()
            if (height == 0) {
                height = (ScreenUtils.getScreenHeight(this@ProVersionActivity) * 0.4f).toInt()
            }

            val baseList = arrayListOf(
                R.drawable.nur_2,
                R.drawable.nur_3,
                R.drawable.nur_4,
                R.drawable.nur_1,
            )
            val tripled = ArrayList<Int>().apply {
                addAll(baseList)
                addAll(baseList)
                addAll(baseList)
            }

            val imgAdapter = ImgAdapter(
                AppUtils.getAppVersionName(this@ProVersionActivity),
                tripled,
                height
            )
            val layoutManager = LinearLayoutManager(this@ProVersionActivity, LinearLayoutManager.HORIZONTAL, false)
            layoutManager.isItemPrefetchEnabled = true
            layoutManager.initialPrefetchItemCount = 6

            rv.layoutManager = layoutManager
            rv.adapter = imgAdapter
            rv.setHasFixedSize(true)
            rv.setItemViewCacheSize(12)
            rv.itemAnimator = null

            rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                        isUserScrolling = true
                    } else if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        isUserScrolling = false
                        startAutoScroll()
                    }
                }
            })

            rv.post { rv.scrollToPosition(baseList.size) }
            startAutoScroll()
        }
    }

    private fun startAutoScroll() {
        try {
            autoScrollHandler.removeCallbacks(autoScrollRunnable)
            autoScrollHandler.postDelayed(autoScrollRunnable, 250L)
        } catch (_: Exception) {
        }
    }

    private fun stopAutoScroll() {
        autoScrollHandler.removeCallbacks(autoScrollRunnable)
    }

    // ──────────────────────────────────────────────
    //  Haptic feedback
    // ──────────────────────────────────────────────

    private fun playVibration() {
        runOnUiThread {
            MyVibrationHelper(this@ProVersionActivity).vibrate(250L)
        }
    }
}
