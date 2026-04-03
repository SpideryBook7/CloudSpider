package com.spiderybook.ui.player

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.spiderybook.R

class PlayerEpisodeAdapter(
    private val episodes: List<String>,
    private val posterUrl: String,
    private var currentIndex: Int,
    private val onEpisodeClick: (Int) -> Unit
) : RecyclerView.Adapter<PlayerEpisodeAdapter.EpisodeViewHolder>() {

    fun updateCurrentIndex(index: Int) {
        val prevIndex = currentIndex
        currentIndex = index
        notifyItemChanged(prevIndex)
        notifyItemChanged(currentIndex)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpisodeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_player_episode, parent, false)
        return EpisodeViewHolder(view)
    }

    override fun onBindViewHolder(holder: EpisodeViewHolder, position: Int) {
        val isActive = position == currentIndex
        val rawTitle = episodes[position]
        
        val epNumStr = rawTitle.replace(Regex("[^0-9]"), "")
        holder.tvEpisodeNumber.text = if(epNumStr.isNotEmpty()) "EPISODE $epNumStr" else "EPISODE ${episodes.size - position}"
        
        holder.tvEpisodeTitle.text = rawTitle

        holder.ivThumb.load(posterUrl) {
            crossfade(true)
        }

        holder.indicatorActive.visibility = if (isActive) View.VISIBLE else View.GONE
        holder.tvBadgeLive.visibility = if (isActive) View.VISIBLE else View.GONE
        holder.viewDimmer.visibility = if (isActive) View.VISIBLE else View.GONE
        holder.ivPlayActive.visibility = if (isActive) View.VISIBLE else View.GONE
        
        if (isActive) {
            holder.tvEpisodeNumber.setTextColor(android.graphics.Color.parseColor("#B366FF"))
            holder.ivPlayOutline.visibility = View.GONE
            holder.itemView.setBackgroundColor(android.graphics.Color.parseColor("#1AFFFFFF"))
        } else {
            holder.tvEpisodeNumber.setTextColor(android.graphics.Color.WHITE)
            holder.ivPlayOutline.visibility = View.VISIBLE
            holder.itemView.setBackgroundColor(android.graphics.Color.TRANSPARENT)
        }

        holder.itemView.setOnClickListener {
            onEpisodeClick(position)
        }
    }

    override fun getItemCount(): Int = episodes.size

    class EpisodeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val indicatorActive: View = view.findViewById(R.id.indicator_active)
        val ivThumb: ImageView = view.findViewById(R.id.iv_thumb)
        val viewDimmer: View = view.findViewById(R.id.view_dimmer)
        val ivPlayActive: ImageView = view.findViewById(R.id.iv_play_active)
        val tvEpisodeNumber: TextView = view.findViewById(R.id.tv_episode_number)
        val tvEpisodeTitle: TextView = view.findViewById(R.id.tv_episode_title)
        val tvBadgeLive: TextView = view.findViewById(R.id.tv_badge_live)
        val ivPlayOutline: ImageView = view.findViewById(R.id.iv_play_outline)
    }
}
