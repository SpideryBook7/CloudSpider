package com.spiderybook.ui.downloads

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.spiderybook.databinding.ItemActiveDownloadBinding

data class ActiveDownload(
    val id: Long,
    val title: String,
    val downloadedBytes: Long,
    val totalBytes: Long,
    val statusText: String
)

class ActiveDownloadAdapter(
    private var items: List<ActiveDownload>,
    private val onCancelClick: (ActiveDownload) -> Unit
) : RecyclerView.Adapter<ActiveDownloadAdapter.ViewHolder>() {

    fun updateDownloads(newItems: List<ActiveDownload>) {
        // En una app más robusta se usaría DiffUtil aquí.
        // Dado que este es un listado corto de descargas activas (generalmente 1-3), notifyDataSetChanged es aceptable para actualización en tiempo real.
        this.items = newItems
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemActiveDownloadBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ActiveDownload) {
            binding.tvTitle.text = item.title
            
            if (item.totalBytes > 0) {
                val progress = ((item.downloadedBytes.toDouble() / item.totalBytes.toDouble()) * 100).toInt()
                val dlMb = item.downloadedBytes / (1024.0 * 1024)
                val totalMb = item.totalBytes / (1024.0 * 1024)
                
                binding.tvProgressText.text = String.format(java.util.Locale.US, "%.1f MB / %.1f MB (%d%%)", dlMb, totalMb, progress)
                binding.progressDownload.isIndeterminate = false
                binding.progressDownload.max = 100
                binding.progressDownload.progress = progress
            } else {
                binding.tvProgressText.text = item.statusText
                binding.progressDownload.isIndeterminate = true
            }
            
            binding.btnCancel.setOnClickListener { onCancelClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemActiveDownloadBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size
}
