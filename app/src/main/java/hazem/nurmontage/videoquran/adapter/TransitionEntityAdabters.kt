package hazem.nurmontage.videoquran.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.entity_timeline.EntityQuranTimeline

/**
 * RecyclerView adapter for selecting Aya (Quran verse) transition effects.
 *
 * Similar to [TransitionBismilahAdabters] but operates on [EntityQuranTimeline]
 * entities and adds subscription gating. Non-subscribers see a lock overlay
 * on transitions beyond index 10, and tapping those items triggers
 * [ITransition.toSubscribe] instead of applying the transition.
 *
 * The adapter tracks its own selection state and notifies the host via
 * [ITransition] callbacks. When the host calls [update] the entire data
 * set is replaced (e.g. switching from "in" to "out" transition list).
 *
 * @property isSubscribe        Whether the user has an active subscription
 * @property iTransition        Callback for transition selection events
 * @property entityQuranTimeline The Quran timeline entity this adapter operates on
 *
 * Converted from TransitionEntityAdabters.java (144 lines).
 */
class TransitionEntityAdabters(
    private val isSubscribe: Boolean,
    private val iTransition: ITransition?,
    list: List<TransitionItem>,
    select: Int,
    private val entityQuranTimeline: EntityQuranTimeline
) : RecyclerView.Adapter<TransitionEntityAdabters.ViewHolder>() {

    private var list: List<TransitionItem> = list
    private var max: Int = list.size
    private var select: Int = select
    private var type: String = "in"

    // ── Callback interface ──────────────────────────────────────────────

    /**
     * Callback for Aya transition selection events.
     * Defined here because [EffectAyaFragment] has not been converted yet;
     * when it is, this interface should be moved into that fragment class and
     * this adapter should import it from there.
     */
    interface ITransition {
        fun in(type: String, entity: EntityQuranTimeline)
        fun out(type: String, entity: EntityQuranTimeline)
        fun applyAll(index: Int, entity: EntityQuranTimeline)
        fun destroy(entity: EntityQuranTimeline)
        fun onHideFragment(entity: EntityQuranTimeline)
        fun playing(entity: EntityQuranTimeline)
        fun remove(index: Int, entity: EntityQuranTimeline)
        fun toSubscribe()
        fun updateDurationIn(duration: Float, entity: EntityQuranTimeline)
        fun updateDurationOut(duration: Float, entity: EntityQuranTimeline)
    }

    // ── TransitionItem model ───────────────────────────────────────────

    /**
     * Represents a single transition effect item displayed in the list.
     *
     * @property type         Transition type string (matches [Constants.TransitionType.value])
     * @property idRessource  Drawable resource ID for the transition icon
     * @property angle        Rotation angle in degrees for the icon
     */
    data class TransitionItem(
        val type: String,
        val idRessource: Int,
        val angle: Int
    )

    // ── Public API ─────────────────────────────────────────────────────

    fun getSelect(): Int = select

    fun update(list: List<TransitionItem>, type: String, select: Int) {
        this.select = select
        this.list = list
        this.type = type
        this.max = list.size
        notifyDataSetChanged()
    }

    fun isHaveSelect(): Boolean = select != -1

    fun unselect() {
        if (select == -1) return
        val old = select
        select = -1
        notifyItemChanged(old)
    }

    // ── Adapter overrides ──────────────────────────────────────────────

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_anim, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Show lock overlay for premium transitions (index > 10) when not subscribed
        if (!isSubscribe) {
            if (position > 10) {
                holder.disableView.visibility = View.VISIBLE
            } else {
                holder.disableView.visibility = View.GONE
            }
        }

        val item = list[position]
        holder.animationItem.rotation = item.angle.toFloat()
        holder.animationItem.setImageResource(item.idRessource)
        if (position == select) {
            holder.animationItem.setBackgroundResource(R.drawable.circle_item_menu_select)
        } else {
            holder.animationItem.setBackgroundResource(R.drawable.circle_effect)
        }
    }

    override fun getItemCount(): Int = max

    // ── ViewHolder ─────────────────────────────────────────────────────

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val animationItem: ImageView = itemView.findViewById(R.id.anim_item)
        val disableView: ImageView = itemView.findViewById(R.id.iv_disable)

        init {
            itemView.setOnClickListener {
                val pos = adapterPosition
                if (iTransition == null) return@setOnClickListener

                // Premium gate: non-subscribers tapping premium transitions get subscribe prompt
                if (!isSubscribe && pos > 10) {
                    iTransition.toSubscribe()
                    return@setOnClickListener
                }

                // Don't re-select the already selected item
                if (select == pos) return@setOnClickListener

                val oldSelect = select
                select = pos
                notifyItemChanged(oldSelect)
                notifyItemChanged(select)

                val item = list[pos]
                if (type == "in") {
                    iTransition.in(item.type, entityQuranTimeline)
                } else if (type == "out") {
                    iTransition.out(item.type, entityQuranTimeline)
                }
            }
        }
    }
}
