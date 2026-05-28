package hazem.nurmontage.videoquran.adapter

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import hazem.nurmontage.videoquran.R

/**
 * RecyclerView adapter for displaying a horizontal color palette.
 *
 * Each item is a circular swatch from the provided [colors] array.
 * The selected position gets a white stroke highlight.
 * Clicking a swatch fires [IColor.onColor] with the color value and position.
 *
 * Converted from ColorAdabter.java (original misspelling corrected).
 */
class ColorAdapter(
    private val iColorCallback: IColor?,
    private val colors: IntArray,
    private var posSelect: Int
) : RecyclerView.Adapter<ColorAdapter.ViewHolder>() {

    /** Callback interface for color selection events. */
    interface IColor {
        fun onColor(color: Int, position: Int)
    }

    var enabled: Boolean = true

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.image)

        init {
            itemView.setOnClickListener {
                val pos = adapterPosition
                if (iColorCallback == null || posSelect == pos || !enabled) return@setOnClickListener
                val oldPos = posSelect
                posSelect = pos
                notifyItemChanged(oldPos)
                notifyItemChanged(posSelect)
                iColorCallback.onColor(colors[pos], pos)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_color, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        setGradientBackground(holder.imageView, holder.itemView, colors[position], position == posSelect)
    }

    override fun getItemCount(): Int = colors?.size ?: 0

    fun getPosSelect(): Int = posSelect

    private fun setGradientBackground(imageView: View, itemView: View, color: Int, isSelected: Boolean) {
        // Selected state: white stroke on the item background
        if (isSelected) {
            val selectedBg = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                cornerRadius = 100f
                setStroke(3, -0x1) // white
            }
            itemView.background = selectedBg
        } else {
            itemView.background = null
        }

        // Color swatch inside the circle
        val colorBg = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            cornerRadius = 100f
            setColor(color)
        }
        imageView.background = colorBg
    }
}
