package hazem.nurmontage.videoquran.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.model.ModelFeatures
import hazem.nurmontage.videoquran.views.text.TextCustumFont

/**
 * RecyclerView Adapter for displaying a features comparison list
 * (Free vs Pro version feature comparison).
 *
 * Originally: FeaturesAdabter.java (preserved typo in original package)
 * Converted to: FeaturesAdabter.kt — idiomatic Kotlin, full logic preserved
 *
 * Features:
 * - Displays feature names in a simple list format
 * - Each row shows a single feature name as a text chip
 * - Supports subscription state via [setSubscribe] which triggers a full
 *   refresh of the adapter (used to show/hide premium badges externally)
 * - In the original app, this adapter was used to display a comparison table
 *   of free vs premium features on the Pro Version / About screen
 * - Clean version: the subscription flag is retained for compatibility but
 *   all features are treated as available
 *
 * @property list List of feature items to display
 * @property isSubscibe Whether the user has an active subscription
 */
class FeaturesAdabter(
    private var list: List<ModelFeatures>?,
    private var isSubscibe: Boolean = false
) : RecyclerView.Adapter<FeaturesAdabter.ViewHolder>() {

    /**
     * Updates the subscription state and refreshes the adapter.
     * In the original app, this controlled the display of premium badges
     * or feature availability indicators.
     *
     * @param subscribed Whether the user is now subscribed
     */
    fun setSubscribe(subscribed: Boolean) {
        isSubscibe = subscribed
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_feature, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val feature = list?.get(position) ?: return
        holder.text.text = feature.name
    }

    override fun getItemCount(): Int = list?.size ?: 0

    /**
     * ViewHolder for feature comparison items.
     * Displays the feature name in a custom font text view.
     */
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val text: TextCustumFont = itemView.findViewById(R.id.tv_feature)
    }
}
