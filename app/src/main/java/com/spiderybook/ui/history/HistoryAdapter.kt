package com.spiderybook.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.spiderybook.data.local.entity.HistoryEntity
import com.spiderybook.databinding.ItemHomeChildBinding

class HistoryAdapter(
    private var items: List<HistoryEntity>,
    private val onClick: (HistoryEntity) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

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
            binding.root.setOnClickListener { onClick(item) }
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
