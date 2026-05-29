package hazem.nurmontage.videoquran.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.model.ModelFeatures
import hazem.nurmontage.videoquran.views.text.TextCustumFont

/**
 * RecyclerView Adapter for displaying a features comparison list.
 *
 * Originally: FeaturesAdabter.java (preserved typo in original package)
 * Converted to: FeaturesAdabter.kt — idiomatic Kotlin, full logic preserved
 *
 * Features:
 * - Displays feature names in a simple list format
 * - Each row shows a single feature name as a text chip
 * - All features are treated as available (billing removed)
 *
 * @property list List of feature items to display
 */
class FeaturesAdabter(
    private var list: List<ModelFeatures>?
) : RecyclerView.Adapter<FeaturesAdabter.ViewHolder>() {

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
