package com.spiderybook.ui.downloads

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.spiderybook.databinding.ItemDownloadGroupBinding
import java.io.File

data class DownloadGroup(
    val title: String,
    val episodesCount: Int,
    val totalSizeBytes: Long,
    val firstFile: File,
    val posterUrl: String? = null
)

class DownloadGroupAdapter(
    private val items: List<DownloadGroup>,
    private val onDeleteClick: (DownloadGroup) -> Unit,
    private val onClick: (DownloadGroup) -> Unit
) : RecyclerView.Adapter<DownloadGroupAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemDownloadGroupBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DownloadGroup) {
            binding.tvTitle.text = item.title
            val sizeMb = item.totalSizeBytes / (1024 * 1024)
            val sizeGb = sizeMb / 1024.0
            val sizeStr = if (sizeGb >= 1.0) String.format(java.util.Locale.US, "%.1f GB", sizeGb) else "$sizeMb MB"
            
            val label = if (item.episodesCount == 1) "Movie" else "${item.episodesCount} Episodes"
            binding.tvSubtitle.text = "$label • $sizeStr"
            
            if (!item.posterUrl.isNullOrEmpty()) {
                binding.imgPoster.load(item.posterUrl) { crossfade(true) }
            } else {
                binding.imgPoster.load(com.spiderybook.R.drawable.logo)
            }
            
            binding.btnDelete.setOnClickListener { onDeleteClick(item) }
            binding.root.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemDownloadGroupBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size
}
