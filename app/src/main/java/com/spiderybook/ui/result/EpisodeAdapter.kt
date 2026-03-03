package com.spiderybook.ui.result

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.spiderybook.databinding.ItemEpisodeBinding
import com.spiderybook.domain.model.Episode

class EpisodeAdapter(
    private val items: List<Episode>,
    private val onClick: (Episode) -> Unit,
    private val onLongClick: (Episode, Boolean) -> Unit,
    private val onDownloadClick: (Episode) -> Unit
) : RecyclerView.Adapter<EpisodeAdapter.EpisodeViewHolder>() {

    private val watchProgress = mutableMapOf<String, Int>()

    inner class EpisodeViewHolder(private val binding: ItemEpisodeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(item: Episode) {
            binding.tvEpisodeName.text = item.name // Remove number prefix if needed, or keep it loosely
            
            // Handle Watch Progress
            val progress = watchProgress[item.url] ?: 0
            if (progress > 0) {
                 binding.progressWatch.visibility = android.view.View.VISIBLE
                 binding.progressWatch.progress = progress
            } else {
                 binding.progressWatch.visibility = android.view.View.GONE
            }
            
            // Placeholder: Load poster as thumbnail if specific thumb is missing
             binding.imgThumbnail.load(item.posterUrl) {
                crossfade(true)
                placeholder(android.R.drawable.ic_menu_gallery)
            }
            
            binding.root.setOnClickListener { onClick(item) }
            val isSeen = progress >= 95
            binding.root.setOnLongClickListener { 
                onLongClick(item, isSeen)
                true
            }
            binding.btnDownloadEpisode.setOnClickListener { onDownloadClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpisodeViewHolder {
        return EpisodeViewHolder(
            ItemEpisodeBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: EpisodeViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
    
    fun setWatchProgress(progressMap: Map<String, Int>) {
        watchProgress.clear()
        watchProgress.putAll(progressMap)
        notifyDataSetChanged()
    }
}
