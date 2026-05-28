package hazem.nurmontage.videoquran.adapter

import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.model.ItemQuranSearch

/**
 * RecyclerView adapter for displaying Quran ayah search results.
 *
 * Originally: SearchQuranAdabters.java (131 lines)
 * Converted to: SearchQuranAdabters.kt — idiomatic Kotlin, full logic preserved
 *
 * Each row displays an ayah (verse) with its surah name and ayah number.
 * The search keyword is highlighted within the ayah text using
 * [SpannableString] with a [ForegroundColorSpan] in teal color.
 *
 * **Multi-select range**: The adapter supports a contiguous range selection
 * defined by [minSelected] and [maxSelected]. Items within the range
 * (inclusive) are highlighted with a teal background color. This allows
 * the user to select multiple consecutive ayahs for batch operations
 * like adding several verses to the timeline at once.
 *
 * Click behavior expands or resets the selection range:
 * - If no range exists ([minSelected] == -1), the clicked item becomes
 *   both the min and max of the range
 * - If the clicked item is below [minSelected], it becomes the new min
 * - If the clicked item is above [maxSelected], it becomes the new max
 * - If the clicked item is within the existing range, the range resets
 *   to just that item (allowing the user to start a new range from it)
 *
 * @see ISearchQuranCallback
 * @see ItemQuranSearch
 */
class SearchQuranAdabters(
    private val callback: ISearchQuranCallback?
) : RecyclerView.Adapter<SearchQuranAdabters.ViewHolder>() {

    private val searchList: MutableList<ItemQuranSearch> = ArrayList()

    /** The minimum (top) position of the selection range, or -1 if none. */
    private var minSelected: Int = -1

    /** The maximum (bottom) position of the selection range, or -1 if none. */
    private var maxSelected: Int = -1

    // ──────────────────────────────────────────────────────────────────────
    // Inner interface
    // ──────────────────────────────────────────────────────────────────────

    /**
     * Callback interface for Quran search result interactions.
     *
     * The hosting Fragment implements this to handle ayah selection,
     * typically by adding the selected ayahs to the video timeline.
     */
    interface ISearchQuranCallback {
        /**
         * Called when an ayah is clicked.
         * @param minSelected The start position of the selection range
         * @param maxSelected The end position of the selection range
         * @param item        The clicked search result item
         */
        fun onClick(minSelected: Int, maxSelected: Int, item: ItemQuranSearch)
    }

    // ──────────────────────────────────────────────────────────────────────
    // Public API
    // ──────────────────────────────────────────────────────────────────────

    /** Returns the number of search results. */
    fun getSize(): Int = searchList.size

    /** Returns the minimum position of the current selection range. */
    fun getMinSelected(): Int = minSelected

    /** Returns the maximum position of the current selection range. */
    fun getMaxSelected(): Int = maxSelected

    /**
     * Replace the entire search result list.
     * Resets the selection range.
     */
    fun setList(list: List<ItemQuranSearch>) {
        searchList.clear()
        searchList.addAll(list)
        notifyDataSetChanged()
    }

    /** Append a single search result to the end of the list. */
    fun add(item: ItemQuranSearch) {
        searchList.add(item)
        notifyItemInserted(searchList.size - 1)
    }

    /**
     * Remove all search results and clear the selection range.
     * Uses [notifyItemRangeRemoved] for a smooth removal animation.
     */
    fun clear() {
        val size = searchList.size
        if (size == 0) return
        searchList.clear()
        notifyItemRangeRemoved(0, size)
    }

    // ──────────────────────────────────────────────────────────────────────
    // Adapter overrides
    // ──────────────────────────────────────────────────────────────────────

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.row_search_quran, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = searchList[position]

        if (item.aya != null) {
            holder.name.text = "${item.surahName} (${item.to})"

            if (item.startSpannable != -1) {
                val spannableString = SpannableString(item.aya)
                spannableString.setSpan(
                    ForegroundColorSpan(-10929), // Teal highlight color
                    item.startSpannable,
                    item.endSpannble,
                    SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                holder.aya.text = spannableString
            } else {
                holder.aya.text = item.aya
            }
        } else {
            holder.name.text = item.surahIndex.toString()
        }

        val isInRange = minSelected != -1 && position >= minSelected && position <= maxSelected
        holder.itemView.setBackgroundColor(if (isInRange) -14540254 else 0)
    }

    override fun getItemCount(): Int = searchList.size

    // ──────────────────────────────────────────────────────────────────────
    // ViewHolder
    // ──────────────────────────────────────────────────────────────────────

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val name: TextView = itemView.findViewById(R.id.tv_surah_name_and_number)
        val aya: TextView = itemView.findViewById(R.id.tv_surah)

        init {
            itemView.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos == RecyclerView.NO_POSITION) return@setOnClickListener

                if (minSelected == -1) {
                    minSelected = pos
                    maxSelected = pos
                } else if (pos < minSelected) {
                    minSelected = pos
                } else if (pos > maxSelected) {
                    maxSelected = pos
                } else {
                    minSelected = pos
                    maxSelected = pos
                }

                notifyDataSetChanged()
                callback?.onClick(minSelected, maxSelected, searchList[pos])
            }
        }
    }
}
