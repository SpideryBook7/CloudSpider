package com.spiderybook.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.spiderybook.data.local.entity.HistoryEntity
import com.spiderybook.databinding.ItemHomeChildBinding

class HistoryAdapter(
    private var items: List<HistoryEntity>,
    private val onClick: (HistoryEntity) -> Unit,
    private val onSelectionModeChanged: (Boolean) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    var isSelectionMode = false
        private set
    val selectedUrls = mutableSetOf<String>()

    fun clearSelection() {
        isSelectionMode = false
        selectedUrls.clear()
        notifyDataSetChanged()
    }

    fun submitList(newItems: List<HistoryEntity>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemHomeChildBinding) :
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(item: HistoryEntity) {
            binding.tvTitle.text = item.name
            binding.imgPoster.load(item.posterUrl) {
                crossfade(true)
            }
            
            // CheckBox visibility and state
            binding.cbSelect.visibility = if (isSelectionMode) android.view.View.VISIBLE else android.view.View.GONE
            binding.cbSelect.isChecked = selectedUrls.contains(item.url)

            binding.root.setOnClickListener { 
                if (isSelectionMode) {
                    toggleSelection(item.url)
                } else {
                    onClick(item) 
                }
            }
            
            binding.root.setOnLongClickListener {
                if (!isSelectionMode) {
                    isSelectionMode = true
                    selectedUrls.add(item.url)
                    onSelectionModeChanged(true)
                    notifyDataSetChanged()
                }
                true
            }
        }
        
        private fun toggleSelection(url: String) {
            if (selectedUrls.contains(url)) {
                selectedUrls.remove(url)
            } else {
                selectedUrls.add(url)
            }
            
            // If user deselected everything, leave selection mode
            if (selectedUrls.isEmpty()) {
                isSelectionMode = false
                onSelectionModeChanged(false)
                notifyDataSetChanged()
            } else {
                notifyItemChanged(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemHomeChildBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}
