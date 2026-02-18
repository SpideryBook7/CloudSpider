package com.spiderybook.ui.result

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.spiderybook.databinding.ItemEpisodeBinding
import com.spiderybook.domain.model.Episode

class EpisodeAdapter(
    private val items: List<Episode>,
    private val onClick: (Episode) -> Unit
) : RecyclerView.Adapter<EpisodeAdapter.EpisodeViewHolder>() {

    inner class EpisodeViewHolder(private val binding: ItemEpisodeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(item: Episode) {
            binding.tvEpisodeName.text = item.name // Remove number prefix if needed, or keep it loosely
            // binding.tvEpisodeNumber.text = ... (removed)
            
            // Placeholder: Load poster as thumbnail if specific thumb is missing
            // In a real app, episodes have their own thumbs. For now, use the poster passed to adapter or generic?
            // The item_episode.xml has img_thumbnail. 
            // We need to pass the main poster to this adapter if episodes don't have images.
            // item.posterUrl might be null in Episode model? Let's check. 
            // Assuming Episode has no image, we might want to use a placeholder or the main poster.
            // For now, let's just set a gray background or use a placeholder resource.
             binding.imgThumbnail.load(item.posterUrl) {
                crossfade(true)
                placeholder(android.R.drawable.ic_menu_gallery)
            }
            
            binding.root.setOnClickListener { onClick(item) }
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
}
